/*
 * Copyright (C) 2025 BashaMobile
 *
 * List preference for auto-reboot interval (milliseconds).
 */

package com.bmobile.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class AutoRebootIntervalController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "AutoRebootInterval";
    private static final long MIN_INTERVAL_MS = 60L * 60L * 1000L;

    private ListPreference mPreference;

    public AutoRebootIntervalController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return PrivacySecurityHelper.isAutoRebootEnabled(mContext)
                ? AVAILABLE : DISABLED_DEPENDENT_SETTING;
    }

    @Override
    public String getSummary() {
        return formatInterval(PrivacySecurityHelper.getAutoRebootInterval(mContext));
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref instanceof ListPreference) {
            bindListPreference((ListPreference) pref);
        }
    }

    @Override
    public void updateState(Preference preference) {
        if (preference == null) {
            return;
        }
        if (!(preference instanceof ListPreference)) {
            return;
        }

        ListPreference listPreference = (ListPreference) preference;
        bindListPreference(listPreference);
        mPreference = listPreference;

        long intervalMs = PrivacySecurityHelper.getAutoRebootInterval(mContext);
        intervalMs = sanitizeStoredInterval(intervalMs);

        final String value = resolveListValue(listPreference, intervalMs);
        intervalMs = Long.parseLong(value);

        if (intervalMs != PrivacySecurityHelper.getAutoRebootInterval(mContext)) {
            PrivacySecurityHelper.setAutoRebootInterval(mContext, intervalMs);
        }

        setListValueSafely(listPreference, value);
        listPreference.setSummary(formatInterval(intervalMs));
        listPreference.setEnabled(PrivacySecurityHelper.isAutoRebootEnabled(mContext));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!(preference instanceof ListPreference)) {
            return false;
        }

        try {
            long intervalMs = Long.parseLong(newValue.toString());
            if (intervalMs < MIN_INTERVAL_MS) {
                Log.w(TAG, "Interval too short, using minimum: " + MIN_INTERVAL_MS);
                intervalMs = MIN_INTERVAL_MS;
            }

            if (!PrivacySecurityHelper.setAutoRebootInterval(mContext, intervalMs)) {
                Log.e(TAG, "Failed to save auto reboot interval");
                return false;
            }

            ContentResolver resolver = mContext.getContentResolver();
            resolver.notifyChange(
                    android.provider.Settings.Secure.getUriFor(
                            android.provider.Settings.Secure.AUTO_REBOOT_DELAY),
                    null, false);
            resolver.notifyChange(
                    android.provider.Settings.Secure.getUriFor(
                            android.provider.Settings.Secure.AUTO_REBOOT_ENABLED),
                    null, false);

            Intent configIntent = new Intent("com.bmobile.action.AUTO_REBOOT_CONFIG_CHANGED");
            configIntent.setPackage(mContext.getPackageName());
            mContext.sendBroadcast(configIntent);

            final String value = String.valueOf(intervalMs);
            if (mPreference != null) {
                setListValueSafely(mPreference, value);
                mPreference.setSummary(formatInterval(intervalMs));
            }

            Log.d(TAG, "Auto reboot interval set to " + formatInterval(intervalMs)
                    + " (" + intervalMs + " ms)");
            return true;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid interval value: " + newValue, e);
            return false;
        }
    }

    private void bindListPreference(ListPreference listPreference) {
        final CharSequence[] entries = mContext.getResources().getTextArray(
                R.array.auto_reboot_interval_entries);
        final CharSequence[] entryValues = mContext.getResources().getTextArray(
                R.array.auto_reboot_interval_values);
        listPreference.setEntries(entries);
        listPreference.setEntryValues(entryValues);
    }

    /** Reject legacy bad values (e.g. 24 ms / 24 hours saved as int). */
    private static long sanitizeStoredInterval(long intervalMs) {
        if (intervalMs < MIN_INTERVAL_MS) {
            return MIN_INTERVAL_MS;
        }
        return intervalMs;
    }

    private static void setListValueSafely(ListPreference listPreference, String value) {
        final int index = indexOfValue(listPreference.getEntryValues(), value);
        if (index >= 0) {
            listPreference.setValueIndex(index);
        } else {
            listPreference.setValueIndex(0);
        }
    }

    /** Pick exact or closest entry value (milliseconds). */
    private static String resolveListValue(ListPreference listPreference, long intervalMs) {
        CharSequence[] entryValues = listPreference.getEntryValues();
        if (entryValues == null || entryValues.length == 0) {
            return String.valueOf(intervalMs);
        }

        String exact = String.valueOf(intervalMs);
        if (indexOfValue(entryValues, exact) >= 0) {
            return exact;
        }

        long bestMs = MIN_INTERVAL_MS;
        long bestDelta = Long.MAX_VALUE;
        for (CharSequence entryValue : entryValues) {
            if (entryValue == null) {
                continue;
            }
            final String text = entryValue.toString();
            if (text.isEmpty()) {
                continue;
            }
            final long candidate;
            try {
                candidate = Long.parseLong(text);
            } catch (NumberFormatException e) {
                continue;
            }
            long delta = Math.abs(intervalMs - candidate);
            if (delta < bestDelta) {
                bestDelta = delta;
                bestMs = candidate;
            }
        }
        return String.valueOf(bestMs);
    }

    private static int indexOfValue(CharSequence[] entryValues, String value) {
        if (value == null || entryValues == null) {
            return -1;
        }
        for (int i = 0; i < entryValues.length; i++) {
            if (entryValues[i] != null && value.contentEquals(entryValues[i])) {
                return i;
            }
        }
        return -1;
    }

    private String formatInterval(long intervalMs) {
        long hours = intervalMs / (60 * 60 * 1000);
        long days = hours / 24;

        if (days >= 1) {
            if (days == 1) {
                return mContext.getString(R.string.auto_reboot_interval_one_day);
            }
            return mContext.getString(R.string.auto_reboot_interval_days, days);
        }
        if (hours >= 1) {
            if (hours == 1) {
                return mContext.getString(R.string.auto_reboot_interval_one_hour);
            }
            return mContext.getString(R.string.auto_reboot_interval_hours, hours);
        }
        long minutes = intervalMs / (60 * 1000);
        return mContext.getString(R.string.auto_reboot_interval_minutes, minutes);
    }
}
