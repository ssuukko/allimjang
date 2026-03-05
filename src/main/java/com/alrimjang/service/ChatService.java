package com.alrimjang.service;

import com.alrimjang.model.ChatTaskCreateRequest;
import com.alrimjang.model.entity.ChatMessage;
import com.alrimjang.model.entity.ChatTask;

import java.util.List;

public interface ChatService {
    ChatMessage send(String roomId, String content, String username);
    List<ChatMessage> getRecent(String roomId, int limit, String username);
    int getUnreadCount(String roomId, String username);
    void markAsRead(String roomId, String username);
    List<ChatTask> getTasks(String roomId);
    ChatTask createTask(String roomId, String username, ChatTaskCreateRequest request);
    void completeTask(String roomId, String taskId, String username);
    void confirmTask(String roomId, String taskId, String username);
}
