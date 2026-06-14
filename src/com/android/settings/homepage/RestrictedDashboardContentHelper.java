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

package com.android.settings.homepage;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.bmobile.fragments.BMobileExpressiveSettingsAdapter;
import com.bmobile.fragments.FunDisplaySettingsAdapter;
import com.bmobile.fragments.MaterialDashboardGridAdapter;
import com.android.settings.kidssafe.KidsSafeGridAdapter;

import java.util.Iterator;
import java.util.List;

/**
 * Hides apps and privacy destinations on non-Parent dashboards (YR, KS, BMobile, …).
 * Parent (style 0) keeps full navigation and search.
 */
public final class RestrictedDashboardContentHelper {

    private static final int PARENT_STYLE = 0;

    private static final String[] BLOCKED_CLASS_PREFIXES = {
            "com.android.settings.applications.",
            "com.android.settings.security.",
            "com.android.settings.privacy.",
            "com.android.settings.safetycenter.",
            "com.android.settings.spa.app.",
    };

    private static final String[] BLOCKED_EXACT_CLASSES = {
            "com.bmobile.fragments.SystemApplicationSettings",
            "com.android.settings.applications.ManageApplications",
            "com.android.settings.applications.appinfo.AppInfoDashboardFragment",
            "com.android.settings.applications.specialaccess.AppSecSettings",
            "com.android.settings.applications.AppDashboardFragment",
            "com.android.settings.privacy.PrivacyDashboardFragment",
            "com.android.settings.privacy.PrivacyControlsFragment",
    };

    /** Homepage preference keys removed on restricted dashboards. */
    private static final String[] HIDDEN_PREFERENCE_KEYS = {
            "top_level_apps",
            "top_level_privacy",
            "top_level_privacy_controls",
    };

    private RestrictedDashboardContentHelper() {}

    /** True for every dashboard except Parent (style 0). */
    public static boolean isContentRestricted(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        return DashboardStyleHelper.getDashboardStyle(context) != PARENT_STYLE;
    }

    public static boolean isBlockedDestination(@Nullable String className) {
        if (TextUtils.isEmpty(className)) {
            return false;
        }
        for (String prefix : BLOCKED_CLASS_PREFIXES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        for (String exact : BLOCKED_EXACT_CLASSES) {
            if (className.equals(exact)) {
                return true;
            }
        }
        return false;
    }

    public static void removeHiddenHomepagePreferences(@Nullable PreferenceScreen screen,
            @Nullable Context context) {
        if (screen == null || !isContentRestricted(context)) {
            return;
        }
        for (String key : HIDDEN_PREFERENCE_KEYS) {
            final Preference pref = screen.findPreference(key);
            if (pref != null) {
                screen.removePreference(pref);
            }
        }
    }

    public static void filterExpressiveGridItems(@Nullable Context context,
            @Nullable List<BMobileExpressiveSettingsAdapter.CardItem> items) {
        filterDestinations(context, items);
    }

    public static void filterMaterialGridItems(@Nullable Context context,
            @Nullable List<MaterialDashboardGridAdapter.CardItem> items) {
        filterDestinations(context, items);
    }

    public static void filterFunDisplayGridItems(@Nullable Context context,
            @Nullable List<FunDisplaySettingsAdapter.CardItem> items) {
        filterDestinations(context, items);
    }

    public static void filterKidsSafeGridItems(@Nullable Context context,
            @Nullable List<KidsSafeGridAdapter.CardItem> items) {
        filterDestinations(context, items);
    }

    private static <T> void filterDestinations(@Nullable Context context, @Nullable List<T> items) {
        if (items == null || !isContentRestricted(context)) {
            return;
        }
        final Iterator<T> it = items.iterator();
        while (it.hasNext()) {
            final T item = it.next();
            final String dest = getDestinationFragment(item);
            if (isBlockedDestination(dest)) {
                it.remove();
            }
        }
    }

    @Nullable
    private static <T> String getDestinationFragment(@NonNull T item) {
        if (item instanceof BMobileExpressiveSettingsAdapter.CardItem) {
            return ((BMobileExpressiveSettingsAdapter.CardItem) item).destFragment;
        }
        if (item instanceof MaterialDashboardGridAdapter.CardItem) {
            return ((MaterialDashboardGridAdapter.CardItem) item).destFragment;
        }
        if (item instanceof FunDisplaySettingsAdapter.CardItem) {
            return ((FunDisplaySettingsAdapter.CardItem) item).destFragment;
        }
        if (item instanceof KidsSafeGridAdapter.CardItem) {
            return ((KidsSafeGridAdapter.CardItem) item).destFragment;
        }
        return null;
    }
}
