/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.epic.fragments;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.settings.R;
import com.android.settings.homepage.DashboardStyleHelper;

/** BMobile-branded dashboard picker (Kathaleya, Cards, Home, Expressive, Icons, DynamicTabs, Neo). */
public class BMobileDashboardSettings extends BrandDashboardSettings {

    public static void launchFrom(@NonNull Context context) {
        launchFragment(context, BMobileDashboardSettings.class.getName(),
                R.string.bmobile_dashboard_title);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.bmobile_dashboard_settings;
    }

    @Override
    protected int getPageTitleResId() {
        return R.string.bmobile_dashboard_title;
    }

    @Override
    protected int getLoadErrorResId() {
        return R.string.bmobile_dashboard_load_error;
    }

    @Override
    protected int getDefaultBrandStyle() {
        return DashboardStyleHelper.getDefaultStyle();
    }

    @Override
    protected String getLogTag() {
        return "BMobileDashboardSettings";
    }
}
