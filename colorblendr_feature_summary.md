# ColorBlendr-Style Color Customization Feature - Plan Summary

## Feature Overview

### What It Does
The ColorBlendr-style Color Customization feature provides **fine-tuning controls** for Material You colors, allowing users to adjust saturation, lightness, and enable pitch black theme. When activated:

1. **Accent Saturation**: Adjusts how vibrant/saturated accent colors are (0-200%)
2. **Background Saturation**: Adjusts saturation of background/surface colors (0-200%)
3. **Background Lightness**: Adjusts brightness of background colors (0-200%)
4. **Pitch Black Theme**: Uses pure black (#000000) instead of dark gray in dark mode
5. **Manual Color Override**: Override primary color manually with color picker

### Key Characteristics
- **Fine-Tuning**: Precise control over color properties
- **Real-Time Preview**: See changes immediately
- **System Integration**: Modifies Material You colors system-wide
- **Persistent**: Settings persist across reboots
- **No Root Required**: Works at system level (we're building ROM)
- **Functional**: Actually modifies colors (not just visual)

## Launch Location

### Navigation Path
```
Settings App 
  → Anatolia Settings → Color Customization
```

### Entry Point Details
- **File**: `res/xml/anatolia.xml`
- **Preference Key**: `colorblendr_category`
- **Title**: "Color Customization"
- **Summary**: "Fine-tune Material You colors"
- **Fragment**: `com.android.settings.display.MonetColorBlendrSettingsFragment`

### Why Separate Entry in Anatolia Settings?
- ✅ **Separate for testing** - Easy to test independently
- ✅ **No root required** - Works at system level (we're building ROM)
- ✅ **Can move later** - Can integrate into Monet Colors later if desired
- ✅ **Follows pattern** - Matches other testing features in Anatolia Settings

## User Experience Flow

### Using Color Customization

1. **Access**: Settings → Display → Monet Colors → Color Customization
2. **View**: Sees Color Customization screen with:
   - Accent Saturation slider (0-200%, default: 100%)
   - Background Saturation slider (0-200%, default: 100%)
   - Background Lightness slider (0-200%, default: 100%)
   - Pitch Black toggle (OFF by default)
   - Manual Color Override button
3. **Adjust Saturation**: Drag Accent Saturation slider:
   - Value changes (0-200%)
   - Accent colors become more/less vibrant
   - Changes visible immediately in UI
4. **Adjust Background**: Drag Background Saturation slider:
   - Value changes (0-200%)
   - Background colors become more/less saturated
   - Changes visible immediately
5. **Adjust Brightness**: Drag Background Lightness slider:
   - Value changes (0-200%)
   - Backgrounds get brighter (higher) or darker (lower)
   - Changes visible immediately
6. **Enable Pitch Black**: Toggle Pitch Black ON:
   - Dark mode switches to pure black (#000000)
   - All dark backgrounds become black
   - Changes visible immediately
7. **Manual Override**: Tap Manual Color Override:
   - Color picker dialog opens
   - User selects custom color
   - Primary color changes to selected color
   - System applies immediately

## Technical Architecture

### Components

1. **MonetColorBlendrSettingsFragment.java** (UI Fragment)
   - Displays saturation/lightness sliders
   - Shows pitch black toggle
   - Provides manual color picker
   - Updates UI in real-time

2. **ColorBlendrController.java** (Preference Controller)
   - Manages saturation/lightness values
   - Reads/writes Settings.Secure keys
   - Applies color modifications
   - Updates UI state

3. **ColorBlendrHelper.java** (Helper)
   - Applies saturation to colors (HSV conversion)
   - Applies lightness to colors (HSV conversion)
   - Converts to pitch black
   - Generates modified ColorScheme

### Resource Organization (Portable Design)

All resources are isolated in separate files for easy porting:

- **Strings**: `res/values/colorblendr_strings.xml`
- **Dimens**: `res/values/colorblendr_dimens.xml`
- **Colors**: `res/values/colorblendr_colors.xml`
- **Drawables**: `res/drawable/colorblendr_*.xml`
- **Layouts**: `res/xml/monet_color_blendr_settings.xml`

### Framework Integration (Required - Single Phase)

**Complete single-phase implementation** with framework integration from the start:

**Add 7 Settings.Secure keys to `frameworks/base/core/java/android/provider/Settings.java`:**
- `MONET_ACCENT_SATURATION` - Accent saturation (0-200%)
- `MONET_BACKGROUND_SATURATION` - Background saturation (0-200%)
- `MONET_BACKGROUND_LIGHTNESS` - Background lightness (0-200%)
- `MONET_PITCH_BLACK` - Pitch black enabled (0/1)
- `MONET_MANUAL_COLOR_OVERRIDE` - Manual color override (ARGB)
- `MONET_COLOR_MODIFICATIONS_ENABLED` - Modifications enabled (0/1)
- `MONET_COLOR_SCHEME_MODIFIED` - Modified color scheme (JSON)

**SystemUI Enhancement**: Enhance `MonetColorController` to read and apply modifications

**No Other Framework Changes**:
- No SystemUI service creation
- Uses existing ColorScheme API
- Uses existing ThemeOverlayManager
- Integrates with existing Monet system

## Functional Requirements (Must Work)

### Core Functionality
✅ **Accent Saturation**: Actually adjusts saturation of accent colors
✅ **Background Saturation**: Actually adjusts saturation of background colors
✅ **Background Lightness**: Actually adjusts brightness of background colors
✅ **Pitch Black**: Actually uses pure black (#000000) in dark mode
✅ **Manual Override**: Actually overrides primary color
✅ **Real-Time Preview**: Changes visible immediately
✅ **Persistent**: Settings persist across reboots

### Not Just Visual
- Saturation actually modifies color HSV values
- Lightness actually modifies color brightness
- Pitch black actually uses #000000
- Manual override actually changes primary color
- SystemUI applies modifications system-wide

## Implementation Status

- ✅ **Plan Created**: Complete with all details
- ⏳ **Framework Changes**: Pending (7 Settings.Secure keys + SystemUI enhancement)
- ⏳ **Settings Implementation**: Pending (Fragment, Controller, Helper)
- ⏳ **Resources**: Pending (strings, dimens, colors, drawables, layouts)
- ⏳ **Testing**: Pending

## Files Created/Modified

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
- `res/xml/monet_color_settings.xml` - Add ColorBlendr entry (or `anatolia.xml`)

## Design Principles Followed

1. **Portability**: All resources isolated, easy to extract
2. **Minimal Framework Changes**: Only Settings keys + SystemUI enhancement
3. **Functional**: Actually modifies colors, not just visual
4. **Integration**: Works with existing Monet color features
5. **Clean Architecture**: Follows Android and LineageOS patterns
6. **Testable**: Launched from Monet Colors or Anatolia Settings
7. **User-Friendly**: Real-time preview, intuitive controls

## Key Implementation Details

### Saturation Adjustment
- Converts colors to HSV color space
- Adjusts saturation channel (S)
- Range: 0-200% (100% = no change)
- Formula: `newSaturation = originalSaturation * (saturationPercent / 100)`

### Lightness Adjustment
- Converts colors to HSV color space
- Adjusts value/brightness channel (V)
- Range: 0-200% (100% = no change)
- Formula: `newValue = originalValue * (lightnessPercent / 100)`

### Pitch Black Theme
- Checks if dark mode is enabled
- Replaces all dark backgrounds with #000000
- Applies to system and app themes
- Works with existing dark mode toggle

### Manual Color Override
- Uses color picker dialog
- Overrides primary color in ColorScheme
- Applies immediately
- Can be reset to wallpaper colors

## Integration with Existing Features

### Works With:
- **Color Style Selection**: Saturation/lightness applied after style
- **Color Presets**: Can fine-tune preset colors
- **Custom Gradients**: Can adjust gradient saturation/lightness
- **Time-Based Colors**: Can fine-tune time-based colors
- **Contextual Colors**: Can adjust contextual color saturation

### Value Ranges:
- **0%**: Completely desaturated (grayscale) or completely dark
- **100%**: Default (no change)
- **200%**: Maximum saturation or maximum brightness

## Limitations & Considerations

### Limitations
1. **SystemUI Required**: Requires SystemUI modifications to apply changes
2. **Color Accuracy**: HSV conversion may have slight color shifts
3. **Performance**: Real-time updates may impact performance on low-end devices
4. **Compatibility**: Works with Android 12+ (Material You)

### Considerations
1. **User Education**: Users need to understand saturation/lightness concepts
2. **Default Values**: 100% = no change (intuitive)
3. **Reset Option**: Provide way to reset to defaults
4. **Preview**: Real-time preview helps users understand changes

## Future Enhancements

- **Per-App Customization**: Different saturation/lightness per app
- **Color History**: Remember recently used manual colors
- **Preset Combinations**: Save saturation/lightness combinations
- **Advanced Controls**: RGB/HSL sliders for precise control
- **Animation**: Smooth transitions when changing values
- **Export/Import**: Share color customization settings

