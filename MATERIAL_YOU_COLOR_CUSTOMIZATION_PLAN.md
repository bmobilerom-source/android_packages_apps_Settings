# Material You Color Customization - Implementation Plan

## Executive Summary

This plan outlines how to implement ColorBlendr-like Material You color customization features directly in the Settings app. Unlike ColorBlendr which requires root or Shizuku, this implementation uses Android's built-in Runtime Resource Overlay (RRO) system and framework APIs, making it available to all users without root privileges.

**Reference:** Based on analysis of [ColorBlendr](https://github.com/TeaqariaWTF/ColorBlendr) features, adapted for built-in Settings integration.

---

## 1. ColorBlendr Feature Analysis

### 1.1 Core Features in ColorBlendr

Based on the [ColorBlendr repository](https://github.com/TeaqariaWTF/ColorBlendr):

1. **Accent Saturation Changer**
   - Adjust saturation of accent colors
   - Range: 0-200% (typically)
   - Affects primary accent colors

2. **Background Saturation Changer**
   - Adjust saturation of background colors
   - Range: 0-200%
   - Affects neutral/background tones

3. **Background Lightness Changer**
   - Adjust lightness/brightness of backgrounds
   - Range: 0-100%
   - Dark mode and light mode support

4. **Pitch Black Theme**
   - Pure black (#000000) backgrounds in dark mode
   - Overrides default dark gray backgrounds
   - Better for OLED displays

5. **Manual Color Override**
   - Pick custom accent colors
   - Override wallpaper-extracted colors
   - Custom primary/secondary/tertiary colors

6. **Color Style Selection**
   - TONAL_SPOT, VIBRANT, EXPRESSIVE, etc.
   - Different Monet color generation styles

### 1.2 How ColorBlendr Works

**With Root:**
- Uses FabricatedOverlay API to create runtime overlays
- Dynamically modifies Material You colors without permanent files
- Applies changes immediately

**Without Root (Shizuku/ADB):**
- Uses ADB commands to modify system properties
- Limited functionality
- Requires Shizuku or ADB access

**Our Approach (Built-in):**
- Use Runtime Resource Overlays (RRO) system
- Create dynamic overlay package
- Modify Monet color generation parameters
- Store preferences in Settings.Secure/System
- Apply changes via OverlayManager API

---

## 2. Android Material You Color System

### 2.1 Monet Color Generation

**Key Components:**
- `WallpaperColors` - Extracted from wallpaper
- `ColorScheme` - Generates color palettes
- `TonalPalette` - Contains color shades (50-900)
- Theme overlays - Apply colors system-wide

**Color Properties:**
- `system_accent1_*` - Primary accent (50-900 shades)
- `system_accent2_*` - Secondary accent
- `system_accent3_*` - Tertiary accent
- `system_neutral1_*` - Primary neutral/background
- `system_neutral2_*` - Secondary neutral

**Modification Points:**
1. **Saturation** - Adjust color saturation before palette generation
2. **Lightness** - Adjust brightness/lightness of backgrounds
3. **Hue** - Override hue for accent colors
4. **Style** - Change Monet generation style (TONAL_SPOT, VIBRANT, etc.)

### 2.2 Runtime Resource Overlays (RRO)

**How RRO Works:**
- Overlay packages can override framework resources
- Applied at runtime without app restart
- Priority-based overlay stacking
- Can be enabled/disabled dynamically

**Our Implementation:**
- Create overlay package: `com.android.settings.materialyou.overlay`
- Override `system_accent*` and `system_neutral*` colors
- Apply modifications based on user preferences
- Enable/disable via OverlayManager

---

## 3. Architecture Design

### 3.1 Component Structure

```
┌─────────────────────────────────────────────────────────────┐
│         MaterialYouColorSettings Fragment                   │
│  ┌───────────────────────────────────────────────────────┐ │
│  │   MaterialYouColorManager (Singleton)                 │ │
│  │  - Manages color modifications                         │ │
│  │  - Creates/updates overlay package                     │ │
│  │  - Handles color calculations                          │ │
│  │  - Persists settings                                   │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              Preference Controllers                    │ │
│  │  - AccentSaturationController                          │ │
│  │  - BackgroundSaturationController                      │ │
│  │  - BackgroundLightnessController                       │ │
│  │  - PitchBlackController                                │ │
│  │  - CustomColorController                               │ │
│  │  - ColorStyleController                                │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              Custom Preference Views                  │ │
│  │  - ColorSaturationSeekBarPreference                   │ │
│  │  - ColorLightnessSeekBarPreference                    │ │
│  │  - ColorPickerPreference                              │ │
│  │  - ColorPreviewView                                    │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 File Structure

```
packages/apps/Settings/
├── src/com/android/settings/display/
│   ├── MaterialYouColorSettings.java          # Main fragment
│   ├── MaterialYouColorManager.java           # Singleton manager
│   ├── controllers/
│   │   ├── AccentSaturationController.java
│   │   ├── BackgroundSaturationController.java
│   │   ├── BackgroundLightnessController.java
│   │   ├── PitchBlackController.java
│   │   ├── CustomColorController.java
│   │   └── ColorStyleController.java
│   └── utils/
│       ├── ColorUtils.java                    # Color manipulation
│       └── OverlayManager.java                # Overlay management
├── res/
│   ├── xml/
│   │   └── material_you_color_settings.xml    # Preference screen
│   ├── layout/
│   │   ├── color_picker_preference.xml
│   │   ├── color_preview_view.xml
│   │   └── color_saturation_seekbar.xml
│   └── values/
│       └── strings.xml                         # New strings
└── overlay/
    └── MaterialYouOverlay/
        ├── AndroidManifest.xml
        ├── Android.bp
        └── res/
            └── values/
                └── colors.xml                  # Dynamic color overrides
```

---

## 4. Implementation Details

### 4.1 MaterialYouColorManager (Singleton)

**Purpose:** Central manager for Material You color customization

**Key Responsibilities:**
- Calculate modified colors based on user preferences
- Create and update overlay package
- Manage overlay lifecycle
- Persist settings to Settings.Secure
- Apply color modifications

**Implementation:**

```java
package com.android.settings.display;

import android.content.Context;
import android.content.om.OverlayInfo;
import android.content.om.OverlayManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class MaterialYouColorManager {
    private static final String TAG = "MaterialYouColorManager";
    private static MaterialYouColorManager sInstance;
    
    // Overlay package name
    private static final String OVERLAY_PACKAGE = "com.android.settings.materialyou.overlay";
    
    // Settings keys
    private static final String KEY_ACCENT_SATURATION = "material_you_accent_saturation";
    private static final String KEY_BACKGROUND_SATURATION = "material_you_background_saturation";
    private static final String KEY_BACKGROUND_LIGHTNESS = "material_you_background_lightness";
    private static final String KEY_PITCH_BLACK = "material_you_pitch_black";
    private static final String KEY_CUSTOM_ACCENT_COLOR = "material_you_custom_accent_color";
    private static final String KEY_COLOR_STYLE = "material_you_color_style";
    
    // Default values
    private static final int DEFAULT_SATURATION = 100; // 100% = no change
    private static final int DEFAULT_LIGHTNESS = 100;
    private static final int DEFAULT_COLOR_STYLE = 0; // TONAL_SPOT
    
    private Context mContext;
    private OverlayManager mOverlayManager;
    private MaterialYouOverlayHelper mOverlayHelper;
    
    private MaterialYouColorManager(Context context) {
        mContext = context.getApplicationContext();
        mOverlayManager = mContext.getSystemService(OverlayManager.class);
        mOverlayHelper = new MaterialYouOverlayHelper(context);
    }
    
    public static synchronized MaterialYouColorManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MaterialYouColorManager(context);
        }
        return sInstance;
    }
    
    /**
     * Apply all color modifications
     */
    public void applyColorModifications() {
        if (!isOverlayInstalled()) {
            installOverlay();
        }
        
        updateOverlayColors();
        enableOverlay();
    }
    
    /**
     * Get accent saturation (0-200, 100 = no change)
     */
    public int getAccentSaturation() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            KEY_ACCENT_SATURATION, DEFAULT_SATURATION);
    }
    
    public void setAccentSaturation(int saturation) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            KEY_ACCENT_SATURATION, saturation);
        applyColorModifications();
    }
    
    /**
     * Get background saturation (0-200, 100 = no change)
     */
    public int getBackgroundSaturation() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            KEY_BACKGROUND_SATURATION, DEFAULT_SATURATION);
    }
    
    public void setBackgroundSaturation(int saturation) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            KEY_BACKGROUND_SATURATION, saturation);
        applyColorModifications();
    }
    
    /**
     * Get background lightness (0-200, 100 = no change)
     */
    public int getBackgroundLightness() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            KEY_BACKGROUND_LIGHTNESS, DEFAULT_LIGHTNESS);
    }
    
    public void setBackgroundLightness(int lightness) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            KEY_BACKGROUND_LIGHTNESS, lightness);
        applyColorModifications();
    }
    
    /**
     * Check if pitch black theme is enabled
     */
    public boolean isPitchBlackEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            KEY_PITCH_BLACK, 0) == 1;
    }
    
    public void setPitchBlackEnabled(boolean enabled) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            KEY_PITCH_BLACK, enabled ? 1 : 0);
        applyColorModifications();
    }
    
    /**
     * Get custom accent color (ARGB int, 0 = use wallpaper)
     */
    public int getCustomAccentColor() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            KEY_CUSTOM_ACCENT_COLOR, 0);
    }
    
    public void setCustomAccentColor(int color) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            KEY_CUSTOM_ACCENT_COLOR, color);
        applyColorModifications();
    }
    
    /**
     * Get color style (0=TONAL_SPOT, 1=VIBRANT, etc.)
     */
    public int getColorStyle() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            KEY_COLOR_STYLE, DEFAULT_COLOR_STYLE);
    }
    
    public void setColorStyle(int style) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            KEY_COLOR_STYLE, style);
        applyColorModifications();
    }
    
    /**
     * Calculate modified color with saturation adjustment
     */
    public int adjustSaturation(int color, int saturationPercent) {
        if (saturationPercent == 100) {
            return color; // No change
        }
        
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        
        // Adjust saturation (0.0 to 2.0, where 1.0 = 100%)
        float saturationMultiplier = saturationPercent / 100f;
        hsv[1] = Math.max(0f, Math.min(1f, hsv[1] * saturationMultiplier));
        
        return Color.HSVToColor(Color.alpha(color), hsv);
    }
    
    /**
     * Calculate modified color with lightness adjustment
     */
    public int adjustLightness(int color, int lightnessPercent) {
        if (lightnessPercent == 100) {
            return color; // No change
        }
        
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        
        // Adjust value (lightness) (0.0 to 2.0, where 1.0 = 100%)
        float lightnessMultiplier = lightnessPercent / 100f;
        hsv[2] = Math.max(0f, Math.min(1f, hsv[2] * lightnessMultiplier));
        
        return Color.HSVToColor(Color.alpha(color), hsv);
    }
    
    /**
     * Update overlay colors based on current settings
     */
    private void updateOverlayColors() {
        // Get current wallpaper colors
        WallpaperColors wallpaperColors = getWallpaperColors();
        if (wallpaperColors == null) {
            Log.w(TAG, "No wallpaper colors available");
            return;
        }
        
        // Calculate modified colors
        Map<String, Integer> colorOverrides = new HashMap<>();
        
        // Apply accent saturation
        int accentSaturation = getAccentSaturation();
        if (accentSaturation != 100) {
            // Modify accent colors
            modifyAccentColors(colorOverrides, wallpaperColors, accentSaturation);
        }
        
        // Apply background saturation and lightness
        int bgSaturation = getBackgroundSaturation();
        int bgLightness = getBackgroundLightness();
        if (bgSaturation != 100 || bgLightness != 100) {
            modifyBackgroundColors(colorOverrides, wallpaperColors, bgSaturation, bgLightness);
        }
        
        // Apply pitch black
        if (isPitchBlackEnabled()) {
            applyPitchBlack(colorOverrides);
        }
        
        // Apply custom accent color
        int customColor = getCustomAccentColor();
        if (customColor != 0) {
            applyCustomAccentColor(colorOverrides, customColor);
        }
        
        // Update overlay
        mOverlayHelper.updateColors(colorOverrides);
    }
    
    /**
     * Modify accent colors with saturation adjustment
     */
    private void modifyAccentColors(Map<String, Integer> overrides,
            WallpaperColors colors, int saturation) {
        // Get primary accent color
        Color primaryAccent = colors.getPrimaryColor();
        if (primaryAccent != null) {
            int modifiedColor = adjustSaturation(primaryAccent.toArgb(), saturation);
            // Apply to all accent shades
            applyToAccentShades(overrides, modifiedColor);
        }
    }
    
    /**
     * Modify background colors with saturation and lightness
     */
    private void modifyBackgroundColors(Map<String, Integer> overrides,
            WallpaperColors colors, int saturation, int lightness) {
        // Get neutral colors
        Color neutralColor = colors.getSecondaryColor();
        if (neutralColor != null) {
            int color = neutralColor.toArgb();
            color = adjustSaturation(color, saturation);
            color = adjustLightness(color, lightness);
            // Apply to neutral shades
            applyToNeutralShades(overrides, color);
        }
    }
    
    /**
     * Apply pitch black theme
     */
    private void applyPitchBlack(Map<String, Integer> overrides) {
        // Override dark mode backgrounds with pure black
        overrides.put("system_neutral1_900", Color.BLACK);
        overrides.put("system_neutral1_1000", Color.BLACK);
        overrides.put("system_neutral2_900", Color.BLACK);
        overrides.put("system_neutral2_1000", Color.BLACK);
        overrides.put("system_surface_container_dark", Color.BLACK);
        overrides.put("system_surface_dim_dark", Color.BLACK);
    }
    
    /**
     * Apply custom accent color
     */
    private void applyCustomAccentColor(Map<String, Integer> overrides, int color) {
        // Generate tonal palette from custom color
        TonalPalette palette = TonalPalette.fromInt(color);
        applyTonalPaletteToAccent(overrides, palette);
    }
    
    /**
     * Get current wallpaper colors
     */
    private WallpaperColors getWallpaperColors() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
        return wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
    }
    
    /**
     * Check if overlay is installed
     */
    private boolean isOverlayInstalled() {
        try {
            PackageManager pm = mContext.getPackageManager();
            pm.getPackageInfo(OVERLAY_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Install overlay package
     */
    private void installOverlay() {
        // Overlay should be pre-installed as part of Settings app
        // This method ensures it's available
        mOverlayHelper.ensureOverlayInstalled();
    }
    
    /**
     * Enable overlay
     */
    private void enableOverlay() {
        try {
            OverlayInfo info = mOverlayManager.getOverlayInfo(
                OVERLAY_PACKAGE, UserHandle.myUserId());
            if (info != null && !info.isEnabled()) {
                mOverlayManager.setEnabled(OVERLAY_PACKAGE, true, UserHandle.myUserId());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error enabling overlay", e);
        }
    }
    
    /**
     * Disable overlay
     */
    public void disableOverlay() {
        try {
            mOverlayManager.setEnabled(OVERLAY_PACKAGE, false, UserHandle.myUserId());
        } catch (Exception e) {
            Log.e(TAG, "Error disabling overlay", e);
        }
    }
    
    /**
     * Reset all modifications
     */
    public void reset() {
        Settings.Secure.putInt(mContext.getContentResolver(), KEY_ACCENT_SATURATION, DEFAULT_SATURATION);
        Settings.Secure.putInt(mContext.getContentResolver(), KEY_BACKGROUND_SATURATION, DEFAULT_SATURATION);
        Settings.Secure.putInt(mContext.getContentResolver(), KEY_BACKGROUND_LIGHTNESS, DEFAULT_LIGHTNESS);
        Settings.Secure.putInt(mContext.getContentResolver(), KEY_PITCH_BLACK, 0);
        Settings.Secure.putInt(mContext.getContentResolver(), KEY_CUSTOM_ACCENT_COLOR, 0);
        Settings.Secure.putInt(mContext.getContentResolver(), KEY_COLOR_STYLE, DEFAULT_COLOR_STYLE);
        
        disableOverlay();
    }
    
    // Helper methods for applying colors to shades
    private void applyToAccentShades(Map<String, Integer> overrides, int baseColor) {
        // Generate tonal palette and apply to all accent shades
        TonalPalette palette = TonalPalette.fromInt(baseColor);
        for (int shade = 50; shade <= 900; shade += 50) {
            overrides.put("system_accent1_" + shade, palette.get(shade));
        }
    }
    
    private void applyToNeutralShades(Map<String, Integer> overrides, int baseColor) {
        // Similar for neutral colors
        TonalPalette palette = TonalPalette.fromInt(baseColor);
        for (int shade = 50; shade <= 1000; shade += 50) {
            overrides.put("system_neutral1_" + shade, palette.get(shade));
            overrides.put("system_neutral2_" + shade, palette.get(shade));
        }
    }
    
    private void applyTonalPaletteToAccent(Map<String, Integer> overrides, TonalPalette palette) {
        for (int shade = 50; shade <= 900; shade += 50) {
            overrides.put("system_accent1_" + shade, palette.get(shade));
        }
    }
}
```

### 4.2 MaterialYouOverlayHelper

**Purpose:** Helper class for managing overlay package

**Responsibilities:**
- Create/update overlay colors.xml
- Install overlay package
- Update overlay resources dynamically

**Implementation:**

```java
package com.android.settings.display;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class MaterialYouOverlayHelper {
    private static final String TAG = "MaterialYouOverlayHelper";
    private Context mContext;
    
    public MaterialYouOverlayHelper(Context context) {
        mContext = context;
    }
    
    /**
     * Update overlay colors
     */
    public void updateColors(Map<String, Integer> colorOverrides) {
        // Generate colors.xml content
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<resources>\n");
        
        for (Map.Entry<String, Integer> entry : colorOverrides.entrySet()) {
            String colorName = entry.getKey();
            int color = entry.getValue();
            String hexColor = String.format("#%08X", color);
            xml.append("  <color name=\"").append(colorName).append("\">")
               .append(hexColor).append("</color>\n");
        }
        
        xml.append("</resources>\n");
        
        // Write to overlay package
        writeOverlayColors(xml.toString());
    }
    
    /**
     * Write colors.xml to overlay package
     */
    private void writeOverlayColors(String xmlContent) {
        // In a real implementation, this would:
        // 1. Create/update overlay APK
        // 2. Install overlay package
        // 3. Enable overlay
        
        // For now, we'll use a simpler approach:
        // Store colors in Settings and apply via framework API
        // The actual overlay package should be pre-built
        
        Log.d(TAG, "Updating overlay colors");
        // Implementation would update overlay package resources
    }
    
    /**
     * Ensure overlay is installed
     */
    public void ensureOverlayInstalled() {
        // Overlay should be pre-installed
        // This method verifies it exists
        PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo("com.android.settings.materialyou.overlay", 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Overlay package not found - should be pre-installed");
        }
    }
}
```

### 4.3 MaterialYouColorSettings Fragment

**Purpose:** Main Settings page for Material You customization

**Implementation:**

```java
package com.android.settings.display;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

public class MaterialYouColorSettings extends DashboardFragment {
    private static final String TAG = "MaterialYouColorSettings";
    
    private MaterialYouColorManager mColorManager;
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mColorManager = MaterialYouColorManager.getInstance(context);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected String getLogTag() {
        return TAG;
    }
    
    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.material_you_color_settings;
    }
    
    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new AccentSaturationController(context, this));
        controllers.add(new BackgroundSaturationController(context, this));
        controllers.add(new BackgroundLightnessController(context, this));
        controllers.add(new PitchBlackController(context, this));
        controllers.add(new CustomColorController(context, this));
        controllers.add(new ColorStyleController(context, this));
        return controllers;
    }
    
    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }
}
```

### 4.4 Preference Controllers

**Example: AccentSaturationController**

```java
package com.android.settings.display.controllers;

import android.content.Context;
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.display.MaterialYouColorManager;
import com.android.settings.widget.SeekBarPreference;

public class AccentSaturationController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {
    
    private MaterialYouColorManager mColorManager;
    private SeekBarPreference mPreference;
    
    public AccentSaturationController(Context context, String key) {
        super(context, key);
        mColorManager = MaterialYouColorManager.getInstance(context);
    }
    
    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
    
    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        if (mPreference != null) {
            mPreference.setOnPreferenceChangeListener(this);
            mPreference.setMax(200); // 0-200%
            mPreference.setProgress(mColorManager.getAccentSaturation());
            mPreference.setSummary(mContext.getString(
                R.string.accent_saturation_summary, mPreference.getProgress()));
        }
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int saturation = (Integer) newValue;
        mColorManager.setAccentSaturation(saturation);
        mPreference.setSummary(mContext.getString(
            R.string.accent_saturation_summary, saturation));
        return true;
    }
}
```

---

## 5. Overlay Package Structure

### 5.1 Overlay Package Creation

**Location:** `packages/apps/Settings/overlay/MaterialYouOverlay/`

**AndroidManifest.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.android.settings.materialyou.overlay">
    
    <overlay
        android:targetPackage="android"
        android:priority="100"
        android:isStatic="false" />
        
    <application
        android:label="@string/material_you_overlay_label"
        android:hasCode="false" />
</manifest>
```

**Android.bp:**
```
runtime_resource_overlay {
    name: "MaterialYouOverlay",
    
    theme: "MaterialYouOverlay",
    product_specific: true,
}
```

**res/values/colors.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Colors will be dynamically updated by MaterialYouColorManager -->
    <!-- Initial values match system defaults -->
</resources>
```

---

## 6. Preference Screen Layout

### 6.1 material_you_color_settings.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:settings="http://schemas.android.com/apk/res-auto"
    android:title="@string/material_you_color_title">
    
    <!-- Master Toggle -->
    <SwitchPreferenceCompat
        android:key="material_you_color_enabled"
        android:title="@string/material_you_color_enable_title"
        android:summary="@string/material_you_color_enable_summary"
        android:order="0"
        settings:controller="com.android.settings.display.controllers.MaterialYouMasterController"/>
    
    <!-- Accent Saturation -->
    <com.android.settings.widget.SeekBarPreference
        android:key="accent_saturation"
        android:title="@string/accent_saturation_title"
        android:summary="@string/accent_saturation_summary"
        android:icon="@drawable/ic_palette"
        android:layout="@layout/dot_preference_top_card_progress"
        android:order="10"
        android:max="200"
        android:defaultValue="100"
        settings:controller="com.android.settings.display.controllers.AccentSaturationController"/>
    
    <!-- Background Saturation -->
    <com.android.settings.widget.SeekBarPreference
        android:key="background_saturation"
        android:title="@string/background_saturation_title"
        android:summary="@string/background_saturation_summary"
        android:icon="@drawable/ic_palette"
        android:layout="@layout/dot_preference_middle_card_progress"
        android:order="20"
        android:max="200"
        android:defaultValue="100"
        settings:controller="com.android.settings.display.controllers.BackgroundSaturationController"/>
    
    <!-- Background Lightness -->
    <com.android.settings.widget.SeekBarPreference
        android:key="background_lightness"
        android:title="@string/background_lightness_title"
        android:summary="@string/background_lightness_summary"
        android:icon="@drawable/ic_brightness"
        android:layout="@layout/dot_preference_middle_card_progress"
        android:order="30"
        android:max="200"
        android:defaultValue="100"
        settings:controller="com.android.settings.display.controllers.BackgroundLightnessController"/>
    
    <!-- Pitch Black Theme -->
    <SwitchPreferenceCompat
        android:key="pitch_black"
        android:title="@string/pitch_black_title"
        android:summary="@string/pitch_black_summary"
        android:order="40"
        settings:controller="com.android.settings.display.controllers.PitchBlackController"/>
    
    <!-- Custom Accent Color -->
    <Preference
        android:key="custom_accent_color"
        android:title="@string/custom_accent_color_title"
        android:summary="@string/custom_accent_color_summary"
        android:order="50"
        settings:controller="com.android.settings.display.controllers.CustomColorController"/>
    
    <!-- Color Style -->
    <ListPreference
        android:key="color_style"
        android:title="@string/color_style_title"
        android:summary="@string/color_style_summary"
        android:order="60"
        android:entries="@array/color_style_names"
        android:entryValues="@array/color_style_values"
        settings:controller="com.android.settings.display.controllers.ColorStyleController"/>
    
    <!-- Reset -->
    <Preference
        android:key="reset_colors"
        android:title="@string/reset_colors_title"
        android:summary="@string/reset_colors_summary"
        android:order="100"
        settings:controller="com.android.settings.display.controllers.ResetColorsController"/>
        
</PreferenceScreen>
```

### 6.2 Integration into Display Settings

**Add to display settings or create new entry:**

```xml
<!-- In display_settings.xml or top_level_settings.xml -->
<Preference
    android:key="material_you_colors"
    android:title="@string/material_you_color_title"
    android:fragment="com.android.settings.display.MaterialYouColorSettings"
    android:order="-110"
    settings:keywords="@string/keywords_material_you_colors"/>
```

---

## 7. Implementation Phases

### Phase 1: Core Infrastructure
**Goal:** Basic color modification system

**Tasks:**
1. Create `MaterialYouColorManager.java`
2. Create overlay package structure
3. Implement basic color calculation methods
4. Add Settings.Secure persistence
5. Test overlay creation and enabling

**Success Criteria:**
- Overlay package installs correctly
- Colors can be modified
- Settings persist

### Phase 2: Saturation & Lightness Controls
**Goal:** Basic color adjustments

**Tasks:**
1. Create saturation controllers
2. Create lightness controller
3. Implement HSV color manipulation
4. Add seekbar preferences
5. Test color modifications

**Success Criteria:**
- Saturation adjustments work
- Lightness adjustments work
- Changes apply immediately
- No visual artifacts

### Phase 3: Pitch Black & Custom Colors
**Goal:** Advanced features

**Tasks:**
1. Create pitch black controller
2. Create custom color picker
3. Implement color picker UI
4. Add color preview
5. Test all features together

**Success Criteria:**
- Pitch black works
- Custom colors apply
- Color picker works
- Preview displays correctly

### Phase 4: Color Style Selection
**Goal:** Monet style selection

**Tasks:**
1. Create color style controller
2. Implement style switching
3. Add style descriptions
4. Test style changes

**Success Criteria:**
- Style selection works
- Styles apply correctly
- Descriptions are clear

### Phase 5: UI Polish & Testing
**Goal:** Production-ready

**Tasks:**
1. Add icons and strings
2. Improve color preview
3. Add help text
4. Test on various devices
5. Performance optimization

**Success Criteria:**
- UI is polished
- All features work reliably
- Performance is acceptable
- No memory leaks

---

## 8. Safety Considerations

### 8.1 Safe Implementation Approach

**Why This is Safe:**
1. **No Root Required:** Uses Android's built-in overlay system
2. **Reversible:** All changes can be reset
3. **Isolated:** Only affects Material You colors, not system stability
4. **Validated:** Uses framework APIs, not hacks
5. **Persistent:** Settings stored in Settings.Secure (backed up)

### 8.2 Error Handling

**Safety Measures:**
- Validate color values before applying
- Handle overlay installation failures gracefully
- Provide fallback to default colors
- Log errors for debugging
- Don't crash if overlay unavailable

### 8.3 Compatibility

**Device Variations:**
- Different Android versions
- Different OEM implementations
- Different overlay support

**Handling:**
- Check overlay availability
- Graceful degradation
- Device-specific testing
- User feedback collection

---

## 9. Resource Requirements

### 9.1 New Files

**Java:**
- `MaterialYouColorManager.java` (~600 lines)
- `MaterialYouColorSettings.java` (~100 lines)
- `MaterialYouOverlayHelper.java` (~200 lines)
- `AccentSaturationController.java` (~80 lines)
- `BackgroundSaturationController.java` (~80 lines)
- `BackgroundLightnessController.java` (~80 lines)
- `PitchBlackController.java` (~60 lines)
- `CustomColorController.java` (~100 lines)
- `ColorStyleController.java` (~80 lines)
- `ColorUtils.java` (~150 lines)

**XML:**
- `res/xml/material_you_color_settings.xml`
- `overlay/MaterialYouOverlay/AndroidManifest.xml`
- `overlay/MaterialYouOverlay/Android.bp`
- `overlay/MaterialYouOverlay/res/values/colors.xml`

**Resources:**
- `res/values/strings.xml` (new strings)
- `res/values/arrays.xml` (style arrays)
- `res/drawable/ic_palette.xml`
- `res/drawable/ic_brightness.xml`

### 9.2 Modified Files

- `res/xml/display_settings.xml` or `top_level_settings.xml` (add entry)
- `SettingsGateway.java` (register fragment)

---

## 10. Testing Plan

### 10.1 Functional Testing

- [ ] Accent saturation works
- [ ] Background saturation works
- [ ] Background lightness works
- [ ] Pitch black applies correctly
- [ ] Custom colors work
- [ ] Color style selection works
- [ ] Reset function works
- [ ] Settings persist across reboots
- [ ] Overlay enables/disables correctly

### 10.2 Compatibility Testing

- [ ] Works on various Android versions
- [ ] Works with different wallpapers
- [ ] Works in light/dark mode
- [ ] Works with existing theme overlays
- [ ] Handles missing overlay gracefully

### 10.3 Performance Testing

- [ ] Color calculations are fast
- [ ] Overlay updates don't lag
- [ ] No memory leaks
- [ ] Battery usage acceptable

---

## 11. Future Enhancements

1. **Per-App Colors:** Different colors per app
2. **Color Presets:** Save/load color combinations
3. **Advanced Color Picker:** HSL/RGB pickers
4. **Color History:** Undo/redo changes
5. **Scheduled Themes:** Auto-switch colors
6. **Wallpaper Sync:** Auto-adjust when wallpaper changes
7. **Color Extraction:** Extract colors from images
8. **Accessibility:** High contrast modes

---

## 12. Conclusion

This plan provides a comprehensive roadmap for implementing ColorBlendr-like Material You color customization directly in the Settings app. The implementation:

1. **No Root Required:** Uses Android's built-in overlay system
2. **Safe & Reversible:** All changes can be reset
3. **Feature-Rich:** Includes saturation, lightness, pitch black, custom colors
4. **User-Friendly:** Integrated into Settings UI
5. **Maintainable:** Clean architecture with singleton pattern
6. **Extensible:** Easy to add more features later

The architecture follows Android Settings best practices and integrates seamlessly with the existing Settings infrastructure, providing users with powerful Material You customization without requiring root access.

