# Planning Agent Rules for LineageOS Settings Features

## Core Principles

### 1. Resource Organization (CRITICAL for Portability)
When planning any new feature, ALWAYS create separate, isolated resource files:

**Required Separate Files**:
- `res/values/{feature}_strings.xml` - All string resources
- `res/values/{feature}_dimens.xml` - All dimension resources  
- `res/values/{feature}_colors.xml` - All color resources
- `res/drawable/{feature}_*.xml` - All drawable resources with feature prefix

**Why**: 
- Enables easy porting to other ROMs
- Prevents conflicts with existing Settings resources
- Maintains clean separation of concerns
- Follows Android best practices

**Example**: SOS feature uses `sos_strings.xml`, `sos_dimens.xml`, `sos_colors.xml`

### 2. Integration Strategy

**Step 1: Analyze Source Repository/App**
- Review the external repository or app you're planning from
- Identify reusable layouts, components, and code patterns
- Extract Android-compatible code that works in Settings context
- Note any dependencies or requirements

**Step 2: Find Best Integration Point**
- Determine the cleanest way to integrate into Settings app
- Prefer existing Android APIs over custom implementations
- Use app-level services (ForegroundService) over system services when possible
- Minimize framework changes

**Step 3: Ensure Functionality**
- Features must actually WORK, not just display visually
- Services must perform real operations (BLE, sensors, etc.)
- UI must reflect actual state, not just visual representation
- Handle permissions, errors, and edge cases properly

### 3. Framework Changes (Minimize)

**Prefer**:
- Settings.Secure keys for state storage
- App-level services (ForegroundService)
- Existing Android APIs (BLE, Sensors, etc.)
- Settings app modifications only

**Avoid**:
- System service creation unless absolutely necessary
- SystemUI modifications if possible
- Framework-level changes beyond Settings keys
- Custom system APIs

**Example**: SOS feature only adds 4 Settings.Secure keys, uses ForegroundService in Settings app

### 4. Launch Point for Testing

**Always**: Launch from Anatolia Settings page (`res/xml/anatolia.xml`) for initial testing

**Why**:
- Custom settings area for testing new features
- Easy to enable/disable for testing
- Can be moved to main Settings later if desired
- Follows existing pattern in codebase

**Implementation**:
```xml
<!-- In res/xml/anatolia.xml -->
<PreferenceScreen
    android:key="{feature}_category"
    android:title="@string/{feature}_settings_title"
    android:summary="@string/{feature}_settings_summary"
    android:fragment="com.android.settings.{feature}.{Feature}SettingsFragment" />
```

### 5. Feature Structure Template

**Java/Kotlin Files**:
```
src/com/android/settings/{feature}/
├── {Feature}Service.java - Service (if needed)
├── {Feature}SettingsFragment.java - Main UI fragment
├── {Feature}Controller.java - Preference controller
└── {Feature}Helper.java - Helper utilities (if needed)
```

**Resource Files**:
```
res/
├── xml/{feature}_settings.xml - Preference screen
├── values/{feature}_strings.xml - Strings
├── values/{feature}_dimens.xml - Dimensions
├── values/{feature}_colors.xml - Colors
└── drawable/{feature}_*.xml - Drawables
```

**Manifest**:
```xml
<!-- In AndroidManifest.xml -->
<service
    android:name="com.android.settings.{feature}.{Feature}Service"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="..." />
```

### 6. Functional Requirements Checklist

Before finalizing plan, ensure:
- [ ] Feature actually performs its function (not just visual)
- [ ] Services work correctly (BLE, sensors, etc.)
- [ ] Permissions are handled properly
- [ ] Error cases are handled
- [ ] UI reflects actual state
- [ ] Resources are isolated and portable
- [ ] Framework changes are minimal
- [ ] Launch point is Anatolia Settings page
- [ ] All strings/dimens/colors in separate files

### 7. Example: SOS Emergency Signal Feature

**What It Does**:
- Broadcasts SOS signal via BLE when toggled ON
- Plays audible siren (SOS pattern)
- Shows persistent notification
- Works completely offline

**Where It Launches**:
- Anatolia Settings page → "Emergency SOS" → SOS Settings screen

**Resources**:
- `sos_strings.xml` - All strings
- `sos_dimens.xml` - Dimensions (to be created)
- `sos_colors.xml` - Colors (to be created)
- `ic_sos.xml` - Icon drawable

**Framework Changes**:
- Only 4 Settings.Secure keys added
- No SystemUI changes
- Uses ForegroundService in Settings app

**Functionality**:
- Actually broadcasts BLE signals ✅
- Actually plays siren ✅
- Service persists correctly ✅
- UI reflects real state ✅

## Planning Workflow

1. **Analyze Source**: Review external repo/app structure and code
2. **Identify Patterns**: Extract reusable layouts, components, APIs
3. **Design Integration**: Find best way to integrate into Settings
4. **Plan Resources**: Create separate resource files for portability
5. **Minimize Framework**: Use app-level services, existing APIs
6. **Ensure Functionality**: Plan for real functionality, not just UI
7. **Set Launch Point**: Use Anatolia Settings for testing
8. **Document Everything**: Create comprehensive plan document

## Key Reminders

- **Portability First**: Every feature should be easily portable
- **Function Over Form**: Features must work, not just look good
- **Minimal Changes**: Prefer app-level over framework-level
- **Clean Organization**: Separate resources, clear structure
- **Testable**: Launch from Anatolia for easy testing
- **Documented**: Comprehensive plans with all details

