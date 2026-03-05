package com.alrimjang.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SurveyResponse {
    private String id;
    private String surveyId;
    private String userId;
    private String username;
    private String displayName;
    private LocalDateTime submittedAt;
}
