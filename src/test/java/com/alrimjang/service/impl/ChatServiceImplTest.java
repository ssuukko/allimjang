package com.alrimjang.service.impl;

import com.alrimjang.mapper.ChatMessageMapper;
import com.alrimjang.mapper.ChatReadMapper;
import com.alrimjang.mapper.ChatTaskMapper;
import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.entity.ChatTask;
import com.alrimjang.service.ChatRoomAccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private ChatReadMapper chatReadMapper;

    @Mock
    private ChatTaskMapper chatTaskMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ChatRoomAccessService chatRoomAccessService;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    void if_not_assignee_when_completeTask_then_throw() {
        String roomId = "dm__admin__user";
        ChatTask task = new ChatTask();
        task.setId("t1");
        task.setRoomId(roomId);
        task.setAssigneeUsername("user");

        when(chatRoomAccessService.isDirectRoom(roomId)).thenReturn(true);
        when(chatRoomAccessService.canAccess(roomId, "admin")).thenReturn(true);
        when(chatTaskMapper.findByIdAndRoomId("t1", roomId)).thenReturn(task);

        assertThatThrownBy(() -> chatService.completeTask(roomId, "t1", "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("담당자만 완료 처리");

        verify(chatTaskMapper, never()).markDone(any(), any(), any());
    }

    @Test
    void if_assignee_when_completeTask_then_markDone() {
        String roomId = "dm__admin__user";
        ChatTask task = new ChatTask();
        task.setId("t1");
        task.setRoomId(roomId);
        task.setAssigneeUsername("user");

        when(chatRoomAccessService.isDirectRoom(roomId)).thenReturn(true);
        when(chatRoomAccessService.canAccess(roomId, "user")).thenReturn(true);
        when(chatTaskMapper.findByIdAndRoomId("t1", roomId)).thenReturn(task);
        when(chatTaskMapper.markDone(any(), any(), any())).thenReturn(1);

        chatService.completeTask(roomId, "t1", "user");

        verify(chatTaskMapper).markDone(any(), any(), any());
    }
}
