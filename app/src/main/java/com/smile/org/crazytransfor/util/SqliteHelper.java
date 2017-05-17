package com.smile.org.crazytransfor.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Administrator on 2017/5/14 0014.
 */

public class SqliteHelper extends SQLiteOpenHelper {
    public static final String TB_NAME= "coodinatepoints_table";
    public SqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    //创建表
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( "CREATE TABLE IF NOT EXISTS "+
                TB_NAME+ "("+
                "id"+ " integer primary key,"+
                "key" + " text not null," +
                "x"+ " integer,"+
                "y"+ " integer"+
                ")"
        );
        Log.d(DataHelper.TAG ,"onCreate" );
    }
    //更新表
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL( "DROP TABLE IF EXISTS " + TB_NAME );
        onCreate(db);
        Log.e(DataHelper.TAG ,"onUpgrade" );
    }
    //更新列
    public void updateColumn(SQLiteDatabase db, String oldColumn, String newColumn, String typeColumn){
        try{
            db.execSQL( "ALTER TABLE " +
                    TB_NAME + " CHANGE " +
                    oldColumn + " "+ newColumn +
                    " " + typeColumn
            );
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
