package com.alrimjang.model.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
    private String id;
    private String orgId;
    private String code;
    private String name;
    private String type;
    private LocalDateTime createdAt;
}
