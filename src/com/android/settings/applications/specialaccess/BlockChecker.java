/*
 * Copyright (C) 2025 bmobile
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

package com.android.settings.applications.specialaccess;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;

/**
 * Central utility to check if a fragment/page is blocked.
 * Provides a single point of truth for all block checks.
 */
public class BlockChecker {
    private static final String TAG = "BlockChecker";
    
    // Fragment class names that can be blocked
    private static final String FRAGMENT_APP_DASHBOARD = 
        "com.android.settings.applications.AppDashboardFragment";
    private static final String FRAGMENT_LOCATION_SETTINGS = 
        "com.android.settings.location.LocationSettings";
    private static final String FRAGMENT_ACCOUNT_DASHBOARD = 
        "com.android.settings.accounts.AccountDashboardFragment";
    private static final String FRAGMENT_SAFETY_CENTER = 
        "com.android.settings.safetycenter.MoreSecurityPrivacyFragment";
    private static final String FRAGMENT_PRIVACY_DASHBOARD = 
        "com.android.settings.privacy.PrivacyDashboardFragment";
    private static final String FRAGMENT_SECURITY_SETTINGS = 
        "com.android.settings.security.SecuritySettings";
    private static final String FRAGMENT_DEVICE_TWEAKS = 
        "com.epic.fragments.DeviceTweaksSettings";
    private static final String FRAGMENT_SYSTEM_OPTIMIZATION = 
        "com.epic.fragments.SystemOptimizationSettings";
    private static final String FRAGMENT_APPSEC = 
        "com.android.settings.applications.specialaccess.AppSecSettings";
    private static final String FRAGMENT_BLOCATION = 
        "com.android.settings.location.BLocationSettings";
    private static final String FRAGMENT_USB_DETAILS = 
        "com.android.settings.connecteddevice.usb.UsbDetailsFragment";
    private static final String FRAGMENT_MANAGE_APPLICATIONS = 
        "com.android.settings.applications.manageapplications.ManageApplications";
    
    /**
     * Check if a fragment is blocked.
     * @param context Context
     * @param fragmentName Full class name of fragment
     * @return true if blocked, false if allowed
     */
    public static boolean isBlocked(Context context, String fragmentName) {
        if (context == null || fragmentName == null) {
            return false;
        }
        
        try {
            // Check each block type
            if (FRAGMENT_APP_DASHBOARD.equals(fragmentName)) {
                return BlockAppDashboardController.isBlocked(context);
            }
            
            if (FRAGMENT_LOCATION_SETTINGS.equals(fragmentName)) {
                return BlockLocationSettingsController.isBlocked(context);
            }
            
            if (FRAGMENT_ACCOUNT_DASHBOARD.equals(fragmentName)) {
                return BlockAccountDashboardController.isBlocked(context);
            }
            
            // Block USB popup page
            if (FRAGMENT_USB_DETAILS.equals(fragmentName)) {
                return BlockUsbPopupController.isBlocked(context);
            }
            
            // Block ManageApplications when install whitelist is enabled
            if (FRAGMENT_MANAGE_APPLICATIONS.equals(fragmentName)) {
                return InstallAppWhitelistController.isWhitelistEnabled(context);
            }
            
            // Block Safety Center and all related fragments
            if (FRAGMENT_SAFETY_CENTER.equals(fragmentName) || 
                FRAGMENT_PRIVACY_DASHBOARD.equals(fragmentName) ||
                FRAGMENT_SECURITY_SETTINGS.equals(fragmentName) ||
                fragmentName.contains("SafetyCenter") ||
                fragmentName.contains("MoreSecurityPrivacy")) {
                return BlockSafetyCenterController.isBlocked(context);
            }
            
            // New pages - use controllers
            if (FRAGMENT_DEVICE_TWEAKS.equals(fragmentName)) {
                return BlockDeviceTweaksController.isBlocked(context);
            }
            
            if (FRAGMENT_SYSTEM_OPTIMIZATION.equals(fragmentName)) {
                return BlockSystemOptimizationController.isBlocked(context);
            }
            
            if (FRAGMENT_APPSEC.equals(fragmentName)) {
                return isBlockedBySetting(context, "block_appsec_enabled");
            }
            
            if (FRAGMENT_BLOCATION.equals(fragmentName)) {
                return BlockBLocationController.isBlocked(context);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking block status for: " + fragmentName, e);
            return false; // Fail open - allow access on error
        }
        
        return false;
    }
    
    /**
     * Helper to check block setting directly (for pages without controllers).
     */
    private static boolean isBlockedBySetting(Context context, String settingKey) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), settingKey, 0) != 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking setting: " + settingKey, e);
            return false;
        }
    }
    
    /**
     * Check if blocked and show toast message.
     * @param context Context
     * @param fragmentName Fragment class name
     * @return true if blocked, false if allowed
     */
    public static boolean checkAndShowMessage(Context context, String fragmentName) {
        if (isBlocked(context, fragmentName)) {
            Toast.makeText(context, 
                getBlockedMessage(context, fragmentName), 
                Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    
    /**
     * Get appropriate blocked message for fragment.
     */
    private static String getBlockedMessage(Context context, String fragmentName) {
        try {
            if (FRAGMENT_APP_DASHBOARD.equals(fragmentName)) {
                return context.getString(R.string.app_dashboard_blocked_message);
            }
            if (FRAGMENT_LOCATION_SETTINGS.equals(fragmentName)) {
                return context.getString(R.string.location_settings_blocked_message);
            }
            if (FRAGMENT_ACCOUNT_DASHBOARD.equals(fragmentName)) {
                return context.getString(R.string.account_dashboard_blocked_message);
            }
            if (FRAGMENT_SAFETY_CENTER.equals(fragmentName) ||
                FRAGMENT_PRIVACY_DASHBOARD.equals(fragmentName) ||
                FRAGMENT_SECURITY_SETTINGS.equals(fragmentName) ||
                fragmentName.contains("SafetyCenter") ||
                fragmentName.contains("MoreSecurityPrivacy")) {
                return context.getString(R.string.safety_center_blocked_message);
            }
            if (FRAGMENT_DEVICE_TWEAKS.equals(fragmentName)) {
                return context.getString(R.string.device_tweaks_blocked_message);
            }
            if (FRAGMENT_SYSTEM_OPTIMIZATION.equals(fragmentName)) {
                return context.getString(R.string.system_optimization_blocked_message);
            }
            if (FRAGMENT_APPSEC.equals(fragmentName)) {
                return context.getString(R.string.appsec_blocked_message);
            }
            if (FRAGMENT_BLOCATION.equals(fragmentName)) {
                return context.getString(R.string.blocation_blocked_message);
            }
            return context.getString(R.string.page_blocked_message);
        } catch (Exception e) {
            Log.e(TAG, "Error getting blocked message", e);
            return context.getString(R.string.page_blocked_message);
        }
    }
}

