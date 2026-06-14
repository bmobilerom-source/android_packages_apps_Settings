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

package com.android.settings.search;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.homepage.RestrictedDashboardContentHelper;

/**
 * Restricts Settings search on non-Parent dashboards so users cannot reach apps, privacy,
 * or security pages via the search bar.
 *
 * Keep in sync with SettingsIntelligence ChildSafeSearchHelper.
 */
public final class ChildSafeSearchHelper {

    private ChildSafeSearchHelper() {}

    public static boolean isSearchRestricted(@Nullable Context context) {
        return RestrictedDashboardContentHelper.isContentRestricted(context);
    }

    public static boolean isBlockedSearchTarget(@Nullable String className) {
        return RestrictedDashboardContentHelper.isBlockedDestination(className);
    }

    public static boolean shouldSuppressIndexable(@NonNull Context context,
            @Nullable String className) {
        return isSearchRestricted(context) && isBlockedSearchTarget(className);
    }
}
