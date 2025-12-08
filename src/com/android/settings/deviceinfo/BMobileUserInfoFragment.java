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
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.SettingsBaseActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.widget.LayoutPreference;

import androidx.preference.PreferenceScreen;

import java.io.FileNotFoundException;

public class BMobileUserInfoFragment extends SettingsPreferenceFragment {

    private ImageView iv;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(getPrefXmlResId());
        context = getActivity();
        ((SettingsBaseActivity) getActivity()).mAppBarLayout.setExpanded(false);
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
        if (resultCode == RESULT_OK && data != null && data.getData() != null && requestCode == 100) {
            String path = data.getData().toString();
            try {
                iv.setImageBitmap(BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(Uri.parse(path))));
            } catch (FileNotFoundException e) {
                iv.setImageResource(R.drawable.user);
            }
            context.getSharedPreferences("bmobile_image_path", Context.MODE_PRIVATE)
                    .edit().putString("image_path", path).commit();
        }
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

        String path = activity.getSharedPreferences(getImagePrefName(), Context.MODE_PRIVATE)
                .getString(getImagePrefKey(), "");
        if (!path.isEmpty()) {
            try {
                iv.setImageBitmap(BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(Uri.parse(path))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        final EntityHeaderController controller = EntityHeaderController
                .newInstance(activity, this, userCard)
                .setButtonActions(EntityHeaderController.ActionType.ACTION_NONE,
                        EntityHeaderController.ActionType.ACTION_NONE);

        // Force a stable default avatar to match design expectations.
        controller.setIcon(activity.getDrawable(R.drawable.user_png));

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
        return "bmobile_image_path";
    }

    protected String getImagePrefKey() {
        return "image_path";
    }
}

