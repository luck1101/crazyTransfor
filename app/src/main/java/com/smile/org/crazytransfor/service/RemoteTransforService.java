package com.smile.org.crazytransfor.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.smile.org.crazytransfor.R;
import com.smile.org.crazytransfor.util.Utils;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2017/5/6 0006.
 */

public class RemoteTransforService extends Service {
    private String TAG = RemoteTransforService.class.getSimpleName();
    //定义浮动窗口布局
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;

    Button mFloatView;
    private ArrayList<String> peoplePhones = new ArrayList<>();
    private TransforMoneyRunnable mTransforMoneyRunnable;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        createFloatView();
        mTransforMoneyRunnable = new TransforMoneyRunnable();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ArrayList<String> phones = intent.getStringArrayListExtra("data");
        Log.d(TAG,"phones = " + phones);
        if(phones != null){
            peoplePhones.clear();
            peoplePhones.addAll(phones);
        }
        return super.onStartCommand(intent, flags, startId);

    }

    private void createFloatView()
    {
        wmParams = new WindowManager.LayoutParams();
        //获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        Log.i(TAG, "mWindowManager--->" + mWindowManager);
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 0;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

         /*// 设置悬浮窗口长宽数据
        wmParams.width = 200;
        wmParams.height = 80;*/

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        //浮动窗口按钮
        mFloatView = (Button)mFloatLayout.findViewById(R.id.btn_start);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //设置监听浮动窗口的触摸移动
        mFloatView.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // TODO Auto-generated method stub
                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                wmParams.x = (int) event.getRawX() - mFloatView.getMeasuredWidth()/2;
                Log.i(TAG, "RawX" + event.getRawX());
                Log.i(TAG, "X" + event.getX());
                //减25为状态栏的高度
                wmParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight()/2 - 25;
                Log.i(TAG, "RawY" + event.getRawY());
                Log.i(TAG, "Y" + event.getY());
                //刷新
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                return false;  //此处必须返回false，否则OnClickListener获取不到监听
            }
        });

        mFloatView.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                isTransforRuning = !isTransforRuning;
                if(isTransforRuning){
                    Toast.makeText(RemoteTransforService.this, "start", Toast.LENGTH_SHORT).show();
                    mTransforMoneyRunnable.setData(peoplePhones);
                    mTransforMoneyRunnable.start();
                    new Thread(mTransforMoneyRunnable).start();
                    mFloatView.setText(R.string.str_action_stop);
                }else{
                    Toast.makeText(RemoteTransforService.this, "stop", Toast.LENGTH_SHORT).show();
                    mTransforMoneyRunnable.stop();
                    mFloatView.setText(R.string.str_action_start);
                }
            }
        });
    }

    private boolean isTransforRuning = false;

    class TransforMoneyRunnable implements Runnable{
        private boolean isStop = false;
        private ArrayList<String> myPhones = new ArrayList<>();
        public void setData(ArrayList<String> list){
            if(!Utils.isEmpty(list)){
                myPhones.clear();
                myPhones.addAll(list);
            }
        }
        public void start(){
            isStop = false;
        }
        public void stop(){
            isStop = true;
        }

        @Override
        public void run() {
            try {
                for (String phone : myPhones){
                    if(isStop){
                        Log.d(TAG,"stop TransforMoneyRunnable");
                        break;
                    }
                    //String cmd1 = "input text " + phone;
                    //String cmd2 = "input keyevent 66";
                    //String[] cmds = {cmd1,cmd2};
                    //String cmds = "input keyevent 4";
                    //Utils.CommandResult result = Utils.execCommand(cmds,false);
                    //Log.d(TAG,"phone = " + phone + ",result = " + result.result);
                    Utils.sendTextCmd(phone);
                    Thread.currentThread().sleep(5000);
                }
            }catch (Exception e){
                Log.e(TAG,"Exception description = " + e.getMessage() + ",e = " + e);
            }

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mFloatLayout != null)
        {
            //移除悬浮窗口
            mWindowManager.removeView(mFloatLayout);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
