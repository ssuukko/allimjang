package com.alrimjang.service;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveUserTracker {

    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    public void markActive(String username) {
        if (username != null && !username.isBlank()) {
            activeUsers.add(username);
        }
    }

    public void markInactive(String username) {
        if (username != null && !username.isBlank()) {
            activeUsers.remove(username);
        }
    }

    public boolean isActive(String username) {
        return activeUsers.contains(username);
    }
}
