package com.technology.circles.apps.done.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        AlertManager manager = new AlertManager(context.getApplicationContext());
        manager.reStartAlarm();
    }
}
