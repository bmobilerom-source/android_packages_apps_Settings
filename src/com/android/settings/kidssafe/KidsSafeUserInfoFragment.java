/*
 * Copyright (C) 2016 The Pure Nexus Project
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

package com.android.settings.kidssafe;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.widget.LayoutPreference;

import java.io.FileNotFoundException;

/** KidsSafe user profile sub-screen opened from the KS Fun homepage card. */
public class KidsSafeUserInfoFragment extends SettingsPreferenceFragment {

    private static final String KEY_USER_CARD = "kidssafe_user_header";
    private static final String PREFS_NAME = "kidssafe_user_profile";
    private static final String KEY_DISPLAY_NAME = "display_name";

    private ImageView mAvatarView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.kidssafe_userinfo_pref);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindUserCard();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null
                && requestCode == 100 && mAvatarView != null) {
            String path = data.getData().toString();
            try {
                mAvatarView.setImageBitmap(BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(Uri.parse(path))));
            } catch (FileNotFoundException e) {
                mAvatarView.setImageResource(R.drawable.user_png);
            }
            requireContext().getSharedPreferences("image_path", Context.MODE_PRIVATE)
                    .edit()
                    .putString("image_path", path)
                    .commit();
        }
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

    private void bindUserCard() {
        final LayoutPreference headerPreference =
                (LayoutPreference) getPreferenceScreen().findPreference(KEY_USER_CARD);
        if (headerPreference == null) {
            return;
        }

        final View userCard = headerPreference.findViewById(R.id.entity_header);
        final TextView titleView = headerPreference.findViewById(R.id.entity_header_title);
        final TextView emailView = headerPreference.findViewById(R.id.email);
        mAvatarView = headerPreference.findViewById(R.id.image_holder);

        if (emailView != null) {
            emailView.setVisibility(View.GONE);
        }

        final String displayName = getDisplayName();
        final CharSequence label = !TextUtils.isEmpty(displayName)
                ? displayName
                : getString(R.string.kidssafe_owner_name_hint);

        if (titleView != null) {
            titleView.setText(label);
            titleView.setOnClickListener(v -> showNameDialog(titleView));
        }

        Intent pickImage = new Intent(Intent.ACTION_GET_CONTENT);
        pickImage.setType("image/*");
        if (mAvatarView != null) {
            mAvatarView.setOnClickListener(v -> startActivityForResult(pickImage, 100));
        }

        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        String path = activity.getSharedPreferences("image_path", Context.MODE_PRIVATE)
                .getString("image_path", "");
        if (!path.isEmpty() && mAvatarView != null) {
            try {
                mAvatarView.setImageBitmap(BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(Uri.parse(path))));
            } catch (FileNotFoundException e) {
                // Keep default avatar.
            }
        }

        final EntityHeaderController controller = EntityHeaderController
                .newInstance(activity, this, userCard)
                .setButtonActions(EntityHeaderController.ActionType.ACTION_NONE,
                        EntityHeaderController.ActionType.ACTION_NONE)
                .setLabel(label);

        controller.done(true);
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
                    bindUserCard();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
