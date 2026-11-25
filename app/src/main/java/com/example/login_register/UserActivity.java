package com.example.login_register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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
    private ProfileDBHelper profileDb;
    private TextView tvName, tvEmail, tvPhone, tvAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_user);
        profileDb = new ProfileDBHelper(this);
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
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        loadProfile();
    }

    // Tên, số điện thoại, địa chỉ lấy từ database; email là tài khoản đăng nhập
    private void loadProfile() {
        String[] p = profileDb.getProfile(SharePreferance.getUserKey());
        String account = SharePreferance.username == null ? "" : SharePreferance.username;
        tvName.setText(getString(R.string.user_name) + (p[0].isEmpty() ? account : p[0]));
        tvEmail.setText(getString(R.string.user_email) + account);
        tvPhone.setText(getString(R.string.user_phone) + p[1]);
        tvAddress.setText(getString(R.string.user_address) + p[2]);
    }

    private void showEditDialog() {
        String[] p = profileDb.getProfile(SharePreferance.getUserKey());
        View view = getLayoutInflater().inflate(R.layout.dialog_profile, null);
        EditText edFullName = view.findViewById(R.id.edFullName);
        EditText edPhone = view.findViewById(R.id.edPhone);
        EditText edAddress = view.findViewById(R.id.edAddress);
        edFullName.setText(p[0]);
        edPhone.setText(p[1]);
        edAddress.setText(p[2]);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa thông tin")
                .setView(view)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = edFullName.getText().toString().trim();
            String phone = edPhone.getText().toString().trim();
            String address = edAddress.getText().toString().trim();
            if (!phone.isEmpty() && !phone.matches("0\\d{9,10}")) {
                edPhone.setError("Số điện thoại không hợp lệ");
                return;
            }
            profileDb.saveProfile(SharePreferance.getUserKey(), name, phone, address);
            loadProfile();
            dialog.dismiss();
        });
    }

    private void setupActions() {
        findViewById(R.id.btnHistory).setOnClickListener(view ->
                startActivity(new Intent(this, OrdersActivity.class)));
        findViewById(R.id.btnViewHistory).setOnClickListener(view ->
                startActivity(new Intent(this, ViewHistoryActivity.class)));
        findViewById(R.id.btnFavorites).setOnClickListener(view ->
                startActivity(new Intent(this, FavoriteActivity.class)));
        findViewById(R.id.btnEditInfo).setOnClickListener(view -> showEditDialog());
    }

    @Override
    protected void onDestroy() {
        profileDb.close();
        super.onDestroy();
    }
}