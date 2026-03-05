package com.alrimjang.service;

import com.alrimjang.model.entity.Survey;

import java.util.List;
import java.util.Map;

public interface SurveyService {
    void createSurvey(String title,
                      String description,
                      String startAt,
                      String endAt,
                      List<String> questionTitles,
                      List<String> questionTypes,
                      List<String> questionOptions,
                      String creatorUsername);

    List<Survey> getPublishedSurveys(String username);
    List<Survey> getAllSurveysForAdmin();

    Survey getSurveyForAnswer(String surveyId, String username);
    Survey getSurveyResultsForAdmin(String surveyId);

    void submitResponse(String surveyId, String username, Map<String, String[]> parameterMap);
}
