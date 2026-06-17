# Security Grid BMobile Port (`securitygrid_*`)

## Problem

The upstream BMobile/Anatolia Security & Privacy card dashboard lived under `com.epic.fragments` with scattered resource prefixes (`anatolia_*`, `xd_*`, `security_privacy_*`). That made the feature hard to maintain, merge, and ship as a self-contained LineageOS Settings customization.

## Solution

Port **Security Grid only** into `com.bmobile.fragments` with a portable, namespaced resource bundle: `securitygrid_*`.

System Grid was **not** renamed; it remains on the existing `anatolia_settings_system_grid` / `system_grid_*` resources from commit `c52a27c7ca9`.

## What was added

### Java (`src/com/bmobile/fragments/`)

| Class | Role |
|-------|------|
| `SecurityPrivacyGrid` | Main card dashboard fragment |
| `SecurityPrivacyGridAdapter` | RecyclerView adapter (card types, toggles, launches) |
| `SecurityInfoHeaderController` | Device info header on the security grid |
| `PocketModeSettings` | Pocket mode sub-screen |

### Resources

| Type | Files |
|------|--------|
| Preference XML | `res/xml/securitygrid_settings.xml`, `res/xml/securitygrid_pocket_mode.xml` |
| Layouts | `res/layout/securitygrid_two_column_grid.xml`, `securitygrid_header.xml`, `securitygrid_card_*.xml` |
| Values | `res/values/securitygrid_strings.xml`, `securitygrid_colors.xml`, `securitygrid_dimens.xml`, `securitygrid_styles.xml` |
| Drawable | `res/drawable/securitygrid_pref_card.xml` |

### Launch wiring

- **Dashboards** (`top_level_settings.xml`, `top_level_settings_v2.xml`, `top_level_settings_epic.xml`, `top_level_settings_custom_v2.xml`): Security entry → `com.bmobile.fragments.SecurityPrivacyGrid` with `@string/securitygrid_title` / `securitygrid_summary`.
- **Manifest**: `Settings$SecurityPrivacyGridActivity` with action `com.android.settings.SECURITY_GRID_SETTINGS`.
- **Settings.java**: `SecurityPrivacyGridActivity` inner class.

## How to launch

```bash
# Settings UI
Settings → Security (BMobile Security Grid)

# ADB
adb shell am start -a com.android.settings.SECURITY_GRID_SETTINGS
adb shell am start -n com.android.settings/.Settings\$SecurityPrivacyGridActivity
```

## Cards on the grid

1. Security header (device info)
2. Fingerprint
3. Pocket mode
4. Special access
5. Disable QS (toggle)
6. Lock screen
7. Screen timeout
8. Disable SAF (toggle)
9. Window ignore secure (toggle)

## Why this works

- **Isolation**: All Security Grid UI strings, dimensions, colors, and layouts use the `securitygrid_*` prefix, so merges with AOSP/LineageOS are predictable.
- **Stable class names**: Java classes keep `SecurityPrivacyGrid*` names to avoid breaking dashboard `android:fragment` references and adapter logic.
- **Fallback**: When a destination fragment is missing, the adapter falls back to `com.android.settings.security.SecuritySettings` with `securitygrid_title`.
- **Direct launch**: The manifest activity exposes the same fragment for deep links and QA without navigating the homepage.

## Build note

Rebuild Settings after syncing:

```bash
m Settings
```

## Related commit

- System Grid (unchanged naming): `Settings: port SystemGrid card dashboard from upstream`
- This port: Security Grid as portable `securitygrid_*` bundle
