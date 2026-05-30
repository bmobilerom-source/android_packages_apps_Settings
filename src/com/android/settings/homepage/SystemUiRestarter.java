/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.homepage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.settings.Utils;

/**
 * Restarts SystemUI via the platform {@code SysuiRestartReceiver} broadcast.
 */
public final class SystemUiRestarter {

    private static final String TAG = "SystemUiRestarter";
    private static final String ACTION_RESTART = "com.android.systemui.action.RESTART";

    private SystemUiRestarter() {}

    public static boolean restart(@NonNull Context context) {
        try {
            final Intent intent = new Intent(ACTION_RESTART);
            intent.setData(Uri.parse("package:" + Utils.SYSTEMUI_PACKAGE_NAME));
            context.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "SysuiRestartReceiver broadcast failed", e);
        }
        return false;
    }
}
