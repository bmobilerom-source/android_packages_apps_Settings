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
 * 2. Ensure WallpaperManager is available
 * 3. Update Settings.System key if needed (currently "settings_wallpaper_background_enabled")
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
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.android.internal.graphics.ColorUtils;

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
    private static final String SETTING_KEY = "settings_wallpaper_background_enabled";
    
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
        
        // Observe settings changes
        mSettingsObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                updateWallpaperBackground();
            }
        };
        
        mContentResolver.registerContentObserver(
            Settings.System.getUriFor(SETTING_KEY), false, mSettingsObserver);
        
        updateWallpaperBackground();
    }

    private void updateWallpaperBackground() {
        boolean isEnabled = Settings.System.getInt(mContentResolver, SETTING_KEY, 0) != 0;
        
        if (!isEnabled) {
            setVisibility(GONE);
            return;
        }
        
        setVisibility(VISIBLE);
        
        try {
            // Get wallpaper
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            
            if (wallpaperDrawable == null) {
                Log.w(TAG, "Wallpaper drawable is null");
                setVisibility(GONE);
                return;
            }
            
            // Convert to bitmap
            Bitmap wallpaperBitmap = drawableToBitmap(wallpaperDrawable);
            setImageBitmap(wallpaperBitmap);
            
            // Apply blur effect
            setRenderEffect(RenderEffect.createBlurEffect(80f, 80f, Shader.TileMode.CLAMP));
            
            // Apply adaptive tint based on theme
            applyAdaptiveTint();
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting wallpaper background", e);
            setVisibility(GONE);
        }
    }

    private void applyAdaptiveTint() {
        // Detect light/dark mode
        int nightMode = mContext.getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = (nightMode == Configuration.UI_MODE_NIGHT_YES);
        
        if (isDarkMode) {
            // Dark mode: apply dark mask (black overlay with medium opacity)
            // This makes the wallpaper darker so light cards stand out
            int darkMask = ColorUtils.blendARGB(Color.TRANSPARENT, Color.BLACK, 0.5f);
            setColorFilter(darkMask, PorterDuff.Mode.SRC_ATOP);
        } else {
            // Light mode: apply light tint (white overlay with low opacity)
            // This makes the wallpaper lighter so dark cards stand out
            int lightTint = ColorUtils.blendARGB(Color.TRANSPARENT, Color.WHITE, 0.3f);
            setColorFilter(lightTint, PorterDuff.Mode.SRC_ATOP);
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

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Reapply tint when theme changes
        if (getVisibility() == VISIBLE) {
            applyAdaptiveTint();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mSettingsObserver != null && mContentResolver != null) {
            mContentResolver.unregisterContentObserver(mSettingsObserver);
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}

