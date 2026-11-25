package com.example.login_register;

import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharePreferance.restore(this);
    }
}