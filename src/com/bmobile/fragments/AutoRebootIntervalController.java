/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bmobile.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

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
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!(preference instanceof ListPreference)) {
            return;
        }

        ListPreference listPreference = (ListPreference) preference;
        mPreference = listPreference;

        long intervalMs = PrivacySecurityHelper.getAutoRebootInterval(mContext);
        String value = resolveListValue(listPreference, intervalMs);
        intervalMs = Long.parseLong(value);

        if (intervalMs != PrivacySecurityHelper.getAutoRebootInterval(mContext)) {
            PrivacySecurityHelper.setAutoRebootInterval(mContext, intervalMs);
        }

        listPreference.setValue(value);
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

            if (mPreference != null) {
                mPreference.setValue(String.valueOf(intervalMs));
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

    /** Pick exact or closest entry value (milliseconds). */
    private static String resolveListValue(ListPreference listPreference, long intervalMs) {
        CharSequence[] entryValues = listPreference.getEntryValues();
        if (entryValues == null || entryValues.length == 0) {
            return String.valueOf(intervalMs);
        }

        String exact = String.valueOf(intervalMs);
        for (CharSequence entryValue : entryValues) {
            if (entryValue != null && exact.equals(entryValue.toString())) {
                return exact;
            }
        }

        long bestMs = Long.parseLong(entryValues[0].toString());
        long bestDelta = Math.abs(intervalMs - bestMs);
        for (CharSequence entryValue : entryValues) {
            if (entryValue == null) {
                continue;
            }
            long candidate = Long.parseLong(entryValue.toString());
            long delta = Math.abs(intervalMs - candidate);
            if (delta < bestDelta) {
                bestDelta = delta;
                bestMs = candidate;
            }
        }
        return String.valueOf(bestMs);
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
