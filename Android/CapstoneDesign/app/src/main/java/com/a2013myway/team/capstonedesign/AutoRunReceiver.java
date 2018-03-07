package com.a2013myway.team.capstonedesign;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoRunReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        // 핸드폰 부팅시 앱 자동실행 시키기
        String action = intent.getAction();
        if(action.equals("android.intent.action.BOOT_COMPLETED")){
            Intent boot_intent = new Intent(context,DetectService.class);
            boot_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //context.startActivity(boot_intent);
            context.startService(boot_intent);

        }///
//        throw new UnsupportedOperationException("Not yet implemented");
    }
}
