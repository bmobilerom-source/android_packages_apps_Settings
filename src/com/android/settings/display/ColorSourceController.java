package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

import org.json.JSONException;
import org.json.JSONObject;

public class ColorSourceController extends BasePreferenceController implements Preference.OnPreferenceChangeListener {

    private static final String COLOR_SOURCE_KEY = "color_source";

    public ColorSourceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public String getPreferenceKey() {
        return COLOR_SOURCE_KEY;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof ListPreference) {
            final ListPreference listPreference = (ListPreference) preference;
            String currentValue = getCurrentColorSource();
            listPreference.setValue(currentValue);
        }
    }

    private String getCurrentColorSource() {
        String overlayPackagesJson = Settings.Secure.getString(
                mContext.getContentResolver(),
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES);

        if (overlayPackagesJson != null && !overlayPackagesJson.isEmpty()) {
            try {
                JSONObject object = new JSONObject(overlayPackagesJson);
                return object.optString("android.theme.customization.color_source", "both");
            } catch (JSONException e) {
                // Ignore and return default
            }
        }
        return "both"; // Default
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String value = (String) newValue;

        String overlayPackagesJson = Settings.Secure.getString(
                mContext.getContentResolver(),
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES);

        JSONObject object;
        if (overlayPackagesJson != null && !overlayPackagesJson.isEmpty()) {
            try {
                object = new JSONObject(overlayPackagesJson);
            } catch (JSONException e) {
                object = new JSONObject();
            }
        } else {
            object = new JSONObject();
        }

        try {
            object.put("android.theme.customization.color_source", value);

            boolean success = Settings.Secure.putString(
                    mContext.getContentResolver(),
                    Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                    object.toString());

            if (success) {
                triggerThemeRefresh();
            }

            updateState(preference);
            return success;
        } catch (JSONException e) {
            return false;
        }
    }

    private void triggerThemeRefresh() {
        try {
            android.content.Intent wallpaperIntent = new android.content.Intent("android.intent.action.WALLPAPER_CHANGED");
            wallpaperIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(wallpaperIntent);

            android.content.Intent configIntent = new android.content.Intent("android.intent.action.CONFIGURATION_CHANGED");
            configIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(configIntent);
        } catch (Exception e) {
            // Best effort - ignore exceptions
        }
    }
}