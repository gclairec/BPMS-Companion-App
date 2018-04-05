/*
 * File: MainActivity.java
 *
 * Abstract: Activity class. Display the Home screen.
 *
 * Copyright (c) 2015 OMRON HEALTHCARE Co., Ltd. All rights reserved.
 */

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static boolean mAppLaunched = false;

    private Button mBLSbtn;
    private Button mWSSbtn;
    private Button mLogbtn;
    private OnClickListener mBLSbtnListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, BlsActivity.class);
            startActivity(intent);
        }
    };
    private OnClickListener mWSSbtnListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, WssActivity.class);
            startActivity(intent);
        }
    };
    private OnClickListener mLogbtnListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, LogViewActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.dMethodIn();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        AppLog.i("----------------------------------------");
        if (null != packageInfo) {
            AppLog.i("BleSampleOmron " + packageInfo.versionName + " started.");
        } else {
            AppLog.i("BleSampleOmron " + "unknown version" + " started.");
        }
        AppLog.i("Device Name: " + Build.MANUFACTURER + " " + Build.DEVICE);
        AppLog.i("OS Version: " + Build.VERSION.RELEASE);
        Date d = new Date(Build.TIME);
        AppLog.i("Build: " + d.toString());
        AppLog.i("----------------------------------------");

        super.onCreate(savedInstanceState);

        // Confirm that this device is compatible with BLE.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "This device doesn't support BLE.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Confirm that this device is compatible with Bluetooth.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "This device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

//        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        if (! mBtAdapter.isEnabled()) {
//            mBtAdapter.enable();
//        }

        // Confirm that Bluetooth is working on this device.
//        if (!bluetoothAdapter.isEnabled()) {
//            Toast.makeText(this, "Bluetooth isn't working on this device.\nPlease enable Bluetooth.", Toast.LENGTH_LONG).show();
//        }

        setContentView(R.layout.activity_main);

        TextView textViewVersion = (TextView) findViewById(R.id.textViewVersion);
        if (null != packageInfo) {
            textViewVersion.setText(packageInfo.versionName);
        } else {
            textViewVersion.setText("unknown version");
        }
        mBLSbtn = (Button) findViewById(R.id.btnBLS);
        mBLSbtn.setOnClickListener(mBLSbtnListener);
        mWSSbtn = (Button) findViewById(R.id.btnWSS);
        mWSSbtn.setOnClickListener(mWSSbtnListener);
        mLogbtn = (Button) findViewById(R.id.btnLog);
        mLogbtn.setOnClickListener(mLogbtnListener);

        // Clear history data
        if (!mAppLaunched) {
            HistoryData hd = (HistoryData) this.getApplication();
            for (int i = 0; i < HistoryData.SERV_MAX; i++) {
                hd.clear(i);
            }
            hd.save();
        }
        mAppLaunched = true;

        startService(new Intent(this, LogViewService.class));
    }

    @Override
    protected void onDestroy() {
        AppLog.dMethodIn();
        super.onDestroy();
        stopService(new Intent(this, LogViewService.class));
    }
}
