package com.a2013myway.team.capstonedesign;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.Locale;

public class voice_conversion implements OnInitListener {
    private TextToSpeech textToSpeech;

    public voice_conversion(Context context){
        textToSpeech = new TextToSpeech(context,this);
    }

    public void onInit(int status) {
        if(status != TextToSpeech.ERROR) {
            textToSpeech.setLanguage(Locale.KOREAN);
        }
    }

    public void speak(String text){
        String utteranceId=this.hashCode() + "";
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null,utteranceId);
    }

    public void onDestroy() {
        textToSpeech.shutdown();
    }
}