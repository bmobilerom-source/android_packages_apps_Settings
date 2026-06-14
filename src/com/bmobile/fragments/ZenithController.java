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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/** Launches Zenith (digital wellbeing) when the prebuilt app is installed. */
public class ZenithController extends BasePreferenceController {

    private static final String TAG = "ZenithController";

    public static final String PACKAGE_NAME = "com.etrisad.zenith";
    public static final String LAUNCHER_ACTIVITY = "com.etrisad.zenith.MainActivity";

    public ZenithController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return isInstalled(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    public static boolean isInstalled(@NonNull Context context) {
        try {
            context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void launch(@Nullable Context context) {
        if (context == null) {
            return;
        }
        try {
            final PackageManager pm = context.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(PACKAGE_NAME);
            if (intent == null) {
                intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setClassName(PACKAGE_NAME, LAUNCHER_ACTIVITY);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Zenith", e);
            Toast.makeText(context, R.string.zenith_not_installed, Toast.LENGTH_SHORT).show();
        }
    }
}
