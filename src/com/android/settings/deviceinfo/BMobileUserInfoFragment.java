/*
 * bMobile variant of UserInfoFragement to keep resources isolated.
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
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.SettingsBaseActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.password.ConfirmDeviceCredentialActivity;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.widget.LayoutPreference;
import com.android.settings.homepage.DashboardStyleHelper;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import java.io.FileNotFoundException;

public class BMobileUserInfoFragment extends SettingsPreferenceFragment {

    private static final String TAG = "BMobileUserInfoFragment";
    private static final int REQUEST_CODE_CONFIRM_CREDENTIAL = 1004;
    private boolean mIsAuthenticated = false;
    
    private ImageView iv;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Restore authentication state
        if (savedInstanceState != null) {
            mIsAuthenticated = savedInstanceState.getBoolean("is_authenticated", false);
        }
        
        // Only load preferences if authenticated or if no lock screen is set
        if (mIsAuthenticated || !isKeyguardSecure()) {
            addPreferencesFromResource(getPrefXmlResId());
            updateUserHubVisibility();
        }
        context = getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_authenticated", mIsAuthenticated);
    }

    private boolean isKeyguardSecure() {
        Context context = getContext();
        if (context == null) {
            return false;
        }
        KeyguardManager km = context.getSystemService(KeyguardManager.class);
        return km != null && km.isKeyguardSecure();
    }

    @Override
    public void onStart() {
        super.onStart();
        
        // Check if authentication is required
        if (!mIsAuthenticated) {
            checkAndRequestAuthentication();
            return;
        }
        
        // Collapse the app bar when fragment starts
        // mAppBarLayout access removed - handled by parent class
        onUserCard();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    protected String getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);
        return account != null ? account.name : null;
    }

    protected Account getAccount(AccountManager accountManager) {
        // Prefer Google, then Outlook, Yahoo, Proton, else first available.
        Account[] google = accountManager.getAccountsByType("com.google");
        if (google != null && google.length > 0) return google[0];

        Account[] outlook = accountManager.getAccountsByType("com.microsoft.office.outlook");
        if (outlook != null && outlook.length > 0) return outlook[0];

        Account[] yahoo = accountManager.getAccountsByType("com.yahoo.mobile.client.android.mail.acct");
        if (yahoo != null && yahoo.length > 0) return yahoo[0];

        Account[] proton = accountManager.getAccountsByType("ch.protonmail.android");
        if (proton != null && proton.length > 0) return proton[0];

        Account[] any = accountManager.getAccounts();
        return (any != null && any.length > 0) ? any[0] : null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_CONFIRM_CREDENTIAL) {
            if (resultCode == Activity.RESULT_OK) {
                mIsAuthenticated = true;
                // Load preferences if not already loaded
                if (getPreferenceScreen() == null) {
                    addPreferencesFromResource(getPrefXmlResId());
                }
                updateUserHubVisibility();
                // Now proceed with normal fragment initialization
                // mAppBarLayout access removed - handled by parent class
                onUserCard();
            } else {
                // Authentication failed or cancelled - finish the activity
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
            return;
        }
        
        if (resultCode == RESULT_OK && data != null && data.getData() != null && requestCode == 100) {
            String path = data.getData().toString();
            try {
                iv.setImageBitmap(BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(Uri.parse(path))));
            } catch (FileNotFoundException e) {
                iv.setImageResource(R.drawable.user_png);
            }
            context.getSharedPreferences(getImagePrefName(), Context.MODE_PRIVATE)
                    .edit().putString(getImagePrefKey(), path).commit();
        }
    }

    private void checkAndRequestAuthentication() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        KeyguardManager km = context.getSystemService(KeyguardManager.class);
        if (km == null || !km.isKeyguardSecure()) {
            // No lock screen set up - allow access without authentication
            mIsAuthenticated = true;
            // Load preferences if not already loaded
            if (getPreferenceScreen() == null) {
                addPreferencesFromResource(getPrefXmlResId());
            }
            updateUserHubVisibility();
            // Proceed with normal initialization
            // mAppBarLayout access removed - handled by parent class
            onUserCard();
            return;
        }

        // Request device credential confirmation
        Intent intent = new Intent();
        intent.setClassName("com.android.settings",
                ConfirmDeviceCredentialActivity.class.getName());
        intent.putExtra(KeyguardManager.EXTRA_TITLE,
                context.getString(R.string.bmobile_userinfo_confirm_credential_title));
        intent.putExtra(KeyguardManager.EXTRA_DESCRIPTION,
                context.getString(R.string.bmobile_userinfo_confirm_credential_description));
        intent.putExtra(KeyguardManager.EXTRA_DISALLOW_BIOMETRICS_IF_POLICY_EXISTS, false);

        try {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_CREDENTIAL);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch credential confirmation", e);
            // If we can't launch auth, allow access (fallback)
            mIsAuthenticated = true;
            // Load preferences if not already loaded
            if (getPreferenceScreen() == null) {
                addPreferencesFromResource(getPrefXmlResId());
            }
            updateUserHubVisibility();
            // mAppBarLayout access removed - handled by parent class
            onUserCard();
        }
    }

    /** Parent hub (Atomichub2) is only reachable from the Parent (style 0) dashboard. */
    private void updateUserHubVisibility() {
        Preference hub = findPreference("bmobile_action_hub");
        if (hub == null) {
            return;
        }
        Context ctx = getContext();
        if (ctx == null) {
            hub.setVisible(false);
            return;
        }
        hub.setVisible(DashboardStyleHelper.usesUserInfoActionHub(
                DashboardStyleHelper.getDashboardStyle(ctx)));
    }

    private void onUserCard() {
        final LayoutPreference headerPreference =
                (LayoutPreference) getPreferenceScreen().findPreference(getUserCardKey());
        final View userCard = headerPreference.findViewById(R.id.entity_header);
        final TextView useremail = headerPreference.findViewById(R.id.email);
        String email = getEmail(getContext());
        useremail.setText(email != null ? email : "Add a Google account to show email");
        iv = headerPreference.findViewById(R.id.image_holder);
        Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
        chooser.setType("image/*");
        iv.setOnClickListener(v -> startActivityForResult(chooser, 100));

        final Activity activity = getActivity();
        final Bundle bundle = getArguments();

        Drawable chosenDrawable = null;
        String path = activity.getSharedPreferences(getImagePrefName(), Context.MODE_PRIVATE)
                .getString(getImagePrefKey(), "");
        if (!path.isEmpty()) {
            try {
                Bitmap bmp = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(Uri.parse(path)));
                if (bmp != null) {
                    iv.setImageBitmap(bmp);
                    chosenDrawable = new BitmapDrawable(getResources(), bmp);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            iv.setImageResource(R.drawable.user_png);
        }

        final EntityHeaderController controller = EntityHeaderController
                .newInstance(activity, this, userCard)
                .setButtonActions(EntityHeaderController.ActionType.ACTION_NONE,
                        EntityHeaderController.ActionType.ACTION_NONE);

        // Use chosen avatar if available, else fallback to default.
        if (chosenDrawable != null) {
            controller.setIcon(chosenDrawable);
        } else {
            controller.setIcon(activity.getDrawable(R.drawable.user_png));
        }

        final UserManager userManager = (UserManager) getActivity().getSystemService(
                Context.USER_SERVICE);
        controller.setLabel(Utils.getExistingUser(userManager,
                android.os.Process.myUserHandle()).name);

        controller.done(true);
    }

    protected String getUserCardKey() {
        return "bmobile_user_header";
    }

    protected int getPrefXmlResId() {
        return R.xml.bmobile_userinfo_pref;
    }

    protected String getImagePrefName() {
        return "shared_user_image_path";
    }

    protected String getImagePrefKey() {
        return "image_path";
    }

    @Override
    public boolean onPreferenceTreeClick(androidx.preference.Preference preference) {
        String key = preference.getKey();
        android.util.Log.d("BMobileUserInfoFragment", "onPreferenceTreeClick called for key: " + key);

        // Handle private space preferences
        if ("private_space_access".equals(key) || "private_space_security".equals(key)) {
            android.util.Log.d("BMobileUserInfoFragment", "Launching Private Space Setup Activity");

            try {
                // Launch Private Space Setup Activity directly using Intent
                android.content.Intent intent = new android.content.Intent();
                intent.setClassName("com.android.settings", "com.android.settings.privatespace.PrivateSpaceSetupActivity");
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);

                android.util.Log.d("BMobileUserInfoFragment", "Private Space Setup Activity launched successfully");
                return true;
            } catch (Exception e) {
                android.util.Log.e("BMobileUserInfoFragment", "Failed to launch Private Space Setup Activity", e);
                android.widget.Toast.makeText(getContext(), "Unable to open Private Space", android.widget.Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return super.onPreferenceTreeClick(preference);
    }
}

