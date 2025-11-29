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

package com.epic.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class DisplayPageColors extends SettingsPreferenceFragment {

    private static final String TAG = "DisplayPageColors";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.display_page_colors);
        
        // Handle Live Display preference click
        Preference liveDisplayPref = findPreference("live_display");
        if (liveDisplayPref != null) {
            liveDisplayPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    launchLiveDisplay();
                    return true;
                }
            });
        }
    }

    private void launchLiveDisplay() {
        try {
            // Try LineageParts LiveDisplay
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("org.lineageos.lineageparts", 
                "org.lineageos.lineageparts.PartsActivity"));
            intent.putExtra(":settings:show_fragment", 
                "org.lineageos.lineageparts.livedisplay.LiveDisplaySettings");
            intent.putExtra(":settings:show_fragment_title", 
                getString(R.string.live_display_title));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            PackageManager pm = getContext().getPackageManager();
            if (pm.resolveActivity(intent, 0) != null) {
                startActivity(intent);
                return;
            }
            
            // Fallback: Try PART action
            intent = new Intent("org.lineageos.lineageparts.PART.livedisplay");
            intent.setComponent(new ComponentName("org.lineageos.lineageparts", 
                "org.lineageos.lineageparts.PartsActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            if (pm.resolveActivity(intent, 0) != null) {
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Live Display", e);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

