package com.example.madkomatapp;

import android.app.Application;
import android.content.Intent;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;

public class MadkomatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Network service
        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));
    }
}
