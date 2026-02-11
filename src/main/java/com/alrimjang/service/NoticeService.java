package com.alrimjang.service;

import com.alrimjang.model.entity.Notice;

import java.util.List;

public interface NoticeService {

    List<Notice> getNoticeList();

    Notice getNoticeById(String id);

    Notice  getNoticeByIdForEdit(String id);

    void createNotice(Notice notice);

    void updateNotice(Notice notice);

    void deleteNotice(String id);

}
