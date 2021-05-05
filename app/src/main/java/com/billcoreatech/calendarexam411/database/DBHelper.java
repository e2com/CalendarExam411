package com.billcoreatech.calendarexam411.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ImageData" ;
    private static final int DB_Ver = 1 ; //
    String TAG = DB_NAME + ":";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_Ver);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        StringBuffer sb = new StringBuffer();
        sb.append("create table imageData (                    ");
        sb.append("   _id integer primary key autoincrement, ");
        sb.append("   mdate text,                            ");
        sb.append("   msg   text,                            ");
        sb.append("   image blob                             ");
        sb.append(" )                                        ");

        db.execSQL(sb.toString());

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
