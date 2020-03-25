package com.technology.circles.apps.done.activities_fragments.activity_sync_data;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.technology.circles.apps.done.models.MyAlertModel;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.paperdb.Paper;
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
    private int count = 1;
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

            dataBaseActions.displayAllAlert();

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == write_req && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
            dataBaseActions.displayAllAlert();

        }
    }


    private void syncData(int size) {

        Api.getService(Tags.base_url)
                .getMyAlert(userModel.getToken())
                .enqueue(new Callback<MyAlertModel>() {
                    @Override
                    public void onResponse(Call<MyAlertModel> call, Response<MyAlertModel> response) {

                        if (response.isSuccessful()&&response.body()!=null&&response.body().getData()!=null)
                        {
                            if (response.body().getData().size()>0)
                            {
                                if (size==0)
                                {
                                    downloadDataBase(response.body().getData());
                                }else
                                    {
                                        uploadLocaleAlert();
                                    }
                            }else
                                {
                                    if (size>0)
                                    {
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

    private void navigateToHomeActivity() {
        Intent  intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void uploadDataBase() {


    }

    private void downloadDataBase(List<MyAlertModel.AlertData> data) {
        total = data.size();
        binding.tvTotal.setText("("+data.size()+")");
        binding.tvCount.setText(String.valueOf(count));
        binding.progBar.setMax(total);



        for (MyAlertModel.AlertData model :data)
        {
            AlertModel alertModel = new AlertModel(model.getAlert().getLocal_id(),Long.parseLong(model.getAlert().getTime_int()),Long.parseLong(model.getAlert().getDate_int()),Integer.parseInt(model.getAlert().getAlert_type()),Integer.parseInt(model.getAlert().getIs_alert()),Integer.parseInt(model.getAlert().getIs_inner_call()),Integer.parseInt(model.getAlert().getIs_outer_call()),1,model.getAlert().getDetails());
            alertModel.setAlert_time(model.getAlert().getDate_str());



            Calendar now = Calendar.getInstance();
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


            if (now.getTimeInMillis()>calendar.getTimeInMillis())
            {
                alertModel.setAlert_state(0);
            }else
            {
                alertModel.setAlert_state(1);

            }


            if (model.getAlert().getIs_sound().equals("1"))
            {
                alertModel.setAudio_name(model.getAlert().getSound_file());
                alertModel.setIs_sound(1);

                alertModelListWithSound.add(alertModel);

            }else
            {
                alertModel.setIs_sound(0);

                alertModel.setAudio_name("");

                dataBaseActions.insert(alertModel);
                Log.e("ins1","1");

            }


        }

        if (alertModelListWithSound.size()>0)
        {
            new MyTask().execute(alertModelListWithSound.get(index));
        }

    }


    private void uploadLocaleAlert() {
        navigateToHomeActivity();
    }



    public class MyTask extends AsyncTask<AlertModel,Void,AlertModel> {
        private AlertModel alertModel;
        private File soundFile;


        @Override
        protected AlertModel doInBackground(AlertModel... alertModels) {
            alertModel = alertModels[0];
            try {
                File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/Done_Audio");
                if (!file.exists())
                {
                    file.mkdir();
                }

                soundFile = new File(file,alertModel.getAudio_name());

                URL url = new URL(Tags.sound_path+alertModel.getAudio_name());

                Log.e("url",url.toString());

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
            /*if (soundFile!=null)
            {
                soundFile.delete();
            }*/
            dataBaseActions.insert(alertModel);
            Log.e("ins2","2");
            index++;
            if (index<alertModelListWithSound.size())
            {
                new MyTask().execute(alertModelListWithSound.get(index));

            }

            Log.e("index",index+"__");



        }
    }



    @Override
    public void insertedSuccess() {
        binding.tvCount.setText(String.valueOf(count));
        binding.progBar.setProgress(count);
        count++;
        if (count>=total)
        {
            navigateToHomeActivity();
            Log.e("finished","true");
        }
        Log.e("count",count+"__");
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
    public void displayAllAlerts(List<AlertModel> alertModelList) {
        syncData(alertModelList.size());

    }

    @Override
    public void onBackPressed() {

    }
}
