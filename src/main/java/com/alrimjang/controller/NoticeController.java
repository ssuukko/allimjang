package com.alrimjang.controller;

import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.common.PageRequest;
import com.alrimjang.model.common.PageResult;
import com.alrimjang.model.entity.Notice;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.NoticeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final UserMapper userMapper;

    private boolean isAdmin(Users user) {
        if (user == null) {
            return false;
        }
        return "admin".equalsIgnoreCase(user.getId())
                || "admin".equalsIgnoreCase(user.getUsername())
                || "ADMIN".equalsIgnoreCase(user.getRole());
    }

    @GetMapping("/notices")
    public String notices(Model model,
                          Principal principal,
                          @RequestParam(name = "keyword", defaultValue = "") String keyword,
                          @ModelAttribute("pageRequest") @Valid PageRequest pageRequest) {
        Users actor = userMapper.findByUsername(principal.getName());
        boolean actorIsAdmin = isAdmin(actor);

        PageResult<Notice> pageResult = noticeService.getNoticePage(actorIsAdmin, keyword, pageRequest);

        model.addAttribute("notices", pageResult.getItems());
        model.addAttribute("isAdmin", actorIsAdmin);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", pageResult.getCurrentPage());
        model.addAttribute("pageSize", pageResult.getSize());
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalCount", pageResult.getTotalCount());

        return "notices/list";
    }

    @PostMapping("/notices")
    public String createNotice(@ModelAttribute Notice notice, Principal principal) {

        Users users = userMapper.findByUsername(principal.getName());
        notice.setAuthorName(users.getName());
        notice.setAuthorId(users.getId());

        noticeService.createNotice(notice);

        return "redirect:/notices";
    }

    @GetMapping("/notices/{id}")
    public String noticeDetail(@PathVariable String id, Model model, Principal principal) {
        Users actor = userMapper.findByUsername(principal.getName());
        String actorId = actor.getId();
        String actorUsername = actor.getUsername();
        boolean actorIsAdmin = isAdmin(actor);
        Notice notice = noticeService.getNoticeById(id, actorIsAdmin);

        if(notice == null) {
            return "redirect:/notices";
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

        return "notices/form";
    }

    @GetMapping("/notices/{id}/edit")
    public String editNoticeForm(@PathVariable String id, Model model, Principal principal) {

        Users actor = userMapper.findByUsername(principal.getName());
        String actorId = actor.getId();
        String actorUsername = actor.getUsername();
        boolean actorIsAdmin = isAdmin(actor);
        Notice notice = noticeService.getNoticeForEditByActor(id, actorId, actorUsername, actorIsAdmin);

        model.addAttribute("notice", notice);
        model.addAttribute("idEdit", true);

        return "notices/form";
    }

    @PostMapping("/notices/{id}")
    public String updateNotice(@PathVariable String id,
                               @ModelAttribute Notice notice,
                               Principal principal) {

        Users actor = userMapper.findByUsername(principal.getName());
        String actorId = actor.getId();
        String actorUsername = actor.getUsername();
        boolean actorIsAdmin = isAdmin(actor);

        noticeService.updateNoticeByActor(id, notice, actorId, actorUsername, actorIsAdmin);

        return "redirect:/notices/" + id;
    }

    @PostMapping("/notices/{id}/delete")
    public String deleteNotice(@PathVariable String id, Principal principal) {
        Users actor = userMapper.findByUsername(principal.getName());
        String actorId = actor.getId();
        String actorUsername = actor.getUsername();
        boolean actorIsAdmin = isAdmin(actor);

        noticeService.deleteNoticeByActor(id, actorId, actorUsername, actorIsAdmin);
        return "redirect:/notices";
    }

    @PostMapping("/notices/{id}/hide")
    public String hideNotice(@PathVariable String id, Principal principal) {
        Users actor = userMapper.findByUsername(principal.getName());
        boolean actorIsAdmin = isAdmin(actor);

        noticeService.hideNoticeByActor(id, actorIsAdmin);
        return "redirect:/notices/" + id;
    }

    @PostMapping("/notices/{id}/unhide")
    public String unhideNotice(@PathVariable String id, Principal principal) {
        Users actor = userMapper.findByUsername(principal.getName());
        boolean actorIsAdmin = isAdmin(actor);

        noticeService.unhideNoticeByActor(id, actorIsAdmin);
        return "redirect:/notices/" + id;
    }
}
