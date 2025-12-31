/*
 * Copyright (C) 2025 BashaMobile
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * TRANSFER TO OTHER ROMS:
 * =======================
 * This is an adaptive wallpaper background view for settings pages.
 * To transfer to other ROMs:
 * 1. Copy this file
 * 2. Copy WallpaperBackgroundHelper.java
 * 3. Ensure WallpaperManager is available
 * 4. Update Settings.System key in WallpaperBackgroundHelper if needed
 */

package com.android.settings.preferences.ui;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.android.internal.graphics.ColorUtils;
import com.android.settings.awaken.fragments.DisplayCustomizationsHelper;

/**
 * Adaptive Wallpaper Background View
 * 
 * Displays the device wallpaper as a background with:
 * - Blur effect for better card visibility
 * - Light tint for light mode (white overlay)
 * - Dark mask for dark mode (black overlay)
 * - Respects toggle setting to enable/disable
 */
public class AdaptiveWallpaperBackgroundView extends ImageView {

    private static final String TAG = "AdaptiveWallpaperBG";
    
    private Handler mHandler;
    private ContentObserver mSettingsObserver;
    private ContentResolver mContentResolver;
    private Context mContext;

    public AdaptiveWallpaperBackgroundView(Context context) {
        super(context);
        init(context);
    }

    public AdaptiveWallpaperBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AdaptiveWallpaperBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mContentResolver = context.getContentResolver();

        // Configure as background element - don't intercept touches or focus
        setElevation(0f);
        setTranslationZ(0f);
        setZ(0f);
        setClickable(false);
        setFocusable(false);
        
        // Observe settings changes
        mSettingsObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                // Ensure update runs on main thread
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateWallpaperBackground();
                    }
                });
            }
        };
        
        // Observe wallpaper background setting changes
        mContentResolver.registerContentObserver(
            Settings.System.getUriFor(com.android.settings.display.WallpaperBackgroundHelper.SETTING_KEY),
            false, mSettingsObserver, android.os.UserHandle.USER_CURRENT);

        // Also observe wallpaper blur setting changes
        mContentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SETTINGS_WALLPAPER_BLUR_ENABLED),
            false, mSettingsObserver, android.os.UserHandle.USER_CURRENT);

        // Also observe wallpaper blur radius changes
        mContentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SETTINGS_WALLPAPER_BLUR_RADIUS),
            false, mSettingsObserver, android.os.UserHandle.USER_CURRENT);

        // Also observe gradient settings changes
        mContentResolver.registerContentObserver(
            Settings.System.getUriFor("monet_gradient_enabled"),
            false, mSettingsObserver, android.os.UserHandle.USER_CURRENT);
        mContentResolver.registerContentObserver(
            Settings.System.getUriFor("monet_gradient_type"),
            false, mSettingsObserver, android.os.UserHandle.USER_CURRENT);
        mContentResolver.registerContentObserver(
            Settings.System.getUriFor("monet_gradient_colors"),
            false, mSettingsObserver, android.os.UserHandle.USER_CURRENT);
        
        // Update on main thread
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateWallpaperBackground();
            }
        });
    }

    private void updateWallpaperBackground() {
        if (mContext == null) {
            Log.w(TAG, "Context is null, cannot update wallpaper background");
            return;
        }

        // Check if gradients are enabled (only in dark mode)
        boolean gradientEnabled = isGradientEnabledInDarkMode();
        boolean wallpaperActive = com.android.settings.display.WallpaperBackgroundHelper.isActive(mContext);

        Log.d(TAG, "Wallpaper background active: " + wallpaperActive + ", Gradient enabled in dark mode: " + gradientEnabled);

        // Show background if either wallpaper OR gradients are enabled
        if (!wallpaperActive && !gradientEnabled) {
            setVisibility(GONE);
            setImageBitmap(null);
            setRenderEffect(null);
            setColorFilter(null);
            setForeground(null);
            setBackground(null);
            return;
        }

        setVisibility(VISIBLE);

        // If gradients are enabled in dark mode, apply them as background only
        if (gradientEnabled) {
            applyGradientBackground();
            Log.d(TAG, "Applied gradient background");

            // If wallpaper is also active, apply it on top of gradient
            if (wallpaperActive) {
                applyWallpaperBackground();
            } else {
                // Just gradient, no wallpaper
                setImageBitmap(null);
                setRenderEffect(null);
                setColorFilter(null);
            }
        } else if (wallpaperActive) {
            // Only wallpaper, no gradient
            setForeground(null);
            setBackground(null);
            applyWallpaperBackground();
        }
    }

    private void applyWallpaperBackground() {
        // Wallpaper background only works in dark mode
        Log.d(TAG, "Wallpaper background active in dark mode");

        try {
            // Get wallpaper
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
            if (wallpaperManager == null) {
                Log.w(TAG, "WallpaperManager is null");
                return;
            }

            Drawable wallpaperDrawable = wallpaperManager.getDrawable();

            if (wallpaperDrawable == null) {
                Log.w(TAG, "Wallpaper drawable is null");
                return;
            }

            // Convert to bitmap
            Bitmap wallpaperBitmap = drawableToBitmap(wallpaperDrawable);
            if (wallpaperBitmap == null) {
                Log.w(TAG, "Failed to convert wallpaper to bitmap");
                return;
            }

            setImageBitmap(wallpaperBitmap);

            // Check if blur is enabled and apply appropriate blur effect
            boolean blurEnabled = DisplayCustomizationsHelper.isWallpaperBlurEnabled(mContext);
            if (blurEnabled) {
                // Get blur radius from settings (default 20, scale to blur effect)
                int blurRadius = DisplayCustomizationsHelper.getWallpaperBlurRadius(mContext);
                // Convert radius to blur effect (scale factor for RenderEffect)
                float blurEffect = blurRadius * 3.0f; // Scale radius to blur intensity
                setRenderEffect(RenderEffect.createBlurEffect(blurEffect, blurEffect, Shader.TileMode.CLAMP));
                Log.d(TAG, "Applied blur effect with radius: " + blurRadius + " (effect: " + blurEffect + ")");
            } else {
                // No blur effect
                setRenderEffect(null);
                Log.d(TAG, "Blur disabled, no blur effect applied");
            }

            // Apply adaptive tint based on theme
            applyAdaptiveTint();

            Log.d(TAG, "Wallpaper background applied successfully");

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: Missing READ_EXTERNAL_STORAGE permission or wallpaper access", e);
        } catch (Exception e) {
            Log.e(TAG, "Error setting wallpaper background", e);
        }
    }

    private void applyAdaptiveTint() {
        // Detect light/dark mode
        int nightMode = mContext.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = (nightMode == Configuration.UI_MODE_NIGHT_YES);

        if (isDarkMode) {
            // Dark mode: apply subtle dark mask (black overlay with low opacity)
            // This makes the wallpaper slightly darker so cards stand out
            int darkMask = ColorUtils.blendARGB(Color.TRANSPARENT, Color.BLACK, 0.25f);
            setColorFilter(darkMask, PorterDuff.Mode.SRC_ATOP);
        } else {
            // Light mode: apply subtle light tint (white overlay with very low opacity)
            // This makes the wallpaper slightly lighter so dark cards stand out
            int lightTint = ColorUtils.blendARGB(Color.TRANSPARENT, Color.WHITE, 0.15f);
            setColorFilter(lightTint, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void applyGradientBackground() {
        // Check if custom gradients are enabled in dark mode
        boolean gradientEnabled = isGradientEnabledInDarkMode();
        if (!gradientEnabled) {
            // No gradient, clear any existing background
            setForeground(null);
            setBackground(null);
            return;
        }

        // Create gradient drawable
        GradientDrawable gradient = createGradientBackground();
        if (gradient != null) {
            // Apply gradient as background only (not foreground to avoid UI interference)
            setBackground(gradient);
            setForeground(null); // Ensure no foreground overlay
            Log.d(TAG, "Applied gradient background: " + getGradientType());

            // Ensure gradient is visible even without wallpaper
            setImageBitmap(null);
            setRenderEffect(null);
            setColorFilter(null);
        } else {
            // Fallback: clear background
            setBackground(null);
            Log.w(TAG, "Failed to create gradient background");
        }
    }

    private boolean isGradientEnabled() {
        try {
            return Settings.System.getInt(mContext.getContentResolver(),
                    "monet_gradient_enabled", 0) == 1;
        } catch (Exception e) {
            Log.e(TAG, "Error reading gradient enabled setting", e);
            return false;
        }
    }

    private boolean isGradientEnabledInDarkMode() {
        // Only enable gradients in dark mode
        int nightMode = mContext.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = (nightMode == Configuration.UI_MODE_NIGHT_YES);

        return isDarkMode && isGradientEnabled();
    }

    private String getGradientType() {
        try {
            String type = Settings.System.getString(mContext.getContentResolver(),
                    "monet_gradient_type");
            return type != null ? type : "none";
        } catch (Exception e) {
            Log.e(TAG, "Error reading gradient type setting", e);
            return "none";
        }
    }

    private GradientDrawable createGradientBackground() {
        try {
            String colorsString = Settings.System.getString(mContext.getContentResolver(),
                    "monet_gradient_colors");

            int[] colors;
            if (TextUtils.isEmpty(colorsString)) {
                // Use default colors for current type
                colors = getDefaultColorsForType(getGradientType());
            } else {
                String[] colorStrings = colorsString.split(",");
                colors = new int[colorStrings.length];
                for (int i = 0; i < colorStrings.length; i++) {
                    try {
                        colors[i] = Color.parseColor(colorStrings[i].trim());
                    } catch (IllegalArgumentException e) {
                        Log.w(TAG, "Invalid color format: " + colorStrings[i] + ", using transparent");
                        colors[i] = Color.TRANSPARENT;
                    }
                }
            }

            if (colors == null || colors.length < 2) {
                return null;
            }

            GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                colors
            );
            gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);

            // Use full opacity for background gradient (no distracting alpha mask)
            gradient.setAlpha(255); // 100% opacity for clean background

            return gradient;
        } catch (Exception e) {
            Log.e(TAG, "Error creating gradient background", e);
            return null;
        }
    }

    private int[] getDefaultColorsForType(String type) {
        switch (type) {
            case "sunrise":
                return new int[]{
                    Color.parseColor("#FFF8E1"),
                    Color.parseColor("#FFE0B2"),
                    Color.parseColor("#FFCC02"),
                    Color.parseColor("#FF9800")
                };
            case "ocean":
                return new int[]{
                    Color.parseColor("#E3F2FD"),
                    Color.parseColor("#90CAF9"),
                    Color.parseColor("#42A5F5"),
                    Color.parseColor("#1976D2")
                };
            case "forest":
                return new int[]{
                    Color.parseColor("#E8F5E8"),
                    Color.parseColor("#81C784"),
                    Color.parseColor("#4CAF50"),
                    Color.parseColor("#388E3C")
                };
            case "lavender":
                return new int[]{
                    Color.parseColor("#F3E5F5"),
                    Color.parseColor("#BA68C8"),
                    Color.parseColor("#8E24AA"),
                    Color.parseColor("#6A1B9A")
                };
            case "none":
            default:
                // Return transparent colors for "none" - no gradient background
                return new int[]{
                    Color.TRANSPARENT,
                    Color.TRANSPARENT
                };
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        
        if (width <= 0 || height <= 0) {
            // Fallback to screen dimensions
            width = mContext.getResources().getDisplayMetrics().widthPixels;
            height = mContext.getResources().getDisplayMetrics().heightPixels;
        }
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        
        return bitmap;
    }

    /**
     * Public method to update the background (called externally)
     */
    public void updateBackground() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateWallpaperBackground();
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Update wallpaper background when configuration changes (e.g., theme change)
        updateBackground();
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Update when attached to window (observer already registered in init)
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateWallpaperBackground();
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mSettingsObserver != null && mContentResolver != null) {
            try {
                mContentResolver.unregisterContentObserver(mSettingsObserver);
            } catch (Exception e) {
                Log.w(TAG, "Error unregistering ContentObserver", e);
            }
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}

