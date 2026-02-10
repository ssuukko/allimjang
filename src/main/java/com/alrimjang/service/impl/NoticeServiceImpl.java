package com.alrimjang.service;

import com.alrimjang.model.entity.Notice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NoticeService {

    List<Notice> getNoticeList();
}
