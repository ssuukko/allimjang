package com.alrimjang.mapper;

import com.alrimjang.model.entity.Survey;
import com.alrimjang.model.entity.SurveyOption;
import com.alrimjang.model.entity.SurveyQuestion;
import com.alrimjang.model.entity.SurveyResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SurveyMapper {
    int insertSurvey(Survey survey);

    int insertQuestion(SurveyQuestion question);

    int insertOption(SurveyOption option);

    List<Survey> findPublishedSurveys();
    List<Survey> findAllSurveys();

    Survey findSurveyById(@Param("surveyId") String surveyId);

    List<SurveyQuestion> findQuestionsBySurveyId(@Param("surveyId") String surveyId);

    List<SurveyOption> findOptionsBySurveyId(@Param("surveyId") String surveyId);

    int countResponses(@Param("surveyId") String surveyId);

    int countResponsesBySurveyAndUser(@Param("surveyId") String surveyId, @Param("userId") String userId);
    int countDistinctAnswersByQuestionId(@Param("questionId") String questionId);
    List<SurveyOption> findOptionStatsByQuestionId(@Param("questionId") String questionId);
    List<String> findTextAnswersByQuestionId(@Param("questionId") String questionId, @Param("limit") int limit);

    int insertResponse(SurveyResponse response);

    int insertAnswerOption(@Param("id") String id,
                           @Param("responseId") String responseId,
                           @Param("surveyId") String surveyId,
                           @Param("questionId") String questionId,
                           @Param("optionId") String optionId,
                           @Param("createdAt") LocalDateTime createdAt);

    int insertAnswerText(@Param("id") String id,
                         @Param("responseId") String responseId,
                         @Param("surveyId") String surveyId,
                         @Param("questionId") String questionId,
                         @Param("answerText") String answerText,
                         @Param("createdAt") LocalDateTime createdAt);
}
