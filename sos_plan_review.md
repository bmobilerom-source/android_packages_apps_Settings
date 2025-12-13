# SOS Emergency Signal Plan Review

## Plan Summary

### What the Feature Does
The SOS Emergency Signal feature enables offline emergency signaling using Bluetooth Low Energy (BLE). When activated, the device:
1. **Broadcasts SOS Signal**: Continuously advertises an emergency signal via BLE with a pseudonymized identifier
2. **Plays Audible Siren**: Generates SOS pattern (3 short, 3 long, 3 short beeps) continuously
3. **Shows Persistent Notification**: Displays ongoing notification indicating SOS is broadcasting
4. **Works Completely Offline**: No internet required - uses only Bluetooth Low Energy
5. **Privacy-Focused**: Uses pseudonymized UUID identifiers, no personal data transmitted

### Where It Launches From
**Launch Point**: Anatolia Settings Page (`res/xml/anatolia.xml`)
- Entry added as a PreferenceScreen item
- Title: "Emergency SOS"
- Summary: "Offline emergency signaling"
- Fragment: `com.android.settings.sos.SOSSettingsFragment`

**Navigation Path**:
```
Settings App → Anatolia Settings → Emergency SOS → SOS Settings Screen
```

### User Flow
1. User opens Settings app
2. Navigates to "Anatolia Settings" (custom settings page)
3. Taps "Emergency SOS" preference
4. Opens SOS Settings screen with:
   - Toggle switch: "Send SOS Signal"
   - Status indicator: Shows current broadcast state
   - Information section: Explains how SOS works
5. User toggles "Send SOS Signal" ON
6. System checks Bluetooth availability
7. **If Bluetooth is disabled, automatically enables it** (no user action needed)
8. System waits for Bluetooth to be enabled (if it was disabled)
9. System checks BLE permissions
10. If approved, starts foreground service
11. Service begins BLE advertising and siren playback
12. Persistent notification appears
13. User can toggle OFF to stop broadcasting

## Architecture Components

### 1. SOSService.java (ForegroundService)
- **Purpose**: Core service handling BLE advertising and siren
- **Location**: `src/com/android/settings/sos/SOSService.java`
- **Responsibilities**:
  - BLE advertising via `BluetoothLeAdvertiser`
  - Siren generation via `ToneGenerator`
  - Pseudonymized identifier management
  - Foreground service lifecycle
  - Notification management

### 2. SOSSettingsFragment.java (UI Fragment)
- **Purpose**: Settings screen UI
- **Location**: `src/com/android/settings/sos/SOSSettingsFragment.java`
- **Responsibilities**:
  - Display preferences
  - Handle user interactions
  - Request permissions
  - Update status display

### 3. SOSBroadcastController.java (Controller)
- **Purpose**: Preference controller managing service lifecycle
- **Location**: `src/com/android/settings/sos/SOSBroadcastController.java`
- **Responsibilities**:
  - Control service start/stop
  - Check Bluetooth availability
  - **Automatically enable Bluetooth if disabled** (using BluetoothAdapter.enable())
  - Wait for Bluetooth to be enabled (via BroadcastReceiver)
  - Handle permission requests
  - Update UI state

## Resource Organization (Portable Design)

### Separate Resource Files for Portability
All resources are isolated in dedicated files to enable easy porting:

1. **Strings**: `res/values/sos_strings.xml`
   - All UI text, titles, summaries
   - Notification strings
   - Error messages

2. **Dimens**: `res/values/sos_dimens.xml` (to be created)
   - Layout dimensions
   - Spacing values
   - Size constants

3. **Colors**: `res/values/sos_colors.xml` (to be created)
   - Theme colors
   - Status colors (active/inactive)
   - Notification colors

4. **Drawables**: `res/drawable/` (to be created)
   - `ic_sos.xml` - SOS icon
   - `ic_sos_notification.xml` - Notification icon
   - Any other visual assets

5. **Layouts**: `res/layout/` (if needed)
   - Custom layouts for SOS UI components

### Why Separate Resources?
- **Portability**: Easy to extract and move to other ROMs
- **Maintainability**: Clear separation of concerns
- **No Conflicts**: Avoids conflicts with existing Settings resources
- **Clean Organization**: Follows Android best practices

## Framework Changes (Minimal)

### Required Framework Modification
**File**: `frameworks/base/core/java/android/provider/Settings.java`

**Changes**: Add 4 Settings.Secure keys:
- `SOS_BROADCAST_ENABLED` - Toggle state
- `SOS_IDENTIFIER` - Pseudonymized UUID
- `SOS_DISASTER_DETECTION_ENABLED` - Future feature flag
- `SOS_RECOVERY_MODE_ENABLED` - Future feature flag

**Why Minimal**: Only adds settings keys, no system service changes, no SystemUI modifications

## Functional Requirements

### Core Functionality (Must Work)
1. ✅ **BLE Advertising**: Actually broadcasts SOS signal via Bluetooth
2. ✅ **Siren Playback**: Audible SOS pattern plays continuously
3. ✅ **Service Persistence**: Service survives app backgrounding
4. ✅ **Bluetooth Integration**: Checks and handles Bluetooth state
5. ✅ **Permission Handling**: Requests and handles BLE permissions
6. ✅ **Status Updates**: UI reflects actual broadcast state

### Not Just Visual
- Service actually performs BLE advertising
- Siren actually plays audio
- Notification actually shows service is running
- Settings actually control real functionality

## Integration Points

### Settings App Integration
- **Entry Point**: Anatolia Settings page
- **Service Declaration**: AndroidManifest.xml
- **Preference Screen**: `res/xml/sos_settings.xml`
- **String Resources**: `res/values/sos_strings.xml`

### Framework Integration
- **Settings Keys**: `Settings.Secure` constants
- **No System Services**: Uses existing Android BLE APIs
- **No SystemUI Changes**: Completely self-contained in Settings app

## Testing Location
- **Primary Testing**: Anatolia Settings page
- **Reason**: Custom settings area for testing new features
- **Production**: Can be moved to main Settings later if desired

## Implementation Status
- **Plan Created**: ✅ Complete
- **Framework Changes**: ⏳ Pending
- **Settings Implementation**: ⏳ Pending
- **Testing**: ⏳ Pending

## Key Design Principles Followed

1. **Portability**: All resources isolated, easy to extract
2. **Minimal Framework Changes**: Only Settings keys added
3. **Functional**: Actually works, not just visual
4. **Privacy-First**: Pseudonymized identifiers, no personal data
5. **Offline**: No internet required
6. **Clean Architecture**: Follows Android and LineageOS patterns
7. **Testable**: Launched from Anatolia for easy testing

