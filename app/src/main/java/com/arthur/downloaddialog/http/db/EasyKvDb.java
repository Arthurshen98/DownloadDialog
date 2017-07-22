package com.arthur.downloaddialog.http.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.arthur.downloaddialog.http.tools.CheckTool;
import com.arthur.downloaddialog.http.tools.EasyLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EasyKvDb {

    private final static String TAG = EasyKvDb.class.getName();

    private static DatabaseHelper db;

    private final static String createKV = "create table kv (k text primary key, v text)";

    private final static String insert = "insert into kv values(?,?)";
    private final static String update = "update kv set v=? where k=?";
    private final static String delete = "delete from kv where k=?";
    private final static String query_key_like = "select k from kv where k like ?";
    private final static String query_value_like = "select v from kv where k like ?";
    private final static String query = "select v from kv where k=?";
    private final static String query_key_by_value = "select k from kv where v=?";

    private final static Map<String, Object> cache = new ConcurrentHashMap<String, Object>();

    public synchronized static void init(Context context) {
        if (context != null && db == null) {
            db = new DatabaseHelper(context.getApplicationContext());
        }
    }

    public static boolean isInit(){
        return db != null;
    }

    public static void close() {
        cache.clear();
        getDb().close();
    }

    public static boolean save(final String key, final String value) {
        if (key != null) {
            cache.remove(key);
            try {
                if (found(key)) {
                    getDb().getWritableDatabase().execSQL(update,
                            new String[]{value, key});
                } else {
                    getDb().getWritableDatabase().execSQL(insert,
                            new String[]{key, value});
                }
                return true;
            } catch (Exception e) {
                EasyLog.w(TAG, e.getMessage());
            }
        }
        return false;
    }

    public static List<String> listKeys(String value) {
        List<String> tmp = new ArrayList<String>();
        if (value != null) {
            Cursor cursor = getDb().getReadableDatabase().rawQuery(
                    query_key_like, new String[]{value});
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    tmp.add(cursor.getString(0));
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return tmp;
    }

    public static List<String> queryKeysByValue(String value) {
        List<String> tmp = new ArrayList<String>();
        if (value != null) {
            Cursor cursor = getDb().getReadableDatabase().rawQuery(
                    query_key_by_value, new String[]{value});
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    tmp.add(cursor.getString(0));
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return tmp;
    }

    public static List<String> listValues(String key) {
        List<String> tmp = new ArrayList<String>();
        if (key != null) {
            Cursor cursor = getDb().getReadableDatabase().rawQuery(
                    query_value_like, new String[]{key});
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    tmp.add(cursor.getString(0));
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return tmp;
    }

    public static void cache(String k, Object v) {
        cache.put(k, v);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getCache(String k) {
        Object obj = cache.get(k);
        return (T) obj;
    }

    public static String read(final String key) {
        String value = getCache(key);
        if (value == null) {
            if (key != null) {
                Cursor cursor = getDb().getReadableDatabase().rawQuery(query, new String[]{key});
                if (cursor.moveToFirst()) {
                    value = cursor.getString(0);
                }
                cursor.close();
            }
            if (!CheckTool.isEmpty(value))
                cache.put(key, value);
        }
        return value;
    }

    public static boolean found(final String key) {
        Cursor cursor = getDb().getReadableDatabase().rawQuery(query,
                new String[]{key});
        boolean r = cursor.moveToFirst();
        cursor.close();
        return r;
    }

    public static void delete(final String key) {
        getDb().getWritableDatabase().execSQL(delete, new String[]{key});
        cache.remove(key);
    }

    public static void deleteLike(final String key) {
        if (key != null) {
            List<String> keys = listKeys(key);
            for (String k : keys) {
                delete(k);
            }
        }
    }

    private static DatabaseHelper getDb() {
        // while (db == null) {
        // try {
        // Thread.sleep(10);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        // }
        return db;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, "easyhttp.db", null, 1);
        }

        public void onCreate(SQLiteDatabase database) {
            try {
                database.execSQL(createKV);
            } catch (Exception e) {
                EasyLog.e(TAG, e.getMessage());
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}
