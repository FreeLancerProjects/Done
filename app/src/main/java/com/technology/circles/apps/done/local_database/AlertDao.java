package com.technology.circles.apps.done.local_database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlertDao {

    @Insert
    long insert(AlertModel alertModel);

    @Delete
    void delete(AlertModel alertModel);

    @Update
    void update(AlertModel alertModel);


    @Query("SELECT * FROM alerts_table WHERE alert_type =:type ORDER BY id DESC")
    List<AlertModel> getAllAlerts(int type);

    @Query("SELECT * FROM alerts_table WHERE alert_time LIKE :time LIMIT 1")
    AlertModel getAlertByTime(String time);

    @Query("SELECT * FROM alerts_table WHERE alert_state =:type ORDER BY id ASC")
    List<AlertModel> getAllAlertsByState(int type);

    @Query("SELECT * FROM alerts_table WHERE isOnline =:state ORDER BY id ASC")
    List<AlertModel> getAllAlertsByOnline(int state);

    @Query("SELECT * FROM alerts_table")
    List<AlertModel> getAllAlertsData();

    @Insert
    void insertAllData(List<AlertModel> alertModel);

    @Insert
    void insertDeletedAlert(DeletedAlerts deletedAlerts);

    @Query("SELECT * FROM deleted_alerts")
    List<DeletedAlerts> displayDeletedAlerts();

    @Query("DELETE FROM deleted_alerts")
    void deleteAllDeletedAlert();

    @Query("DELETE FROM alerts_table")
    void deleteAllAlert();

}
