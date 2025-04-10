package com.cartagenacorp.lm_projects.util;

import java.util.UUID;

public class JwtContextHolder {
    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();

    public static void setUserId(UUID userId) {
        currentUserId.set(userId);
    }

    public static UUID getUserId() {
        return currentUserId.get();
    }

    public static void clear() {
        currentUserId.remove();
    }
}

