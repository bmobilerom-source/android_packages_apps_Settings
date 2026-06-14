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
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;

/**
 * Advanced AOD Clock View with multiple animated styles
 */
public class AODClockView extends View {

    private static final String TAG = "AODClockView";

    // Clock styles
    public static final int STYLE_CLASSIC = 0;
    public static final int STYLE_MORPHING = 1;
    public static final int STYLE_PARTICLE = 2;
    public static final int STYLE_LIQUID = 3;
    public static final int STYLE_GEOMETRIC = 4;

    private int mClockStyle = STYLE_CLASSIC;
    private Calendar mCalendar;
    private Paint mPaint;
    private Handler mHandler;
    private Runnable mUpdateRunnable;

    // Animation variables
    private long mStartTime;
    private float mAnimationPhase = 0f;

    // Colors
    private int mPrimaryColor = Color.WHITE;
    private int mSecondaryColor = Color.parseColor("#B0B0B0");
    private int mAccentColor = Color.parseColor("#00BFFF");

    public AODClockView(Context context) {
        super(context);
        init();
    }

    public AODClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AODClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCalendar = Calendar.getInstance();
        mStartTime = System.currentTimeMillis();

        mHandler = new Handler(Looper.getMainLooper());
        mUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateAnimation();
                invalidate();
                mHandler.postDelayed(this, 50); // 20 FPS
            }
        };

        // Start animation loop
        mHandler.post(mUpdateRunnable);
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        mAnimationPhase = (currentTime - mStartTime) / 2000f; // 2 second cycle
        mCalendar.setTimeInMillis(currentTime);
    }

    public void setClockStyle(int style) {
        mClockStyle = style;
        invalidate();
    }

    public void setColors(int primary, int secondary, int accent) {
        mPrimaryColor = primary;
        mSecondaryColor = secondary;
        mAccentColor = accent;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Update time
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        int hours = mCalendar.get(Calendar.HOUR);
        int minutes = mCalendar.get(Calendar.MINUTE);
        int seconds = mCalendar.get(Calendar.SECOND);

        switch (mClockStyle) {
            case STYLE_CLASSIC:
                drawClassicClock(canvas, centerX, centerY, width, height, hours, minutes, seconds);
                break;
            case STYLE_MORPHING:
                drawMorphingClock(canvas, centerX, centerY, width, height, hours, minutes, seconds);
                break;
            case STYLE_PARTICLE:
                drawParticleClock(canvas, centerX, centerY, width, height, hours, minutes, seconds);
                break;
            case STYLE_LIQUID:
                drawLiquidClock(canvas, centerX, centerY, width, height, hours, minutes, seconds);
                break;
            case STYLE_GEOMETRIC:
                drawGeometricClock(canvas, centerX, centerY, width, height, hours, minutes, seconds);
                break;
        }
    }

    private void drawClassicClock(Canvas canvas, int centerX, int centerY, int width, int height,
                                int hours, int minutes, int seconds) {
        float radius = Math.min(width, height) * 0.35f;

        // Draw clock circle
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3f);
        mPaint.setColor(mSecondaryColor);
        canvas.drawCircle(centerX, centerY, radius, mPaint);

        // Draw hour markers
        mPaint.setStrokeWidth(2f);
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI * 2 * i / 12 - Math.PI / 2;
            float startX = centerX + (float) (radius * 0.85 * Math.cos(angle));
            float startY = centerY + (float) (radius * 0.85 * Math.sin(angle));
            float endX = centerX + (float) (radius * 0.95 * Math.cos(angle));
            float endY = centerY + (float) (radius * 0.95 * Math.sin(angle));
            canvas.drawLine(startX, startY, endX, endY, mPaint);
        }

        // Draw minute markers
        mPaint.setStrokeWidth(1f);
        for (int i = 0; i < 60; i++) {
            if (i % 5 != 0) { // Skip hour markers
                double angle = Math.PI * 2 * i / 60 - Math.PI / 2;
                float startX = centerX + (float) (radius * 0.9 * Math.cos(angle));
                float startY = centerY + (float) (radius * 0.9 * Math.sin(angle));
                float endX = centerX + (float) (radius * 0.95 * Math.cos(angle));
                float endY = centerY + (float) (radius * 0.95 * Math.sin(angle));
                canvas.drawLine(startX, startY, endX, endY, mPaint);
            }
        }

        // Draw hour hand
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mPrimaryColor);
        mPaint.setStrokeWidth(6f);
        double hourAngle = Math.PI * 2 * ((hours % 12) + minutes / 60.0) / 12 - Math.PI / 2;
        canvas.drawLine(centerX, centerY,
                      centerX + (float) (radius * 0.5 * Math.cos(hourAngle)),
                      centerY + (float) (radius * 0.5 * Math.sin(hourAngle)), mPaint);

        // Draw minute hand
        mPaint.setStrokeWidth(4f);
        double minuteAngle = Math.PI * 2 * minutes / 60 - Math.PI / 2;
        canvas.drawLine(centerX, centerY,
                      centerX + (float) (radius * 0.7 * Math.cos(minuteAngle)),
                      centerY + (float) (radius * 0.7 * Math.sin(minuteAngle)), mPaint);

        // Draw second hand
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(mAccentColor);
        double secondAngle = Math.PI * 2 * seconds / 60 - Math.PI / 2;
        canvas.drawLine(centerX, centerY,
                      centerX + (float) (radius * 0.8 * Math.cos(secondAngle)),
                      centerY + (float) (radius * 0.8 * Math.sin(secondAngle)), mPaint);

        // Draw center dot
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mPrimaryColor);
        canvas.drawCircle(centerX, centerY, 4f, mPaint);
    }

    private void drawMorphingClock(Canvas canvas, int centerX, int centerY, int width, int height,
                                 int hours, int minutes, int seconds) {
        float radius = Math.min(width, height) * 0.4f;

        // Calculate morphing factor based on seconds
        float morphFactor = (float) (Math.sin(mAnimationPhase * Math.PI * 2) + 1) / 2; // 0 to 1

        // Draw morphing background
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.argb((int) (128 * morphFactor), Color.red(mAccentColor),
                                  Color.green(mAccentColor), Color.blue(mAccentColor)));
        canvas.drawCircle(centerX, centerY, radius * (0.8f + morphFactor * 0.2f), mPaint);

        // Draw morphing numbers
        mPaint.setColor(mPrimaryColor);
        mPaint.setTextSize(24f + morphFactor * 12f);
        mPaint.setTextAlign(Paint.Align.CENTER);

        String timeString = String.format("%02d:%02d", hours == 0 ? 12 : hours, minutes);
        canvas.drawText(timeString, centerX, centerY + 8, mPaint);

        // Draw morphing particles around the clock
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI * 2 * i / 12 + mAnimationPhase;
            float x = centerX + (float) (radius * 1.2 * Math.cos(angle));
            float y = centerY + (float) (radius * 1.2 * Math.sin(angle));
            float particleSize = 3f + (float) Math.sin(mAnimationPhase * 2 + i) * 2f;
            canvas.drawCircle(x, y, particleSize, mPaint);
        }
    }

    private void drawParticleClock(Canvas canvas, int centerX, int centerY, int width, int height,
                                 int hours, int minutes, int seconds) {
        float radius = Math.min(width, height) * 0.35f;

        // Draw time as particles
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mPrimaryColor);

        // Convert time to particle positions
        int totalMinutes = hours * 60 + minutes;
        int totalParticles = 24; // 24 particles for 24 hours

        for (int i = 0; i < totalParticles; i++) {
            double angle = Math.PI * 2 * i / totalParticles - Math.PI / 2;
            float x = centerX + (float) (radius * Math.cos(angle));
            float y = centerY + (float) (radius * Math.sin(angle));

            // Highlight particles based on current time
            if (i < hours) {
                mPaint.setColor(mAccentColor);
                canvas.drawCircle(x, y, 6f, mPaint);
            } else {
                mPaint.setColor(mSecondaryColor);
                canvas.drawCircle(x, y, 4f, mPaint);
            }
        }

        // Draw minute indicator
        double minuteAngle = Math.PI * 2 * minutes / 60 - Math.PI / 2;
        float minuteX = centerX + (float) (radius * 0.7 * Math.cos(minuteAngle));
        float minuteY = centerY + (float) (radius * 0.7 * Math.sin(minuteAngle));
        mPaint.setColor(mPrimaryColor);
        canvas.drawCircle(minuteX, minuteY, 8f, mPaint);

        // Draw animated particles
        for (int i = 0; i < 20; i++) {
            double angle = mAnimationPhase * 2 + i * Math.PI / 10;
            float x = centerX + (float) (radius * 1.5 * Math.cos(angle));
            float y = centerY + (float) (radius * 1.5 * Math.sin(angle));
            float alpha = (float) (Math.sin(mAnimationPhase * 3 + i) + 1) / 2;
            mPaint.setColor(Color.argb((int) (255 * alpha), Color.red(mAccentColor),
                                     Color.green(mAccentColor), Color.blue(mAccentColor)));
            canvas.drawCircle(x, y, 2f, mPaint);
        }
    }

    private void drawLiquidClock(Canvas canvas, int centerX, int centerY, int width, int height,
                               int hours, int minutes, int seconds) {
        float radius = Math.min(width, height) * 0.4f;

        // Create liquid effect with bezier curves
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mPrimaryColor);

        // Draw time as liquid droplets
        String timeString = String.format("%02d:%02d", hours == 0 ? 12 : hours, minutes);
        mPaint.setTextSize(32f);
        mPaint.setTextAlign(Paint.Align.CENTER);

        // Create liquid effect around text
        Rect textBounds = new Rect();
        mPaint.getTextBounds(timeString, 0, timeString.length(), textBounds);

        float textWidth = textBounds.width();
        float textHeight = textBounds.height();

        // Draw liquid background
        float liquidOffset = (float) Math.sin(mAnimationPhase * Math.PI * 2) * 5f;
        RectF liquidRect = new RectF(
            centerX - textWidth / 2 - 10,
            centerY - textHeight / 2 - 10 + liquidOffset,
            centerX + textWidth / 2 + 10,
            centerY + textHeight / 2 + 10 + liquidOffset
        );

        // Create liquid shape with bezier curves
        Path liquidPath = new Path();
        liquidPath.moveTo(liquidRect.left, liquidRect.top);
        liquidPath.lineTo(liquidRect.right, liquidRect.top);
        liquidPath.lineTo(liquidRect.right, liquidRect.bottom - 10);
        liquidPath.cubicTo(
            liquidRect.right - 10, liquidRect.bottom,
            liquidRect.left + 10, liquidRect.bottom,
            liquidRect.left, liquidRect.bottom - 10
        );
        liquidPath.close();

        mPaint.setColor(Color.argb(128, Color.red(mAccentColor),
                                  Color.green(mAccentColor), Color.blue(mAccentColor)));
        canvas.drawPath(liquidPath, mPaint);

        // Draw time text
        mPaint.setColor(mPrimaryColor);
        canvas.drawText(timeString, centerX, centerY + 8, mPaint);

        // Draw liquid droplets
        for (int i = 0; i < 5; i++) {
            float dropletX = centerX + (float) (Math.cos(mAnimationPhase * 2 + i) * (textWidth / 2 + 20));
            float dropletY = centerY + (float) (Math.sin(mAnimationPhase * 1.5 + i) * 20);
            float dropletSize = 3f + (float) Math.sin(mAnimationPhase * 3 + i) * 2f;
            canvas.drawCircle(dropletX, dropletY, dropletSize, mPaint);
        }
    }

    private void drawGeometricClock(Canvas canvas, int centerX, int centerY, int width, int height,
                                  int hours, int minutes, int seconds) {
        float size = Math.min(width, height) * 0.35f;

        // Draw time as geometric shapes
        mPaint.setStyle(Paint.Style.FILL);

        // Hours as triangles
        for (int i = 0; i < hours; i++) {
            float angle = (float) (Math.PI * 2 * i / 12 - Math.PI / 2);
            float x = centerX + (float) (size * 0.8 * Math.cos(angle));
            float y = centerY + (float) (size * 0.8 * Math.sin(angle));

            // Draw triangle
            Path triangle = new Path();
            triangle.moveTo(x, y - 8);
            triangle.lineTo(x - 6, y + 8);
            triangle.lineTo(x + 6, y + 8);
            triangle.close();

            mPaint.setColor(Color.argb(255 - i * 20, Color.red(mPrimaryColor),
                                     Color.green(mPrimaryColor), Color.blue(mPrimaryColor)));
            canvas.drawPath(triangle, mPaint);
        }

        // Minutes as squares
        int squaresToDraw = minutes / 5; // One square per 5 minutes
        for (int i = 0; i < squaresToDraw; i++) {
            float angle = (float) (Math.PI * 2 * i / 12 + Math.PI / 6); // Offset from triangles
            float x = centerX + (float) (size * 1.0 * Math.cos(angle));
            float y = centerY + (float) (size * 1.0 * Math.sin(angle));

            mPaint.setColor(mSecondaryColor);
            canvas.drawRect(x - 4, y - 4, x + 4, y + 4, mPaint);
        }

        // Seconds as animated circles
        for (int i = 0; i < seconds / 5; i++) { // One circle per 5 seconds
            float angle = (float) (Math.PI * 2 * i / 12 + Math.PI / 3 + mAnimationPhase); // Offset and animate
            float x = centerX + (float) (size * 1.2 * Math.cos(angle));
            float y = centerY + (float) (size * 1.2 * Math.sin(angle));

            float circleSize = 3f + (float) Math.sin(mAnimationPhase * 2 + i) * 2f;
            mPaint.setColor(mAccentColor);
            canvas.drawCircle(x, y, circleSize, mPaint);
        }

        // Draw center geometric shape
        mPaint.setColor(mPrimaryColor);
        canvas.drawCircle(centerX, centerY, 6f, mPaint);

        // Draw rotating outer ring
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        RectF outerRect = new RectF(centerX - size * 1.3f, centerY - size * 1.3f,
                                   centerX + size * 1.3f, centerY + size * 1.3f);
        canvas.drawArc(outerRect, mAnimationPhase * 360, 90, false, mPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHandler != null && mUpdateRunnable != null) {
            mHandler.removeCallbacks(mUpdateRunnable);
        }
    }
}



