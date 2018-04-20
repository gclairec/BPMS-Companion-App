package jp.co.omron.healthcare.sampleapps.ble.blesampleomron.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.BlsActivity;
import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.R;

public class SplashActivity extends Activity {

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashfile);

        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPref = getSharedPreferences( "userInfo", Context
                        .MODE_PRIVATE);

                String _patID;

                _patID = sharedPref.getString("patientID", "");

                if(_patID == "")
                {
                    Intent intent = new Intent(SplashActivity.this, QRCodeScanActivity.class);
                    startActivity(intent);
                }else
                {
                    Intent intent = new Intent(SplashActivity.this, BlsActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        },3000);

    }
}
