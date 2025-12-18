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

import android.app.Activity;
import android.content.Context;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.internal.logging.nano.MetricsProto;

/**
 * Controller for launching Private Space dashboard
 */
public class PrivateSpaceLauncherController extends BasePreferenceController {

    public PrivateSpaceLauncherController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!preference.getKey().equals(getPreferenceKey())) {
            return super.handlePreferenceTreeClick(preference);
        }

        try {
            Activity activity = getActivity();
            if (activity == null) {
                android.widget.Toast.makeText(mContext,
                    "Unable to launch Private Space", android.widget.Toast.LENGTH_SHORT).show();
                return true;
            }

            // Launch private space dashboard using SubSettingLauncher (same as Atomichub)
            new SubSettingLauncher(activity)
                    .setDestination("com.android.settings.privatespace.PrivateSpaceDashboardFragment")
                    .setTitleRes(com.android.settings.R.string.private_space_title)
                    .setSourceMetricsCategory(MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                    .launch();

        } catch (Exception e) {
            android.widget.Toast.makeText(mContext,
                "Unable to launch Private Space", android.widget.Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private Activity getActivity() {
        Context context = mContext;
        while (context != null) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            if (context instanceof android.content.ContextWrapper) {
                context = ((android.content.ContextWrapper) context).getBaseContext();
            } else {
                break;
            }
        }
        return null;
    }
}