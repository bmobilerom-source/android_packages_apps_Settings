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

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.kidssafe.KidsSafeGridAdapter;

import java.util.ArrayList;
import java.util.List;

/** Grid wiring for KS Fun dashboard style (16). */
public final class KsFunDashboardHelper {

    private static final String TAG = "KsFunDashboardHelper";

    public static final int STYLE = 16;
    public static final String GRID_PREF_KEY = "ks_fun_expressive_grid";

    private KsFunDashboardHelper() {
    }

    public static void populateGridItems(List<KidsSafeGridAdapter.CardItem> items) {
        final int type = KidsSafeGridAdapter.CARD_TYPE_STANDARD;

        items.add(new KidsSafeGridAdapter.CardItem(type,
                R.string.sound_settings,
                R.string.sound_dashboard_summary_with_dnd,
                "com.android.settings.notification.SoundSettings",
                R.drawable.ic_volume_up_filled));

        items.add(new KidsSafeGridAdapter.CardItem(type,
                R.string.display_page_grid_title,
                R.string.display_page_grid_summary,
                "com.bmobile.fragments.KsDisplayPageGrid",
                R.drawable.ic_settings_display_filled));

        items.add(new KidsSafeGridAdapter.CardItem(type,
                R.string.privacy_dashboard_title,
                R.string.privacy_dashboard_summary,
                "com.android.settings.privacy.PrivacyControlsFragment",
                R.drawable.ic_settings_privacy_filled));

        items.add(new KidsSafeGridAdapter.CardItem(type,
                R.string.location_settings_title,
                R.string.location_settings_loading_app_permission_stats,
                "com.android.settings.location.LocationSettings",
                R.drawable.ic_settings_location_filled));
    }

    public static void setupGrid(RecyclerView rv, Activity activity, Context context,
            int sourceMetrics) {
        if (rv == null || activity == null || context == null) {
            return;
        }

        try {
            GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
            rv.setLayoutManager(layoutManager);

            List<KidsSafeGridAdapter.CardItem> items = new ArrayList<>();
            populateGridItems(items);
            RestrictedDashboardContentHelper.filterKidsSafeGridItems(context, items);

            rv.setAdapter(new KidsSafeGridAdapter(activity, items, sourceMetrics));
            Log.d(TAG, "KS Fun grid setup completed with " + items.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up KS Fun grid", e);
        }
    }
}
