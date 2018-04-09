/*
18.03.10
written by LSH
 */

package com.a2013myway.team.capstonedesign;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by user on 2018-03-10.
 */

/*
사용시
DBHelper dbHelper = new DBHelper(getApplicationContext());
SQLiteDatabase database = dbHelper.getReadableDatabase(); 로 이용
 */



public class DBHelper extends SQLiteOpenHelper {

    public static final String ROOT_DIR = "/data/data/com.a2013myway.team.capstonedesign/databases/";
    private static final String DATABASE_NAME = "capstoneDB.db";
    private static final int SCHEMA_VERSION = 1;


    //관리할 DB이름과 버전 정보를 받음
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
        try{
            boolean bResult = isCheckDB(context);
            if(bResult)
            {
                copyDB(context);
            }
            else
            {

            }
        }catch (Exception e){}
    }

    public boolean isCheckDB(Context context)
    {
        File file = new File(ROOT_DIR+DATABASE_NAME);
        if(file.exists())
            return true;
        else
            return false;
    }

    private void copyDB(Context context) {
        AssetManager assetManager = context.getAssets();
        File folder = new File(ROOT_DIR);
        File file = new File(ROOT_DIR+DATABASE_NAME);

        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;

        try
        {
            InputStream inputStream = assetManager.open("db/"+DATABASE_NAME);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            if(folder.exists())
            {

            }
            else
            {
                folder.mkdirs();
            }

            if(file.exists()){
                file.delete();
                file.createNewFile();
            }

            fileOutputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            int read = -1;
            byte[] buffer = new byte[1024];
            while((read = bufferedInputStream.read(buffer,0,1024))!=-1){
                bufferedOutputStream.write(buffer,0,read);
            }

            bufferedOutputStream.flush();

            bufferedOutputStream.close();
            fileOutputStream.close();
            bufferedInputStream.close();
            inputStream.close();
        }catch (IOException e){}
    }

    //DB를 새로 생성할 때 호출
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    //DB업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
