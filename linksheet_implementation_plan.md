# LinkSheet URL Chooser - Implementation Plan

## Executive Summary

This plan outlines how to integrate **LinkSheet-style URL chooser** functionality into LineageOS Settings, restoring the link chooser that was removed in Android 12+. The feature will allow users to choose which app opens links instead of relying on Android's "verified app links" that automatically route links to specific apps.

**Key Features:**
- **URL Chooser Dialog**: Restore the link app chooser removed in Android 12+
- **App Preferences**: Remember user's preferred app for each domain/website
- **In-App Links**: Handle links opened within apps
- **AMP Link Resolution**: Automatically resolve AMP links to original articles
- **Browser Integration**: Support for private browsing modes
- **Domain Rules**: Configure per-domain link handling

---

## 1. Feature Overview

### What LinkSheet Does

**Core Functionality:**
1. **Intercepts Links**: Catches all http/https intents system-wide
2. **Shows Chooser**: Displays dialog to choose which app opens the link
3. **Remembers Preferences**: Learns user's preferred app for each domain
4. **Handles In-App Links**: Works with links opened within other apps
5. **AMP Resolution**: Automatically resolves AMP links to original content
6. **Browser Integration**: Integrates with browsers for private browsing

**Key Characteristics:**
- **System-Wide**: Intercepts all link intents globally
- **Smart Learning**: Remembers preferences per domain
- **Privacy-Focused**: Local preferences, no cloud sync
- **Customizable**: Extensive configuration options
- **Framework Integration**: Requires system-level intent interception

### Integration with Android System

**How Android Links Work (Before 12):**
- User taps link → Android shows app chooser
- User selects app → Link opens in chosen app
- Optional "Always" checkbox → Preference remembered

**How Android Links Work (After 12):**
- Verified app links automatically open in specific apps
- No chooser shown for "verified" links
- Only shows chooser for unverified links

**LinkSheet Solution:**
- Override Android's link handling
- Show custom chooser for all links
- Manage preferences per domain
- Provide extensive customization

---

## 2. Architecture Design

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│ Android System                                                │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ Intent System (Modified)                             │ │
│ │ - ACTION_VIEW intents intercepted                     │ │
│ │ - LinkSheet service called                            │ │
│ │ - Chooser dialog shown                                │ │
│ └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Settings App                                                │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ LinkSheetService (System Service)                     │ │
│ │ - Intercepts http/https intents                       │ │
│ │ - Shows chooser dialog                                │ │
│ │ - Manages preferences                                 │ │
│ │ - Handles AMP resolution                              │ │
│ └───────────────────────────────────────────────────────┘ │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ LinkPreferencesDatabase (SQLite)                     │ │
│ │ - Stores domain preferences                          │ │
│ │ - Tracks usage statistics                            │ │
│ │ - Manages app rules                                  │ │
│ └───────────────────────────────────────────────────────┘ │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ LinkSheetSettingsFragment (UI Fragment)              │ │
│ │ - Configure preferences                              │ │
│ │ - Manage domain rules                                │ │
│ │ - View usage statistics                              │ │
│ │ - Reset preferences                                  │ │
│ └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Component Breakdown

#### A. LinkSheetService (System Service)

**Purpose**: System service that intercepts and handles link intents

**Responsibilities:**
- Intercept all ACTION_VIEW intents with http/https URIs
- Show custom chooser dialog
- Apply domain preferences
- Handle AMP link resolution
- Manage browser integration

**Location**: `src/com/android/settings/linksheet/LinkSheetService.java`

**Key Methods:**
```java
public class LinkSheetService extends Service {
    private static final String TAG = "LinkSheetService";
    
    // Intent filters for http/https URLs
    private static final String[] URL_SCHEMES = {"http", "https"};
    private static final String ACTION_VIEW = "android.intent.action.VIEW";
    
    private LinkPreferencesDatabase mDatabase;
    private Handler mHandler;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = new LinkPreferencesDatabase(this);
        mHandler = new Handler(Looper.getMainLooper());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null && isHttpUri(uri)) {
                handleLinkIntent(intent, uri);
                return START_NOT_STICKY; // Don't restart if killed
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    private boolean isHttpUri(Uri uri) {
        String scheme = uri.getScheme();
        return "http".equals(scheme) || "https".equals(scheme);
    }
    
    private void handleLinkIntent(Intent originalIntent, Uri uri) {
        String domain = uri.getHost();
        
        // Check if we have a preference for this domain
        DomainPreference pref = mDatabase.getDomainPreference(domain);
        if (pref != null && pref.getPreferredApp() != null) {
            // Use preferred app
            openWithPreferredApp(originalIntent, pref.getPreferredApp());
        } else {
            // Show chooser dialog
            showChooserDialog(originalIntent, uri);
        }
    }
    
    private void openWithPreferredApp(Intent originalIntent, String packageName) {
        originalIntent.setPackage(packageName);
        try {
            startActivity(originalIntent);
        } catch (Exception e) {
            // Fallback to chooser
            showChooserDialog(originalIntent, originalIntent.getData());
        }
    }
    
    private void showChooserDialog(Intent originalIntent, Uri uri) {
        Intent chooserIntent = new Intent(this, LinkChooserActivity.class);
        chooserIntent.putExtra("original_intent", originalIntent);
        chooserIntent.putExtra("uri", uri);
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(chooserIntent);
    }
}
```

#### B. LinkChooserActivity (Chooser Activity)

**Purpose**: Activity that shows the link chooser dialog

**Responsibilities:**
- Display list of apps that can handle the link
- Show domain preferences
- Handle user selection
- Remember preferences when "Always" is checked

**Location**: `src/com/android/settings/linksheet/LinkChooserActivity.java`

**Key Methods:**
```java
public class LinkChooserActivity extends Activity {
    private static final String TAG = "LinkChooserActivity";
    
    private Uri mUri;
    private Intent mOriginalIntent;
    private LinkPreferencesDatabase mDatabase;
    private RecyclerView mAppsList;
    private CheckBox mRememberChoice;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.link_chooser_activity);
        
        mUri = getIntent().getParcelableExtra("uri");
        mOriginalIntent = getIntent().getParcelableExtra("original_intent");
        mDatabase = new LinkPreferencesDatabase(this);
        
        setupViews();
        loadAvailableApps();
    }
    
    private void setupViews() {
        mAppsList = findViewById(R.id.apps_list);
        mRememberChoice = findViewById(R.id.remember_choice);
        
        // Domain info
        TextView domainText = findViewById(R.id.domain_text);
        domainText.setText("Open link from: " + mUri.getHost());
        
        // Buttons
        findViewById(R.id.just_once_button).setOnClickListener(v -> openJustOnce());
        findViewById(R.id.cancel_button).setOnClickListener(v -> finish());
    }
    
    private void loadAvailableApps() {
        PackageManager pm = getPackageManager();
        Intent testIntent = new Intent(mOriginalIntent);
        testIntent.setComponent(null);
        
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(testIntent, 0);
        List<AppInfo> apps = new ArrayList<>();
        
        for (ResolveInfo info : resolveInfos) {
            AppInfo appInfo = new AppInfo();
            appInfo.packageName = info.activityInfo.packageName;
            appInfo.label = info.loadLabel(pm).toString();
            appInfo.icon = info.loadIcon(pm);
            apps.add(appInfo);
        }
        
        AppAdapter adapter = new AppAdapter(apps, this::onAppSelected);
        mAppsList.setAdapter(adapter);
    }
    
    private void onAppSelected(AppInfo appInfo) {
        if (mRememberChoice.isChecked()) {
            // Remember preference
            DomainPreference pref = new DomainPreference();
            pref.setDomain(mUri.getHost());
            pref.setPreferredApp(appInfo.packageName);
            mDatabase.saveDomainPreference(pref);
        }
        
        // Open with selected app
        mOriginalIntent.setPackage(appInfo.packageName);
        try {
            startActivity(mOriginalIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open with " + appInfo.label, 
                Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    
    private void openJustOnce() {
        // Open without remembering preference
        try {
            startActivity(mOriginalIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    
    private static class AppInfo {
        String packageName;
        String label;
        Drawable icon;
    }
}
```

#### C. LinkPreferencesDatabase (SQLite Database)

**Purpose**: Stores domain preferences and usage statistics

**Responsibilities:**
- Store preferred app per domain
- Track usage statistics
- Manage custom rules
- Provide preference queries

**Location**: `src/com/android/settings/linksheet/LinkPreferencesDatabase.java`

**Database Schema:**
```sql
CREATE TABLE domain_preferences (
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    domain TEXT NOT NULL UNIQUE,
    preferred_package TEXT,
    always_ask INTEGER DEFAULT 0,
    created_at INTEGER NOT NULL,
    last_used INTEGER
);

CREATE TABLE app_rules (
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    package_name TEXT NOT NULL UNIQUE,
    rule_type INTEGER NOT NULL, -- 0=allow, 1=deny, 2=preferred
    created_at INTEGER NOT NULL
);

CREATE TABLE usage_stats (
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    domain TEXT NOT NULL,
    package_name TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    UNIQUE(domain, package_name)
);
```

#### D. LinkSheetSettingsFragment (UI Fragment)

**Purpose**: Settings UI for configuring LinkSheet preferences

**Responsibilities:**
- Configure general preferences
- Manage domain preferences
- View usage statistics
- Reset preferences
- Configure app rules

**Location**: `src/com/android/settings/linksheet/LinkSheetSettingsFragment.java`

---

## 3. Framework Integration (Required)

### Intent Filter Registration

**AndroidManifest.xml:**
```xml
<!-- LinkSheet URL Handler Service -->
<service
    android:name="com.android.settings.linksheet.LinkSheetService"
    android:enabled="true"
    android:exported="true">
    <intent-filter android:priority="999">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="http" />
        <data android:scheme="https" />
    </intent-filter>
</service>

<!-- Link Chooser Activity -->
<activity
    android:name="com.android.settings.linksheet.LinkChooserActivity"
    android:theme="@android:style/Theme.DeviceDefault.Light.Dialog.NoActionBar"
    android:excludeFromRecents="true"
    android:exported="false" />
```

### Framework Modifications Required

**1. Priority Intent Resolution**
The service needs high priority (999) to intercept intents before other apps.

**2. System Integration**
May need modifications to `PackageManager` or `IntentResolver` to ensure LinkSheet service takes precedence.

**3. Settings.Secure Keys**
```java
// In frameworks/base/core/java/android/provider/Settings.java
public static final String LINKSHEET_ENABLED = "linksheet_enabled";
public static final String LINKSHEET_AUTO_RESOLVE_AMP = "linksheet_auto_resolve_amp";
public static final String LINKSHEET_ENABLE_PRIVATE_BROWSING = "linksheet_enable_private_browsing";
```

---

## 4. Resource Organization (Portable Design)

### Separate Resource Files

**Strings**: `res/values/linksheet_strings.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Titles -->
    <string name="linksheet_settings_title">Link Chooser</string>
    <string name="linksheet_settings_summary">Choose which app opens links</string>
    
    <!-- Preferences -->
    <string name="linksheet_enable_title">Enable Link Chooser</string>
    <string name="linksheet_enable_summary">Show app chooser for all links</string>
    <string name="linksheet_amp_resolution_title">Resolve AMP Links</string>
    <string name="linksheet_amp_resolution_summary">Automatically open original articles</string>
    <string name="linksheet_private_browsing_title">Private Browsing</string>
    <string name="linksheet_private_browsing_summary">Offer private browsing option</string>
    <string name="linksheet_domain_preferences_title">Domain Preferences</string>
    <string name="linksheet_domain_preferences_summary">Manage per-domain preferences</string>
    
    <!-- Chooser -->
    <string name="linksheet_open_with">Open with</string>
    <string name="linksheet_just_once">Just once</string>
    <string name="linksheet_always">Always</string>
    <string name="linksheet_cancel">Cancel</string>
    
    <!-- Status -->
    <string name="linksheet_enabled">Link chooser enabled</string>
    <string name="linksheet_disabled">Link chooser disabled</string>
    <string name="linksheet_no_apps">No apps can open this link</string>
</resources>
```

**Layouts**: 
- `res/layout/link_chooser_activity.xml` - Chooser activity layout
- `res/layout/link_chooser_item.xml` - App list item layout
- `res/xml/linksheet_settings.xml` - Settings screen

---

## 5. Implementation Checklist

### Phase 1: Core Link Handling
- [ ] Create `LinkSheetService.java` (system service)
- [ ] Create `LinkChooserActivity.java` (chooser dialog)
- [ ] Create `LinkPreferencesDatabase.java` (SQLite database)
- [ ] Add AndroidManifest.xml entries
- [ ] Test basic link interception

### Phase 2: Preference Management
- [ ] Create `LinkSheetSettingsFragment.java` (settings UI)
- [ ] Implement domain preference storage
- [ ] Add usage statistics tracking
- [ ] Create resource files (strings, layouts)
- [ ] Add to Anatolia Settings

### Phase 3: Advanced Features
- [ ] Implement AMP link resolution
- [ ] Add browser private browsing integration
- [ ] Implement app rules and filtering
- [ ] Add domain preference management UI
- [ ] Test edge cases and error handling

### Phase 4: Framework Integration
- [ ] Add Settings.Secure keys
- [ ] Ensure high-priority intent filtering
- [ ] Test system-wide link interception
- [ ] Performance optimization

---

## 6. Technical Implementation Details

### Intent Interception Strategy

**High-Priority Intent Filter:**
```xml
<intent-filter android:priority="999">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="http" />
    <data android:scheme="https" />
</intent-filter>
```

**Service Startup:**
```java
@Override
public int onStartCommand(Intent intent, int flags, int startId) {
    if (ACTION_VIEW.equals(intent.getAction())) {
        handleLinkIntent(intent);
        return START_NOT_STICKY;
    }
    return super.onStartCommand(intent, flags, startId);
}
```

### AMP Link Resolution

**AMP Detection:**
```java
private boolean isAmpLink(Uri uri) {
    String host = uri.getHost();
    String path = uri.getPath();
    return host.contains("googleusercontent.com") || 
           path.contains("/amp/") ||
           uri.getQueryParameter("amp") != null;
}
```

**AMP Resolution:**
```java
private Uri resolveAmpLink(Uri ampUri) {
    // Extract canonical URL from AMP page
    // Implementation depends on AMP page structure
    return canonicalUri;
}
```

### Domain Preference Matching

**Domain Matching:**
```java
private DomainPreference getDomainPreference(String urlHost) {
    // Check exact domain match first
    DomainPreference pref = mDatabase.getDomainPreference(urlHost);
    if (pref != null) return pref;
    
    // Check subdomain matches
    String[] parts = urlHost.split("\\.");
    for (int i = 1; i < parts.length; i++) {
        String domain = String.join(".", Arrays.copyOfRange(parts, i, parts.length));
        pref = mDatabase.getDomainPreference(domain);
        if (pref != null) return pref;
    }
    
    return null;
}
```

---

## 7. Permissions Required

### AndroidManifest.xml

```xml
<!-- Query installed apps for chooser -->
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />

<!-- Internet access for AMP resolution -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- System alert window for chooser dialog -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

### Runtime Permissions

**SYSTEM_ALERT_WINDOW** (if needed for overlay chooser):
- Required if chooser is shown as overlay
- Request via `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`

---

## 8. Files to Create/Modify

### Framework (Commit: `linksheet: URL Chooser`)

**File**: `frameworks/base/core/java/android/provider/Settings.java`
- Add Settings.Secure keys for LinkSheet preferences

### Settings App (Commit: `linksheet: URL Chooser`)

**Created**:
- `src/com/android/settings/linksheet/LinkSheetService.java`
- `src/com/android/settings/linksheet/LinkChooserActivity.java`
- `src/com/android/settings/linksheet/LinkPreferencesDatabase.java`
- `src/com/android/settings/linksheet/LinkSheetSettingsFragment.java`
- `src/com/android/settings/linksheet/LinkSheetController.java`
- `res/xml/linksheet_settings.xml`
- `res/layout/link_chooser_activity.xml`
- `res/layout/link_chooser_item.xml`
- `res/values/linksheet_strings.xml`
- `res/drawable/ic_linksheet.xml`

**Modified**:
- `res/xml/anatolia.xml` - Add LinkSheet preference entry
- `AndroidManifest.xml` - Add service and activity declarations

---

## 9. Functional Requirements (Must Work)

### Core Functionality
✅ **Link Interception**: Actually intercepts http/https links system-wide
✅ **App Chooser**: Shows list of apps that can open the link
✅ **App Selection**: Opens link in selected app
✅ **Preference Memory**: Remembers user's preferred app per domain
✅ **Preference Application**: Uses remembered preferences automatically

### Advanced Functionality
✅ **AMP Resolution**: Automatically resolves AMP links to original articles
✅ **In-App Links**: Handles links opened within other apps
✅ **Browser Integration**: Offers private browsing options
✅ **Domain Rules**: Applies different rules per domain
✅ **Usage Statistics**: Tracks which apps are used for which domains

### Not Just Visual
- Service actually intercepts system intents
- Preferences actually stored and retrieved
- Links actually open in chosen apps
- AMP links actually resolved to originals
- Statistics actually tracked

---

## 10. User Experience Flow

### First-Time Link Opening

1. **User taps link** in any app
2. **LinkSheet intercepts** the intent
3. **Chooser appears** showing available apps
4. **User selects app** and checks "Always" if desired
5. **Link opens** in selected app
6. **Preference saved** for future use

### Subsequent Link Opening

1. **User taps link** from same domain
2. **LinkSheet checks** preferences
3. **Link opens directly** in preferred app (no chooser)
4. **Long-press option** to show chooser anyway

### Managing Preferences

1. **Open Settings** → Anatolia Settings → Link Chooser
2. **View domains** with saved preferences
3. **Edit preferences** per domain
4. **Reset preferences** if needed
5. **Configure rules** for specific apps

---

## 11. Conclusion

This plan provides a comprehensive roadmap for implementing LinkSheet-style URL chooser functionality into LineageOS Settings. The implementation will:

1. **Restore Link Chooser**: Bring back the URL chooser removed in Android 12+
2. **Intercept System-Wide**: Catch all http/https intents globally
3. **Smart Preferences**: Learn and remember user preferences per domain
4. **Advanced Features**: AMP resolution, private browsing, domain rules
5. **System Integration**: Work seamlessly with Android's intent system

The architecture uses:
- **System Service** for intent interception
- **SQLite Database** for preference storage
- **Custom Activity** for chooser dialog
- **Settings UI** for configuration
- **Framework Integration** for high-priority intent filtering

The feature will be launched from **Anatolia Settings** for testing and can be moved to main Settings later if desired.

