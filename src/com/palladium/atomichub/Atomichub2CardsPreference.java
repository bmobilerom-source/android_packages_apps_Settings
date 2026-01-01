/*
 * Lightweight preference embedding the Atomichub2 card grid into any screen.
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

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

public class Atomichub2CardsPreference extends Preference {

    public Atomichub2CardsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.atomichub2);
        setSelectable(false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        bindCard(holder, R.id.card1_2,
                "com.android.settings.deviceinfo.BMobileAccountsFragment",
                R.string.bmobile_accounts_title);
        bindCard(holder, R.id.card2_2,
                "com.bmobile.fragments.SystemApplicationSettings",
                R.string.apps_dashboard_title);
        bindCard(holder, R.id.card3_2,
                "com.bmobile.fragments.BMobileSettingsFragment",
                R.string.bmobile_settings_title);
        bindAppCard(holder, R.id.card4_2, "nethical.locklock", "nethical.locklock.MainActivity");
        bindCard(holder, R.id.card5_2,
                "com.bmobile.fragments.DeviceTweaksSettings",
                R.string.device_tweaks_title);
        bindCard(holder, R.id.card6_2,
                "com.bmobile.fragments.SystemOptimizationSettings",
                R.string.system_optimization_title);
        bindCard(holder, R.id.card7_2,
                "com.android.settings.applications.specialaccess.AppSecSettings",
                R.string.appsec_category_title);
        bindCard(holder, R.id.card2,
                "com.android.settings.deviceinfo.BMobileUserInfoFragment",
                R.string.bmobile_userinfo_title);
    }

    private void bindCard(PreferenceViewHolder holder, int viewId, String fragment, int titleRes) {
        View v = holder.itemView.findViewById(viewId);
        if (v == null) {
            v = holder.findViewById(viewId);
        }
        if (v == null) {
            return;
        }
        v.setClickable(true);
        v.setFocusable(true);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> launch(fragment, titleRes));
    }

    private void bindAppCard(PreferenceViewHolder holder, int viewId, String packageName,
            String className) {
        View v = holder.itemView.findViewById(viewId);
        if (v == null) {
            v = holder.findViewById(viewId);
        }
        if (v == null) {
            return;
        }
        v.setClickable(true);
        v.setFocusable(true);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> launchApp(packageName, className));
    }

    private void launchApp(String packageName, String className) {
        try {
            Activity activity = findActivity(getContext());
            if (activity == null) {
                return;
            }
            Intent intent = new Intent();
            intent.setClassName(packageName, className);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.summary_placeholder, Toast.LENGTH_SHORT).show();
        }
    }

    private void launch(String fragment, int titleRes) {
        try {
            Activity activity = findActivity(getContext());
            if (activity == null) {
                return;
            }
            new SubSettingLauncher(activity)
                    .setDestination(fragment)
                    .setTitleRes(titleRes)
                    .setSourceMetricsCategory(MetricsProto.MetricsEvent.CUSTOM_SETTINGS)
                    .launch();
        } catch (Exception ignored) {
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
