package com.alrimjang.model.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SurveyQuestion {
    private String id;
    private String surveyId;
    private Integer seq;
    private String type;
    private String title;
    private Boolean isRequired;
    private LocalDateTime createdAt;
    private Integer answerCount;

    private List<SurveyOption> options = new ArrayList<>();
    private List<String> textAnswers = new ArrayList<>();
}
