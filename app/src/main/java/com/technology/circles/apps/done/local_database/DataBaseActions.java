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

    public void displayAlertByState(int state)
    {

        new DisplayAlertByStateAsyncTask().execute(state);
    }
    public void displayAlertByOnline(int state)
    {

        new DisplayAlertByOnlineAsyncTask().execute(state);
    }

    public void displayAllAlert()
    {

        new DisplayAllAlertAsyncTask().execute();
    }

    public void insertAllAlert(List<AlertModel> alertModelList)
    {

        new InsertAllAlertAsyncTask().execute(alertModelList);
    }


    public void insertDeletedAlert(DeletedAlerts deletedAlerts)
    {
        new InsertDeletedAlertAsyncTask().execute(deletedAlerts);
    }

    public void deleteAllDeletedAlert()
    {
        new DeleteAllDeletedAlertAsyncTask().execute();
    }

    public void displayAllDeletedAlert()
    {
        new DisplayAllDeletedAlertAsyncTask().execute();
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
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            interaction.onDeleteSuccess();

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


    private class DisplayAlertByOnlineAsyncTask extends AsyncTask<Integer,Void,List<AlertModel>>{

        @Override
        protected List<AlertModel> doInBackground(Integer... integers) {
            List<AlertModel>  data= dao.getAllAlertsByOnline(integers[0]);
            return data;
        }

        @Override
        protected void onPostExecute(List<AlertModel> alertModelList) {
            super.onPostExecute(alertModelList);
            interaction.displayAlertsByOnline(alertModelList);

        }
    }

    private class DisplayAlertByStateAsyncTask extends AsyncTask<Integer,Void,List<AlertModel>>{

        @Override
        protected List<AlertModel> doInBackground(Integer... integers) {
            List<AlertModel>  data= dao.getAllAlertsByState(integers[0]);
            return data;
        }

        @Override
        protected void onPostExecute(List<AlertModel> alertModelList) {
            super.onPostExecute(alertModelList);
            interaction.displayAlertsByState(alertModelList);

        }
    }


    private class DisplayAllAlertAsyncTask extends AsyncTask<Void,Void,List<AlertModel>>{

        @Override
        protected List<AlertModel> doInBackground(Void... voids) {
            List<AlertModel>  data= dao.getAllAlertsData();
            return data;
        }

        @Override
        protected void onPostExecute(List<AlertModel> alertModelList) {
            super.onPostExecute(alertModelList);
            interaction.displayAllAlerts(alertModelList);

        }
    }

    private class InsertAllAlertAsyncTask extends AsyncTask<List<AlertModel>,Void,Void>{


        @Override
        protected Void doInBackground(List<AlertModel>... lists) {
            dao.insertAllData(lists[0]);
            return null;
        }
    }


    private class InsertDeletedAlertAsyncTask extends AsyncTask<DeletedAlerts,Void,Void>{

        @Override
        protected Void doInBackground(DeletedAlerts... deletedAlerts) {
            dao.insertDeletedAlert(deletedAlerts[0]);
            return null;
        }
    }

    private class DeleteAllDeletedAlertAsyncTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            dao.deleteAllDeletedAlert();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            interaction.onDeleteSuccess();
        }
    }

    private class DisplayAllDeletedAlertAsyncTask extends AsyncTask<Void,Void,List<DeletedAlerts>>{

        @Override
        protected List<DeletedAlerts> doInBackground(Void... voids) {
            List<DeletedAlerts> list = dao.displayDeletedAlerts();
            return list;
        }

        @Override
        protected void onPostExecute(List<DeletedAlerts> deletedAlertsList) {
            super.onPostExecute(deletedAlertsList);
            interaction.displayAllDeletedAlerts(deletedAlertsList);
        }
    }

}
