package com.example.login_register;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ProfileDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ProfileDB.db";
    private static final int DB_VERSION = 3;

    public ProfileDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE addresses (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_key TEXT, name TEXT, phone TEXT, detail TEXT, is_default INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE cards (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_key TEXT, holder TEXT, last4 TEXT, expiry TEXT, brand TEXT)");
        db.execSQL("CREATE TABLE wallet (user_key TEXT PRIMARY KEY, balance REAL DEFAULT 0)");
        createProfileTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createProfileTables(db); // bảng viewed tạo mới đã có sẵn cột product_blob
        } else if (oldVersion < 3) {
            db.execSQL("ALTER TABLE viewed ADD COLUMN product_blob BLOB");
        }
    }

    private void createProfileTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS profile (user_key TEXT PRIMARY KEY, " +
                "full_name TEXT, phone TEXT, address TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS viewed (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_key TEXT, product_id TEXT, name TEXT, price REAL, viewed_at INTEGER, " +
                "product_blob BLOB)");
    }

    // ---------- Địa chỉ ----------
    public ArrayList<Address> getAddresses(String userKey) {
        ArrayList<Address> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, phone, detail, is_default FROM addresses " +
                        "WHERE user_key = ? ORDER BY is_default DESC, id DESC",
                new String[]{userKey});
        try {
            while (c.moveToNext()) {
                list.add(new Address(c.getLong(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getInt(4) == 1));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public void saveAddress(String userKey, Address a) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            boolean first = getAddresses(userKey).isEmpty();
            boolean makeDefault = a.isDefault || (a.id == 0 && first);
            if (makeDefault) {
                ContentValues clear = new ContentValues();
                clear.put("is_default", 0);
                db.update("addresses", clear, "user_key = ?", new String[]{userKey});
            }
            ContentValues v = new ContentValues();
            v.put("user_key", userKey);
            v.put("name", a.name);
            v.put("phone", a.phone);
            v.put("detail", a.detail);
            v.put("is_default", makeDefault ? 1 : 0);
            if (a.id == 0) {
                db.insert("addresses", null, v);
            } else {
                if (!makeDefault) v.remove("is_default");
                db.update("addresses", v, "id = ? AND user_key = ?",
                        new String[]{String.valueOf(a.id), userKey});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void setDefaultAddress(String userKey, long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues clear = new ContentValues();
            clear.put("is_default", 0);
            db.update("addresses", clear, "user_key = ?", new String[]{userKey});
            ContentValues set = new ContentValues();
            set.put("is_default", 1);
            db.update("addresses", set, "id = ? AND user_key = ?",
                    new String[]{String.valueOf(id), userKey});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteAddress(String userKey, Address a) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("addresses", "id = ? AND user_key = ?",
                new String[]{String.valueOf(a.id), userKey});
        if (a.isDefault) {
            ArrayList<Address> rest = getAddresses(userKey);
            if (!rest.isEmpty()) setDefaultAddress(userKey, rest.get(0).id);
        }
    }

    // ---------- Thẻ ----------
    public ArrayList<Card> getCards(String userKey) {
        ArrayList<Card> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, holder, last4, expiry, brand FROM cards WHERE user_key = ? ORDER BY id DESC",
                new String[]{userKey});
        try {
            while (c.moveToNext()) {
                list.add(new Card(c.getLong(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4)));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public void addCard(String userKey, Card card) {
        ContentValues v = new ContentValues();
        v.put("user_key", userKey);
        v.put("holder", card.holder);
        v.put("last4", card.last4);
        v.put("expiry", card.expiry);
        v.put("brand", card.brand);
        getWritableDatabase().insert("cards", null, v);
    }

    public void deleteCard(String userKey, long id) {
        getWritableDatabase().delete("cards", "id = ? AND user_key = ?",
                new String[]{String.valueOf(id), userKey});
    }

    // ---------- Ví ----------
    public double getBalance(String userKey) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT balance FROM wallet WHERE user_key = ?", new String[]{userKey});
        try {
            return c.moveToFirst() ? c.getDouble(0) : 0;
        } finally {
            c.close();
        }
    }

    public void addBalance(String userKey, double amount) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT OR IGNORE INTO wallet (user_key, balance) VALUES (?, 0)",
                new Object[]{userKey});
        db.execSQL("UPDATE wallet SET balance = balance + ? WHERE user_key = ?",
                new Object[]{amount, userKey});
    }

    // ---------- Thông tin cá nhân ----------
    // Trả về {họ tên, số điện thoại, địa chỉ}
    public String[] getProfile(String userKey) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT full_name, phone, address FROM profile WHERE user_key = ?",
                new String[]{userKey});
        try {
            if (c.moveToFirst()) {
                return new String[]{nz(c.getString(0)), nz(c.getString(1)), nz(c.getString(2))};
            }
            return new String[]{"", "", ""};
        } finally {
            c.close();
        }
    }

    public void saveProfile(String userKey, String fullName, String phone, String address) {
        ContentValues v = new ContentValues();
        v.put("user_key", userKey);
        v.put("full_name", fullName);
        v.put("phone", phone);
        v.put("address", address);
        getWritableDatabase().insertWithOnConflict("profile", null, v,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    // ---------- Lịch sử xem ----------
    public void addViewed(String userKey, Product product) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("viewed", "user_key = ? AND product_id = ?",
                new String[]{userKey, product.getId()});
        ContentValues v = new ContentValues();
        v.put("user_key", userKey);
        v.put("product_id", product.getId());
        v.put("name", product.getName());
        v.put("price", product.getSalePrice());
        v.put("viewed_at", System.currentTimeMillis());
        v.put("product_blob", serialize(product));
        db.insert("viewed", null, v);
        // Chỉ giữ 50 sản phẩm xem gần nhất
        db.execSQL("DELETE FROM viewed WHERE user_key = ? AND id NOT IN " +
                        "(SELECT id FROM viewed WHERE user_key = ? ORDER BY viewed_at DESC LIMIT 50)",
                new Object[]{userKey, userKey});
    }

    public ArrayList<ViewedProduct> getViewed(String userKey) {
        ArrayList<ViewedProduct> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT product_id, name, price, viewed_at, product_blob FROM viewed " +
                        "WHERE user_key = ? ORDER BY viewed_at DESC", new String[]{userKey});
        try {
            while (c.moveToNext()) {
                list.add(new ViewedProduct(c.getString(0), c.getString(1),
                        c.getDouble(2), c.getLong(3), deserialize(c.getBlob(4))));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public void clearViewed(String userKey) {
        getWritableDatabase().delete("viewed", "user_key = ?", new String[]{userKey});
    }

    private byte[] serialize(Product product) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bytes);
            out.writeObject(product);
            out.close();
            return bytes.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private Product deserialize(byte[] data) {
        if (data == null) return null;
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            Object obj = in.readObject();
            in.close();
            return obj instanceof Product ? (Product) obj : null;
        } catch (Exception e) {
            return null;
        }
    }
}