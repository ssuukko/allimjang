package com.alrimjang.controller;

import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.entity.Notice;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.NoticeService; // 서비스 import 필요
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final UserMapper userMapper;

    @GetMapping("/notices")
    public String notices(Model model, Principal principal) {

        Users user = userMapper.findByUsername(principal.getName());
        List<Notice> noticeList = noticeService.getNoticeList();

        model.addAttribute("notices", noticeList);

        return "notices/list";
    }

    @PostMapping("/notices")
    public String createNotice(@ModelAttribute Notice notice, Principal principal) {
        if (principal != null) {
            Users user = userMapper.findByUsername(principal.getName());
            if (user != null) {
                notice.setAuthorName(user.getName());
                notice.setAuthorId(user.getId());
            }
        }
        noticeService.createNotice(notice);
        return "redirect:/notices";
    }

    @GetMapping("/notices/{id}")
    public String noticeDetail(@PathVariable String id, Model model) {
        Notice notice =  noticeService.getNoticeById(id);

        if(notice == null) {
            return "redirect:/notices";
        }

        model.addAttribute("notice", notice);

        return "notices/detail";
    }

    @GetMapping("/notices/new")
    public String showNoticeForm(Model model) {

        model.addAttribute("notice", new Notice());

        return "notices/form";
    }

    @GetMapping("/notices/{id}/edit")
    public String editNoticeForm(@PathVariable String id, Model model) {
        Notice notice = noticeService.getNoticeByIdForEdit(id);

        if(notice == null) {
            return "redirect:/notices";
        }

        model.addAttribute("notice", notice);
        model.addAttribute("idEdit", true);

        return "notices/form";
    }

    @PostMapping("/notices/{id}")
    public String updateOrDeleteNotice(@PathVariable String id,
                                       @RequestParam(name = "_method", required = false) String method,
                                       @ModelAttribute Notice notice) {
        if ("delete".equals(method)) {
            noticeService.deleteNotice(id);
            return "redirect:/notices";
        }

        notice.setId(id);
        noticeService.updateNotice(notice);

        return "redirect:/notices/" + id;
    }


}