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

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class DashboardStyleSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, OnPreferenceClickListener {

    // Use the same constant from Settings.System to ensure they're always linked
    // This preference key must match Settings.System.SETTINGS_DASHBOARD_STYLE
    private static final String KEY_DASHBOARD_STYLE = Settings.System.SETTINGS_DASHBOARD_STYLE;
    private static final String KEY_COMPACT_DASHBOARD = Settings.System.SETTINGS_COMPACT_DASHBOARD_ENABLED;
    private static final String KEY_DASHBOARD_STYLE_RESET = "dashboard_style_reset";
    private static final String KEY_SYSTEMUI_RESET = "systemui_reset";
    private static final String TAG = "DashboardStyleSettings";
    private static final int DEFAULT_DASHBOARD_STYLE = 8; // Bmobile Expressive Style (default as per user preference)

    private ListPreference mDashboardStyle;
    private androidx.preference.SwitchPreferenceCompat mCompactDashboard;
    private Preference mResetPreference;
    private Preference mSystemUIResetPreference;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        Log.d(TAG, "onCreate called");
        
        try {
            addPreferencesFromResource(R.xml.dashboard_style_settings);
            Log.d(TAG, "Preferences loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading preference XML", e);
            // Show error toast to user
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(),
                        "Error loading dashboard settings: " + e.getMessage(),
                        android.widget.Toast.LENGTH_LONG).show();
            }
            // Don't crash - show error but continue
        }
        
        android.app.Activity activity = getActivity();
        if (activity == null) {
            Log.e(TAG, "Activity is null in onCreate");
            return;
        }
        
        final ContentResolver resolver = activity.getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();
        
        if (prefSet == null) {
            Log.e(TAG, "PreferenceScreen is null after loading XML");
            // Try to create a basic screen
            try {
                setPreferenceScreen(getPreferenceManager().createPreferenceScreen(activity));
                Log.d(TAG, "Created fallback preference screen");
            } catch (Exception e2) {
                Log.e(TAG, "Failed to create preference screen", e2);
                return;
            }
        } else {
            Log.d(TAG, "PreferenceScreen found with " + prefSet.getPreferenceCount() + " preferences");
        }

        Preference dashboardStylePref = findPreference(KEY_DASHBOARD_STYLE);
        if (dashboardStylePref instanceof ListPreference) {
            mDashboardStyle = (ListPreference) dashboardStylePref;
            int dashboardStyle = Settings.System.getIntForUser(resolver,
                    Settings.System.SETTINGS_DASHBOARD_STYLE, DEFAULT_DASHBOARD_STYLE, UserHandle.USER_CURRENT);
            mDashboardStyle.setValue(String.valueOf(dashboardStyle));
            updateSummary(dashboardStyle);
            mDashboardStyle.setOnPreferenceChangeListener(this);
            Log.d(TAG, "Dashboard style preference initialized: " + dashboardStyle);
        } else {
            Log.w(TAG, "Dashboard style preference not found with key: " + KEY_DASHBOARD_STYLE);
        }

        mCompactDashboard = (androidx.preference.SwitchPreferenceCompat) findPreference(KEY_COMPACT_DASHBOARD);
        if (mCompactDashboard != null) {
            boolean compactEnabled = Settings.System.getIntForUser(resolver,
                    Settings.System.SETTINGS_COMPACT_DASHBOARD_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
            mCompactDashboard.setChecked(compactEnabled);
            mCompactDashboard.setOnPreferenceChangeListener(this);
        } else {
            Log.w(TAG, "Compact dashboard preference not found");
        }

        mResetPreference = findPreference(KEY_DASHBOARD_STYLE_RESET);
        if (mResetPreference != null) {
            mResetPreference.setOnPreferenceClickListener(this);
        }

        mSystemUIResetPreference = findPreference(KEY_SYSTEMUI_RESET);
        if (mSystemUIResetPreference != null) {
            mSystemUIResetPreference.setOnPreferenceClickListener(this);
            Log.d(TAG, "SystemUI reset preference found and click listener set");
        } else {
            Log.w(TAG, "SystemUI reset preference NOT found with key: " + KEY_SYSTEMUI_RESET);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mDashboardStyle) {
            String value = (String) newValue;
            int dashboardStyle = Integer.parseInt(value);
            Settings.System.putIntForUser(resolver,
                    Settings.System.SETTINGS_DASHBOARD_STYLE, dashboardStyle,
                    UserHandle.USER_CURRENT);
            updateSummary(dashboardStyle);
            Log.d(TAG, "Dashboard style changed to: " + dashboardStyle);
            
            // Show toast to user
            String[] entries = getResources().getStringArray(R.array.settings_dashboard_style_entries);
            if (dashboardStyle >= 0 && dashboardStyle < entries.length) {
                android.widget.Toast.makeText(getContext(),
                        getString(R.string.dashboard_style_applied, entries[dashboardStyle]),
                        android.widget.Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "Dashboard style index out of bounds: " + dashboardStyle + " (array length: " + entries.length + ")");
            }
            
            // Immediately recreate activity to apply new dashboard style
            // This is the most reliable way to ensure the change is visible
            if (getActivity() != null) {
                getActivity().recreate();
            }
            return true;
        } else if (preference == mCompactDashboard) {
            boolean enabled = (Boolean) newValue;
            try {
                Settings.System.putIntForUser(resolver,
                        Settings.System.SETTINGS_COMPACT_DASHBOARD_ENABLED, enabled ? 1 : 0,
                        UserHandle.USER_CURRENT);
                Log.d(TAG, "Compact dashboard " + (enabled ? "enabled" : "disabled"));
                
                // Show toast to user
                String message = enabled ? "Compact dashboard enabled" : "Compact dashboard disabled";
                android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
                
                // Send broadcast to notify TopLevelSettings
                android.content.Intent intent = new android.content.Intent("com.android.settings.COMPACT_DASHBOARD_CHANGED");
                getActivity().sendBroadcast(intent);
                
                // Post recreate to main thread to avoid crashes during preference change
                View rootView = getView();
                if (rootView != null) {
                    rootView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                getActivity().recreate();
                            }
                        }
                    });
                } else {
                    // Fallback: recreate immediately if view not available
                    if (getActivity() != null) {
                        getActivity().recreate();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error toggling compact dashboard", e);
                android.widget.Toast.makeText(getContext(),
                        "Error changing compact dashboard setting",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Log.d(TAG, "onPreferenceClick called for: " + preference.getKey());
        if (preference == mResetPreference) {
            Log.d(TAG, "Reset preference clicked");
            resetDashboardStyle();
            return true;
        } else if (preference == mSystemUIResetPreference) {
            Log.d(TAG, "SystemUI reset preference clicked");
            reloadSystemUI();
            return true;
        }
        Log.d(TAG, "Unknown preference clicked: " + preference.getKey());
        return false;
    }

    /**
     * Resets the dashboard style to default (V2 Style).
     * Based on AOSP reset pattern.
     */
    private void resetDashboardStyle() {
        ContentResolver resolver = getActivity().getContentResolver();
        if (resolver == null) {
            return;
        }

        // Reset to default style (Bmobile Expressive Style = 8)
        Settings.System.putIntForUser(resolver,
                Settings.System.SETTINGS_DASHBOARD_STYLE,
                DEFAULT_DASHBOARD_STYLE,
                UserHandle.USER_CURRENT);

        // Update preference UI
        if (mDashboardStyle != null) {
            mDashboardStyle.setValue(String.valueOf(DEFAULT_DASHBOARD_STYLE));
            updateSummary(DEFAULT_DASHBOARD_STYLE);
        }

        // Send broadcast to notify TopLevelSettings to reload
        android.content.Intent intent = new android.content.Intent("com.android.settings.DASHBOARD_STYLE_CHANGED");
        getActivity().sendBroadcast(intent);

        // Restart activity to apply default dashboard style
        if (getActivity() != null) {
            getActivity().recreate();
        }
    }

    /**
     * Reloads SystemUI to apply dashboard style changes immediately.
     * Uses multiple methods to ensure SystemUI restarts:
     * 1. Try to send broadcast (may not work if receiver is not exported)
     * 2. Use ActivityManager to kill process (requires proper permissions)
     * 3. Show user-friendly message
     */
    private void reloadSystemUI() {
        Context context = getContext();
        if (context == null) {
            context = getActivity();
        }
        if (context == null) {
            Log.w(TAG, "Context is null, cannot reload SystemUI");
            android.widget.Toast.makeText(getActivity(), "Cannot restart SystemUI - context unavailable",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = false;
        
        // Method 1: Try sending broadcast (may not work if receiver is not exported)
        try {
            android.content.Intent intent = new android.content.Intent();
            intent.setAction("com.android.systemui.action.RESTART");
            intent.setPackage("com.android.systemui");
            intent.setData(android.net.Uri.parse("package:com.android.systemui"));
            intent.addFlags(android.content.Intent.FLAG_RECEIVER_FOREGROUND);
            context.sendBroadcast(intent);
            Log.d(TAG, "SystemUI restart broadcast sent");
            success = true;
        } catch (Exception e) {
            Log.w(TAG, "Broadcast method failed (expected if receiver not exported): " + e.getMessage());
        }

        // Method 2: Try using ActivityManager (may require special permissions)
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                // This may not work without proper system permissions, but worth trying
                am.killBackgroundProcesses("com.android.systemui");
                Log.d(TAG, "SystemUI process kill attempted via ActivityManager");
                success = true;
            }
        } catch (SecurityException e) {
            Log.w(TAG, "ActivityManager method requires system permissions: " + e.getMessage());
        } catch (Exception e) {
            Log.w(TAG, "ActivityManager method failed: " + e.getMessage());
        }

        // Show message to user
        if (success) {
            android.widget.Toast.makeText(context, "SystemUI restart requested",
                    android.widget.Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SystemUI restart request completed");
        } else {
            // If both methods failed, inform user they may need to reboot
            android.widget.Toast.makeText(context, 
                    "SystemUI restart requested. If it doesn't restart, please reboot your device.",
                    android.widget.Toast.LENGTH_LONG).show();
            Log.w(TAG, "SystemUI restart methods may not have worked - user may need to reboot");
        }
    }

    private void updateSummary(int dashboardStyle) {
        if (mDashboardStyle == null) {
            return;
        }
        String[] entries = getResources().getStringArray(R.array.settings_dashboard_style_entries);
        if (entries != null && dashboardStyle >= 0 && dashboardStyle < entries.length) {
            mDashboardStyle.setSummary(entries[dashboardStyle]);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DASHBOARD_SUMMARY;
    }
}

