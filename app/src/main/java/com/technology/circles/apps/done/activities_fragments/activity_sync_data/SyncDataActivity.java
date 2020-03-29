package com.technology.circles.apps.done.activities_fragments.activity_sync_data;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_home.HomeActivity;
import com.technology.circles.apps.done.databinding.ActivitySyncDataBinding;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.local_database.AlertModel;
import com.technology.circles.apps.done.local_database.DataBaseActions;
import com.technology.circles.apps.done.local_database.DatabaseInteraction;
import com.technology.circles.apps.done.local_database.DeletedAlerts;
import com.technology.circles.apps.done.models.MyAlertModel;
import com.technology.circles.apps.done.models.UserModel;
import com.technology.circles.apps.done.preferences.Preferences;
import com.technology.circles.apps.done.remote.Api;
import com.technology.circles.apps.done.share.Common;
import com.technology.circles.apps.done.tags.Tags;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncDataActivity extends AppCompatActivity implements DatabaseInteraction {
    private ActivitySyncDataBinding binding;
    private DataBaseActions dataBaseActions;
    private final String write_perm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final int write_req = 100;
    private Preferences preferences;
    private UserModel userModel;
    private List<AlertModel> alertModelListWithSound;
    private List<AlertModel> alertModelList;
    private List<DeletedAlerts> deletedAlertsList;
    private int count = 0;
    private int total=0;
    private int index = 0;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sync_data);
        initView();
    }

    private void initView() {
        deletedAlertsList = new ArrayList<>();
        alertModelList = new ArrayList<>();
        alertModelListWithSound = new ArrayList<>();
        preferences = Preferences.newInstance();
        userModel = preferences.getUserData(this);
        dataBaseActions = new DataBaseActions(this);
        dataBaseActions.setInteraction(this);
        checkWritePermission();
    }


    private void checkWritePermission() {

        if (ContextCompat.checkSelfPermission(this, write_perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{write_perm}, write_req);

        } else {
            dataBaseActions.displayAllDeletedAlert();

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == write_req && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
            dataBaseActions.displayAllDeletedAlert();
        }
    }


    private void syncData(List<AlertModel> data) {
        alertModelList = data;
        Api.getService(Tags.base_url)
                .getMyAlert(userModel.getToken())
                .enqueue(new Callback<MyAlertModel>() {
                    @Override
                    public void onResponse(Call<MyAlertModel> call, Response<MyAlertModel> response) {

                        if (response.isSuccessful()&&response.body()!=null&&response.body().getData()!=null)
                        {
                            if (response.body().getData().size()>0)
                            {
                                if (data.size()==0)
                                {
                                    downloadDataBase(response.body().getData());

                                    Log.e("down","1");
                                }else
                                    {
                                        index = 0;
                                        total = data.size();
                                        count = 0;


                                        Log.e("local_upload","2");



                                        uploadLocaleAlert();


                                    }
                            }else
                                {
                                    if (data.size()>0)
                                    {
                                        index = 0;
                                        total = data.size();
                                        count = 0;
                                        binding.progBar.setMax(data.size());
                                        binding.progBar.setProgress(count);
                                        binding.tvCount.setText(String.valueOf(count));
                                        binding.tvTotal.setText("("+total+")");
                                        binding.tvState.setText(R.string.uploading);
                                        Log.e("database_upload","3");

                                        uploadDataBase();
                                    }else
                                        {
                                            navigateToHomeActivity();
                                        }
                                }

                        }else
                            {
                                navigateToHomeActivity();
                                if (response.code()==500)
                                {
                                    Toast.makeText(SyncDataActivity.this,"Server Error", Toast.LENGTH_SHORT).show();

                                }else
                                    {
                                        Toast.makeText(SyncDataActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                    }
                            }

                    }

                    @Override
                    public void onFailure(Call<MyAlertModel> call, Throwable t) {
                        try {
                            navigateToHomeActivity();
                            if (t.getMessage() != null) {
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SyncDataActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SyncDataActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void uploadDataBase() {

        if (index<alertModelList.size())
        {
            AlertModel alertModel = alertModelList.get(index);
            String sound_path = alertModel.getAudio_path();

            if (sound_path!=null&&!sound_path.isEmpty())
            {
                File file = new File(sound_path);
                if (file.exists())
                {
                    uploadWithSound(alertModel);
                }else
                {
                    alertModel.setAudio_name("");
                    alertModel.setAudio_path("");
                    alertModel.setIs_sound(0);
                    alertModel.setIs_inner_call(0);
                    alertModel.setIs_outer_call(0);
                    uploadWithoutSound(alertModel);
                }
            }else
            {
                uploadWithoutSound(alertModel);

            }

        }else
            {

                navigateToHomeActivity();
            }




    }

    private void uploadWithSound(AlertModel alertModel) {
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
        MultipartBody.Part sound_file_part = Common.getMultiPartSound(alertModel.getAudio_path(), "sound_file");


        Api.getService(Tags.base_url)
                .makeAlertWithSound(userModel.getToken(),alert_id_part, time_int_part, date_int_part, alert_type_part, is_alert_part, inner_call_part, outer_call_part, is_sound_part, details_part, alert_state_part, alert_time_part, sound_file_part)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            index++;
                            binding.tvCount.setText(String.valueOf(count));
                            binding.progBar.setProgress(count);
                            count++;
                            binding.tvCount.setText(String.valueOf(count));
                            binding.progBar.setProgress(count);
                            alertModel.setIsOnline(1);
                            dataBaseActions.update(alertModel);

                            uploadDataBase();

                        } else {

                            navigateToHomeActivity();

                            if (response.code() == 500) {
                                Toast.makeText(SyncDataActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SyncDataActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
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
                            navigateToHomeActivity();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SyncDataActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SyncDataActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void uploadWithoutSound(AlertModel alertModel) {

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
                        if (response.isSuccessful()) {

                            index++;
                            binding.progBar.setProgress(count);
                            binding.tvCount.setText(String.valueOf(count));
                            count++;
                            binding.tvCount.setText(String.valueOf(count));
                            binding.progBar.setProgress(count);
                            alertModel.setIsOnline(1);
                            dataBaseActions.update(alertModel);

                            uploadDataBase();

                        } else {

                            navigateToHomeActivity();
                            if (response.code() == 500) {
                                Toast.makeText(SyncDataActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SyncDataActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
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
                            navigateToHomeActivity();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SyncDataActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SyncDataActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void downloadDataBase(List<MyAlertModel.AlertData> data) {
        binding.tvState.setText(getString(R.string.downloading));
        total = data.size();
        binding.tvTotal.setText("("+data.size()+")");
        binding.tvCount.setText(String.valueOf(count));
        binding.progBar.setMax(total);
        binding.progBar.setProgress(count);

        for (MyAlertModel.AlertData model :data)
        {
            AlertModel alertModel = new AlertModel(model.getAlert().getLocal_id(),Long.parseLong(model.getAlert().getTime_int()),Long.parseLong(model.getAlert().getDate_int()),Integer.parseInt(model.getAlert().getAlert_type()),Integer.parseInt(model.getAlert().getIs_alert()),Integer.parseInt(model.getAlert().getIs_inner_call()),Integer.parseInt(model.getAlert().getIs_outer_call()),1,model.getAlert().getDetails());
            alertModel.setAlert_time(model.getAlert().getDate_str());



            Calendar now = Calendar.getInstance();
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

            calendar.set(Calendar.DAY_OF_MONTH, calendarDate.get(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.MONTH, calendarDate.get(Calendar.MONTH));
            calendar.set(Calendar.YEAR, calendarDate.get(Calendar.YEAR));
            calendar.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));



            if (now.getTime().after(calendar.getTime()))
            {
                alertModel.setAlert_state(1);
            }else
            {
                alertModel.setAlert_state(0);

            }



            if (model.getAlert().getIs_sound().equals("1")&&!model.getAlert().getSound_file().isEmpty()&&!model.getAlert().getSound_file().equals("0"))
            {

                alertModel.setAudio_name(model.getAlert().getSound_file());
                alertModel.setIs_sound(1);
                alertModelListWithSound.add(alertModel);

            }else
            {
                alertModel.setIs_sound(0);
                alertModel.setAudio_path("");
                alertModel.setIs_inner_call(0);
                alertModel.setIs_outer_call(0);
                alertModel.setAudio_name("");


                dataBaseActions.insert(alertModel);
            }


        }

        if (alertModelListWithSound.size()>0)
        {
            new MyTask().execute(alertModelListWithSound.get(index));
        }

    }


    private void uploadLocaleAlert() {
        dataBaseActions.displayAlertByOnline(0);
    }

    private void navigateToHomeActivity() {
        Intent  intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    public class MyTask extends AsyncTask<AlertModel,Void,AlertModel>
    {
        private AlertModel alertModel;
        private File soundFile;


        @Override
        protected AlertModel doInBackground(AlertModel... alertModels) {
            alertModel = alertModels[0];
            try {
                File file = new File(Tags.local_folder_path);
                if (!file.exists())
                {
                    file.mkdir();
                }

                soundFile = new File(file,alertModel.getAudio_name());

                URL url = new URL(Tags.sound_path+alertModel.getAudio_name());


                URLConnection connection = url.openConnection();
                DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(soundFile));

                byte []  data = new byte[connection.getContentLength()];


                int count;
                while ((count = dataInputStream.read(data))!=-1)
                {
                    dataOutputStream.write(data,0,count);

                }
                dataOutputStream.flush();
                alertModel.setAudio_path(soundFile.getAbsolutePath());
                dataInputStream.close();
                dataOutputStream.close();


            } catch (MalformedURLException e) {
                Log.e("e1",e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("e2",e.getMessage());

                e.printStackTrace();
            }

            return alertModel;
        }

        @Override
        protected void onPostExecute(AlertModel alertModel) {

            dataBaseActions.insert(alertModel);
            index++;
            if (index<alertModelListWithSound.size())
            {
                new MyTask().execute(alertModelListWithSound.get(index));

            }




        }
    }

    @Override
    public void insertedSuccess() {

        count++;
        binding.tvCount.setText(String.valueOf(count));
        binding.progBar.setProgress(count);
        if (count==total)
        {

            navigateToHomeActivity();

        }
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
        if (alertModelList!=null&&alertModelList.size()>0)
        {
            this.alertModelList = alertModelList;

            count = 0;
            total = alertModelList.size();
            binding.progBar.setMax(total);
            binding.progBar.setProgress(count);
            binding.tvCount.setText(String.valueOf(count));
            binding.tvTotal.setText("("+total+")");
            binding.tvState.setText(R.string.uploading);

            uploadDataBase();
        }else {
           navigateToHomeActivity();
        }

    }

    private void deleteAlert()
    {

        if (count<deletedAlertsList.size())
        {
            String id = deletedAlertsList.get(count).getAlert_id();
            Api.getService(Tags.base_url)
                    .delete(userModel.getToken(),id)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()&&response.body()!=null)
                            {
                                count++;
                                binding.progBar.setProgress(count);
                                binding.tvCount.setText(String.valueOf(count));
                                deleteAlert();
                            }else
                            {
                                if (response.code()==500)
                                {
                                    Toast.makeText(SyncDataActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                                }else if (response.code()==422){
                                    count++;
                                    binding.progBar.setProgress(count);
                                    binding.tvCount.setText(String.valueOf(count));
                                    deleteAlert();
                                }else
                                {
                                    Toast.makeText(SyncDataActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            try {
                                navigateToHomeActivity();

                                if (t.getMessage() != null) {
                                    Log.e("msg_category_error", t.getMessage() + "__");

                                    if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                        Toast.makeText(SyncDataActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SyncDataActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }catch (Exception e)
                            {
                                Log.e("Error",e.getMessage()+"__");
                            }
                        }
                    });
        }else
            {
                dataBaseActions.deleteAllDeletedAlert();

            }


    }




    @Override
    public void displayAllAlerts(List<AlertModel> alertModelList) {
        syncData(alertModelList);
    }

    @Override
    public void displayAllDeletedAlerts(List<DeletedAlerts> deletedAlertsList) {

        this.deletedAlertsList.addAll(deletedAlertsList);
        if (deletedAlertsList.size()>0)
        {
            total = deletedAlertsList.size();
            count = 0;
            binding.progBar.setMax(total);
            binding.progBar.setProgress(count);
            binding.tvCount.setText(String.valueOf(count));
            binding.tvTotal.setText("("+total+")");
            binding.tvState.setText(R.string.deleting);
            deleteAlert();
        }else {
            dataBaseActions.displayAllAlert();
        }

    }

    @Override
    public void onDeleteSuccess() {
        deletedAlertsList.clear();
        dataBaseActions.displayAllAlert();


    }

    @Override
    public void onBackPressed() {

    }
}
