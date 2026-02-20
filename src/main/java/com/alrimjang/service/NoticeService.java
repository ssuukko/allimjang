package com.alrimjang.service;

import com.alrimjang.model.common.PageRequest;
import com.alrimjang.model.common.PageResult;
import com.alrimjang.model.entity.Notice;

import java.util.List;

public interface NoticeService {

    PageResult<Notice> getNoticePage(boolean includeHidden, String keyword, String searchType, PageRequest pageRequest);

    Notice getNoticeById(String id, boolean includeHidden);

    Notice getNoticeForEditByActor(String id, String actorId, String actorUsername, boolean isAdmin);

    void createNotice(Notice notice);

    void updateNoticeByActor(String id, Notice notice, String actorId, String actorUsername, boolean isAdmin);

    void deleteNoticeByActor(String id, String actorId, String actorUsername, boolean isAdmin);

    void hideNoticeByActor(String id, boolean isAdmin);

    void unhideNoticeByActor(String id, boolean isAdmin);

    boolean canEdit(Notice notice, String actorId, String actorUsername, boolean isAdmin);

    boolean canDelete(Notice notice, String actorId, String actorUsername, boolean isAdmin);

    boolean canHide(Notice notice, boolean isAdmin);

    boolean canUnhide(Notice notice, boolean isAdmin);
}
