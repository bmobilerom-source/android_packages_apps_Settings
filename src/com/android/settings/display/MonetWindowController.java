package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

public class MonetWindowController extends BasePreferenceController {
    public MonetWindowController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }
    @Override public int getAvailabilityStatus() { return AVAILABLE; }
}
