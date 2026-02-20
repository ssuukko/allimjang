package com.alrimjang.mapper;

import com.alrimjang.model.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {

    List<Notice> findAllVisible();

    List<Notice> findAllIncludingHidden();

    void increaseViewCount(String id);

    Notice findById(String id);

    Notice findVisibleById(String id);

    void insertNotice(Notice notice);

    int updateNoticeByActor(@Param("notice") Notice notice,
                            @Param("actorId") String actorId,
                            @Param("actorUsername") String actorUsername,
                            @Param("isAdmin") boolean isAdmin);

    int deleteNoticeByActor(@Param("id") String id,
                            @Param("actorId") String actorId,
                            @Param("actorUsername") String actorUsername,
                            @Param("isAdmin") boolean isAdmin);

    int hideNoticeByActor(@Param("id") String id, @Param("isAdmin") boolean isAdmin);

    int unhideNoticeByActor(@Param("id") String id, @Param("isAdmin") boolean isAdmin);
}
