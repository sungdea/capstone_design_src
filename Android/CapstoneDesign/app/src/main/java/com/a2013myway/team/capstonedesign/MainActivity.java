/*
18.03.17
modified by LSH
 */

package com.a2013myway.team.capstonedesign;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Locale;


/*
USE
TextToSpeech 객체를 생성하고 밑과 같이 동적할당 해준 뒤에
speak 메서드를 이용하면 된다.
 */

public class MainActivity extends AppCompatActivity{

    private TextToSpeech textToSpeech;
    private BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String utteranceId=this.hashCode() + "";
                textToSpeech.speak("test".toString(),TextToSpeech.QUEUE_FLUSH,null,utteranceId);
            }
        });
    }
}