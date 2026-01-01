/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bmobile.fragments;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.settings.R;
import com.android.settings.homepage.CustomDashboardLauncher;
import com.android.settings.homepage.DashboardStyleHelper;

/** Full dashboard style picker (all layouts). */
public class CustomDashboardSettings extends BrandDashboardSettings {

    public static void launchFrom(Context context) {
        CustomDashboardLauncher.launch(context);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.custom_dashboard_settings;
    }

    @Override
    protected int getPageTitleResId() {
        return R.string.custom_dashboard_title;
    }

    @Override
    protected int getLoadErrorResId() {
        return R.string.custom_dashboard_load_error;
    }

    @Override
    protected int getDefaultBrandStyle() {
        return DashboardStyleHelper.getDefaultStyle();
    }

    @Override
    protected String getLogTag() {
        return "CustomDashboardSettings";
    }
}
