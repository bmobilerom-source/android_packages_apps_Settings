/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.android.settings.display;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.settings.SettingsEnums;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class TimeBasedColorsSettings extends DashboardFragment {
    private static final String TAG = "TimeBasedColorsSettings";

    // Time slots for different color themes
    private static final TimeSlot[] TIME_SLOTS = {
        new TimeSlot("dawn", "Dawn (6:00-9:00)", 6, 0, 9, 0,
            new int[]{Color.parseColor("#FFF8E1"), Color.parseColor("#FFECB3"),
                     Color.parseColor("#FFE082"), Color.parseColor("#FFD54F")}),
        new TimeSlot("morning", "Morning (9:00-12:00)", 9, 0, 12, 0,
            new int[]{Color.parseColor("#E3F2FD"), Color.parseColor("#BBDEFB"),
                     Color.parseColor("#90CAF9"), Color.parseColor("#64B5F6")}),
        new TimeSlot("afternoon", "Afternoon (12:00-17:00)", 12, 0, 17, 0,
            new int[]{Color.parseColor("#FFF3E0"), Color.parseColor("#FFE0B2"),
                     Color.parseColor("#FFCC80"), Color.parseColor("#FFB74D")}),
        new TimeSlot("evening", "Evening (17:00-21:00)", 17, 0, 21, 0,
            new int[]{Color.parseColor("#FCE4EC"), Color.parseColor("#F8BBD9"),
                     Color.parseColor("#F48FB1"), Color.parseColor("#F06292")}),
        new TimeSlot("night", "Night (21:00-6:00)", 21, 0, 6, 0,
            new int[]{Color.parseColor("#0D1B2A"), Color.parseColor("#1B2631"),
                     Color.parseColor("#243447"), Color.parseColor("#2D4A5D")})
    };

    private SwitchPreferenceCompat mEnablePreference;
    private AlarmManager mAlarmManager;
    private BroadcastReceiver mTimeChangeReceiver;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.time_based_colors_settings;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mAlarmManager = getContext().getSystemService(AlarmManager.class);

        // Register receiver for time changes
        mTimeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction()) ||
                    Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                    updateCurrentTimeSlot();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(mTimeChangeReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimeChangeReceiver != null) {
            getContext().unregisterReceiver(mTimeChangeReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCurrentTimeSlot();
        scheduleNextColorChange();
    }

    private void updateCurrentTimeSlot() {
        boolean enabled = android.provider.Settings.Secure.getInt(
            getContext().getContentResolver(),
            android.provider.Settings.Secure.MONET_TIME_BASED_ENABLED, 0) == 1;

        if (!enabled) return;

        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        TimeSlot currentSlot = getCurrentTimeSlot(currentHour, currentMinute);
        if (currentSlot != null) {
            applyTimeSlotColors(currentSlot);
        }
    }

    private TimeSlot getCurrentTimeSlot(int hour, int minute) {
        int currentMinutes = hour * 60 + minute;

        for (TimeSlot slot : TIME_SLOTS) {
            int startMinutes = slot.startHour * 60 + slot.startMinute;
            int endMinutes = slot.endHour * 60 + slot.endMinute;

            // Handle overnight slots (night time)
            if (endMinutes < startMinutes) {
                if (currentMinutes >= startMinutes || currentMinutes <= endMinutes) {
                    return slot;
                }
            } else {
                if (currentMinutes >= startMinutes && currentMinutes <= endMinutes) {
                    return slot;
                }
            }
        }
        return TIME_SLOTS[4]; // Default to night
    }

    private void applyTimeSlotColors(TimeSlot slot) {
        // Save current time slot
        android.provider.Settings.Secure.putString(
            getContext().getContentResolver(),
            android.provider.Settings.Secure.MONET_CURRENT_TIME_SLOT,
            slot.id);

        // Apply colors (this would need framework implementation)
        // For now, just save the preference
        android.provider.Settings.Secure.putString(
            getContext().getContentResolver(),
            android.provider.Settings.Secure.MONET_TIME_SLOT_COLORS,
            slot.colors[0] + "," + slot.colors[1] + "," + slot.colors[2] + "," + slot.colors[3]);
    }

    private void scheduleNextColorChange() {
        boolean enabled = android.provider.Settings.Secure.getInt(
            getContext().getContentResolver(),
            android.provider.Settings.Secure.MONET_TIME_BASED_ENABLED, 0) == 1;

        if (!enabled) return;

        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        // Find next time slot change
        TimeSlot currentSlot = getCurrentTimeSlot(currentHour, currentMinute);
        TimeSlot nextSlot = getNextTimeSlot(currentSlot);

        if (nextSlot != null) {
            Calendar nextChange = Calendar.getInstance();
            nextChange.set(Calendar.HOUR_OF_DAY, nextSlot.startHour);
            nextChange.set(Calendar.MINUTE, nextSlot.startMinute);
            nextChange.set(Calendar.SECOND, 0);

            // If next slot is tomorrow, add a day
            if (nextChange.before(now)) {
                nextChange.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Schedule alarm
            Intent intent = new Intent("com.android.settings.MONET_TIME_CHANGE");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, nextChange.getTimeInMillis(), pendingIntent);
        }
    }

    private TimeSlot getNextTimeSlot(TimeSlot current) {
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            if (TIME_SLOTS[i].id.equals(current.id)) {
                return TIME_SLOTS[(i + 1) % TIME_SLOTS.length];
            }
        }
        return TIME_SLOTS[0];
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_display;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new TimeBasedColorsController(context, "monet_time_based_enabled"));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.time_based_colors_settings) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null);
                }
            };

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        return "time_based_colors_settings";
    }

    private static class TimeSlot {
        final String id;
        final String name;
        final int startHour;
        final int startMinute;
        final int endHour;
        final int endMinute;
        final int[] colors;

        TimeSlot(String id, String name, int startHour, int startMinute,
                int endHour, int endMinute, int[] colors) {
            this.id = id;
            this.name = name;
            this.startHour = startHour;
            this.startMinute = startMinute;
            this.endHour = endHour;
            this.endMinute = endMinute;
            this.colors = colors;
        }
    }
}
