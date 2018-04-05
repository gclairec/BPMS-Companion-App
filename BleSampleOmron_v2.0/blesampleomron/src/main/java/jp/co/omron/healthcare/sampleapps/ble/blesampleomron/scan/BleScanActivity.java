//
//  BleScanActivity.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron.scan;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.AppLog;
import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.R;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.BleScanner;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.DiscoverPeripheral;

public class BleScanActivity extends AppCompatActivity
        implements BleScanFragment.OnEventListener {

    public static final String EXTRA_SCAN_FILTERING_SERVICE_UUIDS = "extra_scan_service_uuids";
    public static final String EXTRA_CONNECT_REQUEST_PERIPHERAL = "extra_connect_request_peripheral";
    public static final int RESPONSE_CODE_CANCEL = 0;
    public static final int RESPONSE_CODE_CONNECT = 1;

    private static final String[] sRequiredPermissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.dMethodIn();
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        ArrayList<ParcelUuid> parcelableArray;
        if (savedInstanceState != null) {
            parcelableArray = savedInstanceState.getParcelableArrayList(EXTRA_SCAN_FILTERING_SERVICE_UUIDS);
        } else {
            Intent intent = getIntent();
            parcelableArray = intent.getParcelableArrayListExtra(EXTRA_SCAN_FILTERING_SERVICE_UUIDS);
        }

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                BleScanFragment.newInstance(parcelableArray)).commit();
    }

    @Override
    protected void onResume() {
        AppLog.dMethodIn();
        super.onResume();
        if (!isAllowRuntimePermissions(sRequiredPermissions)) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        AppLog.dMethodIn();
        super.onPause();
        finish();
    }

    @Override
    public void onScanStartFailure(BleScanner.Reason reason) {
        Toast.makeText(this, getString(R.string.scan_start_failed_message), Toast.LENGTH_SHORT).show();
        setResult(RESPONSE_CODE_CANCEL);
        finish();
    }

    @Override
    public void onScanStopped(BleScanner.Reason reason) {
        if (BleScanner.Reason.StopRequest == reason) {
            // nop
            return;
        }

        Toast.makeText(this, getString(R.string.scan_stop_message), Toast.LENGTH_SHORT).show();
        setResult(RESPONSE_CODE_CANCEL);
        finish();
    }

    @Override
    public void onConnectRequest(DiscoverPeripheral discoverPeripheral) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONNECT_REQUEST_PERIPHERAL, discoverPeripheral);
        setResult(RESPONSE_CODE_CONNECT, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.dMethodIn();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @TargetApi(Build.VERSION_CODES.M)
    private boolean isAllowRuntimePermissions(final String[] requestPermissions) {
        AppLog.dMethodIn();
        if (Build.VERSION_CODES.M > Build.VERSION.SDK_INT) {
            AppLog.i("Unsupported runtime permissions.");
            return true;
        }

        for (String permission : requestPermissions) {
            if (PackageManager.PERMISSION_DENIED == checkSelfPermission(permission)) {
                return false;
            }
        }

        return true;
    }
}
