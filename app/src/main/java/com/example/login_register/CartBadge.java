package com.example.login_register;

import android.view.View;
import android.widget.TextView;

public final class CartBadge {
    private CartBadge() {
    }

    public static void update(View root) {
        TextView badge = root.findViewById(R.id.tvCartBadge);
        if (badge == null) {
            return;
        }
        int quantity = CartManager.getTotalQuantity();
        badge.setVisibility(quantity == 0 ? View.GONE : View.VISIBLE);
        badge.setText(quantity > 99 ? "99+" : String.valueOf(quantity));
        badge.setContentDescription("Số lượng trong giỏ: " + quantity);
    }
}
