package com.alrimjang.service.impl;

import com.alrimjang.mapper.ChatMessageMapper;
import com.alrimjang.mapper.ChatReadMapper;
import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.entity.ChatMessage;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.ChatService;
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
    private final UserMapper userMapper;

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
    public List<ChatMessage> getRecent(String roomId, int limit) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }

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
        chatReadMapper.markAsRead(roomId, user.getId());
    }
}
