# Autofill Service Feature - Plan Summary

## Feature Overview

### What It Does
The Autofill Service feature provides **automatic password/credential filling** into apps, similar to KeePassDX's autofill functionality. When activated:

1. **Detects Input Fields**: Automatically detects username/password fields in apps
2. **Shows Suggestions**: Displays credential suggestions when user taps input fields
3. **Fills Credentials**: Fills username/password automatically when selected
4. **Saves Credentials**: Saves new credentials securely when user logs in
5. **Manages Credentials**: UI for adding/editing/deleting saved credentials

### Key Characteristics
- **System Integration**: Uses Android's built-in Autofill Framework
- **Secure Storage**: Credentials encrypted using Android Keystore
- **Automatic Detection**: Works with apps that support autofill hints
- **User-Friendly**: Simple tap-to-fill interface
- **Privacy-First**: Local-only storage, no cloud, no tracking
- **Functional**: Actually fills credentials (not just visual)

## Launch Location

### Navigation Path
```
Settings App 
  → Anatolia Settings (custom settings page)
    → Autofill Service (preference entry)
      → Autofill Settings Screen (main feature UI)
```

### Entry Point Details
- **File**: `res/xml/anatolia.xml`
- **Preference Key**: `autofill_category`
- **Title**: "Autofill Service"
- **Summary**: "Fill passwords automatically"
- **Fragment**: `com.android.settings.autofill.AutofillSettingsFragment`

### Why Anatolia Settings?
- Custom settings area for testing new features
- Easy to enable/disable during development
- Can be moved to main Settings later if desired
- Follows existing pattern in codebase

## User Experience Flow

### Enabling Autofill Service

1. **Access**: User opens Settings → Anatolia Settings → Autofill Service
2. **View**: Sees Autofill Settings screen with:
   - Toggle switch: "Enable Autofill Service"
   - List: Saved credentials grouped by app
   - Button: "Add Credential"
   - Status: Service status indicator
3. **Enable**: Toggles "Enable Autofill Service" switch ON
4. **System Prompt**: Android shows "Set Autofill Service" dialog
5. **Select**: User selects "LineageOS Autofill Service"
6. **Enabled**: Service now active and ready

### Using Autofill (In Any App)

1. **Open App**: User opens any app with login form
2. **Tap Field**: User taps username or password field
3. **Suggestions Appear**: Autofill suggestions appear above keyboard
   - Shows app icon, app name, username
   - Multiple credentials if multiple saved
4. **Select Credential**: User taps desired credential suggestion
5. **Authenticate**: Biometric prompt appears (fingerprint/face)
6. **Fill**: Credentials automatically fill into username and password fields
7. **Login**: User can now login without typing

### Saving New Credentials

1. **Login**: User logs into app with new credentials
2. **System Detects**: Android detects successful login
3. **Save Prompt**: "Save password for [App Name]?" prompt appears
4. **Save**: User taps "Save"
5. **Stored**: Credentials encrypted and stored securely
6. **Available**: Credentials now available for future autofill

### Managing Credentials

1. **Access**: Settings → Anatolia Settings → Autofill Service
2. **View List**: See all saved credentials grouped by app
3. **Edit Credential**: Tap credential to edit username/password
4. **Delete Credential**: Long-press credential to delete
5. **Add Manually**: Tap "Add Credential" to add credentials manually

## Technical Architecture

### Components

1. **LineageAutofillService.java** (AutofillService)
   - Responds to autofill requests from Android system
   - Provides FillResponse with credentials
   - Handles save requests for new credentials
   - Manages service lifecycle

2. **CredentialDatabase.java** (SQLite)
   - Stores encrypted credential metadata
   - Queries credentials by package/app
   - Manages credential CRUD operations

3. **CredentialEncryptionHelper.java** (Helper)
   - Encrypts credentials using Android Keystore
   - Decrypts credentials on-demand
   - Manages encryption keys

4. **AutofillSettingsFragment.java** (UI Fragment)
   - Displays settings screen
   - Shows saved credentials list
   - Handles add/edit/delete operations
   - Manages service enable/disable

5. **AutofillAuthActivity.java** (Activity)
   - Handles authentication before filling
   - Shows biometric prompt
   - Verifies user identity

### Resource Organization (Portable Design)

All resources are isolated in separate files for easy porting:

- **Strings**: `res/values/autofill_strings.xml`
- **Dimens**: `res/values/autofill_dimens.xml`
- **Colors**: `res/values/autofill_colors.xml`
- **Drawables**: `res/drawable/autofill_*.xml`
- **Layouts**: `res/xml/autofill_settings.xml`, `res/layout/autofill_credential_suggestion.xml`

### Framework Changes (Minimal)

**Optional Change**: Add 1 Settings.Secure key to `frameworks/base/core/java/android/provider/Settings.java`:
- `AUTOFILL_SERVICE_ENABLED` - Toggle state

**Note**: Android already has `Settings.Secure.AUTOFILL_SERVICE` for storing selected service.

**No Other Framework Changes**:
- No SystemUI modifications
- No system service creation
- Uses existing Android Autofill Framework
- Uses existing AutofillService API
- Uses existing AutofillManager API

## Functional Requirements (Must Work)

### Core Functionality
✅ **Autofill Detection**: Actually detects autofill fields in apps
✅ **Credential Suggestions**: Shows credential suggestions when fields tapped
✅ **Credential Filling**: Actually fills credentials into app fields
✅ **Credential Saving**: Saves new credentials from apps
✅ **Credential Storage**: Stores credentials encrypted securely
✅ **Credential Management**: UI for managing saved credentials
✅ **Authentication**: Requires authentication before filling

### Not Just Visual
- Service actually responds to autofill requests
- Credentials actually fill into app fields
- Credentials actually saved when user saves
- Database stores actual encrypted credentials
- UI reflects real credential state

## Implementation Status

- ✅ **Plan Created**: Complete with all details
- ⏳ **Framework Changes**: Pending (optional - Android already has AUTOFILL_SERVICE)
- ⏳ **Settings Implementation**: Pending (Service, Fragment, Database, Helper, Auth Activity)
- ⏳ **Resources**: Pending (strings, dimens, colors, drawables, layouts)
- ⏳ **Testing**: Pending

## Files Created/Modified

### Framework (Commit: `autofill: Service`) - Optional
- `frameworks/base/core/java/android/provider/Settings.java` - Add 1 key (or use existing AUTOFILL_SERVICE)

### Settings App (Commit: `autofill: Service`)
**Created**:
- `src/com/android/settings/autofill/LineageAutofillService.java`
- `src/com/android/settings/autofill/CredentialDatabase.java`
- `src/com/android/settings/autofill/CredentialEncryptionHelper.java`
- `src/com/android/settings/autofill/AutofillSettingsFragment.java`
- `src/com/android/settings/autofill/AutofillAuthActivity.java`
- `src/com/android/settings/autofill/Credential.java` (Data class)
- `res/xml/autofill_settings.xml`
- `res/xml/autofill_service.xml`
- `res/layout/autofill_credential_suggestion.xml`
- `res/values/autofill_strings.xml`
- `res/values/autofill_dimens.xml`
- `res/values/autofill_colors.xml`
- `res/drawable/ic_autofill.xml`
- `res/drawable/ic_lock.xml`

**Modified**:
- `res/xml/anatolia.xml` - Add Autofill preference entry
- `AndroidManifest.xml` - Add service and activity declarations

## Design Principles Followed

1. **Portability**: All resources isolated, easy to extract
2. **Minimal Framework Changes**: Uses existing Android Autofill Framework
3. **Functional**: Actually fills credentials, not just visual
4. **Privacy-First**: Local-only operation, no cloud, no tracking
5. **Security-Focused**: Uses Android Keystore, encrypted storage
6. **Clean Architecture**: Follows Android and LineageOS patterns
7. **Testable**: Launched from Anatolia for easy testing
8. **User-Friendly**: Simple tap-to-fill interface

## Key Implementation Details

### Android Autofill Framework
- Uses `AutofillService` API (Android 8.0+)
- Responds to `onFillRequest()` for providing credentials
- Handles `onSaveRequest()` for saving new credentials
- Integrates with `AutofillManager` system service

### Credential Storage
- SQLite database for credential metadata
- Android Keystore for encryption keys
- AES-256-GCM encryption for credentials
- Local-only storage (app-specific directory)

### Field Detection
- Uses `android:autofillHints` from apps
- Detects `username` and `password` hints
- Supports standard autofill hints
- Works with apps that properly implement autofill

### Authentication
- Biometric authentication before filling
- Fingerprint/face unlock support
- PIN/pattern fallback
- Secure authentication flow

## Security Considerations

### Encryption Security
- Uses Android Keystore (hardware-backed when available)
- AES-256-GCM encryption (strong cipher)
- Keys never leave secure hardware
- IV unique per credential

### Storage Security
- Credentials encrypted at rest
- Database stores encrypted data only
- No plaintext credentials in storage
- Secure credential deletion

### Access Control
- Authentication required before filling
- Biometric verification
- Service runs with system permissions
- No cloud uploads

## Limitations & Considerations

### Limitations
1. **App Support**: Only works with apps that support autofill hints
2. **Field Detection**: May not detect all field types
3. **Complex Forms**: May struggle with complex multi-step forms
4. **Android Version**: Requires Android 8.0+ (API 26+)

### Considerations
1. **Privacy**: Credentials stored locally, encrypted
2. **Security**: Uses Android Keystore for encryption
3. **User Education**: Users need to understand how autofill works
4. **Compatibility**: Works with standard autofill implementations

## Future Enhancements

- **Passkey Support**: Support for WebAuthn passkeys
- **OTP Support**: One-time password generation (TOTP/HOTP)
- **Advanced Matching**: Better field detection and matching
- **Cloud Sync**: Optional encrypted cloud sync (future)
- **Import/Export**: Import credentials from other password managers
- **Password Generator**: Built-in password generator
- **Security Audit**: Password strength checking

