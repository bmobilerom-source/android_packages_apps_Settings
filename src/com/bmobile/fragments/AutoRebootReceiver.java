/*
 * Copyright (C) 2025 BashaMobile
 *
 * Reschedules auto-reboot when settings change (katheleya-quick pattern).
 * Actual reboot is performed by frameworks AutoRebootManager.
 */

package com.bmobile.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public class AutoRebootReceiver extends BroadcastReceiver {
    private static final String TAG = "AutoRebootReceiver";
    private static final String ACTION_TRIGGER = "com.bmobile.action.AUTO_REBOOT_TRIGGER";
    private static final String ACTION_CONFIG_CHANGED = "com.bmobile.action.AUTO_REBOOT_CONFIG_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (ACTION_TRIGGER.equals(action)) {
            performAutoReboot(context);
        } else if (ACTION_CONFIG_CHANGED.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_USER_PRESENT.equals(action)) {
            scheduleAutoReboot(context);
        }
    }

    private void performAutoReboot(Context context) {
        if (!PrivacySecurityHelper.isAutoRebootEnabled(context)) {
            Log.d(TAG, "Auto reboot alarm fired but feature is disabled");
            return;
        }
        Log.i(TAG, "Auto reboot alarm fired — rebooting device");
        PowerManager pm = context.getSystemService(PowerManager.class);
        if (pm != null) {
            pm.reboot("auto_reboot");
        }
    }

    private void scheduleAutoReboot(Context context) {
        if (!PrivacySecurityHelper.isAutoRebootEnabled(context)) {
            Log.d(TAG, "Auto reboot disabled; canceling alarm");
            AlarmManager am = context.getSystemService(AlarmManager.class);
            if (am != null) {
                PendingIntent pi = buildPendingIntent(context);
                am.cancel(pi);
            }
            return;
        }

        long delayMs = PrivacySecurityHelper.getAutoRebootInterval(context);
        long triggerAt = SystemClock.elapsedRealtime() + delayMs;

        AlarmManager am = context.getSystemService(AlarmManager.class);
        if (am == null) return;

        PendingIntent pi = buildPendingIntent(context);
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAt, pi);
            Log.d(TAG, "Scheduled auto reboot in " + (delayMs / 1000 / 60) + " minutes");
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to schedule auto reboot alarm", e);
        }
    }

    private PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(ACTION_TRIGGER);
        intent.setPackage(context.getPackageName());
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
