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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.widget.SwitchWidgetController;
import com.android.settings.password.ConfirmDeviceCredentialActivity;

public class AutoRebootMainSwitchController extends TogglePreferenceController
        implements SwitchWidgetController.OnSwitchChangeListener {

    private static final int REQUEST_CODE_CONFIRM_CREDENTIAL = 1001;
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
        // Store the desired state
        mPendingCheckedState = isChecked;

        // Get the fragment to launch credential confirmation
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            // Check if device has secure lock screen
            android.app.KeyguardManager km = activity.getSystemService(android.app.KeyguardManager.class);
            if (km != null && !km.isKeyguardSecure()) {
                // No lock screen, allow directly without PIN verification
                android.util.Log.d("AutoRebootMainSwitchController",
                        "No secure lock screen, enabling auto reboot directly");
                return applyPendingChange();
            }
            // Launch credential confirmation
            AutoRebootPinHelper.launchCredentialConfirmation(activity, REQUEST_CODE_CONFIRM_CREDENTIAL);
            // Return false for now - will be set after PIN verification
            return false;
        } else {
            // If not an Activity context, try to find the fragment
            // For now, require PIN verification
            android.util.Log.w("AutoRebootMainSwitchController",
                    "Cannot verify PIN - context is not an Activity");
            return false;
        }
    }

    /**
     * Called after PIN verification succeeds or when no lock screen is present.
     */
    public boolean applyPendingChange() {
        boolean success = PrivacySecurityHelper.setAutoRebootEnabled(mContext, mPendingCheckedState);
        if (success) {
            android.util.Log.d("AutoRebootMainSwitchController", 
                    "Auto reboot " + (mPendingCheckedState ? "enabled" : "disabled"));
            // Notify that the setting changed
            android.content.ContentResolver resolver = mContext.getContentResolver();
            resolver.notifyChange(
                    android.provider.Settings.Secure.getUriFor(
                            "auto_reboot_enabled"),
                    null, false);
            
            // Send broadcast to AutoRebootReceiver to trigger scheduling
            android.content.Intent intent = new android.content.Intent("com.epic.action.AUTO_REBOOT_CONFIG_CHANGED");
            intent.setPackage(mContext.getPackageName());
            mContext.sendBroadcast(intent);
            
            // Update UI
            updateState(null);
        } else {
            android.util.Log.e("AutoRebootMainSwitchController", 
                    "Failed to set auto reboot enabled state");
        }
        return success;
    }

    /**
     * Handle activity result from credential confirmation.
     */
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_CREDENTIAL) {
            if (AutoRebootPinHelper.isCredentialConfirmed(resultCode)) {
                return applyPendingChange();
            } else {
                android.util.Log.w("AutoRebootMainSwitchController", 
                        "Credential verification failed or cancelled");
                // Reset UI to current state
                updateState(null);
            }
        }
        return false;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mScreen = screen;
        // Update interval preference state when switch changes
        Preference intervalPref = screen.findPreference("auto_reboot_interval");
        if (intervalPref != null) {
            updateState(intervalPref);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        // Update interval preference enabled state and refresh its state
        if (preference != null && preference.getPreferenceManager() != null) {
            PreferenceScreen screen = preference.getPreferenceManager().getPreferenceScreen();
            if (screen != null) {
                Preference intervalPref = screen.findPreference("auto_reboot_interval");
                if (intervalPref != null) {
                    boolean enabled = isChecked();
                    intervalPref.setEnabled(enabled);
                    // Refresh the interval preference state
                    if (intervalPref.getContext() != null) {
                        AutoRebootIntervalController intervalController = 
                                new AutoRebootIntervalController(intervalPref.getContext(), "auto_reboot_interval");
                        intervalController.updateState(intervalPref);
                    }
                }
            }
        }
    }

    @Override
    public boolean onSwitchToggled(boolean isChecked) {
        boolean result = setChecked(isChecked);
        if (result) {
            // Update interval preference state
            updateState(null);
        }
        return result;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}

