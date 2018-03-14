package com.a2013myway.team.capstonedesign;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class MainActivity extends AppCompatActivity implements OnInitListener {

    private TextToSpeech myTTS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

<<<<<<< HEAD
        myTTS = new TextToSpeech(this, this);
    }
    public void onInit(int status) {
        String myText1 = "one of my favorite acotors";
        String myText2 = "한글이 인식이 안됩니다!";
        myTTS.speak(myText1, TextToSpeech.QUEUE_FLUSH, null);
        myTTS.speak(myText2, TextToSpeech.QUEUE_ADD, null);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myTTS.shutdown();
=======

>>>>>>> e7db5ed6a429600717e492f7dd6ac77efe3474d5
    }
}
