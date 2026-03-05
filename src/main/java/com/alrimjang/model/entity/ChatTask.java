package com.alrimjang.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatTask {
    private String id;
    private String roomId;
    private String title;
    private String description;
    private LocalDateTime deadlineAt;
    private String status;
    private String createdByUserId;
    private String createdByUsername;
    private String createdByName;
    private String assigneeUserId;
    private String assigneeUsername;
    private String assigneeName;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime confirmedAt;
}
