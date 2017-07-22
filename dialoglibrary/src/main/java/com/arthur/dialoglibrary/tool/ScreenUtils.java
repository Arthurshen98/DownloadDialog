package com.arthur.dialoglibrary.tool;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * 获取屏幕宽高
 */
public class ScreenUtils {

    public static int getWidth(Context activity){
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();

        return dm.widthPixels;
    }

    public static int getHeight(Context activity){
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();

        return dm.heightPixels;
    }
}
