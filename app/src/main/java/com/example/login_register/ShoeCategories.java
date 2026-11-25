package com.example.login_register;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ShoeCategories {
    public static final Map<String, String> GENDERS;
    public static final List<String> TYPES = Collections.unmodifiableList(Arrays.asList(
            "Giày thường ngày", "Giày chạy bộ", "Giày thể thao",
            "Dép xỏ ngón", "Dép quai hậu", "Sandal"));

    static {
        Map<String, String> genders = new LinkedHashMap<>();
        genders.put("men", "Nam");
        genders.put("women", "Nữ");
        genders.put("kids", "Trẻ em");
        GENDERS = Collections.unmodifiableMap(genders);
    }

    private ShoeCategories() {
    }
}
