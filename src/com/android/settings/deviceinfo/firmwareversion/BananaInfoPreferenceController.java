/*
 * Copyright (C) 2020 Wave-OS
 * Copyright (C) 2022 Project Arcana
 * Copyright (C) 2022 BananaDroid
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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.widget.TextView;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;

public class BananaInfoPreferenceController extends AbstractPreferenceController {

    private static final String KEY_BANANA_INFO = "banana_info";

    private static final String PROP_BANANA_VERSION = "ro.modversion";
    private static final String PROP_BANANA_DISPLAY_DATE = "ro.banana.display.date";
    private static final String PROP_BANANA_DEVICE = "ro.banana.device";
    private static final String PROP_BANANA_RELEASETYPE = "ro.banana.releasetype";
    private static final String PROP_BANANA_EDITION = "ro.banana.edition";
    private static final String PROP_BANANA_MAINTAINER = "ro.banana.maintainer";

    public BananaInfoPreferenceController(Context context) {
        super(context);
    }

    private String getbananaVersion() {
        final String version = SystemProperties.get(PROP_BANANA_VERSION,
                this.mContext.getString(R.string.device_info_default));
        final String displayDate = SystemProperties.get(PROP_BANANA_DISPLAY_DATE,
                this.mContext.getString(R.string.device_info_default));

        return version + " | " + displayDate;
    }

    private String getDeviceName() {
        String device = SystemProperties.get(PROP_BANANA_DEVICE, "");
        if (device.equals("")) {
            device = Build.MANUFACTURER + " " + Build.MODEL;
        }
        return device;
    }

    private String getbananaReleaseType() {
        final String releaseType = SystemProperties.get(PROP_BANANA_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
        final String bananaEdition = SystemProperties.get(PROP_BANANA_EDITION,
                this.mContext.getString(R.string.device_info_default));

        return releaseType.substring(0, 1).toUpperCase() +
                 releaseType.substring(1).toLowerCase() + " | " + bananaEdition;
    }

    private String getbananaMaintainer() {
        final String bananaMaintainer = SystemProperties.get(PROP_BANANA_MAINTAINER,
                this.mContext.getString(R.string.device_info_default));

        return bananaMaintainer;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final LayoutPreference BananaInfoPreference = screen.findPreference(KEY_BANANA_INFO);
        final TextView version = (TextView) BananaInfoPreference.findViewById(R.id.version_message);
        final TextView device = (TextView) BananaInfoPreference.findViewById(R.id.device_message);
        final TextView releaseType = (TextView) BananaInfoPreference.findViewById(R.id.release_type_message);
        final TextView maintainer = (TextView) BananaInfoPreference.findViewById(R.id.maintainer_message);
        final String bananaVersion = getbananaVersion();
        final String bananaDevice = getDeviceName();
        final String bananaReleaseType = getbananaReleaseType();
        final String bananaMaintainer = getbananaMaintainer();
        version.setText(bananaVersion);
        device.setText(bananaDevice);
        releaseType.setText(bananaReleaseType);
        maintainer.setText(bananaMaintainer);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_BANANA_INFO;
    }
}
