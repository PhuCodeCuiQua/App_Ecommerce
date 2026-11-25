package com.example.login_register.data;

import com.example.login_register.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            if (username != null && !username.trim().isEmpty() && password != null && !password.trim().isEmpty()) {
                LoggedInUser user =
                        new LoggedInUser(
                                java.util.UUID.randomUUID().toString(),
                                username.trim());
                return new Result.Success<>(user);
            } else {
                return new Result.Error(new Exception("Tên đăng nhập và mật khẩu không được để trống"));
            }
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}