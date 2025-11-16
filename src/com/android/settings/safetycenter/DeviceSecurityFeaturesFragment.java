/*
 * Copyright (C) 2025 LineageOS
 * Based on MoreSecurityPrivacyFragment.java
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

package com.android.settings.safetycenter;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.privacy.DeviceSecurityFeaturesPrivacyHubPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

/**
 * Device Security Features Settings Page
 * Independent copy of More Security & Privacy settings with hidden preferences
 */
@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class DeviceSecurityFeaturesFragment extends DashboardFragment {

    private static final String TAG = "DeviceSecurityFeaturesFragment";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.MORE_SECURITY_PRIVACY_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.device_security_features_settings;
    }

    private static final String KEY_DEVICE_SECURITY = "device_security_preference";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        SafetyCenterUtils.replaceEnterpriseStringsForPrivacyEntries(this);
        SafetyCenterUtils.replaceEnterpriseStringsForSecurityEntries(this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Setup device security bottom sheet trigger after preferences are loaded
        // Wrapped in try-catch to prevent crashes during Settings startup
        try {
            androidx.preference.PreferenceScreen screen = getPreferenceScreen();
            if (screen != null && isAdded() && getActivity() != null) {
                androidx.preference.Preference deviceSecurityPref = screen.findPreference(KEY_DEVICE_SECURITY);
                if (deviceSecurityPref != null) {
                    deviceSecurityPref.setOnPreferenceClickListener(preference -> {
                        try {
                            if (getActivity() != null && getParentFragmentManager() != null) {
                                com.epic.fragments.DeviceSecurityBottomSheet bottomSheet = 
                                        com.epic.fragments.DeviceSecurityBottomSheet.newInstance();
                                bottomSheet.show(getParentFragmentManager(), "DeviceSecurityBottomSheet");
                            }
                        } catch (Exception e) {
                            android.util.Log.e(TAG, "Error showing device security bottom sheet", e);
                        }
                        return true;
                    });
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error setting up device security preference", e);
            // Don't crash - just log the error
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        if (use(com.android.settings.security.trustagent.TrustAgentListPreferenceController.class)
                .handleActivityResult(requestCode, resultCode)) {
            return;
        }
        if (use(com.android.settings.security.LockUnificationPreferenceController.class)
                .handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, DeviceSecurityFeaturesFragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        // Add Privacy Hub controller (copied from PrivacyHubPreferenceController logic)
        controllers.add(new DeviceSecurityFeaturesPrivacyHubPreferenceController(context));

        // Add all controllers from MoreSecurityPrivacyFragment
        controllers.addAll(SafetyCenterUtils.getControllersForAdvancedPrivacy(context, lifecycle));
        controllers.addAll(SafetyCenterUtils.getControllersForAdvancedSecurity(context, lifecycle, fragment));

        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.device_security_features_settings) {
                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
                    return buildPreferenceControllers(context, null, null);
                }
                
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    // Completely block Device Security Features from search
                    return null;
                }
                
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    // Always disable Device Security Features from search
                    return false;
                }
                
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    // Hide all preferences from search
                    List<String> keys = super.getNonIndexableKeys(context);
                    if (keys == null) {
                        keys = new java.util.ArrayList<>();
                    }
                    // Block all device security features keys
                    keys.add("device_security_features_settings");
                    keys.add("device_security_features");
                    return keys;
                }
            };
}

