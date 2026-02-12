package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for applying Monet colors to window backgrounds
 * Inspired by MonetCompat's window background theming feature
 */
public class MonetWindowController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public MonetWindowController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(androidx.preference.PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(getPreferenceKey());
        if (preference != null) {
            preference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SwitchPreference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            boolean isEnabled = Settings.Secure.getInt(mContext.getContentResolver(),
                    "monet_apply_to_window", 0) == 1; // Default disabled
            switchPreference.setChecked(isEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isEnabled = (Boolean) newValue;

        boolean settingSaved = Settings.Secure.putInt(mContext.getContentResolver(),
                "monet_apply_to_window", isEnabled ? 1 : 0);

        if (settingSaved) {
            // Notify content resolver of the change
            mContext.getContentResolver().notifyChange(
                    Settings.Secure.getUriFor("monet_apply_to_window"), null);

            // Apply window background theming change
            applyWindowThemingChange(isEnabled);
        }

        return settingSaved;
    }

    private void applyWindowThemingChange(boolean enabled) {
        try {
            android.content.Intent themeIntent = new android.content.Intent("android.intent.action.THEME_CHANGED");
            themeIntent.putExtra("monet_apply_to_window", enabled);
            themeIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(themeIntent);

            // Force configuration change to refresh UI
            android.content.Intent configIntent = new android.content.Intent("android.intent.action.CONFIGURATION_CHANGED");
            configIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(configIntent);

        } catch (Exception e) {
            android.util.Log.e("MonetWindowController", "Failed to apply window theming change", e);
        }
    }
}
