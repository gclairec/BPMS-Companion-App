/*
 * File: BlsHistoricalActivity.java
 *
 * Abstract: Activity class. Display the BLS (Blood Pressure Service) history data.
 *
 * Copyright (c) 2015 OMRON HEALTHCARE Co., Ltd. All rights reserved.
 */

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Locale;

public class BlsHistoricalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        AppLog.dMethodIn();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bls_historical);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        HistoryData hd = (HistoryData) this.getApplication();
        TableLayout tl = (TableLayout) findViewById(R.id.tlHistoryTable);
        String entry = null;
        for (int idx = 0; ; idx++) {
            entry = hd.get(HistoryData.SERV_BLS, idx);
            if (entry == null) {
                break;
            }

            String[] items = entry.split(",");

            // add table row
            TableRow tr = new TableRow(tl.getContext());
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            if (idx % 2 == 0) {
                tr.setBackgroundColor(Color.rgb(0xFF, 0xFF, 0xFF));
            } else {
                tr.setBackgroundColor(Color.rgb(0xEE, 0xEE, 0xEE));
            }

            // set value
            TextView tv = new TextView(tl.getContext());
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(10, 0, 10, 0);
            tv.setText(String.format(Locale.US, "%d", idx + 1));
            tr.addView(tv);
            for (int i = 0; i < items.length; i++) {
                tv = new TextView(tl.getContext());
                tv.setGravity(Gravity.CENTER);
                tv.setPadding(10, 0, 10, 0);
                tv.setText(items[i]);
                tr.addView(tv);
            }
            tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
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
