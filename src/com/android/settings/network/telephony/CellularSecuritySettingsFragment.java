/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.android.settings.network.telephony;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SubscriptionManager;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.network.SubscriptionUtil;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.RestrictedPreference;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Cellular Security features (insecure network notifications, network security controls, etc)
 */
public class CellularSecuritySettingsFragment extends DashboardFragment {

    private static final String TAG = "CellularSecuritySettingsFragment";

    public static final String KEY_CELLULAR_SECURITY_PREFERENCE = "cellular_security";

    private int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.CELLULAR_SECURITY_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.cellular_security;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        
        // Get subscription ID from intent or arguments, or use default
        android.content.Intent intent = getActivity() != null ? getActivity().getIntent() : null;
        if (intent != null) {
            mSubId = intent.getIntExtra(android.provider.Settings.EXTRA_SUB_ID,
                    SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        }
        
        if (mSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID && getArguments() != null) {
            mSubId = getArguments().getInt(android.provider.Settings.EXTRA_SUB_ID,
                    SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        }
        
        if (mSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            // Get default subscription
            android.telephony.SubscriptionInfo info = 
                SubscriptionUtil.getSubscriptionOrDefault(context, mSubId);
            if (info != null) {
                mSubId = info.getSubscriptionId();
            } else {
                // Fallback to default subscription ID
                mSubId = SubscriptionManager.getDefaultSubscriptionId();
            }
        }
        
        Log.d(TAG, "Using subscription ID: " + mSubId);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        super.onCreatePreferences(bundle, rootKey);
        setPreferencesFromResource(R.xml.cellular_security, rootKey);
        
        // Initialize controllers with subscription ID after preferences are loaded
        initializeControllers();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh controller states
        initializeControllers();
        
        // Ensure preferences are enabled and not restricted
        ensurePreferencesEnabled();
    }

    private void ensurePreferencesEnabled() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            return;
        }
        
        // Enable cellular security notifications preference
        Preference notificationsPref = screen.findPreference("cellular_security_notifications");
        if (notificationsPref != null) {
            notificationsPref.setEnabled(true);
            notificationsPref.setSelectable(true);
            if (notificationsPref instanceof RestrictedPreference) {
                ((RestrictedPreference) notificationsPref).setDisabledByAdmin(null);
            }
            if (notificationsPref instanceof SwitchPreferenceCompat) {
                ((SwitchPreferenceCompat) notificationsPref).setEnabled(true);
            }
        }
        
        // Enable require cellular encryption preference
        Preference encryptionPref = screen.findPreference("require_cellular_encryption");
        if (encryptionPref != null) {
            encryptionPref.setEnabled(true);
            encryptionPref.setSelectable(true);
            if (encryptionPref instanceof RestrictedPreference) {
                ((RestrictedPreference) encryptionPref).setDisabledByAdmin(null);
            }
            if (encryptionPref instanceof SwitchPreferenceCompat) {
                ((SwitchPreferenceCompat) encryptionPref).setEnabled(true);
            }
        }
    }

    private void initializeControllers() {
        Collection<List<AbstractPreferenceController>> controllerLists = getPreferenceControllers();
        if (controllerLists == null || controllerLists.isEmpty()) {
            Log.w(TAG, "No controllers found");
            return;
        }
        
        if (!SubscriptionManager.isValidSubscriptionId(mSubId)) {
            Log.w(TAG, "Invalid subscription ID: " + mSubId);
            return;
        }
        
        // Flatten the collection of lists into a single list
        List<AbstractPreferenceController> controllers = new ArrayList<>();
        for (List<AbstractPreferenceController> controllerList : controllerLists) {
            if (controllerList != null) {
                controllers.addAll(controllerList);
            }
        }
        
        for (AbstractPreferenceController controller : controllers) {
            if (controller instanceof TelephonyTogglePreferenceController) {
                TelephonyTogglePreferenceController telephonyController = 
                    (TelephonyTogglePreferenceController) controller;
                
                // Set subscription ID using reflection since mSubId is protected
                try {
                    Field subIdField = TelephonyTogglePreferenceController.class
                            .getDeclaredField("mSubId");
                    subIdField.setAccessible(true);
                    subIdField.setInt(telephonyController, mSubId);
                    Log.d(TAG, "Set subId " + mSubId + " for controller: " + 
                        controller.getClass().getSimpleName());
                } catch (Exception e) {
                    Log.e(TAG, "Failed to set subscription ID for controller: " + 
                        controller.getClass().getSimpleName(), e);
                }
                
                // Special handling for CellularSecurityNotificationsPreferenceController
                if (controller instanceof CellularSecurityNotificationsPreferenceController) {
                    CellularSecurityNotificationsPreferenceController notificationsController = 
                        (CellularSecurityNotificationsPreferenceController) controller;
                    notificationsController.init(mSubId);
                    Log.d(TAG, "Initialized CellularSecurityNotificationsPreferenceController with subId: " + mSubId);
                }
            }
        }
    }
}
