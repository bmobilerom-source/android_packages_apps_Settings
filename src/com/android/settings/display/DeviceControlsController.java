package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for Device Controls in Power Menu
 * Inspired by ClassicPowerMenu's device controls integration
 */
public class DeviceControlsController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public DeviceControlsController(Context context, String preferenceKey) {
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
                    "controls_in_power_menu", 0) == 1;
            switchPreference.setChecked(isEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isEnabled = (Boolean) newValue;
        return Settings.System.putInt(mContext.getContentResolver(),
                "controls_in_power_menu", isEnabled ? 1 : 0);
    }
}
