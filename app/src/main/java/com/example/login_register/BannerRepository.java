package com.example.login_register;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public final class BannerRepository {
    private static final String TAG = "BannerRepository";

    public interface LoadCallback {
        void onSuccess(List<Banner> banners);

        void onFailure(Exception exception);
    }

    private BannerRepository() {
    }

    public static void loadBanners(LoadCallback callback) {
        FirebaseFirestore.getInstance().collection("banners")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Banner> banners = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Boolean active = document.getBoolean("active");
                        if (!Boolean.TRUE.equals(active)) {
                            continue;
                        }
                        Number order = document.getLong("order");
                        banners.add(new Banner(value(document, "tagline"),
                                value(document, "title"), value(document, "accentWord"),
                                value(document, "subtitle"), value(document, "buttonText"),
                                value(document, "productId"), order == null ? 0 : order.intValue(),
                                true));
                    }
                    callback.onSuccess(banners);
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Không thể tải banner", exception);
                    callback.onFailure(exception);
                });
    }

    private static String value(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }
}
