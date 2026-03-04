package com.alrimjang.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private String id;
    private String roomId;
    private String senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
}
