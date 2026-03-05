package com.alrimjang.service.impl;

import com.alrimjang.mapper.ChatMessageMapper;
import com.alrimjang.mapper.ChatReadMapper;
import com.alrimjang.mapper.ChatTaskMapper;
import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.ChatTaskCreateRequest;
import com.alrimjang.model.entity.ChatMessage;
import com.alrimjang.model.entity.ChatTask;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.ChatService;
import com.alrimjang.service.ChatRoomAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatReadMapper chatReadMapper;
    private final ChatTaskMapper chatTaskMapper;
    private final UserMapper userMapper;
    private final ChatRoomAccessService chatRoomAccessService;

    @Override
    public ChatMessage send(String roomId, String content, String username) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 필수입니다.");
        }

        Users user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        chatRoomAccessService.assertAccessible(roomId, user.getUsername());

        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID().toString());
        message.setRoomId(roomId);
        message.setSenderId(user.getId());
        message.setSenderName(user.getName());
        message.setContent(content.trim());
        message.setCreatedAt(LocalDateTime.now());

        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<ChatMessage> getRecent(String roomId, int limit, String username) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        chatRoomAccessService.assertAccessible(roomId, username);

        int safeLimit = Math.max(1, Math.min(limit, 200));
        List<ChatMessage> list = chatMessageMapper.findRecentByRoomId(roomId, safeLimit);
        Collections.reverse(list);
        return list;
    }

    @Override
    public int getUnreadCount(String roomId, String username) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        Users user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        chatRoomAccessService.assertAccessible(roomId, user.getUsername());
        return chatMessageMapper.countUnreadByRoomAndUser(roomId, user.getId());
    }

    @Override
    public void markAsRead(String roomId, String username) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        Users user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        chatRoomAccessService.assertAccessible(roomId, user.getUsername());
        chatReadMapper.markAsRead(roomId, user.getId());
    }

    @Override
    public List<ChatTask> getTasks(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        validateDirectRoom(roomId);
        return chatTaskMapper.findByRoomId(roomId);
    }

    @Override
    public ChatTask createTask(String roomId, String username, ChatTaskCreateRequest request) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        if (request == null) {
            throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
        }
        if (request.getDeadlineAt() == null) {
            throw new IllegalArgumentException("마감일시는 필수입니다.");
        }
        if (request.getDeadlineAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("마감일시는 현재 시각 이후여야 합니다.");
        }
        validateDirectRoom(roomId);

        Users creator = userMapper.findByUsername(username);
        if (creator == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }

        String assigneeUsername = request.getAssigneeUsername() != null ? request.getAssigneeUsername().trim() : "";
        Users assignee = userMapper.findByUsername(assigneeUsername);
        if (assignee == null) {
            throw new IllegalArgumentException("담당자 정보를 찾을 수 없습니다.");
        }
        if (!chatRoomAccessService.canAccess(roomId, creator.getUsername())
                || !chatRoomAccessService.canAccess(roomId, assignee.getUsername())) {
            throw new IllegalArgumentException("DM 방 참여자에게만 할 일을 지정할 수 있습니다.");
        }

        ChatTask task = new ChatTask();
        task.setId(UUID.randomUUID().toString());
        task.setRoomId(roomId);
        task.setTitle(request.getTitle().trim());
        task.setDescription(trimToNull(request.getDescription()));
        task.setDeadlineAt(request.getDeadlineAt());
        task.setStatus("PENDING");
        task.setCreatedByUserId(creator.getId());
        task.setCreatedByUsername(creator.getUsername());
        task.setCreatedByName(creator.getName());
        task.setAssigneeUserId(assignee.getId());
        task.setAssigneeUsername(assignee.getUsername());
        task.setAssigneeName(assignee.getName());
        task.setCreatedAt(LocalDateTime.now());
        task.setCompletedAt(null);
        task.setConfirmedAt(null);

        chatTaskMapper.insert(task);
        return task;
    }

    @Override
    public void completeTask(String roomId, String taskId, String username) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId는 필수입니다.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username은 필수입니다.");
        }
        validateDirectRoom(roomId);
        if (!chatRoomAccessService.canAccess(roomId, username)) {
            throw new IllegalStateException("해당 DM 방 참여자만 접근할 수 있습니다.");
        }

        ChatTask task = chatTaskMapper.findByIdAndRoomId(taskId, roomId);
        if (task == null) {
            throw new IllegalArgumentException("할 일을 찾을 수 없습니다.");
        }

        if (!username.equals(task.getAssigneeUsername())) {
            throw new IllegalStateException("담당자만 완료 처리할 수 있습니다.");
        }

        int updated = chatTaskMapper.markDone(taskId, roomId, LocalDateTime.now());
        if (updated == 0) {
            throw new IllegalStateException("이미 완료되었거나 확인된 할 일입니다.");
        }
    }

    @Override
    public void confirmTask(String roomId, String taskId, String username) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId는 필수입니다.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username은 필수입니다.");
        }
        validateDirectRoom(roomId);
        if (!chatRoomAccessService.canAccess(roomId, username)) {
            throw new IllegalStateException("해당 DM 방 참여자만 접근할 수 있습니다.");
        }

        ChatTask task = chatTaskMapper.findByIdAndRoomId(taskId, roomId);
        if (task == null) {
            throw new IllegalArgumentException("할 일을 찾을 수 없습니다.");
        }
        if (!isTaskCreator(task, username)) {
            throw new IllegalStateException("할 일을 지정한 사람만 확인 처리할 수 있습니다.");
        }

        int updated = chatTaskMapper.markConfirmed(taskId, roomId, LocalDateTime.now());
        if (updated == 0) {
            throw new IllegalStateException("완료된 할 일만 확인할 수 있습니다.");
        }
    }

    private void validateDirectRoom(String roomId) {
        if (!chatRoomAccessService.isDirectRoom(roomId) || roomId.split("__").length != 3) {
            throw new IllegalArgumentException("할 일 기능은 1:1 DM에서만 사용할 수 있습니다.");
        }
    }

    private boolean isTaskCreator(ChatTask task, String username) {
        if (username.equals(task.getCreatedByUsername())) {
            return true;
        }
        Users user = userMapper.findByUsername(username);
        return user != null && user.getId().equals(task.getCreatedByUserId());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
