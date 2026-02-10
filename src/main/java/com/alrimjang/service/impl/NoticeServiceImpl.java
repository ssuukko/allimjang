package com.alrimjang.service.impl;

import com.alrimjang.mapper.NoticeMapper;
import com.alrimjang.model.entity.Notice;
import com.alrimjang.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public List<Notice> getNoticeList() {
        return noticeMapper.findAll();
    }

    @Override
    public Notice getNoticeById(String id) {
        noticeMapper.increaseViewCount(id);
        return noticeMapper.findById(id);
    }
}
