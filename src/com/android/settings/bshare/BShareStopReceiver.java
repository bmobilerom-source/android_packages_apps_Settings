/*
 * Copyright (C) 2025 BashaMobile
 *
 * Broadcast receiver to stop BShare server from notification
 * Inspired by prim-ftpd: https://github.com/wolpi/prim-ftpd
 */

package com.android.settings.bshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver to handle stop action from notification
 */
public class BShareStopReceiver extends BroadcastReceiver {
    private static final String TAG = "BShareStopReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.android.settings.bshare.STOP_SERVER".equals(intent.getAction())) {
            Log.d(TAG, "Stop server requested from notification");
            // Use a static reference or SharedPreferences to stop the server
            // For now, we'll use a static method in BShareManager
            BShareManager.stopServerFromNotification(context);
        }
    }
}
