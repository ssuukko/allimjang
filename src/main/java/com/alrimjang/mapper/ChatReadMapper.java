package com.alrimjang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatReadMapper {
    int markAsRead(@Param("roomId") String roomId, @Param("userId") String userId);
}
