package com.technology.circles.apps.done.activities_fragments.activity_contact;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.adapters.ContactAdapter;
import com.technology.circles.apps.done.databinding.ActivityContactsBinding;
import com.technology.circles.apps.done.interfaces.Listeners;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.models.ContactModel;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class ContactsActivity extends AppCompatActivity implements Listeners.BackListener {

    private ActivityContactsBinding binding;
    private List<ContactModel> contactModelList;
    private final String contact_perm = Manifest.permission.READ_CONTACTS;
    private final int contact_req = 100;
    private ContactAdapter adapter;
    private String lang;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contacts);
        initView();
    }

    private void initView() {
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        contactModelList = new ArrayList<>();
        binding.setBackListener(this);
        binding.setLang(lang);
        binding.progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(contactModelList,this);
        binding.recView.setAdapter(adapter);

        checkPermission();
    }

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(this,contact_perm)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{contact_perm},contact_req);
        }else
            {
                getContacts();
            }
    }

    private void getContacts() {
        String [] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY, ContactsContract.CommonDataKinds.Phone.NUMBER};
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,projection,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (cursor!=null&&cursor.moveToFirst())
        {
            while (cursor.moveToNext())
            {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                ContactModel contactModel = new ContactModel(name,number);
                contactModelList.add(contactModel);
            }


        }
        if (contactModelList.size()>0)
        {
            binding.setCount(contactModelList.size());
            binding.progBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();

        }else
            {
                binding.progBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                binding.setCount(contactModelList.size());
            }



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==contact_req)
        {
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                getContacts();
            }else
                {
                    Toast.makeText(this, R.string.cnt_access_cont, Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void back() {
        finish();
    }
}
