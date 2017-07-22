package com.arthur.downloaddialog.http.request;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.arthur.downloaddialog.http.cookie.ClearableCookieJar;
import com.arthur.downloaddialog.http.cookie.PersistentCookieJar;
import com.arthur.downloaddialog.http.cookie.cache.SetCookieCache;
import com.arthur.downloaddialog.http.cookie.persistence.SharedPrefsCookiePersistor;
import com.arthur.downloaddialog.http.tools.EasyLog;
import com.arthur.downloaddialog.http.tools.NetWorkTool;
import com.arthur.downloaddialog.http.ui.EasyProgressBar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 文件上传
 */
public class EasyUpload {

    private final static String TAG = EasyUpload.class.getSimpleName();

    private final EasyBuilder builder;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Gson gson = new Gson();

    private final JsonParser jsonParser = new JsonParser();

    private final BlockingQueue<File> queue;

    private String name;

    private EasyUploadListener uploadListener;

    public EasyUpload(EasyBuilder builder) {
        this.builder = builder;
        this.queue = new LinkedBlockingQueue<>();
    }

    private static Hashtable<String, String> mContentTypes = new Hashtable<String, String>();

    {
        mContentTypes.put("js", "application/javascript");
        mContentTypes.put("json", "application/json");
        mContentTypes.put("png", "image/png");
        mContentTypes.put("jpg", "image/jpeg");
        mContentTypes.put("jpeg", "image/jpeg");
        mContentTypes.put("html", "text/html");
        mContentTypes.put("css", "text/css");
        mContentTypes.put("mp4", "video/mp4");
        mContentTypes.put("mov", "video/quicktime");
        mContentTypes.put("wmv", "video/x-ms-wmv");
    }

    private String getContentType(String path) {
        String type = tryGetContentType(path);
        if (type != null)
            return type;
        return "text/plain";
    }

    private String tryGetContentType(String path) {
        int index = path.lastIndexOf(".");
        if (index != -1) {
            String e = path.substring(index + 1);
            String ct = mContentTypes.get(e);
            if (ct != null)
                return ct;
        }
        return null;
    }

    public <T> void executeAsync(String name, final EasyUploadListener<T> uploadListener) {
        this.builder.async = true;

        execute(name, uploadListener);
    }

    public <T> void executeSync(String name, final EasyUploadListener<T> uploadListener) {
        this.builder.async = false;

        execute(name, uploadListener);
    }

    private <T> void execute(String name, final EasyUploadListener<T> uploadListener) {
        this.name = name;
        this.uploadListener = uploadListener;

        if (builder.isShowBar && builder.context instanceof Activity) {
            EasyProgressBar.getInstance().startProgressBar((Activity) builder.context, builder.barMessage, builder.canCancel, builder.canFinish, new EasyProgressBar.EasyProgressListener() {
                @Override
                public void cancel() {
                    for (File file : queue) {
                        if (uploadListener != null) {
                            uploadListener.cancel(file);
                        }
                    }
                    queue.clear();
                    removeUpload();
                }
            });
        }

        for (File file : builder.uploadFiles) {
            queue.offer(file);
        }

        task();
    }

    private void task() {
        File file = queue.poll();
        if (file == null) {
            return;
        }
        upload(name, file);
    }

    private void upload(String name, final File file) {
        synchronized (this) {
            try {
                EasyLog.i(TAG, "upload: " + this.builder.toString() + "\nfile: " + file.getPath());
                //判断网络是否正常
                if (!NetWorkTool.networkCanUse(builder.context.getApplicationContext())) {
                    uploadListener.netError();
                    EasyLog.e(TAG, "new error file: " + file.getPath());
                    removeUpload();
                    return;
                }

                ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(builder.context.getApplicationContext()));
                final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(builder.timeOut, TimeUnit.MILLISECONDS).readTimeout(Integer.MAX_VALUE, TimeUnit.MILLISECONDS).writeTimeout(Integer.MAX_VALUE, TimeUnit.MILLISECONDS).cookieJar(cookieJar).build();

                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);

                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + name + "\"; filename=\"" + file.getName() + "\""), RequestBody.create(MediaType.parse(getContentType(file.getPath())), file));

                MultipartBody body = builder.build();

                Request.Builder requestBuilder = new Request.Builder();

                final Request request = requestBuilder.url(this.builder.requestUrl).method("POST", body).build();
                Call call = client.newCall(request);
                EasyProgressBar.getInstance().addCall(call);
                if (this.builder.async) {
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, final IOException e) {
                            EasyLog.e(TAG, "upload error", e);
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (uploadListener != null) {
                                        uploadListener.cancel(file);
                                    }
                                    removeUpload();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            final String responseBody = response.body().string();
                            if (!response.isSuccessful()) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (uploadListener != null) {
                                            uploadListener.error(file, null, response.code(), response.message(), responseBody);
                                        }
                                        EasyLog.e(TAG, "async upload error: code: " + response.code() + " msg: " + response.message() + " body: " + responseBody);
                                        removeUpload();
                                    }
                                });
                                return;
                            }
                            requestCallBack(responseBody, file, response.headers().toMultimap(), true);
                        }
                    });
                } else {
                    try {
                        Response response = call.execute();
                        final String responseBody = response.body().string();
                        if (response.isSuccessful()) {
                            requestCallBack(responseBody, file, response.headers().toMultimap(), false);
                        } else {
                            if (uploadListener != null) {
                                uploadListener.error(file, null, response.code(), response.message(), responseBody);
                            }
                            EasyLog.e(TAG, "sync upload error: code: " + response.code() + " msg: " + response.message() + " body: " + responseBody);
                            removeUpload();
                        }
                    } catch (IOException e) {
                        if (uploadListener != null) {
                            uploadListener.error(file, e, -1, "获取数据失败", null);
                        }
                        EasyLog.e(TAG, "sync upload error: 获取数据失败");
                        removeUpload();
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }


        }
    }

    private <T> void requestCallBack(final String body, final File file, final Map<String, List<String>> multimap, boolean async) {
        if (uploadListener == null) {
            removeUpload();
            return;
        }
        T t = null;
        Type type = uploadListener.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type argument = parameterizedType.getActualTypeArguments()[0];

            try {
                if (argument == JSONObject.class) {
                    t = (T) new JSONObject(body);
                } else if (argument == JSONArray.class) {
                    t = (T) new JSONArray(body);
                } else if (argument == JsonObject.class) {
                    t = (T) jsonParser.parse(body).getAsJsonObject();
                } else if (argument == JsonArray.class) {
                    t = (T) jsonParser.parse(body).getAsJsonArray();
                } else if (argument == String.class) {
                    t = (T) body;
                } else {
                    t = gson.fromJson(body, argument);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        uploadListener.error(file, new RuntimeException("解析数据失败"), -3, "解析数据失败", body);
                        EasyLog.e(TAG, "sync upload error: 获取数据失败");
                        removeUpload();
                    }
                });
                return;
            }
        } else {
            t = (T) body;
        }

        final T back = t;
        if (async) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    uploadListener.success(file, back, multimap);
                    EasyLog.i(TAG, "async upload success: " + file.getPath() + "\nbody: " + body);
                    removeUpload();
                }
            });
        } else {
            uploadListener.success(file, back, multimap);
            EasyLog.i(TAG, "sync upload success: " + file.getPath() + "\nbody: " + body);
            removeUpload();
        }
    }

    private <T> void removeUpload() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                task();

                if (queue.size() == 0) {
                    EasyProgressBar.getInstance().closeProgressBar();
                }
            }
        }, 200);
    }
}
