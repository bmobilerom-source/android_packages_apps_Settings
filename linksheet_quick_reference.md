# LinkSheet Implementation - Quick Reference

## ✅ Answers to Your Questions

### 1. **Framework Integration Needed?** ✅
- **Answer**: YES, framework integration is required
- **Why**: LinkSheet needs to intercept system-wide intents
- **What**: Settings.Secure keys + high-priority intent filter
- **Impact**: Minimal framework changes, but necessary for functionality

### 2. **Launch Location** ✅
- **Answer**: Launches separately from Anatolia Settings
- **Path**: Settings → Anatolia Settings → Link Chooser
- **Why**: Separate for testing, can move later

## Single-Phase Implementation Strategy

### Complete Implementation (Framework + Settings)

**What You Get:**
- ✅ System-wide link interception
- ✅ Custom chooser dialog for all links
- ✅ Domain-based preference memory
- ✅ AMP link resolution
- ✅ Browser private browsing integration
- ✅ Usage statistics and management

**Framework Changes Required:**
1. **Settings.Secure Keys** (`frameworks/base/core/java/android/provider/Settings.java`):
   - `LINKSHEET_ENABLED`
   - `LINKSHEET_AUTO_RESOLVE_AMP`
   - `LINKSHEET_ENABLE_PRIVATE_BROWSING`

2. **High-Priority Intent Filter** (priority="999") in AndroidManifest.xml

**Settings App Implementation:**
- `LinkSheetService.java` - System service for interception
- `LinkChooserActivity.java` - Chooser dialog activity
- `LinkPreferencesDatabase.java` - SQLite for preferences
- `LinkSheetSettingsFragment.java` - Settings UI
- Resource files (strings, layouts, etc.)
- Add entry to `anatolia.xml`

**Everything implemented together in one complete phase!**

## How It Works

### Link Interception Flow

1. **User taps link** → Android sends ACTION_VIEW intent
2. **LinkSheetService intercepts** (high priority filter)
3. **Service checks** if domain has saved preference
4. **If preference exists** → Opens directly in preferred app
5. **If no preference** → Shows LinkChooserActivity
6. **User selects app** → Link opens in chosen app
7. **If "Always" checked** → Preference saved for domain

### Key Technical Components

**Intent Filter (AndroidManifest.xml):**
```xml
<intent-filter android:priority="999">
    <action android:name="android.intent.action.VIEW" />
    <data android:scheme="http" />
    <data android:scheme="https" />
</intent-filter>
```

**Service Registration:**
```xml
<service android:name="com.android.settings.linksheet.LinkSheetService"
         android:enabled="true" android:exported="true">
    <!-- Intent filter with high priority -->
</service>
```

**Database Schema:**
- Domain preferences (preferred app per domain)
- Usage statistics (app usage tracking)
- App rules (allow/deny/preferred rules)

## User Experience

### Everyday Usage
- **First time**: Chooser appears, user selects app
- **Subsequent times**: Link opens directly in preferred app
- **Change preference**: Long-press link or use Settings

### Settings Management
- Enable/disable service
- Configure AMP resolution
- Manage domain preferences
- View usage statistics
- Reset preferences

## Implementation Checklist

### Framework Preparation
- [ ] Add Settings.Secure keys to `frameworks/base/core/java/android/provider/Settings.java`

### Core Service
- [ ] Create `LinkSheetService.java` with intent interception
- [ ] Create `LinkChooserActivity.java` for chooser dialog
- [ ] Create `LinkPreferencesDatabase.java` for storage
- [ ] Update AndroidManifest.xml with service and intent filter

### Settings UI
- [ ] Create `LinkSheetSettingsFragment.java`
- [ ] Create resource files (strings, layouts, drawables)
- [ ] Add entry to `anatolia.xml`

### Advanced Features
- [ ] Implement AMP link resolution
- [ ] Add browser private browsing integration
- [ ] Implement usage statistics
- [ ] Add domain preference management

## Testing Strategy

### Basic Functionality
- [ ] Link interception works
- [ ] Chooser dialog appears
- [ ] App selection works
- [ ] Preference saving works

### Advanced Features
- [ ] AMP links resolve correctly
- [ ] Private browsing options appear
- [ ] Statistics track correctly
- [ ] Domain preferences apply

### Edge Cases
- [ ] Links without http/https schemes
- [ ] Apps that bypass standard intents
- [ ] Multiple apps for same domain
- [ ] Preference conflicts

## Summary

✅ **Framework Integration**: Required for system-wide interception
✅ **Separate Launch**: Anatolia Settings for testing
✅ **Complete Functionality**: Intercepts all links, shows chooser, remembers preferences
✅ **Advanced Features**: AMP resolution, private browsing, statistics
✅ **Single Phase**: Everything implemented together

**Ready to restore the Android link chooser experience!**

