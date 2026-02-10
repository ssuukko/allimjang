package com.alrimjang.controller;

import com.alrimjang.model.entity.Notice;
import com.alrimjang.service.NoticeService; // 서비스 import 필요
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notices")
    public String notices(Model model) {

        List<Notice> noticeList = noticeService.getNoticeList();

        model.addAttribute("notices", noticeList);

        return "notices";
    }
}