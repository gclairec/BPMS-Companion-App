package com.bpmsthesis.claire.bpmsapplication;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class QRCodeScanActivity extends AppCompatActivity {
    public String patientID;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);
        button = (Button) this.findViewById(R.id.button);
        final Activity activity = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String qrData;
        String dataQR = null;
        TextView patID, freq;

        int hours;
        int minutes;
        int seconds;
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
//                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                qrData = result.getContents();
                Toast.makeText(this, "Scanned: " + qrData, Toast.LENGTH_LONG).show();

                patID = (TextView) findViewById(R.id.patientID);
                freq = (TextView) findViewById(R.id.frequency);

                try {
                    // get JSONObject from JSON file
                    JSONObject obj = new JSONObject(qrData);
                    // fetch JSONObject named employee
//                    JSONObject employee = obj.getJSONObject("employee");
                    // get employee name and salary
                    patientID = obj.getString("patientID");
//                    salary = obj.getString("salary");
                    JSONObject freq_qr = obj.getJSONObject("frequency");
                    hours = freq_qr.getInt("hours");
                    minutes = freq_qr.getInt("minutes");
                    seconds = freq_qr.getInt("seconds");

                    try {
                        SQLiteDatabase mydatabase = openOrCreateDatabase(patientID,MODE_PRIVATE,null);
                        Toast.makeText(this, "DB Created", Toast.LENGTH_SHORT).show();
                    }finally {

                    }

                    // set employee name and salary in TextView's
                    patID.setText("PatientID: "+patientID);
                    freq.setText(String.format("Frequency: H:%s, M:%s, S:%s", hours, minutes, seconds));

                } catch (JSONException e) {
                    e.printStackTrace();
                }





            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
