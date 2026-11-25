package com.example.login_register;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private View main;
    private View header;
    private View bottomNav;
    private RecyclerView notificationsView;
    private TextView empty;
    private ProgressBar progress;
    private NotificationAdapter adapter;
    private boolean resumedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        initViews();
        setupInsets();
        setupNotificationsList();
        setupBottomNav();
        loadNotifications();
    }

    private void initViews() {
        main = findViewById(R.id.main);
        header = findViewById(R.id.notificationsHeader);
        bottomNav = findViewById(R.id.bottomNav);
        notificationsView = findViewById(R.id.rvNotifications);
        empty = findViewById(R.id.tvNotificationsEmpty);
        progress = findViewById(R.id.progressNotifications);
    }

    private void setupInsets() {
        BottomNav.applyInsets(this, main, header, bottomNav);
    }

    private void setupNotificationsList() {
        notificationsView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        notificationsView.setAdapter(adapter);
    }

    private void setupBottomNav() {
        BottomNav.setup(this, BottomNav.NOTIFICATIONS);
        BottomNav.handleBack(this);
    }

    private void loadNotifications() {
        progress.setVisibility(View.VISIBLE);
        NotificationRepository.load(new NotificationRepository.LoadCallback() {
            @Override
            public void onSuccess(List<AppNotification> notifications) {
                adapter.submitList(notifications);
                progress.setVisibility(View.GONE);
                boolean isEmpty = notifications.isEmpty();
                notificationsView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                empty.setText("Chưa có thông báo");
                empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                adapter.markAllRead();
                NotificationRepository.markAllRead();
                BottomNav.refreshBadges(NotificationsActivity.this);
            }

            @Override
            public void onFailure(Exception exception) {
                progress.setVisibility(View.GONE);
                notificationsView.setVisibility(View.GONE);
                empty.setText("Không thể tải thông báo");
                empty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resumedOnce) {
            loadNotifications();
        } else {
            resumedOnce = true;
        }
        BottomNav.refreshBadges(this);
    }
}
