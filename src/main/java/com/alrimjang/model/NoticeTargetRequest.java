package com.alrimjang.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NoticeTargetRequest {
    private boolean targetAll;
    private List<String> targetRoles;
    private List<String> targetGroupIds;
}
