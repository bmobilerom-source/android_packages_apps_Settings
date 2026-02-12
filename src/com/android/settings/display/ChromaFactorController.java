package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.core.SliderPreferenceController;

public class ChromaFactorController extends SliderPreferenceController {

    private static final String CHROMA_FACTOR_KEY = "chroma_factor";
    private static final int DEFAULT_VALUE = 95; // Center of 0-195 range (represents 0%)
    private static final int MIN_VALUE = 0;      // Represents -95%
    private static final int MAX_VALUE = 195;    // Represents +100%

    public ChromaFactorController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isSliceable() {
        return false;
    }

    @Override
    public boolean isPublicSlice() {
        return false;
    }

    @Override
    public boolean useDynamicSliceSummary() {
        return false;
    }

    @Override
    public int getSliderPosition() {
        int storedValue = Settings.System.getInt(
                mContext.getContentResolver(),
                "monet_chroma_factor",
                0); // 0 = no adjustment
        // Convert from -95..+100 range to 0..195 range
        return storedValue + 95;
    }

    @Override
    public boolean setSliderPosition(int position) {
        // Convert from 0..195 range to -95..+100 range
        int value = position - 95;
        boolean success = Settings.System.putInt(
                mContext.getContentResolver(),
                "monet_chroma_factor",
                value);

        if (success) {
            triggerThemeRefresh();
        }

        return success;
    }

    @Override
    public int getMax() {
        return MAX_VALUE - MIN_VALUE;
    }

    @Override
    public int getMin() {
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