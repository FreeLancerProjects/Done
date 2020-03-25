package com.technology.circles.apps.done.local_database;



import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.esotericsoftware.kryo.NotNull;

import java.io.Serializable;

@Entity(tableName = "alerts_table")
public class AlertModel implements Serializable {
    @NotNull
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String alert_id;
    private long time;
    private long date;
    private int alert_type;
    private int is_alert;
    private int is_inner_call;
    private int is_outer_call;
    private int is_sound;
    private String alert_time;
    private String audio_name;
    private String audio_path;
    private int alert_state;
    private int isOnline;
    private String details;


    public AlertModel() {
    }

    public AlertModel(String alert_id,long time, long date, int alert_type, int is_alert, int is_inner_call, int is_outer_call, int isOnline, String details) {
        this.alert_id = alert_id;
        this.time = time;
        this.date = date;
        this.alert_type = alert_type;
        this.is_alert = is_alert;
        this.is_inner_call = is_inner_call;
        this.is_outer_call = is_outer_call;
        this.details = details;
        this.isOnline = isOnline;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getIs_alert() {
        return is_alert;
    }

    public void setIs_alert(int is_alert) {
        this.is_alert = is_alert;
    }

    public int getIs_inner_call() {
        return is_inner_call;
    }

    public void setIs_inner_call(int is_inner_call) {
        this.is_inner_call = is_inner_call;
    }

    public int getIs_outer_call() {
        return is_outer_call;
    }

    public void setIs_outer_call(int is_outer_call) {
        this.is_outer_call = is_outer_call;
    }

    public int getIs_sound() {
        return is_sound;
    }

    public void setIs_sound(int is_sound) {
        this.is_sound = is_sound;
    }

    public String getAlert_time() {
        return alert_time;
    }

    public void setAlert_time(String alert_time) {
        this.alert_time = alert_time;
    }

    public String getAudio_name() {
        return audio_name;
    }

    public void setAudio_name(String audio_name) {
        this.audio_name = audio_name;
    }

    public int getAlert_state() {
        return alert_state;
    }

    public void setAlert_state(int alert_state) {
        this.alert_state = alert_state;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setAlert_id(String alert_id) {
        this.alert_id = alert_id;
    }

    public String getAlert_id() {
        return alert_id;
    }

    public String getAudio_path() {
        return audio_path;
    }

    public void setAudio_path(String audio_path) {
        this.audio_path = audio_path;
    }
}
