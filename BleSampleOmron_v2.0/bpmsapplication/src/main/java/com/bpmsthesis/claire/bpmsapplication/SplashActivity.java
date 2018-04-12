package com.bpmsthesis.claire.bpmsapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashfile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        TextView tx1 = findViewById(R.id.titleTxt1);
        TextView tx2 = findViewById(R.id.titleTxt2);

        Typeface custom_font1 = Typeface.createFromAsset(getAssets(),  "fonts/Roboto-Bold.ttf");
        Typeface custom_font2 = Typeface.createFromAsset(getAssets(),  "fonts/Roboto-Regular.ttf");

        tx1.setTypeface(custom_font1);
        tx2.setTypeface(custom_font2);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            },3000);
    }
}
