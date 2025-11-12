# Change: AdaptiveRestrictedSwitchPreference to Normal RestrictedSwitchPreference

## Problem
The extended security settings page was using `com.android.settings.preferences.ui.AdaptiveRestrictedSwitchPreference` which appears to be a custom or device-specific preference component. This could cause compatibility issues when transferring the settings page to other ROMs or devices that don't have this specific component.

## Solution
Changed the preference component from `com.android.settings.preferences.ui.AdaptiveRestrictedSwitchPreference` to the standard `com.android.settingslib.RestrictedSwitchPreference` to ensure better compatibility and portability across different Android ROMs.

## Files Modified
1. `res/xml/anatolia_settings_extended_security.xml` - Line 79: Changed preference class
2. `src/com/epic/fragments/SettingsExtendedSecurity.java` - Updated documentation comments

## Technical Details
- **Old**: `<com.android.settings.preferences.ui.AdaptiveRestrictedSwitchPreference`
- **New**: `<com.android.settingslib.RestrictedSwitchPreference`

## Impact
- The fingerprint ripple effect toggle will now use the standard Android Settings restricted switch preference
- Improved portability to other ROMs
- No functional changes to the user experience
- Maintains all existing security restrictions and behavior

## Verification
- No linter errors introduced
- XML syntax is valid
- Component maintains the same key, title, summary, and default value
- Java code handles the preference generically, so no changes needed there

## Risk Assessment
- **Low Risk**: This is a straightforward preference class change that maintains all attributes and functionality
- **Compatibility**: Standard RestrictedSwitchPreference is available in all Android Settings implementations
- **Testing**: Should be tested on device to ensure the preference appears and functions correctly
