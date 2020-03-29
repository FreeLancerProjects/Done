package com.technology.circles.apps.done.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_home.fragments.Fragment_Private;
import com.technology.circles.apps.done.activities_fragments.activity_home.fragments.Fragment_Public;
import com.technology.circles.apps.done.databinding.AlertRowBinding;
import com.technology.circles.apps.done.local_database.AlertModel;

import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.Holder> {

    private List<AlertModel> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment fragment;

    public AlertAdapter(List<AlertModel> list, Context context, Fragment fragment) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public AlertAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AlertRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.alert_row, parent, false);

        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertAdapter.Holder holder, int position) {
        AlertModel model = list.get(position);
        holder.binding.setModel(model);

        if (model.getIs_alert() == 1) {
            holder.binding.imageAlert.setVisibility(View.VISIBLE);
        } else {
            holder.binding.imageAlert.setVisibility(View.GONE);

        }
        if (model.getIs_inner_call() == 1) {
            holder.binding.imageMegaphone.setVisibility(View.VISIBLE);
        } else {
            holder.binding.imageMegaphone.setVisibility(View.GONE);

        }
        if (model.getIs_outer_call() == 1) {
            holder.binding.imageCall.setVisibility(View.VISIBLE);
        } else {
            holder.binding.imageCall.setVisibility(View.GONE);

        }

        holder.itemView.setOnClickListener(view -> {

            AlertModel model2 = list.get(holder.getAdapterPosition());
            if (fragment instanceof Fragment_Public)
            {
                Fragment_Public fragment_public = (Fragment_Public) fragment;
                fragment_public.setItemData(model2);
            }else if (fragment instanceof Fragment_Private)
            {
                Fragment_Private fragment_private = (Fragment_Private) fragment;
                fragment_private.setItemData(model2);

            }
        });
        holder.binding.imageDelete.setOnClickListener(view -> {
            AlertModel model2 = list.get(holder.getAdapterPosition());
            if (fragment instanceof Fragment_Public)
            {
                Fragment_Public fragment_public = (Fragment_Public) fragment;
                fragment_public.delete(holder.getAdapterPosition(),model2);
            }else if (fragment instanceof Fragment_Private)
            {
                Fragment_Private fragment_private = (Fragment_Private) fragment;
                fragment_private.delete(holder.getAdapterPosition(),model2);

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private AlertRowBinding binding;

        public Holder(@NonNull AlertRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
