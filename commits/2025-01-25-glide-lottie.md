# Commit Message title: Settings: Glide import, lottie deps, and source exposure

## ✅ What’s New
- Added `glide` and `lottie` to Settings’ library dependencies and imported Glide AAR 4.16.0 (with 4.15.1 staged) for image-heavy components.
- Exposed `Settings_srcs` and `Settings_manifest` filegroups so downstream variants (e.g., SettingsGoogle) can run `IndexableProcessor` across shared sources.
- Manifest now declares `tools:replace="android:appComponentFactory"` to keep `CoreComponentFactory` when merging overlays.

## ⚙️ Use Case
- Enables Glide-backed visuals (clockfaces, previews) and Lottie animations while ensuring derivative builds can reuse core sources and manifest safely during overlay merges.

## 🧾 Commit Details
| Description | Change-Id | Signed-off-by |
| --- | --- | --- |
| Add Glide library (base addition) | Icd8f4d35251e9a3fe69387de7bb00bd53326c2c5 | Pranav Vashi \<neobuddy89@gmail.com> |
| Update Glide to 4.15.1 | I07214474e189d9e44548081972f51157b232e005 | NurKeinNeid \<mralexman3000@gmail.com> |
| Update Glide to 4.16.0 (latest) | (same as base add, likely squashed) | Pranav Vashi \<neobuddy89@gmail.com> |
| General Glide update | I4811efa14ce97983258cd59bfd328181fe95baf5 | (not specified) |

## ✍️ Authors
- Pranav Vashi \<neobuddy89@gmail.com>
- NurKeinNeid \<mralexman3000@gmail.com>

