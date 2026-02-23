package com.alrimjang.service.impl;

import com.alrimjang.mapper.NoticeAudienceMapper;
import com.alrimjang.model.entity.NoticeReceipt;
import com.alrimjang.model.entity.NoticeTarget;
import com.alrimjang.model.entity.NoticeTargetType;
import com.alrimjang.service.NoticeAudienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeAudienceServiceImpl implements NoticeAudienceService {

    private final NoticeAudienceMapper noticeAudienceMapper;

    @Override
    @Transactional
    public void configureTargets(String noticeId, boolean targetAll, List<String> roles, List<String> groupIds) {
        if (noticeAudienceMapper.countNotice(noticeId) == 0) {
            throw new IllegalArgumentException("공지사항이 없습니다.");
        }

        noticeAudienceMapper.deleteTargetsByNoticeId(noticeId);

        if (targetAll) {
            insertTarget(noticeId, NoticeTargetType.ALL, null);
            return;
        }

        Set<String> roleSet = normalize(roles);
        Set<String> groupSet = normalize(groupIds);

        if (roleSet.isEmpty() && groupSet.isEmpty()) {
            throw new IllegalArgumentException("대상이 비어 있습니다.");
        }

        for (String role : roleSet) {
            insertTarget(noticeId, NoticeTargetType.ROLE, role);
        }

        for (String groupId : groupSet) {
            insertTarget(noticeId, NoticeTargetType.GROUP, groupId);
        }
    }

    @Override
    @Transactional
    public int deliverNotice(String noticeId) {
        if (noticeAudienceMapper.countNotice(noticeId) == 0) {
            throw new IllegalArgumentException("공지사항이 없습니다.");
        }

        List<String> recipientUserIds = noticeAudienceMapper.findRecipientUserIds(noticeId);
        if (recipientUserIds.isEmpty()) {
            return 0;
        }

        LocalDateTime deliveredAt = LocalDateTime.now();
        List<NoticeReceipt> receipts = recipientUserIds.stream()
                .map(userId -> NoticeReceipt.builder()
                        .id(UUID.randomUUID().toString())
                        .noticeId(noticeId)
                        .userId(userId)
                        .deliveredAt(deliveredAt)
                        .build())
                .collect(Collectors.toList());

        return noticeAudienceMapper.insertReceipts(receipts);
    }

    @Override
    @Transactional
    public void markAsRead(String noticeId, String userId) {
        int updatedCount = noticeAudienceMapper.markAsRead(noticeId, userId);
        if (updatedCount == 0) {
            throw new IllegalArgumentException("읽음 처리할 대상이 없습니다.");
        }
    }

    @Override
    public List<NoticeReceipt> findMyReceipts(String userId, boolean unreadOnly) {
        return noticeAudienceMapper.findReceiptsByUser(userId, unreadOnly);
    }

    private void insertTarget(String noticeId, NoticeTargetType type, String targetValue) {
        NoticeTarget target = new NoticeTarget();
        target.setId(UUID.randomUUID().toString());
        target.setNoticeId(noticeId);
        target.setNoticeTargetType(type);
        target.setTargetValue(targetValue);
        noticeAudienceMapper.insertTarget(target);
    }

    private Set<String> normalize(List<String> values) {
        if (values == null) {
            return Collections.emptySet();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
