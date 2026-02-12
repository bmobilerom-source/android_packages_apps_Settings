package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.core.TogglePreferenceController;

import org.json.JSONException;
import org.json.JSONObject;

public class TintBackgroundController extends TogglePreferenceController {

    public TintBackgroundController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        // TintBackground is a SwitchPreference, state is handled by isChecked()
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(
                mContext.getContentResolver(),
                "monet_tint_background",
                0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        boolean success = Settings.System.putInt(
                mContext.getContentResolver(),
                "monet_tint_background",
                isChecked ? 1 : 0);

        if (success) {
            triggerThemeRefresh();
        }

        return success;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
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