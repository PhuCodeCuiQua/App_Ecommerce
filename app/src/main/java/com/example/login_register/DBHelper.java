package com.example.login_register;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ShopDB.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PRODUCTS = "products";

    private final Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "description TEXT, " +
                "price REAL, " +
                "image TEXT)");

        insertProduct(db, "Aero Zoom", "Giày chạy bộ", 2390000, "ic_shoe_placeholder");
        insertProduct(db, "Aero Max", "Giày chạy bộ", 2790000, "ic_shoe_placeholder");
        insertProduct(db, "Aero Edge", "Giày thể thao", 2990000, "ic_shoe_placeholder");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    public long insertProduct(String name, String description, double price, String imageName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("price", price);
        values.put("image", imageName);
        return db.insert(TABLE_PRODUCTS, null, values);
    }

    private void insertProduct(SQLiteDatabase db, String name, String description,
                               double price, String imageName) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("price", price);
        values.put("image", imageName);
        db.insert(TABLE_PRODUCTS, null, values);
    }

    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from products", null);
        try {
            int idIndex = cursor.getColumnIndexOrThrow("id");
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            int descriptionIndex = cursor.getColumnIndexOrThrow("description");
            int priceIndex = cursor.getColumnIndexOrThrow("price");
            int imageIndex = cursor.getColumnIndexOrThrow("image");

            while (cursor.moveToNext()) {
                String imageName = cursor.getString(imageIndex);
                int imageResId = context.getResources().getIdentifier(
                        imageName, "drawable", context.getPackageName());
                products.add(createProduct(String.valueOf(cursor.getInt(idIndex)),
                        cursor.getString(nameIndex), cursor.getString(descriptionIndex),
                        cursor.getDouble(priceIndex), imageName, imageResId));
            }
        } finally {
            cursor.close();
        }
        return products;
    }

    public Product getProduct(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from products where id = ?",
                new String[]{String.valueOf(id)});
        try {
            if (!cursor.moveToNext()) {
                return null;
            }

            int nameIndex = cursor.getColumnIndexOrThrow("name");
            int descriptionIndex = cursor.getColumnIndexOrThrow("description");
            int priceIndex = cursor.getColumnIndexOrThrow("price");
            int imageIndex = cursor.getColumnIndexOrThrow("image");
            String imageName = cursor.getString(imageIndex);
            int imageResId = context.getResources().getIdentifier(
                    imageName, "drawable", context.getPackageName());

            return createProduct(
                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))),
                    cursor.getString(nameIndex), cursor.getString(descriptionIndex),
                    cursor.getDouble(priceIndex), imageName, imageResId);
        } finally {
            cursor.close();
        }
    }

    private Product createProduct(String id, String name, String description, double price,
                                  String imageName, int imageResId) {
        int resolvedImage = imageResId == 0 ? R.drawable.ic_shoe_placeholder : imageResId;
        ProductColor color = new ProductColor("Mặc định", imageName, resolvedImage);
        return new Product(id, name, description, "", "", price, 0, description,
                "", "", "", Collections.emptyList(), Collections.singletonList(color),
                0, 0, 0);
    }
}
