package com.technology.circles.apps.done.broadcast;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.technology.circles.apps.done.local_database.AlertModel;
import com.technology.circles.apps.done.local_database.DataBaseActions;
import com.technology.circles.apps.done.local_database.DatabaseInteraction;
import com.technology.circles.apps.done.local_database.DeletedAlerts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertManager implements DatabaseInteraction {

    private Context context;
    private DataBaseActions dataBaseActions;

    public AlertManager(Context context) {
        this.context = context;
    }

    private void startAlarm(long time)
    {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"wake_lock");
        wakeLock.acquire();

        String t = new SimpleDateFormat("dd/MMM/yyy hh:mm aa", Locale.ENGLISH).format(new Date(time));
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), AlarmBroadcast.class);
        intent.putExtra("time",t);
        int id = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

        }
        wakeLock.release();
    }

    public void reStartAlarm()
    {
        dataBaseActions = new DataBaseActions(context);
        dataBaseActions.setInteraction(this);
        dataBaseActions.displayAlertByState(0);
    }

    public void startNewAlarm(AlertModel alertModel)
    {
        Calendar calendarTime = Calendar.getInstance();
        calendarTime.setTimeInMillis(System.currentTimeMillis());
        calendarTime.clear();

        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTimeInMillis(System.currentTimeMillis());
        calendarDate.clear();
        calendarTime.setTimeInMillis(alertModel.getTime());
        calendarDate.setTimeInMillis(alertModel.getDate());



        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.clear();
        calendar.set(Calendar.DAY_OF_MONTH,calendarDate.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.MONTH,calendarDate.get(Calendar.MONTH));
        calendar.set(Calendar.YEAR,calendarDate.get(Calendar.YEAR));
        calendar.set(Calendar.HOUR_OF_DAY,calendarTime.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE,calendarTime.get(Calendar.MINUTE));


        startAlarm(calendar.getTimeInMillis());
    }

    @Override
    public void insertedSuccess() {

    }

    @Override
    public void displayData(List<AlertModel> alertModelList) {

    }

    @Override
    public void displayByTime(AlertModel alertModel) {

    }

    @Override
    public void displayAlertsByState(List<AlertModel> alertModelList) {

        try {
            Log.e("size_state",alertModelList.size()+"_");
            Log.e("data1",alertModelList.get(0).getDetails()+"_"+alertModelList.get(0).getAlert_state());
            Log.e("data2",alertModelList.get(1).getDetails()+"_"+alertModelList.get(1).getAlert_state());

        }catch (Exception e){}

        if (alertModelList.size()>0)
        {


            for (AlertModel alertModel:alertModelList)
            {
                Calendar calendarTime = Calendar.getInstance();
                calendarTime.setTimeInMillis(System.currentTimeMillis());
                calendarTime.clear();

                Calendar calendarDate = Calendar.getInstance();
                calendarDate.setTimeInMillis(System.currentTimeMillis());
                calendarDate.clear();
                calendarTime.setTimeInMillis(alertModel.getTime());
                calendarDate.setTimeInMillis(alertModel.getDate());



                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.clear();
                calendar.set(Calendar.DAY_OF_MONTH,calendarDate.get(Calendar.DAY_OF_MONTH));
                calendar.set(Calendar.MONTH,calendarDate.get(Calendar.MONTH));
                calendar.set(Calendar.YEAR,calendarDate.get(Calendar.YEAR));
                calendar.set(Calendar.HOUR_OF_DAY,calendarTime.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE,calendarTime.get(Calendar.MINUTE));


                startAlarm(calendar.getTimeInMillis());


            }
        }

    }

    @Override
    public void displayAlertsByOnline(List<AlertModel> alertModelList) {

    }

    @Override
    public void displayAllAlerts(List<AlertModel> alertModelList) {

    }

    @Override
    public void displayAllDeletedAlerts(List<DeletedAlerts> deletedAlertsList) {

    }

    @Override
    public void onDeleteSuccess() {

    }
}
