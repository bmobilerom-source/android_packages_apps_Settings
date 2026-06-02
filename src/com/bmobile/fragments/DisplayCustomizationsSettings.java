package com.bmobile.fragments;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.bmobile.customization.BatteryStylePreferenceController;
import com.bmobile.customization.LockscreenClockPositionController;
import com.bmobile.customization.LockscreenClockStyleController;
import com.bmobile.customization.LockscreenWidgetExtrasController;
import com.bmobile.customization.LockscreenWidgetsPreferenceController;
import com.bmobile.customization.QsHeaderClockStyleController;

import android.content.Context;

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
        controllers.add(new LockscreenClockPositionController(context, "lockscreen_clock_position"));
        controllers.add(new LockscreenWidgetsPreferenceController(context, "lockscreen_widgets_enabled"));
        controllers.add(new LockscreenWidgetExtrasController(context, "lockscreen_widgets_extras"));
        controllers.add(new BatteryStylePreferenceController(context,
                "status_bar_battery_style_customization"));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.display_customizations);
}
