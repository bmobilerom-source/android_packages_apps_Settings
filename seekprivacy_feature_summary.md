# SeekPrivacy File Protection Feature - Plan Summary

## Feature Overview

### What It Does
The SeekPrivacy File Protection feature provides **file encryption and cloaking** to protect private files from other apps, even those with "All Files Access" permission. When activated:

1. **Protects Files**: User selects files to protect via file picker
2. **Encrypts Files**: Files encrypted using Android Keystore (AES-256-GCM)
3. **Cloaks Files**: Protected files become invisible to other apps
4. **Normal Access**: Owner can still access files normally through Settings
5. **Deep Security**: Files encrypted at rest, protected from physical access

### Key Characteristics
- **Not a Vault**: Files stay protected in their original concept, but encrypted
- **Active Cloaking**: Files hidden from other apps via encryption + separate storage
- **Encryption-Based**: Files encrypted using Android Keystore
- **Seamless UX**: Owner access remains normal through Settings
- **Privacy-First**: No cloud, no tracking, local-only operation
- **Functional**: Actually encrypts/decrypts files (not just visual)

## Launch Location

### Navigation Path
```
Settings App 
  → Anatolia Settings (custom settings page)
    → File Protection (preference entry)
      → SeekPrivacy Settings Screen (main feature UI)
```

### Entry Point Details
- **File**: `res/xml/anatolia.xml`
- **Preference Key**: `seekprivacy_category`
- **Title**: "File Protection"
- **Summary**: "Protect files from other apps"
- **Fragment**: `com.android.settings.seekprivacy.SeekPrivacySettingsFragment`

### Why Anatolia Settings?
- Custom settings area for testing new features
- Easy to enable/disable during development
- Can be moved to main Settings later if desired
- Follows existing pattern in codebase

## User Experience Flow

1. **Access**: User opens Settings → Anatolia Settings → File Protection
2. **View**: Sees SeekPrivacy Settings screen with:
   - Toggle switch: "Enable File Protection"
   - Button: "Protect File" (opens file picker)
   - List: Protected files with access buttons
   - Status: "X files protected"
   - Information section
3. **Activate**: Toggles "Enable File Protection" switch ON
4. **System Checks**: 
   - Storage permissions
   - Android Keystore availability
   - Service requirements
5. **Protect File**: User taps "Protect File":
   - File picker opens
   - User selects file to protect
   - Service encrypts file
   - Original file deleted
   - Encrypted file stored in app-specific storage
   - File added to protected list
6. **Operation**: Protected files:
   - Encrypted and stored securely
   - Invisible to other apps (even with MANAGE_EXTERNAL_STORAGE)
   - Owner can access via Settings
7. **Access File**: User taps protected file:
   - Service decrypts file temporarily
   - File opened with system intent
   - File re-encrypted after access (optional)
8. **Unprotect File**: User long-presses protected file:
   - Confirmation dialog appears
   - User confirms unprotection
   - Service decrypts file
   - File restored to original location
   - File removed from protected list
9. **Deactivation**: User toggles switch OFF:
   - Service stops
   - Files remain encrypted (user must unprotect manually)

## Technical Architecture

### Components

1. **SeekPrivacyService.java** (ForegroundService)
   - Manages file encryption/decryption
   - Handles cloaking operations
   - Shows persistent notification
   - Manages service lifecycle

2. **FileEncryptionHelper.java** (Helper)
   - Handles Android Keystore operations
   - Encrypts files using AES-256-GCM
   - Decrypts files on-demand
   - Manages encryption keys

3. **ProtectedFileDatabase.java** (SQLite)
   - Stores protected file metadata
   - Tracks file protection status
   - Provides queries for cloaking

4. **SeekPrivacySettingsFragment.java** (UI Fragment)
   - Displays settings screen
   - Shows protected files list
   - Handles file selection
   - Provides file access interface

5. **SeekPrivacyController.java** (Controller)
   - Manages protection state
   - Handles permission requests
   - Updates UI state

### Resource Organization (Portable Design)

All resources are isolated in separate files for easy porting:

- **Strings**: `res/values/seekprivacy_strings.xml`
- **Dimens**: `res/values/seekprivacy_dimens.xml`
- **Colors**: `res/values/seekprivacy_colors.xml`
- **Drawables**: `res/drawable/seekprivacy_*.xml`
- **Layouts**: `res/xml/seekprivacy_settings.xml`

### Framework Changes (Minimal)

**Optional Change**: Add 2 Settings.Secure keys to `frameworks/base/core/java/android/provider/Settings.java`:
- `SEEKPRIVACY_ENABLED` - Toggle state
- `SEEKPRIVACY_PROTECTED_COUNT` - Protected files count

**Alternative**: Can use SharedPreferences instead to avoid framework changes entirely.

**No Other Framework Changes**:
- No SystemUI modifications
- No system service creation
- Uses existing Android Keystore API
- Uses existing MediaStore API
- Uses ForegroundService pattern

## Functional Requirements (Must Work)

### Core Functionality
✅ **File Encryption**: Actually encrypts files using Android Keystore
✅ **File Decryption**: Actually decrypts files on-demand
✅ **File Cloaking**: Makes files invisible to other apps via encryption + separate storage
✅ **File Access**: Owner can access files normally through Settings
✅ **File Unprotection**: Removes protection and restores files
✅ **Service Persistence**: Service survives app backgrounding
✅ **Permission Handling**: Requests and handles storage permissions
✅ **Database Storage**: Stores protected file metadata securely

### Not Just Visual
- Files are actually encrypted (not just renamed)
- Encrypted files are unreadable by other apps
- Decryption actually works (files can be opened)
- Service performs real encryption/decryption operations
- Database stores actual file metadata
- UI reflects real protection status

## Implementation Status

- ✅ **Plan Created**: Complete with all details
- ⏳ **Framework Changes**: Pending (optional - can use SharedPreferences)
- ⏳ **Settings Implementation**: Pending (Service, Fragment, Controller, Helper, Database)
- ⏳ **Resources**: Pending (strings, dimens, colors, drawables)
- ⏳ **Testing**: Pending

## Files Created/Modified

### Framework (Commit 1: `seekprivacy: File Protection`) - Optional
- `frameworks/base/core/java/android/provider/Settings.java` - Add 2 keys (or use SharedPreferences)

### Settings App (Commit 2: `seekprivacy: File Protection`)
**Created**:
- `src/com/android/settings/seekprivacy/SeekPrivacyService.java`
- `src/com/android/settings/seekprivacy/FileEncryptionHelper.java`
- `src/com/android/settings/seekprivacy/ProtectedFileDatabase.java`
- `src/com/android/settings/seekprivacy/SeekPrivacyController.java`
- `src/com/android/settings/seekprivacy/SeekPrivacySettingsFragment.java`
- `src/com/android/settings/seekprivacy/ProtectedFile.java` (Data class)
- `res/xml/seekprivacy_settings.xml`
- `res/xml/seekprivacy_file_paths.xml`
- `res/values/seekprivacy_strings.xml`
- `res/values/seekprivacy_dimens.xml`
- `res/values/seekprivacy_colors.xml`
- `res/drawable/ic_seekprivacy.xml`
- `res/drawable/ic_file_protected.xml`
- `res/drawable/ic_add_file.xml`

**Modified**:
- `res/xml/anatolia.xml` - Add SeekPrivacy preference entry
- `AndroidManifest.xml` - Add service and provider declarations

## Design Principles Followed

1. **Portability**: All resources isolated, easy to extract
2. **Minimal Framework Changes**: Can use SharedPreferences instead of Settings keys
3. **Functional**: Actually encrypts/decrypts files, not just visual
4. **Privacy-First**: Local-only operation, no cloud, no tracking
5. **Security-Focused**: Uses Android Keystore, AES-256-GCM encryption
6. **Clean Architecture**: Follows Android and LineageOS patterns
7. **Testable**: Launched from Anatolia for easy testing
8. **User-Friendly**: Seamless access for owner, invisible to others

## Key Implementation Details

### File Encryption
- Uses Android Keystore (hardware-backed when available)
- AES-256-GCM encryption (strong cipher)
- Keys never leave secure hardware
- IV (Initialization Vector) unique per file

### File Cloaking
- Encrypted files stored in app-specific storage
- Original files deleted after encryption
- Other apps can't access encrypted files
- Files invisible to MediaStore queries (limited effectiveness)

### File Access
- Owner requests file access via Settings
- Service decrypts file temporarily
- File opened with system intent
- File re-encrypted after access (optional)

### Storage Structure
- Encrypted files: `app-specific-storage/SeekPrivacy/encrypted/`
- Temp files: `app-specific-storage/SeekPrivacy/temp/`
- Database: `app-specific-storage/SeekPrivacy/metadata/seekprivacy.db`

## Security Considerations

### Encryption Security
- Uses Android Keystore (hardware-backed when available)
- AES-256-GCM encryption (strong cipher)
- Keys never leave secure hardware
- IV unique per file

### Storage Security
- Encrypted files in app-specific storage
- Database encrypted (optional SQLCipher)
- No plaintext files in public storage
- Secure file deletion

### Access Control
- Only Settings app can decrypt files
- Requires device unlock (optional)
- No cloud uploads
- Local-only operation

## Limitations & Considerations

### Limitations
1. **File Size**: Large files may take time to encrypt/decrypt
2. **File Types**: Some file types may not work well (executables, system files)
3. **Cloaking Effectiveness**: Full cloaking against MANAGE_EXTERNAL_STORAGE is limited
4. **Performance**: Encryption/decryption adds overhead
5. **Storage**: Encrypted files take additional storage space

### Considerations
1. **Backup**: Protected files should be backed up securely
2. **Key Management**: Android Keystore handles key security
3. **File Recovery**: Provide way to recover files if app is uninstalled
4. **Battery Impact**: Encryption operations may impact battery
5. **User Education**: Users need to understand how protection works

## Future Enhancements

- **Batch Protection**: Protect multiple files at once
- **Folder Protection**: Protect entire folders
- **Auto-Protection**: Auto-protect files from specific apps
- **Cloud Backup**: Encrypted cloud backup option
- **File Sharing**: Share protected files securely
- **Advanced Cloaking**: Enhanced cloaking mechanisms
- **File Recovery**: Recovery mechanism for lost files
- **Performance Optimization**: Faster encryption/decryption

