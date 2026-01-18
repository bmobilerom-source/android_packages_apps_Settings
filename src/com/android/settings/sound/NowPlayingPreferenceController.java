/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.android.settings.sound;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class NowPlayingPreferenceController extends BasePreferenceController {

    @VisibleForTesting
    static final Intent NOW_PLAYING_INTENT = new Intent(
            "android.settings.AMBIENT_MUSIC_SETTINGS");

    private final PackageManager mPackageManager;

    public NowPlayingPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mPackageManager = context.getPackageManager();
    }

    @Override
    public int getAvailabilityStatus() {
        // Hide Now Playing preference
        return UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference preference = screen.findPreference(getPreferenceKey());
        if (preference != null && isAvailable()) {
            preference.setLayoutResource(R.layout.adaptive_preference_card_middle);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setIntent(NOW_PLAYING_INTENT);
    }
}
