package com.technology.circles.apps.done.local_database;

import java.util.List;

public interface DatabaseInteraction {

    void insertedSuccess();
    void displayData(List<AlertModel> alertModelList);
    void displayByTime(AlertModel alertModel);
    void displayAlertsByState(List<AlertModel> alertModelList);
    void displayAllAlerts(List<AlertModel> alertModelList);


}
