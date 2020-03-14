package com.technology.circles.apps.done.interfaces;



public interface Listeners {

    interface BackListener
    {
        void back();
    }

    interface SkipListener
    {
        void skip();
    }

    interface CreateAccountListener
    {
        void createNewAccount();
    }
    interface ShowCountryDialogListener
    {
        void showDialog();
    }

    interface SettingActionListener
    {
        void contact();
        void changeLanguage();
        void about();
        void rateApp();
        void terms();
        void logout();
    }





}
