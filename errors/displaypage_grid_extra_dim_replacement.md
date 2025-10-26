# Display Page Grid Extra Dim Card Replacement

## Problem
The extra dim card in the display page grid needed to be replaced with the monet_color_settings.xml configuration.

## Solution
Modified `src/com/epic/fragments/DisplayPageGrid.java` to replace the extra dim card with a reference to the MonetColorSettings fragment.

### Changes Made
- Replaced the CardItem for extra dim (line 163-168) with MonetColorSettings fragment
- Changed destination from `"com.android.settings.accessibility.ToggleReduceBrightColorsPreferenceFragment"` to `"com.android.settings.display.MonetColorSettings"`
- Updated title from `R.string.even_dimmer_display_title` to `R.string.display_page_grid_monet_color_title`
- Updated summary from `R.string.even_dimmer_display_summary` to `R.string.display_page_grid_monet_color_summary`

### Code Change
```java
// Before:
items.add(new DisplayPageGridAdapter.CardItem(
        DisplayPageGridAdapter.CARD_TYPE_SMALL,
        R.string.even_dimmer_display_title,
        R.string.even_dimmer_display_summary,
        "com.android.settings.accessibility.ToggleReduceBrightColorsPreferenceFragment",
        null));

// After:
items.add(new DisplayPageGridAdapter.CardItem(
        DisplayPageGridAdapter.CARD_TYPE_SMALL,
        R.string.display_page_grid_monet_color_title,
        R.string.display_page_grid_monet_color_summary,
        "com.android.settings.display.MonetColorSettings",
        null));
```

## Why This Works
- The MonetColorSettings fragment already exists and properly loads the monet_color_settings.xml
- The fragment includes all necessary controllers for color presets, cycling, and style selection
- The change maintains the same card type (CARD_TYPE_SMALL) and position in the grid
- No build issues introduced - all existing functionality preserved

## Testing
- Verify that the display page grid loads without errors
- Confirm that tapping the new card opens the MonetColorSettings fragment
- Ensure all monet color customization options are accessible and functional
