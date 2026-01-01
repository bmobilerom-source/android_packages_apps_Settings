/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.preferences.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

/**
 * Self-contained widget container for Atomichub2 that wires click actions for all cards.
 */
public class Atomichub2View extends LinearLayout {

    public Atomichub2View(Context context) {
        super(context);
    }

    public Atomichub2View(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Atomichub2View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        bindActions();
    }

    private void bindActions() {
        Activity activity = findActivity(getContext());

        View card1 = findViewById(R.id.card1_2);
        View card2 = findViewById(R.id.card2_2);
        View card3 = findViewById(R.id.card3_2);
        View card4 = findViewById(R.id.card4_2);
        View card5 = findViewById(R.id.card5_2);
        View card6 = findViewById(R.id.card6_2);
        View card7 = findViewById(R.id.card7_2);
        View bigCard = findViewById(R.id.card2);

        setInteractiveClick(card1, () -> launchFragment(activity,
                "com.android.settings.deviceinfo.BMobileAccountsFragment",
                R.string.bmobile_accounts_title));
        setInteractiveClick(card2, () -> launchFragment(activity,
                "com.bmobile.fragments.SystemApplicationSettings",
                R.string.apps_dashboard_title));
        setInteractiveClick(card3, () -> launchFragment(activity,
                "com.bmobile.fragments.BMobileSettingsFragment",
                R.string.bmobile_settings_title));
        setInteractiveClick(card4, () -> launchApp(activity,
                "nethical.locklock", "nethical.locklock.MainActivity"));
        setInteractiveClick(card5, () -> launchFragment(activity,
                "com.bmobile.fragments.DeviceTweaksSettings",
                R.string.device_tweaks_title));
        setInteractiveClick(card6, () -> launchFragment(activity,
                "com.bmobile.fragments.SystemOptimizationSettings",
                R.string.system_optimization_title));
        setInteractiveClick(card7, () -> launchFragment(activity,
                "com.android.settings.applications.specialaccess.AppSecSettings",
                R.string.appsec_category_title));
        setInteractiveClick(bigCard, () -> launchFragment(activity,
                "com.android.settings.deviceinfo.BMobileUserInfoFragment",
                R.string.bmobile_userinfo_title));
    }

    private void setInteractiveClick(View v, Runnable action) {
        if (v == null || action == null) {
            return;
        }
        v.setClickable(true);
        v.setFocusable(true);
        v.setFocusableInTouchMode(false);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> action.run());
    }

    private void launchFragment(Activity activity, String fragmentClass, int titleResId) {
        if (activity == null) {
            return;
        }
        try {
            new SubSettingLauncher(activity)
                    .setDestination(fragmentClass)
                    .setTitleRes(titleResId)
                    .setSourceMetricsCategory(MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                    .launch();
        } catch (Exception e) {
            android.util.Log.e("Atomichub2View", "Failed to launch fragment: " + fragmentClass, e);
        }
    }

    private void launchApp(Activity activity, String packageName, String activityClass) {
        if (activity == null) {
            return;
        }
        try {
            Intent intent = new Intent();
            intent.setClassName(packageName, activityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("Atomichub2View", "Failed to launch app: " + packageName, e);
        }
    }

    private Activity findActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
