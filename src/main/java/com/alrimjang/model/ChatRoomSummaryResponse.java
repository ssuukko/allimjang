package com.alrimjang.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomSummaryResponse {
    private String roomId;
    private String lastMessage;
    private String lastSenderName;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;

    private Boolean direct;
    private String roomTitle;
    private String partnerUsername;
    private String partnerName;
}
