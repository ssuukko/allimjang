package com.alrimjang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatDirectRoomResponse {
    private String roomId;
    private String targetUsername;
    private String targetName;
}
