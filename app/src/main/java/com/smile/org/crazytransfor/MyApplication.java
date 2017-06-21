package com.smile.org.crazytransfor;

import android.app.Application;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.smile.org.crazytransfor.module.log.L;
import com.smile.org.crazytransfor.module.log.LogFileStatService;

/**
 * Created by Smile on 2017/5/31.
 */

public class MyApplication extends Application implements Thread.UncaughtExceptionHandler{
    private String TAG = MyApplication.class.getSimpleName();
    private static Context instance;

    public static Context getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //设置异常捕获
        L.d("oncreate()");
        LogFileStatService.startService(this);
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        L.e("thread.name = " + thread.getName() + ",thread.id = " + thread.getId() + ",throwable = " + Log.getStackTraceString(throwable));
        SystemClock.sleep(2000);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }
}
