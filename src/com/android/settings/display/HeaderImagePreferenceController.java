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

package com.android.settings.display;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Controller for QS header image preference
 */
public class HeaderImagePreferenceController extends BasePreferenceController {

    private static final String TAG = "HeaderImagePrefCtrl";

    public HeaderImagePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            // Launch the header image gallery
            Intent intent = new Intent(mContext, HeaderImageGalleryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true;
        }
        return super.handlePreferenceTreeClick(preference);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSelectable(true);
    }

    @Override
    public CharSequence getSummary() {
        String currentImage = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER_IMAGE);

        if (currentImage != null && !currentImage.isEmpty()) {
            return mContext.getString(R.string.qs_header_image_custom_selected);
        } else {
            return mContext.getString(R.string.qs_header_image_none_selected);
        }
    }
}