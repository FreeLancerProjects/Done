package com.technology.circles.apps.done.local_database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class ViewModel extends AndroidViewModel {

    private AlertDatabase alertDatabase;
    private AlertDao alertDao;
    private MutableLiveData<List<AlertModel>> data;


    public ViewModel(@NonNull Application application) {
        super(application);
        alertDatabase = AlertDatabase.newInstance(application);
        alertDao = alertDatabase.getDao();
        data = new MutableLiveData<>();
    }


    public void insert(AlertModel alertModel)
    {

        new InsertAsyncTask().execute(alertModel);
    }

    public void delete(AlertModel alertModel)
    {
        new DeleteAsyncTask().execute(alertModel);
    }

    public void getAllAlertByType(int type)
    {

        new DisplayAsyncTask().execute(type);
    }

    public MutableLiveData<List<AlertModel>> getAllAlert()
    {
        return this.data;
    }

    private class InsertAsyncTask extends AsyncTask<AlertModel,Void,Long>{

        @Override
        protected Long doInBackground(AlertModel... alertModels) {


            long response = alertDao.insert(alertModels[0]);
            return response;
        }


    }


    private class DeleteAsyncTask extends AsyncTask<AlertModel,Void,Void>{

        @Override
        protected Void doInBackground(AlertModel... alertModels) {
            alertDao.delete(alertModels[0]);
            getAllAlertByType(0);
            getAllAlertByType(1);
            return null;
        }
    }


    private class DisplayAsyncTask extends AsyncTask<Integer,Void,List<AlertModel>>{

        @Override
        protected List<AlertModel> doInBackground(Integer... integers) {
            List<AlertModel> data= alertDao.getAllAlerts(integers[0]);
            return data;
        }

        @Override
        protected void onPostExecute(List<AlertModel> alertModels) {
            super.onPostExecute(alertModels);
            data.setValue(alertModels);

        }
    }


}
