package com.arthur.downloaddialog.http.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;


import com.arthur.downloaddialog.R;
import com.arthur.downloaddialog.http.tools.CheckTool;

import java.util.HashSet;
import java.util.Set;

import okhttp3.Call;

/**
 * 转轮
 */
public class EasyProgressBar {

    private Handler maniHandler = new Handler(Looper.getMainLooper());

    private static EasyProgressBar easyProgressBar;

    private EasyProgressBar() {
    }

    public static synchronized EasyProgressBar getInstance() {
        if (easyProgressBar == null) {
            easyProgressBar = new EasyProgressBar();
        }
        return easyProgressBar;
    }


    private Dialog progressBarDialog;

    private Set<Call> callSet = new HashSet<>();

    public synchronized void setCall(Call call) {
        this.callSet.clear();
        this.callSet.add(call);
    }

    public synchronized void addCall(Call call) {
        this.callSet.add(call);
    }

    /**
     * 启动加载进度条
     *
     * @param message   提示文字int 不接收String
     * @param canCancel 是否可关闭进度条状态
     * @param canFinish 是否可关闭当前Activity
     */
    public synchronized void startProgressBar(final Activity act, String message, final boolean canCancel, final boolean canFinish) {
        startProgressBar(act, message, canCancel, canFinish, null);
    }

    /**
     * 启动加载进度条
     *
     * @param message          提示文字int 不接收String
     * @param canCancel        是否可关闭进度条状态
     * @param canFinish        是否可关闭当前Activity
     * @param progressListener 关闭转轮监听
     */
    public synchronized void startProgressBar(final Activity act, final String message, final boolean canCancel, final boolean canFinish, final EasyProgressListener progressListener) {
        this.callSet.clear();
        if (progressBarDialog != null && progressBarDialog.isShowing())
            return;

        maniHandler.post(new Runnable() {
            @Override
            public void run() {
                View view = View.inflate(act, R.layout.easy_dialog_progressbar, null);
                progressBarDialog = EasyDialog.getInstance().buildDialog(act, view, false);
                TextView titleTest = (TextView) view.findViewById(R.id.easy_dialog_loading_txt);
                if (CheckTool.isEmpty(message)) {
                    titleTest.setVisibility(View.GONE);
                } else {
                    titleTest.setVisibility(View.VISIBLE);
                    titleTest.setText(message);
                }

                progressBarDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (i == KeyEvent.KEYCODE_BACK && (canCancel || canFinish)) {
                            if (!CheckTool.isEmpty(callSet)) {
                                for (Call call : callSet) {
                                    call.cancel();
                                }
                            }

                            if (progressListener != null) {
                                progressListener.cancel();
                            }

                            if (canFinish) {
                                act.finish();
                            }
                            closeProgressBar();
                            return true;
                        }
                        return false;
                    }
                });
            }
        });
    }


    // 进度条 关
    public synchronized boolean closeProgressBar() {
        this.callSet.clear();
        if (progressBarDialog != null && progressBarDialog.isShowing()) {
            maniHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressBarDialog.dismiss();
                }
            });
            return true;
        }
        return false;
    }

    public interface EasyProgressListener {
        void cancel();
    }
}
