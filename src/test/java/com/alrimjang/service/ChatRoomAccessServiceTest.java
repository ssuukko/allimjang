package com.alrimjang.service;

import com.alrimjang.mapper.GroupMapper;
import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.entity.Users;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRoomAccessServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private GroupMapper groupMapper;

    @InjectMocks
    private ChatRoomAccessService chatRoomAccessService;

    @Test
    void if_direct_room_contains_user_token_when_canAccess_then_true() {
        boolean result = chatRoomAccessService.canAccess("dm__alice__bob", "alice");

        assertThat(result).isTrue();
    }

    @Test
    void if_group_room_and_member_when_canAccess_then_true() {
        Users user = Users.builder()
                .id("u1")
                .username("alice")
                .build();
        when(userMapper.findByUsername("alice")).thenReturn(user);
        when(groupMapper.countChatMemberByGroupIdAndUserId("g1", "u1")).thenReturn(1);

        boolean result = chatRoomAccessService.canAccess("grp__g1", "alice");

        assertThat(result).isTrue();
    }

    @Test
    void if_group_room_and_not_member_when_assertAccessible_then_throw() {
        Users user = Users.builder()
                .id("u1")
                .username("alice")
                .build();
        when(userMapper.findByUsername("alice")).thenReturn(user);
        when(groupMapper.countChatMemberByGroupIdAndUserId("g1", "u1")).thenReturn(0);

        assertThatThrownBy(() -> chatRoomAccessService.assertAccessible("grp__g1", "alice"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("접근할 수 없습니다");
    }
}
