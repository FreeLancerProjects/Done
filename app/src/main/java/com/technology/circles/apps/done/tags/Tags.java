package com.technology.circles.apps.done.tags;

import android.os.Environment;

public class Tags {

    public static final String base_url ="http://done.creativeshare.sa/";
    public static final String sound_path =base_url+"uploads/files/";
    public static final String image_path = base_url+"uploads/images/";
    public static final String local_folder_path = Environment.getExternalStorageDirectory().getAbsoluteFile()+"/Done_Audio";

    public static final String session_login = "login";
    public static final String session_logout = "logout";

    public static final int PUBLIC_ALERT = 1 ;
    public static final int PRIVATE_ALERT = 2;




}
