package com.alrimjang.controller;

import com.alrimjang.mapper.ChatMessageMapper;
import com.alrimjang.mapper.ChatReadMapper;
import com.alrimjang.mapper.GroupMapper;
import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.ChatDirectRoomResponse;
import com.alrimjang.model.ChatMemberStatus;
import com.alrimjang.model.ChatRoomSummaryResponse;
import com.alrimjang.model.ChatSendRequest;
import com.alrimjang.model.ChatTaskCreateRequest;
import com.alrimjang.model.entity.ChatMessage;
import com.alrimjang.model.entity.ChatTask;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.ActiveUserTracker;
import com.alrimjang.service.ChatService;
import com.alrimjang.service.ChatRoomAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatRoomAccessService chatRoomAccessService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatReadMapper chatReadMapper;
    private final GroupMapper groupMapper;
    private final UserMapper userMapper;
    private final ActiveUserTracker activeUserTracker;

    @GetMapping("/chat")
    public String chatPage(@RequestParam(name = "room", required = false) String room,
                           @RequestParam(name = "partner", required = false) String partner) {
        if (room != null && !room.isBlank()) {
            String next = UriComponentsBuilder.fromPath("/chat/room")
                    .queryParam("room", room)
                    .queryParamIfPresent("partner", java.util.Optional.ofNullable(partner))
                    .build()
                    .toUriString();
            return "redirect:" + next;
        }
        return "chat/list";
    }

    @GetMapping("/chat/room")
    public String chatRoomPage() {
        return "chat/room";
    }

    @GetMapping("/api/chat/rooms/{roomId}/messages")
    @ResponseBody
    public List<ChatMessage> history(@PathVariable String roomId, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        chatRoomAccessService.assertAccessible(roomId, principal.getName());
        return chatService.getRecent(roomId, 100, principal.getName());
    }

    @GetMapping("/api/chat/rooms/{roomId}/tasks")
    @ResponseBody
    public Map<String, Object> tasks(@PathVariable String roomId, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        chatRoomAccessService.assertAccessible(roomId, principal.getName());

        List<ChatTask> tasks = chatService.getTasks(roomId);
        return Map.of(
                "serverNow", LocalDateTime.now(),
                "currentUsername", principal.getName(),
                "tasks", tasks
        );
    }

    @PostMapping("/api/chat/rooms/{roomId}/tasks")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ChatTask createTask(@PathVariable String roomId,
                               @Valid @RequestBody ChatTaskCreateRequest request,
                               Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        chatRoomAccessService.assertAccessible(roomId, principal.getName());
        return chatService.createTask(roomId, principal.getName(), request);
    }

    @PostMapping("/api/chat/rooms/{roomId}/tasks/{taskId}/complete")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeTask(@PathVariable String roomId,
                             @PathVariable String taskId,
                             Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        chatRoomAccessService.assertAccessible(roomId, principal.getName());
        chatService.completeTask(roomId, taskId, principal.getName());
    }

    @PostMapping("/api/chat/rooms/{roomId}/tasks/{taskId}/confirm")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmTask(@PathVariable String roomId,
                            @PathVariable String taskId,
                            Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        chatRoomAccessService.assertAccessible(roomId, principal.getName());
        chatService.confirmTask(roomId, taskId, principal.getName());
    }

    @GetMapping("/api/chat/rooms/{roomId}/unread-count")
    @ResponseBody
    public Map<String, Integer> unreadCount(@PathVariable String roomId, Principal principal) {
        if (principal == null) {
            return Map.of("count", 0);
        }
        chatRoomAccessService.assertAccessible(roomId, principal.getName());
        return Map.of("count", chatService.getUnreadCount(roomId, principal.getName()));
    }

    @GetMapping("/api/chat/members")
    @ResponseBody
    public List<ChatMemberStatus> members(Principal principal) {
        String me = principal != null ? principal.getName() : null;

        return userMapper.findAllUsers().stream()
                .map(user -> new ChatMemberStatus(
                        user.getUsername(),
                        user.getName(),
                        activeUserTracker.isActive(user.getUsername()),
                        me != null && user.getUsername().equals(me)
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/chat/me")
    @ResponseBody
    public Map<String, String> me(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        Users me = userMapper.findByUsername(principal.getName());
        if (me == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        return Map.of(
                "id", me.getId(),
                "username", me.getUsername(),
                "name", me.getName()
        );
    }

    @GetMapping("/api/chat/rooms")
    @ResponseBody
    public List<ChatRoomSummaryResponse> rooms(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        Users me = userMapper.findByUsername(principal.getName());
        if (me == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        String myToken = normalizeRoomToken(me.getUsername());
        List<Users> allUsers = userMapper.findAllUsers();
        Map<String, String> myGroups = groupMapper.findByUserId(me.getId()).stream()
                .collect(Collectors.toMap(
                        g -> g.getId(),
                        g -> g.getName() + (g.getCode() != null && !g.getCode().isBlank() ? " (" + g.getCode() + ")" : ""),
                        (a, b) -> a
                ));

        return chatMessageMapper.findRoomSummaries(me.getId(), myToken).stream()
                .map(summary -> enrichRoomSummary(summary, myToken, allUsers, myGroups))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/chat/direct-room/{targetUsername}")
    @ResponseBody
    public ChatDirectRoomResponse getDirectRoom(@PathVariable String targetUsername, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Users me = userMapper.findByUsername(principal.getName());
        Users target = userMapper.findByUsername(targetUsername);

        if (me == null || target == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        if (me.getUsername().equals(target.getUsername())) {
            throw new IllegalStateException("자기 자신과는 1:1 채팅을 시작할 수 없습니다.");
        }

        String roomId = buildDirectRoomId(me.getUsername(), target.getUsername());
        return new ChatDirectRoomResponse(roomId, target.getUsername(), target.getName());
    }

    @GetMapping("/api/chat/rooms/{roomId}/partner")
    @ResponseBody
    public ChatDirectRoomResponse partner(@PathVariable String roomId, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        chatRoomAccessService.assertAccessible(roomId, principal.getName());
        if (!chatRoomAccessService.isDirectRoom(roomId)) {
            throw new IllegalArgumentException("1:1 채팅방이 아닙니다.");
        }

        Users me = userMapper.findByUsername(principal.getName());
        if (me == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        String[] parts = roomId.split("__");
        if (parts.length != 3) {
            throw new IllegalArgumentException("유효하지 않은 채팅방입니다.");
        }

        String myToken = normalizeRoomToken(me.getUsername());
        String partnerToken = myToken.equals(parts[1]) ? parts[2] : parts[1];
        Users partner = userMapper.findAllUsers().stream()
                .filter(u -> normalizeRoomToken(u.getUsername()).equals(partnerToken))
                .findFirst()
                .orElse(null);

        if (partner == null) {
            return new ChatDirectRoomResponse(roomId, partnerToken, partnerToken);
        }
        return new ChatDirectRoomResponse(roomId, partner.getUsername(), partner.getName());
    }

    @PostMapping("/api/chat/rooms/{roomId}/read")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable String roomId, Principal principal) {
        if (principal == null) {
            return;
        }
        chatRoomAccessService.assertAccessible(roomId, principal.getName());
        chatService.markAsRead(roomId, principal.getName());
    }

    @GetMapping("/api/chat/rooms/{roomId}/read-status")
    @ResponseBody
    public Map<String, Object> readStatus(@PathVariable String roomId, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        chatRoomAccessService.assertAccessible(roomId, principal.getName());

        Users me = userMapper.findByUsername(principal.getName());
        if (me == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("partnerLastReadAt", null);

        if (!chatRoomAccessService.isDirectRoom(roomId)) {
            return result;
        }

        String[] parts = roomId.split("__");
        if (parts.length != 3) {
            return result;
        }

        String myToken = normalizeRoomToken(me.getUsername());
        String partnerToken = myToken.equals(parts[1]) ? parts[2] : parts[1];
        Users partner = userMapper.findAllUsers().stream()
                .filter(u -> normalizeRoomToken(u.getUsername()).equals(partnerToken))
                .findFirst()
                .orElse(null);

        if (partner == null) {
            return result;
        }

        result.put("partnerLastReadAt", chatReadMapper.findLastReadAt(roomId, partner.getId()));
        return result;
    }

    @MessageMapping("/chat/send")
    public void send(ChatSendRequest request, Principal principal) {
        if (principal == null) {
            return;
        }
        if (request == null || request.getRoomId() == null || request.getContent() == null) {
            return;
        }
        chatRoomAccessService.assertAccessible(request.getRoomId(), principal.getName());

        ChatMessage saved = chatService.send(
                request.getRoomId(),
                request.getContent(),
                principal.getName()
        );

        messagingTemplate.convertAndSend("/topic/chat." + request.getRoomId(), saved);
    }

    private String buildDirectRoomId(String usernameA, String usernameB) {
        String a = normalizeRoomToken(usernameA);
        String b = normalizeRoomToken(usernameB);
        if (a.compareTo(b) < 0) {
            return "dm__" + a + "__" + b;
        }
        return "dm__" + b + "__" + a;
    }

    private String normalizeRoomToken(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
    }

    private ChatRoomSummaryResponse enrichRoomSummary(ChatRoomSummaryResponse summary,
                                                      String myToken,
                                                      List<Users> allUsers,
                                                      Map<String, String> myGroups) {
        String roomId = summary.getRoomId();
        boolean direct = chatRoomAccessService.isDirectRoom(roomId);
        boolean group = chatRoomAccessService.isGroupRoom(roomId);
        summary.setDirect(direct);
        summary.setRoomTitle(direct ? "1:1 채팅" : roomId);
        summary.setPartnerUsername(null);
        summary.setPartnerName(null);

        if (group) {
            String groupId = chatRoomAccessService.extractGroupId(roomId);
            String groupName = groupId == null ? null : myGroups.get(groupId);
            summary.setRoomTitle(groupName == null ? "그룹 채팅" : "그룹 - " + groupName);
            return summary;
        }

        if (!direct) {
            return summary;
        }

        String[] parts = roomId.split("__");
        if (parts.length != 3) {
            summary.setRoomTitle(roomId);
            return summary;
        }

        String partnerToken = myToken.equals(parts[1]) ? parts[2] : parts[1];
        Users partner = allUsers.stream()
                .filter(u -> normalizeRoomToken(u.getUsername()).equals(partnerToken))
                .findFirst()
                .orElse(null);

        if (partner == null) {
            summary.setPartnerUsername(partnerToken);
            summary.setPartnerName(partnerToken);
            summary.setRoomTitle("1:1 - @" + partnerToken);
            return summary;
        }

        summary.setPartnerUsername(partner.getUsername());
        summary.setPartnerName(partner.getName());
        summary.setRoomTitle("1:1 - " + partner.getName() + " (@" + partner.getUsername() + ")");
        return summary;
    }
}
