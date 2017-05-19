package com.smile.org.crazytransfor;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.org.crazytransfor.service.RemoteTransforService;
import com.smile.org.crazytransfor.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jxl.Sheet;
import jxl.Workbook;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edit_file_path)
    EditText edit_file_path;
    @BindView(R.id.btn_open)
    Button btn_open;
    @BindView(R.id.btn_start_float)
    Button btn_start_float;
    @BindView(R.id.btn_save)
    Button btn_save;
    @BindView(R.id.btn_clear)
    Button btn_clear;
    @BindView(R.id.txt_file_content)
    TextView txt_file_content;

    private String TAG = MainActivity.class.getSimpleName();
    private final int REC_REQUESTCODE = 1101;

    @OnClick(R.id.btn_save)
    void save(){
        // TODO Auto-generated method stub
        Intent intent = new Intent(MainActivity.this, RemoteTransforService.class);
        intent.putExtra("action","save");
        startService(intent);
    }

    @OnClick(R.id.btn_clear)
    void clear(){
        // TODO Auto-generated method stub
        Intent intent = new Intent(MainActivity.this, RemoteTransforService.class);
        intent.putExtra("action","clear");
        startService(intent);
    }

    @OnClick(R.id.btn_open)
    void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REC_REQUESTCODE);
    }


    @OnClick(R.id.btn_start_float)
    void startFloatWindows() {
        // TODO Auto-generated method stub
        if(TextUtils.isEmpty(edit_file_path.getText())){
            Toast.makeText(this, "请输入文件路径", Toast.LENGTH_SHORT).show();
            return ;
        }
        String filepath = edit_file_path.getText().toString();
        Intent intent = new Intent(MainActivity.this, RemoteTransforService.class);
        //启动FxService
        intent.putExtra("action","start");
        intent.putExtra("filepath",filepath);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Log.d(TAG,"time = " + DateToLong(new Date()));

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
}
