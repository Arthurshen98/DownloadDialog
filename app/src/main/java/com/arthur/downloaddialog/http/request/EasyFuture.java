package com.arthur.downloaddialog.http.request;

/**
 * 回调类
 */
public class EasyFuture {
    EasyBuilder builder;

    protected EasyFuture(EasyBuilder builder) {
        this.builder = builder;
    }

    public <T> void executeAsync(EasyLoadingListener<T> loadingListener){
        this.builder.async = true;
        EasyRequest.getInstence().request(builder, loadingListener);
    }

    public <T> void executeSync(EasyLoadingListener<T> loadingListener){
        this.builder.async = false;
        EasyRequest.getInstence().request(builder, loadingListener);
    }
}
