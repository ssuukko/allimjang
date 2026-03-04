package com.alrimjang.model;

import lombok.Data;

@Data
public class ChatSendRequest {
    private String roomId;
    private String content;
}
