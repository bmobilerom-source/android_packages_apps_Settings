# ColorBlendr-Style Material You Customization - Implementation Plan

## Executive Summary

This plan outlines how to integrate **ColorBlendr-style Material You color customization** features into LineageOS Settings, enhancing the existing Monet color system with saturation, lightness, and pitch black theme controls. These features allow fine-tuning of Material You colors beyond the basic style selection.

**Key Features:**
- **Accent Saturation Control**: Adjust saturation of accent colors (0-200%)
- **Background Saturation Control**: Adjust saturation of background colors (0-200%)
- **Background Lightness Control**: Adjust lightness of background colors (0-200%)
- **Pitch Black Theme**: Pure black backgrounds in dark mode
- **Manual Color Override**: Override individual color values manually

**Important Notes:**
- ✅ **No Root Required**: Works at system level (we're building ROM - no root needed)
- ✅ **Launch Separately**: Launches from Anatolia Settings for testing
- ✅ **Single-Phase Implementation**: Complete implementation in one go with framework integration
- ✅ **Full Functionality**: Colors actually change system-wide from the start

---

## 1. Feature Overview

### What ColorBlendr Does

**Core Functionality:**
1. **Accent Saturation**: Adjusts how vibrant/saturated accent colors are
2. **Background Saturation**: Adjusts saturation of background/surface colors
3. **Background Lightness**: Adjusts brightness of background colors
4. **Pitch Black**: Uses pure black (#000000) instead of dark gray in dark mode
5. **Manual Override**: Manually set individual color values

**Key Characteristics:**
- **Fine-Tuning**: Precise control over color properties
- **Real-Time Preview**: See changes immediately (after SystemUI integration)
- **System Integration**: Modifies Material You colors system-wide
- **Persistent**: Settings persist across reboots
- **No Root Required**: Works at system level (we're building ROM - no root needed)
- **Framework Integration**: Requires SystemUI changes for colors to actually change

### Integration with Existing Monet Features

**Current Monet Features:**
- Color style selection (TONAL_SPOT, VIBRANT, etc.)
- Color presets (10 dark colors)
- Custom gradients (10 multi-color gradients)
- Time-based colors (5 time slots)
- Contextual colors (notification-based)

**New ColorBlendr Features to Add:**
- Accent saturation slider (0-200%)
- Background saturation slider (0-200%)
- Background lightness slider (0-200%)
- Pitch black toggle
- Manual color override picker

---

## 2. Architecture Design

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│ Settings App                                                │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ MonetColorBlendrSettingsFragment (UI)                 │ │
│ │ - Saturation sliders                                  │ │
│ │ - Lightness slider                                   │ │
│ │ - Pitch black toggle                                 │ │
│ │ - Manual color picker                                │ │
│ └───────────────────────────────────────────────────────┘ │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ ColorBlendrController (Preference Controller)        │ │
│ │ - Manages saturation/lightness values                │ │
│ │ - Applies color modifications                        │ │
│ │ - Updates UI state                                   │ │
│ └───────────────────────────────────────────────────────┘ │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ ColorBlendrHelper (Helper)                            │ │
│ │ - Applies saturation to colors                        │ │
│ │ - Applies lightness to colors                        │ │
│ │ - Converts to pitch black                            │ │
│ │ - Generates modified ColorScheme                     │ │
│ └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ SystemUI (Framework)                                        │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ MonetColorController (Enhanced)                        │ │
│ │ - Reads saturation/lightness values                    │ │
│ │ - Applies modifications to ColorScheme                │ │
│ │ - Updates theme overlays                              │ │
│ └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Component Breakdown

#### A. MonetColorBlendrSettingsFragment (UI Fragment)

**Purpose**: Settings UI for ColorBlendr-style customization

**Responsibilities:**
- Display saturation sliders
- Display lightness slider
- Display pitch black toggle
- Display manual color override picker
- Show real-time preview

**Location**: `src/com/android/settings/display/MonetColorBlendrSettingsFragment.java`

**UI Components:**
- SeekBarPreference: Accent saturation (0-200%)
- SeekBarPreference: Background saturation (0-200%)
- SeekBarPreference: Background lightness (0-200%)
- SwitchPreferenceCompat: Pitch black theme
- Preference: Manual color override (opens color picker)

**Key Methods:**
```java
public class MonetColorBlendrSettingsFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    
    private static final String KEY_ACCENT_SATURATION = "accent_saturation";
    private static final String KEY_BACKGROUND_SATURATION = "background_saturation";
    private static final String KEY_BACKGROUND_LIGHTNESS = "background_lightness";
    private static final String KEY_PITCH_BLACK = "pitch_black";
    private static final String KEY_MANUAL_COLOR = "manual_color_override";
    
    private SeekBarPreference mAccentSaturationPref;
    private SeekBarPreference mBackgroundSaturationPref;
    private SeekBarPreference mBackgroundLightnessPref;
    private SwitchPreferenceCompat mPitchBlackPref;
    private Preference mManualColorPref;
    private ColorBlendrController mController;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.monet_color_blendr_settings);
        
        mController = new ColorBlendrController(getContext());
        setupPreferences();
        updatePreferenceStates();
    }
    
    private void setupPreferences() {
        mAccentSaturationPref = findPreference(KEY_ACCENT_SATURATION);
        mBackgroundSaturationPref = findPreference(KEY_BACKGROUND_SATURATION);
        mBackgroundLightnessPref = findPreference(KEY_BACKGROUND_LIGHTNESS);
        mPitchBlackPref = findPreference(KEY_PITCH_BLACK);
        mManualColorPref = findPreference(KEY_MANUAL_COLOR);
        
        mAccentSaturationPref.setOnPreferenceChangeListener(this);
        mBackgroundSaturationPref.setOnPreferenceChangeListener(this);
        mBackgroundLightnessPref.setOnPreferenceChangeListener(this);
        mPitchBlackPref.setOnPreferenceChangeListener(this);
        mManualColorPref.setOnPreferenceClickListener(preference -> {
            showColorPickerDialog();
            return true;
        });
    }
    
    private void updatePreferenceStates() {
        int accentSaturation = mController.getAccentSaturation();
        int backgroundSaturation = mController.getBackgroundSaturation();
        int backgroundLightness = mController.getBackgroundLightness();
        boolean pitchBlack = mController.isPitchBlackEnabled();
        
        mAccentSaturationPref.setProgress(accentSaturation);
        mBackgroundSaturationPref.setProgress(backgroundSaturation);
        mBackgroundLightnessPref.setProgress(backgroundLightness);
        mPitchBlackPref.setChecked(pitchBlack);
        
        updateSummaries();
    }
    
    private void updateSummaries() {
        mAccentSaturationPref.setSummary(
            getString(R.string.colorblendr_accent_saturation_summary, 
                mAccentSaturationPref.getProgress()));
        mBackgroundSaturationPref.setSummary(
            getString(R.string.colorblendr_background_saturation_summary,
                mBackgroundSaturationPref.getProgress()));
        mBackgroundLightnessPref.setSummary(
            getString(R.string.colorblendr_background_lightness_summary,
                mBackgroundLightnessPref.getProgress()));
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAccentSaturationPref) {
            int saturation = (Integer) newValue;
            mController.setAccentSaturation(saturation);
            applyColorModifications();
            updateSummaries();
            return true;
        } else if (preference == mBackgroundSaturationPref) {
            int saturation = (Integer) newValue;
            mController.setBackgroundSaturation(saturation);
            applyColorModifications();
            updateSummaries();
            return true;
        } else if (preference == mBackgroundLightnessPref) {
            int lightness = (Integer) newValue;
            mController.setBackgroundLightness(lightness);
            applyColorModifications();
            updateSummaries();
            return true;
        } else if (preference == mPitchBlackPref) {
            boolean enabled = (Boolean) newValue;
            mController.setPitchBlackEnabled(enabled);
            applyColorModifications();
            return true;
        }
        return false;
    }
    
    private void applyColorModifications() {
        mController.applyColorModifications();
        notifyColorChange();
    }
    
    private void notifyColorChange() {
        Intent intent = new Intent("com.android.settings.MONET_COLORS_CHANGED");
        intent.setPackage("com.android.systemui");
        getContext().sendBroadcast(intent);
    }
    
    private void showColorPickerDialog() {
        // Show color picker dialog for manual color override
        ColorPickerDialog dialog = new ColorPickerDialog(getContext(),
            mController.getManualColorOverride(),
            new ColorPickerDialog.OnColorSelectedListener() {
                @Override
                public void onColorSelected(int color) {
                    mController.setManualColorOverride(color);
                    applyColorModifications();
                }
            });
        dialog.show();
    }
}
```

#### B. ColorBlendrController (Preference Controller)

**Purpose**: Manages ColorBlendr settings and applies modifications

**Responsibilities:**
- Read/write saturation/lightness values
- Apply color modifications
- Generate modified ColorScheme
- Update Settings.Secure keys

**Location**: `src/com/android/settings/display/ColorBlendrController.java`

**Key Methods:**
```java
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
        return Settings.Secure.getInt(mContext.getContentResolver(),
            Settings.Secure.MONET_ACCENT_SATURATION, DEFAULT_SATURATION);
    }
    
    public void setAccentSaturation(int saturation) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            Settings.Secure.MONET_ACCENT_SATURATION, saturation);
    }
    
    public int getBackgroundSaturation() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            Settings.Secure.MONET_BACKGROUND_SATURATION, DEFAULT_SATURATION);
    }
    
    public void setBackgroundSaturation(int saturation) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            Settings.Secure.MONET_BACKGROUND_SATURATION, saturation);
    }
    
    public int getBackgroundLightness() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            Settings.Secure.MONET_BACKGROUND_LIGHTNESS, DEFAULT_LIGHTNESS);
    }
    
    public void setBackgroundLightness(int lightness) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            Settings.Secure.MONET_BACKGROUND_LIGHTNESS, lightness);
    }
    
    public boolean isPitchBlackEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            Settings.Secure.MONET_PITCH_BLACK, 0) == 1;
    }
    
    public void setPitchBlackEnabled(boolean enabled) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            Settings.Secure.MONET_PITCH_BLACK, enabled ? 1 : 0);
    }
    
    public int getManualColorOverride() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            Settings.Secure.MONET_MANUAL_COLOR_OVERRIDE, 0);
    }
    
    public void setManualColorOverride(int color) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            Settings.Secure.MONET_MANUAL_COLOR_OVERRIDE, color);
    }
    
    public void applyColorModifications() {
        mHelper.applyModifications(
            getAccentSaturation(),
            getBackgroundSaturation(),
            getBackgroundLightness(),
            isPitchBlackEnabled(),
            getManualColorOverride());
    }
}
```

#### C. ColorBlendrHelper (Helper Class)

**Purpose**: Applies saturation, lightness, and pitch black modifications to colors

**Responsibilities:**
- Apply saturation to accent colors
- Apply saturation to background colors
- Apply lightness to background colors
- Convert to pitch black
- Generate modified ColorScheme

**Location**: `src/com/android/settings/display/ColorBlendrHelper.java`

**Key Methods:**
```java
public class ColorBlendrHelper {
    private Context mContext;
    private WallpaperManager mWallpaperManager;
    
    public ColorBlendrHelper(Context context) {
        mContext = context;
        mWallpaperManager = WallpaperManager.getInstance(context);
    }
    
    public void applyModifications(int accentSaturation, int backgroundSaturation,
            int backgroundLightness, boolean pitchBlack, int manualColor) {
        
        // Get current wallpaper colors
        WallpaperColors wallpaperColors = mWallpaperManager.getWallpaperColors(
            WallpaperManager.FLAG_SYSTEM);
        
        if (wallpaperColors == null) {
            return;
        }
        
        // Extract base colors
        int primaryColor = wallpaperColors.getPrimaryColor().toArgb();
        int secondaryColor = wallpaperColors.getSecondaryColor() != null ?
            wallpaperColors.getSecondaryColor().toArgb() : primaryColor;
        int tertiaryColor = wallpaperColors.getTertiaryColor() != null ?
            wallpaperColors.getTertiaryColor().toArgb() : primaryColor;
        
        // Apply manual color override if set
        if (manualColor != 0) {
            primaryColor = manualColor;
        }
        
        // Apply accent saturation
        primaryColor = applySaturation(primaryColor, accentSaturation);
        secondaryColor = applySaturation(secondaryColor, accentSaturation);
        tertiaryColor = applySaturation(tertiaryColor, accentSaturation);
        
        // Generate ColorScheme with modifications
        ColorScheme colorScheme = generateModifiedColorScheme(
            primaryColor, secondaryColor, tertiaryColor,
            backgroundSaturation, backgroundLightness, pitchBlack);
        
        // Apply via Settings (SystemUI will read and apply)
        saveColorScheme(colorScheme);
    }
    
    private int applySaturation(int color, int saturationPercent) {
        if (saturationPercent == 100) {
            return color; // No change
        }
        
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        
        // Adjust saturation (0-200% range, 100% = no change)
        float saturationMultiplier = saturationPercent / 100f;
        hsv[1] = Math.max(0f, Math.min(1f, hsv[1] * saturationMultiplier));
        
        return Color.HSVToColor(hsv);
    }
    
    private int applyLightness(int color, int lightnessPercent) {
        if (lightnessPercent == 100) {
            return color; // No change
        }
        
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        
        // Adjust lightness/value (0-200% range, 100% = no change)
        float lightnessMultiplier = lightnessPercent / 100f;
        hsv[2] = Math.max(0f, Math.min(1f, hsv[2] * lightnessMultiplier));
        
        return Color.HSVToColor(hsv);
    }
    
    private ColorScheme generateModifiedColorScheme(int primary, int secondary, int tertiary,
            int backgroundSaturation, int backgroundLightness, boolean pitchBlack) {
        
        // Generate tonal palettes
        TonalPalette accentPalette = TonalPalette.fromInt(primary);
        TonalPalette secondaryPalette = TonalPalette.fromInt(secondary);
        TonalPalette tertiaryPalette = TonalPalette.fromInt(tertiary);
        
        // Generate neutral palettes with modifications
        TonalPalette neutralPalette = generateNeutralPalette(
            backgroundSaturation, backgroundLightness, pitchBlack);
        
        // Create ColorScheme
        ColorScheme colorScheme = new ColorScheme.Builder()
            .setAccentPalette(accentPalette)
            .setSecondaryPalette(secondaryPalette)
            .setTertiaryPalette(tertiaryPalette)
            .setNeutralPalette(neutralPalette)
            .build();
        
        return colorScheme;
    }
    
    private TonalPalette generateNeutralPalette(int saturation, int lightness, boolean pitchBlack) {
        if (pitchBlack) {
            // Generate pure black palette
            return TonalPalette.fromInt(0xFF000000);
        }
        
        // Generate neutral palette with saturation/lightness modifications
        int baseNeutral = 0xFF808080; // Gray base
        int modifiedNeutral = applySaturation(baseNeutral, saturation);
        modifiedNeutral = applyLightness(modifiedNeutral, lightness);
        
        return TonalPalette.fromInt(modifiedNeutral);
    }
    
    private void saveColorScheme(ColorScheme colorScheme) {
        // Save color scheme data to Settings.Secure
        // SystemUI will read and apply
        
        String colorSchemeJson = serializeColorScheme(colorScheme);
        Settings.Secure.putString(mContext.getContentResolver(),
            Settings.Secure.MONET_COLOR_SCHEME_MODIFIED, colorSchemeJson);
        
        // Mark as modified
        Settings.Secure.putInt(mContext.getContentResolver(),
            Settings.Secure.MONET_COLOR_MODIFICATIONS_ENABLED, 1);
    }
    
    private String serializeColorScheme(ColorScheme colorScheme) {
        // Serialize ColorScheme to JSON for storage
        // Implementation depends on ColorScheme structure
        return ""; // Placeholder
    }
}
```

---

## 3. Resource Organization (Portable Design)

### Separate Resource Files

**Strings**: `res/values/colorblendr_strings.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Titles -->
    <string name="colorblendr_settings_title">Color Customization</string>
    <string name="colorblendr_settings_summary">Fine-tune Material You colors</string>
    
    <!-- Preferences -->
    <string name="colorblendr_accent_saturation_title">Accent Saturation</string>
    <string name="colorblendr_accent_saturation_summary">%d%%</string>
    <string name="colorblendr_background_saturation_title">Background Saturation</string>
    <string name="colorblendr_background_saturation_summary">%d%%</string>
    <string name="colorblendr_background_lightness_title">Background Lightness</string>
    <string name="colorblendr_background_lightness_summary">%d%%</string>
    <string name="colorblendr_pitch_black_title">Pitch Black Theme</string>
    <string name="colorblendr_pitch_black_summary">Use pure black in dark mode</string>
    <string name="colorblendr_manual_color_title">Manual Color Override</string>
    <string name="colorblendr_manual_color_summary">Override primary color manually</string>
    
    <!-- Info -->
    <string name="colorblendr_info">Adjust saturation and lightness to fine-tune Material You colors</string>
</resources>
```

**Dimens**: `res/values/colorblendr_dimens.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <dimen name="colorblendr_slider_margin">16dp</dimen>
    <dimen name="colorblendr_preview_size">48dp</dimen>
</resources>
```

**Colors**: `res/values/colorblendr_colors.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="colorblendr_preview_background">@android:color/system_accent1_600</color>
</resources>
```

**Layout**: `res/xml/monet_color_blendr_settings.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:title="@string/colorblendr_settings_title">
    
    <PreferenceCategory
        android:key="colorblendr_saturation_category"
        android:title="@string/colorblendr_saturation_title">
        
        <SeekBarPreference
            android:key="accent_saturation"
            android:title="@string/colorblendr_accent_saturation_title"
            android:summary="@string/colorblendr_accent_saturation_summary"
            android:icon="@drawable/ic_palette"
            android:max="200"
            android:defaultValue="100"
            app:showSeekBarValue="true" />
        
        <SeekBarPreference
            android:key="background_saturation"
            android:title="@string/colorblendr_background_saturation_title"
            android:summary="@string/colorblendr_background_saturation_summary"
            android:icon="@drawable/ic_background"
            android:max="200"
            android:defaultValue="100"
            app:showSeekBarValue="true" />
    </PreferenceCategory>
    
    <PreferenceCategory
        android:key="colorblendr_lightness_category"
        android:title="@string/colorblendr_lightness_title">
        
        <SeekBarPreference
            android:key="background_lightness"
            android:title="@string/colorblendr_background_lightness_title"
            android:summary="@string/colorblendr_background_lightness_summary"
            android:icon="@drawable/ic_brightness"
            android:max="200"
            android:defaultValue="100"
            app:showSeekBarValue="true" />
    </PreferenceCategory>
    
    <PreferenceCategory
        android:key="colorblendr_theme_category"
        android:title="@string/colorblendr_theme_title">
        
        <SwitchPreferenceCompat
            android:key="pitch_black"
            android:title="@string/colorblendr_pitch_black_title"
            android:summary="@string/colorblendr_pitch_black_summary"
            android:icon="@drawable/ic_dark_mode" />
    </PreferenceCategory>
    
    <PreferenceCategory
        android:key="colorblendr_manual_category"
        android:title="@string/colorblendr_manual_title">
        
        <Preference
            android:key="manual_color_override"
            android:title="@string/colorblendr_manual_color_title"
            android:summary="@string/colorblendr_manual_color_summary"
            android:icon="@drawable/ic_color_picker" />
    </PreferenceCategory>
    
    <PreferenceCategory
        android:key="colorblendr_info_category"
        android:title="@string/colorblendr_info_title">
        <Preference
            android:key="colorblendr_info"
            android:title="@string/colorblendr_info"
            android:selectable="false" />
    </PreferenceCategory>
</PreferenceScreen>
```

---

## 4. Framework Integration (Required - Single Phase)

### Framework Integration Required

**This is a complete single-phase implementation** that includes framework integration from the start:

**Settings.Secure Keys** - Required for storing values:

**File**: `frameworks/base/core/java/android/provider/Settings.java`

**Add to Settings.Secure:**
```java
/**
 * Monet Color Customization - Accent saturation (0-200%, 100% = default)
 * @hide
 */
public static final String MONET_ACCENT_SATURATION = "monet_accent_saturation";

/**
 * Monet Color Customization - Background saturation (0-200%, 100% = default)
 * @hide
 */
public static final String MONET_BACKGROUND_SATURATION = "monet_background_saturation";

/**
 * Monet Color Customization - Background lightness (0-200%, 100% = default)
 * @hide
 */
public static final String MONET_BACKGROUND_LIGHTNESS = "monet_background_lightness";

/**
 * Monet Color Customization - Pitch black theme enabled
 * @hide
 */
public static final String MONET_PITCH_BLACK = "monet_pitch_black";

/**
 * Monet Color Customization - Manual color override (ARGB color value)
 * @hide
 */
public static final String MONET_MANUAL_COLOR_OVERRIDE = "monet_manual_color_override";

/**
 * Monet Color Customization - Color modifications enabled
 * @hide
 */
public static final String MONET_COLOR_MODIFICATIONS_ENABLED = "monet_color_modifications_enabled";

/**
 * Monet Color Customization - Modified color scheme data (JSON)
 * @hide
 */
public static final String MONET_COLOR_SCHEME_MODIFIED = "monet_color_scheme_modified";
```

### SystemUI Integration Required (For Colors to Actually Change)

**File**: `frameworks/base/packages/SystemUI/src/com/android/systemui/monet/MonetColorController.java`

**Enhancement**: Read saturation/lightness values and apply modifications to ColorScheme

**⚠️ IMPORTANT**: Without SystemUI integration, the UI will work but colors won't actually change. The Settings app can save values, but SystemUI needs to read and apply them.

### Complete Implementation (Single Phase)

**This implementation uses Settings.Secure from the start** - no temporary storage needed:

**ColorBlendrController uses Settings.Secure:**
```java
// In ColorBlendrController.java
public int getAccentSaturation() {
    return Settings.Secure.getInt(mContext.getContentResolver(),
        Settings.Secure.MONET_ACCENT_SATURATION, 100);
}

public void setAccentSaturation(int saturation) {
    Settings.Secure.putInt(mContext.getContentResolver(),
        Settings.Secure.MONET_ACCENT_SATURATION, saturation);
}
```

**All components implemented together:**
- ✅ Settings UI (Fragment, Controller, Helper)
- ✅ Framework Settings.Secure keys
- ✅ SystemUI modifications
- ✅ Colors actually change system-wide

```java
// In MonetColorController
private void applyColorModifications(ColorScheme baseScheme) {
    int accentSaturation = Settings.Secure.getInt(mContext.getContentResolver(),
        Settings.Secure.MONET_ACCENT_SATURATION, 100);
    int backgroundSaturation = Settings.Secure.getInt(mContext.getContentResolver(),
        Settings.Secure.MONET_BACKGROUND_SATURATION, 100);
    int backgroundLightness = Settings.Secure.getInt(mContext.getContentResolver(),
        Settings.Secure.MONET_BACKGROUND_LIGHTNESS, 100);
    boolean pitchBlack = Settings.Secure.getInt(mContext.getContentResolver(),
        Settings.Secure.MONET_PITCH_BLACK, 0) == 1;
    
    // Apply modifications to ColorScheme
    ColorScheme modifiedScheme = modifyColorScheme(baseScheme,
        accentSaturation, backgroundSaturation, backgroundLightness, pitchBlack);
    
    return modifiedScheme;
}
```

---

## 5. Integration with Existing Monet Features

### Launch Point

**Add to Anatolia Settings** (`res/xml/anatolia.xml`) - **Separate entry for testing**:

```xml
<!-- Color Customization (ColorBlendr-style) -->
<PreferenceScreen
    android:key="colorblendr_category"
    android:title="@string/colorblendr_settings_title"
    android:summary="@string/colorblendr_settings_summary"
    android:fragment="com.android.settings.display.MonetColorBlendrSettingsFragment" />
```

**Why Separate Entry:**
- Easy to test independently
- Can be moved to Monet Colors later if desired
- Follows testing pattern in codebase

---

## 6. Implementation Checklist

### Single Phase: Complete Implementation
- [ ] **Framework Changes**:
  - [ ] Add Settings.Secure keys to `frameworks/base/core/java/android/provider/Settings.java`
  - [ ] Enhance SystemUI MonetColorController to read and apply modifications
  
- [ ] **Settings App**:
  - [ ] Create `MonetColorBlendrSettingsFragment.java`
  - [ ] Create `ColorBlendrController.java` (using Settings.Secure)
  - [ ] Create `ColorBlendrHelper.java`
  - [ ] Create `colorblendr_settings.xml` layout
  - [ ] Create resource files (strings, dimens, colors, drawables)
  - [ ] Add to Anatolia Settings (separate entry for testing)
  
- [ ] **Testing**:
  - [ ] Test UI functionality
  - [ ] Test actual color changes
  - [ ] Test persistence across reboots

### Phase 2: Color Modifications
- [ ] Create `ColorBlendrHelper.java`
- [ ] Implement saturation adjustment
- [ ] Implement lightness adjustment
- [ ] Implement pitch black conversion
- [ ] Implement manual color override

### Phase 3: SystemUI Integration
- [ ] Add Settings.Secure keys to framework
- [ ] Enhance MonetColorController in SystemUI
- [ ] Apply modifications to ColorScheme
- [ ] Update theme overlays

### Phase 4: Testing & Polish
- [ ] Test saturation adjustments
- [ ] Test lightness adjustments
- [ ] Test pitch black theme
- [ ] Test manual color override
- [ ] Test with existing Monet features

---

## 7. Files to Create/Modify

### Framework (Commit: `colorblendr: Customization`)

**File**: `frameworks/base/core/java/android/provider/Settings.java`
- Add 7 Settings.Secure keys

**File**: `frameworks/base/packages/SystemUI/src/com/android/systemui/monet/MonetColorController.java`
- Enhance to read and apply modifications

### Settings App (Commit: `colorblendr: Customization`)

**Created**:
- `src/com/android/settings/display/MonetColorBlendrSettingsFragment.java`
- `src/com/android/settings/display/ColorBlendrController.java`
- `src/com/android/settings/display/ColorBlendrHelper.java`
- `res/xml/monet_color_blendr_settings.xml`
- `res/values/colorblendr_strings.xml`
- `res/values/colorblendr_dimens.xml`
- `res/values/colorblendr_colors.xml`
- `res/drawable/ic_palette.xml`
- `res/drawable/ic_background.xml`
- `res/drawable/ic_brightness.xml`
- `res/drawable/ic_color_picker.xml`

**Modified**:
- `res/xml/anatolia.xml` - Add ColorBlendr entry (separate for testing)

---

## 8. Functional Requirements (Must Work)

### Core Functionality
✅ **Accent Saturation**: Actually adjusts saturation of accent colors
✅ **Background Saturation**: Actually adjusts saturation of background colors
✅ **Background Lightness**: Actually adjusts brightness of background colors
✅ **Pitch Black**: Actually uses pure black in dark mode
✅ **Manual Override**: Actually overrides primary color
✅ **Real-Time Preview**: Changes visible immediately
✅ **Persistent**: Settings persist across reboots

### Not Just Visual
- Saturation actually modifies color values
- Lightness actually modifies brightness
- Pitch black actually uses #000000
- Manual override actually changes primary color
- SystemUI applies modifications system-wide

---

## 9. User Experience Flow

### Using Color Customization

1. **Access**: Settings → Display → Monet Colors → Color Customization
2. **View**: Sees Color Customization screen with:
   - Accent Saturation slider (0-200%)
   - Background Saturation slider (0-200%)
   - Background Lightness slider (0-200%)
   - Pitch Black toggle
   - Manual Color Override button
3. **Adjust Saturation**: Drag Accent Saturation slider:
   - Value changes (0-200%)
   - Colors update in real-time
   - System applies changes immediately
4. **Adjust Lightness**: Drag Background Lightness slider:
   - Value changes (0-200%)
   - Backgrounds get brighter/darker
   - Changes visible immediately
5. **Enable Pitch Black**: Toggle Pitch Black ON:
   - Dark mode uses pure black (#000000)
   - Changes visible immediately
6. **Manual Override**: Tap Manual Color Override:
   - Color picker opens
   - Select custom color
   - Primary color changes to selected color
   - System applies immediately

---

## 10. Conclusion

This plan provides a comprehensive roadmap for integrating ColorBlendr-style Material You color customization into LineageOS Settings. The implementation will:

1. **Fine-Tune Colors**: Precise control over saturation and lightness
2. **Pitch Black Theme**: Pure black backgrounds in dark mode
3. **Manual Override**: Override primary color manually
4. **Real-Time Preview**: See changes immediately
5. **System Integration**: Works with existing Monet color features

The architecture uses:
- **Settings.Secure Keys** for storing values
- **ColorBlendrHelper** for applying modifications
- **SystemUI Integration** for system-wide application
- **Minimal Framework Changes** (Settings keys + SystemUI enhancement)

The feature will integrate with existing Monet color features and can be launched from MonetColorSettings or Anatolia Settings.

