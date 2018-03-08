package com.a2013myway.team.capstonedesign;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DetectService extends Service {
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

    @Override
    public void onCreate() {
        super.onCreate();
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("Capstone Design");
        builder.setContentText("태그 감지 상태 입니다.");
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);

        //NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.notify(2013,builder.build());
        startForeground(2013,builder.build());
        //노티피케이션을 없앨수 없기 위하여 startForeground
    }
}
