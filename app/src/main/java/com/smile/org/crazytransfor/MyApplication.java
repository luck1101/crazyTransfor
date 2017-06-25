package com.smile.org.crazytransfor;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.smile.org.crazytransfor.module.log.L;
import com.smile.org.crazytransfor.module.log.LogFileStatService;
import com.smile.org.crazytransfor.service.RemoteTransforService;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Smile on 2017/5/31.
 */

public class MyApplication extends Application implements Thread.UncaughtExceptionHandler{
    private String TAG = MyApplication.class.getSimpleName();
    private static String PROCESS_MAIN = "com.smile.org.crazytransfor";
    private static MyApplication instance = null;

    public static Context getInstance() {
        return instance;
    }

    public static MyApplication getApplication() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //设置异常捕获
        L.d("oncreate()");

        Thread.setDefaultUncaughtExceptionHandler(this);
        if (isAppMainProcess()) {
            //do something for init
            initUmeng();
            LogFileStatService.startService(this);
        }
    }


    /**
     * 判断是不是UI主进程，因为有些东西只能在UI主进程初始化
     */
    public static boolean isAppMainProcess() {
        try {
            int pid = android.os.Process.myPid();
            String process = getAppNameByPID(MyApplication.getApplication(), pid);
            if (PROCESS_MAIN.equalsIgnoreCase(process)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据Pid得到进程名
     */
    public static String getAppNameByPID(Context context, int pid) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return "";
    }

    private void initUmeng(){
        L.d("initUmeng()");
        String APPKEY = "594df34caed1796f54001b4b";
        String CHANNEL = "ch_1";
        MobclickAgent.UMAnalyticsConfig config = new MobclickAgent.UMAnalyticsConfig(this, APPKEY, CHANNEL, MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.enableEncrypt(true);
        MobclickAgent.startWithConfigure(config);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        L.e("thread.name = " + thread.getName() + ",thread.id = " + thread.getId() + ",throwable = " + Log.getStackTraceString(throwable));
        MobclickAgent.reportError(this,throwable);
        stopRemoteService();
        stopLogService();
        MobclickAgent.onKillProcess(this);
        SystemClock.sleep(2000);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void stopRemoteService(){
        Intent intent = new Intent(instance, RemoteTransforService.class);
        stopService(intent);
    }
    public void stopLogService(){
        Intent intent = new Intent(instance, LogFileStatService.class);
        stopService(intent);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        L.d("系统内存不足onTerminate");
        instance = null;
    }
}
