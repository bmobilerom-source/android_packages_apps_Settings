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
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Data class for header image information
 */
public class HeaderImageInfo {
    private static final String TAG = "HeaderImageInfo";

    private final String mName;
    private final String mPackageName;
    private final int mDrawableResId;
    private final String mUri;
    private final boolean mIsCustom;
    private Drawable mCachedDrawable;

    public HeaderImageInfo(String name, String packageName, int drawableResId, String uri,
            boolean isCustom) {
        mName = name;
        mPackageName = packageName;
        mDrawableResId = drawableResId;
        mUri = uri;
        mIsCustom = isCustom;
    }

    public String getName() {
        return mName;
    }

    public Drawable getThumbnail(Context context) {
        if (mCachedDrawable != null) {
            return mCachedDrawable;
        }
        if (mDrawableResId == 0) {
            return null;
        }
        try {
            final PackageManager pm = context.getPackageManager();
            final Drawable drawable = pm.getResourcesForApplication(mPackageName)
                    .getDrawable(mDrawableResId, null);
            mCachedDrawable = drawable;
            return drawable;
        } catch (Exception e) {
            Log.w(TAG, "Failed to load thumbnail: " + mPackageName + " resId=" + mDrawableResId, e);
            return null;
        }
    }

    public String getUri() {
        return mUri;
    }

    public boolean isCustom() {
        return mIsCustom;
    }

    @Override
    public String toString() {
        return "HeaderImageInfo{" +
                "name='" + mName + '\'' +
                ", uri='" + mUri + '\'' +
                ", isCustom=" + mIsCustom +
                '}';
    }
}