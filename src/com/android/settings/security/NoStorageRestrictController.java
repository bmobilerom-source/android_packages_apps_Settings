/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.settings.security;

import android.content.Context;
import android.content.ContentResolver;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

/**
 * Controller for no storage restrict setting
 */
public class NoStorageRestrictController extends TogglePreferenceController {

    private static final String TAG = "NoStorageRestrictController";

    public NoStorageRestrictController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public boolean isChecked() {
        ContentResolver resolver = mContext.getContentResolver();
        return Settings.Global.getInt(resolver, "no_storage_restrict", 0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        ContentResolver resolver = mContext.getContentResolver();
        return Settings.Global.putInt(resolver, "no_storage_restrict", isChecked ? 1 : 0);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_security;
    }
}
