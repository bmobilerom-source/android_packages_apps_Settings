# AOSPMods (PixelXpert) Remaining Features Analysis

## Summary
Complete catalog of all AOSPMods features not yet implemented in LineageOS Settings. Each feature includes description, implementation complexity, and framework requirements.

## Status Bar Features (Partially Implemented)
✅ **Implemented**: Basic framework and UI structure
❌ **Missing**: Real functionality (requires SystemUI modifications)

### Already Added (Need Framework Work):
- Status bar height adjustment (50-200%)
- Notification icon limit (1-10 icons)
- Combined signal icons (WiFi + Mobile merge)
- Hide roaming indicator
- VoLTE/VoWiFi icon toggles
- Hide privacy chip (mic/camera indicators)
- Multi-row system icons
- Multi-row notification icons
- Network traffic indicator in status bar

## Quick Settings Features (Partially Implemented)
✅ **Implemented**: Basic framework and UI structure
❌ **Missing**: Real functionality (requires SystemUI modifications)

### Already Added (Need Framework Work):
- Volume unmute percentage (0-100%)
- Leveled flashlight with brightness control
- Global flashlight level application
- QS tile label size scaling (-50 to +50)
- QS tile secondary label scaling (-50 to +50)
- QS pull-down gestures with size control (10-50%)
- QS pull-down side selection (left/right)
- One-finger pull-up gesture

## Lock Screen Features (Not Implemented)
🔴 **High Priority** - Popular user features

### Depth Wallpaper Effects
- **Description**: AI-powered dynamic wallpaper integration with lock screen clock
- **Functionality**: Uses ML model to separate foreground/background, applies depth effects
- **Models**: Support for different AI segmentation models (u2net, etc.)
- **Options**: Opacity control (25-255), AOD (Always On Display) support
- **Complexity**: High - Requires ML model integration, SystemUI modifications
- **Framework**: SystemUI, ML Kit integration

### Fingerprint Customizations
- **Description**: Hide fingerprint icon circle or foreground icon
- **Functionality**: Makes under-display fingerprint sensor invisible
- **Options**: Hide circle background, hide foreground icon
- **Complexity**: Medium - SystemUI modifications needed
- **Framework**: SystemUI, Keyguard modifications

### Lock Screen General Options
- **Description**: Double-tap to sleep on lock screen
- **Functionality**: Tap empty area to turn off screen
- **Complexity**: Low - Can use existing power management APIs
- **Framework**: PowerManager, Keyguard

### Advanced Lock Screen Features
- **Description**: Hide user avatar, custom keyguard dimming, shuffle PIN layout
- **Functionality**: Security and privacy enhancements
- **Complexity**: Medium - Keyguard and authentication modifications
- **Framework**: Keyguard, AuthenticationManager

### Custom Text on Lock Screen
- **Description**: Custom carrier text and clock area text
- **Functionality**: Replace default carrier name, add custom text near clock
- **Complexity**: Medium - SystemUI text rendering modifications
- **Framework**: SystemUI, TelephonyManager

## Gesture Navigation Features (Not Implemented)
🟡 **Medium Priority** - Core navigation enhancements

### Back Gesture Enhancements
- **Description**: Customizable back gesture height and sensitivity
- **Functionality**: Left/right back gesture height control (10-100%)
- **Complexity**: Medium - Gesture navigation system modifications
- **Framework**: SystemUI, Navigation gestures

### Custom Swipe Actions
- **Description**: Custom actions for edge swipes
- **Functionality**: Left/right swipe-up actions (apps, shortcuts, etc.)
- **Options**: 25% swipe percentage threshold, custom action selection
- **Complexity**: High - Gesture recognition and action system
- **Framework**: SystemUI, ActivityManager

### Navigation Pill Customization
- **Description**: Customize gesture navigation pill appearance
- **Functionality**: Pill width/height adjustment (50-300%), accent color toggle
- **Complexity**: Medium - Navigation bar UI modifications
- **Framework**: SystemUI, NavigationBar

## Hotspot Features (Not Implemented)
🟢 **Easy Priority** - Can use existing APIs

### Hotspot SSID Hiding
- **Description**: Hide hotspot name from device list
- **Functionality**: Makes hotspot invisible to other devices
- **Complexity**: Low - Uses existing WiFi tethering APIs
- **Framework**: WifiManager, TetheringManager

### Client Management
- **Description**: Set inactivity timeout and max connected clients
- **Functionality**: Auto-disconnect idle clients, limit concurrent connections (0-15)
- **Complexity**: Low - Existing tethering configuration
- **Framework**: TetheringManager, WifiManager

### Connectivity Workarounds
- **Description**: Bypass hotspot client approval requirements
- **Functionality**: Allow seamless connections without user approval
- **Complexity**: Medium - Tethering security modifications
- **Framework**: TetheringManager, NetworkStack

## Theming Features (Not Implemented)
🟡 **Medium Priority** - Visual customization

### Icon Pack System
- **Description**: Dynamic icon theming with custom icon packs
- **Functionality**: Apply themed icons across system and apps
- **Options**: Force themed launcher icons
- **Complexity**: High - Icon loading and theming system
- **Framework**: PackageManager, IconLoader, Launcher integration

## Package Manager Features (Not Implemented)
🔴 **High Priority** - Developer tools

### Security Bypass
- **Description**: Allow mismatched signatures and downgrades
- **Functionality**: Bypass Android security checks for 5 minutes
- **Options**: Allow signature mismatches, allow app downgrades
- **Complexity**: Medium - PackageManager modifications
- **Framework**: PackageManager, InstallSession

## Miscellaneous Features (Not Implemented)
Various system enhancements

### System Enhancements
- **Advanced Power Menu**: Expanded power options
- **Screenshot Sound**: Disable camera sound
- **Clipboard Smart Actions**: Enhanced clipboard functionality
- **Screen Rotation**: Allow all rotations
- **Display Override**: Custom resolution scaling (50-200%)
- **Brightness Range**: Custom brightness limits
- **Volume Steps**: Custom volume increment size

### Launcher Features
- **Clear All Reposition**: Move clear all button in recents

### Doze (AOD) Features
- **AOD Notification Limit**: Limit AOD notification icons
- **Fingerprint in Doze**: Enable fingerprint unlock on AOD
- **Double Tap to Wake**: Wake from AOD with double tap
- **Tap/Pick to Show**: AOD interaction controls
- **Force AOD Charging**: Keep AOD active while charging

### Notification Features
- **Expand All Icons**: Auto-expand notification icons
- **Heads Up Duration**: Custom heads-up notification timeout
- **Default Expansion**: Set default notification expansion state
- **Ongoing Dismiss**: Prevent dismissing ongoing notifications

### Monitoring & Time
- **Network Statistics**: Real-time network monitoring
- **NTP Time Sync**: Automatic time synchronization
- **Custom NTP Servers**: User-defined NTP servers

## Implementation Priority Recommendations

### Phase 1 (High Impact, Lower Complexity):
1. **Hotspot Features** - Use existing APIs, high user value
2. **Lock Screen Double-Tap Sleep** - Simple power management
3. **Fingerprint Hiding** - Popular privacy feature
4. **Custom Carrier Text** - Popular customization

### Phase 2 (Medium Impact, Medium Complexity):
1. **Gesture Navigation Pill** - Visual enhancement
2. **Back Gesture Height** - Navigation improvement
3. **Advanced Power Menu** - System enhancement
4. **Screenshot Sound Toggle** - Quality of life

### Phase 3 (High Impact, High Complexity):
1. **Depth Wallpaper** - Premium feature, requires ML
2. **Icon Pack System** - Major theming overhaul
3. **Custom Swipe Actions** - Advanced navigation
4. **Package Manager Bypass** - Developer tool

### Phase 4 (Framework Intensive):
1. **Status Bar Modifications** - Requires extensive SystemUI changes
2. **QS Tile Modifications** - Requires SystemUI changes
3. **Network Traffic Indicator** - Requires SystemUI changes

## Framework Requirements Summary

### Minimal Framework Changes:
- Hotspot features (existing APIs)
- Basic lock screen toggles (existing APIs)
- Power menu enhancements (existing APIs)

### Moderate Framework Changes:
- Carrier text customization (TelephonyManager)
- Navigation pill theming (SystemUI surface)
- Fingerprint customizations (Keyguard)

### Major Framework Changes:
- Status bar height/icon limits (SystemUI deep integration)
- QS modifications (SystemUI deep integration)
- Gesture system enhancements (SystemUI/InputManager)
- Icon theming system (PackageManager/IconLoader)

## Recommendation
Start with Phase 1 features as they provide high user value with manageable complexity. The hotspot and basic lock screen features can be implemented with minimal framework changes and provide immediate user benefits.
