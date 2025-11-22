/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.android.settings.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class BluetoothTimeoutPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "BluetoothTimeoutPrefCtrl";

    public static final int FALLBACK_BLUETOOTH_TIMEOUT_VALUE = 0;

    private final String mBluetoothTimeoutKey;

    protected BluetoothAdapter mBluetoothAdapter;

    public BluetoothTimeoutPreferenceController(Context context, String key) {
        super(context, key);
        mBluetoothTimeoutKey = key;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            return;
        }
    }

    @Override
    public int getAvailabilityStatus() {
        if (mBluetoothAdapter != null) {
            return UserManager.get(mContext).isAdminUser() ? AVAILABLE : DISABLED_FOR_USER;
        }
        return UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public String getPreferenceKey() {
        return mBluetoothTimeoutKey;
    }

    @Override
    public void updateState(Preference preference) {
        final ListPreference timeoutListPreference = (ListPreference) preference;
        final long currentTimeout = Settings.Global.getLong(mContext.getContentResolver(),
                Settings.Global.BLUETOOTH_OFF_TIMEOUT, FALLBACK_BLUETOOTH_TIMEOUT_VALUE);
        timeoutListPreference.setValue(String.valueOf(currentTimeout));
        updateTimeoutPreferenceDescription(timeoutListPreference,
                Long.parseLong(timeoutListPreference.getValue()));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            long value = Long.parseLong((String) newValue);
            Settings.Global.putLong(mContext.getContentResolver(), Settings.Global.BLUETOOTH_OFF_TIMEOUT, value);
            updateTimeoutPreferenceDescription((ListPreference) preference, value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist bluetooth timeout setting", e);
        }
        return true;
    }

    public static CharSequence getTimeoutDescription(
            long currentTimeout, CharSequence[] entries, CharSequence[] values) {
        if (currentTimeout < 0 || entries == null || values == null
                || values.length != entries.length) {
            return null;
        }

        for (int i = 0; i < values.length; i++) {
            long timeout = Long.parseLong(values[i].toString());
            if (currentTimeout == timeout) {
                return entries[i];
            }
        }
        return null;
    }

    private void updateTimeoutPreferenceDescription(ListPreference preference,
            long currentTimeout) {
        final CharSequence[] entries = preference.getEntries();
        final CharSequence[] values = preference.getEntryValues();
        final CharSequence timeoutDescription = getTimeoutDescription(
                currentTimeout, entries, values);
        String summary = "";
        if (timeoutDescription != null) {
            if (currentTimeout != 0) {
                summary = mContext.getString(R.string.bluetooth_timeout_summary, timeoutDescription);
            } else {
                summary = mContext.getString(R.string.bluetooth_timeout_summary_never);
            }
        }
        preference.setSummary(summary);
    }
}

