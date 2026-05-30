/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.homepage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.settings.Utils;

/**
 * Restarts SystemUI via {@code SysuiRestartReceiver}, with force-stop fallback.
 */
public final class SystemUiRestarter {

    private static final String TAG = "SystemUiRestarter";
    private static final String ACTION_RESTART = "com.android.systemui.action.RESTART";

    private SystemUiRestarter() {}

    public static boolean restart(@NonNull Context context) {
        if (sendSysuiRestartBroadcast(context)) {
            return true;
        }
        return forceStopSystemUi(context);
    }

    /**
     * Same broadcast SysuiRestartReceiver expects: action RESTART, package-scoped intent,
     * and {@code package:com.android.systemui} data (receiver strips the {@code package:} prefix).
     */
    private static boolean sendSysuiRestartBroadcast(@NonNull Context context) {
        try {
            final Intent intent = new Intent(ACTION_RESTART);
            intent.setPackage(Utils.SYSTEMUI_PACKAGE_NAME);
            intent.setData(Uri.fromParts("package", Utils.SYSTEMUI_PACKAGE_NAME, null));
            context.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
            Log.d(TAG, "Sent SysuiRestartReceiver broadcast");
            return true;
        } catch (Exception e) {
            Log.w(TAG, "SysuiRestartReceiver broadcast failed", e);
        }
        return false;
    }

    /** Fallback when broadcast is blocked; persistent SystemUI is restarted by the system. */
    private static boolean forceStopSystemUi(@NonNull Context context) {
        try {
            final ActivityManager am = context.getSystemService(ActivityManager.class);
            if (am != null) {
                am.forceStopPackageAsUser(Utils.SYSTEMUI_PACKAGE_NAME, UserHandle.USER_SYSTEM);
                Log.d(TAG, "forceStopPackage SystemUI");
                return true;
            }
        } catch (Exception e) {
            Log.w(TAG, "forceStopPackage SystemUI failed", e);
        }
        return false;
    }
}
