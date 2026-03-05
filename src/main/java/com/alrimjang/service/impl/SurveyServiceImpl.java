package com.alrimjang.service.impl;

import com.alrimjang.mapper.SurveyMapper;
import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.entity.Survey;
import com.alrimjang.model.entity.SurveyOption;
import com.alrimjang.model.entity.SurveyQuestion;
import com.alrimjang.model.entity.SurveyResponse;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {

    private final SurveyMapper surveyMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void createSurvey(String title,
                             String description,
                             String startAt,
                             String endAt,
                             List<String> questionTitles,
                             List<String> questionTypes,
                             List<String> questionOptions,
                             String creatorUsername) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("설문 제목은 필수입니다.");
        }

        Users creator = userMapper.findByUsername(creatorUsername);
        if (creator == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }

        List<String> safeTitles = questionTitles == null ? new ArrayList<>() : questionTitles;
        List<String> safeTypes = questionTypes == null ? new ArrayList<>() : questionTypes;
        List<String> safeOptions = questionOptions == null ? new ArrayList<>() : questionOptions;
        if (safeTitles.isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 질문이 필요합니다.");
        }

        Survey survey = new Survey();
        survey.setId(UUID.randomUUID().toString());
        survey.setTitle(title.trim());
        survey.setDescription(trimToNull(description));
        survey.setStatus("PUBLISHED");
        survey.setStartAt(parseLocalDateTime(startAt));
        survey.setEndAt(parseLocalDateTime(endAt));
        survey.setCreatedByUserId(creator.getId());
        survey.setCreatedByUsername(creator.getUsername());
        survey.setCreatedByName(creator.getName());
        survey.setCreatedAt(LocalDateTime.now());
        surveyMapper.insertSurvey(survey);

        int createdQuestionCount = 0;
        for (int i = 0; i < safeTitles.size(); i++) {
            String qTitle = safeTitles.get(i) == null ? "" : safeTitles.get(i).trim();
            if (qTitle.isEmpty()) {
                continue;
            }
            String qType = i < safeTypes.size() && safeTypes.get(i) != null ? safeTypes.get(i).trim() : "TEXT";
            if (!Set.of("SINGLE", "MULTIPLE", "TEXT").contains(qType)) {
                qType = "TEXT";
            }

            SurveyQuestion question = new SurveyQuestion();
            question.setId(UUID.randomUUID().toString());
            question.setSurveyId(survey.getId());
            question.setSeq(i + 1);
            question.setType(qType);
            question.setTitle(qTitle);
            question.setIsRequired(true);
            question.setCreatedAt(LocalDateTime.now());
            surveyMapper.insertQuestion(question);
            createdQuestionCount++;

            if ("TEXT".equals(qType)) {
                continue;
            }

            String rawOptionText = i < safeOptions.size() ? safeOptions.get(i) : null;
            List<String> optionLines = splitOptionLines(rawOptionText);
            if (optionLines.isEmpty()) {
                throw new IllegalArgumentException("객관식 문항에는 최소 1개 이상의 선택지가 필요합니다.");
            }
            for (int optIndex = 0; optIndex < optionLines.size(); optIndex++) {
                SurveyOption option = new SurveyOption();
                option.setId(UUID.randomUUID().toString());
                option.setQuestionId(question.getId());
                option.setSeq(optIndex + 1);
                option.setOptionLabel(optionLines.get(optIndex));
                surveyMapper.insertOption(option);
            }
        }

        if (createdQuestionCount == 0) {
            throw new IllegalArgumentException("최소 1개 이상의 유효한 질문이 필요합니다.");
        }
    }

    @Override
    public List<Survey> getPublishedSurveys(String username) {
        Users user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }

        List<Survey> surveys = surveyMapper.findPublishedSurveys();
        for (Survey survey : surveys) {
            survey.setResponseCount(surveyMapper.countResponses(survey.getId()));
            boolean responded = surveyMapper.countResponsesBySurveyAndUser(survey.getId(), user.getId()) > 0;
            survey.setResponded(responded);
        }
        return surveys;
    }

    @Override
    public List<Survey> getAllSurveysForAdmin() {
        List<Survey> surveys = surveyMapper.findAllSurveys();
        for (Survey survey : surveys) {
            survey.setResponseCount(surveyMapper.countResponses(survey.getId()));
        }
        return surveys;
    }

    @Override
    public Survey getSurveyForAnswer(String surveyId, String username) {
        if (surveyId == null || surveyId.isBlank()) {
            throw new IllegalArgumentException("surveyId는 필수입니다.");
        }
        Users user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }

        Survey survey = surveyMapper.findSurveyById(surveyId);
        if (survey == null || !"PUBLISHED".equals(survey.getStatus())) {
            throw new IllegalArgumentException("설문을 찾을 수 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (survey.getStartAt() != null && now.isBefore(survey.getStartAt())) {
            throw new IllegalStateException("아직 시작 전 설문입니다.");
        }
        if (survey.getEndAt() != null && now.isAfter(survey.getEndAt())) {
            throw new IllegalStateException("마감된 설문입니다.");
        }

        List<SurveyQuestion> questions = surveyMapper.findQuestionsBySurveyId(surveyId);
        List<SurveyOption> options = surveyMapper.findOptionsBySurveyId(surveyId);
        Map<String, List<SurveyOption>> optionMap = options.stream()
                .collect(Collectors.groupingBy(SurveyOption::getQuestionId));
        for (SurveyQuestion question : questions) {
            question.setOptions(optionMap.getOrDefault(question.getId(), new ArrayList<>()));
        }

        survey.setQuestions(questions);
        survey.setResponseCount(surveyMapper.countResponses(surveyId));
        survey.setResponded(surveyMapper.countResponsesBySurveyAndUser(surveyId, user.getId()) > 0);
        return survey;
    }

    @Override
    public Survey getSurveyResultsForAdmin(String surveyId) {
        if (surveyId == null || surveyId.isBlank()) {
            throw new IllegalArgumentException("surveyId는 필수입니다.");
        }

        Survey survey = surveyMapper.findSurveyById(surveyId);
        if (survey == null) {
            throw new IllegalArgumentException("설문을 찾을 수 없습니다.");
        }

        List<SurveyQuestion> questions = surveyMapper.findQuestionsBySurveyId(surveyId);
        for (SurveyQuestion question : questions) {
            int answerCount = surveyMapper.countDistinctAnswersByQuestionId(question.getId());
            question.setAnswerCount(answerCount);

            if ("TEXT".equals(question.getType())) {
                question.setTextAnswers(surveyMapper.findTextAnswersByQuestionId(question.getId(), 20));
                continue;
            }

            List<SurveyOption> stats = surveyMapper.findOptionStatsByQuestionId(question.getId());
            for (SurveyOption option : stats) {
                int count = option.getVoteCount() == null ? 0 : option.getVoteCount();
                double rate = answerCount > 0 ? (count * 100.0) / answerCount : 0.0;
                option.setVoteRate(Math.round(rate * 10.0) / 10.0);
            }
            question.setOptions(stats);
        }

        survey.setQuestions(questions);
        survey.setResponseCount(surveyMapper.countResponses(surveyId));
        return survey;
    }

    @Override
    @Transactional
    public void submitResponse(String surveyId, String username, Map<String, String[]> parameterMap) {
        if (surveyId == null || surveyId.isBlank()) {
            throw new IllegalArgumentException("surveyId는 필수입니다.");
        }
        Users user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }

        Survey survey = getSurveyForAnswer(surveyId, username);
        if (Boolean.TRUE.equals(survey.getResponded())) {
            throw new IllegalStateException("이미 응답한 설문입니다.");
        }

        SurveyResponse response = new SurveyResponse();
        response.setId(UUID.randomUUID().toString());
        response.setSurveyId(surveyId);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getName());
        response.setSubmittedAt(LocalDateTime.now());
        surveyMapper.insertResponse(response);

        for (SurveyQuestion question : survey.getQuestions()) {
            String key = "q_" + question.getId();
            String[] values = parameterMap.get(key);

            if ("TEXT".equals(question.getType())) {
                String answerText = firstValue(values);
                if (Boolean.TRUE.equals(question.getIsRequired()) && (answerText == null || answerText.isBlank())) {
                    throw new IllegalArgumentException("필수 질문이 누락되었습니다.");
                }
                if (answerText != null && !answerText.isBlank()) {
                    surveyMapper.insertAnswerText(
                            UUID.randomUUID().toString(),
                            response.getId(),
                            surveyId,
                            question.getId(),
                            answerText.trim(),
                            LocalDateTime.now()
                    );
                }
                continue;
            }

            List<String> selectedOptionIds = normalizeOptionValues(values);
            if (Boolean.TRUE.equals(question.getIsRequired()) && selectedOptionIds.isEmpty()) {
                throw new IllegalArgumentException("필수 질문이 누락되었습니다.");
            }
            if ("SINGLE".equals(question.getType()) && selectedOptionIds.size() > 1) {
                throw new IllegalArgumentException("단일 선택 문항은 1개만 선택할 수 있습니다.");
            }

            Set<String> validOptionIds = question.getOptions().stream()
                    .map(SurveyOption::getId)
                    .collect(Collectors.toSet());
            for (String optionId : selectedOptionIds) {
                if (!validOptionIds.contains(optionId)) {
                    throw new IllegalArgumentException("유효하지 않은 선택지입니다.");
                }
                surveyMapper.insertAnswerOption(
                        UUID.randomUUID().toString(),
                        response.getId(),
                        surveyId,
                        question.getId(),
                        optionId,
                        LocalDateTime.now()
                );
            }
        }
    }

    private LocalDateTime parseLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다.");
        }
    }

    private List<String> splitOptionLines(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        return raw.lines()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String firstValue(String[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    private List<String> normalizeOptionValues(String[] values) {
        if (values == null || values.length == 0) {
            return new ArrayList<>();
        }
        Set<String> dedup = new HashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                dedup.add(value.trim());
            }
        }
        return new ArrayList<>(dedup);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
