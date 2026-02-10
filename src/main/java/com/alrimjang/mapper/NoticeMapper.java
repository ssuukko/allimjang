package com.alrimjang.mapper;

import com.alrimjang.model.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {

    List<Notice> findAll();

    void increaseViewCount(String id);

    Notice findById(String id);
}
