package com.alrimjang.service;

import com.alrimjang.model.entity.ChatMessage;

import java.util.List;

public interface ChatService {
    ChatMessage send(String roomId, String content, String username);
    List<ChatMessage> getRecent(String roomId, int limit);
    int getUnreadCount(String roomId, String username);
    void markAsRead(String roomId, String username);
}
