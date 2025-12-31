/*
 * Copyright (C) 2024 The LineageOS Project
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

package com.android.settings.preferences.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

/**
 * Adaptive background view that provides animated video backgrounds for themes
 * Includes crash prevention for VideoView lifecycle management
 */
public class AdaptiveThemeBackgroundView extends View {
    private static final String TAG = "AdaptiveThemeBackgroundView";

    private Context mContext;
    private VideoView mVideoView;
    private Paint mGradientPaint;
    private Handler mHandler;
    private boolean mIsVideoLoaded = false;
    private boolean mIsDestroyed = false;

    // Video setup runnable
    private final Runnable mVideoSetupRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIsDestroyed) return;

            try {
                setupVideoView();
            } catch (Exception e) {
                Log.e(TAG, "Error in video setup runnable", e);
                cleanupVideoView();
            }
        }
    };

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
        mGradientPaint = new Paint();

        // Create VideoView with lifecycle isolation to prevent crashes
        createIsolatedVideoView();

        // Create gradient shader for fallback
        createGradientShader();
    }

    /**
     * Create VideoView with complete lifecycle isolation to prevent crashes
     */
    private void createIsolatedVideoView() {
        // Create VideoView with maximum isolation from Android's view system
        mVideoView = new VideoView(mContext) {
            @Override
            protected void onAttachedToWindow() {
                // Override to prevent system attach handling that could cause crashes
                try {
                    super.onAttachedToWindow();
                } catch (Exception e) {
                    Log.d(TAG, "Safe VideoView onAttachedToWindow override");
                }
            }

            @Override
            protected void onDetachedFromWindow() {
                // CRITICAL: Completely bypass Android's detach mechanism to prevent NullPointerException crashes
                // The VideoView must never participate in normal view detachment
                try {
                    // Don't call super.dispatchDetachedFromWindow() - this prevents the crash
                    // Instead, handle cleanup manually and safely
                    if (mVideoView != null) {
                        try {
                            if (mVideoView.isPlaying()) {
                                mVideoView.pause(); // Safe pause instead of system-managed cleanup
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "Safe VideoView pause during detach override");
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Safe dispatchDetachedFromWindow override - crash prevented");
                }
            }

            @Override
            public boolean dispatchTouchEvent(android.view.MotionEvent event) {
                // Never consume touch events
                return false;
            }

            @Override
            public boolean onTouchEvent(android.view.MotionEvent event) {
                // Never handle touch events
                return false;
            }
        };

        // Configure VideoView properties
        mVideoView.setZOrderOnTop(false);
        mVideoView.setZOrderMediaOverlay(false);
    }

    /**
     * Create gradient shader for fallback background
     */
    private void createGradientShader() {
        int[] colors = {Color.parseColor("#1a1a1a"), Color.parseColor("#2d2d2d")};
        float[] positions = {0.0f, 1.0f};
        Shader gradient = new LinearGradient(0, 0, 0, getHeight(),
                colors, positions, Shader.TileMode.CLAMP);
        mGradientPaint.setShader(gradient);
    }

    /**
     * Load and start video background
     */
    public boolean loadVideo(Uri videoUri) {
        if (mIsDestroyed || mVideoView == null) {
            Log.w(TAG, "Cannot load video - view destroyed or VideoView null");
            return false;
        }

        try {
            Log.d(TAG, "Loading video: " + videoUri);
            mVideoView.setVideoURI(videoUri);
            mVideoView.setOnPreparedListener(mp -> {
                if (mIsDestroyed) return;

                try {
                    mIsVideoLoaded = true;
                    mp.setLooping(true);
                    mp.setVolume(0f, 0f); // Mute the video

                    // Post setup runnable safely
                    if (mVideoView != null && mVideoView.getHandler() != null) {
                        mVideoView.post(mVideoSetupRunnable);
                    } else {
                        // Fallback: post to main handler
                        mHandler.post(mVideoSetupRunnable);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error preparing video", e);
                    cleanupVideoView();
                }
            });

            mVideoView.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Video playback error: what=" + what + ", extra=" + extra);
                cleanupVideoView();
                return true;
            });

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error loading video", e);
            cleanupVideoView();
            return false;
        }
    }

    /**
     * Setup video view after preparation
     */
    private void setupVideoView() {
        if (mIsDestroyed || mVideoView == null) return;

        try {
            // Start playback
            mVideoView.start();
            Log.d(TAG, "Video playback started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error starting video playback", e);
            cleanupVideoView();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw gradient background as fallback/base layer
        if (getHeight() > 0) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), mGradientPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Update gradient shader when size changes
        if (h > 0) {
            int[] colors = {Color.parseColor("#1a1a1a"), Color.parseColor("#2d2d2d")};
            float[] positions = {0.0f, 1.0f};
            Shader gradient = new LinearGradient(0, 0, 0, h,
                    colors, positions, Shader.TileMode.CLAMP);
            mGradientPaint.setShader(gradient);
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (mIsDestroyed) return;

        mIsDestroyed = true;
        Log.d(TAG, "Cleaning up AdaptiveThemeBackgroundView");

        cleanupVideoView();

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    /**
     * Clean up VideoView safely
     */
    private void cleanupVideoView() {
        if (mVideoView == null) return;

        try {
            // CRITICAL: Remove from parent BEFORE any other operations to prevent dispatchDetachedFromWindow
            if (mVideoView.getParent() != null) {
                ViewGroup parent = (ViewGroup) mVideoView.getParent();
                parent.removeView(mVideoView);
                Log.d(TAG, "VideoView safely removed from parent");
            }

            // Clear all listeners to prevent memory leaks
            mVideoView.setOnCompletionListener(null);
            mVideoView.setOnErrorListener(null);
            mVideoView.setOnPreparedListener(null);
            mVideoView.setOnTouchListener(null);

            // CRITICAL: Safely stop playback without triggering system events
            if (mVideoView.isPlaying()) {
                mVideoView.pause(); // Use pause instead of stopPlayback to avoid system cleanup
            }

            // Suspend to free resources AFTER removal from parent
            try {
                mVideoView.suspend();
            } catch (Exception e) {
                Log.d(TAG, "Safe suspend after parent removal");
            }

            mIsVideoLoaded = false;
            Log.d(TAG, "VideoView cleanup completed safely");

        } catch (Exception e) {
            Log.e(TAG, "Error during VideoView cleanup", e);
        }
    }

    /**
     * Check if video is currently loaded and playing
     */
    public boolean isVideoActive() {
        return mIsVideoLoaded && mVideoView != null && mVideoView.isPlaying();
    }

    @Override
    protected void finalize() throws Throwable {
        cleanup();
        super.finalize();
    }
}
