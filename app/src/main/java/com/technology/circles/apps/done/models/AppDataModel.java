package com.technology.circles.apps.done.models;

import java.io.Serializable;

public class AppDataModel implements Serializable {
    private Setting settings;

    public Setting getSettings() {
        return settings;
    }

    public static class Setting implements Serializable
    {
        private String website;
        private String ar_address;
        private String en_address;
        private String phones;
        private String emails;
        private String ar_about;
        private String en_about;
        private String logo;
        private String facebook;
        private String twitter;
        private String instagram;
        private String linkedin;
        private String whatsapp;
        private String ar_termis_condition;
        private String en_termis_condition;

        public String getWebsite() {
            return website;
        }

        public String getAr_address() {
            return ar_address;
        }

        public String getEn_address() {
            return en_address;
        }

        public String getPhones() {
            return phones;
        }

        public String getEmails() {
            return emails;
        }

        public String getAr_about() {
            return ar_about;
        }

        public String getEn_about() {
            return en_about;
        }

        public String getLogo() {
            return logo;
        }

        public String getFacebook() {
            return facebook;
        }

        public String getTwitter() {
            return twitter;
        }

        public String getInstagram() {
            return instagram;
        }

        public String getLinkedin() {
            return linkedin;
        }

        public String getWhatsapp() {
            return whatsapp;
        }

        public String getAr_termis_condition() {
            return ar_termis_condition;
        }

        public String getEn_termis_condition() {
            return en_termis_condition;
        }
    }
}
