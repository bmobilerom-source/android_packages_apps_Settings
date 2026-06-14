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
 */

package com.bmobile.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Random;

/**
 * Ambient Music Visualizer for AOD
 * Provides real-time audio visualizations with multiple modes
 */
public class AmbientMusicVisualizer extends View implements Visualizer.OnDataCaptureListener {

    private static final String TAG = "AmbientMusicVisualizer";

    // Visualization modes
    public static final int MODE_SPECTRUM = 0;
    public static final int MODE_WAVEFORM = 1;
    public static final int MODE_PARTICLES = 2;
    public static final int MODE_CIRCLES = 3;
    public static final int MODE_BARS = 4;

    private int mVisualizationMode = MODE_SPECTRUM;
    private Visualizer mVisualizer;
    private Paint mPaint;
    private Paint mBackgroundPaint;
    private Handler mHandler;
    private Runnable mUpdateRunnable;

    // Audio data
    private byte[] mWaveformData;
    private byte[] mFftData;
    private float[] mMagnitudes;

    // Animation
    private long mStartTime;
    private float mAnimationPhase = 0f;

    // Colors
    private int mPrimaryColor = Color.parseColor("#00BFFF");
    private int mSecondaryColor = Color.parseColor("#FF1493");
    private int mAccentColor = Color.parseColor("#32CD32");

    // Visualization parameters
    private int mNumBars = 32;
    private float mBarWidth;
    private float mBarSpacing;
    private Random mRandom = new Random();

    // Particle system for particle mode
    private static final int MAX_PARTICLES = 50;
    private float[] mParticleX = new float[MAX_PARTICLES];
    private float[] mParticleY = new float[MAX_PARTICLES];
    private float[] mParticleVX = new float[MAX_PARTICLES];
    private float[] mParticleVY = new float[MAX_PARTICLES];
    private int[] mParticleLife = new int[MAX_PARTICLES];

    public AmbientMusicVisualizer(Context context) {
        super(context);
        init();
    }

    public AmbientMusicVisualizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AmbientMusicVisualizer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mStartTime = System.currentTimeMillis();

        mHandler = new Handler(Looper.getMainLooper());
        mUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateAnimation();
                invalidate();
                mHandler.postDelayed(this, 50); // 20 FPS for smooth animation
            }
        };

        // Initialize particle system
        initParticles();

        // Try to setup visualizer
        setupVisualizer();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mHandler != null && mUpdateRunnable != null) {
            mHandler.post(mUpdateRunnable);
        }
    }

    private void initParticles() {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            mParticleX[i] = mRandom.nextFloat() * getWidth();
            mParticleY[i] = mRandom.nextFloat() * getHeight();
            mParticleVX[i] = (mRandom.nextFloat() - 0.5f) * 4f;
            mParticleVY[i] = (mRandom.nextFloat() - 0.5f) * 4f;
            mParticleLife[i] = mRandom.nextInt(100) + 50;
        }
    }

    private void setupVisualizer() {
        try {
            // Get audio session ID from media player if available
            int audioSessionId = 0;

            // Try to find an active media player
            AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null && audioManager.isMusicActive()) {
                // Music is playing, try to get session
                mVisualizer = new Visualizer(audioSessionId != 0 ? audioSessionId : 0);
                mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                mVisualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate() / 2, true, true);
                mVisualizer.setEnabled(true);
                Log.d(TAG, "Visualizer setup successful");
            } else {
                Log.d(TAG, "No active music playback detected");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup visualizer", e);
            mVisualizer = null;
        }
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        mWaveformData = waveform;
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        mFftData = fft;
        computeMagnitudes();
    }

    private void computeMagnitudes() {
        if (mFftData == null) return;

        int n = mFftData.length / 2;
        if (mMagnitudes == null || mMagnitudes.length != n) {
            mMagnitudes = new float[n];
        }

        for (int i = 0; i < n; i++) {
            float re = mFftData[i * 2];
            float im = mFftData[i * 2 + 1];
            mMagnitudes[i] = (float) Math.sqrt(re * re + im * im);
        }
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        mAnimationPhase = (currentTime - mStartTime) / 2000f; // 2 second cycle

        // Update particles for particle mode
        if (mVisualizationMode == MODE_PARTICLES) {
            updateParticles();
        }
    }

    private void updateParticles() {
        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < MAX_PARTICLES; i++) {
            // Update position
            mParticleX[i] += mParticleVX[i];
            mParticleY[i] += mParticleVY[i];

            // Bounce off walls
            if (mParticleX[i] < 0 || mParticleX[i] > width) {
                mParticleVX[i] *= -1;
                mParticleX[i] = Math.max(0, Math.min(width, mParticleX[i]));
            }
            if (mParticleY[i] < 0 || mParticleY[i] > height) {
                mParticleVY[i] *= -1;
                mParticleY[i] = Math.max(0, Math.min(height, mParticleY[i]));
            }

            // Update life
            mParticleLife[i]--;
            if (mParticleLife[i] <= 0) {
                // Respawn particle
                mParticleX[i] = width / 2f;
                mParticleY[i] = height / 2f;
                mParticleVX[i] = (mRandom.nextFloat() - 0.5f) * 4f;
                mParticleVY[i] = (mRandom.nextFloat() - 0.5f) * 4f;
                mParticleLife[i] = mRandom.nextInt(100) + 50;
            }
        }
    }

    public void setVisualizationMode(int mode) {
        mVisualizationMode = mode;
        invalidate();
    }

    public void setColors(int primary, int secondary, int accent) {
        mPrimaryColor = primary;
        mSecondaryColor = secondary;
        mAccentColor = accent;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBarWidth = w / (float) mNumBars * 0.8f;
        mBarSpacing = w / (float) mNumBars * 0.2f;

        // Reinitialize particles with new size
        initParticles();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        switch (mVisualizationMode) {
            case MODE_SPECTRUM:
                drawSpectrum(canvas, width, height);
                break;
            case MODE_WAVEFORM:
                drawWaveform(canvas, width, height);
                break;
            case MODE_PARTICLES:
                drawParticles(canvas, width, height);
                break;
            case MODE_CIRCLES:
                drawCircles(canvas, width, height);
                break;
            case MODE_BARS:
                drawBars(canvas, width, height);
                break;
        }
    }

    private void drawSpectrum(Canvas canvas, int width, int height) {
        if (mMagnitudes == null || mMagnitudes.length == 0) {
            drawFallbackAnimation(canvas, width, height);
            return;
        }

        mPaint.setStyle(Paint.Style.FILL);

        int barsToDraw = Math.min(mNumBars, mMagnitudes.length / 2);
        float barWidth = width / (float) barsToDraw;

        for (int i = 0; i < barsToDraw; i++) {
            float magnitude = mMagnitudes[i] / 128f; // Normalize
            float barHeight = magnitude * height * 0.8f;

            // Create gradient for each bar
            LinearGradient gradient = new LinearGradient(
                0, height - barHeight, 0, height,
                new int[]{mPrimaryColor, mAccentColor},
                null, Shader.TileMode.CLAMP
            );
            mPaint.setShader(gradient);

            float left = i * barWidth + barWidth * 0.1f;
            float right = (i + 1) * barWidth - barWidth * 0.1f;
            float top = height - barHeight;
            float bottom = height;

            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    private void drawWaveform(Canvas canvas, int width, int height) {
        if (mWaveformData == null || mWaveformData.length == 0) {
            drawFallbackAnimation(canvas, width, height);
            return;
        }

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3f);
        mPaint.setColor(mPrimaryColor);
        mPaint.setShader(null);

        Path path = new Path();
        float centerY = height / 2f;
        float scaleY = height / 512f; // 8-bit audio

        path.moveTo(0, centerY);
        for (int i = 0; i < mWaveformData.length; i++) {
            float x = (float) i / mWaveformData.length * width;
            float y = centerY + (mWaveformData[i] + 128) * scaleY - 128 * scaleY;
            path.lineTo(x, y);
        }

        canvas.drawPath(path, mPaint);

        // Add glow effect
        mPaint.setStrokeWidth(6f);
        mPaint.setAlpha(100);
        canvas.drawPath(path, mPaint);
    }

    private void drawParticles(Canvas canvas, int width, int height) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setShader(null);

        for (int i = 0; i < MAX_PARTICLES; i++) {
            float alpha = mParticleLife[i] / 150f; // Fade out as life decreases
            int color = Color.argb((int) (255 * alpha),
                                 Color.red(mPrimaryColor),
                                 Color.green(mPrimaryColor),
                                 Color.blue(mPrimaryColor));
            mPaint.setColor(color);

            float size = 3f + alpha * 4f;
            canvas.drawCircle(mParticleX[i], mParticleY[i], size, mPaint);
        }
    }

    private void drawCircles(Canvas canvas, int width, int height) {
        float centerX = width / 2f;
        float centerY = height / 2f;

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setShader(null);

        int numCircles = 5;
        for (int i = 0; i < numCircles; i++) {
            float radius = (i + 1) * 20f + (float) Math.sin(mAnimationPhase * 2 + i) * 10f;
            float alpha = 255 - i * 40;

            mPaint.setColor(Color.argb(alpha,
                                     Color.red(mPrimaryColor),
                                     Color.green(mPrimaryColor),
                                     Color.blue(mPrimaryColor)));
            mPaint.setStrokeWidth(2f + i);

            canvas.drawCircle(centerX, centerY, radius, mPaint);
        }
    }

    private void drawBars(Canvas canvas, int width, int height) {
        mPaint.setStyle(Paint.Style.FILL);

        float barWidth = width / (float) mNumBars;
        float centerY = height / 2f;

        for (int i = 0; i < mNumBars; i++) {
            // Create animated height even without real audio data
            float baseHeight = height * 0.3f;
            float animation = (float) Math.sin(mAnimationPhase * 3 + i * 0.5f) * 0.5f + 0.5f;
            float barHeight = baseHeight + animation * height * 0.4f;

            // Alternate colors
            int color = (i % 3 == 0) ? mPrimaryColor :
                       (i % 3 == 1) ? mSecondaryColor : mAccentColor;
            mPaint.setColor(color);

            float left = i * barWidth + barWidth * 0.1f;
            float right = (i + 1) * barWidth - barWidth * 0.1f;
            float top = centerY - barHeight / 2;
            float bottom = centerY + barHeight / 2;

            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    private void drawFallbackAnimation(Canvas canvas, int width, int height) {
        // Fallback animation when no audio data is available
        float centerX = width / 2f;
        float centerY = height / 2f;

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mPrimaryColor);

        // Draw animated circles as fallback
        for (int i = 0; i < 3; i++) {
            float radius = 20f + (float) Math.sin(mAnimationPhase * 2 + i * 2) * 10f;
            float alpha = (int) (128 + Math.sin(mAnimationPhase + i) * 127);
            mPaint.setColor(Color.argb(alpha,
                                     Color.red(mPrimaryColor),
                                     Color.green(mPrimaryColor),
                                     Color.blue(mPrimaryColor)));

            canvas.drawCircle(centerX + i * 40 - 40, centerY, radius, mPaint);
        }
    }

    public void setNumBars(int numBars) {
        mNumBars = numBars;
        mBarWidth = getWidth() / (float) mNumBars * 0.8f;
        mBarSpacing = getWidth() / (float) mNumBars * 0.2f;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHandler != null && mUpdateRunnable != null) {
            mHandler.removeCallbacks(mUpdateRunnable);
        }
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
    }
}



