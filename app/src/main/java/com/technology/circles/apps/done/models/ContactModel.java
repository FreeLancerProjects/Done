package com.technology.circles.apps.done.models;

import java.io.Serializable;

public class ContactModel implements Serializable {
    private String id;
    private String name;
    private String phone;
    private boolean isUseApp;
    private String image;


    public ContactModel(String name, String phone) {
        id="";
        isUseApp =false;
        image="";
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

    public boolean isUseApp() {
        return isUseApp;
    }

    public void setUseApp(boolean useApp) {
        isUseApp = useApp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
