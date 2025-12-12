# Monet Color Features Implementation Guide

## Overview
This guide explains how the Monet color features are implemented and what needs to be done in SystemUI for them to actually work like the wallpaper picker.

## How Wallpaper Picker Implements Monet Colors

### 1. Color Extraction Process
```java
// In WallpaperPicker2.kt (framework)
WallpaperColors.fromBitmap(wallpaperBitmap) → WallpaperColors object
  ├── Primary Color (most prominent)
  ├── Secondary Color (second most prominent)
  ├── Tertiary Color (third most prominent)
  └── Neutral variants
```

### 2. Color Application Process
```java
// SystemUI applies colors through ColorScheme
ColorScheme colorScheme = ColorScheme.create(primary, secondary, tertiary, style);
  ├── TONAL_SPOT (default)
  ├── VIBRANT
  ├── EXPRESSIVE
  ├── SPRITZ
  ├── RAINBOW
  └── FRUIT_SALAD
```

### 3. Theme Application
```java
// Colors applied via ThemeOverlayManager
ThemeOverlayManager.applyColorScheme(colorScheme);
  ├── Updates accent1, accent2, accent3 palettes
  ├── Updates neutral1, neutral2 palettes
  ├── Applies to system and app themes
  └── Triggers UI refresh
```

## Current Settings Implementation

### 1. MonetColorSettings Fragment
- **Location**: `packages/apps/Settings/src/com/android/settings/display/MonetColorSettings.java`
- **Features**: Displays current wallpaper colors, style selection
- **Current Status**: UI works, saves preferences, sends broadcasts

### 2. Color Style Selection
- **Settings Key**: `Settings.Secure.MONET_COLOR_STYLE`
- **Values**: `tonal_spot`, `vibrant`, `expressive`, `spritz`, `rainbow`, `fruit_salad`
- **Current Implementation**: Saves preference, sends broadcast
- **SystemUI Required**: Update `ColorScheme` based on style

### 3. Color Presets
- **Settings Keys**:
  - `Settings.Secure.MONET_COLOR_PRESET` (preset ID)
  - `Settings.Secure.MONET_COLOR_PRESET_VALUES` (color values)
  - `Settings.Secure.MONET_COLOR_OVERRIDE_ENABLED` (1/0)
- **Current Implementation**: Saves colors, sends broadcast
- **SystemUI Required**: Override wallpaper colors with preset values

### 4. Custom Gradients
- **Settings Keys**:
  - `Settings.Secure.MONET_COLOR_GRADIENT` (gradient ID)
  - `Settings.Secure.MONET_GRADIENT_COLORS` (color array)
  - `Settings.Secure.MONET_GRADIENT_ENABLED` (1/0)
- **Current Implementation**: Saves gradient data, sends broadcast
- **SystemUI Required**: Apply gradient as color source

### 5. Time-Based Colors
- **Settings Keys**:
  - `Settings.Secure.MONET_TIME_BASED_ENABLED` (1/0)
  - `Settings.Secure.MONET_CURRENT_TIME_SLOT` (current slot)
  - `Settings.Secure.MONET_TIME_SLOT_COLORS` (slot colors)
- **Current Implementation**: Schedules alarms, applies colors
- **SystemUI Required**: Accept time-based color overrides

### 6. Contextual Colors
- **Settings Keys**:
  - `Settings.Secure.MONET_CONTEXTUAL_ENABLED` (1/0)
  - `Settings.Secure.MONET_LAST_NOTIFICATION_PACKAGE` (package name)
  - `Settings.Secure.MONET_CONTEXTUAL_COLORS` (color values)
- **Current Implementation**: Monitors notifications, applies colors
- **SystemUI Required**: Accept contextual color overrides

## SystemUI Implementation Required

### 1. MonetColorController (New Class)
```java
// frameworks/base/packages/SystemUI/src/com/android/systemui/monet/MonetColorController.java
public class MonetColorController {

    // Listen for Settings changes
    private SettingsObserver mSettingsObserver;

    // Apply color overrides
    public void applyColorOverride(ColorScheme colorScheme) {
        // Update ColorScheme
        // Apply via ThemeOverlayManager
        // Refresh UI components
    }
}
```

### 2. Settings Observer
```java
private class SettingsObserver extends ContentObserver {
    MonetColorController(SettingsObserver.this) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        String key = uri.getLastPathSegment();

        switch (key) {
            case "monet_color_style":
                applyStyleChange();
                break;
            case "monet_color_preset":
                applyPresetColors();
                break;
            case "monet_color_gradient":
                applyGradientColors();
                break;
            // ... other cases
        }
    }
}
```

### 3. Broadcast Receiver
```java
private BroadcastReceiver mMonetReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("com.android.settings.MONET_COLORS_CHANGED".equals(action)) {
            refreshMonetColors();
        }
        // ... other actions
    }
};
```

### 4. Color Application Methods
```java
private void applyStyleChange() {
    String style = Settings.Secure.getString(mContext.getContentResolver(),
        Settings.Secure.MONET_COLOR_STYLE);

    // Update ColorScheme style
    ColorScheme newScheme = mCurrentScheme.withStyle(style);
    applyColorScheme(newScheme);
}

private void applyPresetColors() {
    String colorValues = Settings.Secure.getString(mContext.getContentResolver(),
        Settings.Secure.MONET_COLOR_PRESET_VALUES);

    // Parse and apply preset colors
    ColorScheme presetScheme = parseColorValues(colorValues);
    applyColorScheme(presetScheme);
}

private void applyGradientColors() {
    String gradientValues = Settings.Secure.getString(mContext.getContentResolver(),
        Settings.Secure.MONET_GRADIENT_COLORS);

    // Apply gradient as color source
    applyGradientScheme(gradientValues);
}
```

### 5. Integration Points
- **StatusBar**: Update status bar colors
- **QS Panel**: Update Quick Settings colors
- **Notification Panel**: Update notification colors
- **Settings App**: Update Settings theme colors

## Files to Modify in SystemUI

### Core Files:
1. `frameworks/base/packages/SystemUI/src/com/android/systemui/monet/ColorScheme.java`
2. `frameworks/base/packages/SystemUI/src/com/android/systemui/monet/MonetColorController.java` (new)
3. `frameworks/base/packages/SystemUI/src/com/android/systemui/theme/ThemeOverlayManager.java`

### UI Component Files:
4. `frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/phone/StatusBar.java`
5. `frameworks/base/packages/SystemUI/src/com/android/systemui/qs/QSPanel.java`
6. `frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/NotificationColorPicker.java`

### Settings Files:
7. `frameworks/base/core/java/android/provider/Settings.java` (add keys)

## Settings Keys to Add

```java
// In Settings.Secure
public static final String MONET_COLOR_STYLE = "monet_color_style";
public static final String MONET_COLOR_PRESET = "monet_color_preset";
public static final String MONET_COLOR_PRESET_VALUES = "monet_color_preset_values";
public static final String MONET_COLOR_OVERRIDE_ENABLED = "monet_color_override_enabled";
public static final String MONET_COLOR_GRADIENT = "monet_color_gradient";
public static final String MONET_GRADIENT_COLORS = "monet_gradient_colors";
public static final String MONET_GRADIENT_ENABLED = "monet_gradient_enabled";
public static final String MONET_TIME_BASED_ENABLED = "monet_time_based_enabled";
public static final String MONET_CURRENT_TIME_SLOT = "monet_current_time_slot";
public static final String MONET_TIME_SLOT_COLORS = "monet_time_slot_colors";
public static final String MONET_CONTEXTUAL_ENABLED = "monet_contextual_enabled";
public static final String MONET_LAST_NOTIFICATION_PACKAGE = "monet_last_notification_package";
public static final String MONET_CONTEXTUAL_COLORS = "monet_contextual_colors";
public static final String MONET_SETTINGS_BACKGROUND_COLOR_MODE = "monet_settings_background_color_mode";
public static final String MONET_SETTINGS_BACKGROUND_COLOR_VALUE = "monet_settings_background_color_value";
```

## Testing the Implementation

### Manual Testing:
1. Change color style in Settings → should update system theme
2. Apply color preset → should override wallpaper colors
3. Enable gradients → should apply gradient colors
4. Test time-based colors → should change at scheduled times
5. Test contextual colors → should change based on notifications

### Automated Testing:
1. Unit tests for MonetColorController
2. Integration tests for Settings broadcasts
3. UI tests for color application
4. Performance tests for color transitions

## Backward Compatibility

- **Fallback**: If SystemUI doesn't support new features, use wallpaper colors
- **Version Check**: Check if monet overrides are supported
- **Graceful Degradation**: Fall back to default behavior if needed

## Performance Considerations

- **Caching**: Cache color schemes to avoid recalculation
- **Background Processing**: Process color changes on background threads
- **UI Updates**: Batch UI updates to avoid jank
- **Memory Management**: Clean up unused color resources

## Future Enhancements

1. **Animation**: Smooth transitions between color schemes
2. **Per-App Colors**: Different colors for different apps
3. **User Custom Colors**: Full color picker for custom schemes
4. **Wallpaper Integration**: Auto-update colors when wallpaper changes
5. **Accessibility**: High contrast and color blind friendly options

## Implementation Checklist

### Settings App (Completed):
- ✅ MonetColorSettings fragment
- ✅ Color style selection
- ✅ Color presets (10 dark colors)
- ✅ Custom gradients (10 multi-color)
- ✅ Time-based colors (5 slots)
- ✅ Contextual colors (9+ apps)
- ✅ Settings background control
- ✅ Dynamic color card display
- ✅ String resources separated

### SystemUI (To Implement):
- 🔄 MonetColorController class
- 🔄 Settings observer for changes
- 🔄 Broadcast receiver for updates
- 🔄 Color scheme application methods
- 🔄 Integration with UI components
- 🔄 Settings keys in Settings.java

This implementation follows the same pattern as the wallpaper picker, ensuring consistency and reliability.
