Android DownloadDialog
==
Introduction
--
这是一个apk下载工具，专注于打开应用时apk更新下载，可以显示多种形式，可以强制更新限制，返回键禁止，或者退出app等，
可以打开dialogapk就自动下载或者点击开始下载手动去下载。

Demonstration
--
效果图1，点击dialog直接进行的是实心加载，下载完成之后就可以自动提示安装

![](https://github.com/Arthurshen98/DownloadDialog/blob/master/gif/download_1.gif) 

效果图2，点击dialog显示开始下载，可手动点击下载开始下载

![](https://github.com/Arthurshen98/DownloadDialog/blob/master/gif/download_2.gif) 

效果图3和4，加载异常的情况，会显示异常，可点击重新下载，即可重新开始下载

![](https://github.com/Arthurshen98/DownloadDialog/blob/master/gif/download_4.gif) 
![](https://github.com/Arthurshen98/DownloadDialog/blob/master/gif/download_5.gif) 

效果图5，可更改加载圈的样式，底部text，和成功异常图片的样式都是可以更改的

![](https://github.com/Arthurshen98/DownloadDialog/blob/master/gif/download_8.gif)

Usage
--
# 1，引入 library或者把library中的代码复制到自己的工程中

暂时没有支持gradle引用方式

# 2，使用方式，可以dialog弹出就开始下载或弹出后点击“开始下载”手动开始下载

（1）弹出自动下载

<pre>
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
                            //设置下载时间过长的状态，开放返回关闭dialog权限
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
            //默认不用设置（可设置加载圈的样式，底部颜色，加载颜色，字体颜色）
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
<code>

（2）点击开始下载
<pre>
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
<code>

# 3，（如果没有网络请求工具的看这里）我在demo里面有写封装好的网络请求工具使用OKHttp,专注于上传下载封装，详情见demo

<pre>
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
<code>

# 4，下载中，下载完成，下载错误dialog方法的实现
（1）下载中

<pre>
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
<code>

（2）下载完成

<pre>
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
<code>

（3）下载错误

<pre>
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
<code>

很多方法在源码中，源码中注释写的非常详细，详情可以查看源码便知。
