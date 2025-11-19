 /*
 * Copyright (C) 2016 The Pure Nexus Project
 * used for Nitrogen OS
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

package com.android.settings.deviceinfo;

import static android.app.Activity.RESULT_OK;

import com.android.internal.logging.nano.MetricsProto;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.Surface;
import com.android.settings.R;
import com.android.settings.Utils;
import android.net.Uri;
import com.android.internal.util.UserIcons;
import com.android.settings.core.SettingsBaseActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.drawable.CircleFramedDrawable;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.widget.LayoutPreference;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.ComponentName;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.FileNotFoundException;

import com.android.settings.applications.specialaccess.InstallAppWhitelistController;
import com.android.settings.applications.specialaccess.BlockUsbPopupController;
import com.android.settings.applications.specialaccess.BlockLocationSettingsController;
import com.android.settings.applications.specialaccess.BlockAccountDashboardController;
import com.android.settings.applications.specialaccess.BlockSafetyCenterController;
import com.android.settings.applications.AppDowngradePreferenceController;

public class UserInfoFragement extends SettingsPreferenceFragment {

    UserManager mUserManager;
    Context context;
    private static final String KEY_USER_CARD = "user_header";
    private InstallAppWhitelistController mInstallAppWhitelistController;
    private BlockUsbPopupController mBlockUsbPopupController;
    private BlockLocationSettingsController mBlockLocationSettingsController;
    private BlockAccountDashboardController mBlockAccountDashboardController;
    private BlockSafetyCenterController mBlockSafetyCenterController;
    private AppDowngradePreferenceController mAppDowngradeController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userinfo_pref);
	context = getActivity();
	((SettingsBaseActivity)getActivity()).mAppBarLayout.setExpanded(false);
        
        // Initialize controllers
        final PreferenceScreen prefScreen = getPreferenceScreen();
        if (prefScreen != null) {
            // Initialize Install App Whitelist controller
            mInstallAppWhitelistController = new InstallAppWhitelistController(
                    context, "install_app_whitelist_toggle");
            if (mInstallAppWhitelistController.getAvailabilityStatus() == 
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference whitelistPref = prefScreen.findPreference("install_app_whitelist_toggle");
                if (whitelistPref != null) {
                    mInstallAppWhitelistController.updateState(whitelistPref);
                    whitelistPref.setOnPreferenceChangeListener(mInstallAppWhitelistController);
                }
            }
            
            // Initialize Block USB Popup controller
            mBlockUsbPopupController = new BlockUsbPopupController(
                    context, "block_usb_popup_toggle");
            if (mBlockUsbPopupController.getAvailabilityStatus() == 
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference usbPopupPref = prefScreen.findPreference("block_usb_popup_toggle");
                if (usbPopupPref != null) {
                    mBlockUsbPopupController.updateState(usbPopupPref);
                    usbPopupPref.setOnPreferenceChangeListener(mBlockUsbPopupController);
                }
            }
            
            // Initialize Block Location Settings controller
            mBlockLocationSettingsController = new BlockLocationSettingsController(
                    context, "block_location_settings_toggle");
            if (mBlockLocationSettingsController.getAvailabilityStatus() == 
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference locationPref = prefScreen.findPreference("block_location_settings_toggle");
                if (locationPref != null) {
                    mBlockLocationSettingsController.updateState(locationPref);
                    locationPref.setOnPreferenceChangeListener(mBlockLocationSettingsController);
                }
            }
            
            // Initialize Block Account Dashboard controller
            mBlockAccountDashboardController = new BlockAccountDashboardController(
                    context, "block_account_dashboard_toggle");
            if (mBlockAccountDashboardController.getAvailabilityStatus() == 
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference accountPref = prefScreen.findPreference("block_account_dashboard_toggle");
                if (accountPref != null) {
                    mBlockAccountDashboardController.updateState(accountPref);
                    accountPref.setOnPreferenceChangeListener(mBlockAccountDashboardController);
                }
            }
            
            // Initialize Block Safety Center controller
            mBlockSafetyCenterController = new BlockSafetyCenterController(
                    context, "block_safety_center_toggle");
            if (mBlockSafetyCenterController.getAvailabilityStatus() == 
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference safetyCenterPref = prefScreen.findPreference("block_safety_center_toggle");
                if (safetyCenterPref != null) {
                    mBlockSafetyCenterController.updateState(safetyCenterPref);
                    safetyCenterPref.setOnPreferenceChangeListener(mBlockSafetyCenterController);
                }
            }
            
            // Initialize App Downgrade controller
            mAppDowngradeController = new AppDowngradePreferenceController(
                    context, "pm_downgrade_allowed");
            if (mAppDowngradeController.getAvailabilityStatus() == 
                    com.android.settings.core.BasePreferenceController.AVAILABLE) {
                SwitchPreference downgradePref = prefScreen.findPreference("pm_downgrade_allowed");
                if (downgradePref != null) {
                    mAppDowngradeController.updateState(downgradePref);
                    downgradePref.setOnPreferenceChangeListener(mAppDowngradeController);
                }
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        onUserCard();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

   static String getEmail(Context context) {
            AccountManager accountManager = AccountManager.get(context);
            Account account = getAccount(accountManager);

            if (account == null) {
                return null;
            } else {
                return account.name;
            }
        }

   private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
          return account;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null && requestCode == 100) {
            String path = data.getData().toString();
            try {
                iv.setImageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(path))));
            } catch (FileNotFoundException e) {
                iv.setImageResource(R.drawable.user);
            }
            context.getSharedPreferences("image_path", Context.MODE_PRIVATE).edit().putString("image_path", path).commit();
        }
    }

    private ImageView iv;

    private void onUserCard() {
        final LayoutPreference headerPreference =
                (LayoutPreference) getPreferenceScreen().findPreference(KEY_USER_CARD);
        final View userCard = headerPreference.findViewById(R.id.entity_header);
        final TextView useremail = headerPreference.findViewById(R.id.email);
        String email = getEmail(getContext());
        useremail.setText(email!=null?email:"Add a Google account to show email");
	iv = headerPreference.findViewById(R.id.image_holder);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
	iv.setOnClickListener(v -> {
		startActivityForResult(i, 100);
	});

        final Activity context = getActivity();
        final Bundle bundle = getArguments();

        String path = context.getSharedPreferences("image_path", Context.MODE_PRIVATE).getString("image_path", "");
        if (!path.isEmpty()) {
            try {
                iv.setImageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(path))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        final EntityHeaderController controller = EntityHeaderController
                .newInstance(context, this, userCard)
                .setButtonActions(EntityHeaderController.ActionType.ACTION_NONE,
                        EntityHeaderController.ActionType.ACTION_NONE);

        final int iconId = bundle.getInt("icon_id", 0);
        if (iconId == 0) {
            final UserManager userManager = (UserManager) getActivity().getSystemService(
                    Context.USER_SERVICE);
            final UserInfo info = Utils.getExistingUser(userManager,
                    android.os.Process.myUserHandle());
            controller.setLabel(info.name);
        }

        controller.done(true);
    }
}
