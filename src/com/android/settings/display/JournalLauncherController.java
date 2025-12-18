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

package com.android.settings.display;

import android.content.Context;
import android.content.Intent;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for launching Daily You journal app
 */
public class JournalLauncherController extends BasePreferenceController {

    private static final String JOURNAL_PACKAGE = "com.demizo.daily_you";
    private static final String JOURNAL_ACTIVITY = "com.demizo.daily_you.MainActivity";

    public JournalLauncherController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        android.util.Log.d("JournalLauncherController", "handlePreferenceTreeClick called for key: " + preference.getKey() + ", controller key: " + getPreferenceKey());

        if (!preference.getKey().equals(getPreferenceKey())) {
            android.util.Log.d("JournalLauncherController", "Key mismatch, calling super");
            return super.handlePreferenceTreeClick(preference);
        }

        android.util.Log.d("JournalLauncherController", "Launching Journal app");

        try {
            Intent intent = new Intent();
            intent.setClassName(JOURNAL_PACKAGE, JOURNAL_ACTIVITY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            android.util.Log.d("JournalLauncherController", "Journal app launched successfully");
        } catch (Exception e) {
            android.util.Log.e("JournalLauncherController", "Failed to launch Journal app", e);
            // App not installed or other error
            android.widget.Toast.makeText(mContext,
                "Journal app not installed", android.widget.Toast.LENGTH_SHORT).show();
        }

        return true;
    }
}