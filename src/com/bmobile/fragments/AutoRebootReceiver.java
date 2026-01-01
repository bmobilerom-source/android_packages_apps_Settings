/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bmobile.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

/** Handles scheduled auto-reboot alarms from {@link AutoRebootSettings}. */
public class AutoRebootReceiver extends BroadcastReceiver {
    private static final String TAG = "AutoRebootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !"com.android.settings.AUTO_REBOOT".equals(intent.getAction())) {
            return;
        }
        if (!PrivacySecurityHelper.isAutoRebootEnabled(context)) {
            Log.d(TAG, "Auto reboot disabled; skipping");
            return;
        }
        try {
            PowerManager pm = context.getSystemService(PowerManager.class);
            if (pm != null) {
                pm.reboot("bmobile-auto-reboot");
            }
        } catch (Exception e) {
            Log.e(TAG, "Auto reboot failed", e);
        }
    }
}
