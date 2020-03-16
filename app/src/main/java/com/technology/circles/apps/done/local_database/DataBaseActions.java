package com.technology.circles.apps.done.local_database;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

public class DataBaseActions {

    private Context context;
    private AlertDatabase database;
    private AlertDao dao;
    private DatabaseInteraction interaction;

    public DataBaseActions(Context context) {
        this.context = context;
        database = AlertDatabase.newInstance(context.getApplicationContext());
        dao = database.getDao();

    }

    public void setInteraction(DatabaseInteraction interaction)
    {
        this.interaction = interaction;
    }

    public void insert(AlertModel alertModel)
    {

        new InsertAsyncTask().execute(alertModel);
    }

    public void delete(AlertModel alertModel)
    {

        new DeleteAsyncTask().execute(alertModel);
    }

    public void update(AlertModel alertModel)
    {

        new UpdateAsyncTask().execute(alertModel);
    }


    public void getAllAlertByType(int type)
    {

        new DisplayAsyncTask().execute(type);
    }

    public void displayAlertByTime(String time)
    {

        new DisplayAlertByTimeAsyncTask().execute(time);
    }

    private class InsertAsyncTask extends AsyncTask<AlertModel,Void,Long> {

        @Override
        protected Long doInBackground(AlertModel... alertModels) {


            long response = dao.insert(alertModels[0]);
            return response;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if (aLong>0)
            {
                interaction.insertedSuccess();
            }
        }
    }


    private class DeleteAsyncTask extends AsyncTask<AlertModel,Void,Void>{

        @Override
        protected Void doInBackground(AlertModel... alertModels) {
            dao.delete(alertModels[0]);
            getAllAlertByType(0);
            getAllAlertByType(1);
            return null;
        }
    }


    private class UpdateAsyncTask extends AsyncTask<AlertModel,Void,Void>{

        @Override
        protected Void doInBackground(AlertModel... alertModels) {
            dao.update(alertModels[0]);
            return null;
        }
    }


    private class DisplayAsyncTask extends AsyncTask<Integer,Void,List<AlertModel>>{

        @Override
        protected List<AlertModel> doInBackground(Integer... integers) {
            List<AlertModel> data= dao.getAllAlerts(integers[0]);
            return data;
        }

        @Override
        protected void onPostExecute(List<AlertModel> alertModels) {
            super.onPostExecute(alertModels);
            interaction.displayData(alertModels);
        }
    }


    private class DisplayAlertByTimeAsyncTask extends AsyncTask<String,Void,AlertModel>{

        @Override
        protected AlertModel doInBackground(String... strings) {
            AlertModel data= dao.getAlertByTime(strings[0]);
            return data;
        }

        @Override
        protected void onPostExecute(AlertModel alertModel) {
            super.onPostExecute(alertModel);
            interaction.displayByTime(alertModel);

        }
    }



}
