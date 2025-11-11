/*
 * Copyright (C) 2025 LineageOS
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
 *
 * TRANSFER TO OTHER ROMS:
 * =======================
 * This view applies theme-specific visual effects directly in Settings.
 * To transfer to other ROMs:
 * 1. Copy this file
 * 2. Copy CustomThemeHelper.java
 * 3. Ensure Settings.Secure.SYSTEM_CUSTOM_THEME is defined
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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.VideoView;
import android.view.ViewGroup;
import android.net.Uri;

import com.android.internal.graphics.ColorUtils;
import com.android.settings.R;
import com.android.settings.theme.CustomThemeHelper;

/**
 * Adaptive Theme Background View
 * 
 * Applies theme-specific visual effects:
 * - Black theme: Black depth wallpaper with color accent tint
 * - Vivid theme: Transparent background with 70% blur
 * - Other themes: Standard background
 */
public class AdaptiveThemeBackgroundView extends ImageView {

    private static final String TAG = "AdaptiveThemeBG";
    
    private Handler mHandler;
    private ContentObserver mSettingsObserver;
    private ContentResolver mContentResolver;
    private Context mContext;
    private VideoView mVideoView;
    private int mCurrentTheme = -1; // Track current theme for cleanup
    private String mLastPageKey = null; // Track last page for expressive mode
    private android.animation.ValueAnimator mBlurAnimator; // For animated wallpaper blur
    private static final String ANIMATED_THEME_VIDEO_PATH = "/system/media/theme_animated_background.mp4";
    private static final int DEFAULT_VIDEO_RAW_RES = R.raw.gesture_ambient_lift; // Default video from raw folder (static raw resource)

    public AdaptiveThemeBackgroundView(Context context) {
        super(context);
        init(context);
    }

    public AdaptiveThemeBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AdaptiveThemeBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mContentResolver = context.getContentResolver();
        
        // Ensure this view is always behind other content
        setZ(0f);
        setElevation(0f);
        setTranslationZ(0f);
        
        // Observe theme changes
        mSettingsObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                updateThemeBackground();
            }
        };
        
        mContentResolver.registerContentObserver(
            Settings.Secure.getUriFor(CustomThemeHelper.SETTING_KEY), 
            false, mSettingsObserver);
        
        // Initial update
        updateThemeBackground();
    }

    private void updateThemeBackground() {
        if (mContext == null) {
            return;
        }
        
        int previousTheme = mCurrentTheme;
        int theme = CustomThemeHelper.getCurrentTheme(mContext);
        mCurrentTheme = theme;
        
        // Clean up video view if switching away from animated themes
        if ((previousTheme == CustomThemeHelper.THEME_ANIMATED || previousTheme == CustomThemeHelper.THEME_ANIMATED_WALLPAPER_BLUR) 
            && theme != CustomThemeHelper.THEME_ANIMATED && theme != CustomThemeHelper.THEME_ANIMATED_WALLPAPER_BLUR) {
            cleanupVideoView();
            cleanupBlurAnimator();
        }
        
        // Don't apply default theme
        if (theme == CustomThemeHelper.THEME_DEFAULT) {
            setVisibility(android.view.View.GONE);
            setImageDrawable(null);
            setRenderEffect(null);
            cleanupVideoView();
            return;
        }
        
        setVisibility(android.view.View.VISIBLE);
        
        try {
            if (theme == CustomThemeHelper.THEME_BLACK) {
                // Black theme: Black depth wallpaper (works in both modes)
                applyBlackTheme();
            } else if (theme == CustomThemeHelper.THEME_TRANSPARENT) {
                // Transparent theme: Transparent with 70% blur (works in both modes)
                applyTransparentTheme();
            } else if (theme == CustomThemeHelper.THEME_SNOWPAINT) {
                // Snowpaint theme: Light gray/white tones
                applySnowpaintTheme();
            } else if (theme == CustomThemeHelper.THEME_ESPRESSO) {
                // Espresso theme: Rich brown/dark coffee tones
                applyEspressoTheme();
            } else if (theme == CustomThemeHelper.THEME_CUSTOM_BLUE) {
                // Custom Blue theme: Blue gradient
                applyCustomBlueTheme();
            } else if (theme == CustomThemeHelper.THEME_ANIMATED) {
                // Animated theme: MP4 video background (with fallback to wallpaper/Lottie)
                applyAnimatedTheme();
            } else if (theme == CustomThemeHelper.THEME_EXPRESSIVE) {
                // Expressive mode: Dynamic pastel colors per page
                applyExpressiveTheme();
            } else if (theme == CustomThemeHelper.THEME_ANIMATED_WALLPAPER_BLUR) {
                // Animated wallpaper blur: System wallpaper with fast-moving blur
                applyAnimatedWallpaperBlurTheme();
            } else {
                // Unknown theme: Hide
                setVisibility(android.view.View.GONE);
                setImageDrawable(null);
                setRenderEffect(null);
                cleanupVideoView();
                cleanupBlurAnimator();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating theme background", e);
            setVisibility(android.view.View.GONE);
        }
    }

    private void applyBlackTheme() {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            
            if (wallpaperDrawable != null) {
                // Create a black depth effect by darkening the wallpaper
                Bitmap bitmap = drawableToBitmap(wallpaperDrawable);
                if (bitmap != null) {
                    // Apply dark tint to create black depth effect (70% black overlay)
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.argb(180, 0, 0, 0), PorterDuff.Mode.MULTIPLY);
                    
                    BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
                    setImageDrawable(drawable);
                    
                    // Apply blur for depth (40dp blur radius)
                    float blurRadius = 40.0f;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        setRenderEffect(RenderEffect.createBlurEffect(
                                blurRadius, blurRadius, Shader.TileMode.CLAMP));
                    }
                    
                    // Scale type
                    setScaleType(ScaleType.CENTER_CROP);
                } else {
                    // Fallback: solid black background
                    setImageDrawable(new ColorDrawable(Color.BLACK));
                    setRenderEffect(null);
                }
            } else {
                // Fallback: solid black background
                setImageDrawable(new ColorDrawable(Color.BLACK));
                setRenderEffect(null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying black theme", e);
            setImageDrawable(new ColorDrawable(Color.BLACK));
        }
    }

    private void applyTransparentTheme() {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            
            boolean isDarkMode = CustomThemeHelper.isDarkMode(mContext);
            
            if (wallpaperDrawable != null) {
                Bitmap bitmap = drawableToBitmap(wallpaperDrawable);
                if (bitmap != null) {
                    BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
                    setImageDrawable(drawable);
                    
                    // Apply 70% blur (56dp = 80 * 0.7) - improved for better contrast
                    float blurRadius = CustomThemeHelper.getBlurRadius(mContext);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        setRenderEffect(RenderEffect.createBlurEffect(
                                blurRadius, blurRadius, Shader.TileMode.CLAMP));
                    }
                    
                    // Apply transparency with better contrast for adaptive cards
                    // Dark mode: slightly more opaque for better text readability
                    // Light mode: slightly more transparent for cleaner look
                    float alpha = isDarkMode ? 0.35f : 0.3f;
                    setAlpha(alpha);
                    
                    setScaleType(ScaleType.CENTER_CROP);
                } else {
                    // Fallback: improved transparent background with tint for contrast
                    int tintColor = isDarkMode ? Color.argb(30, 255, 255, 255) : Color.argb(20, 0, 0, 0);
                    setImageDrawable(new ColorDrawable(tintColor));
                    setRenderEffect(null);
                    setAlpha(isDarkMode ? 0.35f : 0.3f);
                }
            } else {
                // Fallback: improved transparent background with tint for contrast
                int tintColor = isDarkMode ? Color.argb(30, 255, 255, 255) : Color.argb(20, 0, 0, 0);
                setImageDrawable(new ColorDrawable(tintColor));
                setRenderEffect(null);
                setAlpha(isDarkMode ? 0.35f : 0.3f);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying transparent theme", e);
            boolean isDarkMode = CustomThemeHelper.isDarkMode(mContext);
            int tintColor = isDarkMode ? Color.argb(30, 255, 255, 255) : Color.argb(20, 0, 0, 0);
            setImageDrawable(new ColorDrawable(tintColor));
            setAlpha(isDarkMode ? 0.35f : 0.3f);
        }
    }
    
    private void applySnowpaintTheme() {
        try {
            // Snowpaint: Light gray and white tones (#1E1E1E)
            int snowpaintColor = Color.rgb(0x1E, 0x1E, 0x1E);
            setImageDrawable(new ColorDrawable(snowpaintColor));
            setRenderEffect(null);
            setAlpha(1.0f);
        } catch (Exception e) {
            Log.e(TAG, "Error applying snowpaint theme", e);
            setImageDrawable(new ColorDrawable(Color.rgb(0x1E, 0x1E, 0x1E)));
        }
    }
    
    private void applyEspressoTheme() {
        try {
            // Espresso: Warm brown and coffee tones with gradient
            // Based on crDroid and other ROMs: warm browns (#3E2723, #5D4037, #6D4C41)
            android.graphics.drawable.GradientDrawable gradient = 
                new android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {
                        Color.rgb(0x3E, 0x27, 0x23), // Dark brown (espresso)
                        Color.rgb(0x5D, 0x40, 0x37), // Medium brown (coffee)
                        Color.rgb(0x6D, 0x4C, 0x41)  // Light brown (latte)
                    });
            setImageDrawable(gradient);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                setRenderEffect(RenderEffect.createBlurEffect(
                        15.0f, 15.0f, Shader.TileMode.CLAMP));
            }
            setAlpha(0.95f);
        } catch (Exception e) {
            Log.e(TAG, "Error applying espresso theme", e);
            setImageDrawable(new ColorDrawable(Color.rgb(0x5D, 0x40, 0x37)));
        }
    }
    
    private void applyCustomBlueTheme() {
        try {
            // Custom Blue: Blue gradient from dark to light blue
            android.graphics.drawable.GradientDrawable gradient = 
                new android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {
                        Color.rgb(0x0D, 0x47, 0xA1), // Dark blue
                        Color.rgb(0x21, 0x96, 0xF3), // Medium blue
                        Color.rgb(0x64, 0xB5, 0xF6)  // Light blue
                    });
            setImageDrawable(gradient);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                setRenderEffect(RenderEffect.createBlurEffect(
                        20.0f, 20.0f, Shader.TileMode.CLAMP));
            }
            setAlpha(0.8f);
        } catch (Exception e) {
            Log.e(TAG, "Error applying custom blue theme", e);
            setImageDrawable(new ColorDrawable(Color.rgb(0x21, 0x96, 0xF3)));
        }
    }
    
    private void applyAnimatedTheme() {
        try {
            // Animated: Try to play MP4 video first, fallback to wallpaper/Lottie
            // Inspired by AdaptiveWallpaperBackgroundView approach
            
            // Method 1: Try MP4 video from system media
            if (setupVideoView()) {
                // Video is playing, hide ImageView
                setVisibility(GONE);
                return;
            }
            
            // Method 2: Fallback to wallpaper with animated blur effect
            setVisibility(VISIBLE);
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            
            if (wallpaperDrawable != null) {
                Bitmap bitmap = drawableToBitmap(wallpaperDrawable);
                if (bitmap != null) {
                    BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
                    setImageDrawable(drawable);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        // Animated blur effect - creates a dynamic, living background
                        setRenderEffect(RenderEffect.createBlurEffect(
                                30.0f, 30.0f, Shader.TileMode.CLAMP));
                    }
                    setAlpha(0.7f);
                    setScaleType(ScaleType.CENTER_CROP);
                } else {
                    // Fallback: gradient animation effect
                    applyAnimatedGradientFallback();
                }
            } else {
                // Fallback: gradient animation effect
                applyAnimatedGradientFallback();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying animated theme", e);
            setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
    
    /**
     * Sets up VideoView for animated theme MP4 playback.
     * Returns true if video is successfully set up and playing.
     * Inspired by AdaptiveWallpaperBackgroundView pattern.
     * First tries default video from raw folder, then allows user to choose custom video.
     */
    private boolean setupVideoView() {
        try {
            // Get parent container to add VideoView
            ViewGroup parent = (ViewGroup) getParent();
            if (parent == null) {
                return false;
            }
            
            // Remove existing VideoView if present
            if (mVideoView != null && mVideoView.getParent() != null) {
                ((ViewGroup) mVideoView.getParent()).removeView(mVideoView);
                mVideoView.stopPlayback();
                mVideoView = null;
            }
            
            // Create and configure VideoView
            mVideoView = new VideoView(mContext);
            mVideoView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            // VideoView doesn't have setScaleType, but we can use layout params and scaling
            
            Uri videoUri = null;
            
            // Method 1: Try default video from raw folder first
            try {
                // Check if raw resource exists
                try {
                    android.content.res.Resources res = mContext.getResources();
                    TypedValue value = new TypedValue();
                    res.getValue(DEFAULT_VIDEO_RAW_RES, value, true);
                    
                    // Resource exists, create URI
                    String rawPath = "android.resource://" + mContext.getPackageName() + "/" + DEFAULT_VIDEO_RAW_RES;
                    videoUri = Uri.parse(rawPath);
                    Log.d(TAG, "Using default animated video from raw folder: " + rawPath);
                } catch (android.content.res.Resources.NotFoundException e) {
                    Log.d(TAG, "Default raw video resource not found, trying file paths");
                    videoUri = null;
                }
            } catch (Exception e) {
                Log.d(TAG, "Error checking raw video resource", e);
                videoUri = null;
            }
            
            // Method 2: If raw video failed, try user-selected custom video path
            if (videoUri == null) {
                String customVideoPath = Settings.Secure.getString(mContext.getContentResolver(),
                    "theme_animated_background_video_path");
                
                if (customVideoPath != null && !customVideoPath.isEmpty()) {
                    java.io.File customFile = new java.io.File(customVideoPath);
                    if (customFile.exists()) {
                        videoUri = Uri.fromFile(customFile);
                        Log.d(TAG, "Using user-selected custom video: " + customVideoPath);
                    }
                }
            }
            
            // Method 3: Fallback to system paths
            if (videoUri == null) {
                java.io.File videoFile = new java.io.File(ANIMATED_THEME_VIDEO_PATH);
                if (!videoFile.exists()) {
                    // Try alternative paths
                    String[] altPaths = {
                        "/system/product/media/theme_animated_background.mp4",
                        "/vendor/media/theme_animated_background.mp4",
                        "/data/local/tmp/theme_animated_background.mp4"
                    };
                    
                    boolean found = false;
                    for (String path : altPaths) {
                        java.io.File altFile = new java.io.File(path);
                        if (altFile.exists()) {
                            videoFile = altFile;
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        Log.d(TAG, "Animated theme video not found, using wallpaper fallback");
                        return false;
                    }
                }
                videoUri = Uri.fromFile(videoFile);
            }
            
            // Set video URI
            mVideoView.setVideoURI(videoUri);
            
            // Set up looping
            mVideoView.setOnCompletionListener(mediaPlayer -> {
                if (mVideoView != null) {
                    mVideoView.start();
                }
            });
            
            // Set up error handling
            mVideoView.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Video playback error: what=" + what + ", extra=" + extra);
                // Fallback to wallpaper
                if (mVideoView != null && mVideoView.getParent() != null) {
                    ((ViewGroup) mVideoView.getParent()).removeView(mVideoView);
                }
                mVideoView = null;
                return true; // Error handled
            });
            
            // Insert VideoView behind this ImageView (at index 0)
            int index = parent.indexOfChild(this);
            parent.addView(mVideoView, index);
            
            // Start playback
            mVideoView.start();
            
            // Apply blur effect to video (if supported)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                mVideoView.setRenderEffect(RenderEffect.createBlurEffect(
                        20.0f, 20.0f, Shader.TileMode.CLAMP));
            }
            
            // Set alpha for transparency
            mVideoView.setAlpha(0.7f);
            
            Log.d(TAG, "Animated theme video started successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up video view for animated theme", e);
            // Clean up on error
            if (mVideoView != null) {
                try {
                    if (mVideoView.getParent() != null) {
                        ((ViewGroup) mVideoView.getParent()).removeView(mVideoView);
                    }
                    mVideoView.stopPlayback();
                } catch (Exception cleanupEx) {
                    Log.e(TAG, "Error cleaning up video view", cleanupEx);
                }
                mVideoView = null;
            }
            return false;
        }
    }
    
    /**
     * Applies gradient fallback for animated theme when video is not available.
     */
    private void applyAnimatedGradientFallback() {
        android.graphics.drawable.GradientDrawable gradient = 
            new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {
                    Color.rgb(0x1A, 0x1A, 0x2E), // Dark purple-blue
                    Color.rgb(0x16, 0x2A, 0x47), // Medium blue
                    Color.rgb(0x0F, 0x34, 0x60)  // Dark blue
                });
        setImageDrawable(gradient);
        setAlpha(0.8f);
    }
    
    /**
     * Cleans up video view when theme changes or view is destroyed.
     */
    private void cleanupVideoView() {
        if (mVideoView != null) {
            try {
                mVideoView.stopPlayback();
                if (mVideoView.getParent() != null) {
                    ((ViewGroup) mVideoView.getParent()).removeView(mVideoView);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up video view", e);
            }
            mVideoView = null;
        }
    }
    
    private void applyExpressiveTheme() {
        try {
            // Expressive mode: Get color based on current page/fragment
            // Always get fresh page key to ensure correct color per page
            String pageKey = getCurrentPageKey();
            
            // Update last page key
            if (pageKey != null) {
                mLastPageKey = pageKey;
            }
            
            // Get expressive color for this page
            int expressiveColor = CustomThemeHelper.getExpressiveColor(mContext, pageKey);
            
            // Create gradient with the expressive color and a slightly darker variant
            // Adjust for dark mode to ensure better contrast
            boolean isDarkMode = CustomThemeHelper.isDarkMode(mContext);
            int darkerColor;
            if (isDarkMode) {
                // In dark mode, make it slightly lighter for better contrast
                darkerColor = Color.rgb(
                    Math.min(255, (Color.red(expressiveColor) * 110) / 100),
                    Math.min(255, (Color.green(expressiveColor) * 110) / 100),
                    Math.min(255, (Color.blue(expressiveColor) * 110) / 100)
                );
            } else {
                // In light mode, make it slightly darker
                darkerColor = Color.rgb(
                    Math.max(0, (Color.red(expressiveColor) * 85) / 100),
                    Math.max(0, (Color.green(expressiveColor) * 85) / 100),
                    Math.max(0, (Color.blue(expressiveColor) * 85) / 100)
                );
            }
            
            android.graphics.drawable.GradientDrawable gradient = 
                new android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {
                        darkerColor,  // Variant at top
                        expressiveColor,  // Main expressive color
                        expressiveColor   // Same color at bottom
                    });
            setImageDrawable(gradient);
            
            // Apply blur mask for smooth transitions
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                setRenderEffect(RenderEffect.createBlurEffect(
                        25.0f, 25.0f, Shader.TileMode.CLAMP));
            }
            
            // Set opacity based on mode for better contrast with adaptive cards
            setAlpha(isDarkMode ? 0.65f : 0.75f);
            setScaleType(ScaleType.CENTER_CROP);
        } catch (Exception e) {
            Log.e(TAG, "Error applying expressive theme", e);
            // Fallback: soft pastel blue
            setImageDrawable(new ColorDrawable(Color.rgb(0x90, 0xCA, 0xF9)));
            setAlpha(0.75f);
        }
    }
    
    /**
     * Gets the current page key for expressive mode color calculation.
     * Tries multiple methods to identify the current page/fragment.
     */
    private String getCurrentPageKey() {
        String pageKey = "default";
        try {
            // Method 1: Try to get from Activity
            if (mContext instanceof android.app.Activity) {
                android.app.Activity activity = (android.app.Activity) mContext;
                pageKey = activity.getClass().getName();
                
                // Try to get fragment from activity
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        android.app.FragmentManager fragmentManager = activity.getFragmentManager();
                        if (fragmentManager != null) {
                            android.app.Fragment fragment = fragmentManager.findFragmentById(android.R.id.content);
                            if (fragment != null) {
                                pageKey = fragment.getClass().getName();
                            }
                        }
                    }
                } catch (Exception e) {
                    // Try androidx fragment
                    try {
                        androidx.fragment.app.FragmentManager fragmentManager = 
                            ((androidx.fragment.app.FragmentActivity) activity).getSupportFragmentManager();
                        if (fragmentManager != null) {
                            androidx.fragment.app.Fragment fragment = fragmentManager.findFragmentById(android.R.id.content);
                            if (fragment != null) {
                                pageKey = fragment.getClass().getName();
                            }
                        }
                    } catch (Exception e2) {
                        // Use activity class name
                    }
                }
            } else {
                // Method 2: Try to get from parent view
                android.view.ViewParent parent = getParent();
                while (parent != null && parent instanceof android.view.View) {
                    android.view.View parentView = (android.view.View) parent;
                    android.content.Context parentContext = parentView.getContext();
                    if (parentContext instanceof android.app.Activity) {
                        android.app.Activity activity = (android.app.Activity) parentContext;
                        pageKey = activity.getClass().getName();
                        
                        // Try to get fragment
                        try {
                            androidx.fragment.app.FragmentManager fragmentManager = 
                                ((androidx.fragment.app.FragmentActivity) activity).getSupportFragmentManager();
                            if (fragmentManager != null) {
                                androidx.fragment.app.Fragment fragment = fragmentManager.findFragmentById(android.R.id.content);
                                if (fragment != null) {
                                    pageKey = fragment.getClass().getName();
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            // Continue searching
                        }
                    }
                    parent = parent.getParent();
                }
            }
            
            // Method 3: Try to get from view tag
            Object fragmentTag = getTag();
            if (fragmentTag != null && fragmentTag.toString().contains(".")) {
                pageKey = fragmentTag.toString();
            }
        } catch (Exception e) {
            Log.d(TAG, "Could not determine page key, using default", e);
        }
        
        Log.d(TAG, "Page key determined: " + pageKey);
        return pageKey;
    }
    
    /**
     * Applies animated wallpaper blur theme - system wallpaper with fast-moving blur effect.
     * Creates a dynamic, animated background that's fast enough to see the animation.
     */
    private void applyAnimatedWallpaperBlurTheme() {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            
            if (wallpaperDrawable != null) {
                Bitmap bitmap = drawableToBitmap(wallpaperDrawable);
                if (bitmap != null) {
                    BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
                    setImageDrawable(drawable);
                    setScaleType(ScaleType.CENTER_CROP);
                    
                    // Start animated blur effect - fast enough to see the animation
                    startAnimatedBlur();
                } else {
                    // Fallback: gradient with animated blur
                    applyAnimatedGradientFallback();
                    startAnimatedBlur();
                }
            } else {
                // Fallback: gradient with animated blur
                applyAnimatedGradientFallback();
                startAnimatedBlur();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying animated wallpaper blur theme", e);
            setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
    
    /**
     * Starts animated blur effect that changes blur radius over time.
     * Creates a fast-moving blur animation that's visible to the user.
     */
    private void startAnimatedBlur() {
        cleanupBlurAnimator();
        
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            // Blur not supported, just set static blur
            return;
        }
        
        // Animate blur radius from 20dp to 60dp and back (fast animation)
        mBlurAnimator = android.animation.ValueAnimator.ofFloat(20.0f, 60.0f);
        mBlurAnimator.setDuration(2000); // 2 seconds for fast, visible animation
        mBlurAnimator.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        mBlurAnimator.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        mBlurAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        
        mBlurAnimator.addUpdateListener(animation -> {
            float blurRadius = (Float) animation.getAnimatedValue();
            try {
                setRenderEffect(RenderEffect.createBlurEffect(
                        blurRadius, blurRadius, Shader.TileMode.CLAMP));
            } catch (Exception e) {
                Log.e(TAG, "Error updating animated blur", e);
            }
        });
        
        mBlurAnimator.start();
    }
    
    /**
     * Cleans up blur animator when theme changes or view is destroyed.
     */
    private void cleanupBlurAnimator() {
        if (mBlurAnimator != null) {
            try {
                mBlurAnimator.cancel();
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up blur animator", e);
            }
            mBlurAnimator = null;
        }
    }
    
    /**
     * Applies custom picture theme - user-selected image from files with blur effect.
     * Loads image from Settings.Secure.THEME_CUSTOM_PICTURE_PATH.
     */
    private void applyCustomPictureTheme() {
        try {
            // Get user-selected image path from Settings
            String imagePath = Settings.Secure.getString(mContext.getContentResolver(),
                    "theme_custom_picture_path");
            
            if (imagePath != null && !imagePath.isEmpty()) {
                java.io.File imageFile = new java.io.File(imagePath);
                if (imageFile.exists()) {
                    // Load image from file
                    android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imagePath, options);
                    
                    if (bitmap != null) {
                        BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
                        setImageDrawable(drawable);
                        setScaleType(ScaleType.CENTER_CROP);
                        
                        // Apply blur effect (50dp blur radius)
                        float blurRadius = 50.0f;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            setRenderEffect(RenderEffect.createBlurEffect(
                                    blurRadius, blurRadius, Shader.TileMode.CLAMP));
                        }
                        
                        // Set alpha for transparency
                        setAlpha(0.8f);
                        
                        Log.d(TAG, "Custom picture theme applied from: " + imagePath);
                        return;
                    }
                }
            }
            
            // Fallback: use system wallpaper with blur
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            
            if (wallpaperDrawable != null) {
                Bitmap bitmap = drawableToBitmap(wallpaperDrawable);
                if (bitmap != null) {
                    BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
                    setImageDrawable(drawable);
                    setScaleType(ScaleType.CENTER_CROP);
                    
                    // Apply blur effect
                    float blurRadius = 50.0f;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        setRenderEffect(RenderEffect.createBlurEffect(
                                blurRadius, blurRadius, Shader.TileMode.CLAMP));
                    }
                    
                    setAlpha(0.8f);
                } else {
                    // Final fallback: solid color with blur
                    setImageDrawable(new ColorDrawable(Color.GRAY));
                    setAlpha(0.8f);
                }
            } else {
                // Final fallback: solid color with blur
                setImageDrawable(new ColorDrawable(Color.GRAY));
                setAlpha(0.8f);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying custom picture theme", e);
            setImageDrawable(new ColorDrawable(Color.GRAY));
            setAlpha(0.8f);
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        
        if (width <= 0 || height <= 0) {
            // Use screen dimensions as fallback
            android.view.Display display = ((android.app.Activity) mContext).getWindowManager().getDefaultDisplay();
            android.graphics.Point size = new android.graphics.Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        }
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        
        return bitmap;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        
        // Ensure this view is always behind other content
        setZ(0f);
        setElevation(0f);
        setTranslationZ(0f);
        
        // Update expressive theme when view is attached (page change)
        // Use post to ensure fragment is fully attached
        post(new Runnable() {
            @Override
            public void run() {
                if (mCurrentTheme == CustomThemeHelper.THEME_EXPRESSIVE) {
                    String currentPageKey = getCurrentPageKey();
                    if (currentPageKey != null && !currentPageKey.equals(mLastPageKey)) {
                        mLastPageKey = currentPageKey;
                        applyExpressiveTheme();
                    }
                }
            }
        });

        // Set up periodic check for page changes (every 500ms) to catch fragment transitions
        if (mCurrentTheme == CustomThemeHelper.THEME_EXPRESSIVE) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentTheme == CustomThemeHelper.THEME_EXPRESSIVE && isAttachedToWindow()) {
                        String currentPageKey = getCurrentPageKey();
                        if (currentPageKey != null && !currentPageKey.equals(mLastPageKey)) {
                            mLastPageKey = currentPageKey;
                            applyExpressiveTheme();
                        }
                        // Continue checking
                        mHandler.postDelayed(this, 500);
                    }
                }
            }, 500);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Clean up video view and animator when view is detached
        cleanupVideoView();
        cleanupBlurAnimator();
        if (mContentResolver != null && mSettingsObserver != null) {
            mContentResolver.unregisterContentObserver(mSettingsObserver);
        }
    }
}

