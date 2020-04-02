package com.technology.circles.apps.done.models;

import java.util.List;

public class ShareModel {
    private String local_id;
    private List<String> to_user_id;

    public ShareModel(String alert_id, List<String> users_id) {
        this.local_id = alert_id;
        this.to_user_id = users_id;
    }

    public String getAlert_id() {
        return local_id;
    }

    public List<String> getUsers_id() {
        return to_user_id;
    }
}

