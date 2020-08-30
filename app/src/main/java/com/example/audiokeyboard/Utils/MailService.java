package com.example.audiokeyboard.Utils;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import androidx.annotation.Nullable;

import java.io.File;

public class MailService extends IntentService {

    public static final String TAG = "MailUploadService";

    public MailService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        System.out.println("in sending files by email");
        File logDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Log");
        File[] logs = logDir.listFiles();
        for (File log : logs) {
            System.out.println("send mail fileName:" + log.getAbsolutePath());
            //Logger.sendEmail(log.getAbsolutePath());
        }


    }
}
