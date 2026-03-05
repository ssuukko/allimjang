package com.alrimjang.model.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Survey {
    private String id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String createdByUserId;
    private String createdByUsername;
    private String createdByName;
    private LocalDateTime createdAt;

    private Integer responseCount;
    private Boolean responded;

    private List<SurveyQuestion> questions = new ArrayList<>();
}
