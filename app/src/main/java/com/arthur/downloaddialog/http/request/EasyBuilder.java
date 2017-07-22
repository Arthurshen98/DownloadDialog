package com.arthur.downloaddialog.http.request;

import android.content.Context;

import com.arthur.downloaddialog.http.tools.MD5Tool;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 网络操作类
 */
public class EasyBuilder {

    protected Context context;
    /**
     * 请求地址
     */
    protected String requestUrl;

    /**
     * 请求类型
     */
    protected String method;

    /**
     * 是否异步请求
     */
    protected boolean async = true;

    /**
     * 表单请求参数
     */
    protected Map<String, String> parameters;

    /**
     * JSON请求参数
     */
    protected JsonObject requestJsonObject;

    /**
     * JSON请求参数
     */
    protected JSONObject requestJSONObject;

    /**
     * 上传文件
     */
    protected List<File> uploadFiles;

    /**
     * header
     */
    protected Map<String, String> headerMap;

    /**
     * 是否开启转轮
     */
    protected boolean isShowBar = false;

    /**
     * 转轮提示文字
     */
    protected String barMessage;

    /**
     * 点击返回是否可以打断请求
     */
    protected boolean canCancel = false;

    /**
     * 点击返回是否可以退出当前界面
     */
    protected boolean canFinish = false;

    /**
     * 超时时间 毫秒
     */
    protected int timeOut = Integer.MAX_VALUE;

    /**
     * 是否先获取本地数据
     */
    protected boolean localFirst = false;

    /**
     * 本地缓存时间
     */
    protected long cacheTime = -1;

    /**
     * 下载地址
     */
    protected String downloadPath;


    protected EasyBuilder(Context context, String url) {
        this.context = context;
        this.requestUrl = url;
    }

    protected String jsonString() {
        if (requestJsonObject != null) {
            return requestJsonObject.toString();
        } else if (requestJSONObject != null) {
            return requestJSONObject.toString();
        }
        return null;
    }


    public EasyBuilder setHeader(Map<String, String> headerMap) {
        this.headerMap = headerMap;
        return this;
    }

    public EasyBuilder setTimeout(int timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    public EasyBuilder barMessage(String msg) {
        if (msg != null && msg.length() > 0) {
            this.isShowBar = true;
        }
        this.barMessage = msg;
        return this;
    }

    public EasyBuilder barCanCancel() {
        this.isShowBar = true;
        this.canCancel = true;
        return this;
    }

    public EasyBuilder barCanFinish() {
        this.isShowBar = true;
        this.canFinish = true;
        return this;
    }

    public EasyBuilder setCacheTime(long exceedTime) {
        if (exceedTime > 0) {
            localFirst = true;
        }
        this.cacheTime = exceedTime;
        return this;
    }

    public EasyBuilder setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public EasyBuilder setJson(JsonObject requestJsonObject) {
        this.requestJsonObject = requestJsonObject;
        return this;
    }

    public EasyBuilder setJSON(JSONObject requestJSONObject) {
        this.requestJSONObject = requestJSONObject;
        return this;
    }

    public EasyFuture asGet(Map<String, String> getObject) {
        this.method = "GET";
        this.requestUrl = EasyURLEncoder.appendUrl(this.requestUrl, getObject);
        return new EasyFuture(this);
    }


    public EasyFuture asHead(Map<String, String> headObject) {
        this.method = "HEAD";
        this.requestUrl = EasyURLEncoder.appendUrl(this.requestUrl, headObject);
        return new EasyFuture(this);
    }


    public EasyFuture asPut(Map<String, String> putObject) {
        this.method = "PUT";
        this.parameters = putObject;
        return new EasyFuture(this);
    }

    public EasyFuture asPatch(Map<String, String> patchObject) {
        this.method = "PATCH";
        this.parameters = patchObject;
        return new EasyFuture(this);
    }


    public EasyFuture asDelete(Map<String, String> deleteObject) {
        this.method = "DELETE";
        this.parameters = deleteObject;
        return new EasyFuture(this);
    }

    public EasyFuture asPostParameters(Map<String, String> parameters) {
        this.method = "POST";
        this.parameters = parameters;
        return new EasyFuture(this);
    }

    public EasyFuture asPostJson(JsonObject requestObject) {
        this.method = "POST";
        this.requestJsonObject = requestObject;
        return new EasyFuture(this);
    }

    public EasyFuture asPostJson(JSONObject requestObject) {
        this.method = "POST";
        this.requestJSONObject = requestObject;
        return new EasyFuture(this);
    }

    public EasyUpload asUploadFile(File file) {
        this.method = "POST";
        List<File> files = new ArrayList<>();
        files.add(file);
        this.uploadFiles = files;
        return new EasyUpload(this);
    }

    public EasyUpload asUploadPath(String path) {
        this.method = "POST";
        List<File> files = new ArrayList<>();
        files.add(new File(path));
        this.uploadFiles = files;
        return new EasyUpload(this);
    }

    public EasyUpload asUploadFile(List<File> files) {
        this.method = "POST";
        this.uploadFiles = files;
        return new EasyUpload(this);
    }

    public EasyUpload asUploadPath(List<String> paths) {
        this.method = "POST";
        this.uploadFiles = new ArrayList<>();
        for (String fileStr : paths) {
            this.uploadFiles.add(new File(fileStr));
        }
        return new EasyUpload(this);
    }

    public EasyDownLoadFuture asDownload(String path) {
        this.downloadPath = path;
        return new EasyDownLoadFuture(this);
    }

    protected String toMd5() {
        return MD5Tool.getMD5String(requestUrl + parameters + jsonString() + headerMap);
    }

    @Override
    public String toString() {
        if ("GET".equals(method)) {
            return "\nurl: " + requestUrl +
                    "\nmethod: " + method +
                    (headerMap != null ? "\nheader: " + headerMap.toString() : "") +
                    (parameters != null ? "\nmap: " + parameters.toString() : "") +
                    "\ntimeOut: " + timeOut + "毫秒" +
                    "\nlocalFirst: " + localFirst;
        } else {
            return "\nurl: " + requestUrl +
                    "\nmethod: " + method +
                    (jsonString() != null ? "\njsonObject: " + jsonString() : "") +
                    (headerMap != null ? "\nheader: " + headerMap.toString() : "") +
                    (parameters != null ? "\nmap: " + parameters.toString() : "");
        }
    }
}
