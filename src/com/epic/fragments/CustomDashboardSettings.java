/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.epic.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.homepage.CustomDashboardBottomBarHelper;
import com.android.settings.homepage.CustomDashboardLauncher;
import com.android.settings.homepage.CustomizeLayoutModeHelper;
import com.android.settings.homepage.DashboardStyleHelper;
import com.android.settings.homepage.DashboardSystemKeys;
import com.android.settings.homepage.SystemUiRestarter;
import com.android.settingslib.widget.LayoutPreference;

import android.widget.TextView;

/**
 * Settings page to pick and manage custom Settings homepage dashboard layouts.
 */
public class CustomDashboardSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    /** Launch from any {@link android.content.Context} (fragment, activity, adapter). */
    public static void launchFrom(Context context) {
        CustomDashboardLauncher.launch(context);
    }

    private static final String KEY_DASHBOARD_STYLE = DashboardSystemKeys.DASHBOARD_STYLE;
    private static final String KEY_HEADER = "custom_dashboard_header";
    private static final String KEY_LAYOUT_MODE = CustomizeLayoutModeHelper.SECURE_KEY;
    private static final String KEY_DASHBOARD_STYLE_RESET = "dashboard_style_reset";
    private static final String KEY_SYSTEMUI_RESET = "systemui_reset";
    private static final String KEY_CURRENT_STYLE = "custom_dashboard_current_style";
    private static final String FRAGMENT_DISPLAY = "com.android.settings.DisplaySettings";
    private static final String TAG = "CustomDashboardSettings";
    private static final int DEFAULT_DASHBOARD_STYLE = DashboardStyleHelper.getDefaultStyle();
    private static final int BOTTOM_NAV_PADDING_DP = 88;

    private ListPreference mDashboardStyle;
    private ListPreference mLayoutMode;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(TAG, "onCreate");
        try {
            addPreferencesFromResource(R.xml.custom_dashboard_settings);
        } catch (Exception e) {
            Log.e(TAG, "Error loading custom dashboard XML", e);
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(),
                        getString(R.string.custom_dashboard_load_error, e.getMessage()),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        }

        final Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null in onCreate");
            return;
        }

        final ContentResolver resolver = context.getContentResolver();
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            Log.e(TAG, "PreferenceScreen is null after loading XML");
            try {
                setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getActivity()));
            } catch (Exception e2) {
                Log.e(TAG, "Failed to create fallback preference screen", e2);
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
                    resolver, DEFAULT_DASHBOARD_STYLE);
            if (!DashboardStyleHelper.isValidStyle(dashboardStyle)) {
                dashboardStyle = DEFAULT_DASHBOARD_STYLE;
                DashboardSystemKeys.putDashboardStyle(resolver, dashboardStyle);
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

        final View bottomBar = CustomDashboardBottomBarHelper.inflate(inflater, wrapper);
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
                            reloadSystemUI();
                        }

                        @Override
                        public void onDisplaySelected() {
                            launchDisplaySettings();
                        }
                    });
        } else {
            Log.w(TAG, "Bottom bar unavailable; preferences still shown");
        }

        return wrapper;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final int pad = (int) (BOTTOM_NAV_PADDING_DP * getResources().getDisplayMetrics().density);
        final RecyclerView list = getListView();
        if (list != null) {
            list.setClipToPadding(false);
            list.setPadding(list.getPaddingLeft(), list.getPaddingTop(),
                    list.getPaddingRight(), pad);
        }
    }

    private void showToast(int resId) {
        Context context = getContext();
        if (context != null) {
            android.widget.Toast.makeText(context, resId, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(int resId, Object... formatArgs) {
        Context context = getContext();
        if (context != null) {
            android.widget.Toast.makeText(context, getString(resId, formatArgs),
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void launchDisplaySettings() {
        final Context context = getContext();
        if (context == null) {
            return;
        }
        new SubSettingLauncher(context)
                .setDestination(FRAGMENT_DISPLAY)
                .setTitleRes(R.string.display_settings)
                .setSourceMetricsCategory(getMetricsCategory())
                .launch();
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
            TextView title = (TextView) root;
            title.setText(R.string.custom_dashboard_title);
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
            DashboardSystemKeys.putDashboardStyle(resolver, suggestedStyle);
            if (mDashboardStyle != null) {
                mDashboardStyle.setValue(String.valueOf(suggestedStyle));
                updateSummary(suggestedStyle);
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
            if (!DashboardStyleHelper.isValidStyle(dashboardStyle)) {
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

    /** Keep Secure declanxafterlab_style aligned when user picks styles 12 or 13 directly. */
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

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (KEY_DASHBOARD_STYLE_RESET.equals(key)) {
            resetDashboardStyle();
            return true;
        }
        if (KEY_SYSTEMUI_RESET.equals(key)) {
            reloadSystemUI();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
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
        DashboardSystemKeys.putDashboardStyle(resolver, DEFAULT_DASHBOARD_STYLE);
        DashboardSystemKeys.putCompactDashboardEnabled(resolver, false);
        CustomizeLayoutModeHelper.setLayoutMode(context, CustomizeLayoutModeHelper.MODE_GRID);
        if (mDashboardStyle != null) {
            mDashboardStyle.setValue(String.valueOf(DEFAULT_DASHBOARD_STYLE));
            updateSummary(DEFAULT_DASHBOARD_STYLE);
        }
        if (mLayoutMode != null) {
            mLayoutMode.setValue(String.valueOf(CustomizeLayoutModeHelper.MODE_GRID));
            mLayoutMode.setSummary(CustomizeLayoutModeHelper.getModeLabel(context,
                    CustomizeLayoutModeHelper.MODE_GRID));
        }
        notifyDashboardStyleChanged();
        refreshCurrentStyleSummary();
        showToast(R.string.dashboard_style_applied,
                getStyleLabel(DEFAULT_DASHBOARD_STYLE));
        CustomDashboardLauncher.reopenSettingsHome(getActivity());
    }

    private void reloadSystemUI() {
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

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DASHBOARD_SUMMARY;
    }
}
