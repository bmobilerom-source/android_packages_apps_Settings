package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for applying Monet colors to menus
 * Inspired by MonetCompat's applyBackgroundColorToMenu feature
 */
public class MonetMenuController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public MonetMenuController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() { return AVAILABLE; }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SwitchPreference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            boolean isEnabled = Settings.Secure.getInt(mContext.getContentResolver(),
                    "monet_apply_to_menus", 1) == 1; // Default enabled
            switchPreference.setChecked(isEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isEnabled = (Boolean) newValue;

        boolean settingSaved = Settings.Secure.putInt(mContext.getContentResolver(),
                "monet_apply_to_menus", isEnabled ? 1 : 0);

        if (settingSaved) {
            // Apply menu theming change
            applyMenuThemingChange(isEnabled);
        }

        return settingSaved;
    }

    private void applyMenuThemingChange(boolean enabled) {
        try {
            android.content.Intent themeIntent = new android.content.Intent("android.intent.action.THEME_CHANGED");
            themeIntent.putExtra("monet_apply_to_menus", enabled);
            themeIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(themeIntent);

            // Force configuration change to refresh UI
            android.content.Intent configIntent = new android.content.Intent("android.intent.action.CONFIGURATION_CHANGED");
            configIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(configIntent);

        } catch (Exception e) {
            android.util.Log.e("MonetMenuController", "Failed to apply menu theming change", e);
        }
    }
}
