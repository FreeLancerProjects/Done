package com.technology.circles.apps.done.activities_fragments.activity_home.fragments;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.technology.circles.apps.done.databinding.FragmentPublicePrivateBinding;
import com.technology.circles.apps.done.local_database.AlertModel;
import com.technology.circles.apps.done.local_database.DataBaseActions;
import com.technology.circles.apps.done.local_database.DatabaseInteraction;
import com.technology.circles.apps.done.tags.Tags;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Public extends Fragment implements DatabaseInteraction {
    private FragmentPublicePrivateBinding binding;
    private HomeActivity activity;
    private AlertAdapter adapter;
    private List<AlertModel>alertModelList;
    private DataBaseActions dataBase;


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
    public void delete(AlertModel model) {
        dataBase.delete(model);


    }

    public void setItemData(AlertModel model) {

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



}
