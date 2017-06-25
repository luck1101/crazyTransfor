package com.smile.org.crazytransfor.model;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import com.smile.org.crazytransfor.MainActivity;
import com.smile.org.crazytransfor.MyApplication;
import com.smile.org.crazytransfor.module.log.L;

/**
 * Created by Smile on 2017/5/31.
 */

public class SharePreferenceUtil {
    /*public static String KEY_POSITION = "position";
    public static SharePreferenceUtil mInstance = null;
    SharedPreferences mSharedPreferences;
    public static int DEFAULT_INT = 0;
    public static SharePreferenceUtil getInstance(){
        if(mInstance == null){
            mInstance = new SharePreferenceUtil(MyApplication.getInstance());
        }
        return mInstance;
    }

    private SharePreferenceUtil(Context c){
        mSharedPreferences= c.getSharedPreferences("data", Activity.MODE_PRIVATE);
    }

    public void save(String key,int value){
        //实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key,value);
        //提交当前数据
        editor.apply();
        editor.commit();
    }

    public int getIntValue(String key){
        int value = mSharedPreferences.getInt(key,DEFAULT_INT);
        return value;
    }

    public void autoAdd(String key){
        int value = mSharedPreferences.getInt(key,DEFAULT_INT);
        value++;
        save(key,value);
    }*/

    public static void createCurrentPosition(Context context){
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://com.test.provider/person"), null, null, null, null);
        if(cursor == null || cursor.getCount() == 0){
            L.d("createCurrentPosition position == 0");
            ContentValues values = new ContentValues();
            values.put("name", "position");
            values.put("position", 0);
            Uri uri = resolver.insert(Uri.parse("content://com.test.provider/person"), values);
            L.d(uri.toString());
        }else{
            L.d("createCurrentPosition have data");
        }
    }

    public static int queryCurrentPosition(Context context){
        int position = 0;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://com.test.provider/person"), null, null, null, null);
        if(cursor != null && cursor.moveToNext()){
            position = cursor.getInt(cursor.getColumnIndex("position"));
        }
        cursor.close();
        return position;
    }

    public static void updateCurrentPosition(Context context,int p){
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("name", "position");
        values.put("position",p);
        int type = resolver.update(Uri.parse("content://com.test.provider/person"), values, null, null);
        L.d("updateCurrentPosition type = "+type);
    }
}
