package org.example.security.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptsService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MS = 5 * 60 * 1000L;

    private record AttemptInfo(int count, long blockedAt) {
    }

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public void loginFailed(String username) {
        AttemptInfo info = attempts.getOrDefault(username, new AttemptInfo(0, 0));
        int newCount = info.count() + 1;
        long blockedAt = newCount >= MAX_ATTEMPTS ? System.currentTimeMillis() : 0;
        attempts.put(username, new AttemptInfo(newCount, blockedAt));
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    public boolean isBlocked(String username) {
        AttemptInfo info = attempts.get(username);
        if (info == null || info.blockedAt() == 0) return false;
        if (System.currentTimeMillis() - info.blockedAt() > BLOCK_DURATION_MS) {
            attempts.remove(username);
            return false;
        }
        return info.count() >= MAX_ATTEMPTS;
    }
}