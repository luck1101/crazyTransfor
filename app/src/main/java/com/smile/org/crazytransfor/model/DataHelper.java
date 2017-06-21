package com.smile.org.crazytransfor.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.smile.org.crazytransfor.module.log.L;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/5/14 0014.
 */

public class DataHelper {
    public static String TAG = "DataHelper";
    // 数据库名称
    private static String DB_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();//"/storage/sdcard0/temp/";
    private static String DB_NAME2 = "coodinate.db";
    // 数据库版本
    private static int DB_VERSION = 2;
    private SQLiteDatabase db;
    private SqliteHelper dbHelper;

    public DataHelper(Context context) {
        String db_file = DB_NAME2;
        if(isExistSDCard()){
            File crazy = new File(DB_DIR + File.separator + "crazy");
            if(!crazy.exists()){
                crazy.mkdir();
            }
            db_file = crazy + File.separator + DB_NAME2;
        }
        dbHelper = new SqliteHelper(context, db_file, null, DB_VERSION );
        db = dbHelper.getWritableDatabase();
    }

    private boolean isExistSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else{
            return false;
        }
    }

    public void close() {
        db.close();
        dbHelper.close();
    }

    public boolean isEmpty(){
        boolean result = false;
        Cursor cursor = db.query(SqliteHelper. TB_NAME, null, null , null, null, null, null);
        if(cursor == null || cursor.getCount() == 0){
            result = true;
        }else{
            result = false;
        }
        cursor.close();
        return result;
    }

    public HashMap<String,PointData> getCoodinateList() {
        HashMap<String,PointData> pointList = new HashMap<>();
        Cursor cursor = db.query(SqliteHelper. TB_NAME, null, null , null, null, null, null);
        cursor.moveToFirst();
        L.d( "GetCoodinateList table count =  " + cursor.getCount());
        while (!cursor.isAfterLast()) {
            PointData pointData = new PointData();
            pointData.key = cursor.getString(1);
            pointData.x = cursor.getInt(2);
            pointData.y = cursor.getInt(3);
            pointList.put(pointData.key,pointData);
            cursor.moveToNext();
        }
        cursor.close();
        return pointList;
    }

    // 添加users表的记录
    public Long saveCoodinate(PointData data) {
        ContentValues values = new ContentValues();
        values.put(PointData.KEY_NAME, data.key);
        values.put(PointData.KEY_X, data.x);
        values.put(PointData.KEY_Y, data.y);
        Long row = db.insert(SqliteHelper. TB_NAME, null, values);
        L.d(  "SaveUserInfo row = " + row);
        return row;
    }

    // 删除users表的记录
    public int delCoodinate() {
        int id = db.delete(SqliteHelper.TB_NAME, null,null);
        L.d(  id + "");
        return id;
    }
}
