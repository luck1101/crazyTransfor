package com.smile.org.crazytransfor;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.smile.org.crazytransfor.model.SharePreferenceUtil;
import com.smile.org.crazytransfor.service.RemoteTransforService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edit_file_path)
    EditText edit_file_path;
    @BindView(R.id.edit_transfor_money)
    EditText edit_transfor_money;
    @BindView(R.id.btn_open)
    Button btn_open;
    @BindView(R.id.btn_start_float)
    Button btn_start_float;
    @BindView(R.id.btn_save)
    Button btn_save;
    @BindView(R.id.btn_clear)
    Button btn_clear;

    private String TAG = MainActivity.class.getSimpleName();
    private final int REC_REQUESTCODE = 1101;
    private static boolean isStart = false;

    @OnClick(R.id.btn_save)
    void save(){
        // TODO Auto-generated method stub
        if(isStart){
            Intent intent = new Intent(MainActivity.this, RemoteTransforService.class);
            intent.putExtra("action","save");
            startService(intent);
        }else{
            Toast.makeText(this, "服务未启动", Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.btn_clear)
    void clear(){
        // TODO Auto-generated method stub
        if(isStart){
            Intent intent = new Intent(MainActivity.this, RemoteTransforService.class);
            intent.putExtra("action","clear");
            startService(intent);
        }else{
            Toast.makeText(this, "服务未启动", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_open)
    void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REC_REQUESTCODE);
    }

    @OnClick(R.id.btn_stop)
    void stopService() {
        Intent intent = new Intent(MainActivity.this, RemoteTransforService.class);
        stopService(intent);
        isStart = false;
    }


    @OnClick(R.id.btn_start_float)
    void startFloatWindows() {
        float money = 0.01f;
        // TODO Auto-generated method stub
        if(TextUtils.isEmpty(edit_file_path.getText())){
            Toast.makeText(this, "请输入文件路径", Toast.LENGTH_SHORT).show();
            return ;
        }
        if(!TextUtils.isEmpty(edit_transfor_money.getText())){
            money = Float.valueOf(edit_transfor_money.getText().toString());
        }
        String filepath = edit_file_path.getText().toString();
        Intent intent = new Intent(MainActivity.this, RemoteTransforService.class);
        //启动FxService
        intent.putExtra("action","start");
        intent.putExtra("filepath",filepath);
        intent.putExtra("money",money);
        SharePreferenceUtil.getInstance(MainActivity.this).save(SharePreferenceUtil.KEY_POSITION,0);
        Log.d(TAG,"filepath = " + filepath + ",money = " + money);
        startService(intent);
        isStart = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Log.d(TAG,"isstart = " + isStart);
        Log.d(TAG,"onCreate()");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG,"isstart = " + isStart);
        Log.d(TAG,"onNewIntent()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume()");
    }

    private String DateToLong(Date time){
        SimpleDateFormat fmt = new SimpleDateFormat("yyMMddHHmm");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcTime=fmt.format(new Date());
        return  utcTime;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REC_REQUESTCODE) {
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String string = uri.toString();
            File file;
            String a[] = new String[2];
            //判断文件是否在sd卡中
            if (string.indexOf(String.valueOf(Environment.getExternalStorageDirectory())) != -1) {
                //对Uri进行切割
                a = string.split(String.valueOf(Environment.getExternalStorageDirectory()));
                //获取到file
                file = new File(Environment.getExternalStorageDirectory(), a[1]);
            } else if (string.indexOf(String.valueOf(Environment.getDataDirectory())) != -1) { //判断文件是否在手机内存中
                //对Uri进行切割
                a = string.split(String.valueOf(Environment.getDataDirectory()));
                //获取到file
                file = new File(Environment.getDataDirectory(), a[1]);
            } else {
                //出现其他没有考虑到的情况
                Toast.makeText(this, "文件路径解析失败！", Toast.LENGTH_SHORT);
                return;
            }
            final String filePath = file.getAbsolutePath();
            Log.d(TAG, "onActivityResult filePath = " + filePath);
            edit_file_path.setText(filePath);
        }
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            Log.d(TAG,"mName = " + mName);
            if (mName.contains(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
