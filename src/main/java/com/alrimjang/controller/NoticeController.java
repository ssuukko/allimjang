package com.alrimjang.controller;

import com.alrimjang.mapper.GroupMapper;
import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.common.PageRequest;
import com.alrimjang.model.common.PageResult;
import com.alrimjang.model.entity.Notice;
import com.alrimjang.model.entity.NoticeReceipt;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.NoticeAudienceService;
import com.alrimjang.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeAudienceService noticeAudienceService;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    @Value("${app.notice.available-roles}")
    private List<String> availableRoles;

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    @GetMapping("/notices")
    public String notices(Model model,
                          Authentication authentication,
                          Principal principal,
                          @RequestParam(name = "keyword", defaultValue = "") String keyword,
                          @RequestParam(name = "searchType", defaultValue = "all") String searchType,
                          @ModelAttribute("pageRequest") @Valid PageRequest pageRequest) {
        boolean actorIsAdmin = isAdmin(authentication);

        PageResult<Notice> pageResult = noticeService.getNoticePage(actorIsAdmin, keyword, searchType, pageRequest);
        Set<String> readNoticeIds = Collections.emptySet();
        if (principal != null) {
            Users actor = userMapper.findByUsername(principal.getName());
            if (actor != null) {
                readNoticeIds = noticeAudienceService.findMyReceipts(actor.getId(), false).stream()
                        .filter(NoticeReceipt::isRead)
                        .map(NoticeReceipt::getNoticeId)
                        .collect(Collectors.toSet());
            }
        }

        model.addAttribute("notices", pageResult.getItems());
        model.addAttribute("readNoticeIds", readNoticeIds);
        model.addAttribute("isAdmin", actorIsAdmin);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("currentPage", pageResult.getCurrentPage());
        model.addAttribute("pageSize", pageResult.getSize());
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalCount", pageResult.getTotalCount());

        return "notices/list";
    }

    @PostMapping("/notices")
    public String createNotice(@ModelAttribute Notice notice,
                               @RequestParam(name = "targetAll", defaultValue = "false") boolean targetAll,
                               @RequestParam(name = "targetRoles", required = false) List<String> targetRoles,
                               @RequestParam(name = "targetGroupIds", required = false) List<String> targetGroupIds,
                               Principal principal) {

        Users users = userMapper.findByUsername(principal.getName());
        notice.setAuthorName(users.getName());
        notice.setAuthorId(users.getId());

        noticeService.createNotice(notice);
        noticeAudienceService.configureTargets(notice.getId(), targetAll, targetRoles, targetGroupIds);
        noticeAudienceService.deliverNotice(notice.getId());

        return "redirect:/notices";
    }

    @GetMapping("/notices/{id}")
    public String noticeDetail(@PathVariable String id, Model model, Principal principal, Authentication authentication) {
        Users actor = userMapper.findByUsername(principal.getName());
        String actorId = actor.getId();
        String actorUsername = actor.getUsername();
        boolean actorIsAdmin = isAdmin(authentication);
        Notice notice = noticeService.getNoticeById(id, actorIsAdmin);

        if (notice == null) {
            return "redirect:/notices";
        }

        try {
            noticeAudienceService.markAsRead(id, actorId);
        } catch (IllegalArgumentException ignored) {
            // 대상자가 아니거나 이미 읽음 처리된 경우는 무시
        }

        model.addAttribute("notice", notice);
        model.addAttribute("canEdit", noticeService.canEdit(notice, actorId, actorUsername, actorIsAdmin));
        model.addAttribute("canDelete", noticeService.canDelete(notice, actorId, actorUsername, actorIsAdmin));
        model.addAttribute("canHide", noticeService.canHide(notice, actorIsAdmin));
        model.addAttribute("canUnhide", noticeService.canUnhide(notice, actorIsAdmin));

        return "notices/detail";
    }

    @GetMapping("/notices/new")
    public String showNoticeForm(Model model) {

        model.addAttribute("notice", new Notice());
        model.addAttribute("availableRoles", availableRoles);
        model.addAttribute("availableGroups", groupMapper.findAll());

        return "notices/form";
    }

    @GetMapping("/notices/{id}/edit")
    public String editNoticeForm(@PathVariable String id, Model model, Principal principal, Authentication authentication) {

        Users actor = userMapper.findByUsername(principal.getName());
        String actorId = actor.getId();
        String actorUsername = actor.getUsername();
        boolean actorIsAdmin = isAdmin(authentication);
        Notice notice = noticeService.getNoticeForEditByActor(id, actorId, actorUsername, actorIsAdmin);

        model.addAttribute("notice", notice);
        model.addAttribute("idEdit", true);
        model.addAttribute("availableRoles", availableRoles);
        model.addAttribute("availableGroups", groupMapper.findAll());

        return "notices/form";
    }

    @PostMapping("/notices/{id}")
    public String updateNotice(@PathVariable String id,
                               @ModelAttribute Notice notice,
                               Principal principal,
                               Authentication authentication) {

        Users actor = userMapper.findByUsername(principal.getName());
        String actorId = actor.getId();
        String actorUsername = actor.getUsername();
        boolean actorIsAdmin = isAdmin(authentication);

        noticeService.updateNoticeByActor(id, notice, actorId, actorUsername, actorIsAdmin);

        return "redirect:/notices/" + id;
    }

    @PostMapping("/notices/{id}/delete")
    public String deleteNotice(@PathVariable String id, Principal principal, Authentication authentication) {
        Users actor = userMapper.findByUsername(principal.getName());
        String actorId = actor.getId();
        String actorUsername = actor.getUsername();
        boolean actorIsAdmin = isAdmin(authentication);

        noticeService.deleteNoticeByActor(id, actorId, actorUsername, actorIsAdmin);
        return "redirect:/notices";
    }

    @PostMapping("/notices/{id}/hide")
    public String hideNotice(@PathVariable String id, Authentication authentication) {
        boolean actorIsAdmin = isAdmin(authentication);

        noticeService.hideNoticeByActor(id, actorIsAdmin);
        return "redirect:/notices/" + id;
    }

    @PostMapping("/notices/{id}/unhide")
    public String unhideNotice(@PathVariable String id, Authentication authentication) {
        boolean actorIsAdmin = isAdmin(authentication);

        noticeService.unhideNoticeByActor(id, actorIsAdmin);
        return "redirect:/notices/" + id;
    }
}
