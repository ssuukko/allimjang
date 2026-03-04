package com.alrimjang.controller;

import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.entity.NoticeReceipt;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.NoticeAudienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserNoticeReceiptApiController {

    private final NoticeAudienceService noticeAudienceService;
    private final UserMapper userMapper;

    @GetMapping("/me/receipts")
    public List<NoticeReceipt> myReceipts(@RequestParam(defaultValue = "false") boolean unreadOnly,
                                          Principal principal) {
        Users user = userMapper.findByUsername(principal.getName());
        return noticeAudienceService.findMyReceipts(user.getId(), unreadOnly);
    }
}
