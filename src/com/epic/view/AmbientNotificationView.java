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

package com.epic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.epic.ambient.NotificationPreviewManager;

/**
 * Interactive Ambient Notification View
 * Displays smart notification previews with category-based styling
 */
public class AmbientNotificationView extends View {

    private static final String TAG = "AmbientNotificationView";

    private NotificationPreviewManager.NotificationPreview mPreview;
    private Paint mBackgroundPaint;
    private Paint mTextPaint;
    private Paint mIconPaint;
    private TextView mTextView;

    // Colors for different categories
    private static final int COLOR_MESSAGE = Color.parseColor("#FF9800");    // Orange
    private static final int COLOR_CALL = Color.parseColor("#F44336");       // Red
    private static final int COLOR_EMAIL = Color.parseColor("#2196F3");      // Blue
    private static final int COLOR_SOCIAL = Color.parseColor("#E91E63");     // Pink
    private static final int COLOR_SYSTEM = Color.parseColor("#9C27B0");     // Purple
    private static final int COLOR_REMINDER = Color.parseColor("#4CAF50");   // Green
    private static final int COLOR_UNKNOWN = Color.parseColor("#607D8B");    // Blue Grey

    private int mBackgroundColor = COLOR_UNKNOWN;
    private boolean mIsCompact = false;

    public AmbientNotificationView(Context context) {
        super(context);
        init();
    }

    public AmbientNotificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AmbientNotificationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(14f);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIconPaint.setColor(Color.WHITE);
        mIconPaint.setTextSize(16f);
        mIconPaint.setTextAlign(Paint.Align.CENTER);

        // Create text view for better text rendering
        mTextView = new TextView(getContext());
        mTextView.setTextColor(Color.WHITE);
        mTextView.setTextSize(12f);
        mTextView.setMaxLines(2);
        mTextView.setEllipsize(android.text.TextUtils.TruncateAt.END);
    }

    public void setNotificationPreview(NotificationPreviewManager.NotificationPreview preview) {
        mPreview = preview;
        updateStyling();
        invalidate();
    }

    public void setCompactMode(boolean compact) {
        mIsCompact = compact;
        updateStyling();
        invalidate();
    }

    private void updateStyling() {
        if (mPreview == null) return;

        // Set background color based on category
        switch (mPreview.category) {
            case NotificationPreviewManager.CATEGORY_MESSAGE:
                mBackgroundColor = COLOR_MESSAGE;
                break;
            case NotificationPreviewManager.CATEGORY_CALL:
                mBackgroundColor = COLOR_CALL;
                break;
            case NotificationPreviewManager.CATEGORY_EMAIL:
                mBackgroundColor = COLOR_EMAIL;
                break;
            case NotificationPreviewManager.CATEGORY_SOCIAL:
                mBackgroundColor = COLOR_SOCIAL;
                break;
            case NotificationPreviewManager.CATEGORY_SYSTEM:
                mBackgroundColor = COLOR_SYSTEM;
                break;
            case NotificationPreviewManager.CATEGORY_REMINDER:
                mBackgroundColor = COLOR_REMINDER;
                break;
            default:
                mBackgroundColor = COLOR_UNKNOWN;
                break;
        }

        // Update text view
        if (mTextView != null && mPreview.smartText != null) {
            mTextView.setText(mPreview.smartText);

            // Adjust text size for compact mode
            if (mIsCompact) {
                mTextView.setTextSize(10f);
                mTextView.setMaxLines(1);
            } else {
                mTextView.setTextSize(12f);
                mTextView.setMaxLines(2);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mPreview == null) return;

        int width = getWidth();
        int height = getHeight();

        // Draw background with rounded corners
        mBackgroundPaint.setColor(mBackgroundColor);
        RectF backgroundRect = new RectF(0, 0, width, height);
        float cornerRadius = mIsCompact ? 8f : 12f;
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, mBackgroundPaint);

        // Draw category indicator
        drawCategoryIndicator(canvas, width, height);

        // Draw notification text
        drawNotificationText(canvas, width, height);
    }

    private void drawCategoryIndicator(Canvas canvas, int width, int height) {
        // Draw small indicator dot
        float indicatorSize = mIsCompact ? 6f : 8f;
        float indicatorX = 12f;
        float indicatorY = height / 2f;

        mIconPaint.setColor(Color.WHITE);
        canvas.drawCircle(indicatorX, indicatorY, indicatorSize, mIconPaint);
    }

    private void drawNotificationText(Canvas canvas, int width, int height) {
        if (mPreview.smartText == null) return;

        float textX = mIsCompact ? 24f : 32f;
        float textY = height / 2f + 4f;

        // Draw emoji icon if present
        String text = mPreview.smartText;
        String emoji = extractEmoji(text);
        if (!emoji.isEmpty()) {
            // Draw emoji separately for better rendering
            mIconPaint.setColor(Color.WHITE);
            canvas.drawText(emoji, textX, textY, mIconPaint);

            // Adjust text position
            textX += 24f;
            text = text.substring(emoji.length()).trim();
        }

        // Draw text
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mIsCompact ? 12f : 14f);

        // Handle text wrapping for non-compact mode
        if (!mIsCompact && text.length() > 20) {
            String line1 = text.substring(0, Math.min(20, text.length()));
            String line2 = text.length() > 20 ? text.substring(20) : "";

            canvas.drawText(line1, textX, textY - 8, mTextPaint);
            if (!line2.isEmpty()) {
                canvas.drawText(line2, textX, textY + 8, mTextPaint);
            }
        } else {
            canvas.drawText(text, textX, textY, mTextPaint);
        }
    }

    private String extractEmoji(String text) {
        if (text == null || text.isEmpty()) return "";

        // Check for common emoji patterns at the start
        String[] emojiPrefixes = {"💬", "📞", "📧", "📱", "🔧", "⏰", "📧", "❤️", "👍", "🔥", "🎉"};
        for (String emoji : emojiPrefixes) {
            if (text.startsWith(emoji)) {
                return emoji;
            }
        }

        return "";
    }

    /**
     * Get the background color for this notification category
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * Check if this notification should be interactive
     */
    public boolean isInteractive() {
        return mPreview != null && mPreview.isInteractive;
    }

    /**
     * Get the notification category
     */
    public int getCategory() {
        return mPreview != null ? mPreview.category : NotificationPreviewManager.CATEGORY_UNKNOWN;
    }
}



