package com.alrimjang.controller;

import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.NoticeTargetRequest;
import com.alrimjang.model.entity.Users;
import com.alrimjang.service.NoticeAudienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeAudienceApiController {

    private final NoticeAudienceService noticeAudienceService;
    private final UserMapper userMapper;

    @PostMapping("/{noticeId}/targets")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void configureTargets(@PathVariable String noticeId,
                                 @RequestBody NoticeTargetRequest request,
                                 Authentication authentication) {
        requireAdmin(authentication);
        noticeAudienceService.configureTargets(
                noticeId,
                request.isTargetAll(),
                request.getTargetRoles(),
                request.getTargetGroupIds()
        );
    }

    @PostMapping("/{noticeId}/deliver")
    public Map<String, Integer> deliver(@PathVariable String noticeId, Authentication authentication) {
        requireAdmin(authentication);
        return Map.of("deliveredCount", noticeAudienceService.deliverNotice(noticeId));
    }

    @PostMapping("/{noticeId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable String noticeId, Principal principal) {
        Users user = userMapper.findByUsername(principal.getName());
        noticeAudienceService.markAsRead(noticeId, user.getId());
    }

    private void requireAdmin(Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin) {
            throw new IllegalStateException("관리자 권한이 필요합니다.");
        }
    }
}
