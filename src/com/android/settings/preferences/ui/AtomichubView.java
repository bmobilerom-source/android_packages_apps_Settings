/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.preferences.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.internal.logging.nano.MetricsProto;

/**
 * Self-contained widget container for Atomichub that wires click actions for all cards.
 */
public class AtomichubView extends LinearLayout {

    public AtomichubView(Context context) {
        super(context);
    }

    public AtomichubView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AtomichubView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        bindActions();
    }

    private void bindActions() {
        Activity activity = findActivity(getContext());

        setInteractiveClick(findViewById(R.id.mcstatus), () ->
                launchApp(activity, "com.demizo.daily_you", "com.demizo.daily_you.MainActivity"));
        setInteractiveClick(findViewById(R.id.mcui), () ->
                launchFragment(activity,
                        "com.android.settings.privatespace.PrivateSpaceDashboardFragment",
                        R.string.private_space_title));
        setInteractiveClick(findViewById(R.id.mctheme), () ->
                launchFragment(activity, "com.bmobile.fragments.MySessionFragment",
                        R.string.my_session_title));
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
            android.util.Log.e("AtomichubView", "Failed to launch fragment: " + fragmentClass, e);
        }
    }

    private void launchApp(Activity activity, String packageName, String className) {
        if (activity == null) {
            return;
        }
        try {
            Intent intent = new Intent();
            intent.setClassName(packageName, className);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("AtomichubView", "Failed to launch app: " + packageName, e);
            Toast.makeText(activity, "App not installed", Toast.LENGTH_SHORT).show();
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
