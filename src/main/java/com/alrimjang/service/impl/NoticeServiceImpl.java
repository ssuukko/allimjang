package com.alrimjang.service.impl;

import com.alrimjang.mapper.NoticeMapper;
import com.alrimjang.model.common.PageRequest;
import com.alrimjang.model.common.PageResult;
import com.alrimjang.model.entity.Notice;
import com.alrimjang.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public PageResult<Notice> getNoticePage(boolean includeHidden, String keyword, PageRequest pageRequest) {
        int safeSize = pageRequest.getSize();
        int totalCount = includeHidden
                ? noticeMapper.countIncludingHidden(keyword)
                : noticeMapper.countVisible(keyword);

        int totalPages = Math.max((int) Math.ceil((double) totalCount / safeSize), 1);
        int safePage = Math.min(Math.max(pageRequest.getPage(), 1), totalPages);
        int offset = (safePage - 1) * safeSize;

        List<Notice> items = includeHidden
                ? noticeMapper.findIncludingHiddenPage(keyword, offset, safeSize)
                : noticeMapper.findVisiblePage(keyword, offset, safeSize);

        return PageResult.of(items, totalCount, safePage, safeSize);
    }

    @Override
    public Notice getNoticeById(String id, boolean includeHidden) {
        Notice notice = includeHidden ? noticeMapper.findById(id) : noticeMapper.findVisibleById(id);
        if (notice == null) {
            return null;
        }

        noticeMapper.increaseViewCount(id);

        return includeHidden ? noticeMapper.findById(id) : noticeMapper.findVisibleById(id);
    }

    @Override
    public Notice getNoticeForEditByActor(String id, String actorId, String actorUsername, boolean isAdmin) {

        Notice target = getRequiredNotice(id);

        validateCanEdit(target, actorId, actorUsername, isAdmin);

        return target;
    }

    @Override
    public void createNotice(Notice notice) {

        notice.setId(UUID.randomUUID().toString());

        noticeMapper.insertNotice(notice);
    }

    @Override
    public void updateNoticeByActor(String id, Notice notice, String actorId, String actorUsername, boolean isAdmin) {

        Notice target = getRequiredNotice(id);
        validateCanEdit(target, actorId, actorUsername, isAdmin);

        notice.setId(id);
        notice.setAuthorId(target.getAuthorId());
        notice.setAuthorName(target.getAuthorName());

        int updatedCount = noticeMapper.updateNoticeByActor(notice, actorId, actorUsername, isAdmin);
        if (updatedCount == 0) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }
    }

    @Override
    public void deleteNoticeByActor(String id, String actorId, String actorUsername, boolean isAdmin) {

        Notice target = getRequiredNotice(id);
        validateCanDelete(target, actorId, actorUsername, isAdmin);

        int deletedCount = noticeMapper.deleteNoticeByActor(id, actorId, actorUsername, isAdmin);
        if (deletedCount == 0) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }
    }

    @Override
    public void hideNoticeByActor(String id, boolean isAdmin) {
        Notice target = getRequiredNotice(id);
        validateCanHide(target, isAdmin);

        int hiddenCount = noticeMapper.hideNoticeByActor(id, isAdmin);
        if (hiddenCount == 0) {
            throw new IllegalStateException("숨김 권한이 없습니다.");
        }
    }

    @Override
    public void unhideNoticeByActor(String id, boolean isAdmin) {
        Notice target = getRequiredNotice(id);
        validateCanUnhide(target, isAdmin);

        int unhiddenCount = noticeMapper.unhideNoticeByActor(id, isAdmin);
        if (unhiddenCount == 0) {
            throw new IllegalStateException("해제 권한이 없습니다.");
        }
    }

    @Override
    public boolean canEdit(Notice notice, String actorId, String actorUsername, boolean isAdmin) {

        if (notice == null) return false;

        String authorId = notice.getAuthorId();
        boolean isAuthor = (actorId != null && actorId.equals(authorId))
                || (actorUsername != null && actorUsername.equals(authorId));

        return !isAdmin && isAuthor;
    }

    @Override
    public boolean canDelete(Notice notice, String actorId, String actorUsername, boolean isAdmin) {

        if (notice == null) return false;

        String authorId = notice.getAuthorId();
        boolean isAuthor = (actorId != null && actorId.equals(authorId))
                || (actorUsername != null && actorUsername.equals(authorId));

        return isAdmin || isAuthor;
    }

    @Override
    public boolean canHide(Notice notice, boolean isAdmin) {
        return notice != null && isAdmin && !Boolean.TRUE.equals(notice.getIsHidden());
    }

    @Override
    public boolean canUnhide(Notice notice, boolean isAdmin) {
        return notice != null && isAdmin && Boolean.TRUE.equals(notice.getIsHidden());
    }

    private Notice getRequiredNotice(String id) {

        Notice target = noticeMapper.findById(id);

        if (target == null) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }

        return target;
    }

    private void validateCanEdit(Notice target, String actorId, String actorUsername, boolean isAdmin) {

        if (!canEdit(target, actorId, actorUsername, isAdmin)) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }
    }

    private void validateCanDelete(Notice target, String actorId, String actorUsername, boolean isAdmin) {

        if (!canDelete(target, actorId, actorUsername, isAdmin)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }
    }

    private void validateCanHide(Notice target, boolean isAdmin) {
        if (!canHide(target, isAdmin)) {
            throw new IllegalStateException("숨김 권한이 없습니다.");
        }
    }

    private void validateCanUnhide(Notice target, boolean isAdmin) {
        if (!canUnhide(target, isAdmin)) {
            throw new IllegalStateException("해제 권한이 없습니다.");
        }
    }
}
