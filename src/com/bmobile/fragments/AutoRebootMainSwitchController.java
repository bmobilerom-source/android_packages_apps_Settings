/*
 * Copyright (C) 2025 BashaMobile
 *
 * Main switch for Auto Reboot — katheleya-quick TogglePreferenceController pattern.
 */

package com.bmobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.TogglePreferenceController;

public class AutoRebootMainSwitchController extends TogglePreferenceController {

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
        mPendingCheckedState = isChecked;

        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            android.app.KeyguardManager km =
                    activity.getSystemService(android.app.KeyguardManager.class);
            if (km != null && !km.isKeyguardSecure()) {
                return applyPendingChange();
            }
            AutoRebootPinHelper.launchCredentialConfirmation(activity,
                    REQUEST_CODE_CONFIRM_CREDENTIAL);
            return false;
        }

        return applyPendingChange();
    }

    public boolean applyPendingChange() {
        boolean success = PrivacySecurityHelper.setAutoRebootEnabled(mContext, mPendingCheckedState);
        if (success) {
            mContext.getContentResolver().notifyChange(
                    android.provider.Settings.Secure.getUriFor(
                            android.provider.Settings.Secure.AUTO_REBOOT_ENABLED),
                    null, false);

            Intent configIntent = new Intent("com.bmobile.action.AUTO_REBOOT_CONFIG_CHANGED");
            configIntent.setPackage(mContext.getPackageName());
            mContext.sendBroadcast(configIntent);

            refreshUi();
        }
        return success;
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_CREDENTIAL) {
            if (AutoRebootPinHelper.isCredentialConfirmed(resultCode)) {
                return applyPendingChange();
            }
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
        if (preference == null) {
            refreshUi();
            return;
        }
        super.updateState(preference);
        updateIntervalEnabledState();
    }

    private void refreshUi() {
        if (mScreen == null) {
            return;
        }
        Preference switchPref = mScreen.findPreference(getPreferenceKey());
        if (switchPref != null) {
            super.updateState(switchPref);
        }
        updateIntervalEnabledState();
    }


    private void updateIntervalEnabledState() {
        if (mScreen == null) {
            return;
        }
        Preference intervalPref = mScreen.findPreference("auto_reboot_interval");
        if (intervalPref != null) {
            intervalPref.setEnabled(isChecked());
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
