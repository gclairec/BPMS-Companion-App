package com.bpmsthesis.claire.servicestest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by CLAIRE on 4/1/2018.
 */

public class MyServices extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started...", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        Toast.makeText(this, "Service Destroyed...", Toast.LENGTH_LONG).show();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
