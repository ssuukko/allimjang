package com.alrimjang.model.entity;

import lombok.Data;

@Data
public class SurveyOption {
    private String id;
    private String questionId;
    private Integer seq;
    private String optionLabel;
    private Integer voteCount;
    private Double voteRate;
}
