package com.arthur.dialoglibrary.tool;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 * Created by arthur on 2017/7/22.
 * Author:shenfei
 * Email:shenxuanfei@163.com
 *
 */

public class ApkUtil {

    /**
     * 打开APK程序代码
     * @param file apk 路径
     * @param context
     */
    public static void openAppFile(File file, Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 获取app系统路径
     *
     * @param context
     * @return
     */
    public static String getAppSystemPath(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
            //        cachePath = context.getExternalCacheDir().getPath();//也可以这么写，只是返回的路径不一样，具体打log看
        } else {
            cachePath = context.getFilesDir().getAbsolutePath();
            //            cachePath = context.getCacheDir().getPath();//也可以这么写，只是返回的路径不一样，具体打log看
        }
        return cachePath;
    }
}
