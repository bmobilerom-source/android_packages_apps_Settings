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
            // Remove custom seed color setting
            Settings.Secure.putString(mContext.getContentResolver(), "monet_custom_seed_color", null);

            // Reset chroma multiplier to default (325% / 3.25x)
            Settings.Secure.putInt(mContext.getContentResolver(), "monet_chroma_multiplier", 325);

            // Reset wallpaper source to default
            Settings.Secure.putString(mContext.getContentResolver(), "monet_wallpaper_source", "system");

            // Send theme change broadcast to reset to wallpaper colors
            android.content.Intent themeIntent = new android.content.Intent("android.intent.action.THEME_CHANGED");
            themeIntent.putExtra("monet_reset_to_wallpaper", true);
            themeIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(themeIntent);

            // Send wallpaper changed broadcast to trigger color re-extraction
            android.content.Intent wallpaperIntent = new android.content.Intent("android.intent.action.WALLPAPER_CHANGED");
            wallpaperIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(wallpaperIntent);

            // Force configuration change
            android.content.Intent configIntent = new android.content.Intent("android.intent.action.CONFIGURATION_CHANGED");
            configIntent.addFlags(android.content.Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            mContext.sendBroadcast(configIntent);

            android.widget.Toast.makeText(mContext,
                "Reset to wallpaper colors!", android.widget.Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            android.util.Log.e("MonetResetCustom", "Failed to reset to wallpaper colors", e);
        }
    }
}
