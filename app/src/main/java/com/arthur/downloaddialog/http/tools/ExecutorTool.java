package com.arthur.downloaddialog.http.tools;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorTool {

    private static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void submitExecutor(final Runnable runnable) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    EasyLog.e("Executor submitExecutor", e.getMessage(), e);
                }
            }
        });
    }

    public static <T> void executeTask(final AsyncTask<Void, Void, T> task) {
        if (Build.VERSION.SDK_INT < 11) {
            task.execute();
        } else {
            executeTaskHoneycomb(task);
        }
    }

    @TargetApi(11)
    private static <T> void executeTaskHoneycomb(final AsyncTask<Void, Void, T> task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
