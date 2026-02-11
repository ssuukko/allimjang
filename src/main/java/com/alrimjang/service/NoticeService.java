package com.alrimjang.service;

import com.alrimjang.model.entity.Notice;

import java.util.List;

public interface NoticeService {

    List<Notice> getNoticeList();

    Notice getNoticeById(String id);

    void createNotice(Notice notice);
}
