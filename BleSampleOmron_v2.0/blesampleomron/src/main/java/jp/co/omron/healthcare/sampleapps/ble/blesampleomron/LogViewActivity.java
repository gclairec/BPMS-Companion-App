//
//  LogViewActivity.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class LogViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.dMethodIn();
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LogViewFragment()).commit();
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
}
