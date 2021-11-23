package xyz.xd.ui.frameworks.fragments;

import android.app.settings.SettingsEnums;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class XDAboutFragment extends DashboardFragment {

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.xd_about;
    }

    @Override
    protected String getLogTag() {
        return "xdAboutSettings";
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DIALOG_FIRMWARE_VERSION;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.xd_about);
}