package com.alrimjang.service;

import org.springframework.stereotype.Service;

@Service
public class ChatRoomAccessService {

    public void assertAccessible(String roomId, String username) {
        if (!canAccess(roomId, username)) {
            throw new IllegalStateException("해당 채팅방에 접근할 수 없습니다.");
        }
    }

    public boolean canAccess(String roomId, String username) {
        if (roomId == null || roomId.isBlank() || username == null || username.isBlank()) {
            return false;
        }
        if (!isDirectRoom(roomId)) {
            return true;
        }

        String[] parts = roomId.split("__");
        if (parts.length != 3) {
            return false;
        }
        String token = normalizeRoomToken(username);
        return token.equals(parts[1]) || token.equals(parts[2]);
    }

    public boolean isDirectRoom(String roomId) {
        return roomId != null && roomId.startsWith("dm__");
    }

    private String normalizeRoomToken(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
    }
}
