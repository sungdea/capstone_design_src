package com.a2013myway.team.capstonedesign;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Locale;

public class DataInfoTTS{

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private TTS tts;

    public DataInfoTTS(Context context){
        dbHelper = new DBHelper(context);
        database = dbHelper.getReadableDatabase();
        tts=new TTS(context, Locale.KOREAN);
    }

    public void run(String id){
        Cursor c=database.rawQuery("select info from `Block` where id = '"+id+"'",null);
        c.moveToNext();
        String info=c.getString(0);
        Log.d("info text : ",info);
        tts.stop();
        tts.speak(info);
    }
}