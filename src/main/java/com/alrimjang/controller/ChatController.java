package com.alrimjang.controller;

import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.ChatDirectRoomResponse;
import com.alrimjang.model.ChatMemberStatus;
import com.alrimjang.model.ChatSendRequest;
import com.alrimjang.model.entity.ChatMessage;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.ActiveUserTracker;
import com.alrimjang.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserMapper userMapper;
    private final ActiveUserTracker activeUserTracker;

    @GetMapping("/chat")
    public String chatPage() {
        return "chat/room";
    }

    @GetMapping("/api/chat/rooms/{roomId}/messages")
    @ResponseBody
    public List<ChatMessage> history(@PathVariable String roomId) {
        return chatService.getRecent(roomId, 100);
    }

    @GetMapping("/api/chat/rooms/{roomId}/unread-count")
    @ResponseBody
    public Map<String, Integer> unreadCount(@PathVariable String roomId, Principal principal) {
        if (principal == null) {
            return Map.of("count", 0);
        }
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

    @PostMapping("/api/chat/rooms/{roomId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable String roomId, Principal principal) {
        if (principal == null) {
            return;
        }
        chatService.markAsRead(roomId, principal.getName());
    }

    @MessageMapping("/chat/send")
    public void send(ChatSendRequest request, Principal principal) {
        if (principal == null) {
            return;
        }
        if (request == null || request.getRoomId() == null || request.getContent() == null) {
            return;
        }

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
}
