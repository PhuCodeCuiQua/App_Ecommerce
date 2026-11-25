package com.example.login_register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class UserActivity extends AppCompatActivity {
    private View topbar;
    private View userScroll;
    private int topbarBasePadding;
    private int scrollBasePaddingBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_user);
        initViews();
        setupInsets();
        setupHeader();
        setupActions();
    }

    private void initViews() {
        topbar = findViewById(R.id.topbarSimple);
        userScroll = findViewById(R.id.userScroll);
        topbarBasePadding = topbar.getPaddingTop();
        scrollBasePaddingBottom = userScroll.getPaddingBottom();
    }

    private void setupInsets() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), findViewById(R.id.main));
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, 0, systemBars.right, 0);
            topbar.setPadding(topbar.getPaddingLeft(), topbarBasePadding + systemBars.top,
                    topbar.getPaddingRight(), topbar.getPaddingBottom());
            userScroll.setPadding(userScroll.getPaddingLeft(), userScroll.getPaddingTop(),
                    userScroll.getPaddingRight(), scrollBasePaddingBottom + systemBars.bottom);
            return insets;
        });
    }

    private void setupHeader() {
        ((TextView) findViewById(R.id.tvTitle)).setText("Thông tin");
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        TextView tvName = findViewById(R.id.tvName);
        tvName.setText(getString(R.string.user_name) + SharePreferance.username);
    }

    private void setupActions() {
        findViewById(R.id.btnFavorites).setOnClickListener(view ->
                startActivity(new Intent(this, FavoriteActivity.class)));
    }
}
