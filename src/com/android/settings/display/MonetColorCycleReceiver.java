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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

/** Cycles Monet preset colors on a timer when color cycling is enabled. */
public class MonetColorCycleReceiver extends BroadcastReceiver {
    public static final String ACTION_COLOR_CYCLE = "com.android.settings.action.COLOR_CYCLE";

    private static final int CYCLE_INTERVAL_MS = 60_000;

    private static final int[] CYCLE_COLORS = {
        0xFF8921C2, 0xFFFE39A4, 0xFF00CED1, 0xFFFF5800, 0xFFFF0000,
        0xFF2F46FA, 0xFF55FC77, 0xFFFB7443, 0xFF00D61C, 0xFF0346F4,
        0xFFBF2ED5, 0xFF150390, 0xFFD99EB0, 0xFFC9A5C0, 0xFF375F47,
        0xFF93B285, 0xFFE040FB, 0xFF18FFFF, 0xFF69F0AE, 0xFFFFFF00
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_COLOR_CYCLE.equals(intent.getAction())) {
            return;
        }

        if (Settings.Secure.getInt(context.getContentResolver(), "monet_color_cycling", 0) != 1) {
            return;
        }

        applyNextCycle(context);
        MonetColorCycleScheduler.scheduleNext(context);
    }

    static void applyNextCycle(Context context) {
        int index = Settings.Secure.getInt(context.getContentResolver(),
                "monet_color_cycle_index", 0);
        int color = CYCLE_COLORS[index % CYCLE_COLORS.length];
        index = (index + 1) % CYCLE_COLORS.length;
        Settings.Secure.putInt(context.getContentResolver(), "monet_color_cycle_index", index);

        String style = MonetThemeApplier.getCurrentStyle(context);
        MonetThemeApplier.applyPreset(context, color, style, index);
    }
}
