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

public final class FavoriteManager {
    private static final String TAG = "FavoriteManager";
    private static final Map<String, Product> favorites = new LinkedHashMap<>();
    private static final List<Runnable> loadCallbacks = new ArrayList<>();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static String loadedUserKey;
    private static String loadingUserKey;

    private FavoriteManager() {
    }

    public static boolean isFavorite(String id) {
        return favorites.containsKey(id);
    }

    public static void toggle(Product product) {
        if (isFavorite(product.getId())) {
            remove(product.getId());
        } else {
            favorites.put(product.getId(), product);
            saveFavorite(product);
        }
    }

    public static void remove(String id) {
        favorites.remove(id);
        favoriteCollection().document(id).delete()
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Error deleting favorite", exception));
    }

    public static List<Product> getFavorites() {
        return new ArrayList<>(favorites.values());
    }

    public static void clearMemory() {
        synchronized (FavoriteManager.class) {
            favorites.clear();
            loadCallbacks.clear();
            loadedUserKey = null;
            loadingUserKey = null;
        }
    }

    public static void load(Runnable onDone) {
        load(FirebaseApp.getInstance().getApplicationContext(), onDone);
    }

    public static void load(Context context, Runnable onDone) {
        String userKey = SharePreferance.getUserKey();
        synchronized (FavoriteManager.class) {
            if (userKey.equals(loadedUserKey)) {
                postDone(onDone);
                return;
            }
            if (userKey.equals(loadingUserKey)) {
                addLoadCallback(onDone);
                return;
            }
            favorites.clear();
            loadingUserKey = userKey;
            loadCallbacks.clear();
            addLoadCallback(onDone);
        }

        favoriteCollection(userKey).get()
                .addOnSuccessListener(snapshot -> loadProducts(context, userKey, snapshot))
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Error loading favorites", exception);
                    finishLoad(userKey, null, false);
                });
    }

    private static void loadProducts(Context context, String userKey, QuerySnapshot snapshot) {
        if (snapshot.isEmpty()) {
            finishLoad(userKey, new LinkedHashMap<>(), true);
            return;
        }
        Map<String, Product> loadedFavorites = new LinkedHashMap<>();
        int[] pending = {snapshot.size()};
        for (DocumentSnapshot favoriteDocument : snapshot.getDocuments()) {
            String productId = favoriteDocument.getString("productId");
            if (productId == null) {
                productId = favoriteDocument.getId();
            }
            final String loadedProductId = productId;
            FirebaseFirestore.getInstance().collection("products").document(productId).get()
                    .addOnSuccessListener(productDocument -> {
                        if (!productDocument.exists()) {
                            Log.e(TAG, "Favorite product does not exist: " + loadedProductId);
                        } else {
                            Product product = ProductMapper.fromDocument(productDocument, context);
                            loadedFavorites.put(product.getId(), product);
                        }
                        finishOneProduct(userKey, loadedFavorites, pending);
                    })
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "Error loading favorite product " + loadedProductId, exception);
                        finishOneProduct(userKey, loadedFavorites, pending);
                    });
        }
    }

    private static synchronized void finishOneProduct(String userKey,
                                                       Map<String, Product> loadedFavorites,
                                                       int[] pending) {
        pending[0]--;
        if (pending[0] == 0) {
            finishLoad(userKey, loadedFavorites, true);
        }
    }

    private static void saveFavorite(Product product) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("productId", product.getId());
        favoriteCollection().document(product.getId()).set(data)
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Error saving favorite", exception));
    }

    private static com.google.firebase.firestore.CollectionReference favoriteCollection() {
        return favoriteCollection(SharePreferance.getUserKey());
    }

    private static com.google.firebase.firestore.CollectionReference favoriteCollection(
            String userKey) {
        return FirebaseFirestore.getInstance()
                .collection("users").document(userKey).collection("favorites");
    }

    private static void finishLoad(String userKey, Map<String, Product> loadedFavorites,
                                   boolean success) {
        List<Runnable> callbacks;
        synchronized (FavoriteManager.class) {
            if (!userKey.equals(loadingUserKey)) {
                return;
            }
            if (success) {
                favorites.clear();
                favorites.putAll(loadedFavorites);
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
