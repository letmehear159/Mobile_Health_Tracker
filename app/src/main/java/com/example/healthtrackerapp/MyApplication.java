package com.example.healthtrackerapp;


import android.app.Application;
import com.example.healthtrackerapp.manager.CloudinaryManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CloudinaryManager.init(this);
    }
}