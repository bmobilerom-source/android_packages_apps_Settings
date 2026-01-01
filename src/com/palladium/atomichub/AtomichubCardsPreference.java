/*
 * Lightweight preference embedding the Atomichub card grid into any screen.
 * Each card launches the correct target: apps or Anatolia fragments.
 */
package com.palladium.atomichub;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.internal.logging.nano.MetricsProto;

public class AtomichubCardsPreference extends Preference {

    public AtomichubCardsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.atomichub);
        setSelectable(false);

        android.util.Log.d("AtomichubCardsPreference", "AtomichubCardsPreference created, context=" + context);

        // #region agent log
        try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub-pref\",\"runId\":\"init\",\"hypothesisId\":\"J\",\"location\":\"AtomichubCardsPreference.java:constructor\",\"message\":\"AtomichubCardsPreference created\",\"data\":{\"context\":\"" + context.getClass().getName() + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
        // #endregion
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        android.util.Log.d("AtomichubCardsPreference", "onBindViewHolder called, holder=" + holder);

        // #region agent log
        try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub-pref\",\"runId\":\"bind\",\"hypothesisId\":\"J\",\"location\":\"AtomichubCardsPreference.java:onBindViewHolder\",\"message\":\"onBindViewHolder called\",\"data\":{\"holderNull\":" + (holder == null) + ",\"viewNull\":" + (holder != null ? holder.itemView == null : true) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
        // #endregion
        
        // Journal → Daily Journal app
        bindAppCard(holder, R.id.mcstatus, "com.demizo.daily_you", "com.demizo.daily_you.MainActivity");
        // Private → PrivateSpaceDashboardFragment
        bindFragmentCard(holder, R.id.mcui, "com.android.settings.privatespace.PrivateSpaceDashboardFragment", R.string.private_space_title);
        // Duress → GestureSettings
        bindFragmentCard(holder, R.id.mcmisc, "com.epic.fragments.GestureSettings", R.string.gestures_title);
        // Session → Session Messenger app
        bindAppCard(holder, R.id.mctheme, "network.loki.messenger", "network.loki.messenger.RoutingActivity");
    }

    private void bindFragmentCard(PreferenceViewHolder holder, int viewId, String fragment, int titleRes) {
        View v = holder.itemView.findViewById(viewId);
        android.util.Log.d("AtomichubCardsPreference", "Binding fragment card - viewId: " + viewId + ", fragment: " + fragment + ", view found: " + (v != null));

        if (v == null) {
            android.util.Log.e("AtomichubCardsPreference", "Card view is null for id: " + viewId + " - fragment: " + fragment + " - trying holder.findViewById");
            v = holder.findViewById(viewId);
            if (v == null) {
                android.util.Log.e("AtomichubCardsPreference", "Card view still null with holder.findViewById for id: " + viewId);
                return;
            }
        }
        v.setClickable(true);
        v.setFocusable(true);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> {
            android.util.Log.d("AtomichubCardsPreference", "Fragment card clicked: " + fragment);
            launchFragment(fragment, titleRes);
        });
    }

    private void bindAppCard(PreferenceViewHolder holder, int viewId, String packageName, String className) {
        View v = holder.itemView.findViewById(viewId);
        android.util.Log.d("AtomichubCardsPreference", "Binding app card - viewId: " + viewId + ", package: " + packageName + ", view found: " + (v != null));

        if (v == null) {
            android.util.Log.e("AtomichubCardsPreference", "App card view is null for id: " + viewId + " - package: " + packageName + " - trying holder.findViewById");
            v = holder.findViewById(viewId);
            if (v == null) {
                android.util.Log.e("AtomichubCardsPreference", "App card view still null with holder.findViewById for id: " + viewId);
                return;
            }
        }
        v.setClickable(true);
        v.setFocusable(true);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> {
            android.util.Log.d("AtomichubCardsPreference", "App card clicked: " + packageName);
            launchApp(packageName, className);
        });
    }

    private void launchFragment(String fragment, int titleRes) {
        android.util.Log.d("AtomichubCardsPreference", "Launching fragment: " + fragment);
        // #region agent log
        try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub-pref\",\"runId\":\"fragment\",\"hypothesisId\":\"J\",\"location\":\"AtomichubCardsPreference.java:launchFragment\",\"message\":\"launching fragment\",\"data\":{\"fragment\":\"" + fragment + "\",\"titleRes\":" + titleRes + "},\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
        // #endregion

        try {
            android.app.Activity activity = findActivity(getContext());
            android.util.Log.d("AtomichubCardsPreference", "Activity found: " + (activity != null));
            // #region agent log
            try { java.io.FileWriter fw = new java.io.FileWriter("/media/linuxmain/lineageos/android/lineageos/.cursor/debug.log", true); fw.write("{\"sessionId\":\"atomichub-pref\",\"runId\":\"fragment\",\"hypothesisId\":\"J\",\"location\":\"AtomichubCardsPreference.java:launchFragment\",\"message\":\"activity found\",\"data\":{\"activityNull\":" + (activity == null) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n"); fw.close(); } catch (Exception e) {}
            // #endregion

            if (activity == null) {
                android.util.Log.e("AtomichubCardsPreference", "No Activity found for launching fragment: " + fragment);
                return;
            }
            android.util.Log.d("AtomichubCardsPreference", "Launching SubSettingLauncher for: " + fragment);
            new SubSettingLauncher(activity)
                    .setDestination(fragment)
                    .setTitleRes(titleRes)
                    .setSourceMetricsCategory(MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                    .launch();
            android.util.Log.d("AtomichubCardsPreference", "SubSettingLauncher launched successfully");
        } catch (Exception e) {
            android.util.Log.e("AtomichubCardsPreference", "Failed to launch fragment: " + fragment, e);
        }
    }

    private void launchApp(String packageName, String className) {
        try {
            android.app.Activity activity = findActivity(getContext());
            if (activity == null) {
                android.util.Log.e("AtomichubCardsPreference", "No Activity found for launching app: " + packageName);
                return;
            }
            Intent intent = new Intent();
            intent.setClassName(packageName, className);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("AtomichubCardsPreference", "Failed to launch app: " + packageName + "/" + className, e);
            Toast.makeText(getContext(), "App not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private android.app.Activity findActivity(android.content.Context context) {
        while (context instanceof android.content.ContextWrapper) {
            if (context instanceof android.app.Activity) {
                return (android.app.Activity) context;
            }
            context = ((android.content.ContextWrapper) context).getBaseContext();
        }
        return null;
    }

}
