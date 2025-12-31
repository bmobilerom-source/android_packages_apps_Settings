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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

// Palette is not available in Settings app - using alternative color extraction

import java.util.HashMap;
import java.util.Map;

/**
 * Service that monitors notifications and applies contextual colors based on notification content
 */
public class ContextualColorsService extends NotificationListenerService {

    private static final String TAG = "ContextualColorsService";
    private static final String CHANNEL_ID = "contextual_colors";
    private static final int NOTIFICATION_ID = 1001;

    private Handler mHandler;
    private boolean mIsEnabled = false;
    private Map<String, Integer> mColorCache = new HashMap<>();

    private BroadcastReceiver mSettingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("monet_contextual_enabled".equals(intent.getAction()) ||
                Settings.Secure.getUriFor("monet_contextual_enabled").toString().equals(intent.getDataString())) {
                boolean enabled = Settings.Secure.getInt(context.getContentResolver(),
                        "monet_contextual_enabled", 0) == 1;
                setEnabled(enabled);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());

        // Create notification channel
        createNotificationChannel();

        // Register settings receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.action.SETTING_CHANGED");
        registerReceiver(mSettingsReceiver, filter);

        // Check initial state
        boolean enabled = Settings.Secure.getInt(getContentResolver(),
                "monet_contextual_enabled", 0) == 1;
        setEnabled(enabled);

        Log.d(TAG, "ContextualColorsService created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setEnabled(false);
        unregisterReceiver(mSettingsReceiver);
        Log.d(TAG, "ContextualColorsService destroyed");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!mIsEnabled) return;

        mHandler.post(() -> {
            try {
                processNotification(sbn);
            } catch (Exception e) {
                Log.e(TAG, "Error processing notification", e);
            }
        });
    }

    private void processNotification(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if (notification == null) return;

        String packageName = sbn.getPackageName();
        String key = sbn.getKey();

        // Skip system notifications
        if (isSystemPackage(packageName)) return;

        // Extract colors from notification
        int dominantColor = extractDominantColor(notification, packageName, key);
        if (dominantColor != 0) {
            applyContextualColors(packageName, dominantColor, sbn.getNotification().tickerText);

            // Store last notification info
            Settings.System.putString(getContentResolver(),
                    "monet_last_notification", packageName);
            Settings.System.putInt(getContentResolver(),
                    "monet_contextual_colors", dominantColor);

            Log.d(TAG, "Applied contextual colors from " + packageName + ": #" +
                    Integer.toHexString(dominantColor));
        }
    }

    private int extractDominantColor(Notification notification, String packageName, String key) {
        try {
            // Try to get color from notification icon
            Icon icon = notification.getSmallIcon();
            if (icon != null) {
                Drawable drawable = icon.loadDrawable(this);
                if (drawable != null) {
                    Bitmap bitmap = drawableToBitmap(drawable);
                    if (bitmap != null) {
                        // Extract dominant color from bitmap (simplified without Palette library)
                        int dominantColor = extractDominantColorFromBitmap(bitmap);
                        if (dominantColor != 0) {
                            return dominantColor;
                        }
                    }
                }
            }

            // Fallback: generate color from package name hash
            int hash = packageName.hashCode();
            return Color.HSVToColor(new float[]{
                    (hash % 360 + 360) % 360, // Hue
                    0.7f, // Saturation
                    0.6f  // Value
            });

        } catch (Exception e) {
            Log.e(TAG, "Error extracting color from notification", e);
            return 0;
        }
    }

    private void applyContextualColors(String packageName, int color, CharSequence title) {
        try {
            // Store the contextual color data
            String colorData = packageName + ":" + color + ":" + (title != null ? title.toString() : "");
            Settings.System.putString(getContentResolver(),
                    "monet_contextual_colors", colorData);

            // For now, we'll store the color but rely on the theme system to pick it up
            // The actual theme application would need framework integration
            // This is a placeholder for future framework enhancement

            // Send our custom intent for other components to respond to
            Intent contextualIntent = new Intent("com.android.settings.action.CONTEXTUAL_COLORS_CHANGED");
            contextualIntent.putExtra("package_name", packageName);
            contextualIntent.putExtra("color", color);
            contextualIntent.putExtra("title", title != null ? title.toString() : "");
            sendBroadcast(contextualIntent);

            // Show foreground notification to indicate active contextual colors
            showContextualColorsNotification(packageName, color, title);

            Log.d(TAG, "Contextual color applied from " + packageName + ": #" + Integer.toHexString(color));

        } catch (Exception e) {
            Log.e(TAG, "Error applying contextual colors", e);
        }
    }

    private void showContextualColorsNotification(String packageName, int color, CharSequence title) {
        try {
            Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_menu_gallery)
                    .setContentTitle("Contextual Colors Active")
                    .setContentText("Colors adapted from " + (title != null ? title : packageName))
                    .setColor(color)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setCategory(Notification.CATEGORY_STATUS);

            startForeground(NOTIFICATION_ID, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Error showing contextual colors notification", e);
        }
    }

    private void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
        if (enabled) {
            // Start foreground service
            showContextualColorsNotification("System", Color.BLUE, "Monitoring notifications");
        } else {
            // Stop foreground service
            stopForeground(true);
            // Reset to default colors
            resetContextualColors();
        }
        Log.d(TAG, "Contextual colors service " + (enabled ? "enabled" : "disabled"));
    }

    private void resetContextualColors() {
        try {
            Settings.System.putString(getContentResolver(), "monet_contextual_colors", null);
            Settings.System.putString(getContentResolver(), "monet_last_notification", null);

            // Disable custom color mode to return to wallpaper-based colors
            Settings.Secure.putInt(getContentResolver(), "monet_custom_color_enabled", 0);

            // Notify system of theme reset
            Intent wallpaperIntent = new Intent("android.intent.action.WALLPAPER_CHANGED");
            sendBroadcast(wallpaperIntent);

            Intent resetIntent = new Intent("com.android.settings.action.CONTEXTUAL_COLORS_RESET");
            sendBroadcast(resetIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error resetting contextual colors", e);
        }
    }

    private boolean isSystemPackage(String packageName) {
        return packageName.startsWith("android") ||
               packageName.startsWith("com.android") ||
               packageName.equals("com.google.android.gms") ||
               packageName.equals("com.google.android.gsf");
    }

    private int extractDominantColorFromBitmap(Bitmap bitmap) {
        try {
            if (bitmap == null || bitmap.isRecycled()) return 0;
            
            // Sample pixels from bitmap to find dominant color
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int sampleSize = Math.max(1, Math.min(width, height) / 10); // Sample every Nth pixel
            
            long rSum = 0, gSum = 0, bSum = 0;
            int count = 0;
            
            for (int y = 0; y < height; y += sampleSize) {
                for (int x = 0; x < width; x += sampleSize) {
                    int pixel = bitmap.getPixel(x, y);
                    int alpha = Color.alpha(pixel);
                    
                    // Skip transparent pixels
                    if (alpha < 128) continue;
                    
                    rSum += Color.red(pixel);
                    gSum += Color.green(pixel);
                    bSum += Color.blue(pixel);
                    count++;
                }
            }
            
            if (count > 0) {
                int r = (int) (rSum / count);
                int g = (int) (gSum / count);
                int b = (int) (bSum / count);
                return Color.rgb(r, g, b);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting dominant color from bitmap", e);
        }
        return 0;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        try {
            if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                return ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();
            }

            // Create bitmap from drawable
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 100,
                    drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 100,
                    Bitmap.Config.ARGB_8888);

            android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error converting drawable to bitmap", e);
            return null;
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Contextual Colors",
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Shows when contextual colors are active");
        channel.setShowBadge(false);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
