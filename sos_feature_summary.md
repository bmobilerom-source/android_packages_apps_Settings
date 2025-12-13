# SOS Emergency Signal Feature - Plan Summary

## Feature Overview

### What It Does
The SOS Emergency Signal feature provides **offline emergency signaling** using Bluetooth Low Energy (BLE). When activated:

1. **Broadcasts Emergency Signal**: Continuously advertises an SOS signal via BLE with a pseudonymized UUID identifier
2. **Plays Audible Siren**: Generates the standard SOS pattern (3 short beeps, 3 long beeps, 3 short beeps) continuously
3. **Shows Persistent Notification**: Displays an ongoing notification indicating SOS is actively broadcasting
4. **Works Completely Offline**: No internet connection required - uses only Bluetooth Low Energy
5. **Privacy-Focused**: Uses pseudonymized UUID identifiers, no personal data transmitted

### Key Characteristics
- **Offline Operation**: Works without internet, GPS, or cellular network
- **BLE Range**: 10-30 meters (typical BLE range)
- **Privacy**: Pseudonymized identifiers, regeneratable for privacy
- **Functional**: Actually broadcasts signals and plays siren (not just visual)
- **Persistent**: Service survives app backgrounding and device sleep

## Launch Location

### Navigation Path
```
Settings App 
  → Anatolia Settings (custom settings page)
    → Emergency SOS (preference entry)
      → SOS Settings Screen (main feature UI)
```

### Entry Point Details
- **File**: `res/xml/anatolia.xml`
- **Preference Key**: `sos_category`
- **Title**: "Emergency SOS"
- **Summary**: "Offline emergency signaling"
- **Fragment**: `com.android.settings.sos.SOSSettingsFragment`

### Why Anatolia Settings?
- Custom settings area for testing new features
- Easy to enable/disable during development
- Can be moved to main Settings later if desired
- Follows existing pattern in codebase

## User Experience Flow

1. **Access**: User opens Settings → Anatolia Settings → Emergency SOS
2. **View**: Sees SOS Settings screen with toggle and status
3. **Activate**: Toggles "Send SOS Signal" switch ON
4. **System Checks**: 
   - Bluetooth availability
   - **Auto-enable Bluetooth if disabled** (automatic)
   - BLE permissions
   - Service requirements
5. **Activation**: If approved, service starts:
   - BLE advertising begins
   - Siren starts playing
   - Notification appears
   - Status updates to "Broadcasting"
6. **Deactivation**: User toggles switch OFF:
   - BLE advertising stops
   - Siren stops
   - Notification dismissed
   - Status updates to "Not broadcasting"

## Technical Architecture

### Components

1. **SOSService.java** (ForegroundService)
   - Handles BLE advertising
   - Manages siren playback
   - Maintains service lifecycle
   - Shows persistent notification

2. **SOSSettingsFragment.java** (UI Fragment)
   - Displays settings screen
   - Handles user interactions
   - Requests permissions
   - Updates status display

3. **SOSBroadcastController.java** (Controller)
   - Manages service lifecycle
   - Checks Bluetooth state
   - Handles permission requests
   - Updates UI state

### Resource Organization (Portable Design)

All resources are isolated in separate files for easy porting:

- **Strings**: `res/values/sos_strings.xml`
- **Dimens**: `res/values/sos_dimens.xml` (to be created)
- **Colors**: `res/values/sos_colors.xml` (to be created)
- **Drawables**: `res/drawable/ic_sos.xml` (to be created)
- **Layouts**: `res/xml/sos_settings.xml`

### Framework Changes (Minimal)

**Only Required Change**: Add 4 Settings.Secure keys to `frameworks/base/core/java/android/provider/Settings.java`:
- `SOS_BROADCAST_ENABLED` - Toggle state
- `SOS_IDENTIFIER` - Pseudonymized UUID
- `SOS_DISASTER_DETECTION_ENABLED` - Future feature flag
- `SOS_RECOVERY_MODE_ENABLED` - Future feature flag

**No Other Framework Changes**:
- No SystemUI modifications
- No system service creation
- Uses existing Android BLE APIs
- Uses ForegroundService pattern

## Functional Requirements (Must Work)

### Core Functionality
✅ **BLE Advertising**: Actually broadcasts SOS signal via Bluetooth Low Energy
✅ **Siren Playback**: Audible SOS pattern plays continuously
✅ **Service Persistence**: Service survives app backgrounding
✅ **Bluetooth Integration**: Checks and handles Bluetooth state
✅ **Auto-Enable Bluetooth**: Automatically enables Bluetooth when SOS is triggered
✅ **Permission Handling**: Requests and handles BLE permissions
✅ **Status Updates**: UI reflects actual broadcast state

### Not Just Visual
- Service performs real BLE advertising operations
- Siren generates actual audio output
- Notification shows service is actually running
- Settings toggle controls real functionality
- Status reflects actual service state

## Implementation Status

- ✅ **Plan Created**: Complete with all details
- ⏳ **Framework Changes**: Pending (Settings.Secure keys)
- ⏳ **Settings Implementation**: Pending (Service, Fragment, Controller)
- ⏳ **Resources**: Pending (strings, dimens, colors, drawables)
- ⏳ **Testing**: Pending

## Files Created/Modified

### Framework (Commit 1: `sos: Broadcast`)
- `frameworks/base/core/java/android/provider/Settings.java` - Add 4 keys

### Settings App (Commit 2: `sos: Broadcast`)
**Created**:
- `src/com/android/settings/sos/SOSService.java`
- `src/com/android/settings/sos/SOSSettingsFragment.java`
- `src/com/android/settings/sos/SOSBroadcastController.java`
- `res/xml/sos_settings.xml`
- `res/values/sos_strings.xml`
- `res/values/sos_dimens.xml` (to be created)
- `res/values/sos_colors.xml` (to be created)
- `res/drawable/ic_sos.xml` (to be created)

**Modified**:
- `res/xml/anatolia.xml` - Add SOS preference entry
- `AndroidManifest.xml` - Add service declaration

## Design Principles Followed

1. **Portability**: All resources isolated, easy to extract
2. **Minimal Framework Changes**: Only Settings keys added
3. **Functional**: Actually works, not just visual
4. **Privacy-First**: Pseudonymized identifiers, no personal data
5. **Offline**: No internet required
6. **Clean Architecture**: Follows Android and LineageOS patterns
7. **Testable**: Launched from Anatolia for easy testing

## Future Enhancements (Foundation Laid)

- **Disaster Detection**: Automatic SOS trigger based on sensor data
- **Recovery Mode**: BLE scanning to find nearby SOS signals
- **Enhanced Features**: Flashlight control, vibration patterns, signal strength visualization

