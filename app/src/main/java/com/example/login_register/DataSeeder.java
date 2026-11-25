package com.example.login_register;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DataSeeder {
    private static final String TAG = "DataSeeder";

    private static final String[][] PRODUCTS = {
            {"aero_zoom", "Aero Zoom", "Giày chạy bộ", "men", "Bán chạy nhất", "2390000", "20"},
            {"aero_max", "Aero Max", "Giày chạy bộ", "men", "Bán chạy nhất", "2790000", "0"},
            {"aero_edge", "Aero Edge", "Giày thể thao", "men", "Bộ sưu tập mùa hè", "2990000", "15"},
            {"urban_classic", "Urban Classic", "Giày thường ngày", "men", "Hàng mới về", "1890000", "0"},
            {"glide_lady", "Glide Lady", "Giày chạy bộ", "women", "Bán chạy nhất", "2190000", "25"},
            {"breeze", "Breeze", "Sandal", "women", "Bộ sưu tập mùa hè", "1290000", "30"},
            {"flex_slide", "Flex Slide", "Dép xỏ ngón", "women", "Hàng mới về", "590000", "0"},
            {"spark_kids", "Spark Kids", "Giày thể thao", "kids", "Bộ sưu tập mùa hè", "1190000", "10"}
    };

    private static final String[][][] PRODUCT_DETAILS = {
            {{"Giày chạy êm nhẹ, phù hợp luyện tập hằng ngày.", "Lưới dệt thoáng khí", "Thiết kế Aero", "Đệm êm; Đế bám tốt"},
                    {"Đen", "Trắng", "Vàng"}},
            {{"Đôi giày cân bằng giữa độ ổn định và sự thoải mái.", "Vải dệt và cao su", "Thiết kế Aero", "Đệm đàn hồi; Thân giày nhẹ"},
                    {"Đen", "Xám", "Xanh navy"}},
            {{"Kiểu dáng năng động với phần đế chắc chắn.", "Da tổng hợp và lưới", "Thiết kế Aero", "Đế chống trượt; Dễ phối đồ"},
                    {"Đen", "Trắng", "Đỏ đô"}},
            {{"Thiết kế cổ điển gọn gàng cho những ngày thường.", "Da tổng hợp", "Thiết kế Urban", "Lót mềm; Đế cao su bền"},
                    {"Trắng", "Đen", "Be"}},
            {{"Giày chạy nhẹ với phom ôm chân và lớp đệm mềm.", "Lưới thoáng khí", "Thiết kế Glide", "Đệm êm; Thoáng khí"},
                    {"Trắng", "Hồng nhạt", "Đen"}},
            {{"Sandal thoáng mát, tiện dụng cho những chuyến đi hè.", "Dây tổng hợp và đế EVA", "Thiết kế Breeze", "Đế nhẹ; Quai mềm"},
                    {"Đen", "Kem", "Xanh bạc hà"}},
            {{"Dép xỏ ngón nhẹ nhàng, phù hợp sử dụng hằng ngày.", "EVA mềm", "Thiết kế Flex", "Nhanh khô; Đế chống trượt"},
                    {"Đen", "Trắng", "Hồng"}},
            {{"Giày thể thao nhẹ, hỗ trợ bé vận động thoải mái.", "Vải lưới và cao su", "Thiết kế Spark", "Dễ mang; Đế bám chắc"},
                    {"Xanh dương", "Đỏ", "Đen"}}
    };

    private static final String[][] REVIEW_TEXT = {
            {"Sản phẩm đúng mô tả, mang rất thoải mái.", "Đường may đẹp, giao hàng nhanh.", "Chất lượng tốt, mình sẽ tiếp tục ủng hộ."},
            {"Đệm êm và nhẹ chân khi đi bộ.", "Màu sắc đẹp, form giày vừa vặn.", "Đóng gói cẩn thận, rất hài lòng."},
            {"Kiểu dáng đẹp, chất liệu dễ vệ sinh.", "Mang cả ngày vẫn thoải mái.", "Sản phẩm tốt so với mức giá."}
    };

    private DataSeeder() {
    }

    public static void seedIfNeeded(Runnable onDone) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("meta").document("seed_v2").get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        onDone.run();
                    } else {
                        seed(db, onDone);
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Không thể kiểm tra dữ liệu khởi tạo", exception);
                    onDone.run();
                });
    }

    private static void seed(FirebaseFirestore db, Runnable onDone) {
        WriteBatch batch = db.batch();
        for (int index = 0; index < PRODUCTS.length; index++) {
            String[] product = PRODUCTS[index];
            String[][] details = PRODUCT_DETAILS[index];
            Map<String, Object> productData = new HashMap<>();
            productData.put("name", product[1]);
            productData.put("type", product[2]);
            productData.put("gender", product[3]);
            productData.put("collection", product[4]);
            productData.put("originalPrice", Long.parseLong(product[5]));
            productData.put("discountPercent", Integer.parseInt(product[6]));
            productData.put("description", details[0][0]);
            productData.put("material", details[0][1]);
            productData.put("designer", details[0][2]);
            productData.put("features", details[0][3]);
            productData.put("sizes", getSizes(product[3]));
            productData.put("colors", getColors(product[0], details[1]));
            productData.put("ratingAverage", 4.1 + (index % 9) * 0.1);
            productData.put("ratingCount", 100 + index * 37);
            productData.put("soldCount", 300 + index * 311);
            batch.set(db.collection("products").document(product[0]), productData);

            for (int reviewIndex = 0; reviewIndex < 3; reviewIndex++) {
                Map<String, Object> review = new HashMap<>();
                review.put("author", new String[]{"Minh Anh", "Hoàng Nam", "Thu Hà"}[reviewIndex]);
                review.put("stars", 4 + ((index + reviewIndex) % 2));
                review.put("content", REVIEW_TEXT[index % REVIEW_TEXT.length][reviewIndex]);
                review.put("createdAt", new Timestamp(
                        new java.util.Date(System.currentTimeMillis() - reviewIndex * 86400000L)));
                batch.set(db.collection("products").document(product[0])
                                .collection("reviews").document("sample" + (reviewIndex + 1)),
                        review);
            }
        }

        Map<String, Object> seedMarker = new HashMap<>();
        seedMarker.put("seededAt", Timestamp.now());
        batch.set(db.collection("meta").document("seed_v2"), seedMarker);
        batch.commit()
                .addOnSuccessListener(unused -> onDone.run())
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Không thể ghi dữ liệu sản phẩm mẫu", exception);
                    onDone.run();
                });
    }

    private static List<Integer> getSizes(String gender) {
        List<Integer> sizes = new ArrayList<>();
        int first = "men".equals(gender) ? 39 : "women".equals(gender) ? 36 : 30;
        int last = "men".equals(gender) ? 44 : "women".equals(gender) ? 41 : 35;
        for (int size = first; size <= last; size++) {
            if (size != first + 2 && size != last - 1) {
                sizes.add(size);
            }
        }
        return sizes;
    }

    private static List<Map<String, Object>> getColors(String productId, String[] names) {
        List<Map<String, Object>> colors = new ArrayList<>();
        for (int index = 0; index < names.length; index++) {
            Map<String, Object> color = new HashMap<>();
            color.put("name", names[index]);
            color.put("image", productId + "_" + (index + 1));
            colors.add(color);
        }
        return colors;
    }
}
