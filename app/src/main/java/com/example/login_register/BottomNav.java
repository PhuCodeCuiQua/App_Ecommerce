package com.example.login_register;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public final class BottomNav {
    public static final int HOME = 0;
    public static final int CATEGORIES = 1;
    public static final int NOTIFICATIONS = 2;
    public static final int ACCOUNT = 3;

    private static final int[] ITEM_IDS = {
            R.id.navHome, R.id.navCategories, R.id.navNotifications, R.id.navAccount
    };
    private static final int[] ICON_IDS = {
            R.id.navHomeIcon, R.id.navCategoriesIcon,
            R.id.navNotificationsIcon, R.id.navAccountIcon
    };
    private static final int[] LABEL_IDS = {
            R.id.tvNavHomeLabel, R.id.tvNavCategoriesLabel,
            R.id.tvNavNotificationsLabel, R.id.tvNavAccountLabel
    };

    private BottomNav() {
    }

    public static void setup(Activity activity, int selectedTab) {
        for (int index = 0; index < ITEM_IDS.length; index++) {
            View item = activity.findViewById(ITEM_IDS[index]);
            if (item == null) {
                continue;
            }
            item.setSelected(index == selectedTab);
            android.widget.ImageView icon = activity.findViewById(ICON_IDS[index]);
            if (icon != null) {
                icon.setSelected(index == selectedTab);
            }
            TextView label = activity.findViewById(LABEL_IDS[index]);
            if (label != null) {
                label.setSelected(index == selectedTab);
                label.setTypeface(null, index == selectedTab
                        ? Typeface.BOLD : Typeface.NORMAL);
            }
            final int destination = index;
            item.setOnClickListener(view -> navigate(activity, selectedTab, destination));
        }
        NotificationRepository.refreshUnread(unread -> refreshBadges(activity));
        refreshBadges(activity);
    }

    public static void refreshBadges(Activity activity) {
        View notificationDot = activity.findViewById(R.id.viewNavNotifDot);
        if (notificationDot != null) {
            notificationDot.setVisibility(NotificationRepository.hasUnread
                    ? View.VISIBLE : View.GONE);
        }
    }

    public static void handleBack(ComponentActivity activity) {
        activity.getOnBackPressedDispatcher().addCallback(activity,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Intent intent = new Intent(activity, ListItemActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(0, 0);
                    }
                });
    }

    public static void applyInsets(Activity activity, View root, View topbar,
                                   View bottomNav) {
        WindowCompat.enableEdgeToEdge(activity.getWindow());
        int rootStart = root.getPaddingStart();
        int rootTop = root.getPaddingTop();
        int rootEnd = root.getPaddingEnd();
        int rootBottom = root.getPaddingBottom();
        int topbarPaddingTop = topbar == null ? 0 : topbar.getPaddingTop();
        int navHeight = bottomNav == null ? 0 : bottomNav.getLayoutParams().height;
        int navPaddingBottom = bottomNav == null ? 0 : bottomNav.getPaddingBottom();
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), root);
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets gesture = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            view.setPadding(rootStart + bars.left, rootTop, rootEnd + bars.right,
                    rootBottom + (bottomNav == null ? gesture.bottom : 0));
            if (topbar != null) {
                topbar.setPadding(topbar.getPaddingLeft(),
                        topbarPaddingTop + bars.top, topbar.getPaddingRight(),
                        topbar.getPaddingBottom());
            }
            if (bottomNav != null) {
                ViewGroup.LayoutParams params = bottomNav.getLayoutParams();
                params.height = navHeight + gesture.bottom;
                bottomNav.setLayoutParams(params);
                bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(),
                        bottomNav.getPaddingRight(), navPaddingBottom + gesture.bottom);
            }
            return windowInsets;
        });
    }

    private static void navigate(Activity activity, int selectedTab, int destination) {
        if (destination == selectedTab) {
            return;
        }
        Class<?> target;
        switch (destination) {
            case HOME:
                target = ListItemActivity.class;
                break;
            case CATEGORIES:
                target = CategoriesActivity.class;
                break;
            case NOTIFICATIONS:
                target = NotificationsActivity.class;
                break;
            case ACCOUNT:
                target = MenuActivity.class;
                break;
            default:
                return;
        }
        Intent intent = new Intent(activity, target);
        if (destination == HOME) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }
}
