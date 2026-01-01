/*
 * Copyright (C) 2025 bmobile
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

package com.android.settings.location;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

/**
 * Controller for mock location toggle.
 * Allows users to enable/disable mock location providers.
 * When enabled, apps can inject fake GPS locations.
 */
public class MockLocationController extends TogglePreferenceController {

    private static final String SETTINGS_KEY = Settings.Secure.ALLOW_MOCK_LOCATION;
    private static final String TAG = "MockLocation";
    private SettingObserver mSettingObserver;
    private Preference mPreference;

    public MockLocationController(Context context, String key) {
        super(context, key);
        mSettingObserver = new SettingObserver(new Handler(Looper.getMainLooper()));
        mContext.getContentResolver().registerContentObserver(
            Settings.Secure.getUriFor(SETTINGS_KEY),
            false,
            mSettingObserver
        );
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        if (mPreference != null && mPreference instanceof SwitchPreference) {
            SwitchPreference switchPref = (SwitchPreference) mPreference;
            updateSummary(switchPref);
        }
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(), SETTINGS_KEY, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        boolean result = Settings.Secure.putInt(mContext.getContentResolver(), SETTINGS_KEY, isChecked ? 1 : 0);
        if (result) {
            Log.d(TAG, "Mock location " + (isChecked ? "enabled" : "disabled"));
            if (mPreference instanceof SwitchPreference) {
                updateSummary((SwitchPreference) mPreference);
            }
        }
        return result;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SwitchPreference) {
            updateSummary((SwitchPreference) preference);
        }
    }

    private void updateSummary(SwitchPreference preference) {
        boolean isEnabled = isChecked();
        if (isEnabled) {
            preference.setSummary(mContext.getString(R.string.mock_location_enabled_warning));
        } else {
            preference.setSummary(mContext.getString(R.string.mock_location_disabled_summary));
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }

    /**
     * Check if mock locations are currently enabled.
     * Static method for use by other classes.
     */
    public static boolean isMockLocationEnabled(Context context) {
        if (context == null) {
            return false;
        }
        return Settings.Secure.getInt(context.getContentResolver(), SETTINGS_KEY, 0) != 0;
    }

    /**
     * Observer for mock location setting changes
     */
    private class SettingObserver extends ContentObserver {
        public SettingObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri.equals(Settings.Secure.getUriFor(SETTINGS_KEY))) {
                if (mPreference != null) {
                    updateState(mPreference);
                }
            }
        }
    }
}

