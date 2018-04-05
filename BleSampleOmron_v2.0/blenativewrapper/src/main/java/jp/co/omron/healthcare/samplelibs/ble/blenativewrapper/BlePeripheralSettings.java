//
//  BlePeripheralSettings.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

public class BlePeripheralSettings {

    @NonNull
    private final String mDeviceId;

    boolean AssistPairingDialog = false;
    boolean UseCreateBond = true;
    boolean AutoPairingEnabled = false;
    String AutoPairingPinCode = "000000";
    boolean StableConnectionEnabled = true;
    long StableConnectionWaitTime = 1500;
    boolean ConnectionRetryEnabled = true;
    long ConnectionRetryDelayTime = 0;
    int ConnectionRetryCount = 5;
    boolean DiscoverServiceRetryEnabled = false;
    long DiscoverServiceRetryDelayTime = 0;
    int DiscoverServiceRetryCount = 3;
    boolean UseRemoveBond = false;
    boolean UseRefreshGatt = false;

    BlePeripheralSettings(@NonNull String deviceId) {
        mDeviceId = deviceId;
    }

    public void setParameter(@NonNull final Bundle bundle) {
        BleLog.dMethodIn("DeviceId:" + mDeviceId);
        BleLog.d("bundle:" + bundle);
        if (bundle.containsKey(Key.AssistPairingDialog.name())) {
            AssistPairingDialog = bundle.getBoolean(Key.AssistPairingDialog.name());
        }
        if (bundle.containsKey(Key.UseCreateBond.name())) {
            UseCreateBond = bundle.getBoolean(Key.UseCreateBond.name());
        }
        if (bundle.containsKey(Key.AutoPairingEnabled.name())) {
            AutoPairingEnabled = bundle.getBoolean(Key.AutoPairingEnabled.name());
        }
        if (bundle.containsKey(Key.AutoPairingPinCode.name())) {
            AutoPairingPinCode = bundle.getString(Key.AutoPairingPinCode.name());
        }
        if (bundle.containsKey(Key.StableConnectionEnabled.name())) {
            StableConnectionEnabled = bundle.getBoolean(Key.StableConnectionEnabled.name());
        }
        if (bundle.containsKey(Key.StableConnectionWaitTime.name())) {
            StableConnectionWaitTime = bundle.getLong(Key.StableConnectionWaitTime.name());
        }
        if (bundle.containsKey(Key.ConnectionRetryEnabled.name())) {
            ConnectionRetryEnabled = bundle.getBoolean(Key.ConnectionRetryEnabled.name());
        }
        if (bundle.containsKey(Key.ConnectionRetryDelayTime.name())) {
            ConnectionRetryDelayTime = bundle.getLong(Key.ConnectionRetryDelayTime.name());
        }
        if (bundle.containsKey(Key.ConnectionRetryCount.name())) {
            ConnectionRetryCount = bundle.getInt(Key.ConnectionRetryCount.name());
        }
        if (bundle.containsKey(Key.DiscoverServiceRetryEnabled.name())) {
            DiscoverServiceRetryEnabled = bundle.getBoolean(Key.DiscoverServiceRetryEnabled.name());
        }
        if (bundle.containsKey(Key.DiscoverServiceRetryDelayTime.name())) {
            DiscoverServiceRetryDelayTime = bundle.getLong(Key.DiscoverServiceRetryDelayTime.name());
        }
        if (bundle.containsKey(Key.DiscoverServiceRetryCount.name())) {
            DiscoverServiceRetryCount = bundle.getInt(Key.DiscoverServiceRetryCount.name());
        }
        if (bundle.containsKey(Key.UseRemoveBond.name())) {
            UseRemoveBond = bundle.getBoolean(Key.UseRemoveBond.name());
        }
        if (bundle.containsKey(Key.UseRefreshGatt.name())) {
            UseRefreshGatt = bundle.getBoolean(Key.UseRefreshGatt.name());
        }
    }

    @NonNull
    public Bundle getParameter(@Nullable List<Key> keys) {
        BleLog.dMethodIn("DeviceId:" + mDeviceId);
        if (null == keys) {
            BleLog.d("get all parameters.");
            keys = Arrays.asList(Key.values());
        }
        final Bundle bundle = new Bundle();
        for (Key key : keys) {
            if (Key.UseCreateBond.equals(key)) {
                bundle.putBoolean(Key.UseCreateBond.name(), UseCreateBond);
            }
            if (Key.AssistPairingDialog.equals(key)) {
                bundle.putBoolean(Key.AssistPairingDialog.name(), AssistPairingDialog);
            }
            if (Key.AutoPairingEnabled.equals(key)) {
                bundle.putBoolean(Key.AutoPairingEnabled.name(), AutoPairingEnabled);
            }
            if (Key.AutoPairingPinCode.equals(key)) {
                bundle.putString(Key.AutoPairingPinCode.name(), AutoPairingPinCode);
            }
            if (Key.StableConnectionEnabled.equals(key)) {
                bundle.putBoolean(Key.StableConnectionEnabled.name(), StableConnectionEnabled);
            }
            if (Key.StableConnectionWaitTime.equals(key)) {
                bundle.putLong(Key.StableConnectionWaitTime.name(), StableConnectionWaitTime);
            }
            if (Key.ConnectionRetryEnabled.equals(key)) {
                bundle.putBoolean(Key.ConnectionRetryEnabled.name(), ConnectionRetryEnabled);
            }
            if (Key.ConnectionRetryDelayTime.equals(key)) {
                bundle.putLong(Key.ConnectionRetryDelayTime.name(), ConnectionRetryDelayTime);
            }
            if (Key.ConnectionRetryCount.equals(key)) {
                bundle.putInt(Key.ConnectionRetryCount.name(), ConnectionRetryCount);
            }
            if (Key.DiscoverServiceRetryEnabled.equals(key)) {
                bundle.putBoolean(Key.DiscoverServiceRetryEnabled.name(), DiscoverServiceRetryEnabled);
            }
            if (Key.DiscoverServiceRetryDelayTime.equals(key)) {
                bundle.putLong(Key.DiscoverServiceRetryDelayTime.name(), DiscoverServiceRetryDelayTime);
            }
            if (Key.DiscoverServiceRetryCount.equals(key)) {
                bundle.putInt(Key.DiscoverServiceRetryCount.name(), DiscoverServiceRetryCount);
            }
            if (Key.UseRemoveBond.equals(key)) {
                bundle.putBoolean(Key.UseRemoveBond.name(), UseRemoveBond);
            }
            if (Key.UseRefreshGatt.equals(key)) {
                bundle.putBoolean(Key.UseRefreshGatt.name(), UseRefreshGatt);
            }
        }
        BleLog.d("bundle:" + bundle);
        return bundle;
    }

    public enum Key {
        AssistPairingDialog,
        UseCreateBond,
        AutoPairingEnabled,
        AutoPairingPinCode,
        StableConnectionEnabled,
        StableConnectionWaitTime,
        ConnectionRetryEnabled,
        ConnectionRetryDelayTime,
        ConnectionRetryCount,
        DiscoverServiceRetryEnabled,
        DiscoverServiceRetryDelayTime,
        DiscoverServiceRetryCount,
        UseRemoveBond,
        UseRefreshGatt
    }
}
