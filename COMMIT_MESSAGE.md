feat: Port bMobile User & Accounts sections and Atomichub card system

This commit ports the complete bMobile User & Accounts sections and 
card navigation system from simplnc/android_packages_apps_Settings (clean-commits
branch). This includes all fragments, controllers, layouts, resources, and
dependencies required for a fully functional implementation.


Components Added:

Core Fragments:
- BMobileUserInfoFragment: User information screen with authentication
- BMobileAccountsFragment: Account management with privacy-focused selection
- MySessionFragment: Session Messenger integration dashboard
- BMobileSettingsFragment: bMobile services and privacy controls
- SystemApplicationSettings: Application management dashboard
- SystemOptimizationSettings: System performance optimization controls
- DeviceTweaksSettings: Device tweaks and power management
- BLocationSettings: Enhanced location privacy and status settings
- AppSecSettings: Application security controls dashboard
- GestureSettings: Custom gesture configuration
- PrivateSpaceDashboardFragment: Private Space management

View Classes:
- AtomichubView: Original 4-card grid navigation widget
- Atomichub2View: Enhanced 7-card horizontal scroll + large card widget
- AtomichubCardsPreference: Preference wrapper for atomichub layout
- Atomichub2CardsPreference: Preference wrapper for atomichub2 layout

Controllers (Preference Visibility & Logic):
- SessionMessengerController: Conditional visibility for Session Messenger
- DailyJournalController: Conditional visibility for Daily Journal
- MicroGPreferenceController: MicroG service launcher
- AuroraServicesPreferenceController: Aurora Services launcher
- FirewallPreferenceController: Firewall app launcher
- AdServicesPreferenceController: Ad Services controls
- BackgroundAppLimitsController: Background process limits toggle
- NetworkOptimizationController: Network optimization toggle
- BatteryOptimizationController: Battery optimization toggle
- StorageOptimizationController: Storage optimization toggle
- ThermalThrottlingController: Thermal throttling toggle
- OptimizationStatusController: System optimization status summary
- FastChargingController: Fast charging toggle
- StayAwakeChargingController: Stay awake while charging toggle
- UsbDebuggingController: USB debugging toggle
- DeveloperOptionsController: Developer options toggle
- LocationPrivacyIndicatorsController: Location access indicators
- LocationPrecisionController: Location precision selector (precise/approximate/coarse)
- LocationServiceStatusController: GPS/Network location status
- LocationPrivacyFooterController: Location privacy footer
- SecurityScoreController: App security score calculator
- SecurityFooterController: App security footer
- BlockAppDashboardController: Block app dashboard toggle
- InstallAppWhitelistController: Install app whitelist toggle
- BlockUsbPopupController: Block USB popup toggle
- BlockLocationSettingsController: Block location settings toggle
- BlockAccountDashboardController: Block account dashboard toggle
- BlockSafetyCenterController: Block Safety Center toggle

Helper Classes:
- SystemOptimizationHelper: System optimization utilities
- PowerTweaksHelper: Power management utilities
- LocationPrivacyServiceHelper: Location privacy service utilities
- CardNavigationHelper: Card navigation utilities
- Additional adapter and helper classes for grid layouts

XML Preference Screens:
- userinfo_pref.xml: Main user information screen with atomichub2
- bmobile_userinfo_pref.xml: bMobile user info with quick access
- bmobile_accounts_pref.xml: bMobile accounts management
- my_session_settings.xml: My Session dashboard
- private_space_settings.xml: Private Space configuration
- private_space_biometric_settings.xml: Private Space biometric settings
- private_space_auto_lock_settings.xml: Private Space auto-lock
- private_space_hide_locked.xml: Private Space hide settings
- anatolia_settings_gestures.xml: Gesture settings
- bmobile_settings.xml: bMobile services dashboard
- system_optimization_settings.xml: System optimization dashboard
- appsec.xml: Application security dashboard
- system_application.xml: System application management
- device_tweaks_settings.xml: Device tweaks dashboard
- blocation.xml: Enhanced location privacy settings

Layout Files:
- atomichub.xml: Original 4-card grid layout
- atomichub2.xml: Enhanced 7-card horizontal scroll + large card layout
- bmobile_user_info_frag.xml: bMobile user info header layout
- bmobile_accounts_info_frag.xml: bMobile accounts header layout
- bmobile_banner_card.xml: bMobile banner card layout
- mainuser_profile_card.xml: Main user profile card with Lottie animation
- All adaptive_preference_card variants (top, middle, bottom, switch variants)

Drawable Resources:
- Atomichub2 card backgrounds (ic_ui_card2, ic_theme_card2, ic_status_card2,
  ic_button_card2, ic_misc_card2, ic_team_card2, ic_ui_big_card2)
- bMobile card backgrounds (bmobile_card_top, bmobile_card_mid, bmobile_card_bot)
- Adaptive preference card backgrounds (day and night mode variants)
- Icons (user_png, bar, multiuser, manage_account, emergency,
  android_version_logo, custom_preference_background, etc.)

Raw Resources (Lottie Animations):
- profile_persons.json: Profile card animation
- display.json: My Session animation
- bmobSettings.json: bMobile Settings animation
- optimise.json: System optimization animation
- powermenu1.json: AppSec animation
- app_illustration.json: System application animation
- location.json: BLocation animation
- gesture_settings.json: Gesture settings animation (if available)

Consolidated Resource Files:
- bmobile_porting_strings.xml: All bMobile-related strings
- bmobile_porting_colors.xml: All bMobile-related colors
- bmobile_porting_dimens.xml: All bMobile-related dimensions
- bmobile_porting_styles.xml: All bMobile-related styles
- bmobile_porting_arrays.xml: All bMobile-related arrays (location precision)

Navigation Integration:
- Updated SettingsGateway.java with fragment registrations for all new fragments
- All fragments properly registered in ENTRY_FRAGMENTS array

Features:
- User information display with account email and avatar selection
- Account management with privacy-focused account selection
- Atomichub card navigation system (original 4-card and enhanced 7-card versions)
- Session Messenger integration (conditional visibility)
- Daily Journal integration (conditional visibility)
- Private Space management
- bMobile services dashboard (MicroG, Aurora Services, Firewall, Ad Services)
- Enhanced location privacy controls (precision, indicators, status)
- System optimization controls (background limits, network, battery, storage, thermal)
- Device tweaks (fast charging, stay awake, USB debugging, developer options)
- Application security dashboard with security score
- Custom gesture settings
- System application management

Authentication:
- Device credential authentication for sensitive settings (BMobileUserInfoFragment,
  BMobileSettingsFragment, BLocationSettings)
- KeyguardManager integration for secure access

Dependencies:
- MaterialCardView for card layouts
- LottieAnimationView for animations
- SubSettingLauncher for fragment navigation
- AccountManager for account information
- SettingsPreferenceFragment and DashboardFragment base classes

Testing:
- SettingsGateway.java updated with proper fragment registrations
- Resource files consolidated for easy maintenance

This implementation provides a complete, plug-and-play port of the bMobile User &
Accounts sections and navigation system.

