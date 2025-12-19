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

package com.android.settings.applications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.internal.logging.nano.MetricsProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic preference that creates cards for installed apps.
 * Cards appear programmatically based on app installation status.
 */
public class DynamicAppCardsPreference extends Preference {

    // App definitions
    private static class AppInfo {
        String packageName;
        String className;
        String title;
        String summary;
        boolean isFragment; // true for fragments, false for external apps

        AppInfo(String packageName, String className, String title, String summary, boolean isFragment) {
            this.packageName = packageName;
            this.className = className;
            this.title = title;
            this.summary = summary;
            this.isFragment = isFragment;
        }
    }

    private final List<AppInfo> availableApps = new ArrayList<>();

    public DynamicAppCardsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.dynamic_app_cards_container);
        setSelectable(false);
        scanInstalledApps();
    }

    private void scanInstalledApps() {
        // Define all possible apps
        List<AppInfo> allApps = new ArrayList<>();
        allApps.add(new AppInfo("com.libremobileos.sidebar", null, "Sidebar",
            "Access sidebar features", false));
        allApps.add(new AppInfo("com.android.documentsui", null, "File Manager",
            "Access cloud media and file management", false));
        allApps.add(new AppInfo("com.cylonid.nativealpha", null, "Native Alpha",
            "Open Native Alpha browser settings", false));
        allApps.add(new AppInfo("nethical.digipaws", null, "DigPaws",
            "Open DigPaws settings", false));
        allApps.add(new AppInfo("com.devrinth.launchpad", null, "System Launch Pad",
            "Quick search for online and system", false));

        // Check which apps are installed and add them to availableApps
        for (AppInfo app : allApps) {
            if (isAppInstalled(app.packageName)) {
                availableApps.add(app);
                android.util.Log.d("DynamicAppCards", "App available: " + app.title + " (" + app.packageName + ")");
            } else {
                android.util.Log.d("DynamicAppCards", "App not installed: " + app.title + " (" + app.packageName + ")");
            }
        }

        android.util.Log.d("DynamicAppCards", "Total available apps: " + availableApps.size());
    }

    private boolean isAppInstalled(String packageName) {
        try {
            getContext().getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        android.util.Log.d("DynamicAppCards", "onBindViewHolder called, available apps: " + availableApps.size());

        // Find the container where we'll add cards
        ViewGroup container = (ViewGroup) holder.itemView.findViewById(R.id.dynamic_cards_container);
        if (container == null) {
            android.util.Log.e("DynamicAppCards", "Container not found!");
            return;
        }

        // Clear any existing cards
        container.removeAllViews();

        // Create cards for each available app
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < availableApps.size(); i++) {
            AppInfo app = availableApps.get(i);
            createAppCard(inflater, container, app, i);
        }

        android.util.Log.d("DynamicAppCards", "Created " + availableApps.size() + " dynamic cards");
    }

    private void createAppCard(LayoutInflater inflater, ViewGroup container, AppInfo app, int index) {
        // Inflate the card layout
        View cardView = inflater.inflate(R.layout.dynamic_app_card, container, false);

        // Set card content
        TextView titleView = cardView.findViewById(R.id.card_title);
        TextView summaryView = cardView.findViewById(R.id.card_summary);

        if (titleView != null) titleView.setText(app.title);
        if (summaryView != null) summaryView.setText(app.summary);

        // Set click listener
        cardView.setOnClickListener(v -> {
            android.util.Log.d("DynamicAppCards", "Card clicked: " + app.title);
            if (app.isFragment) {
                launchFragment(app.packageName, app.title);
            } else {
                launchApp(app.packageName, app.className != null ? app.className : getDefaultClassName(app.packageName));
            }
        });

        // Make card interactive
        cardView.setClickable(true);
        cardView.setFocusable(true);
        cardView.setHapticFeedbackEnabled(true);

        // Add card to container
        container.addView(cardView);

        android.util.Log.d("DynamicAppCards", "Created card for: " + app.title);
    }

    private String getDefaultClassName(String packageName) {
        // Return default main activity class name for the package
        if ("com.libremobileos.sidebar".equals(packageName)) {
            return "com.libremobileos.sidebar.ui.sidebar.SidebarSettingsActivity";
        } else if ("com.android.documentsui".equals(packageName)) {
            return "com.android.documentsui.LauncherActivity";
        } else if ("com.cylonid.nativealpha".equals(packageName)) {
            return "com.cylonid.nativealpha.MainActivity";
        } else if ("nethical.digipaws".equals(packageName)) {
            return "nethical.digipaws.ui.activity.MainActivity";
        } else if ("com.devrinth.launchpad".equals(packageName)) {
            return "com.devrinth.launchpad.activities.SettingsActivity";
        }
        return packageName + ".MainActivity"; // fallback
    }

    private void launchFragment(String fragmentClass, String title) {
        try {
            Activity activity = findActivity(getContext());
            if (activity == null) return;

            new SubSettingLauncher(activity)
                .setDestination(fragmentClass)
                .setTitleText(title)
                .setSourceMetricsCategory(MetricsProto.MetricsEvent.DASHBOARD_SUMMARY)
                .launch();
        } catch (Exception e) {
            android.util.Log.e("DynamicAppCards", "Failed to launch fragment: " + fragmentClass, e);
            Toast.makeText(getContext(), "Failed to open " + title, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchApp(String packageName, String className) {
        try {
            Activity activity = findActivity(getContext());
            if (activity == null) return;

            Intent intent = new Intent();
            intent.setClassName(packageName, className);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("DynamicAppCards", "Failed to launch app: " + packageName, e);
            Toast.makeText(getContext(), "App not installed: " + packageName, Toast.LENGTH_SHORT).show();
        }
    }

    private Activity findActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof android.content.ContextWrapper) {
            return findActivity(((android.content.ContextWrapper) context).getBaseContext());
        }
        return null;
    }
}
