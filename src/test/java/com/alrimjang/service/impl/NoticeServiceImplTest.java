package com.alrimjang.service.impl;

import com.alrimjang.mapper.NoticeMapper;
import com.alrimjang.model.common.PageRequest;
import com.alrimjang.model.common.PageResult;
import com.alrimjang.model.entity.Notice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    @Mock
    private NoticeMapper noticeMapper;

    @InjectMocks
    private NoticeServiceImpl noticeService;

    @Test
    void if_author_and_not_admin_when_canEdit_then_true() {
        // if
        Notice notice = notice("author-1", false);

        // when
        boolean result = noticeService.canEdit(notice, "author-1", "user1", false);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void if_admin_when_canEdit_then_false() {
        // if
        Notice notice = notice("author-1", false);

        // when
        boolean result = noticeService.canEdit(notice, "author-1", "admin", true);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void if_author_or_admin_when_canDelete_then_true() {
        // if
        Notice notice = notice("author-1", false);

        // when
        boolean authorResult = noticeService.canDelete(notice, "author-1", "user1", false);
        boolean adminResult = noticeService.canDelete(notice, "someone", "admin", true);

        // then
        assertThat(authorResult).isTrue();
        assertThat(adminResult).isTrue();
    }

    @Test
    void if_hidden_and_admin_when_canUnhide_then_true() {
        // if
        Notice hidden = notice("author-1", true);

        // when
        boolean result = noticeService.canUnhide(hidden, true);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void if_not_admin_when_hideNoticeByActor_then_throw() {
        // if
        when(noticeMapper.findById("n1")).thenReturn(notice("author-1", false));

        // when / then
        assertThatThrownBy(() -> noticeService.hideNoticeByActor("n1", false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("숨김 권한");

        verify(noticeMapper, never()).hideNoticeByActor(anyString(), anyBoolean());
    }

    @Test
    void if_admin_when_hideNoticeByActor_then_update_called() {
        // if
        when(noticeMapper.findById("n1")).thenReturn(notice("author-1", false));
        when(noticeMapper.hideNoticeByActor("n1", true)).thenReturn(1);

        // when
        noticeService.hideNoticeByActor("n1", true);

        // then
        verify(noticeMapper).hideNoticeByActor("n1", true);
    }

    @Test
    void if_admin_when_unhideNoticeByActor_then_update_called() {
        // if
        when(noticeMapper.findById("n1")).thenReturn(notice("author-1", true));
        when(noticeMapper.unhideNoticeByActor("n1", true)).thenReturn(1);

        // when
        noticeService.unhideNoticeByActor("n1", true);

        // then
        verify(noticeMapper).unhideNoticeByActor("n1", true);
    }

    @Test
    void if_not_author_and_not_admin_when_updateNoticeByActor_then_throw() {
        // if
        Notice target = notice("author-1", false);
        when(noticeMapper.findById("n1")).thenReturn(target);

        // when / then
        assertThatThrownBy(() -> noticeService.updateNoticeByActor(
                "n1", Notice.builder().title("t").content("c").build(), "other", "other", false
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("수정 권한");
    }

    @Test
    void if_author_when_updateNoticeByActor_then_author_fields_preserved() {
        // if
        Notice target = notice("author-1", false);
        target.setAuthorName("작성자");
        when(noticeMapper.findById("n1")).thenReturn(target);
        when(noticeMapper.updateNoticeByActor(any(Notice.class), eq("author-1"), eq("user1"), eq(false))).thenReturn(1);
        Notice update = Notice.builder().title("new title").content("new content").build();

        // when
        noticeService.updateNoticeByActor("n1", update, "author-1", "user1", false);

        // then
        assertThat(update.getId()).isEqualTo("n1");
        assertThat(update.getAuthorId()).isEqualTo("author-1");
        assertThat(update.getAuthorName()).isEqualTo("작성자");
    }

    @Test
    void if_searchType_invalid_when_getNoticePage_then_searchType_normalized_to_all() {
        // if
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        when(noticeMapper.countVisible("kw", "all")).thenReturn(0);
        when(noticeMapper.findVisiblePage("kw", "all", 0, 10)).thenReturn(List.of());

        // when
        PageResult<Notice> result = noticeService.getNoticePage(false, "kw", "wrong", pageRequest);

        // then
        assertThat(result.getCurrentPage()).isEqualTo(1);
        verify(noticeMapper).countVisible("kw", "all");
        verify(noticeMapper).findVisiblePage("kw", "all", 0, 10);
    }

    @Test
    void if_requested_page_over_total_when_getNoticePage_then_page_clamped() {
        // if
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(999);
        pageRequest.setSize(10);
        when(noticeMapper.countIncludingHidden("", "title")).thenReturn(21);
        when(noticeMapper.findIncludingHiddenPage("", "title", 20, 10)).thenReturn(List.of(notice("a", false)));

        // when
        PageResult<Notice> result = noticeService.getNoticePage(true, "", "title", pageRequest);

        // then
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getCurrentPage()).isEqualTo(3);
        verify(noticeMapper).findIncludingHiddenPage("", "title", 20, 10);
    }

    @Test
    void if_notice_not_found_when_deleteNoticeByActor_then_throw() {
        // if
        when(noticeMapper.findById("missing")).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> noticeService.deleteNoticeByActor("missing", "u1", "user1", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    private Notice notice(String authorId, boolean hidden) {
        return Notice.builder()
                .id("n1")
                .title("title")
                .content("content")
                .authorId(authorId)
                .isHidden(hidden)
                .isImportant(false)
                .build();
    }
}
