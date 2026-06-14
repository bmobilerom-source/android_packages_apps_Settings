/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bmobile.customization;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.service.controls.ControlsProviderService;

import com.android.settingslib.applications.ServiceListing;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Shared helpers for device controls settings. */
public final class DeviceControlsUtils {

    public static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    public static final ComponentName PROVIDER_SELECTOR =
            new ComponentName(SYSTEMUI_PACKAGE,
                    "com.android.systemui.controls.management.ControlsProviderSelectorActivity");
    public static final ComponentName CONTROLS_PANEL =
            new ComponentName(SYSTEMUI_PACKAGE,
                    "com.android.systemui.controls.ui.ControlsActivity");

    private DeviceControlsUtils() {}

    public static boolean hasControlsFeature(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CONTROLS);
    }

    public static boolean hasControlsProvider(Context context) {
        final AtomicBoolean hasProvider = new AtomicBoolean(false);
        ServiceListing listing = buildServiceListing(context);
        listing.addCallback(
                services -> hasProvider.set(services != null && !services.isEmpty()));
        listing.reload();
        return hasProvider.get();
    }

    public static ServiceListing buildServiceListing(Context context) {
        return new ServiceListing.Builder(context)
                .setIntentAction(ControlsProviderService.SERVICE_CONTROLS)
                .setPermission(Manifest.permission.BIND_CONTROLS)
                .setNoun("Controls Provider")
                .setSetting("controls_providers")
                .setTag("controls_providers")
                .build();
    }

    public static Intent newProviderSelectorIntent() {
        return new Intent(Intent.ACTION_MAIN)
                .setComponent(PROVIDER_SELECTOR)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static Intent newControlsPanelIntent() {
        return new Intent(Intent.ACTION_MAIN)
                .setComponent(CONTROLS_PANEL)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static boolean canLaunch(Context context, Intent intent) {
        return intent.resolveActivity(context.getPackageManager()) != null;
    }

    public static void notifyProviderAvailability(
            Context context, ServiceListing.Callback callback) {
        ServiceListing listing = buildServiceListing(context);
        listing.addCallback(callback);
        listing.setListening(true);
        listing.reload();
    }

    public static void stopListening(Context context, ServiceListing listing) {
        if (listing != null) {
            listing.setListening(false);
        }
    }

    public static boolean hasProviders(List<?> services) {
        return services != null && !services.isEmpty();
    }
}
