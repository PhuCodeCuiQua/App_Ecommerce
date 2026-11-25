package com.example.login_register;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ProductMapper {
    private ProductMapper() {
    }

    public static Product fromDocument(DocumentSnapshot document, Context context) {
        String legacyImage = document.getString("image");
        List<ProductColor> colors = new ArrayList<>();
        List<?> colorData = document.get("colors") instanceof List
                ? (List<?>) document.get("colors") : new ArrayList<>();
        for (Object value : colorData) {
            if (!(value instanceof Map)) {
                continue;
            }
            Map<?, ?> color = (Map<?, ?>) value;
            String name = stringValue(color.get("name"));
            String imageName = stringValue(color.get("image"));
            colors.add(new ProductColor(name, imageName,
                    resolveImage(context, imageName)));
        }
        if (colors.isEmpty() && legacyImage != null) {
            colors.add(new ProductColor("Mặc định", legacyImage,
                    resolveImage(context, legacyImage)));
        }

        List<Integer> sizes = new ArrayList<>();
        List<?> sizeData = document.get("sizes") instanceof List
                ? (List<?>) document.get("sizes") : new ArrayList<>();
        for (Object size : sizeData) {
            if (size instanceof Number) {
                sizes.add(((Number) size).intValue());
            }
        }

        Number originalPrice = document.get("originalPrice") instanceof Number
                ? (Number) document.get("originalPrice") : document.getDouble("price");
        Number discountPercent = document.get("discountPercent") instanceof Number
                ? (Number) document.get("discountPercent") : 0;
        Number ratingAverage = document.getDouble("ratingAverage");
        Number ratingCount = document.getLong("ratingCount");
        Number soldCount = document.getLong("soldCount");

        return new Product(document.getId(), valueOr(document.getString("name"), ""),
                valueOr(document.getString("type"), ""),
                valueOr(document.getString("gender"), ""),
                valueOr(document.getString("collection"), ""),
                originalPrice == null ? 0 : originalPrice.doubleValue(),
                discountPercent.intValue(),
                valueOr(document.getString("description"), ""),
                valueOr(document.getString("material"), ""),
                valueOr(document.getString("designer"), ""),
                valueOr(document.getString("features"), ""),
                sizes, colors, ratingAverage == null ? 0 : ratingAverage.doubleValue(),
                ratingCount == null ? 0 : ratingCount.intValue(),
                soldCount == null ? 0 : soldCount.intValue());
    }

    private static int resolveImage(Context context, String imageName) {
        int imageResId = imageName.isEmpty() ? 0 : context.getResources().getIdentifier(
                imageName, "drawable", context.getPackageName());
        return imageResId == 0 ? R.drawable.ic_shoe_placeholder : imageResId;
    }

    private static String stringValue(Object value) {
        return value instanceof String ? (String) value : "";
    }

    private static String valueOr(String value, String fallback) {
        return value == null ? fallback : value;
    }
}
