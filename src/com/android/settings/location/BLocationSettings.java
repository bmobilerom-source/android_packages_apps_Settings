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

package com.android.settings.location;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.password.ConfirmDeviceCredentialActivity;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

/**
 * Fragment for enhanced location privacy and status settings.
 * Provides advanced location controls beyond basic location toggle.
 */
@SearchIndexable
public class BLocationSettings extends DashboardFragment {
    private static final String TAG = "BLocationSettings";
    private static final int REQUEST_CODE_CONFIRM_CREDENTIAL = 1002;
    private boolean mIsAuthenticated = false;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.LOCATION;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.blocation;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Initialize controllers (fragments handle their own controllers)
        use(LocationPrivacyIndicatorsController.class);
        LocationPrecisionController precisionController = use(LocationPrecisionController.class);
        LocationServiceStatusController statusController = use(LocationServiceStatusController.class);
        use(LocationPrivacyFooterController.class);
        
        // Register lifecycle observers for controllers that need them
        getSettingsLifecycle().addObserver(statusController);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Restore authentication state
        if (savedInstanceState != null) {
            mIsAuthenticated = savedInstanceState.getBoolean("is_authenticated", false);
        }
        
        getActivity().setTitle(R.string.location_privacy_dashboard_title);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_authenticated", mIsAuthenticated);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Check if authentication is required
        if (!mIsAuthenticated) {
            checkAndRequestAuthentication();
            return;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CONFIRM_CREDENTIAL) {
            if (resultCode == Activity.RESULT_OK) {
                mIsAuthenticated = true;
            } else {
                // Authentication failed or cancelled - finish the activity
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        }
    }

    private void checkAndRequestAuthentication() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        KeyguardManager km = context.getSystemService(KeyguardManager.class);
        if (km == null || !km.isKeyguardSecure()) {
            // No lock screen set up - allow access without authentication
            mIsAuthenticated = true;
            return;
        }

        // Request device credential confirmation
        Intent intent = new Intent();
        intent.setClassName("com.android.settings",
                ConfirmDeviceCredentialActivity.class.getName());
        intent.putExtra(KeyguardManager.EXTRA_TITLE,
                context.getString(R.string.blocation_confirm_credential_title));
        intent.putExtra(KeyguardManager.EXTRA_DESCRIPTION,
                context.getString(R.string.blocation_confirm_credential_description));
        intent.putExtra(KeyguardManager.EXTRA_DISALLOW_BIOMETRICS_IF_POLICY_EXISTS, false);

        try {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_CREDENTIAL);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch credential confirmation", e);
            // If we can't launch auth, allow access (fallback)
            mIsAuthenticated = true;
        }
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.blocation);
}

