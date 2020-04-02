package com.technology.circles.apps.done.activities_fragments.activity_add_note;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_contact.ContactsActivity;
import com.technology.circles.apps.done.broadcast.AlertManager;
import com.technology.circles.apps.done.databinding.ActivityAddNoteBinding;
import com.technology.circles.apps.done.databinding.DialogPasswordBinding;
import com.technology.circles.apps.done.interfaces.Listeners;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.local_database.AlertModel;
import com.technology.circles.apps.done.local_database.DataBaseActions;
import com.technology.circles.apps.done.local_database.DatabaseInteraction;
import com.technology.circles.apps.done.local_database.DeletedAlerts;
import com.technology.circles.apps.done.models.AddAlertModel;
import com.technology.circles.apps.done.models.ContactModel;
import com.technology.circles.apps.done.models.ShareModel;
import com.technology.circles.apps.done.models.UserModel;
import com.technology.circles.apps.done.preferences.Preferences;
import com.technology.circles.apps.done.remote.Api;
import com.technology.circles.apps.done.share.Common;
import com.technology.circles.apps.done.tags.Tags;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNoteActivity extends AppCompatActivity implements Listeners.BackListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, DatabaseInteraction {

    private ActivityAddNoteBinding binding;
    private String lang;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private MediaRecorder recorder;
    private final String write_perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String audio_perm = Manifest.permission.RECORD_AUDIO;
    private final int write_req = 100;

    private boolean isPermissionGranted = false;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;
    private View root;
    private BottomSheetBehavior behavior;
    private LinearLayout btnSocial, btnFriend;
    private boolean isAlert = true;
    private boolean isInnerCall = false;
    private boolean isOutCall = false;
    private AddAlertModel model;
    private DataBaseActions dataBaseActions;
    private Preferences preferences;
    private UserModel userModel;
    private AlertModel alertModel;
    private ContactModel contactModel;

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
        preferences = Preferences.newInstance();
        userModel = preferences.getUserData(this);
        model = new AddAlertModel();
        dataBaseActions = new DataBaseActions(this);
        dataBaseActions.setInteraction(this);
        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
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
                if (mediaPlayer != null && b) {

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

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (isPermissionGranted) {
                    if (recorder != null) {
                        recorder.release();
                        recorder = null;

                    }
                    initRecorder();
                } else {
                    Toast.makeText(this, "Cannot access mic", Toast.LENGTH_SHORT).show();
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (isPermissionGranted) {

                    try {
                        recorder.stop();
                        binding.imageWave.setVisibility(View.GONE);
                        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
                        mediaPlayer = null;
                        initAudio();

                    } catch (Exception e) {
                        binding.imageWave.setVisibility(View.GONE);
                    }


                } else {
                    Toast.makeText(this, "Cannot access mic", Toast.LENGTH_SHORT).show();
                }


            }


            return true;
        });
        binding.changeTime.setOnClickListener(view -> {

            try {
                timePickerDialog.show(getFragmentManager(), "");
            } catch (Exception e) {
            }

        });
        binding.changeDate.setOnClickListener(view -> {

            try {
                datePickerDialog.show(getFragmentManager(), "");
            } catch (Exception e) {
            }

        });
        binding.imagePlay.setOnClickListener(view -> {

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                binding.recordDuration.setText(getDuration(mediaPlayer.getCurrentPosition()));
                mediaPlayer.pause();
                binding.imagePlay.setImageResource(R.drawable.ic_play);

            } else {

                if (mediaPlayer != null) {
                    binding.imagePlay.setImageResource(R.drawable.ic_pause);

                    mediaPlayer.start();
                    updateProgress();


                }
            }

        });
        binding.imageDelete.setOnClickListener(view -> {
            binding.cardView.setVisibility(View.GONE);
            deleteFile();
            model.setAudio_name("");
            model.setSound_path("");
            binding.setModel(model);
            if (mediaPlayer!=null)
            {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
                runnable = null;
            }
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

            if (model.isDataValid(this)) {
                Common.CloseKeyBoard(this, binding.edtDetails);


                if (model.getAlert_type() == Tags.PRIVATE_ALERT) {
                    if (preferences.getPassword(this) == null || preferences.getPassword(this).isEmpty()) {
                        createPasswordDialogAlert();
                    } else {
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    }
                } else {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                }
            }


        });

        btnSocial.setOnClickListener(view -> {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, "");
            intent.setType("text/plain");
            startActivity(intent);
        });

        btnFriend.setOnClickListener(view -> {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            new Handler()
                    .postDelayed(() -> {
                       save(1);
                    }, 500);

        });

        binding.llAlert.setOnClickListener(view -> {

            if (isAlert) {
                isAlert = false;
                binding.llAlert.setBackgroundResource(R.drawable.rounded_gray);
                binding.iconAlert.setColorFilter(ContextCompat.getColor(this, R.color.black));
                binding.tvAlert.setTextColor(ContextCompat.getColor(this, R.color.black));
                model.setAlert(false);

            } else {
                isAlert = true;
                binding.llAlert.setBackgroundResource(R.drawable.rounded);
                binding.iconAlert.setColorFilter(ContextCompat.getColor(this, R.color.white));
                binding.tvAlert.setTextColor(ContextCompat.getColor(this, R.color.white));
                model.setAlert(true);

            }


            binding.setModel(model);


        });

        binding.llVoice.setOnClickListener(view -> {


            if (isInnerCall) {
                isInnerCall = false;
                binding.llVoice.setBackgroundResource(R.drawable.rounded_gray);
                binding.iconVoice.setColorFilter(ContextCompat.getColor(this, R.color.black));
                binding.tvVoice.setTextColor(ContextCompat.getColor(this, R.color.black));
                model.setInnerCall(false);

            } else {
                isInnerCall = true;
                binding.llVoice.setBackgroundResource(R.drawable.rounded);
                binding.iconVoice.setColorFilter(ContextCompat.getColor(this, R.color.white));
                binding.tvVoice.setTextColor(ContextCompat.getColor(this, R.color.white));

                model.setInnerCall(true);
            }


            binding.setModel(model);


        });

        binding.llCall.setOnClickListener(view -> {

            if (isOutCall) {
                isOutCall = false;
                binding.llCall.setBackgroundResource(R.drawable.rounded_gray);
                binding.iconCall.setColorFilter(ContextCompat.getColor(this, R.color.black));
                binding.tvCall.setTextColor(ContextCompat.getColor(this, R.color.black));
                model.setOuterCall(false);

            } else {

                isOutCall = true;

                binding.llCall.setBackgroundResource(R.drawable.rounded);
                binding.iconCall.setColorFilter(ContextCompat.getColor(this, R.color.white));
                binding.tvCall.setTextColor(ContextCompat.getColor(this, R.color.white));

                model.setOuterCall(true);


            }


            binding.setModel(model);


        });

        binding.btnSave.setOnClickListener(view -> {

            if (model.isDataValid(this)) {
                Common.CloseKeyBoard(this, binding.edtDetails);


                if (model.getAlert_type() == Tags.PRIVATE_ALERT) {
                    if (preferences.getPassword(this) == null || preferences.getPassword(this).isEmpty()) {
                        createPasswordDialogAlert();
                    } else {
                        save(0);

                    }
                } else {
                    save(0);

                }
            }
        });

    }

    private void save(int share) {

        int alert = 0;
        int in_call = 0;
        int out_call = 0;
        int is_sound = 0;

        if (isInnerCall) {
            in_call = 1;
        }

        if (isOutCall) {
            out_call = 1;

        }

        if (isAlert) {
            alert = 1;
        }


        alertModel = new AlertModel(UUID.randomUUID().toString(),model.getTime(), model.getDate(), model.getAlert_type(), alert, in_call, out_call, 0, model.getDetails());

        if (!model.getSound_path().isEmpty()) {
            is_sound = 1;

        }

        alertModel.setIs_sound(is_sound);

        Calendar calendarTime = Calendar.getInstance();
        Calendar calendarDate = Calendar.getInstance();
        calendarTime.setTimeInMillis(alertModel.getTime());
        calendarDate.setTimeInMillis(alertModel.getDate());


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.clear();
        calendar.set(Calendar.DAY_OF_MONTH, calendarDate.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.MONTH, calendarDate.get(Calendar.MONTH));
        calendar.set(Calendar.YEAR, calendarDate.get(Calendar.YEAR));
        calendar.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));


        String time = new SimpleDateFormat("dd/MMM/yyyy hh:mm aa", Locale.ENGLISH).format(new Date(calendar.getTimeInMillis()));
        alertModel.setAudio_name(model.getAudio_name());
        alertModel.setAlert_time(time);
        alertModel.setAlert_state(0);
        alertModel.setAudio_path(model.getSound_path());

        if (share==0)
        {
            if (alertModel.getIs_sound()==1)
            {
                saveOnlineWithSound(alertModel,share);
            }else
            {
                saveOnlineWithoutSound(alertModel,share);

            }
        }else if (share==1)
            {
                Intent intent = new Intent(this, ContactsActivity.class);
                startActivityForResult(intent,100);
            }


    }

    private void insertNewAlert(AlertModel alertModel)
    {

        dataBaseActions.insert(alertModel);

        AlertManager manager = new AlertManager(this);
        manager.startNewAlarm(alertModel);
    }

    private void saveOnlineWithSound(AlertModel alertModel, int share)
    {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();

        RequestBody alert_id_part = Common.getRequestBodyText(alertModel.getAlert_id());

        RequestBody time_int_part = Common.getRequestBodyText(String.valueOf(alertModel.getTime()));
        RequestBody date_int_part = Common.getRequestBodyText(String.valueOf(alertModel.getDate()));
        RequestBody alert_type_part = Common.getRequestBodyText(String.valueOf(alertModel.getAlert_type()));
        RequestBody is_alert_part = Common.getRequestBodyText(String.valueOf(alertModel.getIs_alert()));
        RequestBody inner_call_part = Common.getRequestBodyText(String.valueOf(alertModel.getIs_inner_call()));
        RequestBody outer_call_part = Common.getRequestBodyText(String.valueOf(alertModel.getIs_outer_call()));
        RequestBody is_sound_part = Common.getRequestBodyText(String.valueOf(alertModel.getIs_sound()));
        RequestBody details_part = Common.getRequestBodyText(String.valueOf(alertModel.getDetails()));
        RequestBody alert_state_part = Common.getRequestBodyText(String.valueOf(alertModel.getAlert_state()));
        RequestBody alert_time_part = Common.getRequestBodyText(String.valueOf(alertModel.getAlert_time()));
        MultipartBody.Part sound_file_part = Common.getMultiPartSound(model.getSound_path(), "sound_file");


        Api.getService(Tags.base_url)
                .makeAlertWithSound(userModel.getToken(),alert_id_part, time_int_part, date_int_part, alert_type_part, is_alert_part, inner_call_part, outer_call_part, is_sound_part, details_part, alert_state_part, alert_time_part, sound_file_part)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            alertModel.setIsOnline(1);
                            insertNewAlert(alertModel);

                            if (share==0)
                            {
                                dialog.dismiss();

                                setResult(RESULT_OK);
                                finish();
                            }else if (share==1)
                            {
                                shareWithContacts(alertModel.getAlert_id(),dialog);
                            }else if (share==2)
                            {

                            }

                        } else {


                            dialog.dismiss();

                            alertModel.setIsOnline(0);
                            insertNewAlert(alertModel);
                            setResult(RESULT_OK);
                            finish();


                            if (response.code() == 500) {
                                Toast.makeText(AddNoteActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddNoteActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }

                            try {
                                Log.e("error_code", response.code() + "__" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();

                            alertModel.setIsOnline(0);
                            insertNewAlert(alertModel);
                            setResult(RESULT_OK);
                            finish();


                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(AddNoteActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddNoteActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void saveOnlineWithoutSound(AlertModel alertModel, int share)
    {

        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        RequestBody alert_id_part = Common.getRequestBodyText(alertModel.getAlert_id());
        RequestBody time_int_part = Common.getRequestBodyText(String.valueOf(alertModel.getTime()));
        RequestBody date_int_part = Common.getRequestBodyText(String.valueOf(alertModel.getDate()));
        RequestBody alert_type_part = Common.getRequestBodyText(String.valueOf(alertModel.getAlert_type()));
        RequestBody is_alert_part = Common.getRequestBodyText(String.valueOf(alertModel.getIs_alert()));
        RequestBody inner_call_part = Common.getRequestBodyText(String.valueOf(alertModel.getIs_inner_call()));
        RequestBody outer_call_part = Common.getRequestBodyText(String.valueOf(alertModel.getIs_outer_call()));
        RequestBody is_sound_part = Common.getRequestBodyText(String.valueOf(alertModel.getIs_sound()));
        RequestBody details_part = Common.getRequestBodyText(String.valueOf(alertModel.getDetails()));
        RequestBody alert_state_part = Common.getRequestBodyText(String.valueOf(alertModel.getAlert_state()));
        RequestBody alert_time_part = Common.getRequestBodyText(String.valueOf(alertModel.getAlert_time()));


        Api.getService(Tags.base_url)
                .makeAlertWithoutSound(userModel.getToken(),alert_id_part, time_int_part, date_int_part, alert_type_part, is_alert_part, inner_call_part, outer_call_part, is_sound_part, details_part, alert_state_part, alert_time_part)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {

                            alertModel.setIsOnline(1);
                            insertNewAlert(alertModel);

                            if (share==0)
                            {

                                setResult(RESULT_OK);
                                finish();
                            }else if (share ==1)
                            {
                                shareWithContacts(alertModel.getAlert_id(), dialog);
                            }else if (share ==2)
                            {

                            }

                        } else {

                            alertModel.setIsOnline(0);
                            insertNewAlert(alertModel);
                            setResult(RESULT_OK);
                            finish();

                            if (response.code() == 500) {
                                Toast.makeText(AddNoteActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddNoteActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }

                            try {
                                Log.e("error_code", response.code() + "__" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            alertModel.setIsOnline(0);
                            insertNewAlert(alertModel);
                            setResult(RESULT_OK);
                            finish();
                            if (t.getMessage() != null) {
                                Log.e("msg_category_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(AddNoteActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddNoteActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                });

    }

    private void shareWithContacts(String local_alert_id, ProgressDialog dialog) {
        List<String> contactsIds = new ArrayList<>();
        contactsIds.add(contactModel.getId());
        ShareModel shareModel = new ShareModel(local_alert_id,contactsIds);
        Api.getService(Tags.base_url)
                .shareContacts(userModel.getToken(),shareModel)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();

                        if (response.isSuccessful())
                        {
                            setResult(RESULT_OK);
                            finish();
                        }else
                            {
                                try {
                                    Log.e("error_share",response.code()+"_"+response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                setResult(RESULT_OK);
                                finish();
                                if (response.code() == 500)
                                {
                                    Toast.makeText(AddNoteActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(AddNoteActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }

                            }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                        try {
                            dialog.dismiss();
                            setResult(RESULT_OK);
                            finish();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(AddNoteActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddNoteActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {

                        }

                    }
                });
    }

    private void initRecorder()
    {

        Calendar calendar = Calendar.getInstance();
        binding.cardView.setVisibility(View.GONE);
        isPermissionGranted = true;
        String audioName = "AUD" + calendar.getTimeInMillis()+ ".mp3";
        binding.tvName.setText(audioName);

        File folder_done = new File(Tags.local_folder_path);

        if (!folder_done.exists()) {
            folder_done.mkdir();
        }

        String path = folder_done.getAbsolutePath() + "/" + audioName;


        recorder = new MediaRecorder();
        model.setSound_path(path);
        model.setAudio_name(audioName);
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
            Log.e("Failed", "Failed");
            binding.imageWave.setVisibility(View.GONE);
            binding.cardView.setVisibility(View.GONE);

            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }
        }

    }

    private void initAudio() {
        try {


            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(model.getSound_path());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(100.0f, 100.0f);
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
            Log.e("eeeex", e.getMessage());
            binding.imageWave.setVisibility(View.GONE);
            mediaPlayer.release();
            mediaPlayer = null;
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }
            binding.cardView.setVisibility(View.GONE);

        }
    }

    private void updateProgress() {
        binding.seekBar.setProgress(mediaPlayer.getCurrentPosition());
        binding.recordDuration.setText(getDuration(mediaPlayer.getCurrentPosition()));
        handler = new Handler();
        runnable = this::updateProgress;
        handler.postDelayed(runnable, 1000);


    }

    private void checkWritePermission() {

        if (ContextCompat.checkSelfPermission(this, audio_perm) != PackageManager.PERMISSION_GRANTED) {


            isPermissionGranted = false;

            ActivityCompat.requestPermissions(this, new String[]{write_perm, audio_perm}, write_req);


        } else {
            isPermissionGranted = true;
        }
    }

    private String getDuration(long duration) {

        String total_duration = "00:00";

        if (mediaPlayer != null) {
            total_duration = String.format(Locale.ENGLISH, "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
            );


        }

        return total_duration;

    }

    private void createPasswordDialogAlert() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();

        DialogPasswordBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_password, null, false);

        binding.btnSave.setOnClickListener(v ->
                {
                    String password = binding.edtPassword.getText().toString().trim();
                    if (password.isEmpty()) {

                        binding.edtPassword.setError(getString(R.string.field_required));

                    } else if (password.length() < 6) {
                        binding.edtPassword.setError(getString(R.string.pass_short));


                    } else {
                        preferences.create_update_password(this, password);
                        binding.edtPassword.setError(null);
                        Common.CloseKeyBoard(this, binding.edtPassword);
                        save(0);
                        dialog.dismiss();
                    }
                }
        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_bg);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    private void createDatePickerDialog() {
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
        timePickerDialog.setLocale(Locale.ENGLISH);
        timePickerDialog.setMinTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100&&resultCode==RESULT_OK&&data!=null)
        {
            contactModel = (ContactModel) data.getSerializableExtra("data");
            if (alertModel.getIs_sound()==1)
            {
                saveOnlineWithSound(alertModel,1);
            }else
            {
                saveOnlineWithoutSound(alertModel,1);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == write_req && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true;
            binding.imageWave.setVisibility(View.GONE);
        }
    }

    @Override
    public void back() {
        deleteFile();
        finish();
    }

    private void deleteFile() {
        File file = new File(model.getSound_path());
        if (file.exists())
        {
            file.delete();
        }
    }

    @Override
    public void onBackPressed()
    {

        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            back();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;

        }

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
            runnable = null;
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


}
