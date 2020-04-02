package com.technology.circles.apps.done.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_contact.ContactsActivity;
import com.technology.circles.apps.done.databinding.ContactRowBinding;
import com.technology.circles.apps.done.models.ContactModel;

import java.util.List;

import io.paperdb.Paper;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.Holder> {

    private List<ContactModel> list;
    private Context context;
    private LayoutInflater inflater;
    private String lang;
    private ContactsActivity activity;

    public ContactAdapter(List<ContactModel> list, Context context) {
        this.list = list;
        this.context = context;
        this.activity = (ContactsActivity) context;
        inflater = LayoutInflater.from(context);
        Paper.init(context);
        lang = Paper.book().read("lang","ar");
    }

    @NonNull
    @Override
    public ContactAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContactRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.contact_row,parent,false);

        return new Holder(binding) ;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.Holder holder, int position) {
        holder.binding.setLang(lang);
        holder.binding.setModel(list.get(position));
        holder.itemView.setOnClickListener(view -> {
            activity.setItemData(list.get(holder.getAdapterPosition()));
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder{
        private ContactRowBinding binding;
        public Holder(@NonNull ContactRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
