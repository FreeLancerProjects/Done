package com.technology.circles.apps.done.activities_fragments.activity_add_note;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_contact.ContactsActivity;
import com.technology.circles.apps.done.databinding.ActivityAddNoteBinding;
import com.technology.circles.apps.done.interfaces.Listeners;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.local_database.AlertModel;
import com.technology.circles.apps.done.local_database.ViewModel;
import com.technology.circles.apps.done.models.AddAlertModel;
import com.technology.circles.apps.done.share.Common;
import com.technology.circles.apps.done.tags.Tags;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class AddNoteActivity extends AppCompatActivity implements Listeners.BackListener , DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private ActivityAddNoteBinding binding;
    private String lang;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private MediaRecorder recorder;
    private final String write_perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String audio_perm = Manifest.permission.RECORD_AUDIO;
    private final int write_req = 100;
    private String path;
    private String audioName;
    private boolean isPermissionGranted = false;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;
    private View root;
    private BottomSheetBehavior behavior;
    private LinearLayout btnSocial,btnFriend;
    private boolean isAlert = true;
    private boolean isInnerCall = false;
    private boolean isOutCall = false;
    private AddAlertModel model;
    private ViewModel viewModel;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_note);
        initView();


    }


    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        model = new AddAlertModel();
        viewModel = ViewModelProviders.of(this).get(ViewModel.class);
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setBackListener(this);
        binding.setLang(lang);
        binding.setModel(model);

        root = findViewById(R.id.root);
        btnSocial = findViewById(R.id.btnSocial);
        btnFriend = findViewById(R.id.btnFriend);

        behavior = BottomSheetBehavior.from(root);


        checkWritePermission();
        createDatePickerDialog();
        createTimePickerDialog();


        Glide.with(this)
                .asGif()
                .load(R.raw.wave)
                .into(binding.imageWave);

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



        binding.imageMic.setOnTouchListener((view, motionEvent) -> {

            if (motionEvent.getAction()==MotionEvent.ACTION_DOWN)
            {
                if (isPermissionGranted)
                {
                    if (recorder!=null)
                    {
                        recorder.release();
                        recorder = null;

                    }
                    initRecorder();
                }else
                    {
                        Toast.makeText(this, "Cannot access mic", Toast.LENGTH_SHORT).show();
                    }
            }
            else if (motionEvent.getAction()==MotionEvent.ACTION_UP)
            {
                if (isPermissionGranted)
                {

                    try {
                        recorder.stop();
                        binding.imageWave.setVisibility(View.GONE);
                        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
                        mediaPlayer = null;
                        initAudio();

                    }catch (Exception e){
                        binding.imageWave.setVisibility(View.GONE);
                    }


                }else
                {
                    Toast.makeText(this, "Cannot access mic", Toast.LENGTH_SHORT).show();
                }


            }



            return true;
        });

        binding.changeTime.setOnClickListener(view -> {

            try {
                timePickerDialog.show(getFragmentManager(),"");
            }catch (Exception e){}

        });
        binding.changeDate.setOnClickListener(view -> {

            try {
                datePickerDialog.show(getFragmentManager(),"");
            }catch (Exception e){}

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
        binding.imageDelete.setOnClickListener(view -> {
            binding.cardView.setVisibility(View.GONE);

            File file = new File(path);
            file.delete();
            path = "";
            model.setSound_path("");
            binding.setModel(model);
        });
        binding.rbPublic.setOnClickListener(view -> {

            model.setAlert_type(Tags.PUBLIC_ALERT);
            binding.setModel(model);

        });
        binding.rbPrivate.setOnClickListener(view -> {

            model.setAlert_type(Tags.PRIVATE_ALERT);
            binding.setModel(model);

        });
        binding.btnSaveShare.setOnClickListener(view -> {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        btnSocial.setOnClickListener(view -> {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,"");
            intent.setType("text/plain");
            startActivity(intent);
        });

        btnFriend.setOnClickListener(view -> {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            new Handler()
                    .postDelayed(() -> {
                        Intent intent = new Intent(this, ContactsActivity.class);
                        startActivity(intent);
                    },500);

        });

        binding.llAlert.setOnClickListener(view -> {

            if (isAlert)
            {
                isAlert = false;
                binding.llAlert.setBackgroundResource(R.drawable.rounded_gray);
                binding.iconAlert.setColorFilter(ContextCompat.getColor(this,R.color.black));
                binding.tvAlert.setTextColor(ContextCompat.getColor(this,R.color.black));
                model.setAlert(false);

            }else
                {
                    isAlert = true;
                    binding.llAlert.setBackgroundResource(R.drawable.rounded);
                    binding.iconAlert.setColorFilter(ContextCompat.getColor(this,R.color.white));
                    binding.tvAlert.setTextColor(ContextCompat.getColor(this,R.color.white));
                    model.setAlert(true);

                }


            binding.setModel(model);



        });

        binding.llVoice.setOnClickListener(view -> {


            if (isInnerCall)
            {
                isInnerCall = false;
                binding.llVoice.setBackgroundResource(R.drawable.rounded_gray);
                binding.iconVoice.setColorFilter(ContextCompat.getColor(this,R.color.black));
                binding.tvVoice.setTextColor(ContextCompat.getColor(this,R.color.black));
                model.setInnerCall(false);

            }else
                {
                    isInnerCall = true;
                    binding.llVoice.setBackgroundResource(R.drawable.rounded);
                    binding.iconVoice.setColorFilter(ContextCompat.getColor(this,R.color.white));
                    binding.tvVoice.setTextColor(ContextCompat.getColor(this,R.color.white));

                    model.setInnerCall(true);
                }



            binding.setModel(model);



        });

        binding.llCall.setOnClickListener(view -> {

            if (isOutCall)
            {
                isOutCall = false;
                binding.llCall.setBackgroundResource(R.drawable.rounded_gray);
                binding.iconCall.setColorFilter(ContextCompat.getColor(this,R.color.black));
                binding.tvCall.setTextColor(ContextCompat.getColor(this,R.color.black));
                model.setOuterCall(false);

            }else
                {

                    isOutCall = true;

                    binding.llCall.setBackgroundResource(R.drawable.rounded);
                    binding.iconCall.setColorFilter(ContextCompat.getColor(this,R.color.white));
                    binding.tvCall.setTextColor(ContextCompat.getColor(this,R.color.white));

                    model.setOuterCall(true);


                }


            binding.setModel(model);





        });

        binding.btnSave.setOnClickListener(view -> {

            if (model.isDataValid(this))
            {
                Common.CloseKeyBoard(this,binding.edtDetails);

                save();
            }
        });

    }

    private void save() {

        int alert = 0;
        int in_call =0;
        int out_call = 0;
        int is_sound =0;

        if (isInnerCall)
        {
            in_call = 1;
        }

        if (isOutCall)
        {
            out_call = 1;

        }

        if (isAlert)
        {
            alert = 1;
        }




        AlertModel alertModel = new AlertModel(model.getTime(),model.getDate(),model.getAlert_type(),alert,in_call,out_call,model.getDetails());

        if (!model.getSound_path().isEmpty())
        {
            is_sound = 1;
            File file = new File(model.getSound_path());
            byte [] sound = new byte[(int) file.length()];

            try {
                FileInputStream inputStream = new FileInputStream(file);
                inputStream.read(sound);
                inputStream.close();
                alertModel.setSound(sound);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        alertModel.setIs_sound(is_sound);
        viewModel.insert(alertModel);
        setResult(RESULT_OK);
        finish();

    }


    private void initRecorder()
    {


        binding.cardView.setVisibility(View.GONE);
        isPermissionGranted = true;
        audioName = "AUD"+System.currentTimeMillis()+".mp3";
        binding.tvName.setText(audioName);

        path = Environment.getExternalStorageDirectory().getAbsoluteFile()+"/"+audioName;

        recorder = new MediaRecorder();
        model.setSound_path(path);
        binding.setModel(model);

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioChannels(1);
        recorder.setOutputFile(path);
        try {
            recorder.prepare();
            recorder.start();
            binding.imageWave.setVisibility(View.VISIBLE);


        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Faild","Failed");
            binding.imageWave.setVisibility(View.GONE);
            binding.cardView.setVisibility(View.GONE);
            File file = new File(path);
            file.delete();

            if (mediaPlayer!=null)
            {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            if (handler!=null&&runnable!=null)
            {
                handler.removeCallbacks(runnable);
            }
        }

    }

    private void initAudio()
    {
        try {


            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
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
            Log.e("eeeex",e.getMessage());
            File file = new File(path);
            file.delete();
            binding.imageWave.setVisibility(View.GONE);
            mediaPlayer.release();
            mediaPlayer = null;
            if (handler!=null&&runnable!=null)
            {
                handler.removeCallbacks(runnable);
            }
            binding.cardView.setVisibility(View.GONE);

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

    private void checkWritePermission() {

        if (ContextCompat.checkSelfPermission(this,write_perm)!= PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(this,audio_perm)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{write_perm,audio_perm},write_req);

        }else
            {
                isPermissionGranted = true;
            }
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



    private void createDatePickerDialog()
    {
        Calendar calendar = Calendar.getInstance();
        datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.dismissOnPause(true);
        datePickerDialog.setAccentColor(ActivityCompat.getColor(this, R.color.colorPrimary));
        datePickerDialog.setCancelColor(ActivityCompat.getColor(this, R.color.gray4));
        datePickerDialog.setOkColor(ActivityCompat.getColor(this, R.color.colorPrimary));
        datePickerDialog.setOkText(getString(R.string.select));
        datePickerDialog.setCancelText(getString(R.string.cancel));
        datePickerDialog.setMinDate(calendar);
        datePickerDialog.setLocale(Locale.ENGLISH);
        datePickerDialog.setVersion(DatePickerDialog.Version.VERSION_2);

    }

    private void createTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePickerDialog.dismissOnPause(true);
        timePickerDialog.setAccentColor(ActivityCompat.getColor(this, R.color.colorPrimary));
        timePickerDialog.setCancelColor(ActivityCompat.getColor(this, R.color.gray4));
        timePickerDialog.setOkColor(ActivityCompat.getColor(this, R.color.colorPrimary));
        timePickerDialog.setOkText(getString(R.string.select));
        timePickerDialog.setCancelText(getString(R.string.cancel));
        datePickerDialog.setLocale(Locale.ENGLISH);
        timePickerDialog.setVersion(TimePickerDialog.Version.VERSION_2);


    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
        String date = dateFormat.format(new Date(calendar.getTimeInMillis()));
        binding.tvDate.setText(date);

        model.setDate(calendar.getTimeInMillis());
        binding.setModel(model);
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
        String time = dateFormat.format(new Date(calendar.getTimeInMillis()));


        SimpleDateFormat dateFormat1 = new SimpleDateFormat("hh", Locale.ENGLISH);
        String h = dateFormat1.format(new Date(calendar.getTimeInMillis()));


        SimpleDateFormat dateFormat2 = new SimpleDateFormat("mm", Locale.ENGLISH);
        String m = dateFormat2.format(new Date(calendar.getTimeInMillis()));


        SimpleDateFormat dateFormat3 = new SimpleDateFormat("aa", Locale.ENGLISH);
        String a = dateFormat3.format(new Date(calendar.getTimeInMillis()));

        binding.tvHour.setText(h);
        binding.tvMin.setText(m);
        binding.tvAMPM.setText(a);

        model.setTime(calendar.getTimeInMillis());
        binding.setModel(model);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==write_req&&grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED&&grantResults[1]==PackageManager.PERMISSION_GRANTED)
        {
            isPermissionGranted = true;
        }
    }

    @Override
    public void back() {
       finish();
    }

    @Override
    public void onBackPressed() {

        if (behavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
        {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else
            {
                back();
            }
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
