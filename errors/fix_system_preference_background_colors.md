# System Preference Background Colors Fix

## Problem
The background colors for system help and warning preferences were displaying incorrectly on device:
- Help preference background was showing as green (#FF43A047) instead of the appropriate yellow/amber color
- Warning preference background was using a solid color without proper alpha transparency, making it inconsistent with the design system

## Root Cause
The `res/values/colors_system_cards.xml` file contained incorrect color definitions that overrode the proper colors defined in `res/values/at_colors.xml`.

## Solution
Updated the color values in `res/values/colors_system_cards.xml`:

- Changed `system_help_bg` from `#FF43A047` (solid green) to `#f5f501` (strong yellow)
- Changed `system_warning_bg` from `#FFF4511E` (solid orange) to `#99C58181` (light red/pink with 60% alpha)

## Files Modified
- `res/values/colors_system_cards.xml`

## Why This Works
- The new colors provide better visual distinction for system preferences
- Help preferences now use a strong yellow color (#f5f501) for clear informational content indication
- Warning preferences use a subtle red/pink tint for cautionary information
- Help background uses solid color for maximum visibility, warning maintains alpha transparency for subtle appearance

## Testing
- Verify help preference backgrounds appear as strong yellow (#f5f501) instead of green
- Verify warning preference backgrounds appear with appropriate red/pink tint
- Ensure colors work correctly in both light and dark themes
