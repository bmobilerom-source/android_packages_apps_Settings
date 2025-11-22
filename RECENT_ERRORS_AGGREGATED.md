# Recent Build & Runtime Errors and Fixes

This document aggregates fixes for various errors encountered during recent development sessions.

## 1. AppsPreferenceController NullPointerException (Apps.xml crash)

### Problem
A crash occurred in `AppsPreferenceController.initPreferences()` due to a `NullPointerException` when attempting to call `setVisible()` on null preferences.

### Cause
Preferences looked up by key (e.g., `KEY_GAME_SETTINGS_PREF`) were returning null because they were missing from the XML or removed dynamically, but the code did not check for null before usage.

### Solution
Added null checks before all `setVisible()` calls in `AppsPreferenceController.java`.

```java
if (mGameSettingsPref != null) {
    mGameSettingsPref.setVisible(false);
}
```

## 2. LiveDisplay Resources$NotFoundException

### Problem
Crash when launching LiveDisplay from the Display Page Grid.

### Cause
The adapter was using a resource ID (`R.string.color_mode_title`) from the `Settings` package context, but trying to resolve it or use it in a way that failed when launching the `LineageParts` activity, which has its own resources.

### Solution
Changed the launch logic in `DisplayPageGridAdapter.java` to use the string title directly or ensure correct resource resolution.

## 3. Smart Pixels UnknownFormatConversionException

### Problem
Crash when opening Smart Pixels settings.

### Cause
The summary string contained a `%` symbol (e.g., "Turns off % of pixels") which `String.format` interpreted as a format specifier, causing an exception.

### Solution
Escaped the `%` symbol as `%%` in the Java code or XML summary to prevent invalid formatting attempts.

## 4. HideDeveloperStatusSettings ClassCastException

### Problem
Crash in `HideDeveloperStatusSettings`.

### Cause
Casting a `MenuItem.getActionView()` result directly to `SearchView` without checking if it was null or the correct type.

### Solution
Added `instanceof` checks and null checks before casting.

## 5. Status Bar Logo Missing Controllers

### Problem
Crash when opening Status Bar settings.

### Cause
XML referenced `StatusBarLogoController`, `StatusBarLogoPositionController`, and `StatusBarLogoStyleController`, which did not exist.

### Solution
Created the missing controller classes implementing `TogglePreferenceController` and `BasePreferenceController`.

## 6. StatusBarLogoController Abstract Method Error

### Problem
Build failure: `StatusBarLogoController is not abstract and does not override abstract method getSliceHighlightMenuRes()`.

### Cause
`TogglePreferenceController` requires implementing `getSliceHighlightMenuRes()`.

### Solution
Implemented the method to return `0` (NO_RES).

## 7. Auto Reboot Compilation Error

### Problem
`incompatible types: CharSequence[] cannot be converted to String[]`.

### Cause
`ListPreference.getEntryValues()` returns `CharSequence[]`, but the code tried to assign it to a `String[]` variable.

### Solution
Changed the variable type to `CharSequence[]`.

## 8. Ambient Display Blank Screen

### Problem
Ambient display showed a blank screen when a custom image was selected.

### Cause
SystemUI (or the preview) likely lacked permission to read the image URI provided by the file picker.

### Solution
Added `takePersistableUriPermission` in `AmbientCustomizations.java` to ensure the system retains access to the selected image.

```java
getContext().getContentResolver().takePersistableUriPermission(imageUri, 
        Intent.FLAG_GRANT_READ_URI_PERMISSION);
```

## 9. Hide IME Space Logic

### Problem
Feature needed to be hidden when Taskbar is enabled to avoid conflicts.

### Solution
Added logic in `DisplayCustomizations2.java` to check `LineageSettings.System.ENABLE_TASKBAR` and remove the "Hide IME Space" preference if true. Wrapped in try-catch for robustness.

