package com.smile.org.crazytransfor;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.smile.org.crazytransfor.model.SharePreferenceUtil;
import com.smile.org.crazytransfor.module.log.L;
import com.smile.org.crazytransfor.service.RemoteTransforService;
import com.umeng.analytics.MobclickAgent;

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

    @OnClick(R.id.btn_save)
    void save() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(getApplicationContext(), RemoteTransforService.class);
        Bundle bundle = new Bundle();
        bundle.putString("action", "save");
        intent.putExtras(bundle);
        startService(intent);
    }

    @OnClick(R.id.btn_clear)
    void clear() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(getApplicationContext(), RemoteTransforService.class);
        Bundle bundle = new Bundle();
        bundle.putString("action", "clear");
        intent.putExtras(bundle);
        startService(intent);

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
        Intent intent = new Intent(getApplicationContext(), RemoteTransforService.class);
        Bundle bundle = new Bundle();
        bundle.putString("action", "stop");
        intent.putExtras(bundle);
        startService(intent);

    }


    @OnClick(R.id.btn_start_float)
    void startFloatWindows() {
        // TODO Auto-generated method stub
        if (TextUtils.isEmpty(edit_file_path.getText())) {
            Toast.makeText(this, "请输入文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int currentOffset = 0;
            currentOffset = iAidlInterface.getPosition();
            L.d("startFloatWindows() currentOffset = " + currentOffset);
            if (currentOffset > 0) {
                // TODO: 2017/6/2
                final ConfirmDialog confirmDialog = new ConfirmDialog(this, R.style.white_bg_dialog);
                confirmDialog.setContent(String.format(getString(R.string.dialog_tip), currentOffset));
                confirmDialog.setConfirmClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        L.d("sure");
                        confirmDialog.dismiss();
                        startRemoteService();
                    }
                });
                confirmDialog.setCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        L.d("cancel");
                        //SharePreferenceUtil.getInstance().save(SharePreferenceUtil.KEY_POSITION, 0);
                        try {
                            iAidlInterface.setPostion(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            MobclickAgent.reportError(MainActivity.this, e.getStackTrace().toString());
                        }
                        confirmDialog.dismiss();
                        startRemoteService();
                    }
                });
                confirmDialog.setCanceledOnTouchOutside(true);
                confirmDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                confirmDialog.show();
            } else {
                startRemoteService();
            }
        } catch (Exception e) {
            L.d("startFloatWindows e =" + e.getMessage());
            MobclickAgent.reportError(MainActivity.this, e.getStackTrace().toString());
        }

    }

    private void startRemoteService() {
        float money = 0.01f;
        if (!TextUtils.isEmpty(edit_transfor_money.getText())) {
            money = Float.valueOf(edit_transfor_money.getText().toString());
        }
        String filepath = edit_file_path.getText().toString();
        Intent intent = new Intent(getApplicationContext(), RemoteTransforService.class);
        //启动FxService
        Bundle bundle = new Bundle();
        bundle.putString("action", "start");
        bundle.putString("filepath", filepath);
        bundle.putFloat("money", money);
        intent.putExtras(bundle);
        L.d("filepath = " + filepath + ",money = " + money);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        L.d("onCreate()");

    }

    private void bindService() {
        Intent binderIntent = new Intent(this, RemoteTransforService.class);
        boolean isSuccess = bindService(binderIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        L.d("isSuccess = " + isSuccess);
    }

    private IAidlInterface iAidlInterface;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iAidlInterface = IAidlInterface.Stub.asInterface(service);
            L.d("onServiceConnected()");
            //连接成功调动
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //断开连接调用
            L.d("onServiceDisconnected()");
            iAidlInterface = null;
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        L.d("onNewIntent()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        L.d("onStart()");
        bindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        L.d("onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    private String DateToLong(Date time) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyMMddHHmm");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcTime = fmt.format(new Date());
        return utcTime;
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
            L.d("onActivityResult filePath = " + filePath);
            edit_file_path.setText(filePath);
        }
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
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
            L.d("mName = " + mName);
            if (mName.contains(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
