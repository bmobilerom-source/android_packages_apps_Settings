# Intruder Selfie Implementation Plan

## Overview
Integrate "Intruder Selfie" functionality into LineageOS Settings app. Takes a photo with the front camera every time the screen is unlocked, saving it locally with a timestamp. Useful for security - catches anyone trying to unlock your phone.

## Commit Structure
- **Commit 1 (frameworks/base)**: `intruder: Selfie` - Add Settings keys (if needed)
- **Commit 2 (packages/apps/Settings)**: `intruder: Selfie` - Add Settings UI and service implementation

## Features List

### Phase 1: Core Intruder Selfie Functionality (Priority - MVP)
1. **Screen Unlock Detection**
   - Detect when screen is unlocked
   - Use BroadcastReceiver for `ACTION_USER_PRESENT` or KeyguardManager
   - Works after device boot (via BootReceiver)

2. **Automatic Photo Capture**
   - Take photo with front camera on each unlock
   - Silent capture (no preview, no sound)
   - Add timestamp to photo filename
   - Save to app-specific storage

3. **Foreground Service**
   - Lightweight foreground service
   - Persistent notification: "Watching for unlocks..."
   - Starts automatically after boot
   - Survives app backgrounding

4. **Photo Management**
   - View captured photos in Settings UI
   - Delete individual photos
   - Clear all photos option
   - Photo count display

5. **Privacy & Security**
   - Photos stored locally only
   - No internet connectivity required
   - No cloud uploads
   - No analytics or tracking

## Architecture

### Component Structure
```
Settings App (UI Layer)
├── IntruderSelfieSettingsFragment.java - Main settings UI
├── IntruderSelfieController.java - Controls service
├── IntruderSelfieService.java - ForegroundService for monitoring
├── IntruderSelfieBootReceiver.java - Start service on boot
└── IntruderSelfieUnlockReceiver.java - Detect screen unlocks
    └── CameraCaptureHelper.java - Handle camera operations
```

### Design Decision: ForegroundService + BroadcastReceiver Pattern
- **Why**: Clean approach - Settings app manages service, receivers detect events
- **Benefits**: No SystemUI modifications, works after reboot, simple lifecycle
- **Pattern**: Similar to SOS feature - lightweight foreground service with event receivers

## Implementation Details

### FRAMEWORKS/BASE (Commit 1: `intruder: Selfie`)

#### File to Modify: `frameworks/base/core/java/android/provider/Settings.java` (Optional)

**Location**: Inside `public static final class Secure` section
**Add after existing secure settings keys** (around line 2000-3000, depending on AOSP version)

**Code to Add** (if needed for state persistence):
```java
/**
 * Intruder Selfie - Enable/disable automatic photo capture on unlock
 * @hide
 */
public static final String INTRUDER_SELFIE_ENABLED = "intruder_selfie_enabled";

/**
 * Intruder Selfie - Last capture timestamp
 * @hide
 */
public static final String INTRUDER_SELFIE_LAST_CAPTURE = "intruder_selfie_last_capture";
```

**Note**: Can also use SharedPreferences if framework changes are not desired.

### PACKAGES/APPS/SETTINGS (Commit 2: `intruder: Selfie`)

#### File 1: `src/com/android/settings/intruder/IntruderSelfieService.java`

**Purpose**: ForegroundService that monitors screen unlocks and triggers photo capture

**Key Components**:
- Extends `android.app.Service`
- Implements ForegroundService with notification
- Registers BroadcastReceiver for screen unlock events
- Manages camera capture operations
- Handles photo storage

**Key Methods**:
```java
public class IntruderSelfieService extends Service {
    private static final String TAG = "IntruderSelfieService";
    private static final int NOTIFICATION_ID = 1002;
    private static final String ACTION_START_MONITORING = "com.android.settings.intruder.START_MONITORING";
    private static final String ACTION_STOP_MONITORING = "com.android.settings.intruder.STOP_MONITORING";
    
    private BroadcastReceiver mUnlockReceiver;
    private CameraCaptureHelper mCameraHelper;
    private File mPhotoStorageDir;
    private boolean mIsMonitoring = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mCameraHelper = new CameraCaptureHelper(this);
        mPhotoStorageDir = getPhotoStorageDirectory();
        createPhotoStorageDirectory();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_START_MONITORING.equals(intent.getAction())) {
            startMonitoring();
        } else if (intent != null && ACTION_STOP_MONITORING.equals(intent.getAction())) {
            stopMonitoring();
        }
        return START_STICKY;
    }
    
    private void startMonitoring() {
        if (mIsMonitoring) return;
        
        // Check camera permission
        if (!checkCameraPermission()) {
            Log.e(TAG, "Camera permission not granted");
            return;
        }
        
        // Register unlock receiver
        registerUnlockReceiver();
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification());
        
        mIsMonitoring = true;
    }
    
    private void stopMonitoring() {
        if (!mIsMonitoring) return;
        
        unregisterUnlockReceiver();
        stopForeground(true);
        
        mIsMonitoring = false;
    }
    
    private void registerUnlockReceiver() {
        if (mUnlockReceiver == null) {
            mUnlockReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                        captureIntruderSelfie();
                    }
                }
            };
        }
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mUnlockReceiver, filter);
    }
    
    private void captureIntruderSelfie() {
        // Capture photo with front camera
        mCameraHelper.captureFrontCameraPhoto(new CameraCaptureHelper.CaptureCallback() {
            @Override
            public void onCaptureSuccess(File photoFile) {
                Log.d(TAG, "Intruder selfie captured: " + photoFile.getName());
                // Update last capture timestamp
                Settings.Secure.putLong(getContentResolver(),
                    Settings.Secure.INTRUDER_SELFIE_LAST_CAPTURE,
                    System.currentTimeMillis());
            }
            
            @Override
            public void onCaptureError(String error) {
                Log.e(TAG, "Failed to capture intruder selfie: " + error);
            }
        });
    }
    
    private File getPhotoStorageDirectory() {
        // Use app-specific external storage
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IntruderSelfie");
        return storageDir;
    }
    
    private Notification createNotification() {
        Intent stopIntent = new Intent(this, IntruderSelfieService.class);
        stopIntent.setAction(ACTION_STOP_MONITORING);
        PendingIntent stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);
        
        return new Notification.Builder(this, createNotificationChannel())
            .setContentTitle(getString(R.string.intruder_notification_title))
            .setContentText(getString(R.string.intruder_notification_text))
            .setSmallIcon(R.drawable.ic_intruder_selfie)
            .setOngoing(true)
            .addAction(R.drawable.ic_stop,
                getString(R.string.stop),
                stopPendingIntent)
            .build();
    }
}
```

#### File 2: `src/com/android/settings/intruder/CameraCaptureHelper.java`

**Purpose**: Helper class for camera operations

**Key Components**:
- Uses Camera2 API for front camera access
- Handles camera permissions
- Captures photos silently
- Saves with timestamp filename

**Key Methods**:
```java
public class CameraCaptureHelper {
    private Context mContext;
    private CameraManager mCameraManager;
    private String mFrontCameraId;
    
    public CameraCaptureHelper(Context context) {
        mContext = context;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mFrontCameraId = findFrontCameraId();
    }
    
    public void captureFrontCameraPhoto(CaptureCallback callback) {
        if (mFrontCameraId == null) {
            callback.onCaptureError("Front camera not available");
            return;
        }
        
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mFrontCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            
            // Create ImageReader for capturing
            ImageReader imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    if (image != null) {
                        saveImageToFile(image, callback);
                        image.close();
                    }
                }
            }, null);
            
            // Open camera and capture
            mCameraManager.openCamera(mFrontCameraId, mStateCallback, null);
            // ... capture logic
            
        } catch (CameraAccessException e) {
            callback.onCaptureError("Camera access error: " + e.getMessage());
        }
    }
    
    private String findFrontCameraId() {
        try {
            String[] cameraIds = mCameraManager.getCameraIdList();
            for (String id : cameraIds) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    return id;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error finding front camera", e);
        }
        return null;
    }
    
    private void saveImageToFile(Image image, CaptureCallback callback) {
        // Convert Image to JPEG and save with timestamp
        String filename = "intruder_" + System.currentTimeMillis() + ".jpg";
        File photoFile = new File(mPhotoStorageDir, filename);
        // ... save logic
        callback.onCaptureSuccess(photoFile);
    }
    
    public interface CaptureCallback {
        void onCaptureSuccess(File photoFile);
        void onCaptureError(String error);
    }
}
```

#### File 3: `src/com/android/settings/intruder/IntruderSelfieBootReceiver.java`

**Purpose**: Start service automatically after device boot

**Key Components**:
- BroadcastReceiver for `ACTION_BOOT_COMPLETED`
- Starts IntruderSelfieService if enabled

**Implementation**:
```java
public class IntruderSelfieBootReceiver extends BroadcastReceiver {
    private static final String TAG = "IntruderSelfieBootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Check if feature is enabled
            boolean enabled = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.INTRUDER_SELFIE_ENABLED, 0) == 1;
            
            if (enabled) {
                Intent serviceIntent = new Intent(context, IntruderSelfieService.class);
                serviceIntent.setAction(IntruderSelfieService.ACTION_START_MONITORING);
                context.startForegroundService(serviceIntent);
            }
        }
    }
}
```

#### File 4: `src/com/android/settings/intruder/IntruderSelfieController.java`

**Purpose**: Preference controller for Intruder Selfie toggle

**Key Components**:
- Extends `AbstractPreferenceController`
- Manages service lifecycle
- Checks camera permissions
- Updates UI state

**Key Methods**:
```java
public class IntruderSelfieController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {
    
    private static final String KEY_INTRUDER_SELFIE = "intruder_selfie_toggle";
    private SwitchPreferenceCompat mPreference;
    private Context mContext;
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        
        // Save preference
        Settings.Secure.putInt(mContext.getContentResolver(),
            Settings.Secure.INTRUDER_SELFIE_ENABLED,
            enabled ? 1 : 0);
        
        if (enabled) {
            startMonitoring();
        } else {
            stopMonitoring();
        }
        return true;
    }
    
    private void startMonitoring() {
        // Check camera permission
        if (!checkCameraPermission()) {
            requestCameraPermission();
            return;
        }
        
        // Start service
        Intent serviceIntent = new Intent(mContext, IntruderSelfieService.class);
        serviceIntent.setAction(IntruderSelfieService.ACTION_START_MONITORING);
        mContext.startForegroundService(serviceIntent);
    }
    
    private void stopMonitoring() {
        Intent serviceIntent = new Intent(mContext, IntruderSelfieService.class);
        serviceIntent.setAction(IntruderSelfieService.ACTION_STOP_MONITORING);
        mContext.startService(serviceIntent);
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(mContext,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        // Request camera permission
        ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.CAMERA},
            REQUEST_CAMERA_PERMISSION);
    }
    
    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (mPreference != null) {
            boolean enabled = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.INTRUDER_SELFIE_ENABLED, 0) == 1;
            mPreference.setChecked(enabled);
        }
    }
    
    @Override
    public int getAvailabilityStatus() {
        // Check if device has front camera
        return hasFrontCamera() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }
    
    private boolean hasFrontCamera() {
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    return true;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error checking cameras", e);
        }
        return false;
    }
}
```

#### File 5: `src/com/android/settings/intruder/IntruderSelfieSettingsFragment.java`

**Purpose**: Main Settings UI fragment

**Key Components**:
- Extends `SettingsPreferenceFragment`
- Displays toggle, photo count, photo gallery
- Handles permission requests
- Shows captured photos

**Key Methods**:
```java
public class IntruderSelfieSettingsFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    
    private static final String TAG = "IntruderSelfieSettings";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    
    private IntruderSelfieController mController;
    private Preference mPhotoCountPreference;
    private PreferenceCategory mPhotoGalleryCategory;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.intruder_selfie_settings);
        
        mController = new IntruderSelfieController(getContext(), getSettingsLifecycle());
        
        mPhotoCountPreference = findPreference("intruder_photo_count");
        mPhotoGalleryCategory = findPreference("intruder_photo_gallery");
        
        updatePhotoCount();
        loadPhotoGallery();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updatePhotoCount();
        loadPhotoGallery();
    }
    
    private void updatePhotoCount() {
        int count = getPhotoCount();
        if (mPhotoCountPreference != null) {
            mPhotoCountPreference.setSummary(getString(R.string.intruder_photo_count_summary, count));
        }
    }
    
    private void loadPhotoGallery() {
        if (mPhotoGalleryCategory == null) return;
        
        mPhotoGalleryCategory.removeAll();
        
        File[] photos = getPhotoStorageDirectory().listFiles();
        if (photos != null && photos.length > 0) {
            // Sort by date (newest first)
            Arrays.sort(photos, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            
            // Show last 20 photos
            int maxPhotos = Math.min(20, photos.length);
            for (int i = 0; i < maxPhotos; i++) {
                File photo = photos[i];
                Preference photoPreference = createPhotoPreference(photo);
                mPhotoGalleryCategory.addPreference(photoPreference);
            }
        }
    }
    
    private Preference createPhotoPreference(File photoFile) {
        Preference preference = new Preference(getContext());
        preference.setTitle(formatTimestamp(photoFile.lastModified()));
        preference.setSummary(photoFile.getName());
        preference.setIcon(R.drawable.ic_photo);
        
        // Set photo thumbnail if possible
        // ... thumbnail loading logic
        
        preference.setOnPreferenceClickListener(p -> {
            // Open photo viewer
            openPhotoViewer(photoFile);
            return true;
        });
        
        preference.setOnPreferenceLongClickListener(p -> {
            // Show delete dialog
            showDeletePhotoDialog(photoFile);
            return true;
        });
        
        return preference;
    }
    
    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
```

#### File 6: `res/xml/intruder_selfie_settings.xml`

**Purpose**: Preference screen layout

**Content**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    
    <SwitchPreferenceCompat
        android:key="intruder_selfie_toggle"
        android:title="@string/intruder_selfie_title"
        android:summary="@string/intruder_selfie_summary"
        android:icon="@drawable/ic_intruder_selfie"
        app:useSimpleSummaryProvider="true" />
    
    <Preference
        android:key="intruder_photo_count"
        android:title="@string/intruder_photo_count_title"
        android:summary="@string/intruder_photo_count_summary"
        android:selectable="false" />
    
    <Preference
        android:key="intruder_clear_all"
        android:title="@string/intruder_clear_all_title"
        android:summary="@string/intruder_clear_all_summary"
        android:icon="@drawable/ic_delete" />
    
    <PreferenceCategory
        android:key="intruder_photo_gallery"
        android:title="@string/intruder_photo_gallery_title">
        <!-- Photos will be added dynamically -->
    </PreferenceCategory>
    
    <PreferenceCategory
        android:title="@string/intruder_info_category">
        <Preference
            android:key="intruder_info"
            android:summary="@string/intruder_info_summary"
            android:selectable="false" />
    </PreferenceCategory>
</PreferenceScreen>
```

#### File 7: `res/values/intruder_selfie_strings.xml`

**Purpose**: String resources for Intruder Selfie feature

**Content**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="intruder_selfie_settings_title">Intruder Selfie</string>
    <string name="intruder_selfie_settings_summary">Capture photos on screen unlock</string>
    <string name="intruder_selfie_title">Enable Intruder Selfie</string>
    <string name="intruder_selfie_summary">Take photo with front camera when screen is unlocked</string>
    <string name="intruder_photo_count_title">Captured Photos</string>
    <string name="intruder_photo_count_summary">%d photos</string>
    <string name="intruder_clear_all_title">Clear All Photos</string>
    <string name="intruder_clear_all_summary">Delete all captured photos</string>
    <string name="intruder_photo_gallery_title">Photo Gallery</string>
    <string name="intruder_info_category">Information</string>
    <string name="intruder_info_summary">Intruder Selfie silently captures a photo with the front camera every time your screen is unlocked. Photos are stored locally and never uploaded anywhere.</string>
    <string name="intruder_notification_title">Watching for unlocks…</string>
    <string name="intruder_notification_text">Intruder Selfie is active</string>
    <string name="intruder_camera_permission_required">Camera permission required</string>
    <string name="intruder_no_front_camera">Front camera not available</string>
    <string name="intruder_delete_photo">Delete photo?</string>
    <string name="intruder_delete_all_photos">Delete all photos?</string>
    <string name="intruder_photo_deleted">Photo deleted</string>
    <string name="intruder_all_photos_deleted">All photos deleted</string>
</resources>
```

#### File 8: `res/values/intruder_selfie_dimens.xml`

**Purpose**: Dimension resources

**Content**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <dimen name="intruder_photo_thumbnail_size">64dp</dimen>
    <dimen name="intruder_photo_spacing">8dp</dimen>
    <dimen name="intruder_gallery_max_height">400dp</dimen>
</resources>
```

#### File 9: `res/values/intruder_selfie_colors.xml`

**Purpose**: Color resources

**Content**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="intruder_notification_color">#FF5722</color>
    <color name="intruder_photo_border">#CCCCCC</color>
</resources>
```

#### File 10: `res/drawable/ic_intruder_selfie.xml`

**Purpose**: Icon drawable for Intruder Selfie

**Content**: (Vector drawable for camera/security icon)

#### File 11: `res/xml/anatolia.xml` (Modify)

**Purpose**: Add Intruder Selfie preference entry to Anatolia settings

**Location**: After line 134 (before closing `</PreferenceScreen>` tag)

**Add**:
```xml
        <!-- Intruder Selfie -->
        <PreferenceScreen
            android:key="intruder_selfie_category"
            android:title="@string/intruder_selfie_settings_title"
            android:summary="@string/intruder_selfie_settings_summary"
            android:fragment="com.android.settings.intruder.IntruderSelfieSettingsFragment" />
```

#### File 12: `AndroidManifest.xml` (Modify)

**Purpose**: Add service and receiver declarations

**Location**: Inside `<application>` tag, before closing `</application>` tag

**Add**:
```xml
        <!-- Intruder Selfie Service -->
        <service
            android:name="com.android.settings.intruder.IntruderSelfieService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="camera"
            android:permission="android.permission.BIND_FOREGROUND_SERVICE" />
        
        <!-- Intruder Selfie Boot Receiver -->
        <receiver
            android:name="com.android.settings.intruder.IntruderSelfieBootReceiver"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.RECEIVE_BOOT_COMPLETED">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>
```

**Note**: `foregroundServiceType="camera"` is required for Android 14+ when using camera in foreground service.

## Technical Implementation Details

### Screen Unlock Detection

**Method 1: ACTION_USER_PRESENT Broadcast**
```java
IntentFilter filter = new IntentFilter();
filter.addAction(Intent.ACTION_USER_PRESENT);
registerReceiver(mUnlockReceiver, filter);
```

**Method 2: KeyguardManager (Alternative)**
```java
KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
keyguardManager.addKeyguardLockedStateListener(executor, listener);
```

**Preference**: Use `ACTION_USER_PRESENT` - simpler, more reliable

### Camera Capture Implementation

**Camera2 API Usage**:
```java
// Find front camera
CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
String[] cameraIds = cameraManager.getCameraIdList();
for (String id : cameraIds) {
    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
    Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
    if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
        mFrontCameraId = id;
        break;
    }
}

// Capture photo
ImageReader imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1);
CameraDevice cameraDevice = ...;
CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
captureBuilder.addTarget(imageReader.getSurface());
cameraDevice.createCaptureSession(...);
```

### Photo Storage

**Location**: App-specific external storage
```java
File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IntruderSelfie");
```

**Filename Format**: `intruder_<timestamp>.jpg`
- Example: `intruder_1704067200000.jpg`

**Privacy**: Photos stored in app-specific directory, not accessible by other apps without root

### Notification Channel

**Implementation**:
```java
private String createNotificationChannel() {
    String channelId = "intruder_selfie_channel";
    NotificationChannel channel = new NotificationChannel(channelId,
        getString(R.string.intruder_notification_channel_name),
        NotificationManager.IMPORTANCE_LOW);
    channel.setDescription(getString(R.string.intruder_notification_channel_description));
    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(channel);
    return channelId;
}
```

## Testing Checklist

- [ ] **Screen unlock detection**: Correctly detects when screen is unlocked
- [ ] **Photo capture**: Actually takes photos with front camera
- [ ] **Silent capture**: No preview, no sound, no flash
- [ ] **Timestamp**: Photos have correct timestamps in filename
- [ ] **Service persistence**: Service survives app backgrounding
- [ ] **Boot receiver**: Service starts automatically after reboot
- [ ] **Permission handling**: Camera permission requests work correctly
- [ ] **Photo storage**: Photos saved to correct location
- [ ] **Photo gallery**: Photos display correctly in Settings UI
- [ ] **Delete functionality**: Delete individual and all photos works
- [ ] **Notification**: Persistent notification displays correctly
- [ ] **Toggle functionality**: Enable/disable works correctly
- [ ] **Front camera detection**: Correctly identifies front camera
- [ ] **Error handling**: Handles camera unavailable, permission denied, etc.

## Dependencies

- Android Camera2 API (`CameraManager`, `CameraDevice`)
- Settings app framework (already present)
- ForegroundService support (Android 8.0+)
- BroadcastReceiver for screen unlock detection
- File storage APIs (`getExternalFilesDir`)

## Notes

- MVP focuses on core functionality: detect unlock, capture photo, save locally
- Uses existing Android APIs (Camera2, BroadcastReceiver)
- No SystemUI modifications required
- Clean, maintainable code following LineageOS conventions
- Privacy-first design (local storage only, no cloud)
- Works completely offline
- Lightweight foreground service (minimal battery impact)

## Implementation Order

1. **Framework Changes** (frameworks/base) - Optional
   - Add Settings.Secure keys (if using Settings for state)
   - Commit: `intruder: Selfie`

2. **Settings App Changes** (packages/apps/Settings)
   - Create `IntruderSelfieService.java`
   - Create `CameraCaptureHelper.java`
   - Create `IntruderSelfieBootReceiver.java`
   - Create `IntruderSelfieController.java`
   - Create `IntruderSelfieSettingsFragment.java`
   - Create XML layouts and strings
   - Create dimens, colors, drawables
   - Update `AndroidManifest.xml`
   - Update `anatolia.xml`
   - Commit: `intruder: Selfie`

## Future Enhancements

1. **Photo Viewer**: Full-screen photo viewer in Settings
2. **Photo Sharing**: Share photos via other apps
3. **Settings**: Configure photo quality, resolution
4. **Filters**: Filter photos by date range
5. **Export**: Export all photos to external storage
6. **Face Detection**: Optional face detection to reduce false positives

## Resource Organization (Portable Design)

### Separate Resource Files for Portability
All resources are isolated in dedicated files to enable easy porting:

1. **Strings**: `res/values/intruder_selfie_strings.xml`
   - All UI text, titles, summaries
   - Notification strings
   - Error messages

2. **Dimens**: `res/values/intruder_selfie_dimens.xml`
   - Layout dimensions
   - Photo thumbnail sizes
   - Spacing values

3. **Colors**: `res/values/intruder_selfie_colors.xml`
   - Theme colors
   - Notification colors
   - Photo border colors

4. **Drawables**: `res/drawable/intruder_selfie_*.xml`
   - `ic_intruder_selfie.xml` - Main icon
   - `ic_intruder_notification.xml` - Notification icon
   - `ic_photo.xml` - Photo gallery icon
   - `ic_delete.xml` - Delete icon

### Why Separate Resources?
- **Portability**: Easy to extract and move to other ROMs
- **Maintainability**: Clear separation of concerns
- **No Conflicts**: Avoids conflicts with existing Settings resources
- **Clean Organization**: Follows Android best practices

## Key Design Principles

1. **Portability**: All resources isolated, easy to extract
2. **Minimal Framework Changes**: Only Settings keys if needed (can use SharedPreferences)
3. **Functional**: Actually captures photos, not just visual
4. **Privacy-First**: Local storage only, no cloud, no tracking
5. **Offline**: No internet required
6. **Clean Architecture**: Follows Android and LineageOS patterns
7. **Testable**: Launched from Anatolia for easy testing
8. **Lightweight**: Minimal battery impact, efficient camera usage

