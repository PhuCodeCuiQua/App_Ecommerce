package com.example.login_register;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CartManager {
    private static final String TAG = "CartManager";
    private static final Map<String, CartItem> items = new LinkedHashMap<>();
    private static final List<Runnable> loadCallbacks = new ArrayList<>();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static String loadedUserKey;
    private static String loadingUserKey;

    private CartManager() {
    }

    public static void add(Product product, ProductColor color, int size) {
        CartItem candidate = new CartItem(product, color, size, 1);
        CartItem item = items.get(candidate.getKey());
        if (item == null) {
            item = candidate;
            items.put(item.getKey(), item);
        } else {
            item.setQuantity(item.getQuantity() + 1);
        }
        saveItem(item);
    }

    public static void add(Product product) {
        List<ProductColor> colors = product.getColors();
        List<Integer> sizes = product.getSizes();
        add(product, colors.isEmpty() ? null : colors.get(0),
                sizes.isEmpty() ? 0 : sizes.get(0));
    }

    public static void increase(String key) {
        CartItem item = items.get(key);
        if (item != null) {
            item.setQuantity(item.getQuantity() + 1);
            saveItem(item);
        }
    }

    public static void decrease(String key) {
        CartItem item = items.get(key);
        if (item != null && item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            saveItem(item);
        }
    }

    public static void remove(String key) {
        items.remove(key);
        cartCollection().document(toDocumentId(key)).delete()
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Error deleting cart item", exception));
    }

    public static List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public static double getTotal() {
        double total = 0;
        for (CartItem item : items.values()) {
            total += item.getProduct().getSalePrice() * item.getQuantity();
        }
        return total;
    }

    public static int getTotalQuantity() {
        int quantity = 0;
        for (CartItem item : items.values()) {
            quantity += item.getQuantity();
        }
        return quantity;
    }

    public static void clearMemory() {
        synchronized (CartManager.class) {
            items.clear();
            loadCallbacks.clear();
            loadedUserKey = null;
            loadingUserKey = null;
        }
    }

    public static void clear() {
        items.clear();
        cartCollection().get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        document.getReference().delete().addOnFailureListener(exception ->
                                Log.e(TAG, "Error clearing cart item", exception));
                    }
                })
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Error loading cart to clear", exception));
    }

    public static void load(Runnable onDone) {
        load(FirebaseApp.getInstance().getApplicationContext(), onDone);
    }

    public static void load(Context context, Runnable onDone) {
        String userKey = SharePreferance.getUserKey();
        synchronized (CartManager.class) {
            if (userKey.equals(loadedUserKey)) {
                postDone(onDone);
                return;
            }
            if (userKey.equals(loadingUserKey)) {
                addLoadCallback(onDone);
                return;
            }
            items.clear();
            loadingUserKey = userKey;
            loadCallbacks.clear();
            addLoadCallback(onDone);
        }

        cartCollection(userKey).get()
                .addOnSuccessListener(snapshot -> loadProducts(context, userKey, snapshot))
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Error loading cart", exception);
                    finishLoad(userKey, null, false);
                });
    }

    private static void loadProducts(Context context, String userKey, QuerySnapshot snapshot) {
        if (snapshot.isEmpty()) {
            finishLoad(userKey, new LinkedHashMap<>(), true);
            return;
        }

        Map<String, CartItem> loadedItems = new LinkedHashMap<>();
        int[] pending = {snapshot.size()};
        for (DocumentSnapshot cartDocument : snapshot.getDocuments()) {
            String productId = cartDocument.getString("productId");
            if (productId == null) {
                productId = cartDocument.getId().split("_", 2)[0];
            }
            final String loadedProductId = productId;
            FirebaseFirestore.getInstance().collection("products").document(productId).get()
                    .addOnSuccessListener(productDocument -> {
                        if (!productDocument.exists()) {
                            Log.e(TAG, "Cart product does not exist: " + loadedProductId);
                        } else {
                            Product product = ProductMapper.fromDocument(productDocument, context);
                            String colorName = cartDocument.getString("colorName");
                            ProductColor color = findColor(product, colorName);
                            Long savedSize = cartDocument.getLong("size");
                            int size = savedSize == null ? firstSize(product)
                                    : savedSize.intValue();
                            Long savedQuantity = cartDocument.getLong("quantity");
                            CartItem item = new CartItem(product, color, size,
                                    savedQuantity == null ? 1
                                            : Math.max(1, savedQuantity.intValue()));
                            loadedItems.put(item.getKey(), item);
                        }
                        finishOneProduct(userKey, loadedItems, pending);
                    })
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "Error loading cart product " + loadedProductId, exception);
                        finishOneProduct(userKey, loadedItems, pending);
                    });
        }
    }

    private static ProductColor findColor(Product product, String name) {
        for (ProductColor color : product.getColors()) {
            if (name != null && name.equals(color.getName())) {
                return color;
            }
        }
        List<ProductColor> colors = product.getColors();
        return colors.isEmpty() ? null : colors.get(0);
    }

    private static int firstSize(Product product) {
        List<Integer> sizes = product.getSizes();
        return sizes.isEmpty() ? 0 : sizes.get(0);
    }

    private static synchronized void finishOneProduct(String userKey,
                                                       Map<String, CartItem> loadedItems,
                                                       int[] pending) {
        pending[0]--;
        if (pending[0] == 0) {
            finishLoad(userKey, loadedItems, true);
        }
    }

    private static void saveItem(CartItem item) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("productId", item.getProduct().getId());
        data.put("colorName", item.getColorName());
        data.put("size", item.getSize());
        data.put("quantity", item.getQuantity());
        cartCollection().document(toDocumentId(item.getKey())).set(data)
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Error saving cart item", exception));
    }

    private static String toDocumentId(String key) {
        return key.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private static com.google.firebase.firestore.CollectionReference cartCollection() {
        return cartCollection(SharePreferance.getUserKey());
    }

    private static com.google.firebase.firestore.CollectionReference cartCollection(String userKey) {
        return FirebaseFirestore.getInstance()
                .collection("users").document(userKey).collection("cart");
    }

    private static void finishLoad(String userKey, Map<String, CartItem> loadedItems,
                                   boolean success) {
        List<Runnable> callbacks;
        synchronized (CartManager.class) {
            if (!userKey.equals(loadingUserKey)) {
                return;
            }
            if (success) {
                items.clear();
                items.putAll(loadedItems);
                loadedUserKey = userKey;
            }
            loadingUserKey = null;
            callbacks = new ArrayList<>(loadCallbacks);
            loadCallbacks.clear();
        }
        for (Runnable callback : callbacks) {
            postDone(callback);
        }
    }

    private static void addLoadCallback(Runnable callback) {
        if (callback != null) {
            loadCallbacks.add(callback);
        }
    }

    private static void postDone(Runnable callback) {
        if (callback != null) {
            mainHandler.post(callback);
        }
    }
}
