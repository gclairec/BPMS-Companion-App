/*
 * File: BaseBleActivity.java
 *
 * Abstract: Base class of Activity using BLE service.
 *
 * Copyright (c) 2015 OMRON HEALTHCARE Co., Ltd. All rights reserved.
 */

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jp.co.omron.healthcare.sampleapps.ble.blesampleomron.scan.BleScanActivity;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.BlePeripheral;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.BlePeripheralSettings;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.DiscoverPeripheral;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.ErrorCode;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.GattStatusCode;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.GattUUID;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.StateInfo;

public class BaseBleActivity extends AppCompatActivity {

    private static final String ACTION_PAIRING_REQUEST;
    private static final int INDICATION_WAIT_TIME = 1000 * 10;
    private static final String[] sRequiredPermissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_CODE_SCAN = 1;

    static {
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
        } else {
            ACTION_PAIRING_REQUEST = actionPairingRequestStringFromKitkat();
        }
    }

    private final BroadcastReceiver mPairingRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!SettingsFragment.isAutoPairingEnabled(BaseBleActivity.this)) {
                Toast.makeText(BaseBleActivity.this, R.string.confirm_pairing_request, Toast.LENGTH_LONG).show();
            }
        }
    };
    private TextView mBondStatusView;
    private TextView mAclStatusView;
    private TextView mGattStatusView;
    private TextView mDetailedStateView;
    private Button mConnectBtn;
//    private Switch mBluetoothSwitch;
    private UUID[] mScanFilteringServiceUuids = null;
    private BlePeripheral mTargetPeripheral;
    private BleCommunicationExecutor mBleCommunicationExecutor;
    private boolean mIsCtsWritten;
    private TextView mLocalNameView;
    private TextView mAddressView;
    private TextView mBatteryLevelView;
    private TextView mCtsView;
    private boolean mIsBluetoothOn;
    private Handler mMessageHandler;
    private final BroadcastReceiver mBluetoothStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
            if (state == BluetoothAdapter.STATE_ON) {
                mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.BluetoothOn.ordinal()));
            } else if (state == BluetoothAdapter.STATE_OFF) {
                mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.BluetoothOff.ordinal()));
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String actionPairingRequestStringFromKitkat() {
        return BluetoothDevice.ACTION_PAIRING_REQUEST;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.dMethodIn();
        super.onCreate(savedInstanceState);
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (! mBtAdapter.isEnabled()) {
            mBtAdapter.enable();
        }
        ActionBar actionBar = getSupportActionBar();
        if (null == actionBar) {
            finish();
            return;
        }
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        mLocalNameView = (TextView) findViewById(R.id.tvLocalNameValue);
        mAddressView = (TextView) findViewById(R.id.tvBDAddrValue);
        mBatteryLevelView = (TextView) findViewById(R.id.tvBatteryLevelValue);
        mCtsView = (TextView) findViewById(R.id.tvCurrentTimeValue);
        mBondStatusView = (TextView) findViewById(R.id.tvBondStatusValue);
        mAclStatusView = (TextView) findViewById(R.id.tvACLStatusValue);
        mGattStatusView = (TextView) findViewById(R.id.tvGattStatusValue);
        mDetailedStateView = (TextView) findViewById(R.id.tvDetailedStateValue);

        mConnectBtn = (Button) findViewById(R.id.btnConnect);
        mConnectBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectBtn.getText().equals(getString(R.string.connect))) {
                    boolean requested = requestRuntimePermissions(sRequiredPermissions);
                    if (!requested) {

                        Toast.makeText(BaseBleActivity.this, "Before Calling startScnListView", Toast.LENGTH_SHORT).show();
                        startScanListView();//Calls Start Scan List View

                        Toast.makeText(BaseBleActivity.this, "After calling startScanListView", Toast.LENGTH_SHORT).show();
                    }
                } else if (mConnectBtn.getText().equals(getString(R.string.connecting))) {
                    disconnect(mTargetPeripheral, DisconnectReason.UserRequest);
                } else if (mConnectBtn.getText().equals(getString(R.string.disconnect))) {
                    disconnect(mTargetPeripheral, DisconnectReason.UserRequest);
                }
            }
        });

        final Button historicalBtn = (Button) findViewById(R.id.btnHistory);
        if (null != historicalBtn) {
            historicalBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickHistoryButton();
                }
            });
        }

//        mBluetoothSwitch = (Switch) findViewById(R.id.bluetoothSwitch);
//        mBluetoothSwitch.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mIsBluetoothOn) {
//                    setBluetoothEnabled(false);
//                } else {
//                    setBluetoothEnabled(true);
//                }
//                mBluetoothSwitch.setEnabled(false);
//            }
//        });

        mMessageHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (!isDestroyed()) {
                    onReceiveMessage(msg);
                }
            }
        };

        initDeviceInfoView();
        initConnectionView();
    }

    @Override
    protected void onResume() {
        AppLog.dMethodIn();
        super.onResume();
        registerReceiver(mBluetoothStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(mPairingRequestReceiver, new IntentFilter(ACTION_PAIRING_REQUEST));

        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (! mBtAdapter.isEnabled()) {
            mBtAdapter.enable();
        }
//        mIsBluetoothOn = isBluetoothEnabled();
//        mBluetoothSwitch.setChecked(mIsBluetoothOn);
//        if (!mIsBluetoothOn) {
//            Toast.makeText(this, R.string.bluetooth_doesnt_work, Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    protected void onPause() {
        AppLog.dMethodIn();
        super.onPause();
        unregisterReceiver(mBluetoothStateChangedReceiver);
        unregisterReceiver(mPairingRequestReceiver);
    }

    @Override
    protected void onDestroy() {
        AppLog.dMethodIn();
        super.onDestroy();
        if (null != mTargetPeripheral) {
            mTargetPeripheral.destroy();
            mTargetPeripheral = null;
        }
        if (null != mBleCommunicationExecutor) {
            mBleCommunicationExecutor.clear();
            mBleCommunicationExecutor = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        AppLog.dMethodIn();
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        AppLog.dMethodIn();
        MenuItem item = menu.findItem(R.id.menu_settings);
        if (null == mTargetPeripheral) {
            item.setEnabled(true);
        } else if (StateInfo.ConnectionState.Disconnected == mTargetPeripheral.getStateInfo().getConnectionState()) {
            item.setEnabled(true);
        } else {
            item.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.dMethodIn();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean requestRuntimePermissions(final String[] requestPermissions) {
        AppLog.dMethodIn();
        if (Build.VERSION_CODES.M > Build.VERSION.SDK_INT) {
            AppLog.i("Unsupported runtime permissions.");
            return false;
        }

        List<String> deniedPermission = new ArrayList<>();
        for (String permission : requestPermissions) {
            if (PackageManager.PERMISSION_DENIED == checkSelfPermission(permission)) {
                deniedPermission.add(permission);
            }
        }

        if (deniedPermission.isEmpty()) {
            AppLog.i("Runtime permissions are permitted.");
            return false;
        }

        String[] permissions = deniedPermission.toArray(new String[deniedPermission.size()]);
        requestPermissions(permissions, 0);
        AppLog.i("Request permissions.");
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AppLog.dMethodIn();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (PackageManager.PERMISSION_DENIED == result) {
                Toast.makeText(this, R.string.location_permission_denied_message, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        startScanListView();
    }

    protected void setScanFilteringServiceUuids(UUID[] serviceUuids) {
        mScanFilteringServiceUuids = serviceUuids;
    }

    private void startScanListView() {
        AppLog.dMethodIn();
//        if (!mIsBluetoothOn) {
//            Toast.makeText(BaseBleActivity.this, R.string.bluetooth_doesnt_work, Toast.LENGTH_LONG).show();
//            return;
//        }
        Intent intent = new Intent(BaseBleActivity.this, BleScanActivity.class);
        ArrayList<ParcelUuid> parcelUuidList = new ArrayList<>();
        for (UUID uuid : mScanFilteringServiceUuids) {
            parcelUuidList.add(new ParcelUuid(uuid));
        }
//        String uuid0 = parcelUuidList.get(0).getUuid().toString();
//        Toast.makeText(BaseBleActivity.this, uuid0, Toast.LENGTH_LONG).show();


        intent.putParcelableArrayListExtra(BleScanActivity.EXTRA_SCAN_FILTERING_SERVICE_UUIDS, parcelUuidList);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppLog.dMethodIn();
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_SCAN != requestCode) {
            AppLog.w("Unknown request code : " + requestCode);
            return;
        }
        if (BleScanActivity.RESPONSE_CODE_CONNECT != resultCode) {
            return;
        }

        DiscoverPeripheral discoverPeripheral = data.getParcelableExtra(BleScanActivity.EXTRA_CONNECT_REQUEST_PERIPHERAL);
        if (null == discoverPeripheral) {
            AppLog.e("null == discoverPeripheral");
            return;
        }

        initDeviceInfoView();
        initConnectionView();

        onConnect(discoverPeripheral);
    }

    protected void onConnect(@NonNull DiscoverPeripheral discoverPeripheral) {
        AppLog.dMethodIn(discoverPeripheral.getAddress());

        BlePeripheral blePeripheral = new BlePeripheral(this, discoverPeripheral);

        Bundle bundle = new Bundle();
        bundle.putBoolean(BlePeripheralSettings.Key.AssistPairingDialog.name(), SettingsFragment.isAssistPairingDialog(this));
        bundle.putBoolean(BlePeripheralSettings.Key.UseCreateBond.name(), SettingsFragment.isCreateBond(this));
        bundle.putBoolean(BlePeripheralSettings.Key.AutoPairingEnabled.name(), SettingsFragment.isAutoPairingEnabled(this));
        bundle.putString(BlePeripheralSettings.Key.AutoPairingPinCode.name(), SettingsFragment.getAutoPairingPinCode(this));
        bundle.putBoolean(BlePeripheralSettings.Key.StableConnectionEnabled.name(), SettingsFragment.isStableConnectionEnabled(this));
        bundle.putLong(BlePeripheralSettings.Key.StableConnectionWaitTime.name(), SettingsFragment.getStableConnectionWaitTime(this));
        bundle.putBoolean(BlePeripheralSettings.Key.ConnectionRetryEnabled.name(), SettingsFragment.isConnectionRetryEnabled(this));
        bundle.putLong(BlePeripheralSettings.Key.ConnectionRetryDelayTime.name(), SettingsFragment.getConnectionRetryDelayTime(this));
        bundle.putInt(BlePeripheralSettings.Key.ConnectionRetryCount.name(), SettingsFragment.getConnectionRetryCount(this));
        bundle.putBoolean(BlePeripheralSettings.Key.DiscoverServiceRetryEnabled.name(), SettingsFragment.isDiscoverServiceRetryEnabled(this));
        bundle.putLong(BlePeripheralSettings.Key.DiscoverServiceRetryDelayTime.name(), SettingsFragment.getDiscoverServiceRetryDelayTime(this));
        bundle.putInt(BlePeripheralSettings.Key.DiscoverServiceRetryCount.name(), SettingsFragment.getDiscoverServiceRetryCount(this));
        bundle.putBoolean(BlePeripheralSettings.Key.UseRemoveBond.name(), SettingsFragment.isRemoveBond(this));
        bundle.putBoolean(BlePeripheralSettings.Key.UseRefreshGatt.name(), SettingsFragment.isRefreshGatt(this));
        blePeripheral.getSettings().setParameter(bundle);

        blePeripheral.connect(new BlePeripheral.ActionReceiver() {
            @Override
            public void didDisconnection(@NonNull String address) {
                mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.DidDisconnection.ordinal()));
            }

            @Override
            public void onCharacteristicChanged(@NonNull String address, @NonNull BluetoothGattCharacteristic characteristic) {
                if (GattUUID.Characteristic.BloodPressureMeasurementCharacteristic.getUuid().equals(characteristic.getUuid())) {
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.BPMDataRcv.ordinal(), characteristic.getValue()));
                } else if (GattUUID.Characteristic.WeightMeasurementCharacteristic.getUuid().equals(characteristic.getUuid())) {
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.WMDataRcv.ordinal(), characteristic.getValue()));
                } else if (GattUUID.Characteristic.BatteryLevelCharacteristic.getUuid().equals(characteristic.getUuid())) {
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.BatteryDataRcv.ordinal(), characteristic.getValue()));
                } else if (GattUUID.Characteristic.CurrentTimeCharacteristic.getUuid().equals(characteristic.getUuid())) {
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.CTSDataRcv.ordinal(), characteristic.getValue()));
                }
            }
        }, new BlePeripheral.ConnectionListener() {
            @Override
            public void onComplete(@NonNull String address, ErrorCode errorCode) {
                if (null == errorCode) {
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.ConnectionCompleted.ordinal()));
                } else {
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.ConnectionFailed.ordinal(), errorCode));
                }
            }
        }, new StateInfo.StateMonitor() {
            @Override
            public void onBondStateChanged(@NonNull StateInfo.BondState bondState) {
                mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.BondStateChanged.ordinal(), bondState));
            }

            @Override
            public void onAclConnectionStateChanged(@NonNull StateInfo.AclConnectionState aclConnectionState) {
                mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.AclConnectionStateChanged.ordinal(), aclConnectionState));
            }

            @Override
            public void onGattConnectionStateChanged(@NonNull StateInfo.GattConnectionState gattConnectionState) {
                mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.GattConnectionStateChanged.ordinal(), gattConnectionState));
            }

            @Override
            public void onConnectionStateChanged(@NonNull StateInfo.ConnectionState connectionState) {
                AppLog.d(connectionState.name());
            }

            @Override
            public void onDetailedStateChanged(@NonNull StateInfo.DetailedState detailedState) {
                mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.DetailedStateChanged.ordinal(), detailedState));
            }
        });

        mTargetPeripheral = blePeripheral;

        mBleCommunicationExecutor = new BleCommunicationExecutor(mTargetPeripheral, new Handler() {
            public void handleMessage(Message msg) {
                onBleCommunicationComplete(msg);
            }
        });

        mIsCtsWritten = false;
        mLocalNameView.setText(mTargetPeripheral.getLocalName());
        mAddressView.setText(mTargetPeripheral.getAddress());
        updateConnectionView(mTargetPeripheral);
        mConnectBtn.setText(R.string.connecting);
        invalidateOptionsMenu();
    }

    protected void disconnect(@NonNull BlePeripheral blePeripheral, @NonNull final DisconnectReason reason) {
        AppLog.dMethodIn(blePeripheral.getAddress() + " " + reason.name());

        blePeripheral.disconnect(new BlePeripheral.DisconnectionListener() {
            @Override
            public void onComplete(@NonNull String address, ErrorCode errorCode) {
                if (null == errorCode) {
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.DisconnectionCompleted.ordinal()));
                } else {
                    AppLog.e(errorCode.name());
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.DisconnectionFailed.ordinal(), errorCode));
                }
            }
        });

        stopIndicationWaitTimer();
        mBleCommunicationExecutor.clear();
        mConnectBtn.setEnabled(false);
        mConnectBtn.setText(R.string.disconnecting);
    }

    protected void onReceiveMessage(Message msg) {
        MessageType messageType = MessageType.values()[msg.what];
        AppLog.d(messageType.name());
        switch (messageType) {
            case BluetoothOff:
                mIsBluetoothOn = false;
                BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

                if (! mBtAdapter.isEnabled()) {
                    mBtAdapter.enable();
                }
//                mBluetoothSwitch.setChecked(false);
//                mBluetoothSwitch.setEnabled(true);
                initDeviceInfoView();
                initConnectionView();
                break;
            case BluetoothOn:
                mIsBluetoothOn = true;
//                mBluetoothSwitch.setChecked(true);
//                mBluetoothSwitch.setEnabled(true);
                break;
            case ConnectionCompleted:
                AppLog.bleInfo("Connect to " + mTargetPeripheral.getLocalName() + "(" + mTargetPeripheral.getAddress() + ")");
                mConnectBtn.setText(R.string.disconnect);
                startCommunication();
                break;
            case ConnectionFailed:
                mConnectBtn.setText(R.string.connect);
                invalidateOptionsMenu();
                Toast.makeText(BaseBleActivity.this, "Connection failed.", Toast.LENGTH_LONG).show();
                break;
            case DisconnectionCompleted:
                mConnectBtn.setText(R.string.connect);
                mConnectBtn.setEnabled(true);
                invalidateOptionsMenu();
                Toast.makeText(BaseBleActivity.this, "Disconnection completed.", Toast.LENGTH_LONG).show();
                break;
            case DisconnectionFailed:
                if (StateInfo.ConnectionState.Disconnected == mTargetPeripheral.getStateInfo().getConnectionState()) {
                    mConnectBtn.setEnabled(true);
                }
                break;
            case DidDisconnection:
                AppLog.i("Disconnection by peripheral or OS.");
                mConnectBtn.setText(R.string.connect);
                stopIndicationWaitTimer();
                mBleCommunicationExecutor.clear();
                invalidateOptionsMenu();
                break;
            case BondStateChanged:
                StateInfo.BondState bondState = (StateInfo.BondState) msg.obj;
                mBondStatusView.setText(bondState.name());
                if (mTargetPeripheral.getStateInfo().isConnected() && StateInfo.BondState.Bonded == bondState) {
                    // The IndicationWaitTimer will start when both of indication of
                    // BPM or WM is registered and in Bonded state. The timer will startwhen the state is Bonded
                    // because indication of BPM or WM is running in no Nonded state.
                    startIndicationWaitTimer();
                }
                break;
            case AclConnectionStateChanged:
                StateInfo.AclConnectionState aclConnectionState = (StateInfo.AclConnectionState) msg.obj;
                mAclStatusView.setText(aclConnectionState.name());
                break;
            case GattConnectionStateChanged:
                StateInfo.GattConnectionState gattConnectionState = (StateInfo.GattConnectionState) msg.obj;
                mGattStatusView.setText(gattConnectionState.name());
                break;
            case DetailedStateChanged:
                if (mIsBluetoothOn) {
                    StateInfo.DetailedState detailedState = (StateInfo.DetailedState) msg.obj;
                    mDetailedStateView.setText(detailedState.name());
                }
                break;
            case BatteryDataRcv:
                byte[] batteryData = (byte[]) msg.obj;
                AppLog.bleInfo("Battery Level Raw Data:" + Utils.byteDataToHexString(batteryData));
                int batteryLevel = batteryData[0];
                mBatteryLevelView.setText(String.format(Locale.US, "%d %%", batteryLevel));
                AppLog.bleInfo("Battery Level Data:" + batteryLevel);
                break;
            case CTSDataRcv:
                byte[] ctsData = (byte[]) msg.obj;
                AppLog.bleInfo("Current Time Raw Data:" + Utils.byteDataToHexString(ctsData));
                byte[] buf = new byte[2];
                System.arraycopy(ctsData, 0, buf, 0, 2);
                ByteBuffer ctsYearByteBuffer = ByteBuffer.wrap(buf);
                ctsYearByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                int ctsYear = ctsYearByteBuffer.getShort();
                int ctsMonth = ctsData[2];
                int ctsDay = ctsData[3];
                int ctsHour = ctsData[4];
                int ctsMinute = ctsData[5];
                int ctsSecond = ctsData[6];
                byte AdjustReason = ctsData[9];
                String ctsTime = String.format(Locale.US,
                        "%1$04d-%2$02d-%3$02d %4$02d:%5$02d:%6$02d",
                        ctsYear, ctsMonth, ctsDay, ctsHour, ctsMinute, ctsSecond);
                mCtsView.setText(ctsTime);
                AppLog.bleInfo("CTS Data:" + ctsTime + " (AdjustReason:" + AdjustReason + ")");

                if (SettingsFragment.isWriteCTS(this) && !mIsCtsWritten) {
                    AppLog.d("Write CTS");
                    BluetoothGattCharacteristic characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.CurrentTimeCharacteristic.getUuid());
                    if (null == characteristic) {
                        AppLog.e("null == characteristic");
                        break;
                    }
                    byte[] currentTimeData = getCurrentTimeData();
                    characteristic.setValue(currentTimeData);
                    mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.WriteCharacteristic, characteristic));
                    if (!mBleCommunicationExecutor.isExecuting()) {
                        mBleCommunicationExecutor.exec();
                    }
                }
                break;
            case BPMDataRcv:
                restartIndicationWaitTimer();
                break;
            case WMDataRcv:
                restartIndicationWaitTimer();
                break;
            case IndicationWaitTimeout:
                AppLog.e("Indication wait timeout.");
                disconnect(mTargetPeripheral, DisconnectReason.IndicationWaitTimeout);
                break;
            default:
                break;
        }
    }

    private byte[] getCurrentTimeData() {
        byte[] data = new byte[10];
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        data[0] = (byte) year;
        data[1] = (byte) ((year >> 8) & 0xFF);
        data[2] = (byte) (cal.get(Calendar.MONTH) + 1);
        data[3] = (byte) cal.get(Calendar.DAY_OF_MONTH);
        data[4] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        data[5] = (byte) cal.get(Calendar.MINUTE);
        data[6] = (byte) cal.get(Calendar.SECOND);
        data[7] = (byte) ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1); // Rotate
        data[8] = (byte) (cal.get(Calendar.MILLISECOND) * 256 / 1000); // Fractions256
        data[9] = 0x01; // Adjust Reason: Manual time update

        String date = year + "/" + data[2] + "/" + data[3] + " " +
                String.format(Locale.US, "%1$02d:%2$02d:%3$02d", data[4], data[5], data[6]) +
                " (WeekOfDay:" + data[7] + " Fractions256:" + data[8] + " AdjustReason:" + data[9] + ")";
        StringBuilder sb = new StringBuilder("");
        for (byte b : data) {
            sb.append(String.format(Locale.US, "%02x,", b));
        }
        AppLog.bleInfo("CTS Tx Time:" + date);
        AppLog.bleInfo("CTS Tx Data:" + sb.toString());
        return data;
    }

    protected void onClickHistoryButton() {
        // implement in child class
    }

    private boolean isBluetoothEnabled() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager.getAdapter().isEnabled();
    }

    private void setBluetoothEnabled(boolean enable) {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (enable) {
            bluetoothManager.getAdapter().enable();
        } else {
            bluetoothManager.getAdapter().disable();
        }
    }

    private void initDeviceInfoView() {
        mLocalNameView.setText("----");
        mAddressView.setText("----");
        mBatteryLevelView.setText("----");
        mCtsView.setText("----");
    }

    private void initConnectionView() {
        mBondStatusView.setText("---");
        mAclStatusView.setText("---");
        mGattStatusView.setText("---");
        mDetailedStateView.setText("---");
    }

    private void updateConnectionView(BlePeripheral targetPeripheral) {
        mBondStatusView.setText(targetPeripheral.getStateInfo().getBondState().name());
        mAclStatusView.setText(targetPeripheral.getStateInfo().getAclConnectionState().name());
        mGattStatusView.setText(targetPeripheral.getStateInfo().getGattConnectionState().name());
        mDetailedStateView.setText(targetPeripheral.getStateInfo().getDetailedState().name());
    }

    private void startCommunication() {
        AppLog.dMethodIn();
        BluetoothGattCharacteristic characteristic;
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.BatteryLevelCharacteristic.getUuid()))) {
            AppLog.i("[LOG]Battery Service is discovered");
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.SetNotification, characteristic));
        }
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.CurrentTimeCharacteristic.getUuid()))) {
            AppLog.i("[LOG]Current Time Service is discovered");
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.SetNotification, characteristic));
        }
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.BloodPressureMeasurementCharacteristic.getUuid()))) {
            AppLog.i("[LOG]Blood Pressure Service is discovered");
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.SetIndication, characteristic));
        }
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.WeightMeasurementCharacteristic.getUuid()))) {
            AppLog.i("[LOG]Weight Scale Service is discovered");
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.SetIndication, characteristic));
        }
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.BloodPressureFeatureCharacteristic.getUuid()))) {
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.ReadCharacteristic, characteristic));
        }
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.WeightScaleFeatureCharacteristic.getUuid()))) {
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.ReadCharacteristic, characteristic));
        }
        mBleCommunicationExecutor.exec();
    }

    private void onBleCommunicationComplete(Message msg) {
        BleEvent.Type type = BleEvent.Type.values()[msg.what];
        final Object[] objects = (Object[]) msg.obj;
        final BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) objects[0];
        final int gattStatus = (int) objects[1];
        final ErrorCode errorCode = (ErrorCode) objects[2];
        if (null != errorCode) {
            AppLog.e("ble event error. " + errorCode.name());
            disconnect(mTargetPeripheral, DisconnectReason.CommunicationError);
            return;
        }
        AppLog.d(type.name());
        switch (type) {
            case SetNotification: {
                if (GattStatusCode.GATT_SUCCESS != gattStatus) {
                    AppLog.e("Invalid gatt status. status:" + gattStatus);
                    disconnect(mTargetPeripheral, DisconnectReason.GattStatusError);
                    break;
                }
                mBleCommunicationExecutor.exec();
                break;
            }
            case SetIndication: {
                if (GattStatusCode.GATT_SUCCESS != gattStatus) {
                    AppLog.e("Invalid gatt status. status:" + gattStatus);
                    disconnect(mTargetPeripheral, DisconnectReason.GattStatusError);
                    break;
                }
                mBleCommunicationExecutor.exec();
                if (mTargetPeripheral.getStateInfo().isBonded()) {
                    // The IndicationWaitTimer will start when both of indication of
                    // BPM or WM is registered and in Bonded state.The timer will start when the state is Bonded
                    // because indication of BPM or WM is running in Bonded state.
                    startIndicationWaitTimer();
                }
                break;
            }
            case WriteCharacteristic: {
                if (GattUUID.Characteristic.CurrentTimeCharacteristic.getUuid().equals(characteristic.getUuid())) {
                    mIsCtsWritten = true;
                    if (GattStatusCode.GATT_SUCCESS == gattStatus) {
                        mBleCommunicationExecutor.exec();
                    } else if (GattStatusCode.GATT_NO_RESOURCES == gattStatus) {   // 0x80: Write Request Rejected
                        AppLog.i("Write Request Rejected. (0x80)");
                        // If the slave sends error response in CTS,
                        // you don't retry and should send next request.
                        mBleCommunicationExecutor.exec();
                    } else if (GattStatusCode.GATT_ERROR == gattStatus) {   // 0x85: Write Request Rejected
                        AppLog.w("Write Request Rejected. (0x85)");
                        // The status, 0x80 (Data filed ignored) will be notified same status to the application
                        // but there are cases when notified other status, 0x85 to the application in some smartphones.
                        // So the application need to regard as 0x80 only for Current Time Characteristic.
                        mBleCommunicationExecutor.exec();
                    } else {
                        AppLog.e("Invalid gatt status. status:" + gattStatus);
                        disconnect(mTargetPeripheral, DisconnectReason.GattStatusError);
                    }
                } else {
                    if (GattStatusCode.GATT_SUCCESS == gattStatus) {
                        mBleCommunicationExecutor.exec();
                    } else {
                        AppLog.e("Invalid gatt status. status:" + gattStatus);
                        disconnect(mTargetPeripheral, DisconnectReason.GattStatusError);
                    }
                }
                break;
            }
            case ReadCharacteristic: {
                if (GattStatusCode.GATT_SUCCESS != gattStatus) {
                    AppLog.e("Invalid gatt status. status:" + gattStatus);
                    disconnect(mTargetPeripheral, DisconnectReason.GattStatusError);
                    break;
                }
                if (GattUUID.Characteristic.BloodPressureFeatureCharacteristic.getUuid().equals(characteristic.getUuid())) {
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.BPFDataRcv.ordinal(), characteristic.getValue()));
                }
                if (GattUUID.Characteristic.WeightScaleFeatureCharacteristic.getUuid().equals(characteristic.getUuid())) {
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.WSFDataRcv.ordinal(), characteristic.getValue()));
                }
                mBleCommunicationExecutor.exec();
                break;
            }
        }
    }

    private void startIndicationWaitTimer() {
        AppLog.dMethodIn();
        mMessageHandler.sendMessageDelayed(Message.obtain(mMessageHandler,
                MessageType.IndicationWaitTimeout.ordinal()), INDICATION_WAIT_TIME);
    }

    private void stopIndicationWaitTimer() {
        AppLog.dMethodIn();
        mMessageHandler.removeMessages(MessageType.IndicationWaitTimeout.ordinal());
    }

    private void restartIndicationWaitTimer() {
        stopIndicationWaitTimer();
        startIndicationWaitTimer();
    }

    protected enum MessageType {
        BluetoothOff,
        BluetoothOn,
        ConnectionCompleted,
        ConnectionFailed,
        DisconnectionCompleted,
        DisconnectionFailed,
        DidDisconnection,
        Disconnected,
        BondStateChanged,
        AclConnectionStateChanged,
        GattConnectionStateChanged,
        DetailedStateChanged,
        BPFDataRcv,
        BPMDataRcv,
        WMDataRcv,
        WSFDataRcv,
        BatteryDataRcv,
        CTSDataRcv,
        // The waiting time-out message for receiving Indication.
        // After Indication Enable setting, this message will be
        // displayed when not receive the Indication in the prescribed time
        // This is a solution of the  problem in some models. (The  Indication is received in OS level
        // but the OS would return the Indication Confirmation without notification to the app).
        IndicationWaitTimeout,
    }

    private enum DisconnectReason {
        UserRequest,
        CommunicationError,
        GattStatusError,
        IndicationWaitTimeout,
    }

    private static class BleEvent {
        public final Type type;
        public final BluetoothGattCharacteristic characteristic;

        public BleEvent(Type type, BluetoothGattCharacteristic characteristic) {
            this.type = type;
            this.characteristic = characteristic;
        }

        public enum Type {
            SetNotification, SetIndication, WriteCharacteristic, ReadCharacteristic
        }
    }

    private static class BleCommunicationExecutor {
        private final LinkedList<BleEvent> mBleEventList = new LinkedList<>();
        private final BlePeripheral mTargetPeripheral;
        private final Handler mCompletionHandler;
        private boolean mExecuting;

        BleCommunicationExecutor(BlePeripheral targetPeripheral, Handler completionHandler) {
            mTargetPeripheral = targetPeripheral;
            mCompletionHandler = completionHandler;
            mExecuting = false;
        }

        public boolean isExecuting() {
            return mExecuting;
        }

        public void add(BleEvent bleEvent) {
            mBleEventList.add(bleEvent);
        }

        public void clear() {
            mBleEventList.clear();
        }

        public boolean exec() {
            if (mBleEventList.isEmpty()) {
                AppLog.d("event empty.");
                return false;
            }
            if (mExecuting) {
                AppLog.e("event executing.");
                return false;
            }
            final BleEvent bleEvent = mBleEventList.poll();
            final BluetoothGattCharacteristic characteristic = bleEvent.characteristic;
            AppLog.d(bleEvent.type.name());
            switch (bleEvent.type) {
                case SetNotification:
                    mTargetPeripheral.setNotificationEnabled(characteristic, true, new BlePeripheral.SetNotificationResultListener() {
                        @Override
                        public void onComplete(@NonNull String address, BluetoothGattCharacteristic characteristic, int gattStatus, ErrorCode errorCode) {
                            mExecuting = false;
                            Object[] objects = {characteristic, gattStatus, errorCode};
                            mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal(), objects));
                        }
                    });
                    break;
                case SetIndication:
                    mTargetPeripheral.setNotificationEnabled(characteristic, true, new BlePeripheral.SetNotificationResultListener() {
                        @Override
                        public void onComplete(@NonNull String address, BluetoothGattCharacteristic characteristic, int gattStatus, ErrorCode errorCode) {
                            mExecuting = false;
                            Object[] objects = {characteristic, gattStatus, errorCode};
                            mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal(), objects));
                        }
                    });
                    break;
                case WriteCharacteristic:
                    mTargetPeripheral.writeCharacteristic(characteristic, new BlePeripheral.WriteCharacteristicResultListener() {
                        @Override
                        public void onComplete(@NonNull String address, BluetoothGattCharacteristic characteristic, int gattStatus, ErrorCode errorCode) {
                            mExecuting = false;
                            Object[] objects = {characteristic, gattStatus, errorCode};
                            mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal(), objects));
                        }
                    });
                    break;
                case ReadCharacteristic:
                    mTargetPeripheral.readCharacteristic(characteristic, new BlePeripheral.ReadCharacteristicResultListener() {
                        @Override
                        public void onComplete(@NonNull String address, BluetoothGattCharacteristic characteristic, int gattStatus, ErrorCode errorCode) {
                            mExecuting = false;
                            Object[] objects = {characteristic, gattStatus, errorCode};
                            mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal(), objects));
                        }
                    });
                    break;
            }
            mExecuting = true;
            return true;
        }
    }

    public void startService(View view)
    {
        Intent intent = new Intent(this, BPServices.class);
        startService(intent);
    }

    public void stopService(View view)
    {
        Intent intent = new Intent(this, BPServices.class);
        stopService(intent);
    }



}
