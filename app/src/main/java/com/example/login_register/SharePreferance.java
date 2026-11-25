package com.example.login_register;

public class SharePreferance {
    public static String username;

    public static String getUserKey() {
        String userKey = username == null ? "" : username.trim().replace("/", "_");
        return userKey.isEmpty() ? "guest" : userKey;
    }
}
