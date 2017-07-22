package com.arthur.downloaddialog;

import android.app.Application;

import com.arthur.downloaddialog.http.request.Easy;
import com.arthur.downloaddialog.http.tools.EasyLog;

/**
 * Created by arthur on 2017/7/21.
 * Author:shenfei
 * Email:shenxuanfei@163.com
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化上传下载
        EasyLog.setDebugMode(true);
        Easy.init(this);
    }
}
