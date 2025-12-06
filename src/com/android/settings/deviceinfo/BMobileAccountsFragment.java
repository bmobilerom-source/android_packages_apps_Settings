package com.android.settings.deviceinfo;

import com.android.settings.R;
import android.accounts.Account;
import android.accounts.AccountManager;

/**
 * Accounts variant of the bMobile user info fragment.
 */
public class BMobileAccountsFragment extends BMobileUserInfoFragment {

    @Override
    protected int getPrefXmlResId() {
        return R.xml.bmobile_accounts_pref;
    }

    @Override
    protected String getUserCardKey() {
        return "bmobile_accounts_header";
    }

    @Override
    protected String getImagePrefName() {
        return "bmobile_accounts_image_path";
    }

    @Override
    protected String getImagePrefKey() {
        return "image_path";
    }

    @Override
    protected String getEmail(android.content.Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getPrivacyPreferredAccount(accountManager);
        return account != null ? account.name : null;
    }

    /**
     * Privacy-focused selection: prefer non-Google providers first.
     */
    private static Account getPrivacyPreferredAccount(AccountManager accountManager) {
        Account[] proton = accountManager.getAccountsByType("ch.protonmail.android");
        if (proton != null && proton.length > 0) return proton[0];

        Account[] outlook = accountManager.getAccountsByType("com.microsoft.office.outlook");
        if (outlook != null && outlook.length > 0) return outlook[0];

        Account[] yahoo = accountManager.getAccountsByType("com.yahoo.mobile.client.android.mail.acct");
        if (yahoo != null && yahoo.length > 0) return yahoo[0];

        // Fallback to Google only if no privacy-focused account is present.
        Account[] google = accountManager.getAccountsByType("com.google");
        if (google != null && google.length > 0) return google[0];

        Account[] any = accountManager.getAccounts();
        return (any != null && any.length > 0) ? any[0] : null;
    }
}

