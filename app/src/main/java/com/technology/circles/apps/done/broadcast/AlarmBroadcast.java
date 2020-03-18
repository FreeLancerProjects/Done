package com.technology.circles.apps.done.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.technology.circles.apps.done.activities_fragments.activity_inner_call.InnerCallActivity;
import com.technology.circles.apps.done.local_database.AlertModel;
import com.technology.circles.apps.done.local_database.DataBaseActions;
import com.technology.circles.apps.done.local_database.DatabaseInteraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.content.Context.VIBRATOR_SERVICE;

public class AlarmBroadcast extends BroadcastReceiver implements DatabaseInteraction {
    private DataBaseActions dataBaseActions;
    private Context context;
    private LocalNotification localNotification;
    private MediaPlayer mediaPlayer;
    private String path;

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
                intent.putExtra("data",alertModel);
                context.startActivity(intent);
            }else
            {
                if (alertModel.getIs_sound()==1)
                {
                    initMediaPlayer(alertModel);
                }
            }
            Log.e("rr","rrr");
            alertModel.setAlert_state(1);
            dataBaseActions.update(alertModel);


        }




    }

    @Override
    public void displayAlertsByState(List<AlertModel> alertModelList) {

    }

    private void initMediaPlayer(AlertModel alertModel) {

        mediaPlayer = new MediaPlayer();
        File file = getFile(alertModel.getSound(),alertModel);
        if (file!=null)
        {
            try {
                mediaPlayer.setDataSource(file.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(MediaPlayer::start);
                mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                    File file2 = new File(path);
                    file2.delete();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private File getFile(byte[] sound,AlertModel alertModel) {

        File file = null;
        try {


            File folder_done = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/Done_Audio");

            if (!folder_done.exists())
            {
                folder_done.mkdir();
            }

            path = folder_done.getAbsolutePath() + "/" + alertModel.getAudio_name();

            file = new File(path);

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(sound);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("ex",e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ex2",e.getMessage());

        }
        return file;
    }
}
