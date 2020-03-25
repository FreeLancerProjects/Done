package com.technology.circles.apps.done.activities_fragments.activity_setting;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_profile.ProfileActivity;
import com.technology.circles.apps.done.activities_fragments.activity_about_app.AboutAppActivity;
import com.technology.circles.apps.done.databinding.ActivitySettingBinding;
import com.technology.circles.apps.done.interfaces.Listeners;
import com.technology.circles.apps.done.language.LanguageHelper;

import io.paperdb.Paper;

public class SettingActivity extends AppCompatActivity implements Listeners.BackListener ,Listeners.SettingActionListener{

    private ActivitySettingBinding binding;
    private String lang;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);
        initView();
    }

    private void initView() {
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setBackListener(this);
        binding.setAction(this);
        binding.setLang(lang);
    }

    @Override
    public void back() {
        finish();
    }

    @Override
    public void profile() {

        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void contact() {

    }

    @Override
    public void changeLanguage() {

    }

    @Override
    public void share() {

        String app_url = "https://play.google.com/store/apps/details?id=" + getPackageName();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "Done App ");
        intent.putExtra(Intent.EXTRA_TEXT, app_url);
        startActivity(intent);

    }

    @Override
    public void about() {
        Intent intent = new Intent(this, AboutAppActivity.class);
        intent.putExtra("type",2);
        startActivity(intent);
    }

    @Override
    public void rateApp() {

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));

        }
    }

    @Override
    public void terms() {

        Intent intent = new Intent(this, AboutAppActivity.class);
        intent.putExtra("type",1);
        startActivity(intent);
    }

    @Override
    public void logout() {
        setResult(RESULT_OK);
        finish();
    }
}
