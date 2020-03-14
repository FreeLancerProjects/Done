package com.technology.circles.apps.done.activities_fragments.activity_note_details;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.databinding.ActivityNoteDetailsBinding;
import com.technology.circles.apps.done.interfaces.Listeners;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.local_database.AlertModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class NoteDetailsActivity extends AppCompatActivity implements Listeners.BackListener{

    private ActivityNoteDetailsBinding binding;
    private String lang;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;
    private AlertModel alertModel;
    private File file;



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

        if (alertModel.getIs_sound()==1)
        {
            file = getFile(alertModel.getSound());
            if (file!=null)
            {
                initAudio();
            }else
                {
                    Log.e("ddd","fff");
                }
        }

    }

    private File getFile(byte[] sound) {

        File file = null;
        try {
            String path = Environment.getExternalStorageDirectory().getAbsoluteFile() +"/AUD"+alertModel.getId()+".mp3";
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


    @Override
    public void back() {
        finish();
    }

    @Override
    public void onBackPressed() {

        back();
    }

    @Override
    protected void onDestroy() {
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
