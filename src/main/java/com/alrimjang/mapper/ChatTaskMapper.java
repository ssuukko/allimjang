package com.alrimjang.mapper;

import com.alrimjang.model.entity.ChatTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatTaskMapper {
    int insert(ChatTask task);

    List<ChatTask> findByRoomId(@Param("roomId") String roomId);

    ChatTask findByIdAndRoomId(@Param("taskId") String taskId, @Param("roomId") String roomId);

    int markDone(@Param("taskId") String taskId,
                 @Param("roomId") String roomId,
                 @Param("completedAt") LocalDateTime completedAt);

    int markConfirmed(@Param("taskId") String taskId,
                      @Param("roomId") String roomId,
                      @Param("confirmedAt") LocalDateTime confirmedAt);
}
