package com.technology.circles.apps.done.models;

import java.io.Serializable;

public class UserModel implements Serializable {

    private String user_id;
    private String user_type;
    private String name;
    private String phone;
    private String phone_code;
    private String email;
    private String logo;
    private String available;
    private String token;

    public String getUser_id() {
        return user_id;
    }

    public String getUser_type() {
        return user_type;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhone_code() {
        return phone_code;
    }

    public String getEmail() {
        return email;
    }

    public String getLogo() {
        return logo;
    }

    public String getAvailable() {
        return available;
    }

    public String getToken() {
        return token;
    }
}
