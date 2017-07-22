package com.arthur.downloaddialog.http.request;

import com.arthur.downloaddialog.http.tools.CheckTool;
import com.arthur.downloaddialog.http.tools.EasyLog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class EasyDownloadRequest {

    private EasyDownloadRequest() {
    }

    private static EasyDownloadRequest easyRequest;

    protected static synchronized EasyDownloadRequest getInstence() {
        if (easyRequest == null) {
            easyRequest = new EasyDownloadRequest();
        }
        return easyRequest;
    }

    protected static class DownloadModel {

        public DownloadModel(int task, EasyBuilder builder, EasyDownloadListener listener) {
            this.task = task;
            this.builder = builder;
            this.listener = listener;
        }

        public int task;

        public EasyDownloadListener listener;

        public EasyBuilder builder;
    }

    private static Map<String, DownloadModel> downloadMap = new HashMap<>();

    protected int start(EasyBuilder builder, final EasyDownloadListener listener) {
        BaseDownloadTask downloadTask = FileDownloader.getImpl().create(builder.requestUrl);
        if (!CheckTool.isEmpty(builder.headerMap)) {
            for (String headerKey : builder.headerMap.keySet()) {
                downloadTask.addHeader(headerKey, builder.headerMap.get(headerKey));
            }
        }
        downloadTask.setPath(builder.downloadPath);
        downloadTask.setCallbackProgressTimes(300);
        downloadTask.setMinIntervalUpdateSpeed(400);
        downloadTask.setListener(new FileDownloadListener() {

            @Override
            protected void blockComplete(BaseDownloadTask task) {
                if (!CheckTool.isEmpty(listener)) {
                    listener.blockComplete(task);
                }
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                downloadMap.remove(task.getUrl());
                if (!CheckTool.isEmpty(listener)) {
                    listener.completed(task);
                }
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                if (!CheckTool.isEmpty(listener)) {
                    listener.error(task, e);
                }
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                if (!CheckTool.isEmpty(listener)) {
                    listener.warn(task);
                }
            }

            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                if (!CheckTool.isEmpty(listener)) {
                    listener.pending(task, soFarBytes, totalBytes);
                }
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                if (!CheckTool.isEmpty(listener)) {
                    listener.progress(task, soFarBytes, totalBytes);
                }
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                if (!CheckTool.isEmpty(listener)) {
                    listener.paused(task, soFarBytes, totalBytes);
                }
            }
        });
        int task = downloadTask.start();

        DownloadModel downloadModel = new DownloadModel(task, builder, listener);
        downloadMap.put(builder.requestUrl, downloadModel);
        return task;
    }

    public void pauseDownload(String url) {
        try {
            downloadMap.remove(url);
            FileDownloader.getImpl().pause(downloadMap.get(url).task);
        } catch (Throwable e) {
            EasyLog.e("EasyDownload", "pause error", e);
        }
    }

    public void pauseAllDownload() {
        try {
            downloadMap.clear();
            FileDownloader.getImpl().pauseAll();
        } catch (Throwable e) {
            EasyLog.e("EasyDownload", "pause all error", e);
        }
    }
}
