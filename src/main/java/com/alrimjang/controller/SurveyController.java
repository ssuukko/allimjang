package com.alrimjang.controller;

import com.alrimjang.model.entity.Survey;
import com.alrimjang.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping("/surveys")
    public String surveyList(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        List<Survey> surveys = surveyService.getPublishedSurveys(principal.getName());
        model.addAttribute("surveys", surveys);
        return "surveys/list";
    }

    @GetMapping("/surveys/{surveyId}")
    public String surveyAnswerPage(@PathVariable String surveyId,
                                   Model model,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        try {
            Survey survey = surveyService.getSurveyForAnswer(surveyId, principal.getName());
            model.addAttribute("survey", survey);
            return "surveys/form";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/surveys";
        }
    }

    @PostMapping("/surveys/{surveyId}/responses")
    public String submitSurvey(@PathVariable String surveyId,
                               Principal principal,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        try {
            surveyService.submitResponse(surveyId, principal.getName(), request.getParameterMap());
            return "redirect:/surveys/" + surveyId + "?submitted=true";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/surveys/" + surveyId;
        }
    }
}
