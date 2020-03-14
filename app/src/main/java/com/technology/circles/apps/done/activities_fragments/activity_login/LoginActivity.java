package com.technology.circles.apps.done.activities_fragments.activity_login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.listeners.OnCountryPickerListener;
import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_home.HomeActivity;
import com.technology.circles.apps.done.databinding.ActivityLoginBinding;
import com.technology.circles.apps.done.interfaces.Listeners;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.share.Common;

import java.util.Locale;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity implements OnCountryPickerListener, Listeners.ShowCountryDialogListener {

    private ActivityLoginBinding binding;
    private String lang;
    private CountryPicker picker;
    private String code;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setShowListener(this);
        createCountryDialog();

        binding.edtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.toString().startsWith("0"))
                {
                    binding.edtPhone.setText("");
                }
            }
        });
        binding.btnSendCode.setOnClickListener(view -> {

            String phone = binding.edtPhone.getText().toString().trim();

            if (!phone.isEmpty())
            {
                binding.edtPhone.setError(null);
                Common.CloseKeyBoard(this,binding.edtPhone);
                navigateToVerificationActivity(phone);
            }else
                {
                    binding.edtPhone.setError(getString(R.string.field_required));

                }

        });
    }

    private void navigateToVerificationActivity(String phone) {

        /*Intent intent = new Intent(this, VerificationCodeActivity.class);
        intent.putExtra("phone_code",code);
        intent.putExtra("phone",phone);
        startActivity(intent);
        finish();*/

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    private void createCountryDialog() {
        picker = new CountryPicker.Builder()
                .canSearch(true)
                .with(this)
                .listener(this)
                .sortBy(CountryPicker.SORT_BY_NAME)
                .theme(CountryPicker.THEME_NEW)
                .build();

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        try {
            if (picker.getCountryFromSIM() != null) {
                updatePhoneCode(picker.getCountryFromSIM());
            } else if (telephonyManager != null && picker.getCountryByISO(telephonyManager.getNetworkCountryIso()) != null) {
                updatePhoneCode(picker.getCountryByISO(telephonyManager.getNetworkCountryIso()));
            } else if (picker.getCountryByLocale(Locale.getDefault()) != null) {
                updatePhoneCode(picker.getCountryByLocale(Locale.getDefault()));
            } else {
                code = "+966";
                binding.tvCode.setText(code);

            }
        } catch (Exception e) {
            code = "+966";
            binding.tvCode.setText(code);
        }

    }

    @Override
    public void onSelectCountry(Country country) {
        updatePhoneCode(country);
    }

    private void updatePhoneCode(Country country) {
        code = country.getDialCode();
        binding.tvCode.setText(country.getDialCode());
        binding.flag.setImageResource(country.getFlag());


    }


    @Override
    public void showDialog() {
        picker.showBottomSheet(this);
    }
}
