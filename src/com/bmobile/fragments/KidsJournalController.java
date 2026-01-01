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
import android.content.pm.PackageManager;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for Daily Journal preference.
 * Only shows the preference if Daily Journal is installed.
 */
public class KidsJournalController extends BasePreferenceController {

    private static final String PACKAGE_NAME = "com.demizo.daily_you";

    public KidsJournalController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return isAppInstalled() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    private boolean isAppInstalled() {
        try {
            mContext.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
