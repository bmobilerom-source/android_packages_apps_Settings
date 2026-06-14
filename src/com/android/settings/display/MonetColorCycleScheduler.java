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
import android.provider.Settings;

/** Schedules repeating Monet color cycle alarms. */
public final class MonetColorCycleScheduler {
    private static final int CYCLE_INTERVAL_MS = 60_000;

    private MonetColorCycleScheduler() {
    }

    public static void start(Context context) {
        Settings.Secure.putInt(context.getContentResolver(), "monet_color_cycle_index", 0);
        MonetColorCycleReceiver.applyNextCycle(context);
        scheduleNext(context);
    }

    public static void stop(Context context) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0,
                new Intent(MonetColorCycleReceiver.ACTION_COLOR_CYCLE)
                        .setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    static void scheduleNext(Context context) {
        if (Settings.Secure.getInt(context.getContentResolver(), "monet_color_cycling", 0) != 1) {
            return;
        }

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0,
                new Intent(MonetColorCycleReceiver.ACTION_COLOR_CYCLE)
                        .setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + CYCLE_INTERVAL_MS,
                pendingIntent);
    }
}
