package com.sid.soundrecorderutils.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DiaryDataBaseManager {
    private static final String TAG = DiaryDataBaseManager.class.getSimpleName();
    private volatile DiaryDataBaseManager instance;

    private static DiaryDatabaseHelper helper;
    private SQLiteDatabase db;
//    private static Integer diaryNum;

    public DiaryDataBaseManager(Context context) {
        if(helper == null)
            helper = new DiaryDatabaseHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
//        diaryNum = 0;
    }

//    public static DiaryDataBaseManager getInstance() {
//        if (instance == null) {
//            synchronized (DiaryDataBaseManager.class) {
//                if (instance == null) {
//                    instance = new DiaryDataBaseManager(this);
//                }
//            }
//        }
//        return instance;
//    }
    /**
     * insert diary
     * @param diary
     */
    public void insert(Diary diary) {
        db.beginTransaction();	//开始事务
        try {
            db.execSQL("INSERT INTO Diary VALUES(null, ?, ?, ?, ?, ?)", new Object[]{diary.date, diary.time, diary.isImage, diary.discription, diary.hashcode});
            db.setTransactionSuccessful();	//设置事务成功完成
        } finally {
            db.endTransaction();	//结束事务
        }
    }
    /**
     * insert diary
     * @param date,time,isImage
     */
    public void insert(String date, String time, int isImage) {
        db.beginTransaction();	//开始事务
        try {
            db.execSQL("INSERT INTO Diary VALUES(null, ?, ?, ?, null,null)", new Object[]{date, time, isImage});
            db.setTransactionSuccessful();	//设置事务成功完成
            Log.d(TAG, "insert: " + date + "/" + time + "/" + isImage + ".");
            queryDiaryCursor(date, time);
        } finally {
            db.endTransaction();	//结束事务
        }
    }

    /**
     * update a diary's discription
     * @param updatedDiary
     */
    public void update(Diary updatedDiary) {
        Cursor c = queryDiaryCursor(updatedDiary);
        Log.d(TAG, "update: " + updatedDiary.date + "/" + updatedDiary.time + "/" + updatedDiary.isImage);
        int diary_id = -1;
        if (c.moveToNext()) {
            diary_id = c.getInt(c.getColumnIndex("_id"));
        }
        c.close();
        if(diary_id == -1) {
            Log.d(TAG, "update: updateddiary cannot be found in diary.db");
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put("discription", updatedDiary.discription);
        cv.put("hashcode", updatedDiary.hashcode);
        Log.d(TAG, "update " + " to " + updatedDiary.toString());
        db.update("Diary", cv, "_id = ?", new String[]{String.valueOf(diary_id)});
//        db.update("Diary", cv, "title = ?", new String[]{startUpdateDiary.title});
    }

    /**
     * delete diary
     * @param _id
     */
    public void deleteDiary(Integer _id) {
        db.delete("Diary", "_id = ?", new String[]{String.valueOf(_id)});
    }

    /**
     * delete diary
     * @param diary
     */
    public void deleteDiary(Diary diary) {
        db.delete("Diary", "_id = ?", new String[]{String.valueOf(diary._id)});
    }

//    /**
//     * query a diary, return Diary
//     * @param diaryDate
//     * @return
//     */
//    public Diary queryDiary(String diaryDate) {
//        Diary diary = new Diary();
//        Cursor c = queryDiaryCursor(diaryDate);
//        while (c.moveToNext()) {
//            diary._id = c.getInt(c.getColumnIndex("_id"));
//            diary.date = c.getString(c.getColumnIndex("date"));
//            diary.title = c.getString(c.getColumnIndex("title"));
//            diary.content = c.getString(c.getColumnIndex("content"));
//        }
//        c.close();
//        Log.d(TAG, "query: finished");
//        return diary;
//    }
    /**
     * query a diary (without _id), return Diary
     * @param startQueryDiary
     * @return
     */
    public Diary queryDiary(Diary startQueryDiary) {
        Log.d(TAG, "queryDiary: getQueryDiary");
        Log.d(TAG, "queryDiary: to query: " + startQueryDiary.date + " " + startQueryDiary.time + " " + startQueryDiary.isImage);
        Cursor c = queryDiaryCursor(startQueryDiary);
//        if(c.getPosition() == -1) {
//            Log.d(TAG, "queryDiary: cannot find " + startQueryDiary.toString());
//            return null;
//        }
        Diary diary = new Diary();
        if (c.moveToNext()) {
            diary._id = c.getInt(c.getColumnIndex("_id"));
            diary.date = c.getString(c.getColumnIndex("date"));
            diary.time = c.getString(c.getColumnIndex("time"));
            diary.isImage = c.getInt(c.getColumnIndex("isImage"));
            diary.discription = c.getString(c.getColumnIndex("discription"));
            diary.hashcode = c.getString(c.getColumnIndex("hashcode"));
        }
        c.close();
        Log.d(TAG, "queryDiary: returns: " + diary.date + " " + diary.time + " " + diary.isImage + " " + diary.discription + " " + diary.hashcode);
        return diary;
    }

//    /**
//     * query all diaries, return list
//     * @return List<Diary>
//     */
//    public List<Diary> query() {
//        ArrayList<Diary> diaries = new ArrayList<Diary>();
//        Cursor c = queryTheCursor();
//        while (c.moveToNext()) {
//            Log.d(TAG, "query: position: " + c.getPosition());
//            Diary diary = new Diary();
//            diary._id = c.getInt(c.getColumnIndex("_id"));
//            diary.date = c.getString(c.getColumnIndex("date"));
//            diary.title = c.getString(c.getColumnIndex("title"));
//            diary.content = c.getString(c.getColumnIndex("content"));
//            diaries.add(diary);
//        }
//        c.close();
//        Log.d(TAG, "query: finished!!");
//        return diaries;
//    }

//    /**
//     * query all diaries, return cursor
//     * @return	Cursor
//     */
//    public Cursor queryTheCursor() {
//        Cursor c = db.rawQuery("SELECT * FROM Diary", null);
//        Log.d(TAG, "queryTheCursor: position: " + c.getPosition());
//        Log.d(TAG, "queryTheCursor: count:" + c.getCount());
//        return c;
//    }

    /**
     * query a diary, return cursor
     * @param diaryDate
     * @return	Cursor
     */
    public Cursor queryDiaryCursor(String diaryDate, String diaryTime) {
        Cursor c = db.query("Diary",new String[] { "_id","date","time","isImage", "discription", "hashcode" },"date = ? AND time = ?", new String[]{diaryDate, diaryTime},null,null,null, null);
        Log.d(TAG, "queryDiaryCursor: " + diaryDate + "/" + diaryTime + ".");
        Log.d(TAG, "queryDiaryCursor: position: " + c.getPosition());
        Log.d(TAG, "queryDiaryCursor: count:" + c.getCount());
        return c;
    }
    /**
     * query a diary, return cursor
     * @param diary
     * @return	Cursor
     */
    public Cursor queryDiaryCursor(Diary diary) {
        Cursor c = db.query("Diary",new String[] { "_id","date","time","isImage", "discription", "hashcode"  },"date = ? AND time = ?", new String[]{diary.date,diary.time},null,null,null, null);
        Log.d(TAG, "queryDiaryCursor: " + diary.date + "/" + diary.time + ".");
        Log.d(TAG, "queryDiaryCursor: position: " + c.getPosition());
        Log.d(TAG, "queryDiaryCursor: count:" + c.getCount());
        return c;
    }

    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }
}

