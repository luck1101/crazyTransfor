package com.smile.org.crazytransfor.biz;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.smile.org.crazytransfor.model.PointData;
import com.smile.org.crazytransfor.service.RemoteTransforService;
import com.smile.org.crazytransfor.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Administrator on 2017/5/18 0018.
 */

public class TransforMeneyThread extends Thread {
    private String TAG = TransforMeneyThread.class.getSimpleName();
    private Context mContext = null;
    private static TransforMeneyThread single = null;
    private Handler handler;
    private ArrayList<String> myPhones = new ArrayList<>();
    private HashMap<String,PointData> myPoint = new HashMap<>();
    private boolean isPause = false;
    private boolean isClose = false;
    public final static int STATE_STOP = -1;
    public final static int STATE_INIT = 0;
    public final static int STATE_START = 1;
    public final static int STATE_PLAY = 3;
    public final static int STATE_PAUSE = 4;
    public int state = STATE_INIT;

    private TransforMeneyThread(Context c){
        mContext = c;
    }


    public synchronized static TransforMeneyThread getInstance(Context c) {
        if (single == null) {
            single = new TransforMeneyThread(c);
        }
        return single;
    }

    public void setHandler(Handler h){
        handler = h;
    }

    public void setPoints(HashMap<String,PointData> points){
        synchronized (this){
            if(myPoint != points){
                myPoint.clear();
                myPoint = points;
            }
        }

    }

    public void setPhones(ArrayList<String> phones){
        synchronized (this){
            if(myPhones != phones){
                myPhones.clear();
                myPhones.addAll(phones);
            }
        }

    }

    public void onThreadOnPause(){
        state = STATE_PAUSE;
        isPause = true;
    }

    /**
     * 线程等待,不提供给外部调用
     */
    private void onThreadWait() {
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 线程继续运行
     */
    public synchronized void onThreadResume() {
        state = STATE_PLAY;
        isPause = false;
        this.notify();
    }

    /**
     * 关闭线程
     */
    public synchronized void onStopThread() {
        state = STATE_STOP;
        try {
            notify();
            setClose(true);
            interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        single = null;
    }

    public boolean isClose() {
        return isClose;
    }

    public void setClose(boolean isClose) {
        this.isClose = isClose;
    }

    @Override
    public void run() {
        state = STATE_START;
        while (!isClose && !isInterrupted()) {
            if (myPhones.size() > 0 && !isPause) {
                String phone = myPhones.get(0);
                Log.d(TAG,"run phone = " + phone);
                circleTransfor(phone);
                synchronized (myPhones) {
                    myPhones.remove(0);
                }
                Utils.sleep(100);
            }else {
                if(myPhones == null || myPhones.size() == 0){
                    handler.sendEmptyMessage(RemoteTransforService.MSG_REQUEST_DATA);
                    onStopThread();
                    return;
                }
                Log.d(TAG,"onThreadWait");
                onThreadWait();
            }
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
     * 15280595020正常
     * 15280595021被拉黑
     * 15280595030需要输入名字
     */
    private void circleTransfor(String phone){
        try {
            Log.d(TAG,"start");
            int x = 0,y = 0;
            if(waitForActivity(ZFB_MAIN,6)){
                //点击1：首页
                Thread.currentThread().sleep(800);
                x = myPoint.get(RemoteTransforService.KEY_1).x;
                y = myPoint.get(RemoteTransforService.KEY_1).y;
                Log.d(TAG,"tap1 main x = " + x + ",y = " + y);
                Utils.execCommand("input tap " + x + " " + y,true);
            }else{
                Log.e(TAG,"1 not ZFB_MAIN");
            }

            if(waitForActivity(ZFB_MAIN,6)){
                //点击2：+号
                Thread.currentThread().sleep(800);
                x = myPoint.get(RemoteTransforService.KEY_2).x;
                y = myPoint.get(RemoteTransforService.KEY_2).y;
                Log.d(TAG,"tap2 + x = " + x + ",y = " + y);
                Utils.execCommand("input tap " + x + " " + y,true);
            }else{
                Log.e(TAG,"2 not ZFB_MAIN");
            }

            if(waitForActivity(ZFB_MAIN,6)){
                ////点击3：添加好友
                Thread.currentThread().sleep(800);
                x = myPoint.get(RemoteTransforService.KEY_3).x;
                y = myPoint.get(RemoteTransforService.KEY_3).y;
                Log.d(TAG,"tap3 add friend x = " + x + ",y = " + y);
                Utils.execCommand("input tap " + x + " " + y,true);
            }else{
                Log.e(TAG,"3 not ZFB_MAIN");
            }

            if(waitForActivity(ZFB_ADD_FRIEND,6)){
                //点击4：输入框
                Thread.currentThread().sleep(800);
                x = myPoint.get(RemoteTransforService.KEY_4).x;
                y = myPoint.get(RemoteTransforService.KEY_4).y;
                Log.d(TAG,"tap4 input phone x = " + x + ",y = " + y);
                Utils.execCommand("input tap " + x + " " + y,true);
                Thread.currentThread().sleep(1000);
                //输入号码
                Log.d(TAG,"input text " + phone);
                Utils.execCommand("input text " + phone,true);
                Thread.currentThread().sleep(800);
                Utils.execCommand("input keyevent 66 ",true);
            }else{
                Log.e(TAG,"4 not ZFB_ADD_FRIEND");
            }

            if(waitForActivity(ZFB_FRIEND,6)) {
                //点击5：转账
                Thread.currentThread().sleep(2000);
                x = myPoint.get(RemoteTransforService.KEY_5).x;
                y = myPoint.get(RemoteTransforService.KEY_5).y;
                Log.d(TAG,"tap5 transfor x = " + x + ",y = " + y);
                Utils.execCommand("input tap " + x + " " + y,true);
            }else{
                Log.e(TAG,"5 not ZFB_FRIEND,return to ZFB_MAIN");
                Thread.currentThread().sleep(1500);
                Utils.execCommand("input keyevent 4 ",true);
                Thread.currentThread().sleep(1500);
                Utils.execCommand("input keyevent 4 ",true);
                Thread.currentThread().sleep(1500);
                Utils.execCommand("input keyevent 4 ",true);
                Thread.currentThread().sleep(1500);
                return;
            }
            if(waitForActivity(ZFB_FRIEND,2)) {
                //点击5：转账
                Thread.currentThread().sleep(2000);
                x = myPoint.get(RemoteTransforService.KEY_5).x;
                y = myPoint.get(RemoteTransforService.KEY_5).y + 68;
                Log.d(TAG,"tap5 transfor x = " + x + ",y = " + y);
                Utils.execCommand("input tap " + x + " " + y,true);
            }
            if(waitForActivity(ZFB_FRIEND,2)) {
                //点击5：转账
                Thread.currentThread().sleep(2000);
                x = myPoint.get(RemoteTransforService.KEY_5).x;
                y = myPoint.get(RemoteTransforService.KEY_5).y - 68;
                Log.d(TAG,"tap5 transfor x = " + x + ",y = " + y);
                Utils.execCommand("input tap " + x + " " + y,true);
            }

            //判断是否在转账界面
            if(waitForActivity(ZFB_TRANSFOR,6)) {
                //输入转账金额
                Log.d(TAG,"input text 0.01");
                Thread.currentThread().sleep(800);
                Utils.execCommand("input text " + 0.01,true);
                Thread.currentThread().sleep(1000);
                Utils.execCommand("input keyevent 66 ",true);
                Thread.currentThread().sleep(1000);
                //输入备注
                Log.d(TAG,"input text hello");
                Utils.execCommand("input text " + "hello",true);
                Thread.currentThread().sleep(1000);
                Utils.execCommand("input keyevent 4 ",true);
                Thread.currentThread().sleep(1000);
                //点击6:确认转账
                x = myPoint.get(RemoteTransforService.KEY_6).x;
                y = myPoint.get(RemoteTransforService.KEY_6).y;
                Log.d(TAG,"tap6 sure transfor x = " + x + ",y = " + y);
                Utils.execCommand("input tap " + x + " " + y,true);
                Thread.currentThread().sleep(3000);
            }else{
                Log.e(TAG,"6 not ZFB_TRANSFOR");
            }

            //确认转账后，弹出输入用户名时，返回到之前的操作
            if(waitForActivity(ZFB_TRANSFOR,6)) {
                Log.d(TAG,"需要输入用户名，点击取消，返回主页");
                Utils.execCommand("input keyevent 4 ",true);
                Thread.currentThread().sleep(1000);
                x = myPoint.get(RemoteTransforService.KEY_USER_NAME).x;
                y = myPoint.get(RemoteTransforService.KEY_USER_NAME).y;
                Log.d(TAG,"tap6 sure transfor x = " + x + ",y = " + y);
                Utils.execCommand("input tap " + x + " " + y,true);

                Thread.currentThread().sleep(1000);
                Utils.execCommand("input keyevent 4 ",true);
                Thread.currentThread().sleep(1000);
                Utils.execCommand("input keyevent 4 ",true);
                Thread.currentThread().sleep(1000);
                Utils.execCommand("input keyevent 4 ",true);
                Thread.currentThread().sleep(1000);
                Utils.execCommand("input keyevent 4 ",true);
            }

            //确认转账后，弹出你已经被拉黑
            if(waitForActivity(ZFB_TRANSFOR,6)) {
                Log.d(TAG,"被拉黑，点击确定，返回主页");
                x = myPoint.get(RemoteTransforService.KEY_LAHEI).x;
                y = myPoint.get(RemoteTransforService.KEY_LAHEI).y;
                Log.d(TAG,"tap6 sure transfor x = " + x + ",y = " + y);
                Utils.execCommand("input tap " + x + " " + y,true);

                Thread.currentThread().sleep(1000);
                Utils.execCommand("input keyevent 4 ",true);
                Thread.currentThread().sleep(1000);
                Utils.execCommand("input keyevent 4 ",true);
                Thread.currentThread().sleep(1000);
                Utils.execCommand("input keyevent 4 ",true);
                Thread.currentThread().sleep(1000);
                Utils.execCommand("input keyevent 4 ",true);
                return;
            }


            if(waitForActivity(ZFB_TRANSFOR_SUCCES,6)){
                //点击7:完成
                Thread.currentThread().sleep(800);
                x = myPoint.get(RemoteTransforService.KEY_7).x;
                y = myPoint.get(RemoteTransforService.KEY_7).y;
                Log.d(TAG,"tap7 complement x = " + x + ",y = " + y);
                Utils.execCommand("input tap " + x + " " + y,true);
            }else{
                Log.e(TAG,"7 not ZFB_TRANSFOR_SUCCES");
            }

            if(waitForActivity(ZFB_ENTER_PERSON,6)){
                Thread.currentThread().sleep(1500);
                Utils.execCommand("input keyevent 4 ",true);
                Log.d(TAG,"end");
            }else{
                Log.e(TAG,"8 not ZFB_ENTER_PERSON");
            }

        }catch (Exception e){
            Log.e(TAG,"Exception description = " + e.getMessage() + ",e = " + e);
        }
    }

    private boolean waitForActivity(String activity,int count) throws Exception{
        boolean result = false;
        while(count > 0){
            //Utils.sleep(500);
            Thread.currentThread().sleep(500);
            if(isPause){
                return false;
            }
            if(Utils.getTopActivityInfo(mContext).topActivityName.contains(activity)){
                result = true;
                break;
            }
            count--;
        }
        return result;
    }
}
