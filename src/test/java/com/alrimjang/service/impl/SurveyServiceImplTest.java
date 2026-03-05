package com.alrimjang.service.impl;

import com.alrimjang.mapper.SurveyMapper;
import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.entity.Survey;
import com.alrimjang.model.entity.Users;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyServiceImplTest {

    @Mock
    private SurveyMapper surveyMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private SurveyServiceImpl surveyService;

    @Test
    void if_already_responded_when_submitResponse_then_throw() {
        String surveyId = "s1";
        Users user = Users.builder().id("u1").username("user").name("유저").build();

        Survey survey = new Survey();
        survey.setId(surveyId);
        survey.setStatus("PUBLISHED");
        survey.setStartAt(LocalDateTime.now().minusDays(1));
        survey.setEndAt(LocalDateTime.now().plusDays(1));

        when(userMapper.findByUsername("user")).thenReturn(user);
        when(surveyMapper.findSurveyById(surveyId)).thenReturn(survey);
        when(surveyMapper.findQuestionsBySurveyId(surveyId)).thenReturn(List.of());
        when(surveyMapper.findOptionsBySurveyId(surveyId)).thenReturn(List.of());
        when(surveyMapper.countResponses(surveyId)).thenReturn(1);
        when(surveyMapper.countResponsesBySurveyAndUser(surveyId, "u1")).thenReturn(1);

        assertThatThrownBy(() -> surveyService.submitResponse(surveyId, "user", Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 응답");

        verify(surveyMapper, never()).insertResponse(any());
    }

    @Test
    void if_missing_surveyId_when_submitResponse_then_throw() {
        assertThatThrownBy(() -> surveyService.submitResponse("", "user", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("surveyId는 필수");

        verify(userMapper, never()).findByUsername(eq("user"));
    }
}
