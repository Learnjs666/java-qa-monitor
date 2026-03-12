package cn.edu.swpu.java_qa_monitor.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;

public class UserContext {

    private static final ThreadLocal<UserInfo> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(UserInfo userInfo) {
        CURRENT_USER.set(userInfo);
    }

    public static UserInfo get() {
        return CURRENT_USER.get();
    }

    public static void remove() {
        CURRENT_USER.remove();
    }

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String role;
    }
}
