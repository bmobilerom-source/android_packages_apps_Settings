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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.widget.LayoutPreference;

import java.io.FileNotFoundException;

/** KidsSafe user profile sub-screen opened from the KS Fun homepage card. */
public class KidsSafeUserInfoFragment extends SettingsPreferenceFragment {

    private static final String KEY_USER_CARD = "kidssafe_user_header";

    private Context mContext;
    private ImageView mAvatarView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.kidssafe_userinfo_pref);
        mContext = getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        bindUserCard();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    static String getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);
        return account != null ? account.name : null;
    }

    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        return accounts.length > 0 ? accounts[0] : null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null
                && requestCode == 100 && mAvatarView != null && mContext != null) {
            String path = data.getData().toString();
            try {
                mAvatarView.setImageBitmap(BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(Uri.parse(path))));
            } catch (FileNotFoundException e) {
                mAvatarView.setImageResource(R.drawable.user_png);
            }
            mContext.getSharedPreferences("image_path", Context.MODE_PRIVATE)
                    .edit()
                    .putString("image_path", path)
                    .commit();
        }
    }

    private void bindUserCard() {
        final LayoutPreference headerPreference =
                (LayoutPreference) getPreferenceScreen().findPreference(KEY_USER_CARD);
        if (headerPreference == null) {
            return;
        }

        final View userCard = headerPreference.findViewById(R.id.entity_header);
        final TextView userEmail = headerPreference.findViewById(R.id.email);
        mAvatarView = headerPreference.findViewById(R.id.image_holder);

        String email = getEmail(getContext());
        userEmail.setText(email != null ? email : "Add a Google account to show email");

        Intent pickImage = new Intent(Intent.ACTION_GET_CONTENT);
        pickImage.setType("image/*");
        mAvatarView.setOnClickListener(v -> startActivityForResult(pickImage, 100));

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

        final Bundle bundle = getArguments() != null ? getArguments() : Bundle.EMPTY;
        final EntityHeaderController controller = EntityHeaderController
                .newInstance(activity, this, userCard)
                .setButtonActions(EntityHeaderController.ActionType.ACTION_NONE,
                        EntityHeaderController.ActionType.ACTION_NONE);

        if (bundle.getInt("icon_id", 0) == 0) {
            final UserManager userManager =
                    (UserManager) activity.getSystemService(Context.USER_SERVICE);
            final UserInfo info = Utils.getExistingUser(userManager,
                    android.os.Process.myUserHandle());
            controller.setLabel(info.name);
        }

        controller.done(true);
    }
}
