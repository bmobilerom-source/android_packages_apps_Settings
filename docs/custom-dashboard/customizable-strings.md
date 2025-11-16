# Customizable strings reference

All user-visible strings you can change to personalize Settings for a customer **without Java changes**. Primary file: **`res/values/system_basic_strings.xml`**.

Legend:

- **Picker** = shown in a dashboard style list
- **Tile** = homepage row that opens a picker page
- **Header** = banner at top of a homepage layout
- **Toast** = short message after an action

---

## 1. Dashboard picker pages (tile title + summary)

These are the **entry points** — what the customer sees when opening layout settings from a homepage row.

| String name | Default | Used on |
|-------------|---------|---------|
| `custom_dashboard_title` | Custom Dashboard | Full picker tile, card nav, Display page link |
| `custom_dashboard_summary` | Choose the Settings home page layout | Tile subtitle |
| `yr_custom_dashboard_title` | YR Custom Dashboard | YR homepages (OOS11, YR Social, YR Expressive) |
| `yr_custom_dashboard_summary` | Choose a YR Settings home page layout | Tile subtitle |
| `kidssafe_dashboard_title` | KidsSafe Dashboard | KS School, KS Fun homepages |
| `kidssafe_dashboard_summary` | Choose a KidsSafe Settings home page layout | Tile subtitle |
| `bmobile_dashboard_title` | BMobile Dashboard | BMobile Home, BMobile Expressive homepages |
| `bmobile_dashboard_summary` | Choose a BMobile Settings home page layout | Tile subtitle |

**Customer example:** Rename `bmobile_dashboard_title` to `Acme Layouts` and `bmobile_dashboard_summary` to `Pick your Acme Settings home screen`.

---

## 2. Style names in pickers (most important for customers)

Each **`dashboard_style_top_level_*`** string is the **label shown in the dropdown** when choosing a layout. Rename these to match your product names.

| String name | Style ID | Default label | Homepage XML |
|-------------|----------|---------------|--------------|
| `dashboard_style_top_level_settings` | 0 | MainUser | `top_level_settings.xml` |
| `dashboard_style_top_level_epic` | 1 | YR Study | `top_level_settings_epic.xml` |
| `dashboard_style_top_level_v2` | 2 | V2 · top_level_settings_v2.xml | `top_level_settings_v2.xml` |
| `dashboard_style_top_level_material` | 4 | Material · … | `top_level_settings_material.xml` |
| `dashboard_style_top_level_classic` | 5 | KS School | `top_level_settings_classic.xml` |
| `dashboard_style_top_level_custom_v2` | 6 | BMobile Cards | `top_level_settings_custom_v2.xml` |
| `dashboard_style_top_level_fun_display` | 7 | BMobile Home | `top_level_settings_fun_display.xml` |
| `dashboard_style_top_level_bmobile_expressive` | 8 | BMobile Expressive | `top_level_settings_bmobile_expressive.xml` |
| `dashboard_style_top_level_aosp` | 10 | BMobile Icons | `top_level_settings_aosp.xml` |
| `dashboard_style_top_level_oos11` | 11 | YR School | `top_level_settings_oos11.xml` |
| `dashboard_style_top_level_afterlabs_tab` | 12 | DynamicTabs | `top_level_settings_afterlabs_tab.xml` |
| `dashboard_style_top_level_afterlabs_grid` | 13 | YR Social | `top_level_settings_afterlabs_grid.xml` |
| `dashboard_style_top_level_yr_expressive` | 14 | YR Expressive | `top_level_settings_yr_expressive.xml` |
| `dashboard_style_top_level_bmobile_neo` | 15 | BMobile Neo | `top_level_settings_bmobile_neo.xml` |
| `dashboard_style_top_level_ks_fun` | 16 | KS Fun | `top_level_settings_ks_fun.xml` |

**Which picker shows which styles:**

| Picker | Array in `arrays.xml` | Style IDs included |
|--------|----------------------|-------------------|
| Custom Dashboard (all) | `settings_dashboard_style_entries` | 0,1,2,4,5,6,7,8,10,11,12,13,14,15,16 |
| YR Custom Dashboard | `settings_dashboard_style_yr_entries` | 1, 11, 13, 14 |
| BMobile Dashboard | `settings_dashboard_style_bmobile_entries` | 6, 7, 8, 10, 12, 15 |
| KidsSafe Dashboard | `settings_dashboard_style_kidssafe_entries` | 5, 16 |

To **hide** a style from a brand picker, edit the array (not just the string). To **rename** only, edit the string above.

---

## 3. Picker UI labels (inside dashboard settings pages)

| String name | Default | Where |
|-------------|---------|-------|
| `dashboard_style_title` | Dashboard style | ListPreference title |
| `dashboard_style_summary` | Applies when you open Settings home | ListPreference subtitle |
| `custom_dashboard_current_style_title` | Active layout | Read-only row showing current style |
| `dashboard_style_applied` | Dashboard style changed to %1$s | Toast after apply (%1$s = style label) |
| `dashboard_style_reset_title` | Reset dashboard style | Bottom bar button |
| `dashboard_style_reset_summary` | Restore default layout | (reserved / menu) |
| `custom_dashboard_load_error` | Could not open Custom Dashboard: %1$s | Error toast |
| `yr_custom_dashboard_load_error` | Could not open YR Custom Dashboard: %1$s | Error toast |
| `kidssafe_dashboard_load_error` | Could not open KidsSafe Dashboard: %1$s | Error toast |
| `bmobile_dashboard_load_error` | Could not open BMobile Dashboard: %1$s | Error toast |

---

## 4. Bottom toolbar (Custom / YR / BMobile pickers)

KidsSafe Dashboard **has no bottom bar**. Others show:

| String name | Default | Button |
|-------------|---------|--------|
| `dashboard_style_reset_title` | Reset dashboard style | Reset to default style |
| `systemui_reset_title` | Restart SystemUI | Restart SystemUI |
| `systemui_reset_summary` | Apply SystemUI changes | (description) |
| `systemui_reset_success` | SystemUI restarted | Toast |
| `systemui_reset_failed` | Could not restart SystemUI | Toast |
| `custom_dashboard_nav_styles` | Styles | (legacy nav) |
| `custom_dashboard_nav_layout` | Layout | (legacy nav) |
| `custom_dashboard_nav_apply` | Apply & home | (legacy nav) |

Display button uses standard **`display_settings`** from AOSP strings.

---

## 5. Customer OS header (BMobile Cards — style 6)

The **Customer OS card** at the top of **BMobile Cards** (`top_level_settings_custom_v2.xml`).

| String name | Default | Layout |
|-------------|---------|--------|
| `customeros_header_title` | Customer OS | `res/layout/CustomerOS_header.xml` |
| `customeros_header_summary` | Customized Android experience | Same |

Tap opens user profile (`UserInfoFragem`).

**Customer example:**

```xml
<string name="customeros_header_title">Acme Mobile OS</string>
<string name="customeros_header_summary">Built for Acme customers</string>
```

---

## 6. Per-layout style headers

Shown on some sub-settings / style-specific screens:

| String name | Default |
|-------------|---------|
| `epic_style_header_title` | Epic Style |
| `epic_style_header_summary` | Customized Settings Experience |
| `material_style_header_title` | Material Style |
| `material_style_header_summary` | Material Design 3 inspired interface |
| `classic_style_header_title` | Classic Style |
| `classic_style_header_summary` | Traditional categorized layout |
| `fun_display_settings_title` | BMobile Home Settings |
| `bmobile_expressive_settings_title` | BMobile Expressive Settings |

---

## 7. AfterLabs / DynamicTabs labels (styles 12 & 13)

| String name | Default | Used for |
|-------------|---------|----------|
| `afterlabs_tab_connectivity` | Main | Tab strip label |
| `afterlabs_tab_personalize` | Media | Tab strip label |
| `afterlabs_tab_system` | System | Tab strip label |
| `afterlabs_tab_security` | Security | Tab strip label |
| `afterlabs_card_bluetooth_title` | Bluetooth | Grid card |
| `afterlabs_user_title` | User | Tab content |
| `connectivity_category_title` | Connectivity | Category (Custom V2) |
| `customize_layout_mode_title` | AfterLabs layout mode | Hidden pref (optional) |
| `customize_layout_mode_summary` | Grid cards or tabbed… | Hidden pref |
| `customize_layout_mode_grid` | Grid (wide cards) | Layout mode |
| `customize_layout_mode_tabbed` | Tabbed categories | Layout mode |
| `customize_layout_mode_applied` | Layout mode set to %1$s | Toast |

---

## 8. Homepage widgets (battery / storage strip)

File: **`res/values/strings_homepage_widgets.xml`**

| String name | Default |
|-------------|---------|
| `homepage_widgets_title` | Homepage widgets |
| `homepage_widgets_summary` | Show battery and storage widgets |
| `extended_homepage_widgets_title` | Extended homepage widgets |
| `extended_homepage_widgets_summary` | Show extended battery and storage widgets |
| `homepage_widget_battery_title` | Battery |
| `homepage_widget_storage_title` | Storage |
| `homepage_widget_battery_charge` | Charging |
| `homepage_widget_battery_discharge` | Discharging |

---

## 9. User profile card copy

File: **`res/values/banana_strings.xml`**

| String name | Default | Where |
|-------------|---------|-------|
| `userinfo_bubble_setting_description` | Welcome to your Control Center… | User info sub-screen intro |
| `usercard_summary` | Tap for more info | Profile card hint |
| `default_user` | Default User | Fallback name |

KidsSafe profile intro: **`res/values/kidssafe_strings.xml`**

| String name | Default |
|-------------|---------|
| `kidssafe_userinfo_summary` | Your KidsSafe profile and quick settings. |

---

## 10. AOSP homepage bottom bar (style 10 — BMobile Icons)

| String name | Default |
|-------------|---------|
| `top_level_dashboard_nav_network` | Network |
| `top_level_dashboard_nav_display` | Display |
| `top_level_dashboard_nav_system` | System |
| `top_level_dashboard_nav_wallpaper` | Wallpaper |

---

## 11. Card navigation row (BMobile Cards quick cards)

Uses **`custom_dashboard_title`** for the Custom Dashboard card. Network/Display use AOSP **`network_dashboard_title`** / **`display_settings`**.

Layout: `res/layout/toplevel_card_navigation.xml`

---

## 12. Strings that are usually *not* customer-facing

| String | Note |
|--------|------|
| `dashboard_style_top_level_v2` / `_material` | Include internal XML filename — safe to simplify for customers |
| `custom_dashboard_access_*` | Developer / Infinity Suite help text |
| `settings_dashboard_style_values` | Numeric IDs — **do not translate or rename** |

---

## Per-customer overlay pattern (recommended)

For each customer build, prefer a **product overlay** instead of editing defaults:

```
vendor/<customer>/overlay/packages/apps/Settings/res/values/customer_strings.xml
```

Override only the strings that differ. Example minimal overlay:

```xml
<resources>
    <string name="customeros_header_title">Riverdale Schools OS</string>
    <string name="customeros_header_summary">Settings for Riverdale devices</string>
    <string name="dashboard_style_top_level_ks_fun">Riverdale Fun</string>
    <string name="dashboard_style_top_level_classic">Riverdale School</string>
    <string name="kidssafe_dashboard_title">Riverdale Layouts</string>
</resources>
```

Rebuild Settings after overlay changes.
