package com.billcoreatech.calendarexam411.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.billcoreatech.calendarexam411.util.ImageDataBean;

import java.io.ByteArrayOutputStream;

public class DBHandler {

    DBHelper helper ;
    SQLiteDatabase db ;
    String tableName = "ImageData" ;

    String TAG = "ImageData" ;

    public DBHandler(Context context) {
        helper = new DBHelper(context) ;
        db = helper.getWritableDatabase() ;
    }

    public static DBHandler open (Context ctx) throws SQLException {
        DBHandler handler = new DBHandler(ctx) ;
        return handler ;
    }

    public void close() {
        helper.close();
    }

    public Cursor selectAll() {
        StringBuffer sb = new StringBuffer();
        sb.append("select * from " + tableName + " ");
        sb.append("order by mdate desc   ");
        Cursor rs = db.rawQuery(sb.toString(), null) ;
        return rs ;
    }

    public long insertDayinfo(String mDate, String msg, byte[] image) {
        long _id = -1  ;
        ContentValues values = new ContentValues() ;
        values.put("mdate", mDate);
        values.put("msg", msg) ;
        values.put("image", image);
        _id = db.insert(tableName, null, values) ;
        Log.i(TAG, "insert " + _id + " " + image.toString()) ;
        return _id ;
    }

    public byte[] getByteArrayFromBitmap(Bitmap d) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        d.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray() ;
    }

    public Bitmap getAppBitmap(byte[] b) {
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    public ImageDataBean selectDate(String pDate) {
        ImageDataBean imageDataBean = new ImageDataBean();
        StringBuffer sb = new StringBuffer();
        sb.append("select mdate, msg, image from " + tableName + " ");
        sb.append(" where mdate = '" + pDate + "'  ");
        Log.i(TAG, "sql=" + sb.toString());
        Cursor rs = db.rawQuery(sb.toString(), null) ;
        if (rs.moveToNext()) {
            imageDataBean.setmDate(rs.getString(0));
            imageDataBean.setMsg(rs.getString(1));
            imageDataBean.setImage(rs.getBlob(2));
        }
        return imageDataBean ;
    }

    public void deleteOne(String pDate) {
        long _id = -1  ;
        _id = db.delete(tableName, "mdate = '" + pDate + "' ", null) ;
    }
}
