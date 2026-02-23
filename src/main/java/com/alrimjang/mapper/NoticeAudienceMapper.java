package com.alrimjang.mapper;

import com.alrimjang.model.entity.NoticeReceipt;
import com.alrimjang.model.entity.NoticeTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeAudienceMapper {

    int countNotice(@Param("noticeId") String noticeId);

    int deleteTargetsByNoticeId(@Param("noticeId") String noticeId);

    int insertTarget(NoticeTarget target);

    List<String> findRecipientUserIds(@Param("noticeId") String noticeId);

    int insertReceipts(@Param("receipts") List<NoticeReceipt> receipts);

    int markAsRead(@Param("noticeId") String noticeId,
                   @Param("userId") String userId);

    List<NoticeReceipt> findReceiptsByUser(@Param("userId") String userId,
                                           @Param("unreadOnly") boolean unreadOnly);
}
