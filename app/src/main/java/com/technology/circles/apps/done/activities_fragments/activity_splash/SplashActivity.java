package com.technology.circles.apps.done.activities_fragments.activity_splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_login.LoginActivity;
import com.technology.circles.apps.done.activities_fragments.activity_sync_data.SyncDataActivity;
import com.technology.circles.apps.done.databinding.ActivitySplashBinding;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.preferences.Preferences;
import com.technology.circles.apps.done.tags.Tags;

import io.paperdb.Paper;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private Animation animation;
    private Preferences preferences;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang","ar")));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash);
        animation = AnimationUtils.loadAnimation(this,R.anim.dialog_congratulation_enter);
        preferences = Preferences.newInstance();
        binding.imageLogo.startAnimation(animation);



        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                String session = preferences.getSession(SplashActivity.this);

                if (session.equals(Tags.session_login))
                {
                    Intent intent = new Intent(SplashActivity.this, SyncDataActivity.class);
                    startActivity(intent);
                    finish();
                }else
                    {
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
