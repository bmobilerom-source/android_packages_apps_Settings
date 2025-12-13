# Intruder Selfie Feature - Plan Summary

## Feature Overview

### What It Does
The Intruder Selfie feature provides **automatic photo capture** using the front camera every time the screen is unlocked. When activated:

1. **Monitors Screen Unlocks**: Detects when screen is unlocked via BroadcastReceiver
2. **Captures Photos**: Silently takes a photo with the front camera on each unlock
3. **Saves Locally**: Stores photos with timestamps in app-specific storage
4. **Shows Persistent Notification**: Displays "Watching for unlocks..." notification
5. **Works After Boot**: Automatically starts after device reboot
6. **Completely Offline**: No internet required, no cloud uploads, no tracking

### Key Characteristics
- **Silent Operation**: No preview, no sound, no flash
- **Privacy-Focused**: Photos stored locally only, never uploaded
- **Lightweight**: Minimal battery impact, efficient camera usage
- **Functional**: Actually captures photos (not just visual)
- **Persistent**: Service survives app backgrounding and device reboot

## Launch Location

### Navigation Path
```
Settings App 
  → Anatolia Settings (custom settings page)
    → Intruder Selfie (preference entry)
      → Intruder Selfie Settings Screen (main feature UI)
```

### Entry Point Details
- **File**: `res/xml/anatolia.xml`
- **Preference Key**: `intruder_selfie_category`
- **Title**: "Intruder Selfie"
- **Summary**: "Capture photos on screen unlock"
- **Fragment**: `com.android.settings.intruder.IntruderSelfieSettingsFragment`

### Why Anatolia Settings?
- Custom settings area for testing new features
- Easy to enable/disable during development
- Can be moved to main Settings later if desired
- Follows existing pattern in codebase

## User Experience Flow

1. **Access**: User opens Settings → Anatolia Settings → Intruder Selfie
2. **View**: Sees Intruder Selfie Settings screen with:
   - Toggle switch: "Enable Intruder Selfie"
   - Photo count: "X photos captured"
   - Clear All button
   - Photo gallery (last 20 photos)
   - Information section
3. **Activate**: Toggles "Enable Intruder Selfie" switch ON
4. **System Checks**: 
   - Front camera availability
   - Camera permissions
   - Service requirements
5. **Activation**: If approved, service starts:
   - Foreground service begins monitoring
   - Notification appears: "Watching for unlocks..."
   - Service registers for unlock events
6. **Operation**: Each time screen is unlocked:
   - Service detects unlock event
   - Front camera captures photo silently
   - Photo saved with timestamp: `intruder_<timestamp>.jpg`
   - Photo count updates
7. **View Photos**: User can:
   - See photos in gallery within Settings
   - Tap photo to view full size
   - Long-press to delete individual photo
   - Use "Clear All" to delete all photos
8. **Deactivation**: User toggles switch OFF:
   - Service stops monitoring
   - Notification dismissed
   - No more photos captured

## Technical Architecture

### Components

1. **IntruderSelfieService.java** (ForegroundService)
   - Monitors screen unlock events
   - Triggers photo capture
   - Manages service lifecycle
   - Shows persistent notification

2. **CameraCaptureHelper.java** (Helper)
   - Handles camera operations
   - Uses Camera2 API
   - Finds front camera
   - Captures photos silently

3. **IntruderSelfieBootReceiver.java** (BroadcastReceiver)
   - Starts service after device boot
   - Checks if feature is enabled

4. **IntruderSelfieSettingsFragment.java** (UI Fragment)
   - Displays settings screen
   - Shows photo gallery
   - Handles user interactions
   - Requests permissions

5. **IntruderSelfieController.java** (Controller)
   - Manages service lifecycle
   - Checks camera availability
   - Handles permission requests
   - Updates UI state

### Resource Organization (Portable Design)

All resources are isolated in separate files for easy porting:

- **Strings**: `res/values/intruder_selfie_strings.xml`
- **Dimens**: `res/values/intruder_selfie_dimens.xml`
- **Colors**: `res/values/intruder_selfie_colors.xml`
- **Drawables**: `res/drawable/intruder_selfie_*.xml`
- **Layouts**: `res/xml/intruder_selfie_settings.xml`

### Framework Changes (Minimal)

**Optional Change**: Add 2 Settings.Secure keys to `frameworks/base/core/java/android/provider/Settings.java`:
- `INTRUDER_SELFIE_ENABLED` - Toggle state
- `INTRUDER_SELFIE_LAST_CAPTURE` - Last capture timestamp

**Alternative**: Can use SharedPreferences instead to avoid framework changes entirely.

**No Other Framework Changes**:
- No SystemUI modifications
- No system service creation
- Uses existing Android Camera2 API
- Uses BroadcastReceiver for unlock detection
- Uses ForegroundService pattern

## Functional Requirements (Must Work)

### Core Functionality
✅ **Screen Unlock Detection**: Actually detects when screen is unlocked
✅ **Photo Capture**: Actually takes photos with front camera
✅ **Silent Capture**: No preview, no sound, no flash
✅ **Photo Storage**: Photos saved to app-specific storage with timestamps
✅ **Service Persistence**: Service survives app backgrounding
✅ **Boot Start**: Service starts automatically after reboot
✅ **Camera Integration**: Correctly identifies and uses front camera
✅ **Permission Handling**: Requests and handles camera permissions
✅ **Photo Gallery**: Displays captured photos in Settings UI
✅ **Photo Management**: Delete individual and all photos works

### Not Just Visual
- Service performs real unlock detection
- Camera actually captures photos
- Photos are actually saved to storage
- Notification shows service is actually running
- Settings toggle controls real functionality
- Photo gallery shows actual captured photos

## Implementation Status

- ✅ **Plan Created**: Complete with all details
- ⏳ **Framework Changes**: Pending (optional - can use SharedPreferences)
- ⏳ **Settings Implementation**: Pending (Service, Fragment, Controller, Helper)
- ⏳ **Resources**: Pending (strings, dimens, colors, drawables)
- ⏳ **Testing**: Pending

## Files Created/Modified

### Framework (Commit 1: `intruder: Selfie`) - Optional
- `frameworks/base/core/java/android/provider/Settings.java` - Add 2 keys (or use SharedPreferences)

### Settings App (Commit 2: `intruder: Selfie`)
**Created**:
- `src/com/android/settings/intruder/IntruderSelfieService.java`
- `src/com/android/settings/intruder/CameraCaptureHelper.java`
- `src/com/android/settings/intruder/IntruderSelfieBootReceiver.java`
- `src/com/android/settings/intruder/IntruderSelfieController.java`
- `src/com/android/settings/intruder/IntruderSelfieSettingsFragment.java`
- `res/xml/intruder_selfie_settings.xml`
- `res/values/intruder_selfie_strings.xml`
- `res/values/intruder_selfie_dimens.xml`
- `res/values/intruder_selfie_colors.xml`
- `res/drawable/ic_intruder_selfie.xml`
- `res/drawable/ic_intruder_notification.xml`
- `res/drawable/ic_photo.xml`
- `res/drawable/ic_delete.xml`

**Modified**:
- `res/xml/anatolia.xml` - Add Intruder Selfie preference entry
- `AndroidManifest.xml` - Add service and receiver declarations

## Design Principles Followed

1. **Portability**: All resources isolated, easy to extract
2. **Minimal Framework Changes**: Can use SharedPreferences instead of Settings keys
3. **Functional**: Actually captures photos, not just visual
4. **Privacy-First**: Local storage only, no cloud, no tracking
5. **Offline**: No internet required
6. **Clean Architecture**: Follows Android and LineageOS patterns
7. **Testable**: Launched from Anatolia for easy testing
8. **Lightweight**: Minimal battery impact, efficient camera usage
9. **Silent Operation**: No preview, sound, or flash during capture

## Key Implementation Details

### Screen Unlock Detection
- Uses `Intent.ACTION_USER_PRESENT` BroadcastReceiver
- Fired when user successfully unlocks device
- More reliable than `ACTION_SCREEN_ON` (fires even if locked)

### Camera Capture
- Uses Camera2 API (modern, efficient)
- Finds front camera via `LENS_FACING_FRONT`
- Silent capture: no preview window, no sound, no flash
- Saves as JPEG with timestamp filename

### Photo Storage
- Location: `getExternalFilesDir(Environment.DIRECTORY_PICTURES)/IntruderSelfie/`
- Filename: `intruder_<timestamp>.jpg`
- Privacy: App-specific storage, not accessible by other apps

### Foreground Service
- Type: `foregroundServiceType="camera"` (required for Android 14+)
- Notification: Persistent "Watching for unlocks..." notification
- Lifecycle: Starts on boot if enabled, survives app backgrounding

## Future Enhancements (Foundation Laid)

- **Photo Viewer**: Full-screen photo viewer in Settings
- **Photo Sharing**: Share photos via other apps
- **Settings**: Configure photo quality, resolution
- **Filters**: Filter photos by date range
- **Export**: Export all photos to external storage
- **Face Detection**: Optional face detection to reduce false positives

