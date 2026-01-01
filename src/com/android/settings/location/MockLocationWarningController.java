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
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Controller for mock location warning preference.
 * Shows a warning message when mock locations are enabled.
 * Provides a link to developer settings to disable mock location apps.
 */
public class MockLocationWarningController extends BasePreferenceController {

    private static final String SETTINGS_KEY = Settings.Secure.ALLOW_MOCK_LOCATION;
    private SettingObserver mSettingObserver;
    private Preference mPreference;

    public MockLocationWarningController(Context context, String key) {
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
        // Only show warning when mock locations are enabled
        if (isMockLocationEnabled()) {
            return AVAILABLE;
        }
        return CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        if (mPreference != null) {
            updatePreference();
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        updatePreference();
    }

    private void updatePreference() {
        if (mPreference == null) {
            return;
        }

        if (isMockLocationEnabled()) {
            mPreference.setTitle(mContext.getString(R.string.mock_location_warning_title));
            mPreference.setSummary(mContext.getString(R.string.mock_location_warning_summary));
            mPreference.setVisible(true);
            
            // Make it clickable to open developer settings
            mPreference.setOnPreferenceClickListener(preference -> {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("MockLocationWarning", "Failed to open developer settings", e);
                }
                return true;
            });
        } else {
            mPreference.setVisible(false);
        }
    }

    private boolean isMockLocationEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(), SETTINGS_KEY, 0) != 0;
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

