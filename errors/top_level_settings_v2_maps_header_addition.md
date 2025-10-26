# Top Level Settings V2 Maps Header Addition

## Problem
The top_level_settings_v2.xml file needed a Maps header added to the top for branding consistency.

## Solution
Created a new maps_header.xml layout file and added it to the top of the V2 settings file.

### Changes Made
1. **Created New Layout**: `res/layout/maps_header.xml`
   - Based on the existing header layout pattern (similar to tati_header.xml)
   - Displays "Maps" text with proper styling
   - Unique IDs: `maps_banner_container` and `maps_banner_card`

2. **Updated V2 Settings**: `res/xml/top_level_settings_v2.xml`
   - Added LayoutPreference referencing the maps_header layout
   - Positioned at the very top with order="-2000"
   - Non-selectable visual branding element

### Code Changes
```xml
<!-- Added to top_level_settings_v2.xml -->
<com.android.settingslib.widget.LayoutPreference
    android:key="maps_header"
    android:selectable="false"
    android:layout="@layout/maps_header"
    android:order="-2000" />
```

## Why This Works
- Follows the same pattern as other header additions (tati_header in classic settings)
- Provides Maps branding at the top of the V2 settings interface
- Order -2000 ensures it appears before all other preferences
- Maintains existing functionality while adding visual branding
- Consistent with the existing header layout structure

## Testing
- Verify that the V2 settings page loads with the Maps header at the top
- Confirm that "Maps" text displays correctly with proper styling
- Ensure the header doesn't interfere with existing preference functionality
- Test on different screen sizes to verify header displays properly
