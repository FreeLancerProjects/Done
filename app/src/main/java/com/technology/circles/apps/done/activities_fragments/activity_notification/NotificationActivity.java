package com.technology.circles.apps.done.activities_fragments.activity_notification;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.adapters.NotificationAdapter;
import com.technology.circles.apps.done.broadcast.AlertManager;
import com.technology.circles.apps.done.databinding.ActivityNotificationBinding;
import com.technology.circles.apps.done.interfaces.Listeners;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.local_database.AlertModel;
import com.technology.circles.apps.done.local_database.DataBaseActions;
import com.technology.circles.apps.done.local_database.DatabaseInteraction;
import com.technology.circles.apps.done.local_database.DeletedAlerts;
import com.technology.circles.apps.done.models.NotificationDataModel;
import com.technology.circles.apps.done.models.SingleAlertModel;
import com.technology.circles.apps.done.models.UserModel;
import com.technology.circles.apps.done.preferences.Preferences;
import com.technology.circles.apps.done.remote.Api;
import com.technology.circles.apps.done.share.Common;
import com.technology.circles.apps.done.tags.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity implements Listeners.BackListener, DatabaseInteraction {
    private ActivityNotificationBinding binding;
    private String lang;
    private List<NotificationDataModel.NotificationModel> notificationModelList;
    private NotificationAdapter adapter;
    private Preferences preferences;
    private UserModel userModel;
    private int current_page=1;
    private boolean isLoading=false;
    private DataBaseActions dataBaseActions;
    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification);
        initView();


    }

    private void initView() {
        dataBaseActions = new DataBaseActions(this);
        dataBaseActions.setInteraction(this);
        preferences = Preferences.newInstance();
        userModel = preferences.getUserData(this);
        notificationModelList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang", Locale.getDefault().getLanguage());
        binding.setBackListener(this);
        binding.setLang(lang);
        binding.progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this,notificationModelList);
        binding.recView.setAdapter(adapter);

        binding.recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy>0)
                {
                    int total_item = binding.recView.getAdapter().getItemCount();
                    int last_visible_item = ((LinearLayoutManager)binding.recView.getLayoutManager()).findLastCompletelyVisibleItemPosition();

                    if (total_item>=20&&(total_item-last_visible_item)==5&&!isLoading)
                    {

                        isLoading = true;
                        int page = current_page+1;
                        notificationModelList.add(null);
                        adapter.notifyItemInserted(notificationModelList.size()-1);

                        loadMore(page);
                    }
                }
            }
        });
        getNotification();

    }


    private void getNotification()
    {
        try {
            current_page = 1;
            Api.getService(Tags.base_url)
                    .getNotification(lang,userModel.getToken(),current_page,20)
                    .enqueue(new Callback<NotificationDataModel>() {
                        @Override
                        public void onResponse(Call<NotificationDataModel> call, Response<NotificationDataModel> response) {
                            binding.progBar.setVisibility(View.GONE);
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                notificationModelList.clear();
                                notificationModelList.addAll(response.body().getData());
                                if (notificationModelList.size() > 0) {

                                    adapter.notifyDataSetChanged();

                                    binding.tvNoNotification.setVisibility(View.GONE);
                                } else {
                                    binding.tvNoNotification.setVisibility(View.VISIBLE);

                                }
                            } else {
                                if (response.code() == 500) {
                                    Toast.makeText(NotificationActivity.this, "Server Error", Toast.LENGTH_SHORT).show();


                                } else {
                                    Toast.makeText(NotificationActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();

                                    try {

                                        Log.e("error", response.code() + "_" + response.errorBody().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<NotificationDataModel> call, Throwable t) {
                            try {
                                binding.progBar.setVisibility(View.GONE);

                                if (t.getMessage() != null) {
                                    Log.e("error", t.getMessage());
                                    if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                        Toast.makeText(NotificationActivity.this, R.string.something, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(NotificationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } catch (Exception e) {
                            }
                        }
                    });
        } catch (Exception e) {

        }
    }

    private void loadMore(int page)
    {
        try {

            Api.getService(Tags.base_url)
                    .getNotification(lang,userModel.getToken(),page,20)
                    .enqueue(new Callback<NotificationDataModel>() {
                        @Override
                        public void onResponse(Call<NotificationDataModel> call, Response<NotificationDataModel> response) {
                            isLoading = false;
                            notificationModelList.remove(notificationModelList.size() - 1);
                            adapter.notifyItemRemoved(notificationModelList.size() - 1);


                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {

                                int oldPos = notificationModelList.size()-1;

                                notificationModelList.addAll(response.body().getData());

                                if (response.body().getData().size() > 0) {
                                    current_page = response.body().getMeta().getCurrent_page();
                                    adapter.notifyItemRangeChanged(oldPos,notificationModelList.size()-1);

                                }
                            } else {
                                if (response.code() == 500) {
                                    Toast.makeText(NotificationActivity.this, "Server Error", Toast.LENGTH_SHORT).show();


                                } else {
                                    Toast.makeText(NotificationActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();

                                    try {

                                        Log.e("error", response.code() + "_" + response.errorBody().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<NotificationDataModel> call, Throwable t) {
                            try {

                                if (notificationModelList.get(notificationModelList.size() - 1) == null) {
                                    isLoading = false;
                                    notificationModelList.remove(notificationModelList.size() - 1);
                                    adapter.notifyItemRemoved(notificationModelList.size() - 1);

                                }

                                if (t.getMessage() != null) {
                                    Log.e("error", t.getMessage());
                                    if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                        Toast.makeText(NotificationActivity.this, R.string.something, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(NotificationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } catch (Exception e) {
                            }
                        }
                    });
        } catch (Exception e) {

        }
    }


    public void setItemAccept(NotificationDataModel.NotificationModel model, int adapterPosition) {
        accept_refuse(model,adapterPosition,"accept");
    }



    public void setItemRefuse(NotificationDataModel.NotificationModel model, int adapterPosition) {
        accept_refuse(model,adapterPosition,"refuse");

    }

    private void accept_refuse(NotificationDataModel.NotificationModel model, int adapterPosition, String action) {
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();

        Api.getService(Tags.base_url)
                .acceptRefuseAlerts(userModel.getToken(),model.getNotification_id(),model.getFrom_user_id(),action,model.getProcess_id_fk())
                .enqueue(new Callback<SingleAlertModel>() {
                    @Override
                    public void onResponse(Call<SingleAlertModel> call, Response<SingleAlertModel> response) {
                        if (response.isSuccessful()&&response.body()!=null&&response.body().getData()!=null)
                        {
                            model.setAction_type("0");
                            notificationModelList.set(adapterPosition,model);
                            adapter.notifyItemChanged(adapterPosition);
                            if (action.equals("accept"))
                            {
                                SingleAlertModel.Alert  alert = response.body().getData();
                                AlertModel alertModel = new AlertModel(alert.getLocal_id(),Long.parseLong(alert.getTime_int()),Long.parseLong(alert.getDate_int()),Integer.parseInt(alert.getAlert_type()),Integer.parseInt(alert.getIs_alert()),Integer.parseInt(alert.getIs_inner_call()),Integer.parseInt(alert.getIs_outer_call()),1,alert.getDetails());
                                if (alert.getSound_file().equals("0")||alert.getSound_file().isEmpty())
                                {
                                    alertModel.setAudio_name("");
                                    alertModel.setAudio_path("");
                                    alertModel.setIs_sound(0);

                                }else
                                    {
                                        alertModel.setIs_sound(1);
                                        alertModel.setAudio_name(alert.getSound_file());
                                        alertModel.setAudio_path("");
                                    }
                                alertModel.setAlert_time(alert.getDate_str());


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

                                dialog.dismiss();
                                AlertManager alertManager = new AlertManager(NotificationActivity.this);
                                alertManager.startNewAlarm(alertModel);
                                dataBaseActions.insert(alertModel);



                            }else
                                {
                                    dialog.dismiss();

                                }
                        }else
                            {
                                try {
                                    Log.e("error_code",response.code()+"_"+response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                                if (response.code()==500)
                                {
                                    Toast.makeText(NotificationActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                                }else
                                    {
                                        Toast.makeText(NotificationActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                    }
                            }
                    }

                    @Override
                    public void onFailure(Call<SingleAlertModel> call, Throwable t) {

                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(NotificationActivity.this, R.string.something, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(NotificationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (Exception e) {
                        }
                    }
                });
    }


    @Override
    public void back() {
        finish();
    }


    @Override
    public void insertedSuccess() {
        setResult(RESULT_OK);
        finish();
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
