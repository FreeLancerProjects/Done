package com.technology.circles.apps.done.activities_fragments.activity_verification_code;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.activities_fragments.activity_home.HomeActivity;
import com.technology.circles.apps.done.databinding.ActivityVerificationCodeBinding;
import com.technology.circles.apps.done.language.LanguageHelper;
import com.technology.circles.apps.done.share.Common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class VerificationCodeActivity extends AppCompatActivity {
    private ActivityVerificationCodeBinding binding;
    private String lang;
    private String phone_code;
    private String phone;
    private CountDownTimer timer;
    private FirebaseAuth mAuth;
    private String verificationId;
    private String smsCode ;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(LanguageHelper.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDataFromIntent();
        initView();

    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent!=null)
        {
            phone_code = intent.getStringExtra("phone_code");
            phone = intent.getStringExtra("phone");

        }
    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verification_code);
        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
        binding.tvResendCode.setOnClickListener(view ->sendSmsCode());

        binding.btnConfirm.setOnClickListener(view -> {
            String code = binding.edtCode.getText().toString().trim();
            if (!code.isEmpty())
            {
                binding.edtCode.setError(null);
                Common.CloseKeyBoard(this,binding.edtCode);
                checkValidCode(code);
            }else
                {
                   binding.edtCode.setError(getString(R.string.field_required));
                }

        });
        sendSmsCode();


    }




    private void sendSmsCode() {

        startTimer();

        mAuth.setLanguageCode(lang);
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                smsCode = phoneAuthCredential.getSmsCode();
                checkValidCode(smsCode);
            }

            @Override
            public void onCodeSent(@NonNull String verification_id, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verification_id, forceResendingToken);
                VerificationCodeActivity.this.verificationId = verification_id;
            }


            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                if (e.getMessage()!=null)
                {
                    Common.CreateDialogAlert(VerificationCodeActivity.this,e.getMessage());
                }else
                    {
                        Common.CreateDialogAlert(VerificationCodeActivity.this,getString(R.string.failed));

                    }
            }
        };
        PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(
                        phone_code+phone,
                        60,
                        TimeUnit.SECONDS,
                        this,
                        mCallBack

                );


    }

    private void startTimer() {
        binding.tvResendCode.setEnabled(false);
        timer = new CountDownTimer(60*1000,  1000) {
            @Override
            public void onTick(long l) {
                SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.ENGLISH);
                String time = format.format(new Date(l));
                binding.tvTimer.setText(time);
            }

            @Override
            public void onFinish() {
                binding.tvTimer.setText("00:00");
                binding.tvResendCode.setEnabled(true);
            }
        };

        timer.start();
    }


    private void checkValidCode(String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    navigateToHomeActivity();
                }).addOnFailureListener(e -> {
                    if (e.getMessage()!=null){
                        Common.CreateDialogAlert(this,e.getMessage());
                    }else
                        {
                            Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        }
                });
    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer!=null)
        {
            timer.cancel();
        }
    }
}
