package com.epic.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.TopIntroPreference;
import com.epic.controllers.PowerOffVerifyController;

import java.util.ArrayList;
import java.util.List;

public class PowerOffVerifyFragment extends DashboardFragment {

    private static final String TAG = "PowerOffVerifyFragment";

    @Override
    public int getMetricsCategory() {
        return 0; // Replace with appropriate metrics category
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.power_off_verify;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        // Add intro text if available
        TopIntroPreference intro = findPreference("power_off_verify_intro");
        if (intro != null) {
            intro.setSummary(R.string.power_off_verify_summary);
        }
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new PowerOffVerifyController(context, "power_off_verify_enabled"));
        return controllers;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}