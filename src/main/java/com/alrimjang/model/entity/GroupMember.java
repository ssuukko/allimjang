package com.alrimjang.model.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"groupId", "userId"})
public class GroupMember {
    private String groupId;
    private String userId;
}
