package com.arthur.dialoglibrary;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arthur.dialoglibrary.load.RingProgressBar;
import com.arthur.dialoglibrary.tool.ApkUtil;
import com.arthur.dialoglibrary.tool.ScreenUtils;

import java.io.File;

/**
 * Created by arthur on 2017/7/20.
 * Author:shenfei
 * Email:shenxuanfei@163.com
 * 专门用于apk下载自封装的dialog
 */

public class DownloadDialog {

    private static final int LIMITSHR = 1000;
    private static final int MILISHR = 100000;

    private Context context;
    private Dialog dialog;

    private TextView tv_fresh_down_text;
    private ImageView iv_down_load_status;

    private RingProgressBar progressRing;
    private TextView tv_fresh_down_again;
    private LinearLayout ll_down_load_dialog;

    /**
     * 是否下载出错
     */
    private boolean isDownloadError = false;
    /**
     * 是否第一次加载进度
     */
    private boolean isDownload = false;
    /**
     * 是否打开开始下载按钮
     */
    private boolean isOpenStartBtn = false;
    /**
     * 是否正在下载：//关闭重复点击下载按钮
     */
    private boolean isDownloading = false;

    /**
     * 加载进度
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int progress = (int) msg.obj;
            if (progressRing != null) {
                progressRing.setProgress(progress);
            }
        }
    };

    /**
     * 用于处理延迟加载
     */
    private Handler handlerOvertime = new Handler();

    public DownloadDialog(Context context) {
        this.context = context;
    }

    public DownloadDialog builder() {

        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_down_load_item, null);

        tv_fresh_down_text = (TextView) view.findViewById(R.id.tv_fresh_down_text);
        progressRing = (RingProgressBar) view.findViewById(R.id.progress_ring_view);
        iv_down_load_status = (ImageView) view.findViewById(R.id.iv_down_load_status);
        tv_fresh_down_again = (TextView) view.findViewById(R.id.tv_fresh_down_again);
        ll_down_load_dialog = (LinearLayout) view.findViewById(R.id.ll_down_load_dialog);

        if (dialog == null) {
            dialog = new Dialog(context, R.style.AlertDialogStyle);
            dialog.setContentView(view);
        }
        // 调整dialog背景大小
        ll_down_load_dialog.setLayoutParams(new FrameLayout.LayoutParams(
                (int) (ScreenUtils.getWidth(context) * 0.7)
                , LinearLayout.LayoutParams.WRAP_CONTENT));

        return this;
    }

    /**
     * 设置标题
     *
     * @param msg 内容
     * @return
     */
    public DownloadDialog setMsg(String msg) {
        tv_fresh_down_text.setText(msg);
        return this;
    }

    /**
     * 设置重新下载是否显示
     *
     * @param isShow true
     */
    public void setAgainUpdateBtnShow(boolean isShow) {
        //如果打开了开始下载按钮，就不让字体隐藏
        if (!isOpenStartBtn) {
            if (isShow) {
                tv_fresh_down_again.setVisibility(View.VISIBLE);
            } else {
                tv_fresh_down_again.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 下载完成回调监听(基本废除，原因有可能不准确)
     *
     * @param listener
     * @return
     */
    public DownloadDialog setFinishListener(final RingProgressBar.OnProgressListener listener) {
        if (dialog != null) {
            progressRing.setOnProgressListener(new RingProgressBar.OnProgressListener() {
                @Override
                public void progressToComplete() {
                    listener.progressToComplete();
                }
            });
        }
        return this;
    }

    /**
     * 这是开始下载监听 可以手动点击开始下载
     *
     * @param text     btn字体
     * @param listener
     * @return
     */
    public DownloadDialog setStartDownloadBtn(String text, final View.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            isOpenStartBtn = true;
            tv_fresh_down_again.setVisibility(View.VISIBLE);
            tv_fresh_down_again.setText(text);
        }
        tv_fresh_down_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDownloading) {
                    iv_down_load_status.setVisibility(View.GONE);
                    progressRing.setVisibility(View.VISIBLE);
                    progressRing.setProgress(0);
                    listener.onClick(v);
                }
                isOpenStartBtn = false;
            }
        });
        return this;
    }


    /**
     * 重新下载
     *
     * @param text
     * @param listener
     * @return
     */
    public DownloadDialog setAgainUpdateBtn(String text, final View.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            tv_fresh_down_again.setText(text);
        }
        tv_fresh_down_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDownloading) {
                    listener.onClick(v);
                    progressRing.setProgress(0);
                    iv_down_load_status.setVisibility(View.GONE);
                    progressRing.setVisibility(View.VISIBLE);
                }
            }
        });
        return this;
    }

    /**
     * 底部是否下载成功提示
     *
     * @param text
     * @param isClick 点击事件
     */
    public void setBottomStatusText(String text, boolean isClick) {
        if (dialog != null) {
            tv_fresh_down_again.setVisibility(View.VISIBLE);
            tv_fresh_down_again.setText(text);
            if (!isClick) {
                tv_fresh_down_again.setOnClickListener(null);
            }
        }
    }

    /**
     * 设置总大小
     *
     * @param progressTotal 总大小
     */
    public void setProgressTotal(int progressTotal) {
        //关闭重复点击下载按钮
        isDownloading = true;
        if (progressRing != null) {
            if (progressTotal > 100000) {
                progressRing.setMax(progressTotal / LIMITSHR);
            } else if (progressTotal > 10000000) {
                progressRing.setMax(progressTotal / MILISHR);
            } else {
                progressRing.setMax(progressTotal);
            }
        }
    }

    /**
     * 设置进度
     *
     * @param totalProgress 总进度
     * @param progress      当前进度
     */
    public void setUpDateProgress(int totalProgress, int progress) {
        if (dialog != null) {
            //这里是给予progress total只赋值一次
            if (isDownload) {
                setProgressTotal(totalProgress);
                isDownload = false;
            }

            Message message = Message.obtain();
            if (progress > 100000) {
                message.obj = progress / LIMITSHR;
            } else if (progress > 10000000) {
                message.obj = progress / MILISHR;
            } else {
                message.obj = progress;
            }
            handler.sendMessage(message);
        }
    }


    /**
     * 下载失败,不强制更新
     */
    public void setShowDownloadError(String textError, final DownloadOnBackKey downloadOnBackKey) {
        if (dialog != null) {
            if (!TextUtils.isEmpty(textError)) {
                tv_fresh_down_again.setText(textError);
            }
            //打开重复点击下载按钮
            isDownloading = false;
            progressRing.setVisibility(View.GONE);
            tv_fresh_down_again.setVisibility(View.VISIBLE);
            iv_down_load_status.setVisibility(View.VISIBLE);
            iv_down_load_status.setImageResource(R.drawable.ic_download_error);
            statusAnimator(iv_down_load_status);

            //下载失败将开启返回键可关闭dialog
            isDownloadError = true;
            //触摸dialog外面bu可关闭
            dialog.setCanceledOnTouchOutside(false);
            //下载错误时，返回回调一个方法，方便置空dialog和进入两一个界面
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                        downloadOnBackKey.downloadOnBackKey();
                        dialog.dismiss();
                        return true;
                    }else {
                        return false;
                    }
                }
            });
        }
    }

    /**
     * 下载失败,强制更新，无法关闭，否则退出app
     */
    public void setDownloadErrorForce(String textError, final Activity activity) {
        if (dialog != null) {
            progressRing.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(textError)) {
                tv_fresh_down_again.setText(textError);
            }
            //打开重复点击下载按钮
            isDownloading = false;
            tv_fresh_down_again.setVisibility(View.VISIBLE);
            iv_down_load_status.setVisibility(View.VISIBLE);
            iv_down_load_status.setImageResource(R.drawable.ic_download_error);
            statusAnimator(iv_down_load_status);

            //下载失败将开启返回键可关闭dialog
            isDownloadError = false;
            //触摸dialog外面可关闭
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                        activity.finish();
                        dialog.dismiss();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
    }

    /**
     * 下载完成
     */
    public void setShowDownloadOk(final String appFilePath, final DownloadAllComplete downloadAllComplete) {
        if (dialog != null) {
            //打开重复点击下载按钮
            isDownloading = false;

            progressRing.setVisibility(View.GONE);
            iv_down_load_status.setVisibility(View.VISIBLE);
            iv_down_load_status.setImageResource(R.drawable.ic_download_correct);
            statusAnimator(iv_down_load_status);
            //延迟加载安装app
            handlerOvertime.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                    //下载完成所有处理完成
                    downloadAllComplete.downloadComplete();
                    //安装app
                    ApkUtil.openAppFile(new File(appFilePath), context);
                }
            }, 1000);
        }
    }

    /**
     * 设置触摸关闭
     *
     * @param isTouch
     */
    public void setDialogOnTouchClose(boolean isTouch) {
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(isTouch);
        }
    }

    /**
     * 设置按返回键无法关闭
     *
     * @param isNoBack true为无法返回关闭
     */
    public void setOnBackKeyClose(boolean isNoBack) {
        if (!isNoBack) {
            if (dialog != null) {
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                            if (isDownloadError) {
                                dismissDialog();
                                return false;
                            }
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                setCancelable(false);
            }
        }
    }

    /**
     * 取消
     *
     * @param cancel
     * @return
     */
    public DownloadDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }


    public void show() {
        if (dialog != null) {
            isDownload = true;
            dialog.show();
        }
    }

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
            handler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 背景动画
     *
     * @param bg
     */
    private void statusAnimator(View bg) {
        ObjectAnimator animatorScale = ObjectAnimator.ofFloat(bg, "scaleY", 0.2f, 1f);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(bg, "Alpha", 0.0f, 1.0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorScale, animatorAlpha);
        animatorSet.setDuration(300);
        animatorSet.start();
    }

    public TextView getTextViewBottom() {
        return tv_fresh_down_again;
    }

    public ImageView getImageViewCenter() {
        return iv_down_load_status;
    }

    public RingProgressBar getProgressRing() {
        return progressRing;
    }

    public interface DownloadAllComplete {
        void downloadComplete();
    }

    public interface DownloadOnBackKey {
        void downloadOnBackKey();
    }
}
