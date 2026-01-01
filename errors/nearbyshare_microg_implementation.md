# NearbyShare MicroG-Compatible Implementation

## Overview
This implementation uses **native Android APIs only** - no Google Play Services required. Works perfectly with MicroG.

## Key Changes from Google Play Services Version

### ✅ **No Google Dependencies**
- Removed: `com.google.android.gms:play-services-nearby`
- Removed: `com.google.android.gms:play-services-location`
- Uses: Native Android Bluetooth and Wi-Fi Direct APIs

### Implementation Details

#### 1. **Bluetooth File Transfer**
- Uses `BluetoothAdapter` and `BluetoothSocket`
- Standard RFCOMM socket communication
- Works on all Android devices with Bluetooth

#### 2. **Wi-Fi Direct Support** (Optional)
- Uses `WifiP2pManager` for faster transfers
- Falls back to Bluetooth if Wi-Fi Direct unavailable
- No Google services needed

#### 3. **Minimal Code**
- Only essential file transfer functionality
- No complex Google API wrappers
- Simple, maintainable codebase

## Files Created

1. **NearbyShareManager.java** - Native Android API implementation
2. **NearbyShareSettingsFragment.java** - Settings UI
3. **NearbySharePreferenceController.java** - Preference controller
4. **NearbySharePermissionHelper.java** - Permission handling

## Integration

### Added to Connected Devices:
- Preference entry in `connected_devices.xml`
- Opens `NearbyShareSettingsFragment` when clicked
- Shows "Nearby Share" with summary

### Permissions Required:
- Bluetooth (standard Android permissions)
- Wi-Fi Direct (if available)
- Location (for Bluetooth discovery on Android 6+)
- Storage (for file access)

## Usage

1. User opens Connected Devices
2. Taps "Nearby Share"
3. Selects "Send Files" or "Receive Files"
4. Discovers nearby devices via Bluetooth
5. Transfers files using native Android APIs

## Benefits for MicroG Users

- ✅ **No Google Play Services** - Pure AOSP
- ✅ **Works on LineageOS** - No GMS dependency
- ✅ **Privacy-focused** - No Google tracking
- ✅ **Lightweight** - Minimal code footprint
- ✅ **Maintainable** - Standard Android APIs

## Limitations

- Slower than Google's Nearby Connections (uses Bluetooth instead of optimized protocol)
- Requires manual device pairing (no automatic discovery like Google's version)
- File size limited by Bluetooth transfer speed

## Future Enhancements

- Add Wi-Fi Direct for faster transfers
- Implement automatic device discovery
- Add transfer history
- Support for multiple file transfers

