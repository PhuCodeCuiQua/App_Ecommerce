package com.example.login_register;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class MenuActivity extends AppCompatActivity {
    private LinearLayout main;
    private LinearLayout header;
    private LinearLayout body;
    private LinearLayout menuItems;
    private TextView tvUsername;
    private MaterialButton btnEditProfile;
    private int headerPaddingStart;
    private int headerPaddingTop;
    private int headerPaddingEnd;
    private int bodyPaddingStart;
    private int bodyPaddingTop;
    private int bodyPaddingEnd;
    private int bodyPaddingBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_menu);
        initViews();
        setupInsets();
        setupBackButton();
        setupProfile();
        setupMenuItems();
    }

    private void initViews() {
        main = findViewById(R.id.main);
        header = findViewById(R.id.menuHeader);
        body = findViewById(R.id.menuBody);
        menuItems = findViewById(R.id.menuItems);
        tvUsername = findViewById(R.id.tvMenuUsername);
        btnEditProfile = findViewById(R.id.btnEditProfile);
    }

    private void setupInsets() {
        headerPaddingStart = header.getPaddingStart();
        headerPaddingTop = header.getPaddingTop();
        headerPaddingEnd = header.getPaddingEnd();
        bodyPaddingStart = body.getPaddingStart();
        bodyPaddingTop = body.getPaddingTop();
        bodyPaddingEnd = body.getPaddingEnd();
        bodyPaddingBottom = body.getPaddingBottom();
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(main, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPaddingRelative(headerPaddingStart + systemBars.left,
                    headerPaddingTop + systemBars.top, headerPaddingEnd + systemBars.right,
                    header.getPaddingBottom());
            body.setPaddingRelative(bodyPaddingStart + systemBars.left, bodyPaddingTop,
                    bodyPaddingEnd + systemBars.right, bodyPaddingBottom + systemBars.bottom);
            return windowInsets;
        });
    }

    private void setupProfile() {
        String username = SharePreferance.username;
        tvUsername.setText(username == null ? "" : username);
        btnEditProfile.setOnClickListener(view ->
                startActivity(new Intent(this, UserActivity.class)));
    }

    private void setupBackButton() {
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void setupMenuItems() {
        addMenuRow("Đơn hàng của tôi", R.drawable.ic_history, OrdersActivity.class, null);
        addMenuRow("Thẻ và ví", R.drawable.ic_wallet, null, null);
        addMenuRow("Địa chỉ", R.drawable.ic_location, null, null);
        addNotificationRow();
        addMenuRow("Yêu thích", R.drawable.ic_favorite_border,
                FavoriteActivity.class, null);
        addMenuRow("Hồ sơ", R.drawable.ic_person, UserActivity.class, null);
        addMenuRow("Đăng xuất", R.drawable.ic_logout, null, this::logout);
    }

    private void addMenuRow(String label, int iconResource,
                            Class<?> destination, Runnable action) {
        LinearLayout row = createMenuRow(label, iconResource);
        ImageView arrow = new ImageView(this);
        arrow.setLayoutParams(iconLayoutParams());
        arrow.setImageResource(R.drawable.ic_chevron_right);
        setIconTint(arrow);
        row.addView(arrow);
        setRowAction(row, destination, action);
        addRowAndDivider(row);
    }

    private void addNotificationRow() {
        LinearLayout row = createMenuRow("Thông báo", R.drawable.ic_notifications);
        MaterialSwitch notificationSwitch = new MaterialSwitch(this);
        notificationSwitch.setContentDescription("Bật hoặc tắt thông báo");
        notificationSwitch.setMinimumWidth(getResources()
                .getDimensionPixelSize(R.dimen.touch_target));
        row.addView(notificationSwitch, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addRowAndDivider(row);
    }

    private LinearLayout createMenuRow(String label, int iconResource) {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.menu_item_height)));
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(iconLayoutParams());
        icon.setImageResource(iconResource);
        setIconTint(icon);
        row.addView(icon);

        TextView title = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        title.setLayoutParams(titleParams);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setText(label);
        title.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        title.setTextColor(ContextCompat.getColor(this, R.color.md_onSurface));
        row.addView(title);
        return row;
    }

    private LinearLayout.LayoutParams iconLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.icon_size),
                getResources().getDimensionPixelSize(R.dimen.icon_size));
        params.setMarginStart(getResources().getDimensionPixelSize(R.dimen.space_8));
        params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.space_16));
        return params;
    }

    private void setIconTint(ImageView icon) {
        androidx.core.widget.ImageViewCompat.setImageTintList(icon,
                android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.md_onSurfaceVariant)));
    }

    private void setRowAction(View row, Class<?> destination, Runnable action) {
        if (destination != null) {
            row.setClickable(true);
            row.setFocusable(true);
            row.setBackgroundResource(android.R.drawable.list_selector_background);
            row.setOnClickListener(view -> startActivity(new Intent(this, destination)));
        } else if (action != null) {
            row.setClickable(true);
            row.setFocusable(true);
            row.setBackgroundResource(android.R.drawable.list_selector_background);
            row.setOnClickListener(view -> action.run());
        }
    }

    private void addRowAndDivider(View row) {
        menuItems.addView(row);
        View divider = new View(this);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.md_outlineVariant));
        menuItems.addView(divider, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.card_stroke_width)));
    }

    private void logout() {
        SharePreferance.username = null;
        CartManager.clearMemory();
        FavoriteManager.clearMemory();
        Intent intent = new Intent(this, com.example.login_register.ui.login.LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
