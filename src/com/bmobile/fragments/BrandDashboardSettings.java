/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bmobile.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.XmlRes;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.homepage.CustomDashboardBottomBarHelper;
import com.android.settings.homepage.CustomDashboardLauncher;
import com.android.settings.homepage.CustomizeLayoutModeHelper;
import com.android.settings.homepage.DashboardStyleHelper;
import com.android.settings.homepage.DashboardSystemKeys;
import com.android.settings.homepage.SystemUiRestarter;
import com.android.settingslib.widget.LayoutPreference;

/**
 * Base settings page to pick homepage dashboard layouts. Subclasses provide brand-specific
 * titles and filtered style lists (YR, KidsSafe, BMobile, or full list).
 */
public abstract class BrandDashboardSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_DASHBOARD_STYLE = DashboardSystemKeys.DASHBOARD_STYLE;
    private static final String KEY_HEADER = "custom_dashboard_header";
    private static final String KEY_LAYOUT_MODE = CustomizeLayoutModeHelper.SECURE_KEY;
    private static final String KEY_DASHBOARD_STYLE_RESET = "dashboard_style_reset";
    private static final String KEY_SYSTEMUI_RESET = "systemui_reset";
    private static final String KEY_CURRENT_STYLE = "custom_dashboard_current_style";
    private static final String FRAGMENT_DISPLAY_PAGE_GRID =
            "com.bmobile.fragments.DisplayPageGrid";
    private static final int BOTTOM_NAV_PADDING_DP = 88;

    private ListPreference mDashboardStyle;
    private ListPreference mLayoutMode;

    @XmlRes
    protected abstract int getPreferenceScreenResId();

    @StringRes
    protected abstract int getPageTitleResId();

    @StringRes
    protected abstract int getLoadErrorResId();

    /** Style id used when reset is tapped or current style is outside this picker's list. */
    protected abstract int getDefaultBrandStyle();

    protected abstract String getLogTag();

    /** Whether this picker shows the reset / SystemUI / Display bottom bar. */
    protected boolean shouldShowBottomBar() {
        return true;
    }

    protected static void launchFragment(@NonNull Context context, @NonNull String fragmentClass,
            @StringRes int titleResId) {
        new SubSettingLauncher(context)
                .setDestination(fragmentClass)
                .setTitleRes(titleResId)
                .setSourceMetricsCategory(MetricsProto.MetricsEvent.DASHBOARD_SUMMARY)
                .launch();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(getLogTag(), "onCreate");
        try {
            addPreferencesFromResource(getPreferenceScreenResId());
        } catch (Exception e) {
            Log.e(getLogTag(), "Error loading dashboard picker XML", e);
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(),
                        getString(getLoadErrorResId(), e.getMessage()),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        }

        final Context context = getContext();
        if (context == null) {
            Log.e(getLogTag(), "Context is null in onCreate");
            return;
        }

        final ContentResolver resolver = context.getContentResolver();
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            Log.e(getLogTag(), "PreferenceScreen is null after loading XML");
            try {
                setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getActivity()));
            } catch (Exception e2) {
                Log.e(getLogTag(), "Failed to create fallback preference screen", e2);
                return;
            }
        }

        mLayoutMode = findPreference(KEY_LAYOUT_MODE);
        if (mLayoutMode != null) {
            int mode = CustomizeLayoutModeHelper.getLayoutMode(getContext());
            mLayoutMode.setValue(String.valueOf(mode));
            mLayoutMode.setSummary(CustomizeLayoutModeHelper.getModeLabel(getContext(), mode));
            mLayoutMode.setOnPreferenceChangeListener(this);
        }

        mDashboardStyle = findPreference(KEY_DASHBOARD_STYLE);
        if (mDashboardStyle != null) {
            int dashboardStyle = DashboardSystemKeys.getDashboardStyle(
                    resolver, getDefaultBrandStyle());
            if (!DashboardStyleHelper.isValidStyle(dashboardStyle)
                    || !isStyleInPicker(dashboardStyle)) {
                dashboardStyle = getDefaultBrandStyle();
            }
            mDashboardStyle.setValue(String.valueOf(dashboardStyle));
            updateSummary(dashboardStyle);
            mDashboardStyle.setOnPreferenceChangeListener(this);
        }

        refreshCurrentStyleSummary();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final FrameLayout wrapper = new FrameLayout(requireContext());
        wrapper.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final View content = super.onCreateView(inflater, wrapper, savedInstanceState);
        if (content != null) {
            wrapper.addView(content, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }

        final View bottomBar = shouldShowBottomBar()
                ? CustomDashboardBottomBarHelper.inflate(inflater, wrapper) : null;
        if (bottomBar != null) {
            final FrameLayout.LayoutParams bottomLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);
            wrapper.addView(bottomBar, bottomLp);

            CustomDashboardBottomBarHelper.bind(bottomBar, requireContext(),
                    new CustomDashboardBottomBarHelper.Listener() {
                        @Override
                        public void onResetDashboardStyle() {
                            resetDashboardStyle();
                        }

                        @Override
                        public void onResetSystemUi() {
                            reloadSystemUi();
                        }

                        @Override
                        public void onDisplaySelected() {
                            launchDisplaySettings();
                        }
                    });
        } else {
            Log.w(getLogTag(), "Bottom bar unavailable; preferences still shown");
        }

        return wrapper;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!shouldShowBottomBar()) {
            return;
        }
        final int pad = (int) (BOTTOM_NAV_PADDING_DP * getResources().getDisplayMetrics().density);
        final RecyclerView list = getListView();
        if (list != null) {
            list.setClipToPadding(false);
            list.setPadding(list.getPaddingLeft(), list.getPaddingTop(),
                    list.getPaddingRight(), pad);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCurrentStyleSummary();
        bindDashboardHeader();
        if (mLayoutMode != null) {
            int mode = CustomizeLayoutModeHelper.getLayoutMode(getContext());
            mLayoutMode.setValue(String.valueOf(mode));
            mLayoutMode.setSummary(CustomizeLayoutModeHelper.getModeLabel(getContext(), mode));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLayoutMode) {
            final int mode = Integer.parseInt((String) newValue);
            CustomizeLayoutModeHelper.setLayoutMode(getContext(), mode);
            mLayoutMode.setSummary(CustomizeLayoutModeHelper.getModeLabel(getContext(), mode));
            final int suggestedStyle =
                    CustomizeLayoutModeHelper.getSuggestedHomepageStyleForMode(mode);
            if (DashboardStyleHelper.isValidStyle(suggestedStyle)
                    && isStyleInPicker(suggestedStyle)) {
                DashboardSystemKeys.putDashboardStyle(resolver, suggestedStyle);
                if (mDashboardStyle != null) {
                    mDashboardStyle.setValue(String.valueOf(suggestedStyle));
                    updateSummary(suggestedStyle);
                }
            }
            refreshCurrentStyleSummary();
            notifyDashboardStyleChanged();
            android.widget.Toast.makeText(getContext(),
                    getString(R.string.customize_layout_mode_applied,
                            CustomizeLayoutModeHelper.getModeLabel(getContext(), mode)),
                    android.widget.Toast.LENGTH_SHORT).show();
            CustomDashboardLauncher.reopenSettingsHome(getActivity());
            return true;
        }
        if (preference == mDashboardStyle) {
            int dashboardStyle = Integer.parseInt((String) newValue);
            if (!DashboardStyleHelper.isValidStyle(dashboardStyle)
                    || !isStyleInPicker(dashboardStyle)) {
                return false;
            }
            if (!DashboardSystemKeys.putDashboardStyle(resolver, dashboardStyle)) {
                showToast(R.string.dashboard_style_summary);
                return false;
            }
            syncLayoutModeForAfterlabsStyles(dashboardStyle);
            updateSummary(dashboardStyle);
            refreshCurrentStyleSummary();
            notifyDashboardStyleChanged();
            android.widget.Toast.makeText(getContext(),
                    getString(R.string.dashboard_style_applied, getStyleLabel(dashboardStyle)),
                    android.widget.Toast.LENGTH_SHORT).show();
            CustomDashboardLauncher.reopenSettingsHome(getActivity());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (KEY_DASHBOARD_STYLE_RESET.equals(key)) {
            resetDashboardStyle();
            return true;
        }
        if (KEY_SYSTEMUI_RESET.equals(key)) {
            reloadSystemUi();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DASHBOARD_SUMMARY;
    }

    private boolean isStyleInPicker(int style) {
        if (mDashboardStyle == null || getContext() == null) {
            return false;
        }
        CharSequence[] values = mDashboardStyle.getEntryValues();
        if (values == null) {
            return false;
        }
        for (CharSequence value : values) {
            try {
                if (Integer.parseInt(value.toString()) == style) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    private void refreshCurrentStyleSummary() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        Preference current = findPreference(KEY_CURRENT_STYLE);
        if (current != null) {
            current.setSummary(CustomDashboardLauncher.getCurrentStyleLabel(context));
        }
    }

    private void bindDashboardHeader() {
        Preference headerPref = findPreference(KEY_HEADER);
        if (!(headerPref instanceof LayoutPreference)) {
            return;
        }
        View root = ((LayoutPreference) headerPref).findViewById(android.R.id.title);
        if (root instanceof TextView) {
            ((TextView) root).setText(getPageTitleResId());
        }
    }

    private void syncLayoutModeForAfterlabsStyles(int dashboardStyle) {
        if (dashboardStyle == 12) {
            CustomizeLayoutModeHelper.setLayoutMode(getContext(),
                    CustomizeLayoutModeHelper.MODE_TABBED);
        } else if (dashboardStyle == 13) {
            CustomizeLayoutModeHelper.setLayoutMode(getContext(),
                    CustomizeLayoutModeHelper.MODE_GRID);
        }
        if (mLayoutMode != null) {
            int mode = CustomizeLayoutModeHelper.getLayoutMode(getContext());
            mLayoutMode.setValue(String.valueOf(mode));
            mLayoutMode.setSummary(CustomizeLayoutModeHelper.getModeLabel(getContext(), mode));
        }
    }

    private void notifyDashboardStyleChanged() {
        if (getActivity() != null) {
            getActivity().sendBroadcast(
                    new android.content.Intent(CustomDashboardLauncher.ACTION_DASHBOARD_STYLE_CHANGED));
        }
    }

    private void resetDashboardStyle() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            return;
        }
        final int defaultStyle = getDefaultBrandStyle();
        DashboardSystemKeys.putDashboardStyle(resolver, defaultStyle);
        DashboardSystemKeys.putCompactDashboardEnabled(resolver, false);
        CustomizeLayoutModeHelper.setLayoutMode(context, CustomizeLayoutModeHelper.MODE_GRID);
        if (mDashboardStyle != null) {
            mDashboardStyle.setValue(String.valueOf(defaultStyle));
            updateSummary(defaultStyle);
        }
        if (mLayoutMode != null) {
            mLayoutMode.setValue(String.valueOf(CustomizeLayoutModeHelper.MODE_GRID));
            mLayoutMode.setSummary(CustomizeLayoutModeHelper.getModeLabel(context,
                    CustomizeLayoutModeHelper.MODE_GRID));
        }
        notifyDashboardStyleChanged();
        refreshCurrentStyleSummary();
        showToast(R.string.dashboard_style_applied, getStyleLabel(defaultStyle));
        CustomDashboardLauncher.reopenSettingsHome(getActivity());
    }

    private void reloadSystemUi() {
        Context context = getContext();
        if (context == null) {
            context = getActivity();
        }
        if (context == null) {
            return;
        }
        final boolean success = SystemUiRestarter.restart(context);
        showToast(success ? R.string.systemui_reset_success : R.string.systemui_reset_failed);
    }

    private void launchDisplaySettings() {
        final Context context = getContext();
        if (context == null) {
            return;
        }
        new SubSettingLauncher(context)
                .setDestination(FRAGMENT_DISPLAY_PAGE_GRID)
                .setTitleRes(R.string.display_page_grid_title)
                .setSourceMetricsCategory(getMetricsCategory())
                .launch();
    }

    private void updateSummary(int dashboardStyle) {
        if (mDashboardStyle == null) {
            return;
        }
        Context context = getContext();
        if (context != null) {
            mDashboardStyle.setSummary(DashboardStyleHelper.getStyleLabel(context, dashboardStyle));
        }
    }

    private String getStyleLabel(int style) {
        Context context = getContext();
        if (context == null) {
            return getString(R.string.dashboard_style_summary);
        }
        return DashboardStyleHelper.getStyleLabel(context, style);
    }

    private void showToast(int resId) {
        Context context = getContext();
        if (context != null) {
            android.widget.Toast.makeText(context, resId, android.widget.Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void showToast(int resId, Object... formatArgs) {
        Context context = getContext();
        if (context != null) {
            android.widget.Toast.makeText(context, getString(resId, formatArgs),
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
