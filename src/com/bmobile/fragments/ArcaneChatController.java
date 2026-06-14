/*
 * Copyright (C) 2025 LineageOS
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

package com.bmobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/** Shows Your chat when ArcaneChat (Delta Chat / {@code chat.delta.lite}) is installed. */
public class ArcaneChatController extends BasePreferenceController {

    private static final String TAG = "ArcaneChatController";

    /** Installed ArcaneChat package (vendor/bmobile/system/ArcaneChat). */
    public static final String PACKAGE_NAME = "chat.delta.lite";

    /**
     * MAIN/LAUNCHER alias in ArcaneChat; routes into the conversation list after app init.
     * Activity class names are inherited from Delta Chat upstream — package id is not Signal.
     */
    public static final String LAUNCHER_ACTIVITY =
            "org.thoughtcrime.securesms.RoutingActivity";

    /** @deprecated Use {@link #LAUNCHER_ACTIVITY} or {@link #launch(Context)}. */
    public static final String ACTIVITY_CLASS = LAUNCHER_ACTIVITY;

    public ArcaneChatController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return isInstalled(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!getPreferenceKey().equals(preference.getKey())) {
            return super.handlePreferenceTreeClick(preference);
        }
        launch(mContext);
        return true;
    }

    public static boolean isInstalled(@NonNull Context context) {
        try {
            context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /** Opens ArcaneChat via its launcher activity. */
    public static void launch(@Nullable Context context) {
        if (context == null) {
            return;
        }
        try {
            final PackageManager pm = context.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(PACKAGE_NAME);
            if (intent == null) {
                intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setClassName(PACKAGE_NAME, LAUNCHER_ACTIVITY);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Arcane chat", e);
            Toast.makeText(context, R.string.yr_your_chat_summary, Toast.LENGTH_SHORT).show();
        }
    }

    /** @deprecated Use {@link #launch(Context)}. */
    public static void launch(@Nullable Activity activity) {
        launch((Context) activity);
    }
}
