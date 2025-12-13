# SOS Emergency Signal Integration Plan

## Overview
Integrate offline SOS signaling functionality (inspired by Igatha/flare-gun) into LineageOS Settings app. Uses Bluetooth Low Energy (BLE) for offline emergency communication without internet. Priority: Manual SOS broadcasting. Foundation laid for disaster detection and recovery mode.

## Commit Structure
- **Commit 1 (frameworks/base)**: `sos: Broadcast` - Add Settings keys and system properties
- **Commit 2 (packages/apps/Settings)**: `sos: Broadcast` - Add Settings UI and SOS service implementation

## Features List

### Phase 1: Manual SOS Broadcasting (Priority - MVP)
1. **Manual SOS Signal Broadcasting**
   - Toggle switch in Settings UI
   - BLE advertising with pseudonymized identifier
   - Audible siren generation
   - Persistent foreground service
   - Status indicator in Settings

2. **Bluetooth Integration**
   - Automatic Bluetooth enable check
   - Permission handling (BLUETOOTH_ADVERTISE, BLUETOOTH_CONNECT)
   - BLE advertiser setup and management
   - Low-power advertising mode

3. **Privacy & Security**
   - Pseudonymized UUID-based identifiers
   - No personal data transmission
   - No internet connectivity required
   - Regeneratable identifiers

### Phase 2: Foundation for Future Features
4. **Disaster Detection** (Foundation - Not Implemented)
   - Sensor monitoring infrastructure
   - Accelerometer, gyroscope, barometer support
   - Background monitoring capability

5. **Recovery Mode** (Foundation - Not Implemented)
   - BLE scanning infrastructure
   - Distance estimation (RSSI-based)
   - Nearby SOS signal detection

## Architecture

### Component Structure
```
Settings App (UI Layer)
├── SOSSettingsFragment.java - Main settings UI
├── SOSBroadcastController.java - Controls SOS service
└── SOSService.java - ForegroundService for BLE operations
    ├── BLE Advertiser (broadcast SOS)
    ├── BLE Scanner (recovery mode - future)
    └── Sensor Monitor (disaster detection - future)
```

### Design Decision: ForegroundService Pattern
- **Why**: Cleanest approach - Settings app manages service directly
- **Benefits**: No SystemUI modifications needed, simpler lifecycle, easier debugging
- **Pattern**: Similar to `SimSlotChangeService` - lightweight foreground service

## Implementation Phases

### Phase 1: Core SOS Service & Manual Broadcasting (Priority)

#### 1.1 Create SOSService (ForegroundService)
**File**: `src/com/android/settings/sos/SOSService.java`

**Responsibilities**:
- BLE advertising for SOS signals
- Generate pseudonymized identifier (UUID-based)
- Manage service lifecycle (start/stop)
- Handle Bluetooth permissions
- Generate audible siren (MediaPlayer/ToneGenerator)

**Key Methods**:
```java
public class SOSService extends Service {
    private BluetoothLeAdvertiser mAdvertiser;
    private AdvertiseCallback mAdvertiseCallback;
    private MediaPlayer mSirenPlayer;
    private String mSOSIdentifier; // Pseudonymized UUID
    
    public void startSOSBroadcast();
    public void stopSOSBroadcast();
    private void generateSOSIdentifier(); // UUID-based pseudonym
    private void startSiren(); // Audible alarm
    private void stopSiren();
}
```

**BLE Advertising Setup**:
- Use `BluetoothAdapter.getBluetoothLeAdvertiser()`
- Create `AdvertiseData` with SOS service UUID
- Include pseudonymized identifier in manufacturer data
- Set advertising mode: `ADVERTISE_MODE_LOW_LATENCY`
- Set TX power: `ADVERTISE_TX_POWER_HIGH`

#### 1.2 Create SOSBroadcastController
**File**: `src/com/android/settings/sos/SOSBroadcastController.java`

**Responsibilities**:
- Control SOSService lifecycle
- Check Bluetooth availability
- Request permissions
- Update UI state

**Key Methods**:
```java
public class SOSBroadcastController extends AbstractPreferenceController {
    public void startSOSBroadcast();
    public void stopSOSBroadcast();
    public boolean isSOSActive();
    private void checkBluetoothEnabled();
    private void requestPermissions();
}
```

#### 1.3 Create SOSSettingsFragment
**File**: `src/com/android/settings/sos/SOSSettingsFragment.java`

**UI Components**:
- Main toggle: "Send SOS Signal" (SwitchPreferenceCompat)
- Status indicator: Shows if SOS is broadcasting
- Bluetooth status check
- Permission request handling

**Layout**: `res/xml/sos_settings.xml`

**Preference Structure**:
```xml
<PreferenceScreen>
    <SwitchPreferenceCompat
        android:key="sos_broadcast_toggle"
        android:title="@string/sos_broadcast_title"
        android:summary="@string/sos_broadcast_summary" />
    
    <Preference
        android:key="sos_status"
        android:title="@string/sos_status_title"
        android:summary="@string/sos_status_summary" />
    
    <PreferenceCategory
        android:title="@string/sos_info_category">
        <Preference
            android:key="sos_info"
            android:summary="@string/sos_info_summary" />
    </PreferenceCategory>
</PreferenceScreen>
```

#### 1.4 Add to Anatolia Settings
**File**: `res/xml/anatolia.xml`

Add preference entry:
```xml
<PreferenceScreen
    android:key="sos_category"
    android:title="@string/sos_settings_title"
    android:summary="@string/sos_settings_summary"
    android:fragment="com.android.settings.sos.SOSSettingsFragment" />
```

### Phase 2: AndroidManifest Configuration

#### 2.1 Add Service Declaration
**File**: `AndroidManifest.xml`

Add service entry:
```xml
<service
    android:name="com.android.settings.sos.SOSService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="dataSync"
    android:permission="android.permission.BIND_FOREGROUND_SERVICE" />
```

**Note**: `foregroundServiceType` may need adjustment based on Android version requirements.

#### 2.2 Permissions Already Present
- `BLUETOOTH_ADVERTISE` ✓
- `BLUETOOTH_CONNECT` ✓
- `BLUETOOTH_SCAN` ✓
- `FOREGROUND_SERVICE` ✓
- `VIBRATE` ✓ (for future features)

### Phase 3: String Resources

**File**: `res/values/sos_strings.xml`

Create dedicated strings file:
```xml
<string name="sos_settings_title">Emergency SOS</string>
<string name="sos_settings_summary">Offline emergency signaling</string>
<string name="sos_broadcast_title">Send SOS Signal</string>
<string name="sos_broadcast_summary">Broadcast emergency signal via Bluetooth</string>
<string name="sos_status_title">Status</string>
<string name="sos_status_summary">Not broadcasting</string>
<string name="sos_status_active">Broadcasting SOS signal</string>
<string name="sos_info_category">Information</string>
<string name="sos_info_summary">SOS signals work offline using Bluetooth Low Energy. Range: 10-30 meters.</string>
<string name="sos_bluetooth_required">Bluetooth must be enabled</string>
<string name="sos_permission_required">Bluetooth permissions required</string>
<string name="sos_notification_title">SOS Signal Broadcasting</string>
<string name="sos_notification_text">Emergency signal is being broadcast</string>
```

### Phase 4: BLE Implementation Details

#### 4.1 SOS Service UUID
**Standard**: Use custom 128-bit UUID for SOS service
- Base UUID: `00000000-0000-1000-8000-00805F9B34FB`
- Custom service UUID: `6E400001-B5A3-F393-E0A9-E50E24DCCA9E` (SOS Emergency)

#### 4.2 Pseudonymized Identifier
- Generate random UUID on first use
- Store in `Settings.Secure.SOS_IDENTIFIER`
- Regenerate option for privacy

#### 4.3 BLE Advertising Data
```java
AdvertiseData advertiseData = new AdvertiseData.Builder()
    .setIncludeDeviceName(false) // Privacy
    .addServiceUuid(ParcelUuid.fromString(SOS_SERVICE_UUID))
    .addManufacturerData(0x0000, mSOSIdentifier.getBytes())
    .build();
```

### Phase 5: Audible Siren Implementation

**Approach**: Use `ToneGenerator` or `MediaPlayer` with system alert stream

**Implementation**:
```java
private void startSiren() {
    ToneGenerator toneGen = new ToneGenerator(
        AudioManager.STREAM_ALARM, 100);
    // Generate SOS pattern: ... --- ... (3 short, 3 long, 3 short)
    // Loop pattern continuously
}
```

**Alternative**: Use raw audio file in `res/raw/sos_siren.ogg`

### Phase 6: Settings Integration

#### 6.1 Settings.Secure Keys
Add to `frameworks/base/core/java/android/provider/Settings.java`:
```java
/**
 * SOS Emergency Signaling
 * @hide
 */
public static final String SOS_BROADCAST_ENABLED = "sos_broadcast_enabled";

/**
 * SOS pseudonymized identifier (UUID)
 * @hide
 */
public static final String SOS_IDENTIFIER = "sos_identifier";

/**
 * SOS disaster detection enabled (future feature)
 * @hide
 */
public static final String SOS_DISASTER_DETECTION_ENABLED = "sos_disaster_detection_enabled";

/**
 * SOS recovery mode enabled (future feature)
 * @hide
 */
public static final String SOS_RECOVERY_MODE_ENABLED = "sos_recovery_mode_enabled";
```

**Note**: This requires framework modification. For MVP, use `Settings.System` or SharedPreferences.

#### 6.2 Preference Controller Integration
- Extend `AbstractPreferenceController`
- Implement `getAvailabilityStatus()`
- Handle preference changes
- Update service state

## Technical Considerations

### Bluetooth Permissions (Android 12+)
- Runtime permissions required for BLE operations
- Check `BluetoothAdapter.isEnabled()`
- Request `BLUETOOTH_ADVERTISE` permission
- Handle permission callbacks

### Service Lifecycle
- Start service as foreground service
- Show persistent notification: "SOS Signal Broadcasting"
- Keep service alive while broadcasting
- Stop service when toggle disabled

### Battery Optimization
- Request exemption from battery optimization
- Use BLE advertising efficiently (low power mode)
- Minimize siren volume/battery impact

### Privacy & Security
- Pseudonymized identifiers (no personal data)
- No internet connectivity required
- No location data transmitted
- Regeneratable identifiers

## Files to Create/Modify

### FRAMEWORKS/BASE (Commit 1: `sos: Broadcast`)

#### Files to Modify:
1. **`frameworks/base/core/java/android/provider/Settings.java`**
   - Add SOS settings keys to `Settings.Secure` class
   - Location: Inside `public static final class Secure` section
   - Add after existing secure settings keys (around line 2000-3000, depending on AOSP version)
   - Keys to add:
     ```java
     /**
      * SOS Emergency Signaling
      * @hide
      */
     public static final String SOS_BROADCAST_ENABLED = "sos_broadcast_enabled";
     
     /**
      * SOS pseudonymized identifier (UUID)
      * @hide
      */
     public static final String SOS_IDENTIFIER = "sos_identifier";
     
     /**
      * SOS disaster detection enabled (future feature)
      * @hide
      */
     public static final String SOS_DISASTER_DETECTION_ENABLED = "sos_disaster_detection_enabled";
     
     /**
      * SOS recovery mode enabled (future feature)
      * @hide
      */
     public static final String SOS_RECOVERY_MODE_ENABLED = "sos_recovery_mode_enabled";
     ```

#### System Properties (Optional):
- `ro.sos.enabled` - Build-time flag to enable SOS feature
- Add to `frameworks/base/core/java/android/os/SystemProperties.java` if needed
- Or use feature flag in Settings app directly

### PACKAGES/APPS/SETTINGS (Commit 2: `sos: Broadcast`)

#### Files to Create:
1. **`src/com/android/settings/sos/SOSService.java`**
   - ForegroundService for BLE advertising and siren
   - Package: `com.android.settings.sos`
   - Extends: `android.app.Service`
   - Implements: ForegroundService with notification

2. **`src/com/android/settings/sos/SOSSettingsFragment.java`**
   - Main Settings UI fragment
   - Package: `com.android.settings.sos`
   - Extends: `com.android.settings.SettingsPreferenceFragment`
   - Implements: Preference click handlers, permission requests

3. **`src/com/android/settings/sos/SOSBroadcastController.java`**
   - Preference controller for SOS toggle
   - Package: `com.android.settings.sos`
   - Extends: `com.android.settings.core.AbstractPreferenceController`
   - Implements: Service lifecycle management

4. **`res/xml/sos_settings.xml`**
   - Preference screen layout
   - Location: `res/xml/sos_settings.xml`
   - Contains: SwitchPreferenceCompat, status preference, info category

5. **`res/values/sos_strings.xml`**
   - String resources for SOS feature
   - Location: `res/values/sos_strings.xml`
   - Contains: All UI strings, summaries, titles

6. **`res/raw/sos_siren.ogg`** (Optional)
   - Audio file for SOS siren
   - Location: `res/raw/sos_siren.ogg`
   - Format: OGG Vorbis, 8kHz mono, ~5 seconds loop
   - Alternative: Generate programmatically with ToneGenerator

#### Files to Modify:
1. **`res/xml/anatolia.xml`**
   - Add SOS preference entry
   - Location: After line 134 (before closing PreferenceScreen tag)
   - Add:
     ```xml
     <!-- Emergency SOS -->
     <PreferenceScreen
         android:key="sos_category"
         android:title="@string/sos_settings_title"
         android:summary="@string/sos_settings_summary"
         android:fragment="com.android.settings.sos.SOSSettingsFragment" />
     ```

2. **`AndroidManifest.xml`**
   - Add service declaration
   - Location: Inside `<application>` tag
   - Add:
     ```xml
     <service
         android:name="com.android.settings.sos.SOSService"
         android:enabled="true"
         android:exported="false"
         android:foregroundServiceType="dataSync"
         android:permission="android.permission.BIND_FOREGROUND_SERVICE" />
     ```
   - Note: `foregroundServiceType` may need adjustment for Android 14+

3. **`res/values/strings.xml`** (if not using separate file)
   - Add SOS strings if not using `sos_strings.xml`
   - Location: End of file before closing `</resources>` tag

## Testing Checklist

- [ ] Bluetooth enabled/disabled handling
- [ ] Permission requests work correctly
- [ ] SOS broadcast starts/stops properly
- [ ] Service survives app backgrounding
- [ ] Siren plays correctly
- [ ] BLE advertising visible to other devices
- [ ] Battery impact acceptable
- [ ] Settings UI updates correctly
- [ ] Pseudonymized identifier generation works
- [ ] Service notification displays correctly

## Dependencies

- Android BLE API (`BluetoothLeAdvertiser`)
- Settings app framework (already present)
- ForegroundService support (Android 8.0+)

## Notes

- MVP focuses on manual SOS broadcasting only
- Foundation laid for disaster detection and recovery mode
- Uses existing Settings app patterns for consistency
- No SystemUI modifications required
- Clean, maintainable code following LineageOS conventions
- Follows AOSP security best practices
- Privacy-first design (pseudonymized identifiers)

## Implementation Order

1. **Framework Changes** (frameworks/base)
   - Add Settings.Secure keys
   - Commit: `sos: Broadcast`

2. **Settings App Changes** (packages/apps/Settings)
   - Create SOSService.java
   - Create SOSSettingsFragment.java
   - Create SOSBroadcastController.java
   - Create XML layouts and strings
   - Update AndroidManifest.xml
   - Update anatolia.xml
   - Commit: `sos: Broadcast`

## Future Enhancements

1. **Disaster Detection**
   - Sensor monitoring (accelerometer, gyroscope, barometer)
   - Background service for sensor monitoring
   - Automatic SOS trigger on detection

2. **Recovery Mode**
   - BLE scanning for nearby SOS signals
   - Distance estimation (RSSI-based)
   - List of nearby distress signals
   - Navigation assistance

3. **Enhanced Features**
   - Flashlight control (responder can toggle remotely)
   - Vibration patterns
   - Multiple SOS signal types
   - Signal strength visualization
   - History/logging of SOS events

