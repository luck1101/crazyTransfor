package com.smile.org.crazytransfor.module.log;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/**
 * 
 * @Title:过家家
 * @Description: 
 * @Copyright: Copyright (c) 2015
 * @Company: 深圳市过家家
 * @version: 1.0.0.0
 * @author: chuck
 * @createDate 2015-9-25
 *
 */
public class LogFileStatService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            startForeground(LogFileStatService.class.hashCode(), new Notification());
        }
        startCollect();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(startId, new Notification());
        return START_STICKY;
    }


    private void startCollect() {
        Thread thread = new Thread("LogStat") {

            int counter;
            @Override
            public void run() {
                while (true) {
                    if (counter == 0) {
                        LogFileUtil.deleteSdcardExpiredLog();
                    }
                    try {
                        sleep(10000);
                    } catch (InterruptedException e) {

                    }
                    counter++;
                    //没1000 * 10s检查一次是否删除过期文件
                    if (counter == 1000) {
                        counter = 0;
                    }
                    LogLocalStat.getInstance().flush();
                }
            }

        };
        thread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogLocalStat.getInstance().stopFlush();
    }

    public static void startService(Context context) {
        Intent service = new Intent(context, LogFileStatService.class);
        context.startService(service);
    }
}
