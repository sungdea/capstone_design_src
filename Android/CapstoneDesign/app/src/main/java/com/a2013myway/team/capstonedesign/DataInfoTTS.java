package com.a2013myway.team.capstonedesign;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Locale;

public class DataInfoTTS extends Activity {

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private TTS tts;
    String id="19003F54FE8C"; //example

    public DataInfoTTS(){
        dbHelper = new DBHelper(getApplicationContext());
        database = dbHelper.getReadableDatabase();
        tts=new TTS(getApplicationContext(), Locale.KOREAN);
    }

    public void run(String id){
        id=this.id;
        Cursor c=database.rawQuery("select info from Block where id= '"+id+"'",null);
        c.moveToNext();
        String info=c.getString(0);
        tts.speak(info);
    }
}
