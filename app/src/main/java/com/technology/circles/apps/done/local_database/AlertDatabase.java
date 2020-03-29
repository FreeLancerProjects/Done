package com.technology.circles.apps.done.local_database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {AlertModel.class,DeletedAlerts.class},version = 1)
public abstract class AlertDatabase extends RoomDatabase {

    private static AlertDatabase instance = null;

    public abstract AlertDao getDao();

    public static synchronized AlertDatabase newInstance(Context context)
    {
        if (instance==null)
        {
            instance = Room.databaseBuilder(context.getApplicationContext(), AlertDatabase.class,"DoneAppDatabase.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }
}
