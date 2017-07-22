package com.arthur.downloaddialog.http.request;

import com.liulishuo.filedownloader.BaseDownloadTask;

/**
 * 下载回调
 */
public interface EasyDownloadListener {

    void blockComplete(BaseDownloadTask task);

    void completed(BaseDownloadTask task);

    void error(BaseDownloadTask task, Throwable e);

    void warn(BaseDownloadTask task);

    void pending(BaseDownloadTask task, int soFarBytes, int totalBytes);

    void progress(BaseDownloadTask task, int soFarBytes, int totalBytes);

    void paused(BaseDownloadTask task, int soFarBytes, int totalBytes);

}
