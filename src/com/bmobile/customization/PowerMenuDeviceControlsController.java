/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bmobile.customization;

import static org.lineageos.internal.util.PowerMenuConstants.GLOBAL_ACTION_KEY_DEVICECONTROLS;

import android.content.Context;
import android.content.pm.ServiceInfo;

import androidx.preference.Preference;

import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.applications.ServiceListing;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

import lineageos.app.LineageGlobalActions;

import java.util.List;

public class PowerMenuDeviceControlsController extends TogglePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    private final LineageGlobalActions mLineageGlobalActions;
    private ServiceListing mServiceListing;
    private Preference mPreference;

    public PowerMenuDeviceControlsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mLineageGlobalActions = LineageGlobalActions.getInstance(context);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!DeviceControlsUtils.hasControlsFeature(mContext)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return DeviceControlsUtils.hasControlsProvider(mContext) ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        return mLineageGlobalActions.userConfigContains(GLOBAL_ACTION_KEY_DEVICECONTROLS);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        mLineageGlobalActions.updateUserConfig(isChecked, GLOBAL_ACTION_KEY_DEVICECONTROLS);
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        mPreference = preference;
        super.updateState(preference);
        refreshSummary(preference);
    }

    @Override
    public void onStart() {
        mServiceListing = DeviceControlsUtils.buildServiceListing(mContext);
        mServiceListing.addCallback(this::onProvidersChanged);
        mServiceListing.setListening(true);
        mServiceListing.reload();
    }

    @Override
    public void onStop() {
        DeviceControlsUtils.stopListening(mContext, mServiceListing);
        mServiceListing = null;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }

    private void onProvidersChanged(List<ServiceInfo> services) {
        if (mPreference == null) {
            return;
        }
        final boolean hasProviders = DeviceControlsUtils.hasProviders(services);
        mPreference.setVisible(hasProviders);
        mPreference.setEnabled(hasProviders);
    }
}
