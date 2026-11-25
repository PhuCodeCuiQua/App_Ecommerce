package com.example.login_register;

import android.content.Context;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OrderRepository {
    private static final String TAG = "OrderRepository";

    public interface LoadCallback {
        void onSuccess(List<Order> orders);

        void onFailure(Exception exception);
    }

    private OrderRepository() {
    }

    public static void saveOrder(Context context, Order order, Runnable onSuccess,
                                 Runnable onFailure) {
        Timestamp createdAt = Timestamp.now();
        String userKey = SharePreferance.getUserKey();
        Map<String, Object> data = toMap(order, createdAt);
        com.google.firebase.firestore.DocumentReference orderReference =
                FirebaseFirestore.getInstance().collection("users").document(userKey)
                        .collection("orders").document();
        order.setId(orderReference.getId());
        order.setCreatedAt(createdAt.toDate());
        order.setStatus("Đã thanh toán");
        orderReference
                .set(data)
                .addOnSuccessListener(unused -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Không thể lưu đơn hàng", exception);
                    if (onFailure != null) {
                        onFailure.run();
                    }
                });
    }

    public static void loadOrders(Context context, LoadCallback callback) {
        String userKey = SharePreferance.getUserKey();
        FirebaseFirestore.getInstance().collection("users").document(userKey)
                .collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Order> orders = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        orders.add(fromDocument(document));
                    }
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Không thể tải đơn hàng", exception);
                    callback.onFailure(exception);
                });
    }

    private static Map<String, Object> toMap(Order order, Timestamp createdAt) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("createdAt", createdAt);
        data.put("status", "Đã thanh toán");
        data.put("subtotal", order.getSubtotal());
        data.put("shippingFee", order.getShippingFee());
        data.put("discount", order.getDiscount());
        data.put("voucherCode", order.getVoucherCode());
        data.put("shippingMethod", order.getShippingMethod());
        data.put("paymentMethod", order.getPaymentMethod());
        data.put("address", order.getAddress());
        data.put("total", order.getTotal());

        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Map<String, Object> itemData = new LinkedHashMap<>();
            itemData.put("productId", item.getProductId());
            itemData.put("name", item.getName());
            itemData.put("colorName", item.getColorName());
            itemData.put("size", item.getSize());
            itemData.put("quantity", item.getQuantity());
            itemData.put("unitPrice", item.getUnitPrice());
            itemData.put("imageName", item.getImageName());
            items.add(itemData);
        }
        data.put("items", items);
        return data;
    }

    private static Order fromDocument(DocumentSnapshot document) {
        Timestamp createdAt = document.getTimestamp("createdAt");
        List<OrderItem> items = new ArrayList<>();
        Object rawItems = document.get("items");
        if (rawItems instanceof List<?>) {
            for (Object rawItem : (List<?>) rawItems) {
                if (rawItem instanceof Map<?, ?>) {
                    items.add(fromItemMap((Map<?, ?>) rawItem));
                }
            }
        }
        return new Order(document.getId(), createdAt == null ? null : createdAt.toDate(),
                stringValue(document.get("status")), numberValue(document.get("subtotal")),
                numberValue(document.get("shippingFee")), numberValue(document.get("discount")),
                stringValue(document.get("voucherCode")),
                stringValue(document.get("shippingMethod")),
                stringValue(document.get("paymentMethod")), stringValue(document.get("address")),
                numberValue(document.get("total")), items);
    }

    private static OrderItem fromItemMap(Map<?, ?> data) {
        return new OrderItem(stringValue(data.get("productId")), stringValue(data.get("name")),
                stringValue(data.get("colorName")), (int) numberValue(data.get("size")),
                (int) numberValue(data.get("quantity")), numberValue(data.get("unitPrice")),
                stringValue(data.get("imageName")));
    }

    private static String stringValue(Object value) {
        return value instanceof String ? (String) value : "";
    }

    private static double numberValue(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0;
    }
}
