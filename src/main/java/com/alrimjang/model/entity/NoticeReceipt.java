package com.alrimjang.model.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeReceipt {
    private String id;
    private String noticeId;
    private String userId;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;

    private String noticeTitle;

    public boolean isRead() {
        return readAt != null;
    }
}
