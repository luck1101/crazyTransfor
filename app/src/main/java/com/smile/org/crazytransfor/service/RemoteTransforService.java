package com.smile.org.crazytransfor.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
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
import com.smile.org.crazytransfor.model.PointData;
import com.smile.org.crazytransfor.util.DataHelper;
import com.smile.org.crazytransfor.util.Utils;

import java.util.ArrayList;
import java.util.List;

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
    private Context mContext;
    private DataHelper mDataHelper;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        mContext = this;
        myHandler = new MyHandler();
        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        mDataHelper = new DataHelper(mContext);
        createFloatView();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra("action");
        Log.d(TAG,"action = " + action);
        if ("start".equals(action)){
            ArrayList<String> phones = intent.getStringArrayListExtra("data");
            Log.d(TAG,"phones = " + phones);
            if (phones != null){
                peoplePhones.clear();
                peoplePhones.addAll(phones);
            }
        }else if ("save".equals(action)){
            if (!Utils.isEmpty(coordinatePoints)){
                Log.d(TAG,"save coordinatePoints");
                for (PointData pointData : coordinatePoints){
                    mDataHelper.saveCoodinate(pointData);
                }
            }
        }else if ("clear".equals(action)){
            Log.d(TAG,"clear coordinatePoints");
            mDataHelper.delCoodinate();
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
                wmParams1.y = (int) event.getRawY() - mCursorView.getMeasuredHeight()/2;
                //刷新
                mWindowManager.updateViewLayout(mFloatLayout1, wmParams1);
                return false;  //此处必须返回false，否则OnClickListener获取不到监听
            }
        });
    }

    private void removeCursor(){
        if(mFloatLayout1 != null)
        {
            //移除悬浮窗口
            mWindowManager.removeView(mFloatLayout1);
        }
    }

    private PointData getCursorViewCoordinate(){
        int[] location = new int[2];
        mCursorView.getLocationOnScreen(location);
        int x = location[0] + mCursorView.getMeasuredWidth()/2;
        int y = location[1] + mCursorView.getMeasuredHeight()/2;
        Log.d(TAG, "Screen X = "+ x + ",Y = " + y);
        PointData data = new PointData();
        data.x = x;
        data.y = y;
        return data;
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
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
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
                wmParams.y = (int) event.getRawY() - mFloatLayout.getMeasuredHeight()/2;
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
                    PointData addPoint = getCursorViewCoordinate();
                    Log.d(TAG,"addPoint = " + addPoint);
                    Toast.makeText(RemoteTransforService.this, "+("+addPoint.x+","+addPoint.y+")", Toast.LENGTH_SHORT).show();
                    coordinatePoints.add(addPoint);
                    mStartBtn.setText("+"+coordinatePoints.size());
                }else{
                    if(Utils.isEmpty(coordinatePoints)){
                        coordinatePoints = (ArrayList<PointData>) mDataHelper.getCoodinateList();
                    }
                    if(Utils.isEmpty(coordinatePoints)||coordinatePoints.size() < 8){
                        Toast.makeText(RemoteTransforService.this, getString(R.string.str_action_point_invalid), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isTransforRuning = !isTransforRuning;
                    if(isTransforRuning){
                        Toast.makeText(RemoteTransforService.this, "start", Toast.LENGTH_SHORT).show();
                        if(mTransforMeneyThread != null){
                            mTransforMeneyThread.stopThread();
                            mTransforMeneyThread = null;
                        }
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
                    createCursorView();
                    Toast.makeText(RemoteTransforService.this, "record", Toast.LENGTH_SHORT).show();
                    mRecordBtn.setText(R.string.str_action_stop);
                    coordinatePoints.clear();
                    mStartBtn.setText("+");
                }else{
                    Toast.makeText(RemoteTransforService.this, "stop", Toast.LENGTH_SHORT).show();
                    mRecordBtn.setText(R.string.str_action_record);
                    mStartBtn.setText(R.string.str_action_start);
                    removeCursor();
                }
            }
        });
    }

    private boolean isTransforRuning = false;
    private boolean isRecording = false;
    private ArrayList<PointData> coordinatePoints = new ArrayList<>();
    private TransforMeneyThread mTransforMeneyThread = null;

    class TransforMeneyThread extends Thread{
        public volatile boolean exit = false;
        private Handler handler;
        private ArrayList<String> myPhones = new ArrayList<>();
        private ArrayList<PointData> myPoint = new ArrayList<>();

        public TransforMeneyThread(Handler h,ArrayList<String> phones, ArrayList<PointData> points){
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
        public final String ZFB_MAIN = "AlipayLogin";
        public final String ZFB_ADD_FRIEND = "AddFriendActivity_";
        public final String ZFB_FRIEND = "ProfileActivity_";
        public final String ZFB_TRANSFOR = "TFToAccountConfirmActivity_";
        public final String ZFB_INPUT_PASSWD = "FlyBirdWindowActivity";
        public final String ZFB_TRANSFOR_SUCCES = "TransferToAccountSuccessActivity_";
        public final String ZFB_ENTER_PERSON = "PersonalChatMsgActivity_";


        /**
         * 淘宝主页：[packageName = com.eg.android.AlipayGphone/.AlipayLogin,topActivityName = com.eg.android.AlipayGphone.AlipayLogin]
         * 添加朋友：[packageName = com.eg.android.AlipayGphone,topActivityName = com.alipay.mobile.contactsapp.ui.AddFriendActivity_]
         * 好友界面：[packageName = com.eg.android.AlipayGphone,topActivityName = com.alipay.android.phone.wallet.profileapp.ui.ProfileActivity_]
         * 转账界面：[packageName = com.eg.android.AlipayGphone,topActivityName = com.alipay.mobile.transferapp.ui.TFToAccountConfirmActivity_]
         * 转账确认界面：lipay.mobile.transferapp.ui.TFToAccountConfirmActivity_
         * 输入密码界面：[packageName = com.eg.android.AlipayGphone,topActivityName = com.alipay.android.app.flybird.ui.window.FlyBirdWindowActivity]
         * 转账成功界面：[packageName = com.eg.android.AlipayGphone,topActivityName = com.alipay.mobile.transferapp.ui.TransferToAccountSuccessActivity_]
         * 进入个人界面：[packageName = com.eg.android.AlipayGphone,topActivityName = com.alipay.mobile.chatapp.ui.PersonalChatMsgActivity_]按返回又回到主页的朋友选项
         * 方案：1.进入支付宝主页，先点击首页->点击+->点击添加朋友->到添加好友界面点击搜索框->输入号码回车
         *       2.此时判断回车后的当前界面：a.不在ProfileActivity_界面，则输入返回->返回->返回,重新换下一个号码操作；
         *                                 b.在ProfileActivity_界面->点击转账->->返回->输入金额->点击确认转账
         *      3.判断当前界面如果在输入密码FlyBirdWindowActivity界面，输入密码->进到TransferToAccountSuccessActivity_点击完成->进到PersonalChatMsgActivity_点击返回
         * @param phone
         */
        public void circleTransfor(String phone){
            try {
                Log.d(TAG,"start");
                int x = 0,y = 0;
                if(waitForActivity(ZFB_MAIN,6)){
                    //点击1：首页
                    Utils.sleep(800);
                    x = myPoint.get(0).x;
                    y = myPoint.get(0).y;
                    Log.d(TAG,"tap1 main x = " + x + ",y = " + y);
                    Utils.execCommand("input tap " + x + " " + y,true);
                }else{
                    Log.e(TAG,"1 not ZFB_MAIN");
                }

                if(waitForActivity(ZFB_MAIN,6)){
                    //点击2：+号
                    Utils.sleep(800);
                    x = myPoint.get(1).x;
                    y = myPoint.get(1).y;
                    Log.d(TAG,"tap2 + x = " + x + ",y = " + y);
                    Utils.execCommand("input tap " + x + " " + y,true);
                }else{
                    Log.e(TAG,"2 not ZFB_MAIN");
                }

                if(waitForActivity(ZFB_MAIN,6)){
                    ////点击3：添加好友
                    Utils.sleep(800);
                    x = myPoint.get(2).x;
                    y = myPoint.get(2).y;
                    Log.d(TAG,"tap3 add friend x = " + x + ",y = " + y);
                    Utils.execCommand("input tap " + x + " " + y,true);
                }else{
                    Log.e(TAG,"3 not ZFB_MAIN");
                }

                if(waitForActivity(ZFB_ADD_FRIEND,6)){
                    //点击4：输入框
                    Utils.sleep(800);
                    x = myPoint.get(3).x;
                    y = myPoint.get(3).y;
                    Log.d(TAG,"tap4 input phone x = " + x + ",y = " + y);
                    Utils.execCommand("input tap " + x + " " + y,true);
                    Utils.sleep(1000);
                    //输入号码
                    Log.d(TAG,"input text " + phone);
                    Utils.execCommand("input text " + phone,true);
                    Utils.sleep(800);
                    Utils.execCommand("input keyevent 66 ",true);
                }else{
                    Log.e(TAG,"4 not ZFB_ADD_FRIEND");
                }

                if(waitForActivity(ZFB_FRIEND,6)) {
                    //点击5：转账
                    Utils.sleep(2000);
                    x = myPoint.get(4).x;
                    y = myPoint.get(4).y;
                    Log.d(TAG,"tap5 transfor x = " + x + ",y = " + y);
                    Utils.execCommand("input tap " + x + " " + y,true);
                }else{
                    Log.e(TAG,"5 not ZFB_FRIEND,return to ZFB_MAIN");
                    Utils.sleep(1500);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1500);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1500);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1500);
                    return;
                }
                if(waitForActivity(ZFB_FRIEND,2)) {
                    //点击5：转账
                    Utils.sleep(2000);
                    x = myPoint.get(4).x;
                    y = myPoint.get(4).y + 68;
                    Log.d(TAG,"tap5 transfor x = " + x + ",y = " + y);
                    Utils.execCommand("input tap " + x + " " + y,true);
                }
                if(waitForActivity(ZFB_FRIEND,2)) {
                    //点击5：转账
                    Utils.sleep(2000);
                    x = myPoint.get(4).x;
                    y = myPoint.get(4).y - 68;
                    Log.d(TAG,"tap5 transfor x = " + x + ",y = " + y);
                    Utils.execCommand("input tap " + x + " " + y,true);
                }

                //判断是否在转账界面
                if(waitForActivity(ZFB_TRANSFOR,6)) {
                    //输入转账金额
                    Log.d(TAG,"input text 0.01");
                    Utils.sleep(800);
                    Utils.execCommand("input text " + 0.01,true);
                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 66 ",true);
                    Utils.sleep(1000);
                    //输入备注
                    Log.d(TAG,"input text hello");
                    Utils.execCommand("input text " + "hello",true);
                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1000);
                    //点击6:确认转账
                    x = myPoint.get(5).x;
                    y = myPoint.get(5).y;
                    Log.d(TAG,"tap6 sure transfor x = " + x + ",y = " + y);
                    Utils.execCommand("input tap " + x + " " + y,true);
                    Utils.sleep(3000);
                }else{
                    Log.e(TAG,"6 not ZFB_TRANSFOR");
                }

                //确认转账后，弹出输入用户名时，返回到之前的操作
                if(waitForActivity(ZFB_TRANSFOR,6)) {
                    Log.d(TAG,"需要输入用户名，点击取消，返回主页");
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1000);
                    x = myPoint.get(6).x;
                    y = myPoint.get(6).y;
                    Log.d(TAG,"tap6 sure transfor x = " + x + ",y = " + y);
                    Utils.execCommand("input tap " + x + " " + y,true);

                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 4 ",true);
                }

                //确认转账后，弹出你已经被拉黑
                if(waitForActivity(ZFB_TRANSFOR,6)) {
                    Log.d(TAG,"被拉黑，点击确定，返回主页");
                    x = 553;
                    y = 726;
                    Log.d(TAG,"tap6 sure transfor x = " + x + ",y = " + y);
                    Utils.execCommand("input tap " + x + " " + y,true);

                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 4 ",true);
                    return;
                }


                if(waitForActivity(ZFB_TRANSFOR_SUCCES,6)){
                    //点击7:完成
                    Utils.sleep(800);
                    x = myPoint.get(7).x;
                    y = myPoint.get(7).y;
                    Log.d(TAG,"tap7 complement x = " + x + ",y = " + y);
                    Utils.execCommand("input tap " + x + " " + y,true);
                }else{
                    Log.e(TAG,"7 not ZFB_TRANSFOR_SUCCES");
                }

                if(waitForActivity(ZFB_ENTER_PERSON,6)){
                    Utils.sleep(1500);
                    Utils.execCommand("input keyevent 4 ",true);
                    Log.d(TAG,"end");
                }else{
                    Log.e(TAG,"8 not ZFB_ENTER_PERSON");
                }

                /*if(true){
                    Log.d(TAG,"找不到好友，回到主页");
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1000);
                    Utils.execCommand("input keyevent 4 ",true);
                    Utils.sleep(1000);
                    Log.d(TAG,"end");
                    return ;
                }*/

            }catch (Exception e){
                Log.e(TAG,"Exception description = " + e.getMessage() + ",e = " + e);
            }


        }
    }

    public boolean waitForActivity(String activity,int count){
        boolean result = false;
        while(count > 0){
            Utils.sleep(500);
            if(Utils.getTopActivityInfo(mContext).topActivityName.contains(activity)){
                result = true;
                break;
            }
            count--;
        }
        return result;
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
        mDataHelper.close();
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
