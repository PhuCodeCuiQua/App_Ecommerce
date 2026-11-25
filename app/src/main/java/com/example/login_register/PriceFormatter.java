package com.example.login_register;

import java.text.NumberFormat;
import java.util.Locale;

public final class PriceFormatter {
    private PriceFormatter() {
    }

    public static String format(double price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(price) + " đ";
    }
}
