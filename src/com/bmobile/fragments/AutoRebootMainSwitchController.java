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
package com.bmobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.widget.SettingsMainSwitchPreferenceController;
import com.android.settingslib.widget.MainSwitchPreference;

public class AutoRebootMainSwitchController extends SettingsMainSwitchPreferenceController {

    private static final String TAG = "AutoRebootMainSwitch";
    private static final int REQUEST_CODE_CONFIRM_CREDENTIAL = 1001;
    private static final String KEY_INTERVAL = "auto_reboot_interval";

    private boolean mPendingCheckedState = false;
    private PreferenceScreen mScreen;

    public AutoRebootMainSwitchController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return PrivacySecurityHelper.isAutoRebootEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        mPendingCheckedState = isChecked;

        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            android.app.KeyguardManager km =
                    activity.getSystemService(android.app.KeyguardManager.class);
            if (km != null && !km.isKeyguardSecure()) {
                Log.d(TAG, "No secure lock screen, applying auto reboot directly");
                return applyPendingChange();
            }
            AutoRebootPinHelper.launchCredentialConfirmation(activity,
                    REQUEST_CODE_CONFIRM_CREDENTIAL);
            return false;
        }

        Log.w(TAG, "Context is not an Activity, applying without credential UI");
        return applyPendingChange();
    }

    public boolean applyPendingChange() {
        boolean success = PrivacySecurityHelper.setAutoRebootEnabled(mContext, mPendingCheckedState);
        if (success) {
            Log.d(TAG, "Auto reboot " + (mPendingCheckedState ? "enabled" : "disabled"));
            mContext.getContentResolver().notifyChange(
                    android.provider.Settings.Secure.getUriFor(
                            android.provider.Settings.Secure.AUTO_REBOOT_ENABLED),
                    null, false);
            refreshUi();
        } else {
            Log.e(TAG, "Failed to set auto reboot enabled state");
        }
        return success;
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_CREDENTIAL) {
            if (AutoRebootPinHelper.isCredentialConfirmed(resultCode)) {
                return applyPendingChange();
            }
            Log.w(TAG, "Credential verification failed or cancelled");
            refreshUi();
        }
        return false;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mScreen = screen;
        refreshUi();
    }

    @Override
    public void updateState(Preference preference) {
        if (preference == null
                || preference instanceof MainSwitchPreference
                || getPreferenceKey().equals(preference.getKey())) {
            if (mSwitchPreference != null) {
                super.updateState(mSwitchPreference);
            }
        } else {
            super.updateState(preference);
        }
        updateIntervalEnabledState();
    }

    @Override
    public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
        if (!setChecked(isChecked)) {
            final boolean persisted = isChecked();
            buttonView.setChecked(persisted);
            if (mSwitchPreference != null) {
                mSwitchPreference.setChecked(persisted);
            }
        }
    }

    private void refreshUi() {
        if (mSwitchPreference != null) {
            super.updateState(mSwitchPreference);
        }
        updateIntervalEnabledState();
    }

    private void updateIntervalEnabledState() {
        if (mScreen == null) {
            return;
        }
        Preference intervalPref = mScreen.findPreference(KEY_INTERVAL);
        if (intervalPref != null) {
            intervalPref.setEnabled(isChecked());
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
