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
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;

import java.util.Calendar;

/** Applies and schedules time-based Monet color slots. */
public final class MonetTimeBasedColorHelper {
    public static final String ACTION_MONET_TIME_CHANGE =
            "com.android.settings.MONET_TIME_CHANGE";

    private static final TimeSlot[] TIME_SLOTS = {
        new TimeSlot("dawn", 6, 0, 9, 0,
            new int[]{Color.parseColor("#FFF8E1"), Color.parseColor("#FFECB3"),
                     Color.parseColor("#FFE082"), Color.parseColor("#FFD54F")}),
        new TimeSlot("morning", 9, 0, 12, 0,
            new int[]{Color.parseColor("#E3F2FD"), Color.parseColor("#BBDEFB"),
                     Color.parseColor("#90CAF9"), Color.parseColor("#64B5F6")}),
        new TimeSlot("afternoon", 12, 0, 17, 0,
            new int[]{Color.parseColor("#FFF3E0"), Color.parseColor("#FFE0B2"),
                     Color.parseColor("#FFCC80"), Color.parseColor("#FFB74D")}),
        new TimeSlot("evening", 17, 0, 21, 0,
            new int[]{Color.parseColor("#FCE4EC"), Color.parseColor("#F8BBD9"),
                     Color.parseColor("#F48FB1"), Color.parseColor("#F06292")}),
        new TimeSlot("night", 21, 0, 6, 0,
            new int[]{Color.parseColor("#0D1B2A"), Color.parseColor("#1B2631"),
                     Color.parseColor("#243447"), Color.parseColor("#2D4A5D")})
    };

    private MonetTimeBasedColorHelper() {
    }

    public static void applyCurrentTimeSlot(Context context) {
        if (!isEnabled(context)) {
            return;
        }

        Calendar now = Calendar.getInstance();
        TimeSlot slot = getCurrentTimeSlot(
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
        if (slot != null) {
            applyTimeSlot(context, slot);
        }
    }

    public static void scheduleNextColorChange(Context context) {
        if (!isEnabled(context)) {
            return;
        }

        Calendar now = Calendar.getInstance();
        TimeSlot currentSlot = getCurrentTimeSlot(
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
        TimeSlot nextSlot = getNextTimeSlot(currentSlot);
        if (nextSlot == null) {
            return;
        }

        Calendar nextChange = Calendar.getInstance();
        nextChange.set(Calendar.HOUR_OF_DAY, nextSlot.startHour);
        nextChange.set(Calendar.MINUTE, nextSlot.startMinute);
        nextChange.set(Calendar.SECOND, 0);
        nextChange.set(Calendar.MILLISECOND, 0);

        if (nextChange.before(now)) {
            nextChange.add(Calendar.DAY_OF_MONTH, 1);
        }

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, new Intent(ACTION_MONET_TIME_CHANGE).setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, nextChange.getTimeInMillis(), pendingIntent);
    }

    public static void cancelScheduledChanges(Context context) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, new Intent(ACTION_MONET_TIME_CHANGE).setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private static boolean isEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.MONET_TIME_BASED_ENABLED, 0) == 1;
    }

    private static void applyTimeSlot(Context context, TimeSlot slot) {
        Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.MONET_CURRENT_TIME_SLOT, slot.id);
        Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.MONET_TIME_SLOT_COLORS,
                slot.colors[0] + "," + slot.colors[1] + "," + slot.colors[2]
                        + "," + slot.colors[3]);

        String style = MonetThemeApplier.getCurrentStyle(context);
        MonetThemeApplier.applyPreset(context, slot.colors[0], style,
                Math.abs(slot.id.hashCode()) % 1000);
    }

    private static TimeSlot getCurrentTimeSlot(int hour, int minute) {
        int currentMinutes = hour * 60 + minute;

        for (TimeSlot slot : TIME_SLOTS) {
            int startMinutes = slot.startHour * 60 + slot.startMinute;
            int endMinutes = slot.endHour * 60 + slot.endMinute;

            if (endMinutes < startMinutes) {
                if (currentMinutes >= startMinutes || currentMinutes <= endMinutes) {
                    return slot;
                }
            } else if (currentMinutes >= startMinutes && currentMinutes <= endMinutes) {
                return slot;
            }
        }
        return TIME_SLOTS[4];
    }

    private static TimeSlot getNextTimeSlot(TimeSlot current) {
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            if (TIME_SLOTS[i].id.equals(current.id)) {
                return TIME_SLOTS[(i + 1) % TIME_SLOTS.length];
            }
        }
        return TIME_SLOTS[0];
    }

    private static final class TimeSlot {
        final String id;
        final int startHour;
        final int startMinute;
        final int endHour;
        final int endMinute;
        final int[] colors;

        TimeSlot(String id, int startHour, int startMinute, int endHour, int endMinute,
                int[] colors) {
            this.id = id;
            this.startHour = startHour;
            this.startMinute = startMinute;
            this.endHour = endHour;
            this.endMinute = endMinute;
            this.colors = colors;
        }
    }
}
