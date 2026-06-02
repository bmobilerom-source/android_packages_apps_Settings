# Dashboard picker pages

Four separate settings screens let users choose homepage layouts. All share the same logic (`BrandDashboardSettings`) but differ in **title**, **style list**, and **bottom bar**.

## 1. Custom Dashboard (full list)

| | |
|--|--|
| **Fragment** | `com.bmobile.fragments.CustomDashboardSettings` |
| **XML** | `res/xml/custom_dashboard_settings.xml` |
| **Title string** | `custom_dashboard_title` |
| **Bottom bar** | Yes (reset / SystemUI / Display) |
| **Deep link** | `adb shell am start -a com.android.settings.CUSTOM_DASHBOARD_SETTINGS` |
| **Activity** | `Settings.CustomDashboardActivity` |

**Opened from:** Material, V2, MainUser, Display settings, card navigation, many legacy homepages.

---

## 2. YR Custom Dashboard

| | |
|--|--|
| **Fragment** | `com.bmobile.fragments.YrCustomDashboardSettings` |
| **XML** | `res/xml/yr_custom_dashboard_settings.xml` |
| **Title string** | `yr_custom_dashboard_title` |
| **Styles** | YR Study (1), YR School (11), YR Social (13), YR Expressive (14) |
| **Default on reset** | 11 (YR School) |
| **Bottom bar** | Yes |
| **Deep link** | `com.android.settings.YR_CUSTOM_DASHBOARD_SETTINGS` |

**Opened from:** `top_level_settings_oos11.xml`, `top_level_settings_afterlabs_grid.xml`, `top_level_settings_yr_expressive.xml`

---

## 3. BMobile Dashboard

| | |
|--|--|
| **Fragment** | `com.bmobile.fragments.BMobileDashboardSettings` |
| **XML** | `res/xml/bmobile_dashboard_settings.xml` |
| **Title string** | `bmobile_dashboard_title` |
| **Styles** | Cards (6), Home (7), Expressive (8), Icons (10), DynamicTabs (12), Neo (15) |
| **Default on reset** | 7 (BMobile Home) |
| **Bottom bar** | Yes |
| **Deep link** | `com.android.settings.BMOBILE_DASHBOARD_SETTINGS` |

**Opened from:** `top_level_settings_fun_display.xml`, `top_level_settings_bmobile_expressive.xml`

---

## 4. KidsSafe Dashboard

| | |
|--|--|
| **Fragment** | `com.bmobile.fragments.KidsSafeDashboardSettings` |
| **XML** | `res/xml/kidssafe_dashboard_settings.xml` |
| **Title string** | `kidssafe_dashboard_title` |
| **Styles** | KS School (5), KS Fun (16) |
| **Default on reset** | 16 (KS Fun) |
| **Bottom bar** | **No** |
| **Deep link** | `com.android.settings.KIDSSAFE_DASHBOARD_SETTINGS` |

**Opened from:** `top_level_settings_classic.xml`, `top_level_settings_ks_fun.xml`

---

## Picker page UI

Each picker shows:

1. **Active layout** — read-only summary of current `settings_dashboard_style`
2. **Dashboard style** — `ListPreference` bound to brand-specific string array

After selecting a style, Settings home **reopens automatically** and a toast shows `dashboard_style_applied`.
