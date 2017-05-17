package com.smile.org.crazytransfor.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smile.org.crazytransfor.model.PointData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/5/14 0014.
 */

public class DataHelper {
    public static String TAG = "DataHelper";
    // 数据库名称
    private static String DB_NAME = "coodinate.db";
    // 数据库版本
    private static int DB_VERSION = 2;
    private SQLiteDatabase db;
    private SqliteHelper dbHelper;

    public DataHelper(Context context) {
        dbHelper = new SqliteHelper(context, DB_NAME, null, DB_VERSION );
        db = dbHelper.getWritableDatabase();
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
        Log.d(TAG,"GetCoodinateList table count =  " + cursor.getCount());
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
        Log.d(TAG, "SaveUserInfo row = " + row);
        return row;
    }

    // 删除users表的记录
    public int delCoodinate() {
        int id = db.delete(SqliteHelper.TB_NAME, null,null);
        Log.d(TAG, id + "");
        return id;
    }
}
