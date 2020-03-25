package com.technology.circles.apps.done.models;

import android.content.Context;
import android.net.Uri;
import android.util.Patterns;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.databinding.library.baseAdapters.BR;

import com.technology.circles.apps.done.R;

public class SignUpModel extends BaseObservable {

    private String name;
    private String email;
    private String phone_code;
    private String phone;
    private Uri uri;

    public ObservableField<String> error_name= new ObservableField<>();
    public ObservableField<String> error_email= new ObservableField<>();


    public SignUpModel() {
        setName("");
        setEmail("");
        setUri(null);
    }

    public boolean isDataValid(Context context)
    {
        if (!name.isEmpty() &&
                !email.isEmpty()&&
                Patterns.EMAIL_ADDRESS.matcher(email).matches()

        ){
            error_name.set(null);
            error_email.set(null);
            return true;
        }else
            {
                if (name.isEmpty())
                {
                    error_name.set(context.getString(R.string.field_required));
                }else
                    {
                        error_name.set(null);

                    }

                if (email.isEmpty())
                {
                    error_email.set(context.getString(R.string.field_required));
                }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    error_email.set(context.getString(R.string.inv_email));
                }else {
                    error_name.set(null);

                }

                return false;
            }
    }


    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);

    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getPhone_code() {
        return phone_code;
    }

    public void setPhone_code(String phone_code) {
        this.phone_code = phone_code;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
