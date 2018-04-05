/*
 * File: BlsActivity.java
 *
 * Abstract: Activity class. Display the BLS (Blood Pressure Service) data.
 *
 * Copyright (c) 2015 OMRON HEALTHCARE Co., Ltd. All rights reserved.
 */

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.DiscoverPeripheral;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.GattUUID;

public class BlsActivity extends BaseBleActivity {

    private TextView mTimestampView;
    private TextView mSystolicView;
    private TextView mDiastolicView;
    private TextView mMeanApView;
    private TextView mPulseRateView;
    private TextView mUserIDView;
    private TextView mBodyMovementView;
    private TextView mIrregularPulseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.dMethodIn();
        setContentView(R.layout.activity_bls);

        mSystolicView = (TextView) findViewById(R.id.tvSystolicValue);
        mDiastolicView = (TextView) findViewById(R.id.tvDiastolicValue);
        mMeanApView = (TextView) findViewById(R.id.tvMeanAPValue);
        mTimestampView = (TextView) findViewById(R.id.tvTimestampValue);
        mPulseRateView = (TextView) findViewById(R.id.tvPulseRateValue);
        mUserIDView = (TextView) findViewById(R.id.tvUserIDValue);
        mBodyMovementView = (TextView) findViewById(R.id.tvBodyMovementValue);
        mIrregularPulseView = (TextView) findViewById(R.id.tvIrregularPulseValue);

        UUID[] uuids = new UUID[]{GattUUID.Service.BloodPressureService.getUuid()};
        setScanFilteringServiceUuids(uuids);

        initVitalDataView();

        super.onCreate(savedInstanceState); // execute at the end
    }

    @Override
    protected void onDestroy() {
        AppLog.dMethodIn();
        super.onDestroy();

        HistoryData hd = (HistoryData) this.getApplication();
        hd.save(HistoryData.SERV_BLS);
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
            case BPMDataRcv:

                int idx = 0;
                data = (byte[]) msg.obj;
                AppLog.bleInfo("Blood Pressure Measurement Raw Data:" + Utils.byteDataToHexString(data));

                byte flags = data[idx++];

                // 0: mmHg	1: kPa
                boolean kPa = (flags & 0x01) > 0;
                // 0: No Timestamp info 1: With Timestamp info
                boolean timestampFlag = (flags & 0x02) > 0;
                // 0: No PlseRate info 1: With PulseRate info
                boolean pulseRateFlag = (flags & 0x04) > 0;
                // 0: No UserID info 1: With UserID info
                boolean userIdFlag = (flags & 0x08) > 0;
                // 0: No MeasurementStatus info 1: With MeasurementStatus info
                boolean measurementStatusFlag = (flags & 0x10) > 0;

                // Set BloodPressureMeasurement unit
                String unit;
                if (kPa) {
                    unit = "kPa";
                } else {
                    unit = "mmHg";
                }

                // Parse Blood Pressure Measurement
                short systolicVal = 0;
                short diastolicVal = 0;
                short meanApVal = 0;

                System.arraycopy(data, idx, buf, 0, 2);
                idx += 2;
                byteBuffer = ByteBuffer.wrap(buf);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                systolicVal = byteBuffer.getShort();

                System.arraycopy(data, idx, buf, 0, 2);
                idx += 2;
                byteBuffer = ByteBuffer.wrap(buf);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                diastolicVal = byteBuffer.getShort();

                System.arraycopy(data, idx, buf, 0, 2);
                idx += 2;
                byteBuffer = ByteBuffer.wrap(buf);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                meanApVal = byteBuffer.getShort();

                AppLog.bleInfo("systolicValue:" + systolicVal + " " + unit);
                AppLog.bleInfo("diastolicValue:" + diastolicVal + " " + unit);
                AppLog.bleInfo("meanApValue:" + meanApVal + " " + unit);

                mSystolicView.setText(Float.toString(systolicVal) + " " + unit);
                mDiastolicView.setText(Float.toString(diastolicVal) + " " + unit);
                mMeanApView.setText(Float.toString(meanApVal) + " " + unit);

                // Parse Timestamp
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

                // Parse PulseRate
                short pulseRateVal = 0;
                String pulseRateStr = "----";
                if (pulseRateFlag) {
                    System.arraycopy(data, idx, buf, 0, 2);
                    idx += 2;
                    byteBuffer = ByteBuffer.wrap(buf);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    pulseRateVal = byteBuffer.getShort();
                    pulseRateStr = Short.toString(pulseRateVal);
                    AppLog.bleInfo("PulseRate Data:" + pulseRateStr);
                }
                mPulseRateView.setText(pulseRateStr);

                // Parse UserID
                int userIDVal = 0;
                String userIDStr = "----";
                if (userIdFlag) {
                    userIDVal = data[idx++];
                    userIDStr = String.valueOf(userIDVal);
                    AppLog.bleInfo("UserID Data:" + userIDStr);
                }
                mUserIDView.setText(userIDStr);

                // Parse Measurement Status
                int measurementStatusVal = 0;
                String measurementStatusStr = "----";
                if (measurementStatusFlag) {
                    System.arraycopy(data, idx, buf, 0, 2);
                    idx += 2;
                    byteBuffer = ByteBuffer.wrap(buf);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    measurementStatusVal = byteBuffer.getShort();
                    measurementStatusStr = String.format(Locale.US, "%1$04x", (short) measurementStatusVal);
                    AppLog.bleInfo("MeasurementStatus Data:" + measurementStatusStr);

                    mBodyMovementView.setText((measurementStatusVal & 0x0001) == 0 ? "No" : "Yes");
                    mIrregularPulseView.setText((measurementStatusVal & 0x0004) == 0 ? "No" : "Yes");
                } else {
                    mBodyMovementView.setText("----");
                    mIrregularPulseView.setText("----");
                }

                // Output to History
                AppLog.d("Add history");
                String entry = timestampStr
                        + "," + systolicVal
                        + "," + diastolicVal
                        + "," + meanApVal
                        + "," + pulseRateStr
                        + "," + String.format(Locale.US, "%1$02x", flags)
                        + "," + measurementStatusStr;

                HistoryData hd = (HistoryData) this.getApplication();
                hd.add(HistoryData.SERV_BLS, entry);

                // Output log for data aggregation
                // AppLog format: ## For aggregation ## timestamp(date), timestamp(time), systolic, diastolic, meanAP, current date time
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String agg = "## For aggregation ## ";
                agg += dateStr + "," + timeStr;
                agg += "," + systolicVal + "," + diastolicVal + "," + meanApVal;
                agg += "," + sdf.format(c.getTime());
                AppLog.i(agg);
                break;

            case BPFDataRcv:
                data = (byte[]) msg.obj;
                AppLog.bleInfo("Blood Pressure Feature Raw Data:" + Utils.byteDataToHexString(data));
                System.arraycopy(data, 0, buf, 0, 2);
                ByteBuffer bpfByteBuffer = ByteBuffer.wrap(buf);
                bpfByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                short bpfVal = bpfByteBuffer.getShort();
                String bpfStr = String.format(Locale.US, "%1$04x", (short) bpfVal);
                AppLog.bleInfo("Blood Pressure Feature Data:" + bpfStr);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onClickHistoryButton() {
        super.onClickHistoryButton();

        Intent intent = new Intent(BlsActivity.this, BlsHistoricalActivity.class);
        startActivity(intent);
    }


    private void initVitalDataView() {
        mTimestampView.setText("----");
        mSystolicView.setText("----");
        mDiastolicView.setText("----");
        mMeanApView.setText("----");
        mPulseRateView.setText("----");
        mUserIDView.setText("----");
        mBodyMovementView.setText("----");
        mIrregularPulseView.setText("----");
    }
}
