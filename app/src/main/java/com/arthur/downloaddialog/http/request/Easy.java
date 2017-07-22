package com.arthur.downloaddialog.http.request;

import android.content.Context;
import android.util.Log;

import com.arthur.downloaddialog.http.db.EasyKvDb;
import com.arthur.downloaddialog.http.tools.CheckTool;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadHelper;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


/**
 * 网络操作入口
 */
public class Easy {


    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_NOT_AUTHORITATIVE = 203;
    public static final int HTTP_NO_CONTENT = 204;
    public static final int HTTP_RESET = 205;
    public static final int HTTP_PARTIAL = 206;
    public static final int HTTP_MULT_CHOICE = 300;
    public static final int HTTP_MOVED_PERM = 301;
    public static final int HTTP_MOVED_TEMP = 302;
    public static final int HTTP_SEE_OTHER = 303;
    public static final int HTTP_NOT_MODIFIED = 304;
    public static final int HTTP_USE_PROXY = 305;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_PAYMENT_REQUIRED = 402;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_BAD_METHOD = 405;
    public static final int HTTP_NOT_ACCEPTABLE = 406;
    public static final int HTTP_PROXY_AUTH = 407;
    public static final int HTTP_CLIENT_TIMEOUT = 408;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_GONE = 410;
    public static final int HTTP_LENGTH_REQUIRED = 411;
    public static final int HTTP_PRECON_FAILED = 412;
    public static final int HTTP_ENTITY_TOO_LARGE = 413;
    public static final int HTTP_REQ_TOO_LONG = 414;
    public static final int HTTP_UNSUPPORTED_TYPE = 415;
    public static final int HTTP_SERVER_ERROR = 500;
    public static final int HTTP_INTERNAL_ERROR = 500;
    public static final int HTTP_NOT_IMPLEMENTED = 501;
    public static final int HTTP_BAD_GATEWAY = 502;
    public static final int HTTP_UNAVAILABLE = 503;
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    public static final int HTTP_VERSION = 505;



    /**
     * init 方法，需要在application中启动是调用
     *
     * @param context
     */
    public static void init(Context context) {
        EasyKvDb.init(context.getApplicationContext());
        FileDownloader.init(context.getApplicationContext(),
                new FileDownloadHelper.OkHttpClientCustomMaker() { // is not has to provide.
                    @Override
                    public OkHttpClient customMake() {
                        // just for OkHttpClient customize.
                        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
                        // you can set the connection timeout.
                        builder.connectTimeout(15_000, TimeUnit.MILLISECONDS);
                        // you can set the HTTP proxy.
                        builder.proxy(Proxy.NO_PROXY);
                        // etc.
                        return builder.build();
                    }
                });
    }

    /**
     * 加载
     *
     * @param context 上下文，如果需要弹起转轮，则需要传入Activity
     * @param url     请求的地址
     */
    public static EasyBuilder load(Context context, String url) {
        if (context == null || CheckTool.isEmpty(url)) {
            Log.e("Easy", "context is null or url is null");
            throw new RuntimeException("context is null or url is null");
        }
        if (!EasyKvDb.isInit()) {
            throw new RuntimeException("please call init in application onCreate()");
        }

        return new EasyBuilder(context, url);
    }

    /**
     * 取消当前网络操作
     */
    public static void cancelRequest(String url) {
        EasyRequest.getInstence().cancelRequest(url);
    }

    /**
     * 暂停当前下载
     *
     * @param url
     */
    public static void pauseDownload(String url) {
        EasyDownloadRequest.getInstence().pauseDownload(url);
    }

    /**
     * 暂停全部下载
     */
    public static void pauseAllDownload() {
        EasyDownloadRequest.getInstence().pauseAllDownload();
    }
}
