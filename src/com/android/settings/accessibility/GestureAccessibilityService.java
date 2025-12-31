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

package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * GestureAccessibilityService provides touch-free device control using motion gestures.
 * Monitors accelerometer and gyroscope sensors to detect specific motion patterns
 * and triggers corresponding system actions.
 */
public class GestureAccessibilityService extends AccessibilityService {

    private static final String TAG = "GestureAccessibilityService";

    // Sensor components
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private GestureDetector mGestureDetector;

    // Settings keys
    private static final String GESTUREFLOW_ENABLED = "gestureflow_enabled";

    // Sensor listener
    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (mGestureDetector == null) return;

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGestureDetector.processAccelerometerData(event.values);
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                mGestureDetector.processGyroscopeData(event.values);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "Sensor accuracy changed: " + sensor.getName() + " -> " + accuracy);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "GestureAccessibilityService created");

        // Initialize sensor manager and sensors
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Initialize gesture detector
        mGestureDetector = new GestureDetector(this);

        // Only register sensor listeners if gestures are enabled
        if (isGestureControlEnabled()) {
            registerSensorListeners();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "GestureAccessibilityService destroyed");

        // Unregister sensor listeners
        unregisterSensorListeners();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not used for gesture detection, but required by AccessibilityService
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "GestureAccessibilityService interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "GestureAccessibilityService connected");

        // Service is now active and can perform accessibility actions
        // Sensor listeners are already registered in onCreate
    }

    private void registerSensorListeners() {
        if (mSensorManager == null) {
            Log.e(TAG, "SensorManager is null, cannot register listeners");
            return;
        }

        // Register accelerometer listener
        if (mAccelerometer != null) {
            boolean registered = mSensorManager.registerListener(
                mSensorListener,
                mAccelerometer,
                SensorManager.SENSOR_DELAY_GAME
            );
            Log.d(TAG, "Accelerometer listener registered: " + registered);
        } else {
            Log.w(TAG, "Accelerometer sensor not available");
        }

        // Register gyroscope listener
        if (mGyroscope != null) {
            boolean registered = mSensorManager.registerListener(
                mSensorListener,
                mGyroscope,
                SensorManager.SENSOR_DELAY_GAME
            );
            Log.d(TAG, "Gyroscope listener registered: " + registered);
        } else {
            Log.w(TAG, "Gyroscope sensor not available");
        }
    }

    private void unregisterSensorListeners() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorListener);
            Log.d(TAG, "Sensor listeners unregistered");
        }
    }

    /**
     * Check if gesture control is enabled
     */
    public boolean isGestureControlEnabled() {
        try {
            return Settings.System.getInt(getContentResolver(), GESTUREFLOW_ENABLED, 0) == 1;
        } catch (Exception e) {
            Log.e(TAG, "Error reading gesture control setting", e);
            return false;
        }
    }

    /**
     * Trigger back navigation action
     */
    public void triggerBackGesture() {
        Log.d(TAG, "Triggering back gesture action");
        try {
            performGlobalAction(GLOBAL_ACTION_BACK);
        } catch (Exception e) {
            Log.e(TAG, "Error performing back action", e);
        }
    }

    /**
     * Trigger recent apps action
     */
    public void triggerRecentAppsGesture() {
        Log.d(TAG, "Triggering recent apps gesture action");
        try {
            performGlobalAction(GLOBAL_ACTION_RECENTS);
        } catch (Exception e) {
            Log.e(TAG, "Error performing recent apps action", e);
        }
    }

    /**
     * Trigger notification panel action
     */
    public void triggerNotificationPanelGesture() {
        Log.d(TAG, "Triggering notification panel gesture action");
        try {
            performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
        } catch (Exception e) {
            Log.e(TAG, "Error performing notification panel action", e);
        }
    }
}

