/*
18.03.17
modified by LSH
 */

package com.a2013myway.team.capstonedesign;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Locale;


/*
USE
TextToSpeech 객체를 생성하고 밑과 같이 동적할당 해준 뒤에
speak 메서드를 이용하면 된다.
 */

public class MainActivity extends AppCompatActivity {

    //PERMISSION REQUEST CODE
    private static final int PERMISSION_REQUEST = 1;

    private BluetoothAdapter bluetoothAdapter;
    private final int ENABLE_BT = 1;
    private TTS tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,ENABLE_BT);
        }

        Button button = (Button)findViewById(R.id.btn_connect);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });

        //PERMISSION CHECK
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("권한이 필요합니다.").setMessage("앱을 이용하기 위해서 권한에 동의해주세요.").setPositiveButton(android.R.string.ok,null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST);
                    }
                });
                builder.show();
            }
        }
        //tts speak example
        tts=new TTS(this,Locale.KOREAN);
        Button tts_btn=(Button)findViewById(R.id.speak_btn);
        tts_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak("안녕하세요");
            }
        });

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST)
        {
            if(grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                Toast.makeText(getApplicationContext(),"권한에 동의해주시지 않으면 원활한 앱 실행이 불가능합니다.",Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ENABLE_BT && resultCode== Activity.RESULT_OK)
        {
            Toast.makeText(getApplicationContext(),"블루투스가 활성화 되었습니다.",Toast.LENGTH_SHORT).show();
        }
        else if(requestCode==ENABLE_BT&&resultCode==Activity.RESULT_CANCELED)
        {
            Toast.makeText(getApplicationContext(),"앱을 사용하기 위해서는 블루투스 기능이 활성화되어야 합니다.",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}