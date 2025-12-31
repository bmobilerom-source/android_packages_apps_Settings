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

import android.content.Context;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.util.Log;

/**
 * GestureDetector implements motion gesture recognition algorithms.
 * Processes accelerometer and gyroscope data to detect specific gesture patterns
 * and triggers corresponding actions through the accessibility service.
 */
public class GestureDetector {

    private static final String TAG = "GestureDetector";

    // Gesture detection thresholds (m/s²)
    private static final float BACK_GESTURE_THRESHOLD = 15.0f;
    private static final float RECENT_APPS_THRESHOLD = 12.0f;
    private static final float NOTIFICATION_THRESHOLD = 18.0f;

    // Settings keys
    private static final String GESTUREFLOW_SENSITIVITY = "gestureflow_sensitivity";

    // Sensor data buffers
    private final float[] mAccelerometerData = new float[3];
    private final float[] mGyroscopeData = new float[3];

    // Gesture state tracking
    private boolean mBackGestureDetected = false;
    private boolean mRecentAppsGestureDetected = false;
    private boolean mNotificationGestureDetected = false;

    // Timing controls
    private long mLastGestureTime = 0;
    private static final long GESTURE_COOLDOWN = 1000; // 1 second cooldown
    private static final long GESTURE_RESET_DELAY = 500; // Reset flags after 500ms

    // Sensitivity multiplier (configurable, default 1.0)
    private float mSensitivityMultiplier = 1.0f;

    private final GestureAccessibilityService mService;
    private final Context mContext;

    public GestureDetector(GestureAccessibilityService service) {
        mService = service;
        mContext = service.getApplicationContext();
        loadSensitivitySetting();
        Log.d(TAG, "GestureDetector initialized with sensitivity: " + mSensitivityMultiplier);
    }

    /**
     * Process accelerometer sensor data
     */
    public void processAccelerometerData(float[] values) {
        System.arraycopy(values, 0, mAccelerometerData, 0, 3);
        detectGestures();
    }

    /**
     * Process gyroscope sensor data
     */
    public void processGyroscopeData(float[] values) {
        System.arraycopy(values, 0, mGyroscopeData, 0, 3);
        // Gyroscope data can be used for enhanced gesture detection if needed
        detectGestures();
    }

    /**
     * Main gesture detection logic
     */
    private void detectGestures() {
        long currentTime = System.currentTimeMillis();

        // Check cooldown period
        if (currentTime - mLastGestureTime < GESTURE_COOLDOWN) {
            return; // Still in cooldown
        }

        // Get current acceleration values
        float accelX = mAccelerometerData[0];
        float accelY = mAccelerometerData[1];
        float accelZ = mAccelerometerData[2];

        // Calculate acceleration magnitude for threshold-based detection
        float accelerationMagnitude = calculateMagnitude(mAccelerometerData);

        // Apply sensitivity multiplier to thresholds
        float adjustedBackThreshold = BACK_GESTURE_THRESHOLD / mSensitivityMultiplier;
        float adjustedRecentThreshold = RECENT_APPS_THRESHOLD / mSensitivityMultiplier;
        float adjustedNotificationThreshold = NOTIFICATION_THRESHOLD / mSensitivityMultiplier;

        Log.v(TAG, String.format("Accel: X=%.2f, Y=%.2f, Z=%.2f, Mag=%.2f", accelX, accelY, accelZ, accelerationMagnitude));

        // Detect specific gestures based on acceleration patterns

        // Back gesture: Quick pull towards user (high negative Z acceleration)
        if (!mBackGestureDetected && accelZ < -adjustedBackThreshold) {
            Log.d(TAG, String.format("Back gesture detected: Z=%.2f < -%.2f", accelZ, adjustedBackThreshold));
            triggerBackGesture(currentTime);
        }

        // Recent apps gesture: Quick swipe to the right (high positive X acceleration)
        if (!mRecentAppsGestureDetected && accelX > adjustedRecentThreshold) {
            Log.d(TAG, String.format("Recent apps gesture detected: X=%.2f > %.2f", accelX, adjustedRecentThreshold));
            triggerRecentAppsGesture(currentTime);
        }

        // Notification gesture: Quick downward motion (high negative Y acceleration)
        if (!mNotificationGestureDetected && accelY < -adjustedNotificationThreshold) {
            Log.d(TAG, String.format("Notification gesture detected: Y=%.2f < -%.2f", accelY, adjustedNotificationThreshold));
            triggerNotificationGesture(currentTime);
        }

        // Reset gesture flags after a short delay
        resetGestureFlagsDelayed();
    }

    /**
     * Calculate magnitude of acceleration vector
     */
    private float calculateMagnitude(float[] acceleration) {
        return (float) Math.sqrt(
            acceleration[0] * acceleration[0] +
            acceleration[1] * acceleration[1] +
            acceleration[2] * acceleration[2]
        );
    }

    /**
     * Trigger back gesture action
     */
    private void triggerBackGesture(long timestamp) {
        mBackGestureDetected = true;
        mLastGestureTime = timestamp;
        Log.d(TAG, "Back gesture detected - triggering back action");
        mService.triggerBackGesture();
    }

    /**
     * Trigger recent apps gesture action
     */
    private void triggerRecentAppsGesture(long timestamp) {
        mRecentAppsGestureDetected = true;
        mLastGestureTime = timestamp;
        Log.d(TAG, "Recent apps gesture detected - triggering recent apps action");
        mService.triggerRecentAppsGesture();
    }

    /**
     * Trigger notification panel gesture action
     */
    private void triggerNotificationGesture(long timestamp) {
        mNotificationGestureDetected = true;
        mLastGestureTime = timestamp;
        Log.d(TAG, "Notification gesture detected - triggering notification panel action");
        mService.triggerNotificationPanelGesture();
    }

    /**
     * Reset gesture detection flags after a delay
     */
    private void resetGestureFlagsDelayed() {
        new android.os.Handler().postDelayed(() -> {
            mBackGestureDetected = false;
            mRecentAppsGestureDetected = false;
            mNotificationGestureDetected = false;
        }, GESTURE_RESET_DELAY);
    }

    /**
     * Load sensitivity setting from system settings
     */
    private void loadSensitivitySetting() {
        try {
            int sensitivityValue = Settings.System.getInt(
                mContext.getContentResolver(),
                GESTUREFLOW_SENSITIVITY,
                10 // Default sensitivity value
            );

            // Convert to multiplier (1-30 range maps to 0.1-3.0 multiplier)
            mSensitivityMultiplier = Math.max(0.1f, Math.min(3.0f, sensitivityValue / 10.0f));
            Log.d(TAG, "Loaded sensitivity: " + sensitivityValue + " -> multiplier: " + mSensitivityMultiplier);

        } catch (Exception e) {
            Log.e(TAG, "Error loading sensitivity setting, using default", e);
            mSensitivityMultiplier = 1.0f;
        }
    }

    /**
     * Update sensitivity multiplier
     */
    public void setSensitivityMultiplier(float multiplier) {
        mSensitivityMultiplier = Math.max(0.1f, Math.min(3.0f, multiplier));
        Log.d(TAG, "Sensitivity multiplier updated: " + mSensitivityMultiplier);
    }

    /**
     * Get current sensitivity multiplier
     */
    public float getSensitivityMultiplier() {
        return mSensitivityMultiplier;
    }
}

