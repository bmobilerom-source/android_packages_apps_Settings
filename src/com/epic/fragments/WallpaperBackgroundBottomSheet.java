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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.android.settings.display.WallpaperBackgroundHelper;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class WallpaperBackgroundBottomSheet extends BottomSheetDialogFragment {

    private static final String KEY_WALLPAPER_BACKGROUND = "settings_wallpaper_background";
    private SwitchMaterial mWallpaperBackgroundSwitch;
    private LottieAnimationView mLottieAnimationView;

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

            Context context = getContext();
            if (context == null) {
                return view;
            }

            // Setup Lottie animation (yes, Lottie files can be added to bottom sheets!)
            mLottieAnimationView = view.findViewById(R.id.illustration_lottie);
            if (mLottieAnimationView != null) {
                try {
                    // Lottie animation is already configured in XML with app:lottie_rawRes="@raw/display"
                    // It will automatically load and play if the file exists
                    // If the file doesn't exist, it will gracefully fail
                } catch (Exception e) {
                    // If display.json doesn't exist, hide Lottie view
                    mLottieAnimationView.setVisibility(View.GONE);
                }
            }

            // Setup toggle switch
            mWallpaperBackgroundSwitch = view.findViewById(R.id.toggle_wallpaper_background);
            if (mWallpaperBackgroundSwitch != null) {
                // Load current state
                boolean enabled = WallpaperBackgroundHelper.isEnabled(context);
                mWallpaperBackgroundSwitch.setChecked(enabled);
                updateSummary(view, context, enabled);

                // Setup listener
                mWallpaperBackgroundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    WallpaperBackgroundHelper.setEnabled(context, isChecked);
                    updateSummary(view, context, isChecked);
                });
            }

            // Info footer is already set up in XML with TextViews

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

    private void updateSummary(View view, Context context, boolean enabled) {
        // Update summary TextView
        TextView summaryView = view.findViewById(R.id.wallpaper_background_summary);
        if (summaryView != null) {
            int summaryResId = WallpaperBackgroundHelper.getSummaryResId(context);
            summaryView.setText(summaryResId);
        }
    }
}

