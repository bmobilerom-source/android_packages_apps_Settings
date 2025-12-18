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
            boolean isEnabled = Settings.System.getInt(mContext.getContentResolver(),
                    "monet_apply_to_menus", 1) == 1; // Default enabled
            switchPreference.setChecked(isEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isEnabled = (Boolean) newValue;
        return Settings.System.putInt(mContext.getContentResolver(),
                "monet_apply_to_menus", isEnabled ? 1 : 0);
    }
}
