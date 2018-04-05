/*
 * File: WssActivity.java
 *
 * Abstract: Activity class. Display the WSS (Weight Scale Service) data.
 *
 * Copyright (c) 2015 OMRON HEALTHCARE Co., Ltd. All rights reserved.
 */

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.widget.TextView;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.DiscoverPeripheral;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.GattUUID;

public class WssActivity extends BaseBleActivity {

    // Resolution table							  default  1	 2	   3	 4	   5	 6	   7
    private final static double RESOLUTION_KG[] = {0.005, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01, 0.005};
    private final static double RESOLUTION_LB[] = {0.01, 1.0, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01};
    private int mResolutionIdx;

    private TextView mTimestampView;
    private TextView mWeightScaleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.dMethodIn();
        setContentView(R.layout.activity_wss);

        mTimestampView = (TextView) findViewById(R.id.tvTimestampValue);
        mWeightScaleView = (TextView) findViewById(R.id.tvWeightValue);

        UUID[] uuids = new UUID[]{GattUUID.Service.WeightScaleService.getUuid()};
        setScanFilteringServiceUuids(uuids);

        initVitalDataView();

        super.onCreate(savedInstanceState); // execute at the end
    }

    @Override
    protected void onDestroy() {
        AppLog.dMethodIn();
        super.onDestroy();

        HistoryData hd = (HistoryData) this.getApplication();
        hd.save(HistoryData.SERV_WSS);
    }

    @Override
    protected void onConnect(@NonNull DiscoverPeripheral discoverPeripheral) {
        AppLog.dMethodIn();
        initVitalDataView();
        super.onConnect(discoverPeripheral);
    }

    @Override
    protected void onReceiveMessage(Message msg) {
        super.onReceiveMessage(msg);

        byte[] data;
        byte[] buf = new byte[2];
        ByteBuffer byteBuffer;

        MessageType messageType = MessageType.values()[msg.what];
        AppLog.d(messageType.name());
        switch (messageType) {
            case WMDataRcv:

                int idx = 0;

                data = (byte[]) msg.obj;
                AppLog.bleInfo("Weight Measurement Raw Data:" + Utils.byteDataToHexString(data));
                byte flags = data[idx++];

                // Parse Flags
                // 0: kg/cm	 1: lb/inchi
                boolean kg = (flags & 0x01) == 0;
                // 0: No Timestamp info 1: With Timestamp info
                boolean timestampFlag = (flags & 0x02) > 0;
                // 0: No UserID info 1: With UserID info
                boolean userIDFlag = (flags & 0x04) > 0;
                // 0: No BMI info 1: With BMI info
                boolean bmiFlag = (flags & 0x08) > 0;
                AppLog.bleInfo("Flags:0x" + String.format(Locale.US, "%1$02x", flags));
                AppLog.bleInfo("Flags - kg/lb:" + (kg ? "kg" : "lb") + " timestamp:" + timestampFlag + " UserID:" + userIDFlag + "BMI:" + bmiFlag);

                // Parse WeightScale
                System.arraycopy(data, idx, buf, 0, 2);
                idx += 2;
                int weightMeasurementVal = (buf[0] & 0xFF) | ((buf[1] & 0xFF) << 8);
                AppLog.bleInfo("Weight Data(raw):" + weightMeasurementVal);

                double kgWeight = 0;
                double lbWeight = 0;
                if (kg) {
                    if (mResolutionIdx >= RESOLUTION_KG.length) {
                        mResolutionIdx = 0;
                    }
                    BigDecimal bdRaw = new BigDecimal(weightMeasurementVal);
                    BigDecimal bdResolution = new BigDecimal(String.format(Locale.US, "%.4f", RESOLUTION_KG[mResolutionIdx]));
                    kgWeight = bdRaw.multiply(bdResolution).doubleValue();
                    mWeightScaleView.setText(kgWeight + " kg");
                    AppLog.bleInfo("Weight Data(kg):" + kgWeight);

                } else {
                    if (mResolutionIdx >= RESOLUTION_LB.length) {
                        mResolutionIdx = 0;
                    }
                    BigDecimal bdRaw = new BigDecimal(weightMeasurementVal);
                    BigDecimal bdResolution = new BigDecimal(String.format(Locale.US, "%.4f", RESOLUTION_LB[mResolutionIdx]));
                    lbWeight = bdRaw.multiply(bdResolution).doubleValue();
                    mWeightScaleView.setText(lbWeight + " lb");
                    AppLog.bleInfo("Weight Data(lb) :" + lbWeight);
                }

                // Parse Timesamp
                String timestampStr = "----";
                String dateStr = "--";
                String timeStr = "--";
                if (timestampFlag) {
                    System.arraycopy(data, idx, buf, 0, 2);
                    idx += 2;
                    byteBuffer = ByteBuffer.wrap(buf);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

                    int year = byteBuffer.getShort();
                    int month = data[idx++];
                    int day = data[idx++];
                    int hour = data[idx++];
                    int min = data[idx++];
                    int sec = data[idx++];

                    dateStr = String.format(Locale.US, "%1$04d-%2$02d-%3$02d", year, month, day);
                    timeStr = String.format(Locale.US, "%1$02d:%2$02d:%3$02d", hour, min, sec);
                    timestampStr = dateStr + " " + timeStr;
                    AppLog.bleInfo("Timestamp Data:" + timestampStr);
                }
                mTimestampView.setText(timestampStr);

                // Parse UserID
                int userIDVal = 0;
                if (userIDFlag) {
                    userIDVal = data[idx++] & 0xFF;
                    AppLog.bleInfo("UserID Data:" + userIDVal);
                }

                // Output to History
                AppLog.d("Add history");
                String history = timestampStr
                        + "," + (kg ? kgWeight + " kg" : lbWeight + " lb")
                        + "," + String.format(Locale.US, "%1$02x", flags);

                HistoryData hd = (HistoryData) this.getApplication();
                hd.add(HistoryData.SERV_WSS, history);

                // Output log for data aggregation
                // AppLog format: ## For aggregation ## timestamp(date), timestamp(time), weight, current date time
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String agg = "## For aggregation ## ";
                agg += "," + dateStr + "," + timeStr;
                agg += "," + (kg ? kgWeight + " kg" : lbWeight + " lb");
                agg += "," + sdf.format(c.getTime());
                AppLog.i(agg);
                break;

            case WSFDataRcv:
                data = (byte[]) msg.obj;
                AppLog.bleInfo("Weight Feature Raw Data:" + Utils.byteDataToHexString(data));
                byte[] wsfBuf = new byte[4];

                System.arraycopy(data, 0, wsfBuf, 0, 4);
                byteBuffer = ByteBuffer.wrap(wsfBuf);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                int wsfVal = byteBuffer.getInt();
                String wsfStr = String.format(Locale.US, "%08x", (short) wsfVal);
                mResolutionIdx = (wsfVal >> 3) & 0x0000000F;
                AppLog.bleInfo("Weight Scale Feature Data:" + wsfStr);
                AppLog.bleInfo("Weight Measurement resolution idx:" + mResolutionIdx);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onClickHistoryButton() {
        super.onClickHistoryButton();

        Intent intent = new Intent(WssActivity.this, WssHistoricalActivity.class);
        startActivity(intent);
    }

    private void initVitalDataView() {
        mTimestampView.setText("----");
        mWeightScaleView.setText("----");
    }
}
