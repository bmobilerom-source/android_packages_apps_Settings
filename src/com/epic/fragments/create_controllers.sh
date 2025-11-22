#!/bin/bash
# Create all 16 controller classes

controllers=(
"AutoReboot:isAutoRebootEnabled:setAutoRebootEnabled"
"UsbAccessories:isUsbAccessoriesEnabled:setUsbAccessoriesEnabled"
"MacRandomization:isMacRandomizationEnabled:setMacRandomizationEnabled"
"LockdownMode:isLockdownModeEnabled:setLockdownModeEnabled"
"AutoDisableWifi:isAutoDisableWifiEnabled:setAutoDisableWifiEnabled"
"AutoDisableBluetooth:isAutoDisableBluetoothEnabled:setAutoDisableBluetoothEnabled"
"ClipboardNotifications:isClipboardNotificationsEnabled:setClipboardNotificationsEnabled"
"ShowPasswords:isShowPasswordsEnabled:setShowPasswordsEnabled"
"PrivacyIndicators:isPrivacyIndicatorsEnabled:setPrivacyIndicatorsEnabled"
"NetworkTraffic:isNetworkTrafficEnabled:setNetworkTrafficEnabled"
"AlwaysOnVpn:isAlwaysOnVpnEnabled:setAlwaysOnVpnEnabled"
"WebViewJit:isWebViewJitEnabled:setWebViewJitEnabled"
"NativeDebugging:isNativeDebuggingRestricted:setNativeDebuggingRestricted"
"StorageScopes:isStorageScopesEnabled:setStorageScopesEnabled"
"ContactScopes:isContactScopesEnabled:setContactScopesEnabled"
)

for entry in "${controllers[@]}"; do
    IFS=':' read -r name getter setter <<< "$entry"
    cat > "${name}Controller.java" << EOF
/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.epic.fragments;
import android.content.Context;
import com.android.settings.core.TogglePreferenceController;
public class ${name}Controller extends TogglePreferenceController {
    public ${name}Controller(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }
    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
    @Override
    public boolean isChecked() {
        return PrivacySecurityHelper.${getter}(mContext);
    }
    @Override
    public boolean setChecked(boolean isChecked) {
        return PrivacySecurityHelper.${setter}(mContext, isChecked);
    }
    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
EOF
done

# Special case for PrivateDnsController (needs different handling)
cat > PrivateDnsController.java << 'EOF'
/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.epic.fragments;
import android.content.Context;
import com.android.settings.core.BasePreferenceController;
public class PrivateDnsController extends BasePreferenceController {
    public PrivateDnsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }
    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
    @Override
    public CharSequence getSummary() {
        String mode = PrivacySecurityHelper.getPrivateDnsMode(mContext);
        String specifier = PrivacySecurityHelper.getPrivateDnsSpecifier(mContext);
        if (mode == null || mode.equals("off")) {
            return mContext.getString(R.string.private_dns_off);
        } else if (mode.equals("opportunistic")) {
            return mContext.getString(R.string.private_dns_opportunistic);
        } else if (mode.equals("hostname") && specifier != null) {
            return specifier;
        }
        return mContext.getString(R.string.private_dns_off);
    }
}
EOF
