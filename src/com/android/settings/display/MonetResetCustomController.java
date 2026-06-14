package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/**
 * Controller for resetting custom Monet colors back to wallpaper-based theming
 * Inspired by MonetCompat's reset to wallpaper colors feature
 */
public class MonetResetCustomController extends BasePreferenceController {

    public MonetResetCustomController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary("Return to dynamic wallpaper-based Material You theming");
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!preference.getKey().equals(getPreferenceKey())) {
            return super.handlePreferenceTreeClick(preference);
        }

        // Show confirmation dialog
        showResetConfirmationDialog();

        return true;
    }

    private void showResetConfirmationDialog() {
        try {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
            builder.setTitle("Reset Custom Colors");
            builder.setMessage("This will remove your custom color selection and return to wallpaper-based Material You theming. Continue?");
            builder.setPositiveButton("Reset", (dialog, which) -> {
                resetToWallpaperColors();
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();

        } catch (Exception e) {
            android.util.Log.e("MonetResetCustom", "Failed to show reset dialog", e);
        }
    }

    private void resetToWallpaperColors() {
        try {
            Settings.Secure.putInt(mContext.getContentResolver(), "monet_chroma_multiplier", 100);
            Settings.Secure.putString(mContext.getContentResolver(), "monet_wallpaper_source", "system");
            MonetThemeApplier.clearToWallpaper(mContext);

            android.widget.Toast.makeText(mContext,
                "Reset to wallpaper colors!", android.widget.Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            android.util.Log.e("MonetResetCustom", "Failed to reset to wallpaper colors", e);
        }
    }
}
