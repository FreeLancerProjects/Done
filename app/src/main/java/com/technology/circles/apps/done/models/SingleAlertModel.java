package com.technology.circles.apps.done.models;

import java.io.Serializable;

public class SingleAlertModel implements Serializable {

    private Alert data;

    public Alert getData() {
        return data;
    }

    public static class Alert implements Serializable
    {
        private String alert_id;
        private String user_id;
        private String local_id;
        private String time_int;
        private String date_int;
        private String alert_type;
        private String is_alert;
        private String is_inner_call;
        private String is_outer_call;
        private String is_sound;
        private String details;
        private String state_alert;
        private String date_str;
        private String sound_bit;
        private String sound_file;


        public String getLocal_id() {
            return local_id;
        }

        public String getAlert_id() {
            return alert_id;
        }

        public String getUser_id() {
            return user_id;
        }

        public String getTime_int() {
            return time_int;
        }

        public String getDate_int() {
            return date_int;
        }

        public String getAlert_type() {
            return alert_type;
        }

        public String getIs_alert() {
            return is_alert;
        }

        public String getIs_inner_call() {
            return is_inner_call;
        }

        public String getIs_outer_call() {
            return is_outer_call;
        }

        public String getIs_sound() {
            return is_sound;
        }

        public String getDetails() {
            return details;
        }

        public String getState_alert() {
            return state_alert;
        }

        public String getDate_str() {
            return date_str;
        }

        public String getSound_bit() {
            return sound_bit;
        }

        public String getSound_file() {
            return sound_file;
        }

    }
}
