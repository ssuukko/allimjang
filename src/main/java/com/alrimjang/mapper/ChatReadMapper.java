package com.alrimjang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface ChatReadMapper {
    int markAsRead(@Param("roomId") String roomId, @Param("userId") String userId);
    LocalDateTime findLastReadAt(@Param("roomId") String roomId, @Param("userId") String userId);
}
