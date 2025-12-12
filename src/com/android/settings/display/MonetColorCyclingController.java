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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.BasePreferenceController;

public class MonetColorCyclingController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String ACTION_COLOR_CYCLE = "com.android.settings.action.COLOR_CYCLE";
    private static final int CYCLE_INTERVAL_MINUTES = 1; // 1 minute
    private static final int CYCLE_INTERVAL_MS = CYCLE_INTERVAL_MINUTES * 60 * 1000;

    // Ultra-dark colors to cycle through
    private static final int[] DARK_COLORS = {
        0xFF000000, // Jet Black
        0xFF000051, // Midnight Blue
        0xFF8B0000, // Blood Red
        0xFF004D40, // Evergreen
        0xFF263238  // Slate Gray
    };

    private AlarmManager mAlarmManager;
    private PendingIntent mCycleIntent;
    private Handler mHandler;
    private int mCurrentColorIndex = 0;
    private boolean mIsReceiverRegistered = false;

    private final BroadcastReceiver mColorCycleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            android.util.Log.d("MonetColorCyclingController", "Broadcast received: " + intent.getAction());
            if (ACTION_COLOR_CYCLE.equals(intent.getAction())) {
                android.util.Log.d("MonetColorCyclingController", "Processing color cycle action");
                cycleToNextColor();
            }
        }
    };

    public MonetColorCyclingController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());

        // Create the pending intent for color cycling
        Intent cycleIntent = new Intent(ACTION_COLOR_CYCLE);
        cycleIntent.setPackage(context.getPackageName()); // Ensure it goes to our app
        mCycleIntent = PendingIntent.getBroadcast(context, 0, cycleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Check if cycling should be active and start it if needed
        boolean isEnabled = Settings.System.getInt(mContext.getContentResolver(),
                "monet_color_cycling", 0) == 1;
        if (isEnabled) {
            android.util.Log.d("MonetColorCyclingController", "Cycling was enabled, restarting...");
            startColorCycling();
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        android.util.Log.d("MonetColorCyclingController", "updateState called for preference: " + preference.getKey());
        super.updateState(preference);
        if (preference instanceof SwitchPreference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            boolean isEnabled = Settings.System.getInt(mContext.getContentResolver(),
                    "monet_color_cycling", 0) == 1;
            switchPreference.setChecked(isEnabled);
            android.util.Log.d("MonetColorCyclingController", "Switch state set to: " + isEnabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isEnabled = (Boolean) newValue;
        android.util.Log.d("MonetColorCyclingController", "onPreferenceChange called: isEnabled = " + isEnabled);

        if (isEnabled) {
            startColorCycling();
        } else {
            stopColorCycling();
        }

        boolean result = Settings.System.putInt(mContext.getContentResolver(),
                "monet_color_cycling", isEnabled ? 1 : 0);
        android.util.Log.d("MonetColorCyclingController", "Settings saved: " + result);
        return result;
    }

    private void startColorCycling() {
        android.util.Log.d("MonetColorCyclingController", "Starting color cycling");

        try {
            // Register the broadcast receiver if not already registered
            if (!mIsReceiverRegistered) {
                IntentFilter filter = new IntentFilter(ACTION_COLOR_CYCLE);
                mContext.registerReceiver(mColorCycleReceiver, filter, Context.RECEIVER_EXPORTED);
                mIsReceiverRegistered = true;
                android.util.Log.d("MonetColorCyclingController", "Broadcast receiver registered");
            }

            // Start with first color immediately
            mCurrentColorIndex = 0;
            applyCurrentColor();

            // Schedule repeating alarm using setExactAndAllowWhileIdle for better reliability
            long nextTriggerTime = System.currentTimeMillis() + CYCLE_INTERVAL_MS;

            // Cancel any existing alarm first
            mAlarmManager.cancel(mCycleIntent);

            // Use setExactAndAllowWhileIdle for Android 12+ compatibility
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, mCycleIntent);
            } else {
                mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTriggerTime, mCycleIntent);
            }

            android.util.Log.d("MonetColorCyclingController", "Alarm scheduled for " + nextTriggerTime);

            // Schedule the next alarm to create repeating behavior
            scheduleNextAlarm();

        } catch (Exception e) {
            android.util.Log.e("MonetColorCyclingController", "Failed to start color cycling", e);
        }
    }

    private void scheduleNextAlarm() {
        try {
            long nextTriggerTime = System.currentTimeMillis() + CYCLE_INTERVAL_MS;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, mCycleIntent);
            } else {
                mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTriggerTime, mCycleIntent);
            }
        } catch (Exception e) {
            android.util.Log.e("MonetColorCyclingController", "Failed to schedule next alarm", e);
        }
    }

    private void stopColorCycling() {
        android.util.Log.d("MonetColorCyclingController", "Stopping color cycling");

        // Cancel the alarm
        if (mAlarmManager != null && mCycleIntent != null) {
            mAlarmManager.cancel(mCycleIntent);
            android.util.Log.d("MonetColorCyclingController", "Alarm cancelled");
        }

        // Unregister the receiver
        if (mIsReceiverRegistered) {
            try {
                mContext.unregisterReceiver(mColorCycleReceiver);
                mIsReceiverRegistered = false;
                android.util.Log.d("MonetColorCyclingController", "Broadcast receiver unregistered");
            } catch (IllegalArgumentException e) {
                android.util.Log.w("MonetColorCyclingController", "Receiver was not registered or already unregistered");
            }
        }
    }

    private void cycleToNextColor() {
        mCurrentColorIndex = (mCurrentColorIndex + 1) % DARK_COLORS.length;
        android.util.Log.d("MonetColorCyclingController", "Cycling to next color: index " + mCurrentColorIndex + ", color: " + String.format("0x%08X", DARK_COLORS[mCurrentColorIndex]));
        applyCurrentColor();

        // Schedule the next alarm for repeating behavior
        scheduleNextAlarm();
    }

    private void applyCurrentColor() {
        int color = DARK_COLORS[mCurrentColorIndex];
        android.util.Log.d("MonetColorCyclingController", "Applying color: " + String.format("0x%08X", color));

        try {
            // Apply the color using the same method as presets
            boolean colorSaved = Settings.System.putInt(mContext.getContentResolver(),
                    "monet_seed_color", color);
            boolean presetSaved = Settings.System.putString(mContext.getContentResolver(),
                    "monet_color_preset", "cycling_" + mCurrentColorIndex);
            boolean modeSaved = Settings.System.putInt(mContext.getContentResolver(),
                    "monet_preset_enabled", 1);

            android.util.Log.d("MonetColorCyclingController", "Settings saved - color: " + colorSaved + ", preset: " + presetSaved + ", mode: " + modeSaved);

            // Trigger theme refresh with multiple broadcasts for reliability
            Intent[] intents = {
                new Intent("android.intent.action.WALLPAPER_CHANGED"),
                new Intent("android.intent.action.CONFIGURATION_CHANGED"),
                new Intent("android.intent.action.THEME_CHANGED")
            };

            for (Intent intent : intents) {
                intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
                try {
                    mContext.sendBroadcast(intent);
                    android.util.Log.d("MonetColorCyclingController", "Broadcast sent: " + intent.getAction());
                } catch (Exception e) {
                    android.util.Log.w("MonetColorCyclingController", "Failed to send broadcast: " + intent.getAction(), e);
                }
            }

        } catch (Exception e) {
            android.util.Log.e("MonetColorCyclingController", "Failed to apply current color", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        // Clean up resources
        stopColorCycling();
        super.finalize();
    }
}
