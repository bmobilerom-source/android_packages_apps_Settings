# Display Page Grid Display Rotation Card Replacement

## Problem
The display rotation card in the display page grid needed to be replaced with the QS Header settings from display_customizations5.xml (lines 37-45).

## Solution
Modified `src/com/epic/fragments/DisplayPageGrid.java` to replace the display rotation card with a reference to the QsHeader fragment.

### Changes Made
- Replaced the CardItem for display rotation (line 177-182) with QsHeader fragment
- Changed destination from `"org.lineageos.lineageparts.hardware.DisplayRotation"` to `"com.android.settings.awaken.fragments.QsHeader"`
- Updated title from `R.string.display_rotation_title` to `R.string.qs_header_title`
- Updated summary from `R.string.display_rotation_summary` to `R.string.qs_header_summary`

### Code Change
```java
// Before:
items.add(new DisplayPageGridAdapter.CardItem(
        DisplayPageGridAdapter.CARD_TYPE_SMALL,
        R.string.display_rotation_title,
        R.string.display_rotation_summary,
        "org.lineageos.lineageparts.hardware.DisplayRotation",
        null));

// After:
items.add(new DisplayPageGridAdapter.CardItem(
        DisplayPageGridAdapter.CARD_TYPE_SMALL,
        R.string.qs_header_title,
        R.string.qs_header_summary,
        "com.android.settings.awaken.fragments.QsHeader",
        null));
```

## Why This Works
- The QsHeader fragment already exists and properly handles QS header customization
- The fragment provides options for customizing Quick Settings header image and visibility
- The change maintains the same card type (CARD_TYPE_SMALL) and position in the grid
- No build issues introduced - all existing functionality preserved

## Testing
- Verify that the display page grid loads without errors
- Confirm that tapping the new card opens the QS Header settings fragment
- Ensure all QS header customization options are accessible and functional
