/*
 * Copyright (C) 2020 Wave-OS
 * Copyright (C) 2021 ShapeShiftOS
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

package com.android.settings.deviceinfo.aboutphone;

import java.io.IOException;
import android.content.Context;
import android.os.SystemProperties;
import android.widget.TextView;
import android.util.Log;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.utils.SyberiaSpecUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;
import com.android.settingslib.Utils;
import com.android.settings.core.PreferenceControllerMixin;

public class SyberiaInfoPreferenceController extends AbstractPreferenceController {

    private static final String TAG = "SyberiaInfoPrefCtrl";
    private static final String KEY_SYBERIA_INFO = "syberia_info";

    public SyberiaInfoPreferenceController(Context context) {
        super(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        try {
            final LayoutPreference syberiaInfoPreference = screen.findPreference(KEY_SYBERIA_INFO);
            if (syberiaInfoPreference == null) {
                return;
            }
            
            final TextView processor = (TextView) syberiaInfoPreference.findViewById(R.id.processor_message);
            final TextView storage = (TextView) syberiaInfoPreference.findViewById(R.id.storage_code_message);
            final TextView battery = (TextView) syberiaInfoPreference.findViewById(R.id.battery_type_message);
            final TextView infoScreen = (TextView) syberiaInfoPreference.findViewById(R.id.screen_message);
            
            if (processor != null) {
                try {
                    processor.setText(SyberiaSpecUtils.getProcessorModel());
                } catch (Exception e) {
                    processor.setText("Unknown");
                }
            }
            
            if (storage != null) {
                try {
                    storage.setText(String.valueOf(SyberiaSpecUtils.getTotalInternalMemorySize()) + "GB ROM + " + String.valueOf(SyberiaSpecUtils.getTotalRAM()) + "GB RAM");
                } catch (Exception e) {
                    storage.setText("Unknown");
                }
            }
            
            if (battery != null) {
                try {
                    battery.setText(SyberiaSpecUtils.getBatteryCapacity(mContext) + " mAh");
                } catch (Exception e) {
                    battery.setText("Unknown");
                }
            }
            
            if (infoScreen != null) {
                try {
                    infoScreen.setText(SyberiaSpecUtils.getScreenRes(mContext));
                } catch (Exception e) {
                    infoScreen.setText("Unknown");
                }
            }
        } catch (Exception e) {
            // Silently fail to prevent crashes
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SYBERIA_INFO;
    }
}
