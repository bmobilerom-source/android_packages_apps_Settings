# ADB, defaults, and system keys

## System setting

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `settings_dashboard_style` | int (System) | 7 | Active homepage layout ID |
| `settings_compact_dashboard_enabled` | int/bool | false | Legacy compact flag (reset clears) |
| `declanxafterlab_style` | int (Secure) | 0 | AfterLabs layout mode (grid vs tabbed) |

Read current style:

```bash
adb shell settings get system settings_dashboard_style
```

Set style (examples):

```bash
adb shell settings put system settings_dashboard_style 7   # BMobile Home
adb shell settings put system settings_dashboard_style 16  # KS Fun
adb shell settings put system settings_dashboard_style 6   # BMobile Cards (Customer OS header)
```

Force-stop Settings after change if home does not refresh:

```bash
adb shell am force-stop com.android.settings
adb shell am start -a android.settings.SETTINGS
```

---

## Open picker pages

```bash
# Full list
adb shell am start -a com.android.settings.CUSTOM_DASHBOARD_SETTINGS

# Brand pickers
adb shell am start -a com.android.settings.YR_CUSTOM_DASHBOARD_SETTINGS
adb shell am start -a com.android.settings.BMOBILE_DASHBOARD_SETTINGS
adb shell am start -a com.android.settings.KIDSSAFE_DASHBOARD_SETTINGS
```

Package is usually `com.android.settings` (or your product package name).

---

## Broadcast on style change

Action: `com.android.settings.DASHBOARD_STYLE_CHANGED`

Sent by picker after successful apply. Listeners can refresh UI.

---

## Restart SystemUI (picker bottom bar)

Implementation: `SystemUiRestarter.java`

1. Broadcast `com.android.systemui.action.RESTART` with package `com.android.systemui` and data `package:com.android.systemui`
2. Fallback: `ActivityManager.forceStopPackageAsUser("com.android.systemui", USER_SYSTEM)`

Requires system/privileged Settings app. If it fails on a customer build, verify SystemUI includes `SysuiRestartReceiver`.

---

## Change code default style

Edit `DashboardStyleHelper.DEFAULT_STYLE` (currently **7**).

Brand picker reset defaults:

- `YrCustomDashboardSettings` → 11
- `KidsSafeDashboardSettings` → 16
- `BMobileDashboardSettings` / `CustomDashboardSettings` → 7

---

## Build

```bash
m Settings
```

Or full ROM image per your tree’s usual workflow.
