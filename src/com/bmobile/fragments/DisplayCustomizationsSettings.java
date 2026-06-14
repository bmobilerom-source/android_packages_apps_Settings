package com.bmobile.fragments;

import android.content.Context;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.bmobile.customization.DeviceControlsPreferenceController;
import com.bmobile.customization.LockscreenClockStyleController;
import com.bmobile.customization.LockscreenClockTaglineController;
import com.bmobile.customization.LockscreenWidgetsPreferenceController;
import com.bmobile.customization.QsHeaderClockStyleController;
import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class DisplayCustomizationsSettings extends DashboardFragment {

    private static final String TAG = "DisplayCustomizationsSettings";

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.display_customizations;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new QsHeaderClockStyleController(context, "qs_header_clock_style"));
        controllers.add(new LockscreenClockStyleController(context, "lockscreen_clock_style"));
        controllers.add(new LockscreenClockTaglineController(context, "lockscreen_clock_tagline"));
        controllers.add(new LockscreenWidgetsPreferenceController(context, "lockscreen_widgets_enabled"));
        DeviceControlsPreferenceController deviceControlsController =
                new DeviceControlsPreferenceController(context, "lockscreen_device_controls");
        getSettingsLifecycle().addObserver(deviceControlsController);
        controllers.add(deviceControlsController);
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.display_customizations);
}
