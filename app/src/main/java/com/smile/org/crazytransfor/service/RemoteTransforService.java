package com.smile.org.crazytransfor.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.org.crazytransfor.R;
import com.smile.org.crazytransfor.util.Utils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/6 0006.
 */

public class RemoteTransforService extends Service {
    private String TAG = RemoteTransforService.class.getSimpleName();
    //定义浮动窗口布局
    LinearLayout mFloatLayout;
    LinearLayout mFloatLayout1;
    WindowManager.LayoutParams wmParams;
    WindowManager.LayoutParams wmParams1;

    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;

    Button mRecordBtn,mStartBtn;
    Button mCursorView;
    private ArrayList<String> peoplePhones = new ArrayList<>();
    private MyHandler myHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        createCursorView();
        createFloatView();
        myHandler = new MyHandler();
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

    private void createCursorView(){
        wmParams1 = new WindowManager.LayoutParams();
        //设置window type
        wmParams1.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        wmParams1.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams1.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams1.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams1.x = 0;
        wmParams1.y = 0;
        //设置悬浮窗口长宽数据
        wmParams1.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams1.height = WindowManager.LayoutParams.WRAP_CONTENT;
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout1 = (LinearLayout) inflater.inflate(R.layout.float_cursor_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout1, wmParams1);
        mFloatLayout1.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //浮动窗口按钮
        mCursorView = (Button)mFloatLayout1.findViewById(R.id.txt_cursor);
        //设置监听浮动窗口的触摸移动
        mCursorView.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // TODO Auto-generated method stub
                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                wmParams1.x = (int) event.getRawX()- mCursorView.getMeasuredWidth()/2;
                //减25为状态栏的高度
                wmParams1.y = (int) event.getRawY() - mCursorView.getMeasuredHeight()/2 -25;
                //刷新
                mWindowManager.updateViewLayout(mFloatLayout1, wmParams1);
                return false;  //此处必须返回false，否则OnClickListener获取不到监听
            }
        });
    }

    private Point getCursorViewCoordinate(){
        int[] location = new int[2];
        mCursorView.getLocationOnScreen(location);
        int x = location[0] + mCursorView.getMeasuredWidth()/2;
        int y = location[1] + mCursorView.getMeasuredHeight()/2;
        Log.d(TAG, "Screen X = "+ x + ",Y = " + y);
        return new Point(x,y);
    }

    private void createFloatView()
    {
        wmParams = new WindowManager.LayoutParams();
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 0;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_operate_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //设置监听浮动窗口的触摸移动
        mFloatLayout.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // TODO Auto-generated method stub
                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                wmParams.x = (int) event.getRawX() - mFloatLayout.getMeasuredWidth()/2;
                //减25为状态栏的高度
                wmParams.y = (int) event.getRawY() - mFloatLayout.getMeasuredHeight()/2 - 25;
                //刷新
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                return false;  //此处必须返回false，否则OnClickListener获取不到监听
            }
        });

        //浮动窗口按钮
        mRecordBtn = (Button)mFloatLayout.findViewById(R.id.btn_record);
        mStartBtn = (Button)mFloatLayout.findViewById(R.id.btn_start);

        mStartBtn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                if(isRecording){
                    Point addPoint = getCursorViewCoordinate();
                    Log.d(TAG,"addPoint = " + addPoint);
                    Toast.makeText(RemoteTransforService.this, "+("+addPoint.x+","+addPoint.y+")", Toast.LENGTH_SHORT).show();
                    coordinatePoints.add(addPoint);
                }else{
                    isTransforRuning = !isTransforRuning;
                    if(isTransforRuning){
                        Toast.makeText(RemoteTransforService.this, "start", Toast.LENGTH_SHORT).show();
                        mTransforMeneyThread = new TransforMeneyThread(myHandler,peoplePhones,coordinatePoints);
                        mTransforMeneyThread.start();
                        mStartBtn.setText(R.string.str_action_stop);
                    }else{
                        Toast.makeText(RemoteTransforService.this, "stop", Toast.LENGTH_SHORT).show();
                        mTransforMeneyThread.stopThread();
                        mTransforMeneyThread = null;
                        mStartBtn.setText(R.string.str_action_start);
                    }
                }
            }
        });

        mRecordBtn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                isRecording = !isRecording;
                if(isRecording){
                    Toast.makeText(RemoteTransforService.this, "record", Toast.LENGTH_SHORT).show();
                    mRecordBtn.setText(R.string.str_action_stop);
                    coordinatePoints.clear();
                    mStartBtn.setText("+");
                }else{
                    Toast.makeText(RemoteTransforService.this, "stop", Toast.LENGTH_SHORT).show();
                    mRecordBtn.setText(R.string.str_action_record);
                    mStartBtn.setText(R.string.str_action_start);
                }
            }
        });
    }

    private boolean isTransforRuning = false;
    private boolean isRecording = false;
    private ArrayList<Point> coordinatePoints = new ArrayList<>();
    private TransforMeneyThread mTransforMeneyThread = null;

    class TransforMeneyThread extends Thread{
        public volatile boolean exit = false;
        private Handler handler;
        private ArrayList<String> myPhones = new ArrayList<>();
        private ArrayList<Point> myPoint = new ArrayList<>();

        public TransforMeneyThread(Handler h,ArrayList<String> phones, ArrayList<Point> points){
            handler = h;
            myPhones.addAll(phones);
            myPoint.addAll(points);
        }
        public void stopThread(){
            exit = true;
            interrupt();
        }

        @Override
        public void run() {
            try {
                for (String phone : myPhones) {
                    if(exit){
                        break;
                    }
                    circleTransfor(phone);
                }
                handler.sendEmptyMessage(MSG_END);
            } catch (Exception e) {
                Log.e(TAG, "Exception description = " + e.getMessage() + ",e = " + e);
            }
        }
        public void circleTransfor(String phone){
            try {
                Utils.execCommand("input tap " + myPoint.get(0).x + " " + myPoint.get(0).y,true);
                Utils.sleep(1);
                Utils.execCommand("input tap " + myPoint.get(1).x + " " + myPoint.get(1).y,true);
                Utils.sleep(1);
                Utils.execCommand("input tap " + myPoint.get(2).x + " " + myPoint.get(2).y,true);
                Utils.sleep(1);
                String cmd1 = "input text "+ phone;
                String cmd2 = "input keyevent 66";
                String cmds[] = {cmd1,cmd2};
                Utils.execCommand(cmds,true);
                Utils.sleep(1);
            }catch (Exception e){
                Log.e(TAG,"Exception description = " + e.getMessage() + ",e = " + e);
            }


        }
    }




    private static final int MSG_END = 1001;
    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Log.d(TAG,"MyHandler what = " + what);
            switch (what){
                case MSG_END:
                    isTransforRuning = false;
                    mStartBtn.setText(R.string.str_action_start);
                    break;
                default:
                    break;
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
        if(mFloatLayout1 != null)
        {
            //移除悬浮窗口
            mWindowManager.removeView(mFloatLayout1);
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
