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

package com.epic.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.android.settings.R;
import com.android.settings.awaken.fragments.DisplayCustomizationsAdapter;
import com.android.settings.awaken.fragments.DisplayCustomizationsHelper;
import com.android.settings.preferences.ui.AdaptiveSwitchPreference;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class WallpaperBackgroundBottomSheet extends BottomSheetDialogFragment {

    public static WallpaperBackgroundBottomSheet newInstance() {
        return new WallpaperBackgroundBottomSheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set transparent background for the bottom sheet dialog container
        // The content view already has the adaptive background
        if (getDialog() != null && getDialog().getWindow() != null) {
            android.view.Window window = getDialog().getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.wallpaper_background_bottom_sheet, container, false);
            
            if (savedInstanceState == null) {
                try {
                    androidx.fragment.app.FragmentManager fragmentManager = getChildFragmentManager();
                    if (fragmentManager != null && !fragmentManager.isStateSaved()) {
                        fragmentManager.beginTransaction()
                                .replace(R.id.wallpaper_background_preference_container, new WallpaperBackgroundPreferenceFragment())
                                .commitAllowingStateLoss();
                    }
                } catch (Exception e) {
                    android.util.Log.e("WallpaperBackgroundBottomSheet", "Error loading preference fragment", e);
                }
            }
            
            return view;
        } catch (Exception e) {
            android.util.Log.e("WallpaperBackgroundBottomSheet", "Error in onCreateView", e);
            Context ctx = getContext();
            if (ctx != null) {
                return new android.widget.FrameLayout(ctx);
            }
            return null;
        }
    }

    public static class WallpaperBackgroundPreferenceFragment extends PreferenceFragmentCompat {
        private DisplayCustomizationsAdapter mAdapter;
        private AdaptiveSwitchPreference mWallpaperBlurPreference;
        private ListPreference mWallpaperBlurRadiusPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            try {
                setPreferencesFromResource(R.xml.wallpaper_background_bottom_sheet, rootKey);
                
                Context context = getContext();
                if (context == null) {
                    return;
                }
                
                mAdapter = new DisplayCustomizationsAdapter(context);
                
                // Setup wallpaper blur preferences using adapter
                // Note: AdaptiveSwitchPreference extends SwitchPreference, so adapter methods work
                Preference blurPref = findPreference("wallpaper_blur");
                if (blurPref instanceof AdaptiveSwitchPreference) {
                    mWallpaperBlurPreference = (AdaptiveSwitchPreference) blurPref;
                }
                mWallpaperBlurRadiusPreference = findPreference("wallpaper_blur_radius");
                
                if (mWallpaperBlurPreference != null && mWallpaperBlurRadiusPreference != null) {
                    // Setup using adapter - this handles state persistence
                    mAdapter.setupWallpaperBlurPreference(mWallpaperBlurPreference, mWallpaperBlurRadiusPreference);
                    mAdapter.setupWallpaperBlurRadiusPreference(mWallpaperBlurRadiusPreference);
                }
            } catch (Exception e) {
                android.util.Log.e("WallpaperBackgroundBottomSheet", "Error in onCreatePreferences", e);
            }
        }
        
        @Override
        public void onResume() {
            super.onResume();
            // Refresh preference states when bottom sheet is shown
            refreshPreferenceStates();
        }
        
        private void refreshPreferenceStates() {
            Context context = getContext();
            if (context == null) {
                return;
            }
            
            // Refresh wallpaper blur preference state
            if (mWallpaperBlurPreference != null) {
                boolean blurEnabled = DisplayCustomizationsHelper.isWallpaperBlurEnabled(context);
                mWallpaperBlurPreference.setChecked(blurEnabled);
                if (mWallpaperBlurRadiusPreference != null) {
                    mWallpaperBlurRadiusPreference.setEnabled(blurEnabled);
                }
            }
        }
    }
}
