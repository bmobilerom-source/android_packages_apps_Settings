package com.epic.fragments;

import android.content.Context;
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;
import android.util.Log;

public class RestartSystemUIController extends BasePreferenceController {

    private static final String TAG = "RestartSystemUIController";

    public RestartSystemUIController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            restartSystemUI();
            return true;
        }
        return super.handlePreferenceTreeClick(preference);
    }

    private void restartSystemUI() {
        try {
            // Execute pkill to restart SystemUI
            Runtime.getRuntime().exec(new String[]{"/system/bin/sh", "-c", "pkill -f com.android.systemui"});
            Log.d(TAG, "Attempted to restart SystemUI via pkill");
        } catch (Exception e) {
            Log.e(TAG, "Failed to restart SystemUI", e);
        }
    }
}

