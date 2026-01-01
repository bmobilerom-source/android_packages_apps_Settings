/*
 * Lightweight preference embedding the Atomichub2 card grid into any screen.
 * Each card launches the correct Anatolia fragment.
 */
package com.palladium.atomichub;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.internal.logging.nano.MetricsProto;

public class Atomichub2CardsPreference extends Preference {

    public Atomichub2CardsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.atomichub2);
        setSelectable(false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        
        // #region agent log
        try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2-pref\",\"runId\":\"bind\",\"hypothesisId\":\"E\",\"location\":\"Atomichub2CardsPreference.java:onBindViewHolder\",\"message\":\"onBindViewHolder called\",\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
        // #endregion
        
        // Small cards in horizontal scroll view
        // Accounts → BMobileAccountsFragment
        bindCard(holder, R.id.card1_2, "com.android.settings.deviceinfo.BMobileAccountsFragment", R.string.bmobile_accounts_title);
        // Apps → AppDashboardFragment
        bindCard(holder, R.id.card2_2, "com.android.settings.applications.AppDashboardFragment", R.string.apps_dashboard_title);
        // Bmobile → BMobileSettingsFragment
        bindCard(holder, R.id.card3_2, "com.epic.fragments.BMobileSettingsFragment", R.string.bmobile_settings_title);
        // AppLock → LockLock app
        bindAppCard(holder, R.id.card4_2, "nethical.locklock", "nethical.locklock.MainActivity");
        // DeviceTweaks → GestureSettings
        bindCard(holder, R.id.card5_2, "com.epic.fragments.GestureSettings", R.string.gestures_title);
        // Optimize → SystemOptimizationSettings
        bindCard(holder, R.id.card6_2, "com.epic.fragments.SystemOptimizationSettings", R.string.system_optimization_title);
        // bSettings → AppSecSettings
        android.util.Log.d("Atomichub2CardsPreference", "About to bind card7_2");
        bindCard(holder, R.id.card7_2, "com.android.settings.applications.specialaccess.AppSecSettings", R.string.appsec_category_title);
        android.util.Log.d("Atomichub2CardsPreference", "Finished binding card7_2");
        // Big Personal card → BMobileUserInfoFragment
        bindCard(holder, R.id.card2, "com.android.settings.deviceinfo.BMobileUserInfoFragment", R.string.bmobile_userinfo_title);

        // Back button - commented out as btn_trans2 doesn't exist in layout
        // View backBtn = holder.findViewById(R.id.btn_trans2);
        // if (backBtn != null) {
        //     backBtn.setOnClickListener(v -> {
        //         // Try to go back
        //         try {
        //             android.app.Activity activity = findActivity(getContext());
        //             if (activity != null) {
        //                 activity.onBackPressed();
        //             }
        //         } catch (Exception ignored) {}
        //     });
        // }
    }

    private void bindCard(PreferenceViewHolder holder, int viewId, String fragment, int titleRes) {
        android.util.Log.d("Atomichub2CardsPreference", "bindCard called for viewId: " + viewId + ", fragment: " + fragment);
        View v = holder.itemView.findViewById(viewId);
        android.util.Log.d("Atomichub2CardsPreference", "View found with itemView.findViewById: " + (v != null));
        if (v == null) {
            // #region agent log
            try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2-pref\",\"runId\":\"bind\",\"hypothesisId\":\"E\",\"location\":\"Atomichub2CardsPreference.java:bindCard\",\"message\":\"card not found with itemView, trying holder\",\"data\":{\"viewId\":" + viewId + "},\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
            // #endregion
            android.util.Log.d("Atomichub2CardsPreference", "Trying holder.findViewById for viewId: " + viewId);
            v = holder.findViewById(viewId);
            android.util.Log.d("Atomichub2CardsPreference", "View found with holder.findViewById: " + (v != null));
            if (v == null) {
                android.util.Log.e("Atomichub2CardsPreference", "Card view is null for id: " + viewId + " - fragment: " + fragment);
                // #region agent log
                try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2-pref\",\"runId\":\"bind\",\"hypothesisId\":\"E\",\"location\":\"Atomichub2CardsPreference.java:bindCard\",\"message\":\"card not found with holder either\",\"data\":{\"viewId\":" + viewId + "},\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
                // #endregion
                return;
            }
        }
        v.setClickable(true);
        v.setFocusable(true);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> {
            // #region agent log
            try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2-pref\",\"runId\":\"click\",\"hypothesisId\":\"E\",\"location\":\"Atomichub2CardsPreference.java:cardClick\",\"message\":\"card clicked\",\"data\":{\"fragment\":\"" + fragment + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
            // #endregion
            launch(fragment, titleRes);
        });
    }

    private void bindAppCard(PreferenceViewHolder holder, int viewId, String packageName, String className) {
        View v = holder.itemView.findViewById(viewId);
        // #region agent log
        try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2-pref\",\"runId\":\"bind\",\"hypothesisId\":\"J\",\"location\":\"Atomichub2CardsPreference.java:bindAppCard\",\"message\":\"binding app card\",\"data\":{\"viewId\":" + viewId + ",\"package\":\"" + packageName + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
        // #endregion

        if (v == null) {
            android.util.Log.d("Atomichub2CardsPreference", "Card view is null for id: " + viewId + " - trying holder.findViewById");
            v = holder.findViewById(viewId);
            if (v == null) {
                android.util.Log.d("Atomichub2CardsPreference", "Card view still null with holder.findViewById for id: " + viewId);
                return;
            }
        }

        android.util.Log.d("Atomichub2CardsPreference", "Setting click listener for app card " + viewId);
        v.setClickable(true);
        v.setFocusable(true);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> {
            android.util.Log.d("Atomichub2CardsPreference", "App card clicked: " + viewId + " -> " + packageName);
            // #region agent log
            try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub2-pref\",\"runId\":\"click\",\"hypothesisId\":\"J\",\"location\":\"Atomichub2CardsPreference.java:appCardClick\",\"message\":\"app card clicked\",\"data\":{\"viewId\":" + viewId + ",\"package\":\"" + packageName + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
            // #endregion
            launchApp(packageName, className);
        });
    }

    private void launchApp(String packageName, String className) {
        try {
            android.app.Activity activity = findActivity(getContext());
            if (activity == null) {
                android.util.Log.e("Atomichub2CardsPreference", "No Activity found for launching app: " + packageName);
                return;
            }
            android.content.Intent intent = new android.content.Intent();
            intent.setClassName(packageName, className);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("Atomichub2CardsPreference", "Failed to launch app: " + packageName + "/" + className, e);
            android.widget.Toast.makeText(getContext(), "App not installed", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void launch(String fragment, int titleRes) {
        try {
            android.app.Activity activity = findActivity(getContext());
            if (activity == null) {
                android.util.Log.e("Atomichub2CardsPreference", "No Activity found for launching fragment: " + fragment);
                return;
            }
            new SubSettingLauncher(activity)
                    .setDestination(fragment)
                    .setTitleRes(titleRes)
                    .setSourceMetricsCategory(MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                    .launch();
        } catch (Exception e) {
            android.util.Log.e("Atomichub2CardsPreference", "Failed to launch fragment: " + fragment, e);
        }
    }
    
    private android.app.Activity findActivity(Context context) {
        while (context instanceof android.content.ContextWrapper) {
            if (context instanceof android.app.Activity) {
                return (android.app.Activity) context;
            }
            context = ((android.content.ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}

