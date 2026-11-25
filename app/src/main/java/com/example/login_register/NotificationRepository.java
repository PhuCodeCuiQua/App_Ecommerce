package com.example.login_register;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    public static volatile boolean hasUnread;
    private static volatile long unreadVersion;

    public interface LoadCallback {
        void onSuccess(List<AppNotification> notifications);

        void onFailure(Exception exception);
    }

    private NotificationRepository() {
    }

    public static void add(String title, String message, String type) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);
        data.put("type", type);
        data.put("createdAt", Timestamp.now());
        data.put("read", false);
        unreadVersion++;
        hasUnread = true;
        notificationCollection().add(data)
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Không thể lưu thông báo", exception);
                    refreshUnread(null);
                });
    }

    public static void load(LoadCallback callback) {
        CollectionReference notifications = notificationCollection();
        notifications.orderBy("createdAt", Query.Direction.DESCENDING).limit(50).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        seedExamplesIfNeeded(notifications, callback);
                        return;
                    }
                    callback.onSuccess(mapNotifications(snapshot.getDocuments()));
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Không thể tải thông báo", exception);
                    callback.onFailure(exception);
                });
    }

    public static void markAllRead() {
        unreadVersion++;
        hasUnread = false;
        CollectionReference notifications = notificationCollection();
        notifications.get().addOnSuccessListener(snapshot -> {
            if (snapshot.isEmpty()) {
                hasUnread = false;
                return;
            }
            WriteBatch batch = FirebaseFirestore.getInstance().batch();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                batch.update(document.getReference(), "read", true);
            }
            batch.commit().addOnSuccessListener(unused -> hasUnread = false)
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Không thể đánh dấu đã đọc", exception));
        }).addOnFailureListener(exception ->
                Log.e(TAG, "Không thể tải thông báo để đánh dấu đã đọc", exception));
    }

    public static void refreshUnread(Consumer<Boolean> callback) {
        long requestVersion = unreadVersion;
        notificationCollection().whereEqualTo("read", false).limit(1).get()
                .addOnSuccessListener(snapshot -> {
                    if (requestVersion == unreadVersion) {
                        hasUnread = !snapshot.isEmpty();
                    }
                    if (callback != null) {
                        callback.accept(hasUnread);
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Không thể kiểm tra thông báo chưa đọc", exception);
                    if (callback != null) {
                        callback.accept(hasUnread);
                    }
                });
    }

    private static void seedExamplesIfNeeded(CollectionReference notifications,
                                             LoadCallback callback) {
        notifications.get().addOnSuccessListener(current -> {
            if (!current.isEmpty()) {
                callback.onSuccess(mapNotifications(current.getDocuments()));
                return;
            }
            FirebaseFirestore.getInstance().collection("users")
                    .document(SharePreferance.getUserKey()).collection("meta")
                    .document("notif_seed").get()
                    .addOnSuccessListener(seedMarker -> {
                        if (seedMarker.exists()) {
                            callback.onSuccess(new ArrayList<>());
                        } else {
                            writeExamples(notifications, callback);
                        }
                    })
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "Không thể kiểm tra thông báo mẫu", exception);
                        callback.onFailure(exception);
                    });
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Không thể kiểm tra danh sách thông báo", exception);
            callback.onFailure(exception);
        });
    }

    private static void writeExamples(CollectionReference notifications,
                                      LoadCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();
        addExample(batch, notifications, "Khuyến mãi",
                "Flash Sale cuối tuần: giảm đến 30%", "promo", 0);
        addExample(batch, notifications, "Chào mừng",
                "Chào mừng bạn đến với cửa hàng", "system", 1);
        addExample(batch, notifications, "Sản phẩm mới",
                "Sản phẩm mới đã về, xem ngay", "promo", 2);
        Map<String, Object> marker = new HashMap<>();
        marker.put("seededAt", Timestamp.now());
        batch.set(db.collection("users").document(SharePreferance.getUserKey())
                .collection("meta").document("notif_seed"), marker);
        batch.commit().addOnSuccessListener(unused ->
                notifications.orderBy("createdAt", Query.Direction.DESCENDING).limit(50).get()
                        .addOnSuccessListener(snapshot ->
                                callback.onSuccess(mapNotifications(snapshot.getDocuments())))
                        .addOnFailureListener(exception -> {
                            Log.e(TAG, "Không thể tải thông báo mẫu", exception);
                            callback.onFailure(exception);
                        }))
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Không thể tạo thông báo mẫu", exception);
                    callback.onFailure(exception);
                });
    }

    private static void addExample(WriteBatch batch, CollectionReference notifications,
                                   String title, String message, String type, int minutesAgo) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);
        data.put("type", type);
        data.put("createdAt", new Timestamp(
                new Date(System.currentTimeMillis() - minutesAgo * 60000L)));
        data.put("read", false);
        batch.set(notifications.document(), data);
    }

    private static List<AppNotification> mapNotifications(
            List<? extends DocumentSnapshot> documents) {
        List<AppNotification> result = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            Timestamp createdAt = document.getTimestamp("createdAt");
            Boolean read = document.getBoolean("read");
            result.add(new AppNotification(document.getId(), string(document, "title"),
                    string(document, "message"), string(document, "type"),
                    createdAt == null ? null : createdAt.toDate(),
                    Boolean.TRUE.equals(read)));
        }
        return result;
    }

    private static String string(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }

    private static CollectionReference notificationCollection() {
        return FirebaseFirestore.getInstance().collection("users")
                .document(SharePreferance.getUserKey()).collection("notifications");
    }
}
