package com.arthur.downloaddialog.http.tools;

import android.util.Log;

public class EasyLog {
    private static boolean ISSHOW = false;

    public static void setDebugMode(boolean isShow) {
        ISSHOW = isShow;
    }

    public static void d(String msg) {
        d("", msg);
    }

    public static void e(String msg) {
        e("", msg);
    }

    public static void i(String msg) {
        i("", msg);
    }

    public static void w(String msg) {
        w("", msg);
    }

    public static void d(String tag, String msg) {
        if (ISSHOW)
            Log.d(">>> EasyHttp <<<", tag + " >> " + msg);
    }

    public static void e(String tag, String msg) {
        if (ISSHOW)
            Log.e(">>> EasyHttp <<<", tag + " >> " + msg);
    }

    public static void e(String tag, String msg, Throwable e) {
        if (ISSHOW)
            Log.e(">>> EasyHttp <<<", tag + " >> " + msg, e);
    }

    public static void i(String tag, String msg) {
        if (ISSHOW)
            Log.i(">>> EasyHttp <<<", tag + " >> " + msg);
    }

    public static void w(String tag, String msg) {
        if (ISSHOW)
            Log.w(">>> EasyHttp <<<", tag + " >> " + msg);
    }
}
