package com.alrimjang.mapper;

import com.alrimjang.model.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {

    List<Notice> findVisiblePage(@Param("keyword") String keyword,
                                 @Param("searchType") String searchType,
                                 @Param("offset") int offset,
                                 @Param("size") int size);

    int countVisible(@Param("keyword") String keyword,
                     @Param("searchType") String searchType);

    List<Notice> findIncludingHiddenPage(@Param("keyword") String keyword,
                                         @Param("searchType") String searchType,
                                         @Param("offset") int offset,
                                         @Param("size") int size);

    int countIncludingHidden(@Param("keyword") String keyword,
                             @Param("searchType") String searchType);

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
