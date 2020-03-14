package com.technology.circles.apps.done.local_database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlertDao {

    @Insert
    long insert(AlertModel alertModel);

    @Delete
    void delete(AlertModel alertModel);


    @Query("SELECT * FROM alerts_table WHERE alert_type =:type ORDER BY id DESC")
    List<AlertModel> getAllAlerts(int type);

}
