package com.alrimjang.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeTarget {
    private String id;
    private String noticeId;
    private NoticeTargetType noticeTargetType;
    private String targetValue;
    private LocalDateTime createdAt;
}
