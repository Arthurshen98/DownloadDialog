package com.arthur.downloaddialog.http.request;

/**
 * 回调类
 */
public class EasyDownLoadFuture {
    EasyBuilder builder;

    protected EasyDownLoadFuture(EasyBuilder builder) {
        this.builder = builder;
    }

    public <T> void execute(EasyDownloadListener listener){
        EasyDownloadRequest.getInstence().start(builder, listener);
    }
}
