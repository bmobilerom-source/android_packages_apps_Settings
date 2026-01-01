/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bmobile.fragments;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.settings.R;

/** YR-branded dashboard picker (YR Study, YR School, YR Social, YR Expressive). */
public class YrCustomDashboardSettings extends BrandDashboardSettings {

    private static final int DEFAULT_YR_STYLE = 11;

    public static void launchFrom(@NonNull Context context) {
        launchFragment(context, YrCustomDashboardSettings.class.getName(),
                R.string.yr_custom_dashboard_title);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.yr_custom_dashboard_settings;
    }

    @Override
    protected int getPageTitleResId() {
        return R.string.yr_custom_dashboard_title;
    }

    @Override
    protected int getLoadErrorResId() {
        return R.string.yr_custom_dashboard_load_error;
    }

    @Override
    protected int getDefaultBrandStyle() {
        return DEFAULT_YR_STYLE;
    }

    @Override
    protected String getLogTag() {
        return "YrCustomDashboardSettings";
    }
}
