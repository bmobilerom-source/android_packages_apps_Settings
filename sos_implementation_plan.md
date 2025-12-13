# SOS Emergency Signal Implementation Plan

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

## Implementation Details

### FRAMEWORKS/BASE (Commit 1: `sos: Broadcast`)

#### File to Modify: `frameworks/base/core/java/android/provider/Settings.java`

**Location**: Inside `public static final class Secure` section
**Add after existing secure settings keys** (around line 2000-3000, depending on AOSP version)

**Code to Add**:
```java
/**
 * SOS Emergency Signaling - Enable/disable SOS broadcast
 * @hide
 */
public static final String SOS_BROADCAST_ENABLED = "sos_broadcast_enabled";

/**
 * SOS pseudonymized identifier (UUID) - Generated on first use
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

### PACKAGES/APPS/SETTINGS (Commit 2: `sos: Broadcast`)

#### File 1: `src/com/android/settings/sos/SOSService.java`

**Purpose**: ForegroundService for BLE advertising and siren generation

**Key Components**:
- Extends `android.app.Service`
- Implements ForegroundService with notification
- BLE advertising via `BluetoothLeAdvertiser`
- Siren generation via `ToneGenerator` or `MediaPlayer`
- Pseudonymized identifier management

**Key Methods**:
```java
public class SOSService extends Service {
    private static final String TAG = "SOSService";
    private static final String SOS_SERVICE_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    private static final int NOTIFICATION_ID = 1001;
    
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mAdvertiser;
    private AdvertiseCallback mAdvertiseCallback;
    private ToneGenerator mToneGenerator;
    private Handler mHandler;
    private String mSOSIdentifier;
    private boolean mIsBroadcasting = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler(Looper.getMainLooper());
        generateOrRetrieveIdentifier();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_START_SOS.equals(intent.getAction())) {
            startSOSBroadcast();
        } else if (intent != null && ACTION_STOP_SOS.equals(intent.getAction())) {
            stopSOSBroadcast();
        }
        return START_STICKY;
    }
    
    private void startSOSBroadcast() {
        if (mIsBroadcasting) return;
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not available");
            return;
        }
        
        startForeground(NOTIFICATION_ID, createNotification());
        startBLEAdvertising();
        startSiren();
        mIsBroadcasting = true;
    }
    
    private void stopSOSBroadcast() {
        if (!mIsBroadcasting) return;
        stopBLEAdvertising();
        stopSiren();
        stopForeground(true);
        mIsBroadcasting = false;
    }
    
    private void startBLEAdvertising() {
        // Implementation details in full code
    }
    
    private void startSiren() {
        // Generate SOS pattern: ... --- ... (3 short, 3 long, 3 short)
        // Implementation details in full code
    }
    
    private void generateOrRetrieveIdentifier() {
        // Generate or retrieve UUID from Settings.Secure
    }
}
```

**Full Implementation Notes**:
- Use `BluetoothAdapter.getBluetoothLeAdvertiser()` for BLE advertising
- Create `AdvertiseData` with SOS service UUID
- Include pseudonymized identifier in manufacturer data
- Set advertising mode: `ADVERTISE_MODE_LOW_LATENCY`
- Set TX power: `ADVERTISE_TX_POWER_HIGH`
- Use `ToneGenerator` for SOS pattern (3 short, 3 long, 3 short)
- Loop siren pattern continuously
- Show persistent notification while broadcasting

#### File 2: `src/com/android/settings/sos/SOSBroadcastController.java`

**Purpose**: Preference controller for SOS toggle

**Key Components**:
- Extends `AbstractPreferenceController`
- Manages service lifecycle
- Handles Bluetooth checks
- Updates UI state

**Key Methods**:
```java
public class SOSBroadcastController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {
    
    private static final String KEY_SOS_BROADCAST = "sos_broadcast_toggle";
    private SwitchPreferenceCompat mPreference;
    private Context mContext;
    
    public SOSBroadcastController(Context context, Lifecycle lifecycle) {
        super(context);
        mContext = context;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }
    
    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(KEY_SOS_BROADCAST);
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        if (enabled) {
            startSOSBroadcast();
        } else {
            stopSOSBroadcast();
        }
        return true;
    }
    
    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (mPreference != null) {
            mPreference.setChecked(isSOSActive());
        }
    }
    
    private void startSOSBroadcast() {
        // Check Bluetooth availability
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            // Bluetooth not supported
            showError(R.string.sos_bluetooth_not_supported);
            return;
        }
        
        // Auto-enable Bluetooth if disabled
        if (!adapter.isEnabled()) {
            adapter.enable();
            // Wait for Bluetooth to enable (use BroadcastReceiver to detect when ready)
            waitForBluetoothEnabled(adapter);
            return;
        }
        
        // Check permissions
        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions();
            return;
        }
        
        // Start service
        Intent serviceIntent = new Intent(mContext, SOSService.class);
        serviceIntent.setAction(SOSService.ACTION_START_SOS);
        mContext.startForegroundService(serviceIntent);
    }
    
    private void waitForBluetoothEnabled(BluetoothAdapter adapter) {
        // Register BroadcastReceiver to detect when Bluetooth is enabled
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    if (state == BluetoothAdapter.STATE_ON) {
                        context.unregisterReceiver(this);
                        // Now proceed with starting SOS
                        startSOSBroadcast();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(receiver, filter);
    }
    
    private void stopSOSBroadcast() {
        // Stop service
    }
    
    private boolean isSOSActive() {
        // Check if service is running
    }
    
    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
    
    @Override
    public String getPreferenceKey() {
        return KEY_SOS_BROADCAST;
    }
}
```

#### File 3: `src/com/android/settings/sos/SOSSettingsFragment.java`

**Purpose**: Main Settings UI fragment

**Key Components**:
- Extends `SettingsPreferenceFragment`
- Handles preference clicks
- Requests permissions
- Updates status display

**Key Methods**:
```java
public class SOSSettingsFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    
    private static final String TAG = "SOSSettingsFragment";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 100;
    
    private SOSBroadcastController mController;
    private Preference mStatusPreference;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sos_settings);
        
        mController = new SOSBroadcastController(getContext(), getSettingsLifecycle());
        
        mStatusPreference = findPreference("sos_status");
        updateStatus();
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return mController.onPreferenceChange(preference, newValue);
    }
    
    private void updateStatus() {
        if (mStatusPreference != null) {
            boolean isActive = mController.isSOSActive();
            mStatusPreference.setSummary(isActive 
                ? getString(R.string.sos_status_active)
                : getString(R.string.sos_status_summary));
        }
    }
    
    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
```

#### File 4: `res/xml/sos_settings.xml`

**Purpose**: Preference screen layout

**Content**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    
    <SwitchPreferenceCompat
        android:key="sos_broadcast_toggle"
        android:title="@string/sos_broadcast_title"
        android:summary="@string/sos_broadcast_summary"
        android:icon="@drawable/ic_sos"
        app:useSimpleSummaryProvider="true" />
    
    <Preference
        android:key="sos_status"
        android:title="@string/sos_status_title"
        android:summary="@string/sos_status_summary"
        android:selectable="false" />
    
    <PreferenceCategory
        android:title="@string/sos_info_category">
        <Preference
            android:key="sos_info"
            android:summary="@string/sos_info_summary"
            android:selectable="false" />
    </PreferenceCategory>
</PreferenceScreen>
```

#### File 5: `res/values/sos_strings.xml`

**Purpose**: String resources for SOS feature

**Content**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="sos_settings_title">Emergency SOS</string>
    <string name="sos_settings_summary">Offline emergency signaling</string>
    <string name="sos_broadcast_title">Send SOS Signal</string>
    <string name="sos_broadcast_summary">Broadcast emergency signal via Bluetooth</string>
    <string name="sos_status_title">Status</string>
    <string name="sos_status_summary">Not broadcasting</string>
    <string name="sos_status_active">Broadcasting SOS signal</string>
    <string name="sos_info_category">Information</string>
    <string name="sos_info_summary">SOS signals work offline using Bluetooth Low Energy. Range: 10-30 meters. No internet required.</string>
    <string name="sos_bluetooth_required">Bluetooth must be enabled</string>
    <string name="sos_bluetooth_enabling">Enabling Bluetooth...</string>
    <string name="sos_bluetooth_enable_failed">Failed to enable Bluetooth</string>
    <string name="sos_bluetooth_not_supported">Bluetooth not supported on this device</string>
    <string name="sos_permission_required">Bluetooth permissions required</string>
    <string name="sos_notification_title">SOS Signal Broadcasting</string>
    <string name="sos_notification_text">Emergency signal is being broadcast</string>
</resources>
```

#### File 6: `res/xml/anatolia.xml` (Modify)

**Purpose**: Add SOS preference entry to Anatolia settings

**Location**: After line 134 (before closing `</PreferenceScreen>` tag)

**Add**:
```xml
        <!-- Emergency SOS -->
        <PreferenceScreen
            android:key="sos_category"
            android:title="@string/sos_settings_title"
            android:summary="@string/sos_settings_summary"
            android:fragment="com.android.settings.sos.SOSSettingsFragment" />
```

#### File 7: `AndroidManifest.xml` (Modify)

**Purpose**: Add service declaration

**Location**: Inside `<application>` tag, before closing `</application>` tag

**Add**:
```xml
        <!-- SOS Emergency Service -->
        <service
            android:name="com.android.settings.sos.SOSService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="dataSync"
            android:permission="android.permission.BIND_FOREGROUND_SERVICE" />
```

**Note**: `foregroundServiceType` may need adjustment for Android 14+. Use `"dataSync"` or check Android version and use appropriate type.

## Technical Implementation Details

### BLE Advertising Setup

**SOS Service UUID**: `6E400001-B5A3-F393-E0A9-E50E24DCCA9E`

**Advertising Data Structure**:
```java
AdvertiseData advertiseData = new AdvertiseData.Builder()
    .setIncludeDeviceName(false) // Privacy - don't include device name
    .addServiceUuid(ParcelUuid.fromString(SOS_SERVICE_UUID))
    .addManufacturerData(0x0000, mSOSIdentifier.getBytes())
    .build();

AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
    .setConnectable(false) // Non-connectable advertising
    .build();
```

### Pseudonymized Identifier Generation

**Implementation**:
```java
private void generateOrRetrieveIdentifier() {
    String identifier = Settings.Secure.getString(
        getContentResolver(), 
        Settings.Secure.SOS_IDENTIFIER);
    
    if (identifier == null || identifier.isEmpty()) {
        identifier = UUID.randomUUID().toString();
        Settings.Secure.putString(
            getContentResolver(),
            Settings.Secure.SOS_IDENTIFIER,
            identifier);
    }
    
    mSOSIdentifier = identifier;
}
```

### Siren Generation

**SOS Pattern**: `... --- ...` (3 short beeps, 3 long beeps, 3 short beeps)

**Implementation**:
```java
private void startSiren() {
    if (mToneGenerator == null) {
        mToneGenerator = new ToneGenerator(
            AudioManager.STREAM_ALARM, 100);
    }
    
    // Generate SOS pattern in a loop
    mHandler.post(mSirenRunnable);
}

private final Runnable mSirenRunnable = new Runnable() {
    @Override
    public void run() {
        if (!mIsBroadcasting) return;
        
        // 3 short beeps
        mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        mHandler.postDelayed(() -> {
            mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            mHandler.postDelayed(() -> {
                mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                mHandler.postDelayed(() -> {
                    // 3 long beeps
                    mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600);
                    mHandler.postDelayed(() -> {
                        mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600);
                        mHandler.postDelayed(() -> {
                            mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600);
                            mHandler.postDelayed(() -> {
                                // 3 short beeps
                                mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                                mHandler.postDelayed(() -> {
                                    mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                                    mHandler.postDelayed(() -> {
                                        mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                                        // Pause before repeating
                                        mHandler.postDelayed(mSirenRunnable, 1000);
                                    }, 200);
                                }, 200);
                            }, 600);
                        }, 600);
                    }, 600);
                }, 200);
            }, 200);
        }, 200);
    }
};
```

### Notification Creation

**Implementation**:
```java
private Notification createNotification() {
    Intent stopIntent = new Intent(this, SOSService.class);
    stopIntent.setAction(ACTION_STOP_SOS);
    PendingIntent stopPendingIntent = PendingIntent.getService(
        this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);
    
    return new Notification.Builder(this, createNotificationChannel())
        .setContentTitle(getString(R.string.sos_notification_title))
        .setContentText(getString(R.string.sos_notification_text))
        .setSmallIcon(R.drawable.ic_sos)
        .setOngoing(true)
        .addAction(R.drawable.ic_stop, 
            getString(R.string.stop), 
            stopPendingIntent)
        .build();
}
```

### Automatic Bluetooth Enabling

**CRITICAL REQUIREMENT**: Bluetooth must automatically turn on when SOS is triggered.

**Implementation**:
```java
private void startSOSBroadcast() {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if (adapter == null) {
        // Bluetooth not supported - show error
        return;
    }
    
    // Auto-enable Bluetooth if disabled
    if (!adapter.isEnabled()) {
        Log.d(TAG, "Bluetooth disabled, enabling automatically");
        adapter.enable();
        
        // Register BroadcastReceiver to wait for Bluetooth to be enabled
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    if (state == BluetoothAdapter.STATE_ON) {
                        context.unregisterReceiver(this);
                        // Bluetooth is now enabled, proceed with SOS
                        proceedWithSOSStart();
                    } else if (state == BluetoothAdapter.STATE_OFF) {
                        // Bluetooth failed to enable
                        context.unregisterReceiver(this);
                        showError(R.string.sos_bluetooth_enable_failed);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(receiver, filter);
        return;
    }
    
    // Bluetooth is enabled, proceed with SOS
    proceedWithSOSStart();
}

private void proceedWithSOSStart() {
    // Check permissions, then start service
    if (!checkBluetoothPermissions()) {
        requestBluetoothPermissions();
        return;
    }
    
    Intent serviceIntent = new Intent(mContext, SOSService.class);
    serviceIntent.setAction(SOSService.ACTION_START_SOS);
    mContext.startForegroundService(serviceIntent);
}
```

**In SOSService.java**:
```java
private void startSOSBroadcast() {
    if (mIsBroadcasting) return;
    
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if (adapter == null || !adapter.isEnabled()) {
        Log.e(TAG, "Bluetooth not available or not enabled");
        // Try to enable if not enabled
        if (adapter != null && !adapter.isEnabled()) {
            adapter.enable();
        }
        return;
    }
    
    startForeground(NOTIFICATION_ID, createNotification());
    startBLEAdvertising();
    startSiren();
    mIsBroadcasting = true;
}
```

### Bluetooth Permission Handling

**Implementation**:
```java
private boolean checkBluetoothPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (ContextCompat.checkSelfPermission(mContext, 
                Manifest.permission.BLUETOOTH_ADVERTISE) 
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
    }
    return true;
}

private void requestBluetoothPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ActivityCompat.requestPermissions(activity,
            new String[]{
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            },
            REQUEST_BLUETOOTH_PERMISSIONS);
    }
}
```

## Testing Checklist

- [ ] **Bluetooth auto-enable**: Bluetooth automatically turns on when SOS is triggered
- [ ] **Bluetooth state detection**: Correctly detects when Bluetooth is enabled after auto-enable
- [ ] Bluetooth enabled/disabled handling
- [ ] Permission requests work correctly
- [ ] SOS broadcast starts/stops properly
- [ ] Service survives app backgrounding
- [ ] Siren plays correctly (SOS pattern)
- [ ] BLE advertising visible to other devices
- [ ] Battery impact acceptable
- [ ] Settings UI updates correctly
- [ ] Pseudonymized identifier generation works
- [ ] Service notification displays correctly
- [ ] Notification action (stop) works
- [ ] Service survives device sleep/wake
- [ ] Multiple start/stop cycles work correctly
- [ ] Bluetooth enable timeout handling (if Bluetooth fails to enable)

## Dependencies

- Android BLE API (`BluetoothLeAdvertiser`)
- Settings app framework (already present)
- ForegroundService support (Android 8.0+)
- `Settings.Secure` API (requires framework modification)

## Notes

- MVP focuses on manual SOS broadcasting only
- Foundation laid for disaster detection and recovery mode
- Uses existing Settings app patterns for consistency
- No SystemUI modifications required
- Clean, maintainable code following LineageOS conventions
- Follows AOSP security best practices
- Privacy-first design (pseudonymized identifiers)
- Completely offline operation (no internet required)

## Implementation Order

1. **Framework Changes** (frameworks/base)
   - Add Settings.Secure keys to `Settings.java`
   - Commit: `sos: Broadcast`

2. **Settings App Changes** (packages/apps/Settings)
   - Create `SOSService.java`
   - Create `SOSBroadcastController.java`
   - Create `SOSSettingsFragment.java`
   - Create `sos_settings.xml`
   - Create `sos_strings.xml`
   - Update `AndroidManifest.xml`
   - Update `anatolia.xml`
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

