/*
 * Copyright (C) 2025 LineageOS
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
 *
 * TRANSFER TO OTHER ROMS:
 * =======================
 * This helper class makes the custom theme feature independent and transferable.
 * To transfer to other ROMs:
 * 1. Copy this file
 * 2. Ensure Settings.Secure.SYSTEM_CUSTOM_THEME is defined
 * 3. Update the setting key if needed (currently "system_custom_theme")
 * 4. Copy CustomThemeSettings.java and related XML files
 */

package com.android.settings.theme;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.UserHandle;
import android.provider.Settings;

/**
 * Helper class for Custom Theme feature
 * Provides utility methods for checking and managing custom theme settings
 * Makes the feature independent and transferable to other ROMs
 * 
 * Themes are applied directly in Settings using:
 * - Wallpaper backgrounds (for Black theme)
 * - Blur effects (for Vivid theme)
 * - Color tints (for accent colors)
 * - Transparency (for Vivid theme)
 */
public class CustomThemeHelper {
    private static final String TAG = "CustomThemeHelper";
    
    /**
     * Setting key for custom theme
     * Update this if your ROM uses a different key
     */
    public static final String SETTING_KEY = Settings.Secure.SYSTEM_CUSTOM_THEME;
    
    // Theme values
    public static final int THEME_DEFAULT = 0;
    public static final int THEME_BLACK = 1;
    public static final int THEME_TRANSPARENT = 2; // Changed from VIVID
    public static final int THEME_SNOWPAINT = 3;
    public static final int THEME_ESPRESSO = 4;
    public static final int THEME_CUSTOM_BLUE = 5;
    public static final int THEME_ANIMATED = 6;
    public static final int THEME_EXPRESSIVE = 7; // New expressive mode with dynamic colors
    public static final int THEME_ANIMATED_WALLPAPER_BLUR = 8; // Animated wallpaper with moving blur
    public static final int THEME_CUSTOM_PICTURE = 9; // Custom picture from files with blur
    
    /**
     * Check if a specific theme is enabled
     * @param context The context
     * @param themeValue Theme value to check
     * @return true if the theme is active
     */
    public static boolean isThemeActive(Context context, int themeValue) {
        if (context == null) {
            return false;
        }
        int currentTheme = getCurrentTheme(context);
        return currentTheme == themeValue;
    }
    
    /**
     * Get the current theme value from Settings
     * @param context The context
     * @return Current theme value (0-4)
     */
    public static int getCurrentTheme(Context context) {
        if (context == null) {
            return THEME_DEFAULT;
        }
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return THEME_DEFAULT;
            }
            return Settings.Secure.getIntForUser(resolver,
                    SETTING_KEY,
                    THEME_DEFAULT,
                    UserHandle.USER_CURRENT);
        } catch (Exception e) {
            return THEME_DEFAULT;
        }
    }
    
    /**
     * Set the theme value in Settings
     * @param context The context
     * @param themeValue Theme value to set (0-4)
     * @return true if successful
     */
    public static boolean setTheme(Context context, int themeValue) {
        if (context == null) {
            return false;
        }
        if (themeValue < 0 || themeValue > 8) {
            return false;
        }
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return false;
            }
            Settings.Secure.putIntForUser(resolver,
                    SETTING_KEY,
                    themeValue,
                    UserHandle.USER_CURRENT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if device is in dark mode
     * @param context The context
     * @return true if dark mode is active
     */
    public static boolean isDarkMode(Context context) {
        if (context == null) {
            return false;
        }
        int nightMode = context.getResources().getConfiguration().uiMode 
            & Configuration.UI_MODE_NIGHT_MASK;
        return (nightMode == Configuration.UI_MODE_NIGHT_YES);
    }
    
    /**
     * Check if a theme should use wallpaper background
     * Black theme uses a black depth wallpaper
     * Only works in dark mode
     * @param context The context
     * @return true if wallpaper background should be used
     */
    public static boolean shouldUseWallpaperBackground(Context context) {
        if (!isDarkMode(context)) {
            return false; // Only work in dark mode
        }
        int theme = getCurrentTheme(context);
        return theme == THEME_BLACK;
    }
    
    /**
     * Check if a theme should use blur effect
     * Transparent theme uses 70% blur with transparency
     * Only works in dark mode
     * @param context The context
     * @return true if blur should be used
     */
    public static boolean shouldUseBlur(Context context) {
        if (!isDarkMode(context)) {
            return false; // Only work in dark mode
        }
        int theme = getCurrentTheme(context);
        return theme == THEME_TRANSPARENT;
    }
    
    /**
     * Check if a theme should use gradient background
     * Custom Blue theme uses blue gradient
     * Only works in dark mode
     * @param context The context
     * @return true if gradient should be used
     */
    public static boolean shouldUseGradient(Context context) {
        if (!isDarkMode(context)) {
            return false; // Only work in dark mode
        }
        int theme = getCurrentTheme(context);
        return theme == THEME_CUSTOM_BLUE;
    }
    
    /**
     * Check if a theme should use animated background
     * Animated theme uses Lottie animation
     * Only works in dark mode
     * @param context The context
     * @return true if animated background should be used
     */
    public static boolean shouldUseAnimatedBackground(Context context) {
        if (!isDarkMode(context)) {
            return false; // Only work in dark mode
        }
        int theme = getCurrentTheme(context);
        return theme == THEME_ANIMATED;
    }
    
    /**
     * Get blur radius for current theme
     * Transparent theme uses 70% blur (56dp = 80 * 0.7)
     * @param context The context
     * @return Blur radius in dp, or 0 if blur not needed
     */
    public static float getBlurRadius(Context context) {
        if (shouldUseBlur(context)) {
            return 56.0f; // 70% of 80dp
        }
        return 0f;
    }
    
    /**
     * Get transparency alpha for current theme
     * Transparent theme uses transparency
     * @param context The context
     * @return Alpha value (0.0-1.0), 1.0 means fully opaque
     */
    public static float getTransparencyAlpha(Context context) {
        if (shouldUseBlur(context)) {
            return 0.3f; // 70% transparent (30% opacity)
        }
        return 1.0f; // Fully opaque
    }
    
    /**
     * Check if a theme should use color accent tint
     * Black theme uses color accent tint on preferences
     * Only works in dark mode
     * @param context The context
     * @return true if color accent tint should be used
     */
    public static boolean shouldUseAccentTint(Context context) {
        if (!isDarkMode(context)) {
            return false; // Only work in dark mode
        }
        int theme = getCurrentTheme(context);
        return theme == THEME_BLACK;
    }
    
    /**
     * Get theme name string
     * @param context The context
     * @param themeValue Theme value (0-4)
     * @return Theme name
     */
    public static String getThemeName(Context context, int themeValue) {
        if (context == null) {
            return "Unknown";
        }
        try {
            String[] entries = context.getResources().getStringArray(
                    com.android.settings.R.array.custom_theme_entries);
            if (entries != null && themeValue >= 0 && themeValue < entries.length) {
                return entries[themeValue].toString();
            }
        } catch (Exception e) {
            // Fall through to hardcoded names
        }
        
        // Fallback to hardcoded names
        switch (themeValue) {
            case THEME_DEFAULT:
                return "Default";
            case THEME_BLACK:
                return "Black";
            case THEME_TRANSPARENT:
                return "Transparent";
            case THEME_SNOWPAINT:
                return "Snowpaint";
            case THEME_ESPRESSO:
                return "Espresso";
            case THEME_CUSTOM_BLUE:
                return "Custom Blue";
            case THEME_ANIMATED:
                return "Animated";
            case THEME_EXPRESSIVE:
                return "Expressive Mode";
            case THEME_ANIMATED_WALLPAPER_BLUR:
                return "Animated Wallpaper Blur";
            case THEME_CUSTOM_PICTURE:
                return "Custom Picture";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Check if a theme should use expressive mode (dynamic colors per page)
     * Expressive mode uses 5 different pastel colors that change per page
     * Only works in dark mode
     * @param context The context
     * @return true if expressive mode should be used
     */
    public static boolean shouldUseExpressiveMode(Context context) {
        if (!isDarkMode(context)) {
            return false; // Only work in dark mode
        }
        int theme = getCurrentTheme(context);
        return theme == THEME_EXPRESSIVE;
    }
    
    /**
     * Get expressive mode color based on specific page mapping
     * Uses 10 pastel gradient colors mapped to specific pages
     * Colors work well in both light and dark mode with adaptive cards
     * @param context The context
     * @param pageKey Unique identifier for the page (e.g., fragment class name)
     * @return Color value for the page
     */
    public static int getExpressiveColor(Context context, String pageKey) {
        if (pageKey == null) {
            pageKey = "default";
        }
        
        // 10 pastel gradient colors for expressive mode
        // Colors chosen based on WCAG AA standards and Material Design guidelines
        int[] expressiveColors = {
            Color.rgb(0x90, 0xCA, 0xF9), // Pastel blue (#90CAF9) - calm, modern
            Color.rgb(0xFF, 0x6B, 0x9D), // Pastel pink/red (#FF6B9D) - warm, energetic
            Color.rgb(0x81, 0xC7, 0x84), // Pastel green (#81C784) - fresh, clean
            Color.rgb(0xFF, 0xD5, 0x4F), // Pastel amber (#FFD54F) - warm, high contrast
            Color.rgb(0xFF, 0xB7, 0x4D), // Pastel orange (#FFB74D) - vibrant, energetic
            Color.rgb(0xBA, 0x68, 0xC8), // Pastel purple (#BA68C8) - soft, gentle
            Color.rgb(0x4F, 0xC3, 0xF7), // Pastel cyan (#4FC3F7) - cool, refreshing
            Color.rgb(0xFF, 0x8A, 0x80), // Pastel coral (#FF8A80) - warm, inviting
            Color.rgb(0xA5, 0xD6, 0xA7), // Pastel mint (#A5D6A7) - fresh, calming
            Color.rgb(0xCE, 0x93, 0xD8)  // Pastel lavender (#CE93D8) - soft, elegant
        };
        
        // Map specific pages to specific colors
        // Security Extended = Pastel Blue (0)
        if (pageKey.contains("SecurityExtended") || pageKey.contains("SettingsExtendedSecurity") || 
            pageKey.contains("SecurityAdvancedSettings")) {
            return expressiveColors[0]; // Pastel blue
        }
        
        // User Info = Pastel Red/Pink (1)
        if (pageKey.contains("UserInfo") || pageKey.contains("UserInfoFragment") || 
            pageKey.contains("deviceinfo.UserInfoFragment")) {
            return expressiveColors[1]; // Pastel pink/red
        }
        
        // Apps pages = Different colors per app page
        if (pageKey.contains("AppInfoDashboard") || pageKey.contains("AppInfoDashboardFragment")) {
            return expressiveColors[0]; // Pastel blue
        }
        if (pageKey.contains("AppInfo") && !pageKey.contains("AppInfoDashboard")) {
            return expressiveColors[1]; // Pastel pink/red
        }
        if (pageKey.contains("ManageApplications") || pageKey.contains("ManageApplicationsFragment")) {
            return expressiveColors[2]; // Pastel green
        }
        if (pageKey.contains("ApplicationSettings") || pageKey.contains("AppSettings")) {
            return expressiveColors[3]; // Pastel amber
        }
        if (pageKey.contains("AppPermission") || pageKey.contains("PermissionSettings")) {
            return expressiveColors[4]; // Pastel orange
        }
        if (pageKey.contains("DefaultApp") || pageKey.contains("DefaultAppSettings")) {
            return expressiveColors[5]; // Pastel purple
        }
        if (pageKey.contains("SpecialAccess") || pageKey.contains("SpecialAccessSettings")) {
            return expressiveColors[6]; // Pastel cyan
        }
        if (pageKey.contains("UsageAccess") || pageKey.contains("UsageAccessSettings")) {
            return expressiveColors[7]; // Pastel coral
        }
        if (pageKey.contains("DrawOver") || pageKey.contains("DrawOverSettings")) {
            return expressiveColors[8]; // Pastel mint
        }
        if (pageKey.contains("WriteSettings") || pageKey.contains("WriteSettingsSettings")) {
            return expressiveColors[9]; // Pastel lavender
        }
        
        // Connected Devices and subpages = Different colors
        if (pageKey.contains("ConnectedDevice") || pageKey.contains("ConnectedDeviceDashboard")) {
            return expressiveColors[5]; // Pastel purple
        }
        if (pageKey.contains("Bluetooth") || pageKey.contains("BluetoothSettings")) {
            return expressiveColors[6]; // Pastel cyan
        }
        if (pageKey.contains("Nfc") || pageKey.contains("NfcSettings")) {
            return expressiveColors[7]; // Pastel coral
        }
        
        // Network pages - Different colors for each
        if (pageKey.contains("NetworkDashboard") || pageKey.contains("NetworkDashboardFragment")) {
            return expressiveColors[0]; // Pastel blue
        }
        if (pageKey.contains("WifiSettings") || pageKey.contains("WifiSettingsFragment")) {
            return expressiveColors[1]; // Pastel pink/red
        }
        if (pageKey.contains("WifiTether") || pageKey.contains("WifiTetherSettings")) {
            return expressiveColors[2]; // Pastel green
        }
        if (pageKey.contains("Wifi") && !pageKey.contains("WifiTether")) {
            return expressiveColors[3]; // Pastel amber
        }
        if (pageKey.contains("DataUsage") || pageKey.contains("DataUsageSettings")) {
            return expressiveColors[4]; // Pastel orange
        }
        if (pageKey.contains("Vpn") || pageKey.contains("VpnSettings")) {
            return expressiveColors[5]; // Pastel purple
        }
        if (pageKey.contains("MobileNetwork") || pageKey.contains("MobileNetworkSettings")) {
            return expressiveColors[6]; // Pastel cyan
        }
        if (pageKey.contains("AirplaneMode") || pageKey.contains("AirplaneModeSettings")) {
            return expressiveColors[7]; // Pastel coral
        }
        if (pageKey.contains("Ethernet") || pageKey.contains("EthernetSettings")) {
            return expressiveColors[8]; // Pastel mint
        }
        if (pageKey.contains("Network") && !pageKey.contains("NetworkDashboard")) {
            return expressiveColors[9]; // Pastel lavender
        }
        
        // Display pages
        if (pageKey.contains("Display") || pageKey.contains("DisplaySettings") || 
            pageKey.contains("DisplayPageGrid")) {
            return expressiveColors[0]; // Pastel blue
        }
        
        // System pages
        if (pageKey.contains("System") || pageKey.contains("SystemGrid") || 
            pageKey.contains("SystemBasicDefaults")) {
            return expressiveColors[1]; // Pastel pink/red
        }
        
        // Privacy pages
        if (pageKey.contains("Privacy") || pageKey.contains("PrivacyDashboard")) {
            return expressiveColors[2]; // Pastel green
        }
        
        // Sound pages
        if (pageKey.contains("Sound") || pageKey.contains("SoundSettings")) {
            return expressiveColors[3]; // Pastel amber
        }
        
        // Notification pages
        if (pageKey.contains("Notification") || pageKey.contains("ConfigureNotification")) {
            return expressiveColors[4]; // Pastel orange
        }
        
        // Battery pages
        if (pageKey.contains("Battery") || pageKey.contains("PowerUsageSummary")) {
            return expressiveColors[5]; // Pastel purple
        }
        
        // Storage pages
        if (pageKey.contains("Storage") || pageKey.contains("StorageSettings")) {
            return expressiveColors[6]; // Pastel cyan
        }
        
        // Accessibility pages
        if (pageKey.contains("Accessibility") || pageKey.contains("AccessibilitySettings")) {
            return expressiveColors[7]; // Pastel coral
        }
        
        // Location pages - Different colors for each
        if (pageKey.contains("LocationSettings") || pageKey.contains("LocationSettingsFragment")) {
            return expressiveColors[0]; // Pastel blue
        }
        if (pageKey.contains("LocationServices") || pageKey.contains("LocationServicesSettings")) {
            return expressiveColors[1]; // Pastel pink/red
        }
        if (pageKey.contains("LocationScanning") || pageKey.contains("LocationScanningSettings")) {
            return expressiveColors[2]; // Pastel green
        }
        if (pageKey.contains("Location") && !pageKey.contains("LocationSettings") && 
            !pageKey.contains("LocationServices") && !pageKey.contains("LocationScanning")) {
            return expressiveColors[3]; // Pastel amber
        }
        
        // Gesture pages
        if (pageKey.contains("Gesture") || pageKey.contains("GestureSettings")) {
            return expressiveColors[9]; // Pastel lavender
        }
        
        // Additional 60+ page mappings for comprehensive coverage
        // Account pages
        if (pageKey.contains("Account") || pageKey.contains("AccountDashboard")) {
            return expressiveColors[0]; // Pastel blue
        }
        if (pageKey.contains("Passwords") || pageKey.contains("PasswordSettings")) {
            return expressiveColors[1]; // Pastel pink/red
        }
        
        // App-specific pages - Already handled above in Apps pages section
        
        // Network subpages - Already handled above in Network pages section
        
        // Connected Devices subpages
        if (pageKey.contains("Usb") || pageKey.contains("UsbSettings")) {
            return expressiveColors[6]; // Pastel cyan
        }
        if (pageKey.contains("Print") || pageKey.contains("PrintSettings")) {
            return expressiveColors[7]; // Pastel coral
        }
        if (pageKey.contains("Cast") || pageKey.contains("CastSettings")) {
            return expressiveColors[8]; // Pastel mint
        }
        if (pageKey.contains("MediaOutput") || pageKey.contains("MediaOutputSettings")) {
            return expressiveColors[9]; // Pastel lavender
        }
        
        // Display subpages
        if (pageKey.contains("NightDisplay") || pageKey.contains("NightDisplaySettings")) {
            return expressiveColors[0]; // Pastel blue
        }
        if (pageKey.contains("Brightness") || pageKey.contains("BrightnessSettings")) {
            return expressiveColors[1]; // Pastel pink/red
        }
        if (pageKey.contains("FontSize") || pageKey.contains("FontSizeSettings")) {
            return expressiveColors[2]; // Pastel green
        }
        if (pageKey.contains("ScreenTimeout") || pageKey.contains("ScreenTimeoutSettings")) {
            return expressiveColors[3]; // Pastel amber
        }
        if (pageKey.contains("Wallpaper") || pageKey.contains("WallpaperSettings")) {
            return expressiveColors[4]; // Pastel orange
        }
        if (pageKey.contains("Theme") || pageKey.contains("ThemeSettings")) {
            return expressiveColors[5]; // Pastel purple
        }
        
        // Sound subpages
        if (pageKey.contains("Ringtone") || pageKey.contains("RingtoneSettings")) {
            return expressiveColors[6]; // Pastel cyan
        }
        if (pageKey.contains("NotificationSound") || pageKey.contains("NotificationSoundSettings")) {
            return expressiveColors[7]; // Pastel coral
        }
        if (pageKey.contains("AlarmSound") || pageKey.contains("AlarmSoundSettings")) {
            return expressiveColors[8]; // Pastel mint
        }
        if (pageKey.contains("DoNotDisturb") || pageKey.contains("DoNotDisturbSettings")) {
            return expressiveColors[9]; // Pastel lavender
        }
        
        // Notification subpages
        if (pageKey.contains("AppNotification") || pageKey.contains("AppNotificationSettings")) {
            return expressiveColors[0]; // Pastel blue
        }
        if (pageKey.contains("Conversation") || pageKey.contains("ConversationSettings")) {
            return expressiveColors[1]; // Pastel pink/red
        }
        if (pageKey.contains("Bubble") || pageKey.contains("BubbleSettings")) {
            return expressiveColors[2]; // Pastel green
        }
        if (pageKey.contains("NotificationAssistant") || pageKey.contains("NotificationAssistantSettings")) {
            return expressiveColors[3]; // Pastel amber
        }
        
        // Battery subpages
        if (pageKey.contains("BatterySaver") || pageKey.contains("BatterySaverSettings")) {
            return expressiveColors[4]; // Pastel orange
        }
        if (pageKey.contains("BatteryUsage") || pageKey.contains("BatteryUsageSettings")) {
            return expressiveColors[5]; // Pastel purple
        }
        if (pageKey.contains("AdaptiveBattery") || pageKey.contains("AdaptiveBatterySettings")) {
            return expressiveColors[6]; // Pastel cyan
        }
        
        // Storage subpages
        if (pageKey.contains("StorageUsage") || pageKey.contains("StorageUsageSettings")) {
            return expressiveColors[7]; // Pastel coral
        }
        if (pageKey.contains("ManageStorage") || pageKey.contains("ManageStorageSettings")) {
            return expressiveColors[8]; // Pastel mint
        }
        
        // Security subpages
        if (pageKey.contains("ScreenLock") || pageKey.contains("ScreenLockSettings")) {
            return expressiveColors[9]; // Pastel lavender
        }
        if (pageKey.contains("Encryption") || pageKey.contains("EncryptionSettings")) {
            return expressiveColors[0]; // Pastel blue
        }
        if (pageKey.contains("Credential") || pageKey.contains("CredentialSettings")) {
            return expressiveColors[1]; // Pastel pink/red
        }
        if (pageKey.contains("TrustAgent") || pageKey.contains("TrustAgentSettings")) {
            return expressiveColors[2]; // Pastel green
        }
        if (pageKey.contains("DeviceAdmin") || pageKey.contains("DeviceAdminSettings")) {
            return expressiveColors[3]; // Pastel amber
        }
        if (pageKey.contains("SimLock") || pageKey.contains("SimLockSettings")) {
            return expressiveColors[4]; // Pastel orange
        }
        if (pageKey.contains("ScreenPinning") || pageKey.contains("ScreenPinningSettings")) {
            return expressiveColors[5]; // Pastel purple
        }
        
        // Privacy subpages - Different colors for each
        if (pageKey.contains("PrivacyControls") || pageKey.contains("PrivacyControlsSettings")) {
            return expressiveColors[4]; // Pastel orange
        }
        if (pageKey.contains("CameraToggle") || pageKey.contains("CameraToggleSettings")) {
            return expressiveColors[5]; // Pastel purple
        }
        if (pageKey.contains("MicToggle") || pageKey.contains("MicToggleSettings")) {
            return expressiveColors[6]; // Pastel cyan
        }
        if (pageKey.contains("Permission") && !pageKey.contains("AppPermission")) {
            return expressiveColors[7]; // Pastel coral
        }
        
        // Accessibility subpages
        if (pageKey.contains("AccessibilityService") || pageKey.contains("AccessibilityServiceSettings")) {
            return expressiveColors[2]; // Pastel green
        }
        if (pageKey.contains("Caption") || pageKey.contains("CaptionSettings")) {
            return expressiveColors[3]; // Pastel amber
        }
        if (pageKey.contains("Magnification") || pageKey.contains("MagnificationSettings")) {
            return expressiveColors[4]; // Pastel orange
        }
        if (pageKey.contains("ColorCorrection") || pageKey.contains("ColorCorrectionSettings")) {
            return expressiveColors[5]; // Pastel purple
        }
        if (pageKey.contains("TextToSpeech") || pageKey.contains("TextToSpeechSettings")) {
            return expressiveColors[6]; // Pastel cyan
        }
        if (pageKey.contains("DisplaySize") || pageKey.contains("DisplaySizeSettings")) {
            return expressiveColors[7]; // Pastel coral
        }
        if (pageKey.contains("FontSize") || pageKey.contains("FontSizeSettings")) {
            return expressiveColors[8]; // Pastel mint
        }
        if (pageKey.contains("SelectToSpeak") || pageKey.contains("SelectToSpeakSettings")) {
            return expressiveColors[9]; // Pastel lavender
        }
        if (pageKey.contains("TalkBack") || pageKey.contains("TalkBackSettings")) {
            return expressiveColors[0]; // Pastel blue
        }
        if (pageKey.contains("SwitchAccess") || pageKey.contains("SwitchAccessSettings")) {
            return expressiveColors[1]; // Pastel pink/red
        }
        
        // System subpages
        if (pageKey.contains("Language") || pageKey.contains("LanguageSettings")) {
            return expressiveColors[2]; // Pastel green
        }
        if (pageKey.contains("DateTime") || pageKey.contains("DateTimeSettings")) {
            return expressiveColors[3]; // Pastel amber
        }
        if (pageKey.contains("Backup") || pageKey.contains("BackupSettings")) {
            return expressiveColors[4]; // Pastel orange
        }
        if (pageKey.contains("Reset") || pageKey.contains("ResetSettings")) {
            return expressiveColors[5]; // Pastel purple
        }
        if (pageKey.contains("About") || pageKey.contains("AboutSettings")) {
            return expressiveColors[6]; // Pastel cyan
        }
        if (pageKey.contains("Status") || pageKey.contains("StatusSettings")) {
            return expressiveColors[7]; // Pastel coral
        }
        if (pageKey.contains("Legal") || pageKey.contains("LegalSettings")) {
            return expressiveColors[8]; // Pastel mint
        }
        
        // Default: Use hash-based selection for unmapped pages
        int hash = pageKey.hashCode();
        int colorIndex = Math.abs(hash) % expressiveColors.length;
        return expressiveColors[colorIndex];
    }
    
    /**
     * Check if a theme should use animated wallpaper blur
     * Animated wallpaper blur uses system wallpaper with fast-moving blur effect
     * Only works in dark mode
     * @param context The context
     * @return true if animated wallpaper blur should be used
     */
    public static boolean shouldUseAnimatedWallpaperBlur(Context context) {
        if (!isDarkMode(context)) {
            return false; // Only work in dark mode
        }
        int theme = getCurrentTheme(context);
        return theme == THEME_ANIMATED_WALLPAPER_BLUR;
    }
    
    /**
     * Check if theme is valid
     * @param themeValue Theme value to validate
     * @return true if valid (0-9)
     */
    public static boolean isValidTheme(int themeValue) {
        return themeValue >= 0 && themeValue <= 9;
    }
    
    /**
     * Check if custom picture theme is enabled
     * Only works in dark mode
     * @param context The context
     * @return true if custom picture theme is enabled
     */
    public static boolean shouldUseCustomPicture(Context context) {
        if (!isDarkMode(context)) {
            return false; // Only work in dark mode
        }
        int theme = getCurrentTheme(context);
        return theme == THEME_CUSTOM_PICTURE;
    }
}

