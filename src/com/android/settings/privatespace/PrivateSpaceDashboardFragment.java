/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.settings.privatespace;

import static com.android.settings.privatespace.PrivateSpaceAuthenticationActivity.EXTRA_SHOW_PRIVATE_SPACE_UNLOCKED;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

/** Fragment representing the Private Space dashboard in Settings. */
@SearchIndexable
public class PrivateSpaceDashboardFragment extends DashboardFragment {
    private static final String TAG = "PSDashboardFragment";

    @Override
    public void onCreate(Bundle icicle) {
        if (android.os.Flags.allowPrivateProfile()
                && android.multiuser.Flags.enablePrivateSpaceFeatures()) {
            super.onCreate(icicle);
            if (icicle == null
                    && getIntent().getBooleanExtra(EXTRA_SHOW_PRIVATE_SPACE_UNLOCKED, false)) {
                Log.i(TAG, "Private space unlocked showing toast");
                Drawable drawable =
                        getContext().getDrawable(R.drawable.ic_private_space_unlock_icon);
                Toast.makeCustomToastWithIcon(
                                getContext(),
                                null /* looper */,
                                getContext().getString(R.string.private_space_unlocked),
                                Toast.LENGTH_SHORT,
                                drawable)
                        .show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (PrivateSpaceMaintainer.getInstance(getContext()).isPrivateSpaceLocked()) {
            // To make sure the task is removed if it is the last activity in that stack.
            getActivity().finishAndRemoveTask();
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.private_space_settings;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.PRIVATE_SPACE_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
    
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.private_space_settings) {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    // Completely block Private Space from search
                    return null;
                }
                
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    // Always disable Private Space from search
                    return false;
                }
                
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    // Hide all preferences from search
                    List<String> keys = super.getNonIndexableKeys(context);
                    if (keys == null) {
                        keys = new ArrayList<>();
                    }
                    // Block all private space keys
                    keys.add("private_space");
                    keys.add("private_space_settings");
                    return keys;
                }
            };
}
