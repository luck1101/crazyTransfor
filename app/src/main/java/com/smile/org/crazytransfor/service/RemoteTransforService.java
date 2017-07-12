package com.smile.org.crazytransfor.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.org.crazytransfor.ConfirmDialog;
import com.smile.org.crazytransfor.MainActivity;
import com.smile.org.crazytransfor.R;
import com.smile.org.crazytransfor.biz.TransforMeneyThread;
import com.smile.org.crazytransfor.model.DataHelper;
import com.smile.org.crazytransfor.model.PointData;
import com.smile.org.crazytransfor.model.SharePreferenceUtil;
import com.smile.org.crazytransfor.module.log.L;
import com.smile.org.crazytransfor.util.Utils;
import com.umeng.analytics.MobclickAgent;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import jxl.Sheet;
import jxl.Workbook;
import com.smile.org.crazytransfor.IAidlInterface;


/**
 * Created by Administrator on 2017/5/6 0006.
 */

public class RemoteTransforService extends Service {
    //logcat -v time | grep -i -E "RemoteTransforService | MainActivity"
    private String TAG = RemoteTransforService.class.getSimpleName();
    //定义浮动窗口布局
    RelativeLayout mFloatLayout;
    LinearLayout mFloatLayout1;
    WindowManager.LayoutParams wmParams;
    WindowManager.LayoutParams wmParams1;

    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;

    Button mRecordBtn,mStartBtn,mPlayBtn,mLaheiBtn,mUserErrBtn;
    Button mCursorView;
    TextView mCount;
    private ArrayList<String> peoplePhones = new ArrayList<>();
    private MyHandler myHandler;
    private Context mContext;
    private DataHelper mDataHelper;
    @Override
    public void onCreate() {
        super.onCreate();
        L.i( "onCreate()");
        mContext = this;
        myHandler = new MyHandler();
        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        mDataHelper = new DataHelper(mContext);

    }

    public String filePath;
    public static float money = 0.01f;
    public static int COUNT = 1000;
    private PendingIntent pintent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.d("intent = " + intent + ",flag = " + flags + ",startId = " + startId);
        if(intent == null){
            return super.onStartCommand(intent, flags, startId);
        }

        Bundle bundle = intent.getExtras();
        if(bundle != null){
            String action = bundle.getString("action");
            L.d("action = " + action);
            if ("start".equals(action)){
                if(mFloatLayout == null){
                    createFloatView();
                }
                filePath = bundle.getString("filepath");
                money = bundle.getFloat("money",0.01f);
                int currentOffset = SharePreferenceUtil.getInstance().getIntValue(SharePreferenceUtil.KEY_POSITION);
                L.d("filePath = " + filePath + ",money = " + money + ",currentOffset = " + currentOffset);
                new Thread(new ReadExcelRunnble(filePath,currentOffset,COUNT)).start();
            }else if ("save".equals(action)){
                if (coordinatePoints != null && coordinatePoints.size() != 0){
                    L.d("save coordinatePoints");
                    for (String key : coordinatePoints.keySet()){
                        mDataHelper.saveCoodinate(coordinatePoints.get(key));
                    }
                }
            }else if ("clear".equals(action)){
                L.d("clear coordinatePoints");
                mDataHelper.delCoodinate();
            }else if("stop".equals(action)){
                hideViews();
            }
        }
        startForeground(startId, new Notification());
        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    class ReadExcelRunnble implements Runnable {
        private String filePath;
        private int offset=0,count=0;
        private ArrayList<String> phones = new ArrayList<>();

        public ReadExcelRunnble(String path,int offset,int count) {
            filePath = path;
            this.offset = offset;
            this.count = count;
        }

        @Override
        public void run() {
            readExcel(filePath);
        }

        public void readExcel(String path) {
            try {
                InputStream is = new FileInputStream(path);
                Workbook book = Workbook.getWorkbook(is);
                int num = book.getNumberOfSheets();
                L.d( "the num of sheets is " + num + "\n");
                // 获得第一个工作表对象
                Sheet sheet = book.getSheet(0);
                int Rows = sheet.getRows();
                int Cols = sheet.getColumns();
                L.d( "sheets 0 Rows = " + Rows + ",Cols = " + Cols);
                if(offset < Rows){
                    int readCount = count;
                    if(count > Rows - offset){
                        readCount = Rows - offset;
                    }
                    for (int j = 0; j < readCount; ++j) {
                        // getCell(Col,Row)获得单元格的值
                        phones.add(sheet.getCell(0, offset + j).getContents());
                    }
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("data",phones);
                    message.setData(bundle);
                    message.what = MSG_DATA_SUCCESS;
                    myHandler.sendMessage(message);
                }else{
                    //TODO:offset超过列表数
                    myHandler.sendEmptyMessage(MSG_DATA_ERR);
                }
                book.close();
            } catch (Exception e) {
                L.d( "e = " + e.getMessage() + "\n" + e);
                MobclickAgent.reportError(mContext, e.getStackTrace().toString());
                myHandler.sendEmptyMessage(MSG_DATA_ERR);
            }
        }
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
        mFloatLayout1 = null;
    }

    private void hideViews(){
        removeCursor();
        if(mFloatLayout != null){
            //移除悬浮窗口
            mWindowManager.removeView(mFloatLayout);
        }
        mFloatLayout = null;
    }

    private PointData getCursorViewCoordinate(){
        PointData data = new PointData();
        if(mCursorView != null){
            int[] location = new int[2];
            mCursorView.getLocationOnScreen(location);
            int x = location[0] + mCursorView.getMeasuredWidth()/2;
            int y = location[1] + mCursorView.getMeasuredHeight()/2;
            L.d( "Screen X = "+ x + ",Y = " + y);
            data.x = x;
            data.y = y;
        }else{
            data.x = 0;
            data.y = 0;
        }
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
        mFloatLayout = (RelativeLayout) inflater.inflate(R.layout.float_operate_layout, null);
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
        mPlayBtn = (Button)mFloatLayout.findViewById(R.id.btn_play);
        mLaheiBtn = (Button)mFloatLayout.findViewById(R.id.btn_lahei);
        mUserErrBtn = (Button)mFloatLayout.findViewById(R.id.btn_user_name);
        mCount = (TextView) mFloatLayout.findViewById(R.id.txt_count);

        mStartBtn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub

                if(isRecording){
                    if(currentIndex >= KEYS.length){
                        Toast.makeText(RemoteTransforService.this, "不需要再录制了,点数够了", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PointData addPoint = getCursorViewCoordinate();
                    addPoint.key = KEYS[currentIndex];
                    currentIndex++;
                    L.d("addPoint = " + addPoint);
                    Toast.makeText(RemoteTransforService.this, addPoint.key+"("+addPoint.x+","+addPoint.y+")", Toast.LENGTH_SHORT).show();
                    coordinatePoints.put(addPoint.key,addPoint);
                    mStartBtn.setText("+"+coordinatePoints.size());
                }else{
                    if(coordinatePoints == null || coordinatePoints.size() == 0){
                        coordinatePoints = mDataHelper.getCoodinateList();
                    }
                    if(coordinatePoints == null||coordinatePoints.size() < 8){
                        Toast.makeText(RemoteTransforService.this, getString(R.string.str_action_point_invalid), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mPlayBtn.setVisibility(View.VISIBLE);
                    mUserErrBtn.setVisibility(View.GONE);
                    mLaheiBtn.setVisibility(View.GONE);
                    isStarting = !isStarting;
                    if(isStarting){
                        TransforMeneyThread.getInstance(mContext).setPoints(coordinatePoints);
                        TransforMeneyThread.getInstance(mContext).setHandler(myHandler);
                        TransforMeneyThread.getInstance(mContext).setPhones(peoplePhones);
                        TransforMeneyThread.getInstance(mContext).start();
                        mStartBtn.setText(R.string.str_action_stop);
                        mPlayBtn.setText(R.string.str_action_pause);
                        isPlaying = true;
                    }else{
                        Toast.makeText(RemoteTransforService.this, "stop", Toast.LENGTH_SHORT).show();
                        TransforMeneyThread.getInstance(mContext).onStopThread();
                        mStartBtn.setText(R.string.str_action_start);
                    }

                }
            }
        });

        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlaying = !isPlaying;
                if(isPlaying){
                    Toast.makeText(RemoteTransforService.this, "play", Toast.LENGTH_SHORT).show();
                    TransforMeneyThread.getInstance(mContext).onThreadResume();
                    mPlayBtn.setText(R.string.str_action_pause);
                }else{
                    Toast.makeText(RemoteTransforService.this, "pause", Toast.LENGTH_SHORT).show();
                    TransforMeneyThread.getInstance(mContext).onThreadOnPause();
                    mPlayBtn.setText(R.string.str_action_play);
                }

            }
        });

        mRecordBtn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                currentIndex = 0;
                isRecording = !isRecording;
                if(isRecording){
                    createCursorView();
                    Toast.makeText(RemoteTransforService.this, "record", Toast.LENGTH_SHORT).show();
                    mRecordBtn.setText("保存");
                    coordinatePoints.clear();
                    mStartBtn.setText("+");
                    mPlayBtn.setVisibility(View.GONE);
                    mUserErrBtn.setVisibility(View.VISIBLE);
                    mLaheiBtn.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(RemoteTransforService.this, "save", Toast.LENGTH_SHORT).show();
                    mRecordBtn.setText(R.string.str_action_record);
                    mStartBtn.setText(R.string.str_action_start);
                    removeCursor();
                    mPlayBtn.setVisibility(View.GONE);
                    mUserErrBtn.setVisibility(View.GONE);
                    mLaheiBtn.setVisibility(View.GONE);

                    final ConfirmDialog confirmDialog = new ConfirmDialog(mContext, R.style.white_bg_dialog);
                    confirmDialog.setContent("保存现在录制的位置，点击确定后，之前的数据会被删除");
                    confirmDialog.setConfirmClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            L.d("sure");
                            if (coordinatePoints != null && coordinatePoints.size() != 0){
                                L.d("save coordinatePoints");
                                for (String key : coordinatePoints.keySet()){
                                    mDataHelper.saveCoodinate(coordinatePoints.get(key));
                                }
                            }
                            confirmDialog.dismiss();
                        }
                    });
                    confirmDialog.setCancelClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            L.d("cancel");
                            confirmDialog.dismiss();
                        }
                    });
                    confirmDialog.setCanceledOnTouchOutside(true);
                    confirmDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    confirmDialog.show();
                }
            }
        });

        mLaheiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PointData addPoint = getCursorViewCoordinate();
                addPoint.key = KEY_LAHEI;
                L.d("addPoint = " + addPoint);
                Toast.makeText(RemoteTransforService.this, "用户被拉黑，点击确认("+addPoint.x+","+addPoint.y+")", Toast.LENGTH_SHORT).show();
                coordinatePoints.put(addPoint.key,addPoint);
            }
        });

        mUserErrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PointData addPoint = getCursorViewCoordinate();
                addPoint.key = KEY_USER_NAME;
                L.d("addPoint = " + addPoint);
                Toast.makeText(RemoteTransforService.this, "转账需要输入姓氏，点击取消("+addPoint.x+","+addPoint.y+")", Toast.LENGTH_SHORT).show();
                coordinatePoints.put(addPoint.key,addPoint);
            }
        });
    }

    public static final String KEY_LAHEI = "key_lahei";
    public static final String KEY_USER_NAME = "key_user";

    public static final String KEY_1 = "key_1";
    public static final String KEY_2 = "key_2";
    public static final String KEY_3 = "key_3";
    public static final String KEY_4 = "key_4";
    public static final String KEY_5 = "key_5";
    public static final String KEY_6 = "key_6";
    public static final String KEY_7 = "key_7";
    public static final String KEY_8 = "key_8";
    public static final String KEY_9 = "key_9";
    public String[] KEYS= {KEY_1,KEY_2,KEY_3,KEY_4,KEY_5,KEY_6,KEY_7,KEY_8,KEY_9};
    public int currentIndex = 0;

    private boolean isStarting = false;
    private boolean isPlaying = false;
    private boolean isRecording = false;
    private HashMap<String,PointData> coordinatePoints = new HashMap<>();

    public static final int MSG_REQUEST_DATA = 1001;
    public static final int MSG_DATA_ERR = 2001;
    public static final int MSG_DATA_SUCCESS = 2002;
    public static final int MSG_REQUEST_UPDATE_VIEW = 2003;
    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            L.d("MyHandler what = " + what);
            switch (what){
                case MSG_REQUEST_DATA:
                    TransforMeneyThread.getInstance(mContext).onStopThread();
                    Toast.makeText(RemoteTransforService.this, "一批号码转账完毕，读取下一批号码", Toast.LENGTH_SHORT).show();
                    int position = SharePreferenceUtil.getInstance().getIntValue(SharePreferenceUtil.KEY_POSITION);
                    new Thread(new ReadExcelRunnble(filePath,position,COUNT)).start();
                    break;
                case MSG_DATA_SUCCESS:
                    Bundle bundle = msg.getData();
                    ArrayList<String> phones = null;
                    if(bundle != null){
                        phones = bundle.getStringArrayList("data");
                    }
                    if(!Utils.isEmpty(phones)){
                        peoplePhones.clear();
                        peoplePhones.addAll(phones);
                        Toast.makeText(RemoteTransforService.this, "新读取到" + phones.size() + "条新数据，开始转账", Toast.LENGTH_SHORT).show();
                        if(isStarting){
                            TransforMeneyThread.getInstance(mContext).setPoints(coordinatePoints);
                            TransforMeneyThread.getInstance(mContext).setHandler(myHandler);
                            TransforMeneyThread.getInstance(mContext).setPhones(peoplePhones);
                            TransforMeneyThread.getInstance(mContext).start();
                        }
                    }
                    break;
                case MSG_DATA_ERR:
                    Toast.makeText(RemoteTransforService.this, "已经读到文件末尾，没有新数据了", Toast.LENGTH_SHORT).show();
                    stopSelf();
                    break;
                case MSG_REQUEST_UPDATE_VIEW:
                    int position2 = SharePreferenceUtil.getInstance().getIntValue(SharePreferenceUtil.KEY_POSITION)+1;
                    mCount.setText(Integer.toString(position2));
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
        return new MyBinder();
    }

    class MyBinder extends IAidlInterface.Stub{
        @Override
        public int getPosition() throws RemoteException {
            return SharePreferenceUtil.getInstance().getIntValue(SharePreferenceUtil.KEY_POSITION);
        }

        @Override
        public void setPostion(int p) throws RemoteException {
            L.d("setPostion() p = " + p);
            SharePreferenceUtil.getInstance().save(SharePreferenceUtil.KEY_POSITION,p);
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    }



}
