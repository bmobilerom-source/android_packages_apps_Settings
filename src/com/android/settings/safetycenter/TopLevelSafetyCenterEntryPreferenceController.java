/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.settings.safetycenter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.R;

/** Controller for the SafetyCenter entry in top level Settings. */
public class TopLevelSafetyCenterEntryPreferenceController extends BasePreferenceController {

    private static final String TAG = "TopLevelSafetyCenterEntryPreferenceController";

    public TopLevelSafetyCenterEntryPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // Check config flag first - if disabled in config, hide completely
        if (!mContext.getResources().getBoolean(R.bool.config_safety_center_enabled)) {
            Log.d(TAG, "Safety Center disabled via config");
            return CONDITIONALLY_UNAVAILABLE;
        }
        
        // Check if blocked by user toggle
        if (com.android.settings.applications.specialaccess.BlockSafetyCenterController
                .isBlocked(mContext)) {
            Log.d(TAG, "Safety Center blocked by user");
            return CONDITIONALLY_UNAVAILABLE;
        }
        
        // Check if Safety Center is enabled by system
        if (SafetyCenterManagerWrapper.get().isEnabled(mContext)) {
            return AVAILABLE;
        }
        return CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return super.handlePreferenceTreeClick(preference);
        }

        // Block Safety Center access if blocked
        if (com.android.settings.applications.specialaccess.BlockSafetyCenterController
                .isBlocked(mContext)) {
            Log.d(TAG, "Safety Center access is blocked");
            return true; // Return true to indicate we handled it (by blocking it)
        }

        try {
            mContext.startActivity(new Intent(Intent.ACTION_SAFETY_CENTER)
                    .setPackage(mContext.getPackageManager().getPermissionControllerPackageName()));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Unable to open safety center", e);
            return false;
        }
        return true;
    }
}
