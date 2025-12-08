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
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class GestureSecurityBottomSheet extends BottomSheetDialogFragment {

    public static GestureSecurityBottomSheet newInstance() {
        return new GestureSecurityBottomSheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, 0);
    }
    
    @Override
    public void onStart() {
        super.onStart();
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
            View view = inflater.inflate(R.layout.gesture_security_bottom_sheet, container, false);
            
            if (savedInstanceState == null) {
                try {
                    androidx.fragment.app.FragmentManager fragmentManager = getChildFragmentManager();
                    if (fragmentManager != null && !fragmentManager.isStateSaved()) {
                        fragmentManager.beginTransaction()
                                .replace(R.id.gesture_security_preference_container, new GestureSecurityPreferenceFragment())
                                .commitAllowingStateLoss();
                    }
                } catch (Exception e) {
                    android.util.Log.e("GestureSecurityBottomSheet", "Error loading preference fragment", e);
                }
            }
            
            return view;
        } catch (Exception e) {
            android.util.Log.e("GestureSecurityBottomSheet", "Error in onCreateView", e);
            Context ctx = getContext();
            if (ctx != null) {
                return new android.widget.FrameLayout(ctx);
            }
            return null;
        }
    }

    public static class GestureSecurityPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            try {
                setPreferencesFromResource(R.xml.gesture_security_bottom_sheet, rootKey);
                
                Context context = getContext();
                if (context == null) {
                    return;
                }
                
                android.content.ContentResolver resolver = context.getContentResolver();
                
                // Setup preferences with Settings providers
                SwitchPreferenceCompat clipboardOverlay = findPreference("show_clipboard_overlay");
                if (clipboardOverlay != null) {
                    clipboardOverlay.setChecked(Settings.Secure.getInt(resolver, "show_clipboard_overlay", 1) != 0);
                    clipboardOverlay.setOnPreferenceChangeListener((preference, newValue) -> {
                        Settings.Secure.putInt(resolver, "show_clipboard_overlay", (Boolean) newValue ? 1 : 0);
                        return true;
                    });
                }
                
                SwitchPreferenceCompat noStorageRestrict = findPreference("no_storage_restrict");
                if (noStorageRestrict != null) {
                    noStorageRestrict.setChecked(Settings.Global.getInt(resolver, "no_storage_restrict", 0) != 0);
                    noStorageRestrict.setOnPreferenceChangeListener((preference, newValue) -> {
                        Settings.Global.putInt(resolver, "no_storage_restrict", (Boolean) newValue ? 1 : 0);
                        return true;
                    });
                }
                
                SwitchPreferenceCompat windowIgnoreSecure = findPreference("window_ignore_secure");
                if (windowIgnoreSecure != null) {
                    windowIgnoreSecure.setChecked(Settings.Global.getInt(resolver, "window_ignore_secure", 0) != 0);
                    windowIgnoreSecure.setOnPreferenceChangeListener((preference, newValue) -> {
                        Settings.Global.putInt(resolver, "window_ignore_secure", (Boolean) newValue ? 1 : 0);
                        return true;
                    });
                }
                
                SwitchPreferenceCompat secureLockscreenQs = findPreference("secure_lockscreen_qs_disabled");
                if (secureLockscreenQs != null) {
                    secureLockscreenQs.setChecked(Settings.System.getInt(resolver, "secure_lockscreen_qs_disabled", 0) != 0);
                    secureLockscreenQs.setOnPreferenceChangeListener((preference, newValue) -> {
                        Settings.System.putInt(resolver, "secure_lockscreen_qs_disabled", (Boolean) newValue ? 1 : 0);
                        return true;
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("GestureSecurityBottomSheet", "Error in onCreatePreferences", e);
            }
        }
    }
}
