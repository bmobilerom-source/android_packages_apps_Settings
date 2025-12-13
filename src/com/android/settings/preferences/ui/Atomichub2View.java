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

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.internal.logging.nano.MetricsProto;

/**
 * Self-contained widget container for Atomichub2 that wires click actions for all cards.
 * Drop this layout anywhere; no fragment wiring needed.
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
        
        // #region agent log
        try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2\",\"runId\":\"init\",\"hypothesisId\":\"A\",\"location\":\"Atomichub2View.java:bindActions\",\"message\":\"bindActions called\",\"data\":{\"activityNull\":" + (activity == null) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
        // #endregion
        
        // Small cards in horizontal scroll view
        View card1 = findViewById(R.id.card1_2);  // Accounts
        View card2 = findViewById(R.id.card2_2);  // Apps
        View card3 = findViewById(R.id.card3_2);  // Bmobile
        View card4 = findViewById(R.id.card4_2);  // AppLock
        View card5 = findViewById(R.id.card5_2);  // DeviceTweaks
        View card6 = findViewById(R.id.card6_2);  // Optimize
        
        // Big card
        View bigCard = findViewById(R.id.card2);  // Personal
        
        // Back button
        View backBtn = findViewById(R.id.btn_trans2);

        // #region agent log
        try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2\",\"runId\":\"init\",\"hypothesisId\":\"B\",\"location\":\"Atomichub2View.java:cards\",\"message\":\"cards found\",\"data\":{\"card1\":" + (card1 != null) + ",\"card2\":" + (card2 != null) + ",\"card3\":" + (card3 != null) + ",\"card4\":" + (card4 != null) + ",\"card5\":" + (card5 != null) + ",\"card6\":" + (card6 != null) + ",\"bigCard\":" + (bigCard != null) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
        // #endregion

        // Bind click actions
        // Accounts → BMobileAccountsFragment
        setInteractiveClick(card1, () -> {
            // #region agent log
            try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2\",\"runId\":\"click\",\"hypothesisId\":\"C\",\"location\":\"Atomichub2View.java:card1\",\"message\":\"accounts card clicked\",\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
            // #endregion
            launchFragment(activity, "com.android.settings.deviceinfo.BMobileAccountsFragment", R.string.bmobile_accounts_title);
        });
        // Apps → AppDashboardFragment
        setInteractiveClick(card2, () -> {
            // #region agent log
            try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2\",\"runId\":\"click\",\"hypothesisId\":\"C\",\"location\":\"Atomichub2View.java:card2\",\"message\":\"apps card clicked\",\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
            // #endregion
            launchFragment(activity, "com.android.settings.applications.AppDashboardFragment", R.string.apps_dashboard_title);
        });
        // Bmobile → BMobileSettingsFragment
        setInteractiveClick(card3, () -> {
            // #region agent log
            try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2\",\"runId\":\"click\",\"hypothesisId\":\"C\",\"location\":\"Atomichub2View.java:card3\",\"message\":\"bmobile card clicked\",\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
            // #endregion
            launchFragment(activity, "com.epic.fragments.BMobileSettingsFragment", R.string.bmobile_settings_title);
        });
        // AppLock → LockLock app
        setInteractiveClick(card4, () -> {
            // #region agent log
            try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2\",\"runId\":\"click\",\"hypothesisId\":\"C\",\"location\":\"Atomichub2View.java:card4\",\"message\":\"applock card clicked\",\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
            // #endregion
            launchApp(activity, "nethical.locklock", "nethical.locklock.MainActivity");
        });
        // DeviceTweaks → GestureSettings
        setInteractiveClick(card5, () -> {
            // #region agent log
            try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2\",\"runId\":\"click\",\"hypothesisId\":\"C\",\"location\":\"Atomichub2View.java:card5\",\"message\":\"devicetweaks card clicked\",\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
            // #endregion
            launchFragment(activity, "com.epic.fragments.GestureSettings", R.string.gestures_title);
        });
        // Optimize → SystemOptimizationSettings
        setInteractiveClick(card6, () -> {
            // #region agent log
            try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2\",\"runId\":\"click\",\"hypothesisId\":\"C\",\"location\":\"Atomichub2View.java:card6\",\"message\":\"optimize card clicked\",\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
            // #endregion
            launchFragment(activity, "com.epic.fragments.SystemOptimizationSettings", R.string.system_optimization_title);
        });
        // Big Personal card → BMobileUserInfoFragment
        setInteractiveClick(bigCard, () -> {
            // #region agent log
            try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2\",\"runId\":\"click\",\"hypothesisId\":\"C\",\"location\":\"Atomichub2View.java:bigCard\",\"message\":\"big personal card clicked\",\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
            // #endregion
            launchFragment(activity, "com.android.settings.deviceinfo.BMobileUserInfoFragment", R.string.bmobile_userinfo_title);
        });
        
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                if (activity != null) {
                    activity.onBackPressed();
                }
            });
        }
    }

    private void setInteractiveClick(View v, Runnable action) {
        if (v == null || action == null) return;
        v.setClickable(true);
        v.setFocusable(true);
        v.setFocusableInTouchMode(false);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> action.run());
    }

    private void launchFragment(Activity activity, String fragmentClass, int titleResId) {
        if (activity == null) return;
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

