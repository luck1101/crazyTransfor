package com.smile.org.crazytransfor;

import android.app.Application;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Smile on 2017/5/31.
 */

public class MyApplication extends Application implements Thread.UncaughtExceptionHandler{
    private String TAG = MyApplication.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        //设置异常捕获
        Log.d(TAG,"oncreate()");
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e(TAG,"thread.name = " + thread.getName() + ",thread.id = " + thread.getId() + ",throwable = " + Log.getStackTraceString(throwable));
        SystemClock.sleep(2000);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
