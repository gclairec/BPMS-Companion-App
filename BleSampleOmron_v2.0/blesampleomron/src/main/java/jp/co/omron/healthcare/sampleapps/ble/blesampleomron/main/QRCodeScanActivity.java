package jp.co.omron.healthcare.sampleapps.ble.blesampleomron.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.BlsActivity;
import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.R;

public class QRCodeScanActivity extends AppCompatActivity {
    public static String patientID;
    public static int hours, minutes, seconds;

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

    public void saveUserInfo()
    {
        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context
                .MODE_PRIVATE);
        SharedPreferences sharedPrefNotif = getSharedPreferences("notifFreq", Context
                .MODE_PRIVATE);
        SharedPreferences.Editor userInfoEditor = sharedPref.edit();
        userInfoEditor.putString("patientID", patientID);
        userInfoEditor.apply();
        SharedPreferences.Editor userNotifEditor = sharedPrefNotif.edit();
        userNotifEditor.putInt("hours", hours);
        userNotifEditor.putInt("minutes", minutes);
        userNotifEditor.putInt("seconds", seconds);
        userNotifEditor.apply();
        Toast.makeText(this, "Data Saved!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String qrData;
        String dataQR = null;
        TextView patID, freq;
        String _patID;
        int _hours, _mins, _secs;

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

//                    try {
////                        SQLiteDatabase mydatabase = openOrCreateDatabase("PATIENT_BP_DATA",MODE_PRIVATE,null);
//                        Toast.makeText(this, "DB Created", Toast.LENGTH_SHORT).show();
//                    }finally {
//
//                    }
                    saveUserInfo();
                    // set employee name and salary in TextView's



                } catch (JSONException e) {
                    e.printStackTrace();
                }

                SharedPreferences sharedPref = getSharedPreferences("userInfo", Context
                        .MODE_PRIVATE);
                SharedPreferences sharedPrefNotif = getSharedPreferences("notifFreq", Context
                        .MODE_PRIVATE);
//

                _patID = sharedPref.getString("patientID", "");
                _hours = sharedPrefNotif.getInt("hours", 0);
                _mins = sharedPrefNotif.getInt("minutes", 0);
                _secs = sharedPrefNotif.getInt("seconds", 0);

                patID.setText("PatientID: "+ _patID);
                freq.setText(String.format("Frequency: H:%s, M:%s, S:%s", _hours, _mins, _secs));

                Intent intent = new Intent(QRCodeScanActivity.this, BlsActivity.class);
                startActivity(intent);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
