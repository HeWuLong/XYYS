package com.vod.server.security.context;

public class UserContext {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> currentIsAdmin = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static Long getUserId() {
        return currentUserId.get();
    }

    public static void setIsAdmin(Boolean isAdmin) {
        currentIsAdmin.set(Boolean.TRUE.equals(isAdmin));
    }

    public static Boolean getIsAdmin() {
        return Boolean.TRUE.equals(currentIsAdmin.get());
    }

    public static void clear() {
        currentUserId.remove();
        currentIsAdmin.remove();
    }
}