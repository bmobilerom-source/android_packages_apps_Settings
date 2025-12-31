/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.android.settings.display;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

/**
 * Controller for ColorBlendr-style color customization
 * Manages saturation, lightness, and pitch black settings
 */
public class ColorBlendrController {
    private static final String TAG = "ColorBlendrController";
    
    private static final int DEFAULT_SATURATION = 100; // 100% = no change
    private static final int DEFAULT_LIGHTNESS = 100;
    
    private Context mContext;
    private ColorBlendrHelper mHelper;
    
    public ColorBlendrController(Context context) {
        mContext = context;
        mHelper = new ColorBlendrHelper(context);
    }
    
    public int getAccentSaturation() {
        try {
            int value = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.MONET_ACCENT_SATURATION, DEFAULT_SATURATION, UserHandle.USER_CURRENT);
            // Clamp to valid range
            return Math.max(0, Math.min(200, value));
        } catch (Exception e) {
            Log.e(TAG, "Error getting accent saturation", e);
            return DEFAULT_SATURATION;
        }
    }
    
    public void setAccentSaturation(int saturation) {
        try {
            // Clamp to valid range
            saturation = Math.max(0, Math.min(200, saturation));
            Settings.Secure.putIntForUser(mContext.getContentResolver(),
                Settings.Secure.MONET_ACCENT_SATURATION, saturation, UserHandle.USER_CURRENT);
            notifyChange();
        } catch (Exception e) {
            Log.e(TAG, "Error setting accent saturation", e);
        }
    }
    
    public int getBackgroundSaturation() {
        try {
            int value = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.MONET_BACKGROUND_SATURATION, DEFAULT_SATURATION, UserHandle.USER_CURRENT);
            return Math.max(0, Math.min(200, value));
        } catch (Exception e) {
            Log.e(TAG, "Error getting background saturation", e);
            return DEFAULT_SATURATION;
        }
    }
    
    public void setBackgroundSaturation(int saturation) {
        try {
            saturation = Math.max(0, Math.min(200, saturation));
            Settings.Secure.putIntForUser(mContext.getContentResolver(),
                Settings.Secure.MONET_BACKGROUND_SATURATION, saturation, UserHandle.USER_CURRENT);
            notifyChange();
        } catch (Exception e) {
            Log.e(TAG, "Error setting background saturation", e);
        }
    }
    
    public int getBackgroundLightness() {
        try {
            int value = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.MONET_BACKGROUND_LIGHTNESS, DEFAULT_LIGHTNESS, UserHandle.USER_CURRENT);
            return Math.max(0, Math.min(200, value));
        } catch (Exception e) {
            Log.e(TAG, "Error getting background lightness", e);
            return DEFAULT_LIGHTNESS;
        }
    }
    
    public void setBackgroundLightness(int lightness) {
        try {
            lightness = Math.max(0, Math.min(200, lightness));
            Settings.Secure.putIntForUser(mContext.getContentResolver(),
                Settings.Secure.MONET_BACKGROUND_LIGHTNESS, lightness, UserHandle.USER_CURRENT);
            notifyChange();
        } catch (Exception e) {
            Log.e(TAG, "Error setting background lightness", e);
        }
    }
    
    public boolean isPitchBlackEnabled() {
        return Settings.Secure.getIntForUser(mContext.getContentResolver(),
            Settings.Secure.MONET_PITCH_BLACK, 0, UserHandle.USER_CURRENT) == 1;
    }
    
    public void setPitchBlackEnabled(boolean enabled) {
        Settings.Secure.putIntForUser(mContext.getContentResolver(),
            Settings.Secure.MONET_PITCH_BLACK, enabled ? 1 : 0, UserHandle.USER_CURRENT);
        notifyChange();
    }
    
    public int getManualColorOverride() {
        return Settings.Secure.getIntForUser(mContext.getContentResolver(),
            Settings.Secure.MONET_MANUAL_COLOR_OVERRIDE, 0, UserHandle.USER_CURRENT);
    }
    
    public void setManualColorOverride(int color) {
        Settings.Secure.putIntForUser(mContext.getContentResolver(),
            Settings.Secure.MONET_MANUAL_COLOR_OVERRIDE, color, UserHandle.USER_CURRENT);
        notifyChange();
    }
    
    public void applyColorModifications() {
        try {
            if (mHelper != null) {
                mHelper.applyModifications(
                    getAccentSaturation(),
                    getBackgroundSaturation(),
                    getBackgroundLightness(),
                    isPitchBlackEnabled(),
                    getManualColorOverride());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying color modifications", e);
        }
    }
    
    private void notifyChange() {
        try {
            if (mContext == null) {
                Log.w(TAG, "Context is null, cannot notify change");
                return;
            }
            
            // Notify SystemUI of color changes
            ContentResolver resolver = mContext.getContentResolver();
            if (resolver != null) {
                resolver.notifyChange(
                    Settings.Secure.getUriFor(Settings.Secure.MONET_COLOR_MODIFICATIONS_ENABLED),
                    null);
                
                // Mark modifications as enabled
                Settings.Secure.putIntForUser(resolver,
                    Settings.Secure.MONET_COLOR_MODIFICATIONS_ENABLED, 1, UserHandle.USER_CURRENT);
            }
            
            // Send broadcast to trigger SystemUI refresh
            Intent intent = new Intent("com.android.settings.MONET_COLORS_CHANGED");
            intent.setPackage("com.android.systemui");
            intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(intent);
            
            Log.d(TAG, "Color modifications applied and notified");
        } catch (Exception e) {
            Log.e(TAG, "Error notifying color change", e);
        }
    }
}

