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
    private MyHandler myHandler;

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

        Log.d(TAG,"time = " + DateToLong(new Date()));
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
        Intent intent = new Intent(MainActivity.this, RemoteTransforService.class);
        //启动FxService
        myPhones.add("15280595020");
        myPhones.add("15280595021");
        myPhones.add("15280595022");
        myPhones.add("15280595023");
        myPhones.add("15280595024");
        intent.putExtra("action","start");
        intent.putStringArrayListExtra("data", myPhones);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        myHandler = new MyHandler();
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
            if (filePath.endsWith(".xls") || filePath.endsWith(".xlsx")) {
                Log.d(TAG, "start ReadExcelRunnble thread");
                myHandler.post(new ReadExcelRunnble(filePath));
            } else {
                txt_file_content.setText(R.string.file_err);
            }
        }
    }

    public static final int MSG_UPDATE_DATA = 1101;
    public ArrayList<String> myPhones = new ArrayList<String>();

    class MyHandler extends Handler {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            int what = msg.what;
            Log.d(TAG, "msg.what =  " + what);
            Bundle bundle = msg.getData();
            switch (what) {
                case MSG_UPDATE_DATA:
                    ArrayList<String> phones = null;
                    if (bundle != null) {
                        phones = bundle.getStringArrayList("data");
                    }
                    Log.d(TAG, "phones = " + phones);
                    StringBuffer content = new StringBuffer();
                    if (phones == null || phones.isEmpty()) {
                        content.append(getString(R.string.file_empty));
                    } else {
                        myPhones.clear();
                        myPhones.addAll(phones);
                        for (String phone : phones)
                            content.append(phone + "\n");
                    }
                    txt_file_content.setText(content.toString());
                    break;
                default:
                    break;
            }
        }
    }

    class ReadExcelRunnble implements Runnable {
        private String filePath;

        public ReadExcelRunnble(String path) {
            filePath = path;
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
                Log.d(TAG, "the num of sheets is " + num + "\n");
                // 获得第一个工作表对象
                Sheet sheet = book.getSheet(0);
                int Rows = sheet.getRows();
                int Cols = sheet.getColumns();
                Log.d(TAG, "sheets 0 Rows = " + Rows + ",Cols = " + Cols);
                ArrayList<String> phones = new ArrayList<>();
                for (int i = 0; i < Cols; ++i) {
                    for (int j = 0; j < Rows; ++j) {
                        // getCell(Col,Row)获得单元格的值
                        phones.add(sheet.getCell(i, j).getContents());
                    }
                }
                sendMSG(phones);
                book.close();
            } catch (Exception e) {
                Log.d(TAG, "e = " + e.getMessage() + "\n" + e);
            }
        }

        public void sendMSG(ArrayList<String> content) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("data", content);
            msg.what = MSG_UPDATE_DATA;
            msg.setData(bundle);
            myHandler.sendMessage(msg);
        }
    }


}
