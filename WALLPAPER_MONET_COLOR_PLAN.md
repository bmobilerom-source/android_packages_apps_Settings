# Wallpaper Monet Color Integration - Analysis & Enhancement Plan

## Current Implementation Analysis

### 1. How Wallpaper Colors Work

#### Wallpaper Color Extraction Flow:
1. **WallpaperPicker2** (`WallpaperColorsExtractor.kt`):
   - Extracts colors from wallpaper bitmap using `WallpaperColors.fromBitmap()`
   - Processes bitmap through SRGB color space
   - Generates `WallpaperColors` object with primary/secondary/tertiary colors

2. **System Integration** (`frameworks/base`):
   - `WallpaperManager` provides `WallpaperColors` API
   - System extracts colors and generates Monet palette via `com.android.systemui.monet`
   - Creates `TonalPalette` objects for different color shades
   - Applies colors system-wide through theme overlays

3. **Settings App Theming**:
   - Uses `ThemeHelper.trySetDynamicColor()` from SetupDesign library
   - `SettingsThemeHelper.isExpressiveTheme()` checks if Material You is enabled
   - Applies dynamic colors to Settings activities automatically
   - Background colors come from wallpaper's extracted Monet palette

### 2. Current Monet Color Card Implementation

**Location:** `packages/apps/Settings/res/layout/display_page_grid_card_monet_color.xml`

**Features:**
- Shows 4 color swatches (circle1-4 drawables)
- Displays title and summary
- Launches `com.android.settings.display.MonetColorSettings` (currently missing)
- Color swatches are static drawables, not dynamically colored

**Current Limitations:**
- Color swatches don't reflect actual wallpaper colors
- No way to manually select/override Monet colors
- No preview of different color combinations
- Missing `MonetColorSettings` fragment implementation

### 3. System Monet Color Generation

**Key Components:**
- `Settings.Secure.DYNAMIC_COLOR_THEME_ENABLED` - Controls if Material You is enabled
- `com.android.systemui.monet.ColorScheme` - Generates color schemes from wallpaper
- `TonalPalette` - Contains color shades (50-900) for each hue
- Theme overlays apply colors to system and apps

**Color Extraction Process:**
1. Wallpaper bitmap → `WallpaperColors.fromBitmap()`
2. Extract primary/secondary/tertiary colors
3. Generate tonal palettes (accent, neutral, etc.)
4. Create color scheme with multiple styles (TONAL_SPOT, VIBRANT, etc.)
5. Apply via theme overlays

## Enhancement Plan: Adding More Monet Color Options

### Phase 1: Create MonetColorSettings Fragment

**File:** `packages/apps/Settings/src/com/android/settings/display/MonetColorSettings.java`

**Features:**
1. **Display Current Wallpaper Colors**
   - Show extracted primary/secondary/tertiary colors
   - Display color swatches from current wallpaper
   - Show which style is active (TONAL_SPOT, VIBRANT, etc.)

2. **Color Style Selection**
   - TONAL_SPOT (default)
   - VIBRANT
   - EXPRESSIVE
   - SPRITZ
   - RAINBOW
   - FRUIT_SALAD
   - Custom style selection

3. **Manual Color Override**
   - Allow user to pick custom accent color
   - Override wallpaper-extracted colors
   - Save preference to `Settings.Secure`

### Phase 2: Enhance Monet Color Card Display

**File:** `packages/apps/Settings/res/layout/display_page_grid_card_monet_color.xml`

**Changes:**
1. **Dynamic Color Swatches**
   - Replace static drawables with programmatically colored views
   - Use `WallpaperColors` to get actual colors
   - Display primary, secondary, tertiary, neutral colors

2. **Color Preview**
   - Show preview of how colors look in Settings app
   - Display color palette with multiple shades
   - Add "Change Style" button

**Implementation in Adapter:**
```java
// In DisplayPageGridAdapter.java
if (item.cardType == CARD_TYPE_MONET_COLOR) {
    WallpaperManager wm = WallpaperManager.getInstance(context);
    WallpaperColors colors = wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
    
    if (colors != null) {
        // Set actual colors to swatches
        int primary = colors.getPrimaryColor().toArgb();
        int secondary = colors.getSecondaryColor() != null ? 
            colors.getSecondaryColor().toArgb() : primary;
        // ... apply to views
    }
}
```

### Phase 3: Add Custom Color Presets

**New File:** `packages/apps/Settings/src/com/android/settings/display/MonetColorPresets.java`

**Features:**
1. **Predefined Color Combinations**
   - Blue Ocean (blue tones)
   - Sunset (orange/red tones)
   - Forest (green tones)
   - Purple Dream (purple tones)
   - Midnight (dark blue/black)
   - Rose Gold (pink/gold)
   - Ocean Breeze (cyan/teal)
   - Fire (red/orange/yellow)

2. **Preset Storage**
   - Store as `Settings.Secure` keys
   - Format: `monet_color_preset_<name>`
   - Include primary, secondary, tertiary colors

3. **Preset Application**
   - Override wallpaper colors temporarily
   - Apply preset colors to system theme
   - Allow "Reset to Wallpaper" option

### Phase 4: Settings Background Color Control

**New File:** `packages/apps/Settings/src/com/android/settings/display/SettingsBackgroundColorController.java`

**Features:**
1. **Background Color Options**
   - Use wallpaper Monet colors (default)
   - Use custom solid color
   - Use gradient (2-3 colors)
   - Use preset combinations

2. **Implementation Method**
   - Create custom theme overlay for Settings app
   - Override `android:colorBackground` and `android:colorSurface`
   - Apply via `ThemeOverlayManager` or custom theme

3. **Storage**
   - `Settings.Secure.SETTINGS_BACKGROUND_COLOR_MODE` (wallpaper/custom/preset)
   - `Settings.Secure.SETTINGS_BACKGROUND_COLOR_VALUE` (custom color)
   - `Settings.Secure.SETTINGS_BACKGROUND_COLOR_PRESET` (preset name)

### Phase 5: UI Implementation

**New XML Files:**
1. `monet_color_settings.xml` - Main settings screen
2. `monet_color_style_picker.xml` - Style selection
3. `monet_color_preset_grid.xml` - Preset grid
4. `monet_color_custom_picker.xml` - Custom color picker

**Layout Structure:**
```
MonetColorSettings
├── Current Colors Section
│   ├── Wallpaper preview
│   ├── Color swatches (primary, secondary, tertiary)
│   └── Current style indicator
├── Style Selection
│   ├── Radio group with style options
│   └── Style preview cards
├── Preset Colors
│   ├── Grid of preset color cards
│   └── "Apply Preset" button
└── Custom Colors
    ├── Color picker for primary
    ├── Color picker for secondary
    └── "Apply Custom" button
```

### Phase 6: Integration Points

**Files to Modify:**

1. **DisplayPageGridAdapter.java**
   - Update color swatches with actual wallpaper colors
   - Add click handler to launch MonetColorSettings

2. **SettingsActivity.java**
   - Apply custom background color theme if set
   - Override `onCreate()` to check for custom colors

3. **SettingsBaseActivity.java**
   - Apply background color to base activity
   - Handle theme overlay application

4. **top_level_settings.xml**
   - Keep existing wallpaper preference
   - Add optional "Color Theme" preference below wallpaper

### Phase 7: System Integration

**Framework Changes Needed:**

1. **Color Override API**
   - Extend `WallpaperManager` or create `ColorThemeManager`
   - Allow apps to override Monet colors temporarily
   - Store overrides in `Settings.Secure`

2. **Theme Application**
   - Use `ThemeOverlayManager` to apply colors
   - Create overlay package for Settings app
   - Apply colors at runtime without reboot

**Settings Keys:**
```java
// In Settings.Secure
public static final String MONET_COLOR_STYLE = "monet_color_style";
public static final String MONET_COLOR_PRIMARY = "monet_color_primary";
public static final String MONET_COLOR_SECONDARY = "monet_color_secondary";
public static final String MONET_COLOR_TERTIARY = "monet_color_tertiary";
public static final String SETTINGS_BACKGROUND_COLOR_MODE = "settings_background_color_mode";
public static final String SETTINGS_BACKGROUND_COLOR_VALUE = "settings_background_color_value";
```

## Implementation Steps

### Step 1: Create MonetColorSettings Fragment
- [ ] Create `MonetColorSettings.java`
- [ ] Create `monet_color_settings.xml` layout
- [ ] Implement wallpaper color reading
- [ ] Display current colors

### Step 2: Add Color Style Selection
- [ ] Create style picker UI
- [ ] Implement style switching
- [ ] Save style preference
- [ ] Apply style to system

### Step 3: Implement Preset Colors
- [ ] Define color preset data structure
- [ ] Create preset grid layout
- [ ] Implement preset application
- [ ] Add preset storage

### Step 4: Enhance Color Card Display
- [ ] Update `DisplayPageGridAdapter` to use real colors
- [ ] Replace static drawables with dynamic views
- [ ] Add click handler to launch settings

### Step 5: Settings Background Control
- [ ] Create background color controller
- [ ] Implement theme overlay application
- [ ] Add background color picker UI
- [ ] Test background color changes

### Step 6: Testing & Polish
- [ ] Test with different wallpapers
- [ ] Test preset color application
- [ ] Test custom color override
- [ ] Test Settings background changes
- [ ] Add string resources
- [ ] Add icons/drawables

## Technical Considerations

### Color Extraction
- Use `WallpaperManager.getWallpaperColors(FLAG_SYSTEM)`
- Extract primary, secondary, tertiary colors
- Handle null colors gracefully
- Cache colors to avoid repeated extraction

### Theme Application
- Use `ThemeOverlayManager` for system-wide colors
- Create Settings-specific theme overlay for background
- Apply colors without requiring app restart
- Handle theme changes at runtime

### Performance
- Cache wallpaper colors
- Lazy load color swatches
- Use background threads for color extraction
- Optimize color picker UI

### Compatibility
- Check if Material You is enabled
- Fallback to default colors if Monet unavailable
- Handle devices without dynamic color support
- Maintain backward compatibility

## Resources Needed

### New Files:
1. `MonetColorSettings.java` - Main fragment
2. `MonetColorPresets.java` - Preset definitions
3. `SettingsBackgroundColorController.java` - Background controller
4. `monet_color_settings.xml` - Main layout
5. `monet_color_style_picker.xml` - Style picker
6. `monet_color_preset_grid.xml` - Preset grid
7. `monet_color_custom_picker.xml` - Custom picker

### Modified Files:
1. `DisplayPageGridAdapter.java` - Dynamic colors
2. `SettingsActivity.java` - Background theme
3. `SettingsBaseActivity.java` - Base theme
4. `top_level_settings.xml` - Optional preference

### String Resources:
- Monet color settings titles/descriptions
- Style names (Tonal Spot, Vibrant, etc.)
- Preset names (Blue Ocean, Sunset, etc.)
- Button labels (Apply, Reset, etc.)

## Future Enhancements

1. **Live Preview**: Show real-time preview of color changes
2. **Color History**: Remember recently used colors
3. **Custom Gradients**: Allow multi-color gradients
4. **Per-App Colors**: Different colors for different apps
5. **Time-Based Colors**: Colors change based on time of day
6. **Wallpaper Sync**: Auto-update colors when wallpaper changes

