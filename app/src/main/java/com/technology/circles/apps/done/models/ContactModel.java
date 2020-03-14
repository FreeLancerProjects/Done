package com.technology.circles.apps.done.models;

import java.io.Serializable;

public class ContactModel implements Serializable {
    private String name;
    private String phone;

    public ContactModel() {
    }

    public ContactModel(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
