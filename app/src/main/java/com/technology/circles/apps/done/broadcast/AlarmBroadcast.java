package com.technology.circles.apps.done.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.technology.circles.apps.done.activities_fragments.activity_inner_call.InnerCallActivity;
import com.technology.circles.apps.done.local_database.AlertModel;
import com.technology.circles.apps.done.local_database.DataBaseActions;
import com.technology.circles.apps.done.local_database.DatabaseInteraction;
import com.technology.circles.apps.done.local_database.DeletedAlerts;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.content.Context.VIBRATOR_SERVICE;

public class AlarmBroadcast extends BroadcastReceiver implements DatabaseInteraction {
    private DataBaseActions dataBaseActions;
    private Context context;
    private LocalNotification localNotification;
    private MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        dataBaseActions = new DataBaseActions(context.getApplicationContext());
        dataBaseActions.setInteraction(this);
        String time = intent.getStringExtra("time");
        dataBaseActions.displayAlertByTime(time);



    }

    @Override
    public void insertedSuccess() {

    }

    @Override
    public void displayData(List<AlertModel> alertModelList) {

    }

    @Override
    public void displayByTime(AlertModel alertModel)
    {

        if (alertModel!=null)
        {
            Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(3000,VibrationEffect.DEFAULT_AMPLITUDE));
            }else
            {
                vibrator.vibrate(3000);

            }

            if (alertModel.getIs_alert()==1)
            {
                localNotification = new LocalNotification(context,alertModel.getDetails());
                localNotification.manageNotification();
            }


            if (alertModel.getIs_inner_call()==1)
            {
                Intent intent = new Intent(context.getApplicationContext(), InnerCallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("data",alertModel);
                context.startActivity(intent);


            }else
            {
                if (alertModel.getIs_sound()==1)
                {
                    initMediaPlayer(alertModel);
                }
            }
            alertModel.setAlert_state(1);
            dataBaseActions.update(alertModel);


        }




    }

    @Override
    public void displayAlertsByState(List<AlertModel> alertModelList) {

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

    private void initMediaPlayer(AlertModel alertModel) {

        mediaPlayer = new MediaPlayer();
        File file = new File(alertModel.getAudio_path());
        if (file.exists())
        {
            try {
                mediaPlayer.setDataSource(file.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(MediaPlayer::start);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
