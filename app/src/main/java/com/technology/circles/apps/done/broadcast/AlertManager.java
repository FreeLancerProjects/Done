package com.technology.circles.apps.done.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlertManager {

    private Context context;

    public AlertManager(Context context) {
        this.context = context;
    }
    public void startAlarm(long time)
    {
        String t = new SimpleDateFormat("dd/MMM/yyy hh:mm aa", Locale.ENGLISH).format(new Date(time));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), AlarmBroadcast.class);
        intent.putExtra("time",t);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

        }
    }
}
