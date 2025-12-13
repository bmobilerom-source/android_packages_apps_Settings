# LinkSheet URL Chooser Feature - Plan Summary

## Feature Overview

### What It Does
The LinkSheet URL Chooser feature restores the **link app chooser** that was removed in Android 12+, allowing users to choose which app opens links instead of relying on Android's "verified app links" that automatically route links to specific apps. When activated:

1. **Intercepts All Links**: Catches http/https intents system-wide
2. **Shows Chooser Dialog**: Displays available apps that can open the link
3. **Remembers Preferences**: Learns user's preferred app per domain
4. **Handles In-App Links**: Works with links opened within other apps
5. **AMP Link Resolution**: Automatically resolves AMP links to original articles
6. **Browser Integration**: Supports private browsing options

### Key Characteristics
- **System-Wide Interception**: Catches all link intents globally
- **Smart Learning**: Remembers preferences per domain
- **Privacy-Focused**: Local preferences, no cloud sync
- **Framework Integration**: Requires system-level intent interception
- **Customizable**: Extensive configuration options
- **Functional**: Actually intercepts and handles links (not just visual)

## Launch Location

### Navigation Path
```
Settings App 
  → Anatolia Settings (custom settings page)
    → Link Chooser (preference entry)
      → LinkSheet Settings Screen (main feature UI)
```

### Entry Point Details
- **File**: `res/xml/anatolia.xml`
- **Preference Key**: `linksheet_category`
- **Title**: "Link Chooser"
- **Summary**: "Choose which app opens links"
- **Fragment**: `com.android.settings.linksheet.LinkSheetSettingsFragment`

### Why Separate Entry in Anatolia Settings?
- ✅ **Testing First**: Easy to test independently
- ✅ **Framework Dependent**: Requires system-level changes
- ✅ **Follows Pattern**: Matches other system-level features
- ✅ **Can Move Later**: Can integrate into main Settings later

## User Experience Flow

### First-Time Link Opening

1. **User taps any link** in any app (browser, messaging, etc.)
2. **LinkSheet intercepts** the intent system-wide
3. **Chooser dialog appears** showing all apps that can open the link
4. **User sees domain** and available apps
5. **User selects app** and optionally checks "Always" to remember preference
6. **Link opens** in selected app
7. **Preference saved** for that domain

### Subsequent Link Opening

1. **User taps link** from same domain
2. **LinkSheet checks** saved preferences
3. **Link opens directly** in preferred app (no dialog)
4. **Long-press option** available to show chooser anyway

### Managing Preferences

1. **Open Settings** → Anatolia Settings → Link Chooser
2. **View saved preferences** by domain
3. **Edit or remove** domain preferences
4. **Configure global rules** and options
5. **View usage statistics** (which apps used for which domains)
6. **Reset preferences** if needed

## Technical Architecture

### Components

1. **LinkSheetService.java** (System Service)
   - Intercepts all ACTION_VIEW intents with http/https URIs
   - Applies domain preferences or shows chooser
   - Handles AMP link resolution
   - Manages service lifecycle

2. **LinkChooserActivity.java** (Chooser Activity)
   - Shows dialog with list of available apps
   - Handles user selection and preference saving
   - Displays domain information

3. **LinkPreferencesDatabase.java** (SQLite)
   - Stores preferred app per domain
   - Tracks usage statistics
   - Manages app rules and custom settings

4. **LinkSheetSettingsFragment.java** (UI Fragment)
   - Configure enable/disable
   - Manage domain preferences
   - Configure AMP resolution and private browsing
   - View statistics and reset options

### Resource Organization (Portable Design)

All resources are isolated in separate files for easy porting:

- **Strings**: `res/values/linksheet_strings.xml`
- **Dimens**: `res/values/linksheet_dimens.xml`
- **Colors**: `res/values/linksheet_colors.xml`
- **Drawables**: `res/drawable/linksheet_*.xml`
- **Layouts**: `res/layout/link_chooser_activity.xml`, `res/xml/linksheet_settings.xml`

### Framework Integration (Required)

**Required Framework Changes:**

1. **Settings.Secure Keys** (`frameworks/base/core/java/android/provider/Settings.java`):
   - `LINKSHEET_ENABLED` - Enable/disable service
   - `LINKSHEET_AUTO_RESOLVE_AMP` - AMP link resolution
   - `LINKSHEET_ENABLE_PRIVATE_BROWSING` - Private browsing option

2. **High-Priority Intent Filter** - Service needs priority="999" to intercept intents before other apps

**No Other Framework Changes**:
- No SystemUI modifications
- No system service creation (uses Settings app service)
- Uses existing Android Intent system
- Uses existing PackageManager APIs

## Functional Requirements (Must Work)

### Core Functionality
✅ **Link Interception**: Actually intercepts http/https intents system-wide
✅ **App Chooser**: Shows list of apps that can handle the link
✅ **App Selection**: Opens link in selected app correctly
✅ **Preference Memory**: Remembers user's preferred app per domain
✅ **Preference Application**: Uses remembered preferences automatically
✅ **Domain Matching**: Correctly matches subdomains to domain rules

### Advanced Functionality
✅ **AMP Resolution**: Automatically resolves AMP links to original articles
✅ **In-App Links**: Handles links opened within other apps
✅ **Browser Integration**: Offers private browsing options for supported browsers
✅ **Usage Statistics**: Tracks which apps are used for which domains
✅ **App Rules**: Allows configuring rules for specific apps

### Not Just Visual
- Service actually intercepts system intents before other apps
- Preferences actually stored and applied per domain
- Links actually open in chosen apps
- AMP links actually resolved to originals
- Statistics actually tracked and displayed

## Implementation Status

- ✅ **Plan Created**: Complete with all details
- ⏳ **Framework Changes**: Pending (Settings.Secure keys)
- ⏳ **Settings Implementation**: Pending (Service, Activity, Database, Fragment)
- ⏳ **Resources**: Pending (strings, dimens, colors, layouts)
- ⏳ **Testing**: Pending

## Files Created/Modified

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
- `res/values/linksheet_dimens.xml`
- `res/values/linksheet_colors.xml`
- `res/drawable/ic_linksheet.xml`

**Modified**:
- `res/xml/anatolia.xml` - Add LinkSheet preference entry
- `AndroidManifest.xml` - Add service and activity declarations

## Design Principles Followed

1. **Portability**: All resources isolated, easy to extract
2. **Framework Integration**: Minimal but necessary for functionality
3. **Functional**: Actually intercepts and handles links, not just visual
4. **Privacy-First**: Local storage, no cloud, no tracking
5. **System-Level**: Works globally across all apps
6. **Clean Architecture**: Follows Android and LineageOS patterns
7. **Testable**: Launched from Anatolia for easy testing
8. **Documented**: Comprehensive plan with all implementation details

## Key Implementation Details

### Intent Interception Strategy
- System service with high-priority intent filter (priority="999")
- Intercepts ACTION_VIEW intents with http/https schemes
- Applies domain preferences or shows chooser

### Domain Preference System
- Exact domain matching first (example.com)
- Subdomain fallback (sub.example.com → example.com)
- Wildcard support for custom rules
- SQLite storage with efficient queries

### AMP Link Resolution
- Detects AMP links by URL patterns
- Extracts canonical URL from AMP page metadata
- Automatic resolution when enabled
- Fallback to original URL if resolution fails

### Privacy and Security
- All preferences stored locally in encrypted database
- No internet access for link handling (except AMP resolution)
- No data sent to external servers
- Respects user privacy settings

## Limitations & Considerations

### Limitations
1. **Framework Required**: Needs Settings.Secure keys for full functionality
2. **High Priority**: Intent filter priority may conflict with other link handlers
3. **Android 12+**: Designed for modern Android versions
4. **App Compatibility**: Some apps may bypass standard intent system

### Considerations
1. **Performance**: Minimal impact on link opening speed
2. **Battery**: Low battery usage for background service
3. **User Education**: Users need to understand the feature
4. **Compatibility**: Works with standard Android link handling

## Future Enhancements

- **Advanced Domain Rules**: Regex patterns and complex matching
- **App Integration**: Direct integration with browsers and apps
- **Link History**: Complete history of opened links and choices
- **Bulk Management**: Import/export preferences
- **Custom Browsers**: Support for more private browsing options
- **Link Previews**: Show link content before choosing app
- **Network Rules**: Different rules for different network types

