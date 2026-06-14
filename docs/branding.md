# Customer branding notes

This document lists strings and assets customers can customize per ROM build. Override values in a product-specific `res/values/*` overlay when possible.

## Parent profile card

| Item | Resource | Default | Location |
|------|----------|---------|----------|
| ROM name label | `branding_string` | Your ROM | `res/layout/mainuser_profile_card.xml` (right tile title) |
| Profile Lottie | `@raw/profile_persons` | bundled animation | `res/layout/mainuser_profile_card.xml` (`profile_background_lottie`) |

## Display page grid title

| Resource | Default | Notes |
|----------|---------|-------|
| `display_page_grid_title` | Display | Used on all top-level dashboards that link to a display page grid |

## YR user info (Study / Social / Expressive profile screen)

| Resource | Default | Notes |
|----------|---------|-------|
| `branding_yr_user_name` | friend | Inserted into intro text |
| `yr_userinfo_intro` | Hi %1$s, this is your space… | Top intro on `YrUserInfoFragment` |
| `yr_your_chat_title` | Your chat | Opens ArcaneChat (`chat.delta.lite`) when installed |
| Daily You | `kidssafe_daily_you_*` on YR user info | Opens `com.demizo.daily_you` when installed |
| Local display name | `yr_user_profile` SharedPreferences | Tap the name on the profile card to set; falls back to `branding_yr_user_name` in intro |

Display name is stored locally on the YR user info page (`yr_user_profile` SharedPreferences) and does not use the Google account.

### Accounts preference (removed)

The **Accounts** tile was replaced with **Daily You** on `yr_userinfo_pref.xml`. Restore from git history if a customer needs accounts back.

## Privacy Controls intro

| Resource | Default |
|----------|---------|
| `privacy_controls_intro_summary` | Explains this page is where users can try privacy on the device |

Shown at the top of `privacy_controls_settings.xml` on all non–MainUser dashboards (MainUser style 0 keeps the full Privacy dashboard).

## KidsSafe user info (KS Fun)

| Resource | Default | Notes |
|----------|---------|-------|
| `kidssafe_owner_name_hint` | Tap to add your name | Shown until the child sets a local display name |
| Daily You / Your chat | `kidssafe_daily_you_*`, `kidssafe_your_chat_*` | Opens `com.demizo.daily_you` and `chat.delta.lite` (ArcaneChat) when installed |

Display name is stored locally on the KidsSafe user info page only (`kidssafe_user_profile` SharedPreferences) and does not use the Google account.
