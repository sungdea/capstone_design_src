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

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private TTS tts;
    private Cursor cursor;
    //일정 시간 이내에 정지블록을 touch 하였었는가?
    private boolean isTouchedStop;

    private TimerTask mTask;
    private Timer timer;

    public DataInfoTTS(Context context){
        dbHelper = new DBHelper(context);
        database = dbHelper.getReadableDatabase();
        tts=new TTS(context, Locale.KOREAN);
        isTouchedStop = false;
    }

    public void run(String id){

        int type;

        Cursor StopBlockCursor = database.rawQuery("select * from `StopBlock` where tagNum = '"+id+"'",null);
        Cursor LinearBlockCursor = database.rawQuery("select * from `LinearBlock` where tagNum = '"+id+"'",null);

        if(StopBlockCursor.getCount() == 0)
        {
            type = LINEAR_BLOCK;
        }
        else if(LinearBlockCursor.getCount() == 0)
        {
            type = STOP_BLOCK;
        }
        else
        {
            type = ERROR;
        }

        String stop_info;
        String line_info;

        switch (type)
        {
            case STOP_BLOCK:
                StopBlockCursor.moveToFirst();
                stop_info = StopBlockCursor.getString(2);
                Log.d("stop_info",stop_info);
                tts.stop();
                tts.speak(stop_info);
                isTouchedStop = true;
                //statusChange(false);
                break;
            case LINEAR_BLOCK:
                if(isTouchedStop)
                {
                    LinearBlockCursor.moveToFirst();
                    line_info = LinearBlockCursor.getString(3);
                    Log.d("line_info",line_info);
                    tts.stop();
                    tts.speak(line_info);
                    //statusChange(false);
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
    private void statusChange(final boolean touchedStop)
    {
        mTask.cancel();

        timer = new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                isTouchedStop = touchedStop;

            }
        };

        timer.schedule(mTask,15000);
    }
}