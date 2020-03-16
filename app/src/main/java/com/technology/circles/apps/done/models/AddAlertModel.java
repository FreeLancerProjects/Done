package com.technology.circles.apps.done.models;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.databinding.library.baseAdapters.BR;

import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.tags.Tags;

import java.io.Serializable;

public class AddAlertModel extends BaseObservable implements Serializable {

    private long time;
    private long date;
    private int alert_type;
    private int is_local;
    private boolean isAlert;
    private boolean isInnerCall;
    private boolean isOuterCall;
    private String details;
    private String sound_path;
    private String audio_name;

    public ObservableField<String> error_details = new ObservableField<>();


    public boolean isDataValid(Context context) {

        Log.e("time", time + "_");
        Log.e("date", date + "_");
        Log.e("details", details + "_");

        Log.e("islocal", is_local + "_");
        Log.e("alert", isAlert + "_");
        Log.e("out", isOuterCall + "_");
        Log.e("inner", isInnerCall + "_");

        if (time != 0 &&
                date != 0 &&
                alert_type != 0 &&
                is_local != 0 &&
                !details.isEmpty() &&
                (isOuterCall || isInnerCall || isAlert)
        ) {

            error_details.set(null);
            if (isOuterCall()) {

                if (sound_path.isEmpty())
                {

                    Toast.makeText(context, R.string.rec_sound, Toast.LENGTH_SHORT).show();
                    return false;
                }else
                    {
                        return true;


                    }
            } else {

                return true;

            }

        } else {

            if (time == 0) {
                Toast.makeText(context, R.string.ch_time, Toast.LENGTH_SHORT).show();
            }

            if (date == 0) {
                Toast.makeText(context, R.string.ch_date, Toast.LENGTH_SHORT).show();
            }

            if (details.isEmpty()) {
                error_details.set(context.getString(R.string.field_required));

            } else {
                error_details.set(null);
            }

            if (!isAlert && !isInnerCall && !isOuterCall) {
                Toast.makeText(context, R.string.ch_alert_type, Toast.LENGTH_SHORT).show();
            }

            return false;
        }
    }

    public AddAlertModel() {

        setTime(0);
        setDate(0);
        setAlert_type(Tags.PUBLIC_ALERT);
        setIs_local(1);
        setAlert(true);
        setInnerCall(false);
        setOuterCall(false);
        setDetails("");
        setSound_path("");
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getAlert_type() {
        return alert_type;
    }

    public void setAlert_type(int alert_type) {
        this.alert_type = alert_type;
    }

    public int getIs_local() {
        return is_local;
    }

    public void setIs_local(int is_local) {
        this.is_local = is_local;
    }

    public boolean isAlert() {
        return isAlert;
    }

    public void setAlert(boolean alert) {
        isAlert = alert;
    }

    public boolean isInnerCall() {
        return isInnerCall;
    }

    public void setInnerCall(boolean innerCall) {
        isInnerCall = innerCall;
    }

    public boolean isOuterCall() {
        return isOuterCall;
    }

    public void setOuterCall(boolean outerCall) {
        isOuterCall = outerCall;
    }

    @Bindable
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
        notifyPropertyChanged(BR.details);
    }

    public String getSound_path() {
        return sound_path;
    }

    public void setSound_path(String sound_path) {
        this.sound_path = sound_path;
    }

    public String getAudio_name() {
        return audio_name;
    }

    public void setAudio_name(String audio_name) {
        this.audio_name = audio_name;
    }
}
