package com.a2013myway.team.capstonedesign;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by sonseongbin on 2017. 3. 19..
 */

public class TTS extends UtteranceProgressListener implements TextToSpeech.OnInitListener{

    private final String TAG = TTS.class.getSimpleName();

    private TextToSpeech textToSpeech;
    private Locale locale;
    private Context context;
    MediaPlayer mp;

    public TTS(Context context, Locale locale) {
        this.locale = locale;
        this.context=context;
        textToSpeech = new TextToSpeech(context, this);
        textToSpeech.setOnUtteranceProgressListener(this);
    }


    public void speak(String text) {
        TTSFocus(context);
        if(textToSpeech != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String myUtteranceID = "myUtteranceID";
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, myUtteranceID);
            }
            else {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "myUtteranceID");
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, hashMap);
            }
        }
    }

    public void stop() {
        textToSpeech.stop();
    }

    public void shutdown() {
        textToSpeech.shutdown();
    }

    public boolean isSpeaking() {
        return textToSpeech.isSpeaking();
    }

    @Override
    public void onStart(String utteranceId) {
        Log.d(TAG, "onStart / utteranceID = " + utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        Log.d(TAG, "onDone / utteranceID = " + utteranceId);
    }

    @Override
    public void onError(String utteranceId) {
        Log.d(TAG, "onError / utteranceID = " + utteranceId);
    }

    @Override
    public void onInit(int status) {
        if(status != TextToSpeech.ERROR)
            textToSpeech.setLanguage(locale);
    }

    private int TTSFocus(Context context)
    {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int focusRequest = audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
                switch (i)
                {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        mp.pause();
                        //오디오 포커스 체인지 리스너 구현
                }
            }
        },AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);

        return focusRequest;
        //AudioManager.AUDIOFOCUS_REQUEST_FAILED = 0
        //허가는 1
        //코드에 따라 tts음성 출력 구분할 것
    }
}


