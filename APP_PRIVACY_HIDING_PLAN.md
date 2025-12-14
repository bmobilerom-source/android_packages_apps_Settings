# App Privacy & Hiding Features - Implementation Plan

## Executive Summary

This plan outlines how to implement Hide-My-Applist-like app privacy and hiding features directly in the Settings app. Unlike Hide-My-Applist which requires Xposed framework, this implementation uses Android's built-in PackageVisibility API and framework-level hooks, making it available to all users without root or Xposed.

**Reference:** Based on analysis of [Hide-My-ApplistWTF](https://github.com/TeaqariaWTF/Hide-My-ApplistWTF) features, adapted for built-in Settings integration.

---

## 1. Hide-My-Applist Feature Analysis

### 1.1 Core Features in Hide-My-Applist

Based on the [Hide-My-ApplistWTF repository](https://github.com/TeaqariaWTF/Hide-My-ApplistWTF):

1. **App Hiding**
   - Hide apps from app list queries
   - Per-app hiding rules
   - Template-based hiding (hide all root apps, etc.)

2. **Query Interception**
   - Intercept `PackageManager.getInstalledPackages()`
   - Intercept `PackageManager.queryIntentActivities()`
   - Intercept `PackageManager.resolveActivity()`
   - Block app list detection methods

3. **Privacy Protection**
   - Prevent fingerprinting via app list
   - Hide root-related apps
   - Hide system apps
   - Custom hiding rules

4. **Testing Tools**
   - Test app list detection
   - Verify hiding effectiveness
   - Debug mode

### 1.2 How Hide-My-Applist Works

**With Xposed:**
- Hooks into PackageManager methods
- Intercepts app list queries
- Filters results based on rules
- Works system-wide

**Our Approach (Built-in):**
- Use PackageVisibility API (Android 11+)
- Framework-level PackageManager hooks
- Component hiding via PackageManager
- Custom visibility rules
- No Xposed required

---

## 2. Android Package Visibility System

### 2.1 PackageVisibility API

**Key Components:**
- `PackageManager.setPackagesSuspended()` - Suspend packages
- `PackageVisibility` - Control package visibility
- `QUERY_ALL_PACKAGES` permission - Required for full app list access
- Component hiding - Hide specific components

**Limitations:**
- PackageVisibility API is limited
- Some apps bypass visibility restrictions
- Framework hooks needed for comprehensive hiding

### 2.2 Framework-Level Implementation

**Since we're building LineageOS:**
- Can modify framework PackageManager
- Add custom visibility filters
- Implement app hiding hooks
- No root/Xposed needed

---

## 3. Architecture Design

### 3.1 Component Structure

```
┌─────────────────────────────────────────────────────────────┐
│         AppPrivacySettings Fragment                         │
│  ┌───────────────────────────────────────────────────────┐ │
│  │   AppPrivacyManager (Singleton)                        │ │
│  │  - Manages app hiding rules                            │ │
│  │  - Handles visibility filters                          │ │
│  │  - Persists settings                                   │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              Preference Controllers                   │ │
│  │  - AppHidingListController                             │ │
│  │  - TemplateRulesController                             │ │
│  │  - QueryInterceptionController                         │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              Framework Hooks                          │ │
│  │  - PackageManager hooks                                │ │
│  │  - Visibility filters                                  │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 File Structure

```
packages/apps/Settings/
├── src/com/android/settings/privacy/
│   ├── AppPrivacySettings.java              # Main fragment
│   ├── AppPrivacyManager.java               # Singleton manager
│   ├── controllers/
│   │   ├── AppHidingListController.java
│   │   ├── TemplateRulesController.java
│   │   └── QueryInterceptionController.java
│   └── utils/
│       ├── AppHidingHelper.java             # App hiding logic
│       └── VisibilityFilter.java            # Visibility filtering
└── res/
    ├── xml/
    │   └── app_privacy_settings.xml          # Preference screen
    └── layout/
        └── app_hiding_list.xml              # App list layout

frameworks/base/core/java/android/content/pm/
└── PackageManager.java                      # Add hiding hooks
```

---

## 4. Implementation Details

### 4.1 AppPrivacyManager (Singleton)

**Purpose:** Central manager for app hiding

**Key Responsibilities:**
- Manage hidden app list
- Apply visibility filters
- Persist settings
- Coordinate with framework

**Implementation:**

```java
package com.android.settings.privacy;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;

public class AppPrivacyManager {
    private static final String TAG = "AppPrivacyManager";
    private static AppPrivacyManager sInstance;
    
    // Settings keys
    private static final String KEY_HIDDEN_APPS = "app_privacy_hidden_apps";
    private static final String KEY_HIDE_ROOT_APPS = "app_privacy_hide_root_apps";
    private static final String KEY_HIDE_SYSTEM_APPS = "app_privacy_hide_system_apps";
    private static final String KEY_ENABLE_HIDING = "app_privacy_enable_hiding";
    
    private Context mContext;
    private Set<String> mHiddenPackages = new HashSet<>();
    
    private AppPrivacyManager(Context context) {
        mContext = context.getApplicationContext();
        loadHiddenPackages();
    }
    
    public static synchronized AppPrivacyManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AppPrivacyManager(context);
        }
        return sInstance;
    }
    
    /**
     * Check if app hiding is enabled
     */
    public boolean isHidingEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
            KEY_ENABLE_HIDING, 0) == 1;
    }
    
    public void setHidingEnabled(boolean enabled) {
        Settings.Secure.putInt(mContext.getContentResolver(),
            KEY_ENABLE_HIDING, enabled ? 1 : 0);
        notifyFramework();
    }
    
    /**
     * Check if package should be hidden
     */
    public boolean isPackageHidden(String packageName) {
        if (!isHidingEnabled()) {
            return false;
        }
        
        // Check explicit hiding
        if (mHiddenPackages.contains(packageName)) {
            return true;
        }
        
        // Check template rules
        if (shouldHideByTemplate(packageName)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Add package to hidden list
     */
    public void hidePackage(String packageName) {
        mHiddenPackages.add(packageName);
        saveHiddenPackages();
        notifyFramework();
    }
    
    /**
     * Remove package from hidden list
     */
    public void unhidePackage(String packageName) {
        mHiddenPackages.remove(packageName);
        saveHiddenPackages();
        notifyFramework();
    }
    
    /**
     * Get all hidden packages
     */
    public Set<String> getHiddenPackages() {
        return new HashSet<>(mHiddenPackages);
    }
    
    /**
     * Check if should hide by template rules
     */
    private boolean shouldHideByTemplate(String packageName) {
        // Hide root apps
        if (Settings.Secure.getInt(mContext.getContentResolver(),
                KEY_HIDE_ROOT_APPS, 0) == 1) {
            if (isRootApp(packageName)) {
                return true;
            }
        }
        
        // Hide system apps
        if (Settings.Secure.getInt(mContext.getContentResolver(),
                KEY_HIDE_SYSTEM_APPS, 0) == 1) {
            if (isSystemApp(packageName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if app is root-related
     */
    private boolean isRootApp(String packageName) {
        // Common root app package names
        String[] rootApps = {
            "com.topjohnwu.magisk",
            "me.weishu.kernelsu",
            "com.android.vending", // Play Store (often checked)
            "com.termux",
            "com.noshufou.android.su",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser"
        };
        
        for (String rootApp : rootApps) {
            if (packageName.equals(rootApp)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if app is system app
     */
    private boolean isSystemApp(String packageName) {
        try {
            PackageManager pm = mContext.getPackageManager();
            android.content.pm.ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return (info.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Load hidden packages from Settings
     */
    private void loadHiddenPackages() {
        String hidden = Settings.Secure.getString(mContext.getContentResolver(), KEY_HIDDEN_APPS);
        if (hidden != null && !hidden.isEmpty()) {
            String[] packages = hidden.split(",");
            for (String pkg : packages) {
                if (!pkg.isEmpty()) {
                    mHiddenPackages.add(pkg);
                }
            }
        }
    }
    
    /**
     * Save hidden packages to Settings
     */
    private void saveHiddenPackages() {
        StringBuilder sb = new StringBuilder();
        for (String pkg : mHiddenPackages) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(pkg);
        }
        Settings.Secure.putString(mContext.getContentResolver(), KEY_HIDDEN_APPS, sb.toString());
    }
    
    /**
     * Notify framework of changes
     */
    private void notifyFramework() {
        // Send broadcast or use ContentResolver to notify framework
        // Framework will reload hiding rules
    }
}
```

### 4.2 AppPrivacySettings Fragment

**Purpose:** Main Settings page for app privacy

**Implementation:**

```java
package com.android.settings.privacy;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

public class AppPrivacySettings extends DashboardFragment {
    private static final String TAG = "AppPrivacySettings";
    
    private AppPrivacyManager mPrivacyManager;
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPrivacyManager = AppPrivacyManager.getInstance(context);
    }
    
    @Override
    protected String getLogTag() {
        return TAG;
    }
    
    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.app_privacy_settings;
    }
    
    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new AppHidingListController(context, this));
        controllers.add(new TemplateRulesController(context, this));
        controllers.add(new QueryInterceptionController(context, this));
        return controllers;
    }
    
    @Override
    public int getMetricsCategory() {
        return SettingsEnums.PRIVACY;
    }
}
```

### 4.3 AppHidingListController

**Purpose:** Controller for app hiding list

**Features:**
- Display list of installed apps
- Toggle hiding per app
- Search functionality
- Filter system/user apps

**Implementation:**

```java
package com.android.settings.privacy.controllers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.privacy.AppPrivacyManager;
import com.android.settings.privacy.AppPrivacySettings;
import com.android.settingslib.widget.AppPreference;

import java.util.ArrayList;
import java.util.List;

public class AppHidingListController extends BasePreferenceController {
    private AppPrivacyManager mPrivacyManager;
    private AppPrivacySettings mFragment;
    private PreferenceScreen mScreen;
    
    public AppHidingListController(Context context, String key) {
        super(context, key);
        mPrivacyManager = AppPrivacyManager.getInstance(context);
    }
    
    public AppHidingListController(Context context, AppPrivacySettings fragment) {
        this(context, "app_hiding_list");
        mFragment = fragment;
    }
    
    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
    
    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mScreen = screen;
        updateAppList();
    }
    
    private void updateAppList() {
        if (mScreen == null) return;
        
        PackageManager pm = mContext.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        
        mScreen.removeAll();
        
        for (ApplicationInfo info : apps) {
            String packageName = info.packageName;
            AppPreference preference = new AppPreference(mContext);
            preference.setKey(packageName);
            preference.setTitle(info.loadLabel(pm).toString());
            preference.setIcon(info.loadIcon(pm));
            preference.setChecked(mPrivacyManager.isPackageHidden(packageName));
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                boolean hidden = (Boolean) newValue;
                if (hidden) {
                    mPrivacyManager.hidePackage(packageName);
                } else {
                    mPrivacyManager.unhidePackage(packageName);
                }
                return true;
            });
            mScreen.addPreference(preference);
        }
    }
}
```

---

## 5. Framework-Level Implementation

### 5.1 PackageManager Hooks

**Location:** `frameworks/base/core/java/android/content/pm/PackageManager.java`

**Modifications:**
- Add hiding filter to `getInstalledPackages()`
- Add hiding filter to `queryIntentActivities()`
- Add hiding filter to `resolveActivity()`
- Check AppPrivacyManager for hidden packages

**Implementation Pattern:**

```java
// In PackageManager.java
public List<PackageInfo> getInstalledPackages(int flags) {
    List<PackageInfo> packages = getInstalledPackagesInternal(flags);
    
    // Apply app hiding filter
    if (AppPrivacyManager.isHidingEnabled()) {
        packages = filterHiddenPackages(packages);
    }
    
    return packages;
}

private List<PackageInfo> filterHiddenPackages(List<PackageInfo> packages) {
    AppPrivacyManager manager = AppPrivacyManager.getInstance(mContext);
    List<PackageInfo> filtered = new ArrayList<>();
    
    for (PackageInfo pkg : packages) {
        if (!manager.isPackageHidden(pkg.packageName)) {
            filtered.add(pkg);
        }
    }
    
    return filtered;
}
```

---

## 6. Preference Screen Layout

### 6.1 app_privacy_settings.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:settings="http://schemas.android.com/apk/res-auto"
    android:title="@string/app_privacy_title">
    
    <!-- Master Toggle -->
    <SwitchPreferenceCompat
        android:key="app_privacy_enable"
        android:title="@string/app_privacy_enable_title"
        android:summary="@string/app_privacy_enable_summary"
        android:order="0"
        settings:controller="com.android.settings.privacy.controllers.AppPrivacyMasterController"/>
    
    <!-- Template Rules -->
    <PreferenceCategory
        android:key="template_rules"
        android:title="@string/template_rules_title"
        android:order="10">
        
        <SwitchPreferenceCompat
            android:key="hide_root_apps"
            android:title="@string/hide_root_apps_title"
            android:summary="@string/hide_root_apps_summary"
            android:order="11"
            settings:controller="com.android.settings.privacy.controllers.HideRootAppsController"/>
        
        <SwitchPreferenceCompat
            android:key="hide_system_apps"
            android:title="@string/hide_system_apps_title"
            android:summary="@string/hide_system_apps_summary"
            android:order="12"
            settings:controller="com.android.settings.privacy.controllers.HideSystemAppsController"/>
    </PreferenceCategory>
    
    <!-- App Hiding List -->
    <PreferenceCategory
        android:key="app_hiding_list"
        android:title="@string/app_hiding_list_title"
        android:order="20"
        settings:controller="com.android.settings.privacy.controllers.AppHidingListController"/>
    
    <!-- Testing Tools -->
    <PreferenceCategory
        android:key="testing_tools"
        android:title="@string/testing_tools_title"
        android:order="30">
        
        <Preference
            android:key="test_app_list"
            android:title="@string/test_app_list_title"
            android:summary="@string/test_app_list_summary"
            android:fragment="com.android.settings.privacy.TestAppListFragment"
            android:order="31"/>
    </PreferenceCategory>
    
</PreferenceScreen>
```

### 6.2 Integration into Privacy Settings

**Add to `privacy_dashboard_settings.xml`:**

```xml
<!-- App Privacy & Hiding -->
<Preference
    android:key="app_privacy"
    android:title="@string/app_privacy_title"
    android:fragment="com.android.settings.privacy.AppPrivacySettings"
    android:order="50"
    settings:keywords="@string/keywords_app_privacy"/>
```

---

## 7. Implementation Phases

### Phase 1: Core Infrastructure
**Goal:** Basic app hiding system

**Tasks:**
1. Create `AppPrivacyManager.java`
2. Add Settings.Secure persistence
3. Implement basic hiding logic
4. Test package hiding

**Success Criteria:**
- Apps can be hidden
- Settings persist
- Basic hiding works

### Phase 2: UI Implementation
**Goal:** Settings UI for app hiding

**Tasks:**
1. Create `AppPrivacySettings.java`
2. Create preference controllers
3. Implement app list display
4. Add toggle functionality

**Success Criteria:**
- UI displays correctly
- Apps can be toggled
- Changes apply immediately

### Phase 3: Framework Integration
**Goal:** Framework-level hooks

**Tasks:**
1. Modify PackageManager
2. Add hiding filters
3. Test query interception
4. Verify hiding effectiveness

**Success Criteria:**
- Framework hooks work
- Queries are filtered
- Hiding is effective

### Phase 4: Template Rules
**Goal:** Automatic hiding rules

**Tasks:**
1. Implement root app detection
2. Implement system app detection
3. Add template toggles
4. Test template rules

**Success Criteria:**
- Template rules work
- Apps are auto-hidden
- Rules are configurable

### Phase 5: Testing & Polish
**Goal:** Production-ready

**Tasks:**
1. Add testing tools
2. Improve UI
3. Add help text
4. Performance optimization

**Success Criteria:**
- All features work
- UI is polished
- Performance is good

---

## 8. Safety Considerations

### 8.1 Safe Implementation

**Why This is Safe:**
1. **No Root Required:** Uses framework APIs
2. **Reversible:** All changes can be undone
3. **Isolated:** Only affects app visibility
4. **Validated:** Uses standard Android APIs
5. **Secure:** Settings stored in Settings.Secure

### 8.2 Limitations

**Known Limitations:**
- Some apps may bypass visibility restrictions
- Framework modifications required
- May not work with all detection methods
- Requires ROM-level implementation

**Mitigation:**
- Document limitations
- Provide testing tools
- Continuous improvement
- User education

---

## 9. Resource Requirements

### 9.1 New Files

**Java:**
- `AppPrivacyManager.java` (~300 lines)
- `AppPrivacySettings.java` (~100 lines)
- `AppHidingListController.java` (~150 lines)
- `TemplateRulesController.java` (~100 lines)
- `AppHidingHelper.java` (~200 lines)

**XML:**
- `res/xml/app_privacy_settings.xml`
- `res/layout/app_hiding_list.xml`

**Framework:**
- `frameworks/base/core/java/android/content/pm/PackageManager.java` (modifications)

### 9.2 Modified Files

- `res/xml/privacy_dashboard_settings.xml` (add entry)
- `SettingsGateway.java` (register fragment)

---

## 10. Testing Plan

### 10.1 Functional Testing

- [ ] Apps can be hidden
- [ ] Hidden apps don't appear in queries
- [ ] Template rules work
- [ ] Settings persist
- [ ] Reset function works

### 10.2 Effectiveness Testing

- [ ] Test with various apps
- [ ] Verify hiding works
- [ ] Check detection bypass
- [ ] Test edge cases

---

## 11. Conclusion

This plan provides a roadmap for implementing Hide-My-Applist-like features directly in Settings. The implementation:

1. **No Xposed Required:** Uses framework APIs
2. **Built-in:** Part of ROM
3. **Feature-Rich:** Includes hiding, templates, testing
4. **User-Friendly:** Integrated into Settings
5. **Maintainable:** Clean architecture
6. **Extensible:** Easy to add features

The architecture follows Android Settings best practices and provides users with powerful app privacy protection without requiring Xposed framework.

