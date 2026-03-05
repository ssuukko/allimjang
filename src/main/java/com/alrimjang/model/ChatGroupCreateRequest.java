package com.alrimjang.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatGroupCreateRequest {
    private String roomName;
    private List<String> memberUsernames;
}
