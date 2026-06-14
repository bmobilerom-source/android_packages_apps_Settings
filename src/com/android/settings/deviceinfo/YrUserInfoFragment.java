/*
 * YR-branded user info — Your chat, Daily You, editable local display name.
 */
package com.android.settings.deviceinfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settingslib.widget.LayoutPreference;
import com.android.settingslib.widget.TopIntroPreference;

/** User profile screen for YR Study / School / Social / Expressive layouts. */
public class YrUserInfoFragment extends BMobileUserInfoFragment {

    private static final String KEY_INTRO = "yr_userinfo_intro";
    private static final String PREFS_NAME = "yr_user_profile";
    private static final String KEY_DISPLAY_NAME = "display_name";

    @Override
    protected int getPrefXmlResId() {
        return R.xml.yr_userinfo_pref;
    }

    @Override
    public void onStart() {
        super.onStart();
        bindYrProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindYrProfile();
    }

    private SharedPreferences profilePrefs() {
        return requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private String getDisplayName() {
        return profilePrefs().getString(KEY_DISPLAY_NAME, "");
    }

    private void saveDisplayName(String name) {
        profilePrefs().edit().putString(KEY_DISPLAY_NAME, name.trim()).commit();
    }

    private String getEffectiveName() {
        final String saved = getDisplayName();
        if (!TextUtils.isEmpty(saved)) {
            return saved;
        }
        return getString(R.string.branding_yr_user_name);
    }

    private void bindYrProfile() {
        if (getPreferenceScreen() == null) {
            return;
        }
        bindIntro();
        bindDisplayNameOnCard();
    }

    private void bindIntro() {
        final Context context = getContext();
        if (context == null) {
            return;
        }
        final TopIntroPreference intro =
                (TopIntroPreference) getPreferenceScreen().findPreference(KEY_INTRO);
        if (intro == null) {
            return;
        }
        intro.setTitle(context.getString(R.string.yr_userinfo_intro, getEffectiveName()));
    }

    private void bindDisplayNameOnCard() {
        final LayoutPreference headerPreference =
                (LayoutPreference) getPreferenceScreen().findPreference(getUserCardKey());
        if (headerPreference == null) {
            return;
        }

        final TextView titleView = headerPreference.findViewById(R.id.entity_header_title);
        final TextView emailView = headerPreference.findViewById(R.id.email);
        if (emailView != null) {
            emailView.setVisibility(View.GONE);
        }
        if (titleView == null) {
            return;
        }

        final String saved = getDisplayName();
        final CharSequence label = !TextUtils.isEmpty(saved)
                ? saved
                : getString(R.string.kidssafe_owner_name_hint);
        titleView.setText(label);
        titleView.setOnClickListener(v -> showNameDialog(titleView));
    }

    private void showNameDialog(TextView titleView) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        final EditText input = new EditText(activity);
        input.setHint(R.string.kidssafe_owner_name_dialog_hint);
        input.setText(getDisplayName());
        input.setSingleLine(true);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.kidssafe_owner_name_dialog_title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    final String name = input.getText().toString().trim();
                    saveDisplayName(name);
                    titleView.setText(TextUtils.isEmpty(name)
                            ? getString(R.string.kidssafe_owner_name_hint)
                            : name);
                    bindIntro();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
