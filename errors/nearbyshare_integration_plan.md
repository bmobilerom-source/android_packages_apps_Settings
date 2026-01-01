# NearbyShare Integration Plan for Android Settings

## Overview
This document outlines the plan to integrate NearbyShare file transfer functionality (similar to [Coder481/NearbyShare](https://github.com/Coder481/NearbyShare) and [Prashant-Android/NearbyShare-BLE](https://github.com/Prashant-Android/NearbyShare-BLE)) into the Android Settings application.

## Repository Analysis

### Coder481/NearbyShare
- **Technology**: Google Nearby Connections API (`play-services-nearby:18.3.0`)
- **Features**: 
  - Offline file sharing between 2 Android devices
  - Uses BLE + Wi-Fi for connection
  - Advertising (Sender) and Discovering (Receiver) modes
  - File transfer with progress tracking
- **Permissions**: Bluetooth, Wi-Fi, Location (required for Nearby API)
- **Language**: Kotlin

### Prashant-Android/NearbyShare-BLE
- **Technology**: Google Nearby Connections API
- **Features**:
  - File transfer via BLE or Wi-Fi
  - Device discovery
  - Connection management
  - Modern UI with Jetpack Compose
  - Transfer progress monitoring
- **Language**: Kotlin

## Integration Approach

### ✅ **Can Be Integrated Without Framework Changes**

Unlike FuckLocation, NearbyShare uses **Google Play Services APIs** which are available to any app, including Settings. No framework modifications needed.

### Key Requirements:
1. **Google Play Services**: Nearby Connections API dependency
2. **Permissions**: Bluetooth, Wi-Fi, Location (runtime permissions)
3. **UI Integration**: Settings fragment/activity
4. **File Picker**: Android's standard file picker

---

## Implementation Plan

### Phase 1: Dependencies & Setup

#### 1.1 Add Gradle Dependencies

**File**: `build.gradle` (app module)

```gradle
dependencies {
    // Nearby Connections API
    implementation 'com.google.android.gms:play-services-nearby:18.7.0'
    
    // Location services (required for Nearby API)
    implementation 'com.google.android.gms:play-services-location:21.0.1'
    
    // Kotlin Coroutines (if using Kotlin)
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3"
}
```

#### 1.2 Add Permissions

**File**: `AndroidManifest.xml`

```xml
<!-- Required for Nearby Connections -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Required for Android 12+ -->
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" 
    android:usesPermissionFlags="neverForLocation" />

<!-- For file sharing -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
```

---

### Phase 2: Core Implementation

#### 2.1 Create NearbyShare Service/Manager

**File**: `src/com/android/settings/nearbyshare/NearbyShareManager.java`

```java
package com.android.settings.nearbyshare;

import android.content.Context;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.DiscoveryOptions;

/**
 * Manager class for Nearby Connections API
 * Handles advertising, discovery, and file transfer
 */
public class NearbyShareManager {
    private static final String TAG = "NearbyShareManager";
    private static final String SERVICE_ID = "com.android.settings.nearbyshare";
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    
    private final Context mContext;
    private ConnectionsClient mConnectionsClient;
    private NearbyShareCallback mCallback;
    
    public interface NearbyShareCallback {
        void onDeviceFound(String endpointId, String deviceName);
        void onConnectionInitiated(String endpointId, String deviceName);
        void onConnectionResult(String endpointId, boolean success);
        void onDisconnected(String endpointId);
        void onPayloadReceived(Payload payload);
        void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update);
    }
    
    public NearbyShareManager(Context context) {
        mContext = context;
        mConnectionsClient = Nearby.getConnectionsClient(context);
    }
    
    public void setCallback(NearbyShareCallback callback) {
        mCallback = callback;
    }
    
    // Start advertising (sender mode)
    public void startAdvertising(String deviceName) {
        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build();
            
        mConnectionsClient.startAdvertising(
            deviceName,
            SERVICE_ID,
            mConnectionLifecycleCallback,
            advertisingOptions
        );
    }
    
    // Start discovering (receiver mode)
    public void startDiscovering() {
        DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build();
            
        mConnectionsClient.startDiscovery(
            SERVICE_ID,
            mEndpointDiscoveryCallback,
            discoveryOptions
        );
    }
    
    // Stop advertising/discovering
    public void stop() {
        mConnectionsClient.stopAdvertising();
        mConnectionsClient.stopDiscovery();
        mConnectionsClient.stopAllEndpoints();
    }
    
    // Send file
    public void sendFile(String endpointId, java.io.File file) {
        Payload filePayload = Payload.fromFile(file);
        mConnectionsClient.sendPayload(endpointId, filePayload);
    }
    
    // Accept connection
    public void acceptConnection(String endpointId) {
        mConnectionsClient.acceptConnection(endpointId, mPayloadCallback);
    }
    
    // Reject connection
    public void rejectConnection(String endpointId) {
        mConnectionsClient.rejectConnection(endpointId);
    }
    
    // Connection lifecycle callbacks
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback = 
        new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(String endpointId, ConnectionInfo info) {
                if (mCallback != null) {
                    mCallback.onConnectionInitiated(endpointId, info.getEndpointName());
                }
            }
            
            @Override
            public void onConnectionResult(String endpointId, ConnectionResolution resolution) {
                boolean success = resolution.getStatus().isSuccess();
                if (mCallback != null) {
                    mCallback.onConnectionResult(endpointId, success);
                }
            }
            
            @Override
            public void onDisconnected(String endpointId) {
                if (mCallback != null) {
                    mCallback.onDisconnected(endpointId);
                }
            }
        };
    
    // Endpoint discovery callback
    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback = 
        new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                if (mCallback != null) {
                    mCallback.onDeviceFound(endpointId, info.getEndpointName());
                }
            }
            
            @Override
            public void onEndpointLost(String endpointId) {
                // Handle endpoint lost
            }
        };
    
    // Payload callback
    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            if (mCallback != null) {
                mCallback.onPayloadReceived(payload);
            }
        }
        
        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            if (mCallback != null) {
                mCallback.onPayloadTransferUpdate(endpointId, update);
            }
        }
    };
}
```

#### 2.2 Create Permission Helper

**File**: `src/com/android/settings/nearbyshare/NearbySharePermissionHelper.java`

```java
package com.android.settings.nearbyshare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

/**
 * Helper class to check and request permissions for Nearby Share
 */
public class NearbySharePermissionHelper {
    
    public static boolean arePermissionsGranted(Context context) {
        boolean basic = ContextCompat.checkSelfPermission(context, 
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(context, 
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return basic
                && ContextCompat.checkSelfPermission(context, 
                    Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, 
                    Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, 
                    Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        return basic;
    }
    
    public static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[] {
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            return new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
    }
}
```

---

### Phase 3: UI Implementation

#### 3.1 Create Settings Fragment

**File**: `src/com/android/settings/nearbyshare/NearbyShareSettingsFragment.java`

```java
package com.android.settings.nearbyshare;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

/**
 * Settings fragment for Nearby Share file transfer
 */
public class NearbyShareSettingsFragment extends SettingsPreferenceFragment {
    private static final String TAG = "NearbyShareSettings";
    private static final int REQUEST_CODE_PICK_FILE = 1001;
    
    private NearbyShareManager mNearbyShareManager;
    private Preference mStartAdvertisingPref;
    private Preference mStartDiscoveringPref;
    private Preference mSelectFilePref;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nearbyshare_settings);
        
        mNearbyShareManager = new NearbyShareManager(getContext());
        mNearbyShareManager.setCallback(mCallback);
        
        setupPreferences();
    }
    
    private void setupPreferences() {
        PreferenceScreen screen = getPreferenceScreen();
        
        mStartAdvertisingPref = screen.findPreference("nearbyshare_start_advertising");
        mStartDiscoveringPref = screen.findPreference("nearbyshare_start_discovering");
        mSelectFilePref = screen.findPreference("nearbyshare_select_file");
        
        mStartAdvertisingPref.setOnPreferenceClickListener(preference -> {
            if (checkPermissions()) {
                startAdvertising();
            }
            return true;
        });
        
        mStartDiscoveringPref.setOnPreferenceClickListener(preference -> {
            if (checkPermissions()) {
                startDiscovering();
            }
            return true;
        });
        
        mSelectFilePref.setOnPreferenceClickListener(preference -> {
            openFilePicker();
            return true;
        });
    }
    
    private boolean checkPermissions() {
        if (!NearbySharePermissionHelper.arePermissionsGranted(getContext())) {
            requestPermissions(
                NearbySharePermissionHelper.getRequiredPermissions(),
                REQUEST_CODE_PERMISSIONS
            );
            return false;
        }
        return true;
    }
    
    private void startAdvertising() {
        String deviceName = android.os.Build.MODEL;
        mNearbyShareManager.startAdvertising(deviceName);
        mStartAdvertisingPref.setSummary("Advertising...");
    }
    
    private void startDiscovering() {
        mNearbyShareManager.startDiscovering();
        mStartDiscoveringPref.setSummary("Discovering devices...");
    }
    
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();
            // Handle file selection
        }
    }
    
    private final NearbyShareManager.NearbyShareCallback mCallback = 
        new NearbyShareManager.NearbyShareCallback() {
            @Override
            public void onDeviceFound(String endpointId, String deviceName) {
                // Show device in UI
            }
            
            @Override
            public void onConnectionInitiated(String endpointId, String deviceName) {
                // Show connection dialog
            }
            
            @Override
            public void onConnectionResult(String endpointId, boolean success) {
                // Update UI
            }
            
            @Override
            public void onDisconnected(String endpointId) {
                // Update UI
            }
            
            @Override
            public void onPayloadReceived(Payload payload) {
                // Save received file
            }
            
            @Override
            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                // Update progress
            }
        };
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNearbyShareManager != null) {
            mNearbyShareManager.stop();
        }
    }
}
```

#### 3.2 Create Preference XML

**File**: `res/xml/nearbyshare_settings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:settings="http://schemas.android.com/apk/res-auto"
    android:key="nearbyshare_settings_screen"
    android:title="@string/nearbyshare_settings_title">
    
    <com.android.settingslib.widget.TopIntroPreference
        android:key="nearbyshare_intro"
        android:title="@string/nearbyshare_intro_title"
        android:summary="@string/nearbyshare_intro_summary"
        settings:searchable="false" />
    
    <PreferenceCategory
        android:key="nearbyshare_mode_category"
        android:title="@string/nearbyshare_mode_category_title"
        android:order="10">
        
        <Preference
            android:key="nearbyshare_start_advertising"
            android:title="@string/nearbyshare_start_advertising_title"
            android:summary="@string/nearbyshare_start_advertising_summary"
            android:order="11"
            android:icon="@drawable/ic_nearbyshare_send"
            settings:layout="@layout/adaptive_preference_card_top" />
        
        <Preference
            android:key="nearbyshare_start_discovering"
            android:title="@string/nearbyshare_start_discovering_title"
            android:summary="@string/nearbyshare_start_discovering_summary"
            android:order="12"
            android:icon="@drawable/ic_nearbyshare_receive"
            settings:layout="@layout/adaptive_preference_card_bottom" />
    </PreferenceCategory>
    
    <PreferenceCategory
        android:key="nearbyshare_file_category"
        android:title="@string/nearbyshare_file_category_title"
        android:order="20">
        
        <Preference
            android:key="nearbyshare_select_file"
            android:title="@string/nearbyshare_select_file_title"
            android:summary="@string/nearbyshare_select_file_summary"
            android:order="21"
            android:icon="@drawable/ic_file"
            settings:layout="@layout/adaptive_preference_card" />
    </PreferenceCategory>
    
    <PreferenceCategory
        android:key="nearbyshare_devices_category"
        android:title="@string/nearbyshare_devices_category_title"
        android:order="30">
        <!-- Dynamic device list will be added here -->
    </PreferenceCategory>
    
</PreferenceScreen>
```

#### 3.3 Add to Connected Devices

**File**: `res/xml/connected_devices.xml` (modify existing)

```xml
<!-- Add after existing preferences -->
<Preference
    android:fragment="com.android.settings.nearbyshare.NearbyShareSettingsFragment"
    android:key="nearbyshare_settings"
    android:title="@string/nearbyshare_settings_title"
    android:summary="@string/nearbyshare_settings_summary"
    android:icon="@drawable/ic_nearbyshare"
    android:order="50"
    settings:searchable="true" />
```

---

### Phase 4: String Resources

**File**: `res/values/nearbyshare_strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Main Settings -->
    <string name="nearbyshare_settings_title">Nearby Share</string>
    <string name="nearbyshare_settings_summary">Share files with nearby devices</string>
    
    <!-- Intro -->
    <string name="nearbyshare_intro_title">Share files offline</string>
    <string name="nearbyshare_intro_summary">Transfer files to nearby Android devices using Bluetooth and Wi-Fi</string>
    
    <!-- Mode Category -->
    <string name="nearbyshare_mode_category_title">Connection Mode</string>
    <string name="nearbyshare_start_advertising_title">Send Files</string>
    <string name="nearbyshare_start_advertising_summary">Make your device discoverable</string>
    <string name="nearbyshare_start_discovering_title">Receive Files</string>
    <string name="nearbyshare_start_discovering_summary">Search for nearby devices</string>
    
    <!-- File Category -->
    <string name="nearbyshare_file_category_title">File Transfer</string>
    <string name="nearbyshare_select_file_title">Select File</string>
    <string name="nearbyshare_select_file_summary">Choose a file to share</string>
    
    <!-- Devices Category -->
    <string name="nearbyshare_devices_category_title">Nearby Devices</string>
    
    <!-- Status Messages -->
    <string name="nearbyshare_status_advertising">Advertising...</string>
    <string name="nearbyshare_status_discovering">Discovering devices...</string>
    <string name="nearbyshare_status_connected">Connected</string>
    <string name="nearbyshare_status_transferring">Transferring...</string>
    <string name="nearbyshare_status_complete">Transfer complete</string>
    
    <!-- Errors -->
    <string name="nearbyshare_error_permissions">Permissions required</string>
    <string name="nearbyshare_error_no_devices">No devices found</string>
    <string name="nearbyshare_error_connection_failed">Connection failed</string>
</resources>
```

---

### Phase 5: Drawable Resources

Create icons:
- `res/drawable/ic_nearbyshare.xml` - Main icon
- `res/drawable/ic_nearbyshare_send.xml` - Send icon
- `res/drawable/ic_nearbyshare_receive.xml` - Receive icon
- `res/drawable/ic_file.xml` - File icon

---

### Phase 6: Register in SettingsGateway

**File**: `src/com/android/settings/core/gateway/SettingsGateway.java`

```java
// Add to ENTRY_FRAGMENTS array
com.android.settings.nearbyshare.NearbyShareSettingsFragment.class.getName(),
```

---

## Implementation Checklist

### Dependencies
- [ ] Add Google Play Services Nearby API dependency
- [ ] Add Location Services dependency
- [ ] Add Kotlin Coroutines (if using Kotlin)

### Permissions
- [ ] Add Bluetooth permissions to manifest
- [ ] Add Wi-Fi permissions to manifest
- [ ] Add Location permissions to manifest
- [ ] Add Storage permissions to manifest
- [ ] Implement runtime permission requests

### Core Implementation
- [ ] Create NearbyShareManager class
- [ ] Create PermissionHelper class
- [ ] Implement advertising mode
- [ ] Implement discovery mode
- [ ] Implement file transfer
- [ ] Implement connection callbacks

### UI Implementation
- [ ] Create NearbyShareSettingsFragment
- [ ] Create preference XML
- [ ] Add to Connected Devices screen
- [ ] Create device list UI
- [ ] Create connection dialog
- [ ] Create transfer progress UI

### Resources
- [ ] Add string resources
- [ ] Create drawable icons
- [ ] Add layout resources (if needed)

### Integration
- [ ] Register fragment in SettingsGateway
- [ ] Add to Connected Devices XML
- [ ] Test permissions flow
- [ ] Test advertising/discovery
- [ ] Test file transfer

### Testing
- [ ] Test on Android 11 and below
- [ ] Test on Android 12+ (new Bluetooth permissions)
- [ ] Test file transfer between devices
- [ ] Test connection stability
- [ ] Test error handling

---

## Key Differences from Standalone Apps

### Settings Integration Benefits:
1. **No separate app**: Integrated into system Settings
2. **System permissions**: Can request permissions more seamlessly
3. **Consistent UI**: Matches Settings app design language
4. **Better discoverability**: Users find it in Connected Devices

### Considerations:
1. **Google Play Services**: Requires GMS (may not work on AOSP without GMS)
2. **Permissions**: Must handle runtime permissions carefully
3. **Lifecycle**: Must properly handle fragment lifecycle
4. **Background**: Settings fragments have different lifecycle than Activities

---

## Framework Changes Required

### ❌ **NO FRAMEWORK CHANGES NEEDED**

This implementation uses:
- **Google Play Services APIs** (available to any app)
- **Standard Android permissions** (runtime permissions)
- **Standard file picker** (Intent.ACTION_GET_CONTENT)

No modifications to AOSP framework required.

---

## Alternative: AOSP-Only Implementation

If Google Play Services is not available, you would need to:
1. Implement BLE scanning manually
2. Implement Wi-Fi Direct manually
3. Create custom file transfer protocol
4. **This would require significant framework work**

**Recommendation**: Use Google Play Services API (as in the reference repos) for easier implementation.

---

## File Structure

```
src/com/android/settings/nearbyshare/
├── NearbyShareManager.java              # Core API wrapper
├── NearbyShareSettingsFragment.java     # Main settings fragment
├── NearbySharePermissionHelper.java     # Permission handling
├── NearbyShareDeviceController.java     # Device list controller
└── NearbyShareTransferController.java   # Transfer progress controller

res/xml/
└── nearbyshare_settings.xml            # Preference screen

res/values/
└── nearbyshare_strings.xml             # String resources

res/drawable/
├── ic_nearbyshare.xml                  # Main icon
├── ic_nearbyshare_send.xml             # Send icon
└── ic_nearbyshare_receive.xml          # Receive icon
```

---

## References

- [Coder481/NearbyShare](https://github.com/Coder481/NearbyShare)
- [Prashant-Android/NearbyShare-BLE](https://github.com/Prashant-Android/NearbyShare-BLE)
- [Google Nearby Connections API Documentation](https://developers.google.com/nearby/connections/overview)
- Current Settings: `connected_devices.xml`, `BluetoothDashboardFragment.java`

