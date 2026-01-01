/*
 * Copyright (C) 2025 BashaMobile
 *
 * Permission helper for BShare
 */

package com.android.settings.bshare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

/**
 * Helper for checking BShare permissions
 */
public class BSharePermissionHelper {
    
    public static boolean arePermissionsGranted(Context context) {
        // WiFi permissions (for network access)
        boolean network = ContextCompat.checkSelfPermission(context, 
            Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(context, 
            Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
            
        // Storage permissions (for file access)
        boolean storage = true; // Android 10+ uses scoped storage
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ may need additional permissions
            return network && storage;
        }
        return network && storage;
    }
    
    public static String[] getRequiredPermissions() {
        return new String[] {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        };
    }
}

