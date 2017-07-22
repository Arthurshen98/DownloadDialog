package com.arthur.downloaddialog.http.request;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 请求回调方法
 */
public abstract class EasyUploadListener<T> {
    public abstract void netError();

    public abstract void success(File file, T result, Map<String, List<String>> headerMap);

    public abstract void cancel(File file);

    public abstract void error(File file, Throwable e, int code, String error, String result);
}
