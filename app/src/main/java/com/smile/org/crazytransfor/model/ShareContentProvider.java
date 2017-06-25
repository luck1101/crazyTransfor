package com.smile.org.crazytransfor.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Administrator on 2017/6/25 0025.
 */

public class ShareContentProvider extends ContentProvider {
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PERSON_CODE = 1 ;
    private static final int PERSON_CODES = 2 ;

    static{
        URI_MATCHER.addURI("com.test.provider", "person", PERSON_CODES);
        URI_MATCHER.addURI("com.test.provider", "person/#", PERSON_CODE);
    }

    PositionHelper dbHelper;

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        dbHelper = new PositionHelper(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        switch (URI_MATCHER.match(uri)) {
            case PERSON_CODE:
                long id = ContentUris.parseId(uri);
                String where = " _id = "+id;
                if(selection != null){
                    where +=  " and "+selection;
                }
                cursor = db.query("person", null, where, selectionArgs, null, null, sortOrder);
                break;
            case PERSON_CODES:
                cursor = db.query("person", null, null, null, null, null, null);
                break;
            default:
                break;
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        String type = "";
        switch (URI_MATCHER.match(uri)) {
            case PERSON_CODE:
                type = "vnd.android.cursor.item/person";
                break;
            case PERSON_CODES:
                type = "vnd.android.cursor.dir/person";
                break;
            default:
                break;
        }
        return type;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert("person", null, values);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int type = 0;
        switch (URI_MATCHER.match(uri)) {
            case PERSON_CODE:
                long id = ContentUris.parseId(uri);
                String where = " _id ="+id;
                if(selection != null){
                    where += " and "+selection;
                }
                type = db.delete("person", where, selectionArgs);
                break;
            case PERSON_CODES:
                type = db.delete("person", null, null);
                break;
            default:
                break;
        }
        return type;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int type = 0;
        switch (URI_MATCHER.match(uri)) {
            case PERSON_CODE:
                long id = ContentUris.parseId(uri);
                String where = " _id ="+id;
                if(selection != null){
                    where += " and "+selection;
                }
                type = db.update("person", values, selection, selectionArgs);
                break;
            case PERSON_CODES:
                type = db.update("person", values, null, null);
                break;
            default:
                break;
        }
        return type;
    }

}
