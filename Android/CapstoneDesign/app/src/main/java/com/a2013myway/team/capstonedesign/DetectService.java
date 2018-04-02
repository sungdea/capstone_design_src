package com.a2013myway.team.capstonedesign;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.IBinder;

public class DetectService extends Service {

    public int isblutoothconnected = 0;
    // 0 연결x
    // 1 연결o

    private BluetoothAdapter bluetoothAdapter;

    public DetectService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        //startForeground();

        //throw new UnsupportedOperationException("Not yet implemented");

        //service 객체와 액티비티 사이에서 통신할 때 사용하는 메서드
        return null;
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
                        //오디오 포커스 체인지 리스너 구현
                }
            }
        },AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);

        return focusRequest;
        //AudioManager.AUDIOFOCUS_REQUEST_FAILED = 0
        //허가는 1
        //코드에 따라 tts음성 출력 구분할 것
    }
    @Override
    public void onCreate() {
        super.onCreate();

        //initialize bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("Capstone Design");
        builder.setContentText("태그 감지 상태 입니다.");
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);

        //NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.notify(2013,builder.build());
        startForeground(2013,builder.build());
        //노티피케이션을 없앨수 없기 위하여 startForeground

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
