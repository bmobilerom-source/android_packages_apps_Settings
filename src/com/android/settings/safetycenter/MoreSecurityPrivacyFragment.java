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

package com.android.settings.safetycenter;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.security.LockUnificationPreferenceController;
import com.android.settings.security.trustagent.TrustAgentListPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.drawer.CategoryKey;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

/**
 * An overflow menu for {@code SecuritySettings} containing advanced security and privacy settings.
 *
 * <p>This also includes all work-profile related settings.
 */
@SearchIndexable
public class MoreSecurityPrivacyFragment extends DashboardFragment {
    private static final String TAG = "MoreSecurityPrivacyFragment";
    private static final String KEY_NOTIFICATION_WORK_PROFILE_NOTIFICATIONS =
            "privacy_lock_screen_work_profile_notifications";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.MORE_SECURITY_PRIVACY_SETTINGS;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.more_security_privacy_settings;
    }

    @Override
    public String getCategoryKey() {
        return CategoryKey.CATEGORY_MORE_SECURITY_PRIVACY_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        SafetyCenterUtils.replaceEnterpriseStringsForPrivacyEntries(this);
        SafetyCenterUtils.replaceEnterpriseStringsForSecurityEntries(this);
    }

    /** see confirmPatternThenDisableAndClear */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (use(TrustAgentListPreferenceController.class)
                .handleActivityResult(requestCode, resultCode)) {
            return;
        }
        if (use(LockUnificationPreferenceController.class)
                .handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this /* host*/);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, DashboardFragment host) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.addAll(SafetyCenterUtils.getControllersForAdvancedPrivacy(context, lifecycle));
        controllers.addAll(
                SafetyCenterUtils.getControllersForAdvancedSecurity(context, lifecycle, host));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.more_security_privacy_settings) {
                /**
                 * Completely block More Security & Privacy from search
                 */
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    // Completely block from search
                    return null;
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null, null);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    // Hide all preferences from search
                    final List<String> keys = super.getNonIndexableKeys(context);
                    if (keys == null) {
                        return new java.util.ArrayList<>();
                    }
                    // Block all more security & privacy keys
                    keys.add(KEY_NOTIFICATION_WORK_PROFILE_NOTIFICATIONS);
                    keys.add("more_security_privacy");
                    keys.add("more_security");
                    keys.add("more_privacy");
                    return keys;
                }

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    // Always disable More Security & Privacy from search
                    return false;
                }
            };
}
