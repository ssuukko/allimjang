package com.alrimjang.service;

import com.alrimjang.mapper.GroupMapper;
import com.alrimjang.mapper.UserMapper;
import com.alrimjang.model.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomAccessService {

    private final UserMapper userMapper;
    private final GroupMapper groupMapper;

    public void assertAccessible(String roomId, String username) {
        if (!canAccess(roomId, username)) {
            throw new IllegalStateException("해당 채팅방에 접근할 수 없습니다.");
        }
    }

    public boolean canAccess(String roomId, String username) {
        if (roomId == null || roomId.isBlank() || username == null || username.isBlank()) {
            return false;
        }
        if (isDirectRoom(roomId)) {
            String[] parts = roomId.split("__");
            if (parts.length != 3) {
                return false;
            }
            String token = normalizeRoomToken(username);
            return token.equals(parts[1]) || token.equals(parts[2]);
        }

        if (!isGroupRoom(roomId)) {
            return false;
        }
        String groupId = extractGroupId(roomId);
        if (groupId == null) {
            return false;
        }
        Users user = userMapper.findByUsername(username);
        if (user == null || user.getId() == null || user.getId().isBlank()) {
            return false;
        }
        return groupMapper.countChatMemberByGroupIdAndUserId(groupId, user.getId()) > 0;
    }

    public boolean isDirectRoom(String roomId) {
        return roomId != null && roomId.startsWith("dm__");
    }

    public boolean isGroupRoom(String roomId) {
        return roomId != null && roomId.startsWith("grp__");
    }

    public String extractGroupId(String roomId) {
        if (!isGroupRoom(roomId)) {
            return null;
        }
        String groupId = roomId.substring("grp__".length());
        return groupId.isBlank() ? null : groupId;
    }

    private String normalizeRoomToken(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
    }
}
