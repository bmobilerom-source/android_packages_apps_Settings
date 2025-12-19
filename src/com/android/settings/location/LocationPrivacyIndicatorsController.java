/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.settings.location;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.UserHandle;
import android.os.UserManager;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.applications.RecentAppOpsAccess;

import java.util.List;

/**
 * Controller for location privacy indicators preference.
 * Shows count of apps with location access and recent usage.
 */
public class LocationPrivacyIndicatorsController extends BasePreferenceController {

    private RecentAppOpsAccess mRecentLocationApps;

    public LocationPrivacyIndicatorsController(Context context, String key) {
        super(context, key);
        mRecentLocationApps = RecentAppOpsAccess.createForLocation(context);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        // Get count of apps with location access
        List<RecentAppOpsAccess.Access> recentAccesses = mRecentLocationApps.getAppListSorted(false);
        int appCount = recentAccesses.size();

        // Count recent accesses (last hour)
        long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
        int recentCount = 0;
        for (RecentAppOpsAccess.Access access : recentAccesses) {
            if (access.accessFinishTime > oneHourAgo) {
                recentCount++;
            }
        }

        // Calculate privacy score (simplified)
        int privacyScore = calculatePrivacyScore(appCount, recentCount);

        // Update preference
        preference.setTitle(mContext.getString(R.string.location_apps_with_access, appCount));
        preference.setSummary(mContext.getString(R.string.location_recent_access_count, recentCount));
    }

    private int calculatePrivacyScore(int appCount, int recentCount) {
        // Simplified privacy scoring algorithm
        int baseScore = 100;

        // Deduct points for each app with location access
        baseScore -= (appCount * 5);

        // Deduct points for recent access frequency
        baseScore -= (recentCount * 10);

        // Ensure score stays within bounds
        return Math.max(0, Math.min(100, baseScore));
    }
}
