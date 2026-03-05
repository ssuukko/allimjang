package com.alrimjang.mapper;

import com.alrimjang.model.ChatRoomSummaryResponse;
import com.alrimjang.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    int insert(ChatMessage message);
    List<ChatMessage> findRecentByRoomId(@Param("roomId") String roomId, @Param("limit") int limit);
    int countUnreadByRoomAndUser(@Param("roomId") String roomId, @Param("userId") String userId);
    List<ChatRoomSummaryResponse> findRoomSummaries(@Param("userId") String userId, @Param("userToken") String userToken);
}
