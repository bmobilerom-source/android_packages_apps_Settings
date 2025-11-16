# Customer branding guide

How to white-label the Settings dashboard experience for a specific customer (school, enterprise, MVNO, etc.).

## Tier 1 — Strings only (no rebuild of logic)

Edit or overlay these for **maximum customer impact with minimum risk**:

### A. Customer OS banner (BMobile Cards, style 6)

The clickable header on **BMobile Cards** homepage:

- **Strings:** `customeros_header_title`, `customeros_header_summary`
- **Layout:** `res/layout/CustomerOS_header.xml`
- **Wired in:** `top_level_settings_custom_v2.xml` → key `Branding_CustomerOS_view`

This is the closest equivalent to a **“Customer OS card”** — rename to the customer product name.

### B. Style names in pickers

Rename `dashboard_style_top_level_*` so pickers show customer product names, e.g.:

- `KS School` → `Riverdale Standard`
- `KS Fun` → `Riverdale Kids`
- `BMobile Home` → `Acme Home`

### C. Picker page titles

- `kidssafe_dashboard_title` / `_summary` for school customers
- `bmobile_dashboard_title` / `_summary` for consumer/MVNO
- `yr_custom_dashboard_title` / `_summary` for youth/education YR line

### D. User profile copy

- `userinfo_bubble_setting_description` — main user profile intro
- `kidssafe_userinfo_summary` — KidsSafe profile intro
- `usercard_summary` — profile card hint on homepages

---

## Tier 2 — Visual assets

| Asset | Path | Used for |
|-------|------|----------|
| Custom dashboard icon | `res/drawable/ic_custom_dashboard` | Picker tiles, bottom bar |
| User avatar default | `res/drawable/user_png` | Profile cards, KidsSafe header |
| User card accent bar | `res/drawable/bar` | Profile layout divider |
| User card background color | `res/values/colors.xml` → `usercardbg` | Profile card fill |
| Card backgrounds | `res/drawable/octavi_card`, `custom_preference_background` | Homepage cards |
| Pastel grid gradients | `res/drawable/pastel_gradient_*` | Expressive / KS Fun grid cards |

Replace drawables in a **customer overlay** to match brand colors without touching Java.

---

## Tier 3 — Per-customer build variants

Recommended ROM structure:

```
vendor/<customer>/overlay/packages/apps/Settings/
  res/values/customer_strings.xml    ← strings from Tier 1
  res/drawable/customer_logo.png     ← optional
  res/drawable/user_png.png          ← optional branded avatar placeholder
```

Set **default style** for a customer at first boot via:

- Framework default for `settings_dashboard_style`, or
- Device-specific `SettingsProvider` default, or
- Post-boot script: `adb shell settings put system settings_dashboard_style 16`

---

## Example: school customer “Riverdale”

| Setting | Value |
|---------|-------|
| `customeros_header_title` | Riverdale OS |
| `customeros_header_summary` | Managed school device |
| `dashboard_style_top_level_classic` | Riverdale Standard |
| `dashboard_style_top_level_ks_fun` | Riverdale Fun |
| `kidssafe_dashboard_title` | Riverdale Layouts |
| Default style ID | 16 (KS Fun) or 5 (KS School) |

Ship with **KidsSafe Dashboard** tile on homepage; hide BMobile/YR pickers by not exposing those homepages.

---

## Example: MVNO consumer “Acme Mobile”

| Setting | Value |
|---------|-------|
| `bmobile_dashboard_title` | Acme Layouts |
| `dashboard_style_top_level_fun_display` | Acme Home |
| `dashboard_style_top_level_custom_v2` | Acme Cards |
| `customeros_header_title` | Acme Mobile |
| Default style ID | 7 (BMobile Home) |

---

## What customers cannot change without code

- Which tiles appear on each homepage (controlled by `top_level_settings_*.xml`)
- Grid card destinations (Java in `TopLevelSettings`, `KsFunDashboardHelper`, etc.)
- Which styles appear in each brand picker (`arrays.xml` entries)
- Bottom bar presence per style (`TopLevelSettings.shouldShowTopLevelBottomBar`)

For those, file a build-time change or customer-specific XML fork.
