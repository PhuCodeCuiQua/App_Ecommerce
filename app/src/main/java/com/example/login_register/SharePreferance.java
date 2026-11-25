package com.example.login_register;

import android.content.Context;

public class SharePreferance {
    private static final String PREFS = "session";
    private static final String KEY_USERNAME = "username";

    public static String username;

    public static String getUserKey() {
        String userKey = username == null ? "" : username.trim().replace("/", "_");
        return userKey.isEmpty() ? "guest" : userKey;
    }

    // Gọi sau khi đăng nhập thành công hoặc khi đăng xuất
    public static void save(Context context) {
        context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_USERNAME, username).apply();
    }

    // Gọi khi app khởi động để khôi phục tài khoản đã đăng nhập
    public static void restore(Context context) {
        if (username == null) {
            username = context.getApplicationContext()
                    .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getString(KEY_USERNAME, null);
        }
    }
}