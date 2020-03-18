package com.technology.circles.apps.done.activities_fragments.activity_inner_call;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.databinding.ActivityInnerCallBinding;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.local_database.AlertModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.paperdb.Paper;

public class InnerCallActivity extends AppCompatActivity {
    private ActivityInnerCallBinding binding;
    private AlertModel alertModel;
    private MediaPlayer mediaPlayer1,mediaPlayer2;
    private String path;

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_inner_call);
        getDataFromIntent();
        initView();


    }

    private void getDataFromIntent()
    {

        Intent intent = getIntent();
        if (intent!=null&&intent.hasExtra("data"))
        {
            alertModel = (AlertModel) intent.getSerializableExtra("data");
        }
    }

    private void initView()
    {

        initMediaPlayerRingtone();
        if (alertModel.getIs_sound()==1)
        {
            binding.fab.setText(getString(R.string.listen));
        }else
            {
                binding.fab.setText(getString(R.string.reply));

            }
        binding.fab.setOnActiveListener(() -> {
            binding.fab.setVisibility(View.INVISIBLE);
            if (mediaPlayer1!=null)
            {
                mediaPlayer1.release();
                mediaPlayer1=null;
            }

            if (alertModel.getIs_sound()==1)
            {


                initMediaPlayerCall();
            }else
            {
                finish();
            }
        });

    }

    private void initMediaPlayerRingtone()
    {
        mediaPlayer1 = MediaPlayer.create(this,R.raw.ring);
        mediaPlayer1.setLooping(true);
        mediaPlayer1.setOnPreparedListener(MediaPlayer::start);



    }

    private void initMediaPlayerCall()
    {
        mediaPlayer2 = new MediaPlayer();
        mediaPlayer2 = new MediaPlayer();
        File file = getFile(alertModel.getSound(),alertModel);
        if (file!=null)
        {
            try {
                mediaPlayer2.setDataSource(file.getAbsolutePath());
                mediaPlayer2.prepare();
                mediaPlayer2.setOnPreparedListener(MediaPlayer::start);
                mediaPlayer2.setOnCompletionListener(mediaPlayer -> {
                    File file2 = new File(path);
                    file2.delete();
                    finish();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getFile(byte[] sound,AlertModel alertModel)
    {

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

    @Override
    public void onBackPressed()
    {
        if (mediaPlayer1!=null)
        {
            mediaPlayer1.release();
            mediaPlayer1=null;
        }

        if (mediaPlayer2!=null)
        {
            mediaPlayer2.release();
            mediaPlayer2=null;
        }

        finish();
    }
}
