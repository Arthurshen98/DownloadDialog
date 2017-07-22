package com.arthur.downloaddialog.http.request;//package com.witclass.common.net.loadbuilder;

import android.os.AsyncTask;

import com.arthur.downloaddialog.http.db.EasyKvDb;
import com.arthur.downloaddialog.http.tools.CheckTool;
import com.arthur.downloaddialog.http.tools.ExecutorTool;


/**
 *
 */
public class EasyDbLoading {

    protected interface LibraryDbCallBack {
        void dbCallBack(String data, boolean isToGetNet, boolean isHaveCache);
    }

    private static EasyDbLoading dbLoading;

    private EasyDbLoading() {
    }

    protected static synchronized EasyDbLoading getInstance() {
        if (dbLoading == null) {
            dbLoading = new EasyDbLoading();
        }
        return dbLoading;
    }

    void getLocalDBData(final EasyBuilder builder, final LibraryDbCallBack libraryDbCallBack) {
        if (builder.async) {
            AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                boolean isToGetNet = true;

                @Override
                protected String doInBackground(Void... params) {
                    //获取缓存数据
                    String resultDb = EasyKvDb.read(builder.toMd5() + "-data");
                    if (!CheckTool.isEmpty(resultDb)) {
                        //如果有缓存数据，则获取上次缓存的时间
                        //通过上次缓存的时间，判断当前是否需要请求网络，默认时间是 EXCEED_TIME
                        isToGetNet = isExceedForNowDateTime(lastDate(builder), builder.cacheTime);
                    }
                    return resultDb;
                }

                @Override
                protected void onPostExecute(String resultDb) {
                    libraryDbCallBack.dbCallBack(resultDb, isToGetNet, !CheckTool.isEmpty(resultDb));
                }
            };
            ExecutorTool.executeTask(task);
        } else {
            String resultDb = EasyKvDb.read(builder.toMd5() + "-data");
            boolean isToGetNet = true;
            if (!CheckTool.isEmpty(resultDb)) {
                //如果有缓存数据，则获取上次缓存的时间
                //通过上次缓存的时间，判断当前是否需要请求网络，默认时间是 EXCEED_TIME
                isToGetNet = isExceedForNowDateTime(lastDate(builder), builder.cacheTime);
            }
            libraryDbCallBack.dbCallBack(resultDb, isToGetNet, !CheckTool.isEmpty(resultDb));
        }
    }

    /**
     * 存储本地数据
     *
     * @param builder 请求数据
     * @param data    缓存的数据
     */
    void saveLoace(final EasyBuilder builder, String data) {
        EasyKvDb.save(builder.toMd5() + "-data", data);
        EasyKvDb.save(builder.toMd5() + "-time", System.currentTimeMillis() + "");
    }


    /**
     * 判断是否超过当前时间一段距离
     *
     * @param from 要计算的时间
     * @param time 时间的距离
     */
    private boolean isExceedForNowDateTime(long from, long time) {
        long nowTime = System.currentTimeMillis();
        return nowTime - from > time;
    }

    private long lastDate(EasyBuilder builder){
        String lastDateStr = EasyKvDb.read(builder.toMd5() + "-time");
        if (CheckTool.isEmpty(lastDateStr)){
            lastDateStr = "0";
        }
        return Long.parseLong(lastDateStr);
    }

}
