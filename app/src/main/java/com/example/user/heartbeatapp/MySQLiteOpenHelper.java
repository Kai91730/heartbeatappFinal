package com.example.user.heartbeatapp;

/**
 * Created by user on 2017/9/6.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "TravelSpots";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "Spot";
    private static final String COL_id = "id";
    private static final String COL_name = "name";
    private static final String COL_web = "web";
    private static final String COL_phone = "phone";
    private static final String COL_address = "address";
    private static final String COL_image = "image";
    //將id欄位設定為自動編號
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " ( " +
                    COL_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_name + " TEXT NOT NULL, " +
                    COL_web + " TEXT, " +
                    COL_phone + " TEXT, " +
                    COL_address + " TEXT, " +
                    COL_image + " BLOB ); ";
    //指定之後要連結DB_NAME代表資料庫
    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    //資料庫被更新時會呼叫此方法，此時刪除既存的資料表並呼叫onCreate()重建該資料表
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }
    //資料庫被更新時會呼叫此方法，此時刪除既存的資料表並呼叫onCreate()重建該資料表
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    //取得所有資訊
    public List<Spot> getAllSpots() {
        SQLiteDatabase db = getReadableDatabase();
        //欲查詢的欄位
        String[] columns = {
                COL_id, COL_name, COL_web, COL_phone, COL_address, COL_image
        };
        //呼叫query()需要傳遞多個參數，每個參數都代表SQL查詢語法的一部份，null代表略過該部分
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null,
                null);


//        String sql = "SELECT * FROM Spot;";
//        String[] args = {};
//        Cursor cursor = db.rawQuery(sql, args);
        //建立List準備儲存符合條件的Spot物件(景點)
        List<Spot> spotList = new ArrayList<>();
        //將Cursor指標不斷向下一筆動，直到沒有資料為止
        while (cursor.moveToNext()) {
            //依照類型呼叫對應的getter方法，並指定欄位索引，已取得各個欄位的值
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String web = cursor.getString(2);
            String phone = cursor.getString(3);
            String address = cursor.getString(4);
            //圖檔儲存的資料為Blob,所以取出時呼叫getBlob()
            byte[] image = cursor.getBlob(5);
            //呼叫Spot建構式並取得的值傳入，建立Spot物件並儲存在List內
            Spot spot = new Spot(id, name, web, phone, address, image);
            spotList.add(spot);
        }
        cursor.close();
        return spotList;
    }

    public Spot findById(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] columns = {
                COL_name, COL_web, COL_phone, COL_address, COL_image
        };
        String selection = COL_id + " = ?;";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs,
                null, null, null);
        Spot spot = null;
        if (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String web = cursor.getString(1);
            String phone = cursor.getString(2);
            String address = cursor.getString(3);
            byte[] image = cursor.getBlob(4);
            spot = new Spot(id, name, web, phone, address, image);
        }
        cursor.close();
        return spot;
    }

    public long insert(Spot spot) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_name, spot.getName());
        values.put(COL_web, spot.getWeb());
        values.put(COL_phone, spot.getPhone());
        values.put(COL_address, spot.getAddress());
        values.put(COL_image, spot.getImage());
        return db.insert(TABLE_NAME, null, values);
    }

    public int update(Spot spot) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_name, spot.getName());
        values.put(COL_web, spot.getWeb());
        values.put(COL_phone, spot.getPhone());
        values.put(COL_address, spot.getAddress());
        values.put(COL_image, spot.getImage());
        String whereClause = COL_id + " = ?;";
        String[] whereArgs = {Integer.toString(spot.getId())};
        return db.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    public int deleteById(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COL_id + " = ?;";
        String[] whereArgs = {String.valueOf(id)};
        return db.delete(TABLE_NAME, whereClause, whereArgs);
    }
}