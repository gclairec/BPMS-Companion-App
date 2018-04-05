//
//  SettingsFragment.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private CheckBoxPreference mAutoPairingEnabledCBPreference;
    private EditTextPreference mAutoPairingPinCodeETPreference;
    private CheckBoxPreference mStableConnectionEnabledCBPreference;
    private EditTextPreference mStableConnectionWaitTimeETPreference;
    private CheckBoxPreference mConnectionRetryEnabledCBPreference;
    private EditTextPreference mConnectionRetryDelayTimeETPreference;
    private EditTextPreference mConnectionRetryCountETPreference;
    private CheckBoxPreference mDiscoverServiceRetryEnabledCBPreference;
    private EditTextPreference mDiscoverServiceRetryDelayTimeETPreference;
    private EditTextPreference mDiscoverServiceRetryCountETPreference;

    public static boolean isAssistPairingDialog(Context context) {
        boolean ret = (boolean) getPreferenceValue(
                context, PreferenceElement.AssistPairingDialog);
        AppLog.d(Boolean.toString(ret));
        return ret;
    }

    public static boolean isCreateBond(Context context) {
        boolean ret = (boolean) getPreferenceValue(
                context, PreferenceElement.UseCreateBond);
        AppLog.d(Boolean.toString(ret));
        return ret;
    }

    public static boolean isAutoPairingEnabled(Context context) {
        boolean ret = (boolean) getPreferenceValue(
                context, PreferenceElement.AutoPairingEnabled);
        AppLog.d(Boolean.toString(ret));
        return ret;
    }

    public static String getAutoPairingPinCode(Context context) {
        String ret = (String) getPreferenceValue(
                context, PreferenceElement.AutoPairingPinCode);
        AppLog.d(ret);
        return ret;
    }

    public static boolean isStableConnectionEnabled(Context context) {
        boolean ret = (boolean) getPreferenceValue(
                context, PreferenceElement.StableConnectionEnabled);
        AppLog.d(Boolean.toString(ret));
        return ret;
    }

    public static long getStableConnectionWaitTime(Context context) {
        long ret = (long) getPreferenceValue(
                context, PreferenceElement.StableConnectionWaitTime);
        AppLog.d(Long.toString(ret));
        return ret;
    }

    public static boolean isConnectionRetryEnabled(Context context) {
        boolean ret = (boolean) getPreferenceValue(
                context, PreferenceElement.ConnectionRetryEnabled);
        AppLog.d(Boolean.toString(ret));
        return ret;
    }

    public static long getConnectionRetryDelayTime(Context context) {
        long ret = (long) getPreferenceValue(
                context, PreferenceElement.ConnectionRetryDelayTime);
        AppLog.d(Long.toString(ret));
        return ret;
    }

    public static int getConnectionRetryCount(Context context) {
        int ret = (int) getPreferenceValue(
                context, PreferenceElement.ConnectionRetryCount);
        AppLog.d(Integer.toString(ret));
        return ret;
    }

    public static boolean isDiscoverServiceRetryEnabled(Context context) {
        boolean ret = (boolean) getPreferenceValue(
                context, PreferenceElement.DiscoverServiceRetryEnabled);
        AppLog.d(Boolean.toString(ret));
        return ret;
    }

    public static long getDiscoverServiceRetryDelayTime(Context context) {
        long ret = (long) getPreferenceValue(
                context, PreferenceElement.DiscoverServiceRetryDelayTime);
        AppLog.d(Long.toString(ret));
        return ret;
    }

    public static int getDiscoverServiceRetryCount(Context context) {
        int ret = (int) getPreferenceValue(
                context, PreferenceElement.DiscoverServiceRetryCount);
        AppLog.d(Integer.toString(ret));
        return ret;
    }

    public static boolean isWriteCTS(Context context) {
        boolean ret = (boolean) getPreferenceValue(
                context, PreferenceElement.WriteCts);
        AppLog.d(Boolean.toString(ret));
        return ret;
    }

    public static boolean isRemoveBond(Context context) {
        boolean ret = (boolean) getPreferenceValue(
                context, PreferenceElement.UseRemoveBond);
        AppLog.d(Boolean.toString(ret));
        return ret;
    }

    public static boolean isRefreshGatt(Context context) {
        boolean ret = (boolean) getPreferenceValue(
                context, PreferenceElement.UseRefreshGatt);
        AppLog.d(Boolean.toString(ret));
        return ret;
    }

    private static Object getPreferenceValue(Context context, PreferenceElement element) {
        if (element.valueType == Boolean.class) {
            return getBooleanPreferenceValue(context, element);
        } else if (element.valueType == String.class) {
            return getStringPreferenceValue(context, element);
        } else if (element.valueType == Integer.class) {
            String ret = getStringPreferenceValue(context, element);
            return Integer.parseInt(ret);
        } else if (element.valueType == Long.class) {
            String ret = getStringPreferenceValue(context, element);
            return Long.parseLong(ret);
        }
        return null;
    }

    private static boolean getBooleanPreferenceValue(Context context, PreferenceElement element) {
        return getBooleanPreferenceValue(context, element.preferenceKeyResourceId, (Boolean) element.defaultValue);
    }

    private static boolean getBooleanPreferenceValue(
            Context context, int preferenceKey, boolean defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getResources().getString(preferenceKey);
        return pref.getBoolean(key, defaultValue);
    }

    private static String getStringPreferenceValue(Context context, PreferenceElement element) {
        return getStringPreferenceValue(context, element.preferenceKeyResourceId, (String) element.defaultValue);
    }

    private static String getStringPreferenceValue(
            Context context, int preferenceKey, String defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getResources().getString(preferenceKey);
        return pref.getString(key, defaultValue);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        addPreferencesFromResource(R.xml.settings);

        mAutoPairingEnabledCBPreference = (CheckBoxPreference) findPreference(
                getString(R.string.key_auto_pairing_enabled));
        mAutoPairingEnabledCBPreference.setOnPreferenceChangeListener(this);

        mAutoPairingPinCodeETPreference = (EditTextPreference) findPreference(
                getString(R.string.key_auto_pairing_pin_code));
        mAutoPairingPinCodeETPreference.setOnPreferenceChangeListener(this);

        mStableConnectionEnabledCBPreference = (CheckBoxPreference) findPreference(
                getString(R.string.key_stable_connection_enabled));
        mStableConnectionEnabledCBPreference.setOnPreferenceChangeListener(this);

        mStableConnectionWaitTimeETPreference = (EditTextPreference) findPreference(
                getString(R.string.key_stable_connection_wait_time));
        mStableConnectionWaitTimeETPreference.setOnPreferenceChangeListener(this);

        mConnectionRetryEnabledCBPreference = (CheckBoxPreference) findPreference(
                getString(R.string.key_connection_retry_enabled));
        mConnectionRetryEnabledCBPreference.setOnPreferenceChangeListener(this);

        mConnectionRetryDelayTimeETPreference = (EditTextPreference) findPreference(
                getString(R.string.key_connection_retry_delay_time));
        mConnectionRetryDelayTimeETPreference.setOnPreferenceChangeListener(this);

        mConnectionRetryCountETPreference = (EditTextPreference) findPreference(
                getString(R.string.key_connection_retry_count));
        mConnectionRetryCountETPreference.setOnPreferenceChangeListener(this);

        mDiscoverServiceRetryEnabledCBPreference = (CheckBoxPreference) findPreference(
                getString(R.string.key_discover_service_retry_enabled));
        mDiscoverServiceRetryEnabledCBPreference.setOnPreferenceChangeListener(this);

        mDiscoverServiceRetryDelayTimeETPreference = (EditTextPreference) findPreference(
                getString(R.string.key_discover_service_retry_delay_time));
        mDiscoverServiceRetryDelayTimeETPreference.setOnPreferenceChangeListener(this);

        mDiscoverServiceRetryCountETPreference = (EditTextPreference) findPreference(
                getString(R.string.key_discover_service_retry_count));
        mDiscoverServiceRetryCountETPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_initialize_settings:
                reset();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String preferenceKey = preference.getKey();

        if (preferenceKey.equals(mAutoPairingEnabledCBPreference.getKey())) {
            mAutoPairingPinCodeETPreference.setEnabled((boolean) newValue);
            return true;
        }

        if (preferenceKey.equals(mAutoPairingPinCodeETPreference.getKey())) {
            mAutoPairingPinCodeETPreference.setSummary(newValue.toString());
            return true;
        }

        if (preferenceKey.equals(mStableConnectionEnabledCBPreference.getKey())) {
            mStableConnectionWaitTimeETPreference.setEnabled((boolean) newValue);
            return true;
        }

        if (preferenceKey.equals(mStableConnectionWaitTimeETPreference.getKey())) {
            mStableConnectionWaitTimeETPreference.setSummary(
                    getString(R.string.summary_format_time, newValue.toString()));
            return true;
        }

        if (preferenceKey.equals(mConnectionRetryEnabledCBPreference.getKey())) {
            mConnectionRetryDelayTimeETPreference.setEnabled((boolean) newValue);
            mConnectionRetryCountETPreference.setEnabled((boolean) newValue);
            return true;
        }

        if (preferenceKey.equals(mConnectionRetryDelayTimeETPreference.getKey())) {
            mConnectionRetryDelayTimeETPreference.setSummary(
                    getString(R.string.summary_format_time, newValue.toString()));
            return true;
        }

        if (preferenceKey.equals(mConnectionRetryCountETPreference.getKey())) {
            mConnectionRetryCountETPreference.setSummary(newValue.toString());
            return true;
        }

        if (preferenceKey.equals(mDiscoverServiceRetryEnabledCBPreference.getKey())) {
            mDiscoverServiceRetryDelayTimeETPreference.setEnabled((boolean) newValue);
            mDiscoverServiceRetryCountETPreference.setEnabled((boolean) newValue);
            return true;
        }

        if (preferenceKey.equals(mDiscoverServiceRetryDelayTimeETPreference.getKey())) {
            mDiscoverServiceRetryDelayTimeETPreference.setSummary(
                    getString(R.string.summary_format_time, newValue.toString()));
            return true;
        }

        if (preferenceKey.equals(mDiscoverServiceRetryCountETPreference.getKey())) {
            mDiscoverServiceRetryCountETPreference.setSummary(newValue.toString());
            return true;
        }

        return false;
    }

    private void refresh() {
        for (PreferenceElement element : PreferenceElement.values()) {
            Preference preference = findPreference(getActivity().getString(element.preferenceKeyResourceId));
            if (null == preference) {
                continue;
            }
            if (element.valueType == Boolean.class) {
                ((CheckBoxPreference) preference).setChecked(getBooleanPreferenceValue(getActivity(), element));
            } else {
                ((EditTextPreference) preference).setText(getStringPreferenceValue(getActivity(), element));
            }
        }

        mAutoPairingPinCodeETPreference.setEnabled(
                mAutoPairingEnabledCBPreference.isChecked());
        mAutoPairingPinCodeETPreference.setSummary(
                mAutoPairingPinCodeETPreference.getText());

        mStableConnectionWaitTimeETPreference.setEnabled(
                mStableConnectionEnabledCBPreference.isChecked());
        mStableConnectionWaitTimeETPreference.setSummary(
                getString(R.string.summary_format_time, mStableConnectionWaitTimeETPreference.getText()));

        mConnectionRetryDelayTimeETPreference.setEnabled(
                mConnectionRetryEnabledCBPreference.isChecked());
        mConnectionRetryDelayTimeETPreference.setSummary(
                getString(R.string.summary_format_time, mConnectionRetryDelayTimeETPreference.getText()));
        mConnectionRetryCountETPreference.setEnabled(
                mConnectionRetryEnabledCBPreference.isChecked());
        mConnectionRetryCountETPreference.setSummary(
                mConnectionRetryCountETPreference.getText());

        mDiscoverServiceRetryDelayTimeETPreference.setEnabled(
                mDiscoverServiceRetryEnabledCBPreference.isChecked());
        mDiscoverServiceRetryDelayTimeETPreference.setSummary(
                getString(R.string.summary_format_time, mDiscoverServiceRetryDelayTimeETPreference.getText()));
        mDiscoverServiceRetryCountETPreference.setEnabled(
                mDiscoverServiceRetryEnabledCBPreference.isChecked());
        mDiscoverServiceRetryCountETPreference.setSummary(
                mDiscoverServiceRetryCountETPreference.getText());
    }

    public void reset() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().apply();
        refresh();
    }

    private enum PreferenceElement {
        AssistPairingDialog(R.string.key_assist_pairing_dialog, Boolean.class, false),
        UseCreateBond(R.string.key_use_create_bond, Boolean.class, true),
        AutoPairingEnabled(R.string.key_auto_pairing_enabled, Boolean.class, false),
        AutoPairingPinCode(R.string.key_auto_pairing_pin_code, String.class, "000000"),
        StableConnectionEnabled(R.string.key_stable_connection_enabled, Boolean.class, false),
        StableConnectionWaitTime(R.string.key_stable_connection_wait_time, Long.class, "1000"),
        ConnectionRetryEnabled(R.string.key_connection_retry_enabled, Boolean.class, true),
        ConnectionRetryDelayTime(R.string.key_connection_retry_delay_time, Long.class, "0"),
        ConnectionRetryCount(R.string.key_connection_retry_count, Integer.class, "5"),
        DiscoverServiceRetryEnabled(R.string.key_discover_service_retry_enabled, Boolean.class, false),
        DiscoverServiceRetryDelayTime(R.string.key_discover_service_retry_delay_time, Long.class, "0"),
        DiscoverServiceRetryCount(R.string.key_discover_service_retry_count, Integer.class, "3"),
        WriteCts(R.string.key_write_cts, Boolean.class, true),
        UseRemoveBond(R.string.key_use_remove_bond, Boolean.class, false),
        UseRefreshGatt(R.string.key_use_refresh_gatt, Boolean.class, false),;
        @StringRes
        public final int preferenceKeyResourceId;
        public final Class valueType;
        public final Object defaultValue;

        PreferenceElement(int preferenceKeyResourceId, Class valueType, Object defaultValue) {
            this.preferenceKeyResourceId = preferenceKeyResourceId;
            this.valueType = valueType;
            this.defaultValue = defaultValue;
        }
    }
}
