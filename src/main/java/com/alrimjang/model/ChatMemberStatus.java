package com.alrimjang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMemberStatus {
    private String username;
    private String name;
    private boolean online;
    private boolean me;
}
