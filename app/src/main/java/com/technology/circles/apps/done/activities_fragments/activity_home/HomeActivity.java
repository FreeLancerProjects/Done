package com.technology.circles.apps.done.activities_fragments.activity_home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_add_note.AddNoteActivity;
import com.technology.circles.apps.done.activities_fragments.activity_home.fragments.Fragment_Private;
import com.technology.circles.apps.done.activities_fragments.activity_home.fragments.Fragment_Public;
import com.technology.circles.apps.done.activities_fragments.activity_login.LoginActivity;
import com.technology.circles.apps.done.activities_fragments.activity_notification.NotificationActivity;
import com.technology.circles.apps.done.activities_fragments.activity_profile.ProfileActivity;
import com.technology.circles.apps.done.activities_fragments.activity_setting.SettingActivity;
import com.technology.circles.apps.done.adapters.ViewPagerAdapter;
import com.technology.circles.apps.done.databinding.ActivityHomeBinding;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.models.NotificationCount;
import com.technology.circles.apps.done.models.UserModel;
import com.technology.circles.apps.done.preferences.Preferences;
import com.technology.circles.apps.done.remote.Api;
import com.technology.circles.apps.done.tags.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity  {

    private ActivityHomeBinding binding;
    private String lang;
    private List<Fragment> fragmentList;
    private List<String> titleList;
    private ViewPagerAdapter adapter;
    private Preferences preferences;
    private UserModel userModel;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initView();

    }

    private void initView() {
        preferences = Preferences.newInstance();
        userModel = preferences.getUserData(this);
        fragmentList = new ArrayList<>();
        titleList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang","ar");

        fragmentList.add(Fragment_Public.newInstance());
        fragmentList.add(Fragment_Private.newInstance());

        titleList.add(getString(R.string.pub));
        titleList.add(getString(R.string.pers));

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragments(fragmentList);
        adapter.addTitle(titleList);

        binding.tab.setupWithViewPager(binding.pager);
        binding.pager.setAdapter(adapter);
        binding.pager.setOffscreenPageLimit(fragmentList.size());
        binding.fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddNoteActivity.class);
            startActivityForResult(intent,100);
        });
        binding.imageProfile.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
        binding.imageSetting.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivityForResult(intent,200);
        });

        binding.flNotification.setOnClickListener(view -> {
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);
            readNotification();
        });

        getNotificationCount();

    }


    private void getNotificationCount() {
        Api.getService(Tags.base_url).
                getUnreadNotificationCount(lang,userModel.getToken()).
                enqueue(new Callback<NotificationCount>() {
                    @Override
                    public void onResponse(Call<NotificationCount> call, Response<NotificationCount> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            binding.setCount(response.body().getCount());
                        } else {
                            try {

                                Log.e("error", response.code() + "_" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (response.code() == 500) {
                                Toast.makeText(HomeActivity.this, "Server Error", Toast.LENGTH_SHORT).show();


                            } else {
                                Toast.makeText(HomeActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();


                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<NotificationCount> call, Throwable t) {

                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(HomeActivity.this, R.string.something, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(HomeActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (Exception e) {
                        }



                    }
                });
    }

    private void readNotification() {
        Api.getService(Tags.base_url).
                readNotification(userModel.getToken()).
                enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            binding.setCount(0);
                        } else {
                            try {

                                Log.e("error", response.code() + "_" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (response.code() == 500) {
                                Toast.makeText(HomeActivity.this, "Server Error", Toast.LENGTH_SHORT).show();


                            } else {
                                Toast.makeText(HomeActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();


                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(HomeActivity.this, R.string.something, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(HomeActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (Exception e) {
                        }



                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==100&&resultCode==RESULT_OK)
        {
            Fragment_Public fragment_public = (Fragment_Public) adapter.getItem(0);
            Fragment_Private fragment_private = (Fragment_Private) adapter.getItem(1);
            fragment_public.getAlerts();
            fragment_private.getAlerts();
        }else if (requestCode ==200&&resultCode==RESULT_OK)
        {
            preferences.clear(this);
            userModel = null;
            navigateToSignInActivity();
        }
    }

    private void navigateToSignInActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }





}
