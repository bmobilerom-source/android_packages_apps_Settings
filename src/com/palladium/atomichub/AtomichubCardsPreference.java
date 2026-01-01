/*
 * Lightweight preference embedding the Atomichub card grid into any screen.
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

public class AtomichubCardsPreference extends Preference {

    public AtomichubCardsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.atomichub);
        setSelectable(false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        bindAppCard(holder, R.id.mcstatus, "com.demizo.daily_you", "com.demizo.daily_you.MainActivity");
        bindFragmentCard(holder, R.id.mcui,
                "com.android.settings.privatespace.PrivateSpaceDashboardFragment",
                R.string.private_space_title);
        bindNoOpCard(holder, R.id.mcmisc);
        bindFragmentCard(holder, R.id.mctheme,
                "com.bmobile.fragments.MySessionFragment",
                R.string.my_session_title);
    }

    private void bindFragmentCard(PreferenceViewHolder holder, int viewId, String fragment,
            int titleRes) {
        View v = findCardView(holder, viewId);
        if (v == null) {
            return;
        }
        v.setClickable(true);
        v.setFocusable(true);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> launchFragment(fragment, titleRes));
    }

    private void bindAppCard(PreferenceViewHolder holder, int viewId, String packageName,
            String className) {
        View v = findCardView(holder, viewId);
        if (v == null) {
            return;
        }
        v.setClickable(true);
        v.setFocusable(true);
        v.setHapticFeedbackEnabled(true);
        v.setOnClickListener(view -> launchApp(packageName, className));
    }

    private void bindNoOpCard(PreferenceViewHolder holder, int viewId) {
        View v = findCardView(holder, viewId);
        if (v == null) {
            return;
        }
        v.setClickable(false);
        v.setFocusable(false);
    }

    private View findCardView(PreferenceViewHolder holder, int viewId) {
        View v = holder.itemView.findViewById(viewId);
        if (v == null) {
            v = holder.findViewById(viewId);
        }
        return v;
    }

    private void launchFragment(String fragment, int titleRes) {
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
        } catch (Exception e) {
            android.util.Log.e("AtomichubCardsPreference", "Failed to launch fragment: " + fragment,
                    e);
        }
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
            android.util.Log.e("AtomichubCardsPreference", "Failed to launch app: " + packageName,
                    e);
            Toast.makeText(getContext(), "App not installed", Toast.LENGTH_SHORT).show();
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
