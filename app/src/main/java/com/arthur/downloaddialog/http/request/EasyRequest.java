package com.arthur.downloaddialog.http.request;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.arthur.downloaddialog.http.cookie.ClearableCookieJar;
import com.arthur.downloaddialog.http.cookie.PersistentCookieJar;
import com.arthur.downloaddialog.http.cookie.cache.SetCookieCache;
import com.arthur.downloaddialog.http.cookie.persistence.SharedPrefsCookiePersistor;
import com.arthur.downloaddialog.http.tools.CheckTool;
import com.arthur.downloaddialog.http.tools.EasyLog;
import com.arthur.downloaddialog.http.tools.NetWorkTool;
import com.arthur.downloaddialog.http.tools.ToastUpAndDown;
import com.arthur.downloaddialog.http.ui.EasyProgressBar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 网络操作实现类
 */
public class EasyRequest {

    private Dialog progressBarDialog;

    private OkHttpClient client;


    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Handler maniHandler = new Handler(Looper.getMainLooper());

    private Map<String, Call> callMap = new Hashtable<String, Call>();

    private Gson gson = new Gson();
    private JsonParser jsonParser = new JsonParser();

    private EasyRequest() {
    }

    private static EasyRequest easyRequest;

    protected static synchronized EasyRequest getInstence() {
        if (easyRequest == null) {
            easyRequest = new EasyRequest();
        }
        return easyRequest;
    }

    /**
     * 请求入口
     *
     * @param builder             请求实体
     * @param easyLoadingListener 回调方法
     */
    protected <T> void request(final EasyBuilder builder, final EasyLoadingListener<T> easyLoadingListener) {
        if (builder == null) {
            ToastUpAndDown.toast(builder.context.getApplicationContext(), "请传入请求参数");
            Log.e("LOADING", "请传入请求参数", new RuntimeException("请传入请求参数"));
            return;
        }

        if (builder.async && builder.isShowBar && builder.context instanceof Activity) {
            EasyProgressBar.getInstance().startProgressBar((Activity) builder.context, builder.barMessage, builder.canCancel, builder.canFinish);
        }

        if (builder.localFirst) {
            getLocalData(builder, easyLoadingListener);
        } else {
            try {
                getNetData(builder, easyLoadingListener);
            } catch (IOException e) {
                if (easyLoadingListener != null) {
                    easyLoadingListener.error(e, -2, "请求出现异常", null, null);
                }
            }
        }
    }

    /**
     * 获取本地数据
     *
     * @param builder
     * @param easyLoadingListener
     */
    private <T> void getLocalData(final EasyBuilder builder, final EasyLoadingListener<T> easyLoadingListener) {
        EasyDbLoading.getInstance().getLocalDBData(builder, new EasyDbLoading.LibraryDbCallBack() {
            @Override
            public void dbCallBack(String data, boolean isToGetNet, boolean isHaveCache) {
                if (isHaveCache) {
                    EasyProgressBar.getInstance().closeProgressBar();
                    requestCallBack(data, true, null, builder.async, easyLoadingListener);
                }

                if (isToGetNet) {
                    try {
                        getNetData(builder, new EasyLoadingListener<String>() {
                            @Override
                            public void netError() {
                                easyLoadingListener.netError();
                            }

                            @Override
                            public void success(boolean local, String result, Map<String, List<String>> headerMap) {
                                if (!CheckTool.isEmpty(result)) {
                                    EasyDbLoading.getInstance().saveLoace(builder, result);
                                }
                                requestCallBack(result, false, null, builder.async, easyLoadingListener);
                            }

                            @Override
                            public void error(Throwable e, int code, String error, String result, Map<String, List<String>> headerMap) {
                                easyLoadingListener.error(e, code, error, result, headerMap);
                            }
                        });
                    } catch (Throwable e) {
                        EasyLog.e("EasyRequest", "Get Net data error", e);
                    }
                }
            }
        });
    }

    /**
     * 获取网络数据
     *
     * @param builder
     * @param easyLoadingListener
     */
    private <T> void getNetData(final EasyBuilder builder, final EasyLoadingListener<T> easyLoadingListener) throws IOException {
        EasyLog.i("Easy", "call: " + builder.toString());
        //判断网络是否正常
        if (!NetWorkTool.networkCanUse(builder.context.getApplicationContext())) {
            EasyProgressBar.getInstance().closeProgressBar();
            easyLoadingListener.netError();
            return;
        }

        ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(builder.context.getApplicationContext()));
        client = new OkHttpClient.Builder().connectTimeout(builder.timeOut, TimeUnit.MILLISECONDS).cookieJar(cookieJar).build();

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(builder.requestUrl).build();
        if (!CheckTool.isEmpty(builder.headerMap)) {
            for (String key : builder.headerMap.keySet()) {
                requestBuilder.addHeader(key, builder.headerMap.get(key));
            }
        }

        RequestBody body = null;
        if (!CheckTool.isEmpty(builder.jsonString())) {
            body = RequestBody.create(JSON, builder.jsonString());
        } else if (!CheckTool.isEmpty(builder.parameters)) {
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (String key : builder.parameters.keySet()) {
                formBodyBuilder.add(key, builder.parameters.get(key));
            }
            body = formBodyBuilder.build();
        }

        final Request request = requestBuilder.url(builder.requestUrl).method(builder.method, body).build();

        Call call = client.newCall(request);
        callMap.put(builder.requestUrl, call);

        EasyProgressBar.getInstance().setCall(call);

        if (builder.async) {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    callMap.remove(call.request().url().toString());
                    maniHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            EasyProgressBar.getInstance().closeProgressBar();
                            if (easyLoadingListener != null) {
                                easyLoadingListener.error(e, -1, "请求失败", null, null);
                            }
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    final String responseBody = response.body().string();
                    EasyLog.i("Easy", "async onResponse: " + responseBody);
                    callMap.remove(call.request().url().toString());
                    maniHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            EasyProgressBar.getInstance().closeProgressBar();
                        }
                    });
                    if (!response.isSuccessful()) {
                        maniHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (easyLoadingListener != null) {
                                    easyLoadingListener.error(null, response.code(), response.message(), responseBody, null);
                                }
                            }
                        });
                        return;
                    }
                    requestCallBack(responseBody, false, response.headers().toMultimap(), true, easyLoadingListener);
                }
            });
        } else {
            Response response = call.execute();
            final String responseBody = response.body().string();
            EasyLog.i("Easy", "sync onResponse: " + responseBody);
            if (response.isSuccessful()) {
                requestCallBack(responseBody, false, response.headers().toMultimap(), false, easyLoadingListener);
            } else {
                if (easyLoadingListener != null) {
                    easyLoadingListener.error(null, response.code(), response.message(), responseBody, null);
                }
            }
        }
    }

    private <T> void requestCallBack(final String body, final boolean local, final Map<String, List<String>> multimap, boolean async, final EasyLoadingListener<T> easyLoadingListener) {
        if (easyLoadingListener == null) {
            return;
        }
        T t = null;
        Type type = easyLoadingListener.getClass().getGenericSuperclass();
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
                EasyProgressBar.getInstance().closeProgressBar();
                e.printStackTrace();
                maniHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        easyLoadingListener.error(new RuntimeException("解析数据失败"), -3, "解析数据失败", body, multimap);
                    }
                });
                return;
            }
        } else {
            t = (T) body;
        }

        final T back = t;
        if (async) {
            EasyProgressBar.getInstance().closeProgressBar();
            maniHandler.post(new Runnable() {
                @Override
                public void run() {
                    easyLoadingListener.success(local, back, multimap);
                }
            });
        } else {
            easyLoadingListener.success(local, back, multimap);
        }
    }

    protected void cancelRequest(String url) {
        Call call = callMap.get(url);
        if (call != null) {
            call.cancel();
        }
    }
}
