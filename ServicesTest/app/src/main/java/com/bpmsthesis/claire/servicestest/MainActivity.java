package com.bpmsthesis.claire.servicestest;

import android.content.Intent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startService(View view)
    {
        Intent intent = new Intent(this, MyServices.class);
        startService(intent);
    }

    public void stopService(View view)
    {
        Intent intent = new Intent(this, MyServices.class);
        stopService(intent);
    }

}
