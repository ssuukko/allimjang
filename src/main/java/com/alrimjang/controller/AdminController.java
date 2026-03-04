package com.alrimjang.controller;

import com.alrimjang.mapper.GroupMapper;
import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.entity.Notice;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.NoticeAudienceService;
import com.alrimjang.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final NoticeService noticeService;
    private final NoticeAudienceService noticeAudienceService;

    @Value("${app.notice.available-roles}")
    private List<String> availableRoles;

    @GetMapping("/admin")
    public String adminIndex() {
        return "admin/index";
    }

    @GetMapping("/admin/noticewriters")
    public String noticeWriterPage(Model model) {
        model.addAttribute("users", userMapper.findAllUsers());
        return "admin/notice-writers";
    }

    @GetMapping("/admin/users")
    public String usersPage(Model model) {
        model.addAttribute("users", userMapper.findAllUsers());
        return "admin/users";
    }

    @GetMapping("/admin/notifications/new")
    public String notificationWritePage(Model model) {
        model.addAttribute("notice", new Notice());
        model.addAttribute("availableRoles", availableRoles);
        model.addAttribute("availableGroups", groupMapper.findAll());
        return "admin/notification-form";
    }

    @PostMapping("/admin/noticewriters")
    @Transactional
    public String updateNoticeWriters(@RequestParam(name = "writerUserIds", required = false) List<String> writerUserIds) {
        Set<String> grantedUserIds = writerUserIds == null ? new HashSet<>() : new HashSet<>(writerUserIds);
        List<Users> users = userMapper.findAllUsers();

        for (Users user : users) {
            String role = user.getRole() == null ? "" : user.getRole();
            if ("ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role)) {
                continue;
            }
            userMapper.updateNoticeWritePermission(user.getId(), grantedUserIds.contains(user.getId()));
        }
        return "redirect:/admin/noticewriters?saved=true";
    }

    @PostMapping("/admin/notifications")
    @Transactional
    public String createNotification(@ModelAttribute Notice notice,
                                     @RequestParam(name = "targetAll", defaultValue = "false") boolean targetAll,
                                     @RequestParam(name = "targetRoles", required = false) List<String> targetRoles,
                                     @RequestParam(name = "targetGroupIds", required = false) List<String> targetGroupIds,
                                     Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Users actor = userMapper.findByUsername(principal.getName());
        if (actor == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }

        notice.setAuthorId(actor.getId());
        notice.setAuthorName(actor.getName());
        if (notice.getIsImportant() == null) {
            notice.setIsImportant(false);
        }

        noticeService.createNotice(notice);
        noticeAudienceService.configureTargets(notice.getId(), targetAll, targetRoles, targetGroupIds);
        noticeAudienceService.deliverNotice(notice.getId());
        return "redirect:/admin/notifications/new?saved=true";
    }
}
