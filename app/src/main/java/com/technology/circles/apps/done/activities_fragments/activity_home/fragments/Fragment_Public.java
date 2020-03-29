package com.technology.circles.apps.done.activities_fragments.activity_home.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_home.HomeActivity;
import com.technology.circles.apps.done.activities_fragments.activity_note_details.NoteDetailsActivity;
import com.technology.circles.apps.done.adapters.AlertAdapter;
import com.technology.circles.apps.done.broadcast.AlertManager;
import com.technology.circles.apps.done.databinding.FragmentPublicePrivateBinding;
import com.technology.circles.apps.done.local_database.AlertModel;
import com.technology.circles.apps.done.local_database.DataBaseActions;
import com.technology.circles.apps.done.local_database.DatabaseInteraction;
import com.technology.circles.apps.done.local_database.DeletedAlerts;
import com.technology.circles.apps.done.models.UserModel;
import com.technology.circles.apps.done.preferences.Preferences;
import com.technology.circles.apps.done.remote.Api;
import com.technology.circles.apps.done.share.Common;
import com.technology.circles.apps.done.tags.Tags;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Public extends Fragment implements DatabaseInteraction {
    private FragmentPublicePrivateBinding binding;
    private HomeActivity activity;
    private AlertAdapter adapter;
    private List<AlertModel>alertModelList;
    private DataBaseActions dataBase;
    private Preferences preferences;
    private UserModel userModel;
    private int delete_pos = -1;


    public static Fragment_Public newInstance()
    {
        Fragment_Public fragment = new Fragment_Public();
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_publice_private,container,false);
        initView();
        return binding.getRoot();
    }

    private void initView() {

        alertModelList = new ArrayList<>();
        activity = (HomeActivity) getActivity();
        preferences = Preferences.newInstance();
        userModel = preferences.getUserData(activity);
        dataBase = new DataBaseActions(activity.getApplicationContext());
        dataBase.setInteraction(this);

        binding.progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(activity,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        binding.recView.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new AlertAdapter(alertModelList,activity,this);
        binding.recView.setAdapter(adapter);
        getAlerts();


    }

    public void getAlerts(){
        dataBase.getAllAlertByType(Tags.PUBLIC_ALERT);

    }
    public void delete(int adapterPosition, AlertModel model)
    {

        delete_pos = adapterPosition;
        deleteByLocalId(model);


    }

    private void deleteByLocalId(AlertModel model)
    {

        ProgressDialog dialog = Common.createProgressDialog(activity,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url)
                .delete(userModel.getToken(),model.getAlert_id())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            dataBase.delete(model);
                            deleteFile(model);
                            AlertManager alertManager = new AlertManager(activity.getApplicationContext());
                            alertManager.reStartAlarm();
                        }else
                        {
                            dialog.dismiss();

                            dataBase.delete(model);
                            deleteFile(model);
                            AlertManager alertManager = new AlertManager(activity.getApplicationContext());
                            alertManager.reStartAlarm();
                            dataBase.insertDeletedAlert(new DeletedAlerts(model.getAlert_id()));
                            if (response.code()==500)
                            {
                                Toast.makeText(activity, "Server Error", Toast.LENGTH_SHORT).show();
                            }else
                            {
                                Toast.makeText(activity,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            AlertManager alertManager = new AlertManager(activity.getApplicationContext());
                            alertManager.reStartAlarm();
                            dataBase.delete(model);
                            dataBase.insertDeletedAlert(new DeletedAlerts(model.getAlert_id()));
                            deleteFile(model);

                            if (t.getMessage() != null) {
                                Log.e("msg_category_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(activity, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {
                            Log.e("Error",e.getMessage()+"__");
                        }
                    }
                });
    }

    private void deleteFile(AlertModel model) {
        File file = new File(Tags.local_folder_path+"/"+model.getAudio_name());
        if (file.exists())
        {
            file.delete();
        }
    }

    public void setItemData(AlertModel model)
    {

        Intent intent = new Intent(activity, NoteDetailsActivity.class);
        intent.putExtra("data",model);
        startActivity(intent);
    }

    @Override
    public void insertedSuccess() {

    }

    @Override
    public void displayData(List<AlertModel> alertModelList) {

        binding.progBar.setVisibility(View.GONE);
        this.alertModelList.clear();
        adapter.notifyDataSetChanged();
        this.alertModelList.addAll(alertModelList);

        if (this.alertModelList.size()>0)
        {
            binding.tvNoData.setVisibility(View.GONE);

            adapter.notifyDataSetChanged();


        }else {
            binding.tvNoData.setVisibility(View.VISIBLE);

        }
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

        if (delete_pos!=-1)
        {
            try {
                alertModelList.remove(delete_pos);
                adapter.notifyItemRemoved(delete_pos);
                if (alertModelList.size()>0)
                {
                    binding.tvNoData.setVisibility(View.GONE);
                }else {
                    binding.tvNoData.setVisibility(View.VISIBLE);

                }
            }catch (Exception e){}
        }
    }


}
