package com.alrimjang.model.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeReceiptResponse {
    private String noticeId;
    private String noticeTitle;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private boolean read;
}
