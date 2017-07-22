package com.arthur.downloaddialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.arthur.dialoglibrary.DownloadDialog;
import com.arthur.dialoglibrary.load.RingProgressBar;
import com.arthur.dialoglibrary.tool.ApkUtil;
import com.arthur.downloaddialog.http.request.Easy;
import com.arthur.downloaddialog.http.request.EasyDownloadListener;
import com.liulishuo.filedownloader.BaseDownloadTask;

public class MainActivity extends Activity implements View.OnClickListener {


    private DownloadDialog downLoadDialog;
    private Handler handlerOvertime = new Handler();
    /**
     * 是否强制更新
     */
    private boolean isMustDownload = false;
    /**
     * 是否页面重新加载，并且不安装app
     */
    private boolean isUninstallReturn = false;

    /**
     * //为了测试添加了i，改动这个i可以重新加载下载效果，否则会判断本如果有下载会不重复下载，直接完成
     * //正常情况下i是不需要的
     */
    private int i = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start_download).setOnClickListener(this);
        findViewById(R.id.start_download_two).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_download:
                showDialogTwo();
                break;
            case R.id.start_download_two:
                showDialog();
                break;
        }

    }

    /**
     * 显示dialog
     * 1，显示“开始下载”点击开始下载即可开始下载
     * 区别在于是否实现setStartDownloadBtn方法
     */
    private void showDialog() {
        if (downLoadDialog == null) {
            downLoadDialog = new DownloadDialog(this);
            downLoadDialog.builder()
                    .setMsg("XX app下载")
                    .setAgainUpdateBtn("重新下载", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startDownload();
                            //设置下载时间过长的状态，开放返回权限
                            downloadOverTime();
                        }
                    })
                    .setStartDownloadBtn("开始下载", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startDownload();
                        }
                    })
                    .show();
            //设置触摸不可关闭
            downLoadDialog.setDialogOnTouchClose(false);
            //设置返回不可关闭
            downLoadDialog.setOnBackKeyClose(false);
            //设置重新下载显示
            downLoadDialog.setAgainUpdateBtnShow(false);
            //直接开始下载，不用手动点击开始下载
//            startDownload();
        } else {
            downLoadDialog = null;
        }
    }

    /**
     * 显示dialog
     * 2，直接运行该dialog即可开始下载
     * 区别在于是否实现setStartDownloadBtn方法
     * 或直接运行dialog就运行startDownload();方法
     */
    private void showDialogTwo() {
        if (downLoadDialog == null) {
            downLoadDialog = new DownloadDialog(this);
            downLoadDialog.builder()
                    .setMsg("XX app下载")
                    .setAgainUpdateBtn("重新下载", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startDownload();
                            downloadOverTime();
                        }
                    })
//                    .setStartDownloadBtn("开始下载", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            startDownload();
//                        }
//                    })
                    .show();
            //设置加载圈的样式，底部颜色，加载颜色，字体颜色
            downLoadDialog.getProgressRing().setStyle(RingProgressBar.FILL);
            downLoadDialog.getProgressRing().setRingColor(getResources().getColor(R.color.colorPrimary));
            downLoadDialog.getProgressRing().setRingProgressColor(getResources().getColor(R.color.colorPrimary));
            downLoadDialog.getProgressRing().setTextColor(getResources().getColor(R.color.colorPrimary));

            //设置触摸不可关闭
            downLoadDialog.setDialogOnTouchClose(false);
            //设置返回不可关闭
            downLoadDialog.setOnBackKeyClose(false);
            //设置重新下载显示
            downLoadDialog.setAgainUpdateBtnShow(false);
            //直接开始下载，不用手动点击开始下载
            startDownload();
        } else {
            downLoadDialog = null;
        }
    }

    //开始下载
    private void startDownload() {
        //为了测试添加了i，改动这个i可以重新加载下载效果，否则会判断本如果有下载会不重复下载，直接完成
        i++;
        //服务器给你的下载地址
        String url = "https://apkegg.mumayi.com/cooperation/2015/02/07/91/916451/qihuanshejiFantaShooting_V2.21_mumayi_8c478.apk";
        //服务器给你的版本号
        String version = "1.0." + i;
        //修改这个路径可再次下载效果
        final String appFilePath = ApkUtil.getAppSystemPath(this) + "/" + "xx_app_13" + version + ".apk";
        //使用这个下载框架需要在application中初始化
        Easy.load(this, url)
                .asDownload(appFilePath)
                .execute(new EasyDownloadListener() {
                    @Override

                    public void blockComplete(BaseDownloadTask task) {

                    }

                    @Override
                    public void completed(BaseDownloadTask task) {
                        respondDownCompleted(appFilePath);
                    }

                    @Override
                    public void error(BaseDownloadTask task, Throwable e) {
                        respondDownError();
                    }

                    @Override
                    public void warn(BaseDownloadTask task) {

                    }

                    @Override
                    public void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    public void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        respondDownProgress(soFarBytes, totalBytes);
                    }

                    @Override
                    public void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }
                });
    }


    /**
     * 下载完成
     *
     * @param appFilePath
     */
    private void respondDownCompleted(final String appFilePath) {
        if (downLoadDialog != null) {
            downLoadDialog.setBottomStatusText("下载成功", false);
            downLoadDialog.setShowDownloadOk(appFilePath, new DownloadDialog.DownloadAllComplete() {
                @Override
                public void downloadComplete() {
                    downLoadDialog = null;
                    if (isMustDownload) {
                        MainActivity.this.finish();
                    } else {
                        isUninstallReturn = true;
                    }
                }
            });
        }
    }

    /**
     * 正在下载中
     *
     * @param soFarBytes 当前进度
     * @param totalBytes 总进度
     */
    private void respondDownProgress(int soFarBytes, int totalBytes) {
        if (downLoadDialog != null) {
            downLoadDialog.setUpDateProgress(totalBytes, soFarBytes);
        }
    }

    /**
     * 下载错误
     */
    private void respondDownError() {
        if (downLoadDialog != null) {
            if (isMustDownload) {
                //返回退出app
                downLoadDialog.setDownloadErrorForce("重新下载", MainActivity.this);
            } else {
                //返回可进行其他操作
                downLoadDialog.setShowDownloadError("重新下载", new DownloadDialog.DownloadException() {
                    @Override
                    public void downloadException() {
                        downLoadDialog = null;
                        //进入其他页面
                        interAppDelayed(1000);
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isUninstallReturn) {
//            interAppDelayed(500);
        }
    }

    /**
     * 延迟进入app
     */
    private void interAppDelayed(int time) {
        handlerOvertime.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        }, time);
    }

    /**
     * 当下载过长时,给予返回的权限
     * 给30秒下载时间，
     * 如果超过这个时间可以给予返回权限
     */
    private void downloadOverTime() {
        try {
            handlerOvertime.postDelayed(new Runnable() {
                @Override
                public void run() {
                    downLoadDialog.setDownloadErrorForce("下载时间变得漫长了", MainActivity.this);
                }
            }, 30000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerOvertime.removeCallbacks(null);
    }
}
