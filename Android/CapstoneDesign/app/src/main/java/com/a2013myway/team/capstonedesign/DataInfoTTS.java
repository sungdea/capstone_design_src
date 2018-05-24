package com.a2013myway.team.capstonedesign;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DataInfoTTS{
    private final int LINEAR_BLOCK = 1;
    private final int STOP_BLOCK = 0;
    private final int ERROR = 2;

    private int stopTime;
    private int linearTime;

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private TTS tts;

    private String tagIndex;

    //일정 시간 이내에 정지블록을 touch 하였었는가?
    private boolean isTouchedStop;

    private TimerTask mTask;
    private Timer timer;

    public DataInfoTTS(Context context){
        dbHelper = new DBHelper(context);
        database = dbHelper.getReadableDatabase();
        tts=new TTS(context, Locale.KOREAN);
        isTouchedStop = false;
        timer = new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                isTouchedStop = false;
                Log.d("touch여부",isTouchedStop+"");
            }
        };
    }

    public void run(String id){

        int type;

        Cursor StopBlockCursor = database.rawQuery("select * from `StopBlock` where tagNum = '"+id+"'",null);
        Cursor LinearBlockCursor = database.rawQuery("select * from `LinearBlock` where tagNum = '"+id+"'",null);

        if(StopBlockCursor.getCount()!=0)
        {
            Log.d("LinearBlock","진입");

            type = STOP_BLOCK;
        }
        else if(LinearBlockCursor.getCount() != 0)
        {
            Log.d("StopBlock","진입");
            type = LINEAR_BLOCK;
        }
        else
        {
            type = ERROR;
        }

        String location;

        switch (type)
        {
            case STOP_BLOCK:
                StopBlockCursor.moveToNext();
                location = StopBlockCursor.getString(2);
                tts.stop();
                tts.speak(location);
                isTouchedStop = true;
                stopTime = (int)System.currentTimeMillis();

                tagIndex = StopBlockCursor.getString(0);
                Log.d("touch여뷰",isTouchedStop+"");
                break;
            case LINEAR_BLOCK:
                LinearBlockCursor.moveToNext();
                Log.d("리니어 블록에서 touch",isTouchedStop+"");
                location = LinearBlockCursor.getString(3);
                String stopTagNum = LinearBlockCursor.getString(2);

                boolean isclosed = tagIndex.equals(stopTagNum);
                Log.d("stoptagnum",stopTagNum);
                Log.d("tagindex",tagIndex);

                linearTime = (int)System.currentTimeMillis();
                int deviation = linearTime-stopTime;
                Log.d("시간 차이",deviation+"");
                if(deviation<15000 && isclosed)
                {
                    stopTime = linearTime;
                    tts.stop();
                    tts.speak(location);
                }
                else
                {
                    tts.stop();
                    tts.speak("정지블록을 먼저 태그해 주세요.");
                }
                break;
            default:
                tts.stop();
                tts.speak("태그를 다시 인식하여 주세요.");
                break;
        }

    }
}