# SeekPrivacy File Protection - Implementation Plan

## Executive Summary

This plan outlines how to integrate **SeekPrivacy**-style file protection into LineageOS Settings. The feature will encrypt and cloak private files, making them invisible to other apps (even those with MANAGE_EXTERNAL_STORAGE permission) while allowing normal access for the device owner through Settings.

**Key Features:**
- **File Encryption**: Encrypts designated files using Android Keystore
- **File Cloaking**: Makes protected files invisible to other apps
- **Seamless Access**: Owner can access files normally through Settings
- **Deep Security**: Files encrypted at rest, protected from physical access

---

## 1. Feature Overview

### What SeekPrivacy Does

**Core Functionality:**
1. **Protect Files**: User selects files to protect
2. **Encrypt Files**: Files are encrypted using Android Keystore
3. **Cloak Files**: Protected files become invisible to other apps
4. **Normal Access**: Owner can still open/view/share files normally
5. **Deep Security**: Encryption protects from physical access

**Key Characteristics:**
- Not a vault - files stay in their original locations
- Active cloaking - files hidden from other apps in real-time
- Encryption-based - files encrypted at rest
- Seamless UX - owner access remains normal
- Privacy-first - no cloud, no tracking, local only

### How It Works (Technical)

1. **File Selection**: User picks files to protect via file picker
2. **Encryption**: Files encrypted using AES-256-GCM via Android Keystore
3. **Metadata Storage**: Protected file list stored in encrypted database
4. **Cloaking**: MediaStore queries filtered to exclude protected files
5. **Decryption**: Files decrypted on-demand when owner accesses them
6. **Re-encryption**: Files re-encrypted after access

---

## 2. Architecture Design

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│ Settings App                                                │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ SeekPrivacySettingsFragment (UI)                      │ │
│ │ - File selection UI                                   │ │
│ │ - Protected files list                                │ │
│ │ - File access interface                              │ │
│ └───────────────────────────────────────────────────────┘ │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ SeekPrivacyService (ForegroundService)                │ │
│ │ - File encryption/decryption                         │ │
│ │ - Cloaking management                                │ │
│ │ - MediaStore filtering                               │ │
│ └───────────────────────────────────────────────────────┘ │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ FileEncryptionHelper (Helper)                         │ │
│ │ - Android Keystore operations                        │ │
│ │ - AES-256-GCM encryption                             │ │
│ │ - File I/O operations                                │ │
│ └───────────────────────────────────────────────────────┘ │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ ProtectedFileDatabase (SQLite)                        │ │
│ │ - Encrypted database                                 │ │
│ │ - Protected file metadata                           │ │
│ │ - Encryption keys mapping                            │ │
│ └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Component Breakdown

#### A. SeekPrivacyService (ForegroundService)

**Purpose**: Manages file encryption, decryption, and cloaking operations

**Responsibilities:**
- Encrypt files when added to protection
- Decrypt files when owner accesses them
- Manage cloaking state
- Handle MediaStore filtering
- Show persistent notification

**Location**: `src/com/android/settings/seekprivacy/SeekPrivacyService.java`

**Key Methods:**
```java
public class SeekPrivacyService extends ForegroundService {
    private static final String TAG = "SeekPrivacyService";
    private static final String ACTION_ENCRYPT_FILE = "com.android.settings.seekprivacy.ENCRYPT_FILE";
    private static final String ACTION_DECRYPT_FILE = "com.android.settings.seekprivacy.DECRYPT_FILE";
    private static final String ACTION_UNPROTECT_FILE = "com.android.settings.seekprivacy.UNPROTECT_FILE";
    
    private FileEncryptionHelper mEncryptionHelper;
    private ProtectedFileDatabase mDatabase;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mEncryptionHelper = new FileEncryptionHelper(this);
        mDatabase = new ProtectedFileDatabase(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);
        
        if (ACTION_ENCRYPT_FILE.equals(action)) {
            encryptFile(fileUri);
        } else if (ACTION_DECRYPT_FILE.equals(action)) {
            decryptFile(fileUri);
        } else if (ACTION_UNPROTECT_FILE.equals(action)) {
            unprotectFile(fileUri);
        }
        
        return START_STICKY;
    }
    
    private void encryptFile(Uri fileUri) {
        // Encrypt file and add to protected list
    }
    
    private void decryptFile(Uri fileUri) {
        // Decrypt file temporarily for access
    }
    
    private void unprotectFile(Uri fileUri) {
        // Decrypt and remove from protected list
    }
}
```

#### B. FileEncryptionHelper (Helper Class)

**Purpose**: Handles all encryption/decryption operations using Android Keystore

**Responsibilities:**
- Generate encryption keys via Android Keystore
- Encrypt files using AES-256-GCM
- Decrypt files on-demand
- Manage key lifecycle

**Location**: `src/com/android/settings/seekprivacy/FileEncryptionHelper.java`

**Key Methods:**
```java
public class FileEncryptionHelper {
    private static final String KEYSTORE_ALIAS = "SeekPrivacyKey";
    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    
    private Context mContext;
    private KeyStore mKeyStore;
    
    public FileEncryptionHelper(Context context) {
        mContext = context;
        initializeKeyStore();
    }
    
    private void initializeKeyStore() {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            mKeyStore.load(null);
            
            if (!mKeyStore.containsAlias(KEYSTORE_ALIAS)) {
                generateKey();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing keystore", e);
        }
    }
    
    private void generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM, "AndroidKeyStore");
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .build();
        keyGenerator.init(spec);
        keyGenerator.generateKey();
    }
    
    public byte[] encryptFile(File file) throws Exception {
        // Read file, encrypt using Keystore key, return encrypted bytes
    }
    
    public byte[] decryptFile(File encryptedFile) throws Exception {
        // Decrypt file using Keystore key, return decrypted bytes
    }
}
```

#### C. ProtectedFileDatabase (SQLite Database)

**Purpose**: Stores metadata about protected files

**Responsibilities:**
- Store protected file URIs and paths
- Store encryption metadata
- Track file protection status
- Provide queries for cloaking

**Location**: `src/com/android/settings/seekprivacy/ProtectedFileDatabase.java`

**Database Schema:**
```sql
CREATE TABLE protected_files (
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_uri TEXT NOT NULL UNIQUE,
    original_path TEXT NOT NULL,
    encrypted_path TEXT NOT NULL,
    file_name TEXT NOT NULL,
    file_size INTEGER NOT NULL,
    mime_type TEXT,
    date_protected INTEGER NOT NULL,
    date_accessed INTEGER,
    is_cloaked INTEGER DEFAULT 1
);
```

**Key Methods:**
```java
public class ProtectedFileDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "seekprivacy.db";
    private static final int DATABASE_VERSION = 1;
    
    public void addProtectedFile(Uri fileUri, String originalPath, String encryptedPath) {
        // Add file to protected list
    }
    
    public void removeProtectedFile(Uri fileUri) {
        // Remove file from protected list
    }
    
    public boolean isFileProtected(Uri fileUri) {
        // Check if file is protected
    }
    
    public List<ProtectedFile> getAllProtectedFiles() {
        // Get all protected files
    }
    
    public List<String> getProtectedFilePaths() {
        // Get all protected file paths for cloaking
    }
}
```

#### D. SeekPrivacySettingsFragment (UI Fragment)

**Purpose**: Main Settings UI for file protection

**Responsibilities:**
- Display protected files list
- Allow file selection for protection
- Provide file access interface
- Show protection status
- Handle unprotect operations

**Location**: `src/com/android/settings/seekprivacy/SeekPrivacySettingsFragment.java`

**UI Components:**
- Toggle: "Enable File Protection"
- Button: "Protect File" (opens file picker)
- List: Protected files with access buttons
- Info: Protection status and statistics

**Key Methods:**
```java
public class SeekPrivacySettingsFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    
    private static final int REQUEST_CODE_PICK_FILE = 1001;
    private static final int REQUEST_CODE_MANAGE_STORAGE = 1002;
    
    private SwitchPreferenceCompat mEnableToggle;
    private Preference mProtectFilePref;
    private PreferenceCategory mProtectedFilesCategory;
    private SeekPrivacyController mController;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.seekprivacy_settings);
        
        mController = new SeekPrivacyController(getContext());
        setupPreferences();
    }
    
    private void setupPreferences() {
        mEnableToggle = findPreference("seekprivacy_enable");
        mProtectFilePref = findPreference("seekprivacy_protect_file");
        mProtectedFilesCategory = findPreference("seekprivacy_protected_files_category");
        
        mEnableToggle.setOnPreferenceChangeListener(this);
        mProtectFilePref.setOnPreferenceClickListener(preference -> {
            requestFileSelection();
            return true;
        });
        
        updateProtectedFilesList();
    }
    
    private void requestFileSelection() {
        // Request MANAGE_EXTERNAL_STORAGE permission if needed
        if (Environment.isExternalStorageManager()) {
            launchFilePicker();
        } else {
            requestManageStoragePermission();
        }
    }
    
    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                protectFile(fileUri);
            }
        }
    }
    
    private void protectFile(Uri fileUri) {
        Intent serviceIntent = new Intent(getContext(), SeekPrivacyService.class);
        serviceIntent.setAction(SeekPrivacyService.ACTION_ENCRYPT_FILE);
        serviceIntent.putExtra(SeekPrivacyService.EXTRA_FILE_URI, fileUri);
        getContext().startForegroundService(serviceIntent);
        
        updateProtectedFilesList();
    }
    
    private void updateProtectedFilesList() {
        mProtectedFilesCategory.removeAll();
        
        List<ProtectedFile> protectedFiles = mController.getProtectedFiles();
        for (ProtectedFile file : protectedFiles) {
            Preference filePref = createFilePreference(file);
            mProtectedFilesCategory.addPreference(filePref);
        }
    }
    
    private Preference createFilePreference(ProtectedFile file) {
        Preference pref = new Preference(getContext());
        pref.setTitle(file.getFileName());
        pref.setSummary(formatFileSize(file.getFileSize()));
        pref.setIcon(R.drawable.ic_file_protected);
        
        pref.setOnPreferenceClickListener(preference -> {
            openProtectedFile(file);
            return true;
        });
        
        pref.setOnPreferenceLongClickListener(preference -> {
            showUnprotectDialog(file);
            return true;
        });
        
        return pref;
    }
    
    private void openProtectedFile(ProtectedFile file) {
        // Decrypt file temporarily and open with system intent
        Intent serviceIntent = new Intent(getContext(), SeekPrivacyService.class);
        serviceIntent.setAction(SeekPrivacyService.ACTION_DECRYPT_FILE);
        serviceIntent.putExtra(SeekPrivacyService.EXTRA_FILE_URI, file.getFileUri());
        getContext().startForegroundService(serviceIntent);
        
        // After decryption, open file
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(file.getFileUri(), file.getMimeType());
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(openIntent);
    }
    
    private void showUnprotectDialog(ProtectedFile file) {
        new AlertDialog.Builder(getContext())
            .setTitle(R.string.seekprivacy_unprotect_title)
            .setMessage(R.string.seekprivacy_unprotect_message)
            .setPositiveButton(R.string.unprotect, (dialog, which) -> {
                unprotectFile(file);
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }
    
    private void unprotectFile(ProtectedFile file) {
        Intent serviceIntent = new Intent(getContext(), SeekPrivacyService.class);
        serviceIntent.setAction(SeekPrivacyService.ACTION_UNPROTECT_FILE);
        serviceIntent.putExtra(SeekPrivacyService.EXTRA_FILE_URI, file.getFileUri());
        getContext().startForegroundService(serviceIntent);
        
        updateProtectedFilesList();
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mEnableToggle) {
            boolean enabled = (Boolean) newValue;
            Settings.Secure.putInt(getContext().getContentResolver(),
                Settings.Secure.SEEKPRIVACY_ENABLED, enabled ? 1 : 0);
            
            if (enabled) {
                startProtectionService();
            } else {
                stopProtectionService();
            }
            return true;
        }
        return false;
    }
    
    private void startProtectionService() {
        Intent serviceIntent = new Intent(getContext(), SeekPrivacyService.class);
        getContext().startForegroundService(serviceIntent);
    }
    
    private void stopProtectionService() {
        Intent serviceIntent = new Intent(getContext(), SeekPrivacyService.class);
        getContext().stopService(serviceIntent);
    }
}
```

#### E. SeekPrivacyController (Preference Controller)

**Purpose**: Manages protection state and UI updates

**Responsibilities:**
- Check protection status
- Manage service lifecycle
- Handle permission requests
- Update UI state

**Location**: `src/com/android/settings/seekprivacy/SeekPrivacyController.java`

**Key Methods:**
```java
public class SeekPrivacyController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {
    
    private SwitchPreferenceCompat mPreference;
    private Context mContext;
    
    public SeekPrivacyController(Context context) {
        super(context, "seekprivacy_enable");
        mContext = context;
    }
    
    @Override
    public int getAvailabilityStatus() {
        // Check if device supports file encryption
        return AVAILABLE;
    }
    
    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (mPreference != null) {
            boolean enabled = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.SEEKPRIVACY_ENABLED, 0) == 1;
            mPreference.setChecked(enabled);
        }
    }
    
    public List<ProtectedFile> getProtectedFiles() {
        ProtectedFileDatabase database = new ProtectedFileDatabase(mContext);
        return database.getAllProtectedFiles();
    }
}
```

---

## 3. File Cloaking Implementation

### Challenge: Making Files Invisible to Other Apps

**Problem**: Android's scoped storage makes it difficult to hide files from apps with MANAGE_EXTERNAL_STORAGE.

**Solution**: Use MediaStore API filtering and file encryption:

1. **Encrypt Files**: Protected files are encrypted, making them unreadable
2. **Store Separately**: Encrypted files stored in app-specific directory
3. **Original File Removal**: Original file deleted after encryption
4. **MediaStore Filtering**: Filter MediaStore queries to exclude protected files (if possible)
5. **Access Control**: Only Settings app can decrypt and access files

### Cloaking Strategy

**Approach 1: Encryption + Separate Storage** (Recommended)
- Encrypt files and store in app-specific storage
- Delete original files
- Other apps can't access encrypted files
- Owner accesses via Settings app only

**Approach 2: MediaStore Metadata Filtering** (Limited)
- Use MediaStore API to hide files
- Limited effectiveness against MANAGE_EXTERNAL_STORAGE
- May not work on all Android versions

**Approach 3: ContentProvider Filtering** (Complex)
- Create custom ContentProvider
- Filter file access requests
- Requires framework changes

**Recommended**: Approach 1 (Encryption + Separate Storage) - Simplest and most effective.

---

## 4. Implementation Details

### File Encryption Flow

1. **User Selects File**:
   - File picker opens
   - User selects file to protect
   - File URI obtained

2. **File Encryption**:
   - Read original file
   - Generate encryption key (if needed)
   - Encrypt file using AES-256-GCM
   - Store encrypted file in app-specific storage
   - Delete original file
   - Store metadata in database

3. **File Cloaking**:
   - Original file removed from public storage
   - Encrypted file stored in app-specific directory
   - Other apps can't access encrypted file
   - File invisible to MediaStore queries

4. **File Access**:
   - Owner requests file access via Settings
   - Service decrypts file temporarily
   - File opened with system intent
   - File re-encrypted after access (optional)

5. **File Unprotection**:
   - Owner requests unprotection
   - Service decrypts file
   - File restored to original location
   - Metadata removed from database

### Android Keystore Integration

**Key Generation:**
```java
KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
    "SeekPrivacyKey",
    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
    .setKeySize(256)
    .setUserAuthenticationRequired(false) // Allow background encryption
    .build();
keyGenerator.init(spec);
SecretKey key = keyGenerator.generateKey();
```

**File Encryption:**
```java
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
cipher.init(Cipher.ENCRYPT_MODE, key);
byte[] iv = cipher.getIV();

// Read file
byte[] fileData = readFile(file);

// Encrypt
byte[] encryptedData = cipher.doFinal(fileData);

// Write encrypted file with IV prepended
writeEncryptedFile(encryptedFile, iv, encryptedData);
```

**File Decryption:**
```java
// Read IV and encrypted data
byte[] iv = readIV(encryptedFile);
byte[] encryptedData = readEncryptedData(encryptedFile);

// Decrypt
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
GCMParameterSpec spec = new GCMParameterSpec(128, iv);
cipher.init(Cipher.DECRYPT_MODE, key, spec);
byte[] decryptedData = cipher.doFinal(encryptedData);

// Write decrypted file temporarily
writeTempFile(decryptedData);
```

### Storage Structure

**App-Specific Storage:**
```
/data/data/com.android.settings/files/SeekPrivacy/
├── encrypted/
│   ├── file1.encrypted
│   ├── file2.encrypted
│   └── ...
├── temp/
│   └── (temporary decrypted files)
└── metadata/
    └── seekprivacy.db
```

**File Naming:**
- Encrypted files: `{original_filename}_{hash}.encrypted`
- Temp files: `{original_filename}_{timestamp}.tmp`
- Database: `seekprivacy.db` (encrypted SQLite)

---

## 5. Resource Organization (Portable Design)

### Separate Resource Files

**Strings**: `res/values/seekprivacy_strings.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Titles -->
    <string name="seekprivacy_settings_title">File Protection</string>
    <string name="seekprivacy_settings_summary">Protect files from other apps</string>
    
    <!-- Preferences -->
    <string name="seekprivacy_enable_title">Enable File Protection</string>
    <string name="seekprivacy_enable_summary">Encrypt and cloak protected files</string>
    <string name="seekprivacy_protect_file_title">Protect File</string>
    <string name="seekprivacy_protect_file_summary">Select a file to protect</string>
    
    <!-- Status -->
    <string name="seekprivacy_status_protected">%d files protected</string>
    <string name="seekprivacy_status_none">No files protected</string>
    
    <!-- Actions -->
    <string name="seekprivacy_unprotect_title">Unprotect File</string>
    <string name="seekprivacy_unprotect_message">Remove protection from this file?</string>
    <string name="unprotect">Unprotect</string>
    
    <!-- Errors -->
    <string name="seekprivacy_error_encryption">Failed to encrypt file</string>
    <string name="seekprivacy_error_decryption">Failed to decrypt file</string>
    <string name="seekprivacy_error_permission">Storage permission required</string>
</resources>
```

**Dimens**: `res/values/seekprivacy_dimens.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <dimen name="seekprivacy_file_icon_size">48dp</dimen>
    <dimen name="seekprivacy_file_list_padding">16dp</dimen>
</resources>
```

**Colors**: `res/values/seekprivacy_colors.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="seekprivacy_protected_file_color">@android:color/system_accent1_600</color>
    <color name="seekprivacy_error_color">@android:color/system_error_color</color>
</resources>
```

**Drawables**: `res/drawable/ic_seekprivacy.xml`, `res/drawable/ic_file_protected.xml`

**Layout**: `res/xml/seekprivacy_settings.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:title="@string/seekprivacy_settings_title">
    
    <SwitchPreferenceCompat
        android:key="seekprivacy_enable"
        android:title="@string/seekprivacy_enable_title"
        android:summary="@string/seekprivacy_enable_summary"
        android:icon="@drawable/ic_seekprivacy"
        app:useSimpleSummaryProvider="true" />
    
    <Preference
        android:key="seekprivacy_protect_file"
        android:title="@string/seekprivacy_protect_file_title"
        android:summary="@string/seekprivacy_protect_file_summary"
        android:icon="@drawable/ic_add_file" />
    
    <PreferenceCategory
        android:key="seekprivacy_protected_files_category"
        android:title="@string/seekprivacy_protected_files_title">
        <!-- Dynamically populated -->
    </PreferenceCategory>
    
    <PreferenceCategory
        android:key="seekprivacy_info_category"
        android:title="@string/seekprivacy_info_title">
        <Preference
            android:key="seekprivacy_status"
            android:title="@string/seekprivacy_status_title"
            android:summary="@string/seekprivacy_status_none"
            android:selectable="false" />
    </PreferenceCategory>
</PreferenceScreen>
```

---

## 6. Framework Changes (Minimal)

### Settings.Secure Keys

**File**: `frameworks/base/core/java/android/provider/Settings.java`

**Add to Settings.Secure:**
```java
/**
 * SeekPrivacy File Protection - Enable/disable file protection
 * @hide
 */
public static final String SEEKPRIVACY_ENABLED = "seekprivacy_enabled";

/**
 * SeekPrivacy File Protection - Protected files count
 * @hide
 */
public static final String SEEKPRIVACY_PROTECTED_COUNT = "seekprivacy_protected_count";
```

**Alternative**: Can use SharedPreferences instead to avoid framework changes entirely.

### No Other Framework Changes

- No SystemUI modifications
- No system service creation
- Uses existing Android Keystore API
- Uses existing MediaStore API
- Uses ForegroundService pattern

---

## 7. Permissions Required

### AndroidManifest.xml

```xml
<!-- Storage access for file operations -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
    tools:ignore="ScopedStorage" />

<!-- Foreground service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
```

### Runtime Permissions

**MANAGE_EXTERNAL_STORAGE** (Android 11+):
- Required for accessing all files
- Requested via `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`
- Checked via `Environment.isExternalStorageManager()`

**Code:**
```java
private void requestManageStoragePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            startActivity(intent);
        }
    }
}
```

---

## 8. AndroidManifest.xml Changes

### Service Declaration

```xml
<!-- SeekPrivacy File Protection Service -->
<service
    android:name="com.android.settings.seekprivacy.SeekPrivacyService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="dataSync"
    android:permission="android.permission.BIND_FOREGROUND_SERVICE" />
```

### File Provider (for sharing decrypted files)

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.seekprivacy.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/seekprivacy_file_paths" />
</provider>
```

**File**: `res/xml/seekprivacy_file_paths.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path name="seekprivacy_temp" path="SeekPrivacy/temp/" />
</paths>
```

---

## 9. Implementation Checklist

### Phase 1: Core Infrastructure
- [ ] Create `SeekPrivacyService.java` (ForegroundService)
- [ ] Create `FileEncryptionHelper.java` (Encryption helper)
- [ ] Create `ProtectedFileDatabase.java` (SQLite database)
- [ ] Create `SeekPrivacyController.java` (Preference controller)
- [ ] Create `SeekPrivacySettingsFragment.java` (UI fragment)
- [ ] Create resource files (strings, dimens, colors, drawables)
- [ ] Create `seekprivacy_settings.xml` layout
- [ ] Add service to AndroidManifest.xml

### Phase 2: File Encryption
- [ ] Implement Android Keystore key generation
- [ ] Implement file encryption (AES-256-GCM)
- [ ] Implement file decryption
- [ ] Test encryption/decryption with various file types
- [ ] Handle encryption errors gracefully

### Phase 3: File Protection UI
- [ ] Implement file picker integration
- [ ] Implement protected files list display
- [ ] Implement file access interface
- [ ] Implement unprotect functionality
- [ ] Add file protection status display

### Phase 4: Cloaking & Security
- [ ] Implement file cloaking (encryption + separate storage)
- [ ] Test cloaking effectiveness
- [ ] Implement secure file deletion
- [ ] Add database encryption (optional)

### Phase 5: Integration & Testing
- [ ] Add to Anatolia Settings page
- [ ] Test with various file types
- [ ] Test permission handling
- [ ] Test service lifecycle
- [ ] Test file access and unprotection
- [ ] Performance testing

---

## 10. Files to Create/Modify

### Framework (Commit 1: `seekprivacy: File Protection`) - Optional

**File**: `frameworks/base/core/java/android/provider/Settings.java`
- Add 2 Settings.Secure keys (or use SharedPreferences)

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

---

## 11. Functional Requirements (Must Work)

### Core Functionality
✅ **File Encryption**: Actually encrypts files using Android Keystore
✅ **File Decryption**: Actually decrypts files on-demand
✅ **File Cloaking**: Makes files invisible to other apps
✅ **File Access**: Owner can access files normally
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

---

## 12. Limitations & Considerations

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

---

## 13. Future Enhancements

1. **Batch Protection**: Protect multiple files at once
2. **Folder Protection**: Protect entire folders
3. **Auto-Protection**: Auto-protect files from specific apps
4. **Cloud Backup**: Encrypted cloud backup option
5. **File Sharing**: Share protected files securely
6. **Advanced Cloaking**: Enhanced cloaking mechanisms
7. **File Recovery**: Recovery mechanism for lost files
8. **Performance Optimization**: Faster encryption/decryption

---

## 14. Security Considerations

### Encryption Security
- Uses Android Keystore (hardware-backed when available)
- AES-256-GCM encryption (strong cipher)
- Keys never leave secure hardware
- IV (Initialization Vector) unique per file

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

---

## 15. Testing Plan

### Functional Testing
- [ ] File encryption works correctly
- [ ] File decryption works correctly
- [ ] File cloaking works (files invisible to other apps)
- [ ] File access works for owner
- [ ] File unprotection works
- [ ] Service persists correctly
- [ ] Permissions handled correctly
- [ ] Database stores metadata correctly

### Security Testing
- [ ] Encrypted files unreadable by other apps
- [ ] Keys stored securely in Android Keystore
- [ ] No plaintext files in public storage
- [ ] Secure file deletion works

### Performance Testing
- [ ] Encryption performance acceptable
- [ ] Decryption performance acceptable
- [ ] UI remains responsive during operations
- [ ] Battery impact acceptable

### Compatibility Testing
- [ ] Works on Android 11+
- [ ] Works with various file types
- [ ] Works with MANAGE_EXTERNAL_STORAGE
- [ ] Works with scoped storage

---

## 16. Conclusion

This plan provides a comprehensive roadmap for integrating SeekPrivacy-style file protection into LineageOS Settings. The implementation will:

1. **Encrypt Files**: Actually encrypts files using Android Keystore
2. **Cloak Files**: Makes files invisible to other apps via encryption + separate storage
3. **Seamless Access**: Owner can access files normally through Settings
4. **Deep Security**: Files encrypted at rest, protected from physical access
5. **Privacy-First**: Local-only operation, no cloud, no tracking

The architecture uses:
- **ForegroundService** for encryption/decryption operations
- **Android Keystore** for secure key management
- **SQLite Database** for protected file metadata
- **App-Specific Storage** for encrypted files
- **Minimal Framework Changes** (optional Settings.Secure keys)

The feature will be launched from **Anatolia Settings** for testing and can be moved to main Settings later if desired.

