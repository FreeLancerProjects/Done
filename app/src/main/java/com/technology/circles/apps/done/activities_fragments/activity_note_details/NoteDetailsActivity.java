package com.technology.circles.apps.done.activities_fragments.activity_note_details;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.databinding.ActivityNoteDetailsBinding;
import com.technology.circles.apps.done.interfaces.Listeners;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.local_database.AlertModel;
import com.technology.circles.apps.done.local_database.DataBaseActions;
import com.technology.circles.apps.done.local_database.DatabaseInteraction;
import com.technology.circles.apps.done.local_database.DeletedAlerts;
import com.technology.circles.apps.done.models.SingleAlertModel;
import com.technology.circles.apps.done.models.UserModel;
import com.technology.circles.apps.done.preferences.Preferences;
import com.technology.circles.apps.done.remote.Api;
import com.technology.circles.apps.done.tags.Tags;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoteDetailsActivity extends AppCompatActivity implements Listeners.BackListener, DatabaseInteraction {

    private ActivityNoteDetailsBinding binding;
    private String lang;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;
    private AlertModel alertModel;
    private File file;
    private final String write_perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final int write_req = 100;
    private UserModel userModel;
    private Preferences preferences;
    private SingleAlertModel.Alert alert;
    private DataBaseActions dataBaseActions;




    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_note_details);
        getDataFromIntent();
        initView();


    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent!=null)
        {
            alertModel = (AlertModel) intent.getSerializableExtra("data");

        }
    }


    private void initView() {
        dataBaseActions = new DataBaseActions(this);
        dataBaseActions.setInteraction(this);
        preferences = Preferences.newInstance();
        userModel =preferences.getUserData(this);

        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setBackListener(this);
        binding.setLang(lang);
        binding.setModel(alertModel);



        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaPlayer!=null&&b)
                {

                    mediaPlayer.seekTo(i);


                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        binding.imagePlay.setOnClickListener(view -> {

            if (mediaPlayer!=null&&mediaPlayer.isPlaying())
            {
                binding.recordDuration.setText(getDuration(mediaPlayer.getCurrentPosition()));
                mediaPlayer.pause();
                binding.imagePlay.setImageResource(R.drawable.ic_play);

            }else
            {

                if (mediaPlayer!=null)
                {
                    binding.imagePlay.setImageResource(R.drawable.ic_pause);

                    mediaPlayer.start();
                    updateProgress();


                }
            }

        });

        binding.llDownload.setOnClickListener(view -> {
            binding.llDownload.setEnabled(false);
            binding.imageDownload.setColorFilter(ContextCompat.getColor(this,R.color.gray4));
            binding.progBar.setVisibility(View.VISIBLE);
            binding.tvDownload.setText(R.string.downloading);
            getOnlineSound();

        });
        checkWritePermission();
    }

    private void displayRecord()
    {
        if (alertModel.getIs_sound()==1)
        {
            file = new File(alertModel.getAudio_path());
            if (file.exists())
            {
                initAudio();
            }else
            {
                binding.llDownload.setVisibility(View.VISIBLE);
            }
        }
    }



    private void initAudio()
    {
        try {


            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(100.0f,100.0f);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepare();
            binding.recordDuration.setText(getDuration(mediaPlayer.getDuration()));

            mediaPlayer.setOnPreparedListener(mediaPlayer -> {
                binding.cardView.setVisibility(View.VISIBLE);
                binding.seekBar.setMax(mediaPlayer.getDuration());
                binding.imagePlay.setImageResource(R.drawable.ic_play);
            });

            mediaPlayer.setOnCompletionListener(mediaPlayer -> {

                binding.recordDuration.setText(getDuration(mediaPlayer.getDuration()));
                binding.imagePlay.setImageResource(R.drawable.ic_play);
                binding.seekBar.setProgress(0);
                handler.removeCallbacks(runnable);

            });

        } catch (IOException e) {


        }
    }

    private void updateProgress()
    {
        binding.seekBar.setProgress(mediaPlayer.getCurrentPosition());
        binding.recordDuration.setText(getDuration(mediaPlayer.getCurrentPosition()));
        handler = new Handler();
        runnable  = this::updateProgress;
        handler.postDelayed(runnable,1000);



    }



    private String getDuration(long duration)
    {

        String total_duration="00:00";

        if (mediaPlayer!=null)
        {
            total_duration = String.format(Locale.ENGLISH,"%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
            );


        }

        return total_duration;

    }

    private void getOnlineSound() {

        Api.getService(Tags.base_url)
                .getSingleAlert(userModel.getToken(),alertModel.getAlert_id())
                .enqueue(new Callback<SingleAlertModel>() {
                    @Override
                    public void onResponse(Call<SingleAlertModel> call, Response<SingleAlertModel> response) {

                        if (response.isSuccessful()&&response.body()!=null&&response.body().getData()!=null)
                        {
                            alert = response.body().getData();
                            binding.llDownload.setVisibility(View.VISIBLE);
                            new MyTask().execute();
                        }else
                        {
                            resetDownloadUI();

                            if (response.code()==500)
                            {
                                Toast.makeText(NoteDetailsActivity.this,"Server Error", Toast.LENGTH_SHORT).show();

                            }else
                            {
                                Toast.makeText(NoteDetailsActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        }

                    }

                    @Override
                    public void onFailure(Call<SingleAlertModel> call, Throwable t) {
                        try {
                            resetDownloadUI();
                            if (t.getMessage() != null) {
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(NoteDetailsActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(NoteDetailsActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void resetDownloadUI() {
        binding.llDownload.setEnabled(true);
        binding.imageDownload.setColorFilter(ContextCompat.getColor(NoteDetailsActivity.this,R.color.colorPrimary));
        binding.progBar.setVisibility(View.GONE);
        binding.tvDownload.setText(R.string.download);
    }


    private class MyTask extends AsyncTask<Void,Void,String>{
        private String path="";
        private boolean  isMkdir = true;
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(Tags.sound_path+alert.getSound_file());
                URLConnection connection = url.openConnection();
                connection.connect();
                File file = new File(Tags.local_folder_path);
                if (!file.exists())
                {
                    isMkdir = file.mkdir();
                }

                if (isMkdir)
                {
                    File sound_file = new File(file,alert.getSound_file());
                    path = sound_file.getAbsolutePath();
                    DataInputStream inputStream = new DataInputStream(connection.getInputStream());
                    DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(sound_file));
                    byte [] data;
                    if (connection.getContentLength()>0)
                    {
                        data = new byte[connection.getContentLength()];
                    }else
                    {
                        data = new byte[1024*4];

                    }

                    int count;
                    while ((count=inputStream.read(data))!=-1)
                    {
                        outputStream.write(data,0,count);
                    }
                    outputStream.flush();
                    inputStream.close();
                    outputStream.close();

                }



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return path;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!s.isEmpty())
            {

                file = new File(s);
                initAudio();
                binding.llDownload.setVisibility(View.GONE);
                alertModel.setAudio_path(s);
                alertModel.setAudio_name(alert.getSound_file());
                dataBaseActions.update(alertModel);

            }else
            {
                if (!isMkdir)
                {
                    resetDownloadUI();
                    Toast.makeText(NoteDetailsActivity.this, "Cannot create folder for app done", Toast.LENGTH_LONG).show();
                }else
                    {
                        binding.llDownload.setVisibility(View.GONE);

                    }

            }
        }
    }

    private void checkWritePermission() {

        if (ContextCompat.checkSelfPermission(this, write_perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{write_perm}, write_req);

        } else {
            displayRecord();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == write_req && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
            displayRecord();
        }
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


    @Override
    public void back() {
        finish();
    }

    @Override
    public void onBackPressed() {

        back();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mediaPlayer!=null)
        {
            mediaPlayer.release();
            mediaPlayer=null;

        }

        if (handler!=null&&runnable!=null)
        {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }



}
