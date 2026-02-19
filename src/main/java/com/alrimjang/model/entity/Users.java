package com.alrimjang.model.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {
    private String id;
    private String username;
    private String password;
    private String name;
    private String role;
    private LocalDateTime createdAt;
    private Boolean enabled;
}