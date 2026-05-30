# Dashboard style map

Complete reference: style ID → user label → homepage XML → notable UI.

| ID | Label string | Homepage XML | Bottom bar on home | Custom Dashboard tile on home |
|----|--------------|--------------|--------------------|------------------------------|
| 0 | `dashboard_style_top_level_settings` | `top_level_settings.xml` | Yes | Custom Dashboard |
| 1 | `dashboard_style_top_level_epic` | `top_level_settings_epic.xml` | No | Custom Dashboard (hidden in some builds) |
| 2 | `dashboard_style_top_level_v2` | `top_level_settings_v2.xml` | Yes | Custom Dashboard |
| 4 | `dashboard_style_top_level_material` | `top_level_settings_material.xml` | Yes | Custom Dashboard |
| 5 | `dashboard_style_top_level_classic` | `top_level_settings_classic.xml` | No | KidsSafe Dashboard |
| 6 | `dashboard_style_top_level_custom_v2` | `top_level_settings_custom_v2.xml` | Yes | — (Customer OS header) |
| 7 | `dashboard_style_top_level_fun_display` | `top_level_settings_fun_display.xml` | No | BMobile Dashboard |
| 8 | `dashboard_style_top_level_bmobile_expressive` | `top_level_settings_bmobile_expressive.xml` | No | BMobile Dashboard |
| 10 | `dashboard_style_top_level_aosp` | `top_level_settings_aosp.xml` | Yes | Custom Dashboard |
| 11 | `dashboard_style_top_level_oos11` | `top_level_settings_oos11.xml` | Floating OOS11 bar | YR Custom Dashboard |
| 12 | `dashboard_style_top_level_afterlabs_tab` | `top_level_settings_afterlabs_tab.xml` | No | — |
| 13 | `dashboard_style_top_level_afterlabs_grid` | `top_level_settings_afterlabs_grid.xml` | No | YR Custom Dashboard |
| 14 | `dashboard_style_top_level_yr_expressive` | `top_level_settings_yr_expressive.xml` | No | YR Custom Dashboard |
| 15 | `dashboard_style_top_level_bmobile_neo` | `top_level_settings_bmobile_neo.xml` | Yes | Custom Dashboard |
| 16 | `dashboard_style_top_level_ks_fun` | `top_level_settings_ks_fun.xml` | No | KidsSafe Dashboard |

## Invalid / removed IDs

| ID | Status |
|----|--------|
| 3 | Removed (was Compact) |
| 9 | Removed (was Security Extended) |

`DashboardStyleHelper.isValidStyle()` accepts 0–8 and 10–16.

## Default style

- Code default: **7** (`DashboardStyleHelper.DEFAULT_STYLE`)
- Factory reset / picker reset: **7** (BMobile Home) for Custom and BMobile pickers
- YR picker reset default: **11**
- KidsSafe picker reset default: **16**

## Adding a new style (developer)

1. Create `res/xml/top_level_settings_<name>.xml`
2. Add `dashboard_style_top_level_<name>` string in `system_basic_strings.xml`
3. Add entry + value to `settings_dashboard_style_entries` / `_values` in `arrays.xml`
4. Add `case` in `DashboardStyleHelper.getPreferenceScreenResIdForStyle()`
5. Extend `isValidStyle()` upper bound
6. Wire grid setup in `TopLevelSettings` if using RecyclerView grid
7. Add to appropriate brand array if needed
