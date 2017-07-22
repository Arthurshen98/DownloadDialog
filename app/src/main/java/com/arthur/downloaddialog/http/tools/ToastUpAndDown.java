package com.arthur.downloaddialog.http.tools;

import android.content.Context;
import android.widget.Toast;

public class ToastUpAndDown {

    private static boolean ISSHOW = false;

    public static void setDebugMode(boolean isShow) {
        ISSHOW = isShow;
    }

    public static void debug(Context ctx, final String msg) {
        if (ISSHOW)
            toast(ctx, "调试：" + msg);
    }

    public static void toast(Context ctx, final String msg) {
        toast(ctx, msg, Toast.LENGTH_SHORT);
    }

    public static void toast(Context ctx, String msg, int time) {
        Toast.makeText(ctx, msg, time).show();
    }
}
