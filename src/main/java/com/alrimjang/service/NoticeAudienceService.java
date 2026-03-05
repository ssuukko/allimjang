package com.alrimjang.service;

import com.alrimjang.model.entity.NoticeReceipt;

import java.util.List;

public interface NoticeAudienceService {

    void configureTargets(String noticeId, boolean targetAll, List<String> roles, List<String> groupIds);

    int deliverNotice(String noticeId);

    void markAsRead(String noticeId, String userId);

    boolean hasReceipt(String noticeId, String userId);

    List<NoticeReceipt> findMyReceipts(String userId, boolean unreadOnly);
}
