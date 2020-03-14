package com.technology.circles.apps.done.activities_fragments.activity_home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_add_note.AddNoteActivity;
import com.technology.circles.apps.done.activities_fragments.activity_home.fragments.Fragment_Private;
import com.technology.circles.apps.done.activities_fragments.activity_home.fragments.Fragment_Public;
import com.technology.circles.apps.done.adapters.ViewPagerAdapter;
import com.technology.circles.apps.done.databinding.ActivityHomeBinding;
import com.technology.circles.apps.done.language.LanguageHelper;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private String lang;
    private List<Fragment> fragmentList;
    private List<String> titleList;
    private ViewPagerAdapter adapter;
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
        }
    }
}