package com.technology.circles.apps.done.models;

import java.io.Serializable;
import java.util.List;

public class UsersDataModel implements Serializable {

    private List<UserModel> data;

    public List<UserModel> getData() {
        return data;
    }
}
