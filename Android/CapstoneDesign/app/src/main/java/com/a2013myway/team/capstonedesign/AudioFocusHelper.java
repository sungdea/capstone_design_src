package com.a2013myway.team.capstonedesign;

import android.media.AudioManager;

/**
 * Created by shotc on 2018-03-18.
 */

public class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {

    AudioManager manager;

    public AudioFocusHelper(AudioManager audioManager) {
        super();
        manager = audioManager;
    }

    private void abandonAudioFocus(){
        manager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch(focusChange){
            case AudioManager.AUDIOFOCUS_GAIN:
                //tts 구현
                abandonAudioFocus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //볼륨 줄이기
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                abandonAudioFocus();
                break;
        }
    }
}
