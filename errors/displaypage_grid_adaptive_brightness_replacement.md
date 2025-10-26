# Display Page Grid Adaptive Brightness Card Replacement

## Problem
The adaptive brightness card in the display page grid needed to be replaced with the display_customizations3.xml configuration.

## Solution
Modified `src/com/epic/fragments/DisplayPageGrid.java` to replace the adaptive brightness card with a reference to the DisplayCustomizations3 fragment.

### Changes Made
- Replaced the CardItem for adaptive brightness (line 156-161) with DisplayCustomizations3 fragment
- Changed destination from `"com.android.settings.display.AutoBrightnessSettings"` to `"com.android.settings.awaken.fragments.DisplayCustomizations3"`
- Updated title from `R.string.auto_brightness_title` to `R.string.display_customizations3_title`
- Updated summary from `R.string.auto_brightness_description` to `R.string.display_customizations_summary`

### Code Change
```java
// Before:
items.add(new DisplayPageGridAdapter.CardItem(
        DisplayPageGridAdapter.CARD_TYPE_SMALL,
        R.string.auto_brightness_title,
        R.string.auto_brightness_description,
        "com.android.settings.display.AutoBrightnessSettings",
        null));

// After:
items.add(new DisplayPageGridAdapter.CardItem(
        DisplayPageGridAdapter.CARD_TYPE_SMALL,
        R.string.display_customizations3_title,
        R.string.display_customizations_summary,
        "com.android.settings.awaken.fragments.DisplayCustomizations3",
        null));
```

## Why This Works
- The DisplayCustomizations3 fragment already exists and properly loads the display_customizations3.xml
- The fragment includes all necessary controllers for display effects, color temperature, and animation scales
- The change maintains the same card type (CARD_TYPE_SMALL) and position in the grid
- No build issues introduced - all existing functionality preserved

## Testing
- Verify that the display page grid loads without errors
- Confirm that tapping the new card opens the DisplayCustomizations3 fragment
- Ensure all display customization options are accessible and functional
