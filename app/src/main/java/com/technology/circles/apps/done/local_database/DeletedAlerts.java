package com.technology.circles.apps.done.local_database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "deleted_alerts")
public class DeletedAlerts implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String alert_id;

    public DeletedAlerts(String alert_id) {
        this.alert_id = alert_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAlert_id() {
        return alert_id;
    }

    public void setAlert_id(String alert_id) {
        this.alert_id = alert_id;
    }
}
