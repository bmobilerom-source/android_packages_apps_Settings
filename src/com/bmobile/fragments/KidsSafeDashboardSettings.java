/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bmobile.fragments;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.settings.R;

/** KidsSafe-branded dashboard picker (KS School, KS Fun). */
public class KidsSafeDashboardSettings extends BrandDashboardSettings {

    private static final int DEFAULT_KIDSSAFE_STYLE = 16;

    public static void launchFrom(@NonNull Context context) {
        launchFragment(context, KidsSafeDashboardSettings.class.getName(),
                R.string.kidssafe_dashboard_title);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.kidssafe_dashboard_settings;
    }

    @Override
    protected int getPageTitleResId() {
        return R.string.kidssafe_dashboard_title;
    }

    @Override
    protected int getLoadErrorResId() {
        return R.string.kidssafe_dashboard_load_error;
    }

    @Override
    protected int getDefaultBrandStyle() {
        return DEFAULT_KIDSSAFE_STYLE;
    }

    @Override
    protected String getLogTag() {
        return "KidsSafeDashboardSettings";
    }

    @Override
    protected boolean shouldShowBottomBar() {
        return false;
    }
}
