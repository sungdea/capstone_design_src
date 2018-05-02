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
        String distance;

        switch (type)
        {
            case STOP_BLOCK:
                StopBlockCursor.moveToNext();
                location = StopBlockCursor.getString(2);
                tts.stop();
                tts.speak("현재 위치는 "+location+"입니다.");
                isTouchedStop = true;
                timer.cancel();
                mTask = new TimerTask() {
                    @Override
                    public void run() {
                        isTouchedStop = false;
                        Log.d("touch여부",isTouchedStop+"");
                    }
                };
                timer.schedule(mTask,15000);
                Log.d("touch여뷰",isTouchedStop+"");
                break;
            case LINEAR_BLOCK:
                LinearBlockCursor.moveToNext();
                Log.d("리니어 블록에서 touch",isTouchedStop+"");
                if(isTouchedStop)
                {
                    //Cursor LinearBlockInfoCursor = database.rawQuery("select * from LinearBlockInfo where tagNum = '"+id+"'",null);
                    location = LinearBlockCursor.getString(3);
                    //distance = LinearBlockInfoCursor.getString(2);
                    tts.stop();
                    //tts.speak("해당 방향으로 "+distance+"미터 앞에"+location+" 위치해 있습니다.");
                    tts.speak("해당 방향으로 " +location +"이 있습니다.");

                    timer.cancel();
                    mTask = new TimerTask() {
                        @Override
                        public void run() {
                            isTouchedStop = false;
                            Log.d("touch여부",isTouchedStop+"");
                        }
                    };
                    timer.schedule(mTask,15000);
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