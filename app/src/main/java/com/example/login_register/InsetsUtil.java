package com.example.login_register;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class InsetsUtil {
    // Gọi ngay sau setContentView(...). Layout cần có id "main" và thanh "topbarSimple".
    public static void apply(Activity activity) {
        WindowCompat.enableEdgeToEdge(activity.getWindow());
        View main = activity.findViewById(R.id.main);
        View topbar = activity.findViewById(R.id.topbarSimple);
        int topbarBasePadding = topbar.getPaddingTop();

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), main);
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(main, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, 0, bars.right, bars.bottom);
            topbar.setPadding(topbar.getPaddingLeft(), topbarBasePadding + bars.top,
                    topbar.getPaddingRight(), topbar.getPaddingBottom());
            return insets;
        });
    }
}