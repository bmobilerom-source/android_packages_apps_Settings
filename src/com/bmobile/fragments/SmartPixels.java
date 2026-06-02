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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bmobile.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.TwoStatePreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class SmartPixels extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "SmartPixels";
    private static final String SMART_PIXELS_ENABLE = "smart_pixels_enable";
    private static final String SMART_PIXELS_ON_POWER_SAVE = "smart_pixels_on_power_save";
    private static final String SMART_PIXELS_PATTERN = "smart_pixels_pattern";
    private static final String SMART_PIXELS_SHIFT_TIME = "smart_pixels_shift_time";

    private TwoStatePreference mSmartPixelsEnabled;
    private TwoStatePreference mSmartPixelsPowerSave;
    private ListPreference mSmartPixelsPattern;
    private ListPreference mSmartPixelsShiftTime;
    private SmartPixelsObserver mSmartPixelsObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.smart_pixels);

        final Context context = getActivity();
        if (context == null) {
            return;
        }
        final ContentResolver resolver = context.getContentResolver();

        mSmartPixelsEnabled = (TwoStatePreference) findPreference(SMART_PIXELS_ENABLE);
        mSmartPixelsPowerSave = (TwoStatePreference) findPreference(SMART_PIXELS_ON_POWER_SAVE);
        mSmartPixelsPattern = (ListPreference) findPreference(SMART_PIXELS_PATTERN);
        mSmartPixelsShiftTime = (ListPreference) findPreference(SMART_PIXELS_SHIFT_TIME);

        if (mSmartPixelsEnabled != null) {
            mSmartPixelsEnabled.setOnPreferenceChangeListener(this);
            boolean spEnabled = Settings.System.getIntForUser(resolver,
                    Settings.System.SMART_PIXELS_ENABLE, 0, UserHandle.USER_CURRENT) == 1;
            mSmartPixelsEnabled.setChecked(spEnabled);
        }

        if (mSmartPixelsPowerSave != null) {
            mSmartPixelsPowerSave.setOnPreferenceChangeListener(this);
            boolean spEnabledOnPS = Settings.System.getIntForUser(resolver,
                    Settings.System.SMART_PIXELS_ON_POWER_SAVE, 0, UserHandle.USER_CURRENT) == 1;
            mSmartPixelsPowerSave.setChecked(spEnabledOnPS);
        }

        if (mSmartPixelsPattern != null) {
            mSmartPixelsPattern.setOnPreferenceChangeListener(this);
            int pattern = Settings.System.getIntForUser(resolver,
                    Settings.System.SMART_PIXELS_PATTERN, 0, UserHandle.USER_CURRENT);
            mSmartPixelsPattern.setValue(String.valueOf(pattern));
            updatePatternSummary(pattern);
        }

        if (mSmartPixelsShiftTime != null) {
            mSmartPixelsShiftTime.setOnPreferenceChangeListener(this);
            int shiftTime = Settings.System.getIntForUser(resolver,
                    Settings.System.SMART_PIXELS_SHIFT_TIME, 0, UserHandle.USER_CURRENT);
            mSmartPixelsShiftTime.setValue(String.valueOf(shiftTime));
            updateShiftTimeSummary(shiftTime);
        }

        mSmartPixelsObserver = new SmartPixelsObserver(new Handler());
    }

    private void updateSwitchPreferences() {
        final Context mContext = getActivity();
        if (mContext == null) {
            return;
        }
        final ContentResolver resolver = mContext.getContentResolver();
        if (mSmartPixelsEnabled != null) {
            boolean spEnabled = Settings.System.getIntForUser(resolver,
                    Settings.System.SMART_PIXELS_ENABLE, 0, UserHandle.USER_CURRENT) == 1;
            mSmartPixelsEnabled.setChecked(spEnabled);
        }

        if (mSmartPixelsPowerSave != null) {
            boolean spEnabledOnPS = Settings.System.getIntForUser(resolver,
                    Settings.System.SMART_PIXELS_ON_POWER_SAVE, 0, UserHandle.USER_CURRENT) == 1;
            mSmartPixelsPowerSave.setChecked(spEnabledOnPS);
        }

        if (mSmartPixelsPattern != null) {
            int pattern = Settings.System.getIntForUser(resolver,
                    Settings.System.SMART_PIXELS_PATTERN, 0, UserHandle.USER_CURRENT);
            mSmartPixelsPattern.setValue(String.valueOf(pattern));
            updatePatternSummary(pattern);
        }

        if (mSmartPixelsShiftTime != null) {
            int shiftTime = Settings.System.getIntForUser(resolver,
                    Settings.System.SMART_PIXELS_SHIFT_TIMEOUT, 0, UserHandle.USER_CURRENT);
            mSmartPixelsShiftTime.setValue(String.valueOf(shiftTime));
            updateShiftTimeSummary(shiftTime);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSmartPixelsObserver != null) {
            mSmartPixelsObserver.register();
        }
        updateSwitchPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSmartPixelsObserver != null) {
            mSmartPixelsObserver.unregister();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getActivity();
        if (context == null) {
            return false;
        }
        final ContentResolver resolver = context.getContentResolver();
        if (preference == mSmartPixelsEnabled) {
            boolean enabled = (Boolean) newValue;
            boolean success = Settings.System.putIntForUser(resolver,
                    Settings.System.SMART_PIXELS_ENABLE,
                    enabled ? 1 : 0, UserHandle.USER_CURRENT);
            if (success) {
                resolver.notifyChange(Settings.System.getUriFor(
                        Settings.System.SMART_PIXELS_ENABLE), null, false);
                Log.d(TAG, "Smart Pixels enabled: " + enabled);
            } else {
                Log.e(TAG, "Failed to set Smart Pixels enabled: " + enabled);
            }
            return success;
        } else if (preference == mSmartPixelsPowerSave) {
            boolean enabled = (Boolean) newValue;
            boolean success = Settings.System.putIntForUser(resolver,
                    Settings.System.SMART_PIXELS_ON_POWER_SAVE,
                    enabled ? 1 : 0, UserHandle.USER_CURRENT);
            if (success) {
                resolver.notifyChange(Settings.System.getUriFor(
                        Settings.System.SMART_PIXELS_ON_POWER_SAVE), null, false);
                Log.d(TAG, "Smart Pixels on power save: " + enabled);
            } else {
                Log.e(TAG, "Failed to set Smart Pixels on power save: " + enabled);
            }
            return success;
        } else if (preference == mSmartPixelsPattern) {
            int pattern = Integer.parseInt((String) newValue);
            boolean success = Settings.System.putIntForUser(resolver,
                    Settings.System.SMART_PIXELS_PATTERN,
                    pattern, UserHandle.USER_CURRENT);
            if (success) {
                resolver.notifyChange(Settings.System.getUriFor(
                        Settings.System.SMART_PIXELS_PATTERN), null, false);
                updatePatternSummary(pattern);
                Log.d(TAG, "Smart Pixels pattern: " + pattern);
            } else {
                Log.e(TAG, "Failed to set Smart Pixels pattern: " + pattern);
            }
            return success;
        } else if (preference == mSmartPixelsShiftTime) {
            int shiftTime = Integer.parseInt((String) newValue);
            boolean success = Settings.System.putIntForUser(resolver,
                    Settings.System.SMART_PIXELS_SHIFT_TIMEOUT,
                    shiftTime, UserHandle.USER_CURRENT);
            if (success) {
                resolver.notifyChange(Settings.System.getUriFor(
                        Settings.System.SMART_PIXELS_SHIFT_TIMEOUT), null, false);
                updateShiftTimeSummary(shiftTime);
                Log.d(TAG, "Smart Pixels shift time: " + shiftTime);
            } else {
                Log.e(TAG, "Failed to set Smart Pixels shift time: " + shiftTime);
            }
            return success;
        }
        return false;
    }

    private void updatePatternSummary(int pattern) {
        if (mSmartPixelsPattern != null) {
            String[] entries = getResources().getStringArray(R.array.smart_pixels_percent_strings);
            if (pattern >= 0 && pattern < entries.length) {
                String summary = entries[pattern] + "%%";
                mSmartPixelsPattern.setSummary(summary);
            } else {
                mSmartPixelsPattern.setSummary("");
            }
        }
    }

    private void updateShiftTimeSummary(int shiftTime) {
        if (mSmartPixelsShiftTime != null) {
            String[] entries = getResources().getStringArray(R.array.smart_pixels_shift_times);
            if (shiftTime >= 0 && shiftTime < entries.length) {
                String summary = entries[shiftTime].replace("%", "%%");
                mSmartPixelsShiftTime.setSummary(summary);
            } else {
                mSmartPixelsShiftTime.setSummary("");
            }
        }
    }

    private class SmartPixelsObserver extends ContentObserver {
        public SmartPixelsObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            final Context mContext = getActivity();
            if (mContext == null) {
                return;
            }
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SMART_PIXELS_ENABLE), false, this, UserHandle.USER_CURRENT);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SMART_PIXELS_ON_POWER_SAVE), false, this, UserHandle.USER_CURRENT);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SMART_PIXELS_PATTERN), false, this, UserHandle.USER_CURRENT);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SMART_PIXELS_SHIFT_TIMEOUT), false, this, UserHandle.USER_CURRENT);
        }

        public void unregister() {
            final Context mContext = getActivity();
            if (mContext == null) {
                return;
            }
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updateSwitchPreferences();
        }
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.smart_pixels;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
