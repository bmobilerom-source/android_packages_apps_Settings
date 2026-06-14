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

package com.bmobile.ambient;

import android.content.Context;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Smart Notification Preview Manager
 * Handles intelligent notification content analysis and summarization for AOD
 */
public class NotificationPreviewManager {

    private static final String TAG = "NotificationPreviewManager";

    // Notification categories
    public static final int CATEGORY_UNKNOWN = 0;
    public static final int CATEGORY_MESSAGE = 1;
    public static final int CATEGORY_CALL = 2;
    public static final int CATEGORY_EMAIL = 3;
    public static final int CATEGORY_SOCIAL = 4;
    public static final int CATEGORY_SYSTEM = 5;
    public static final int CATEGORY_REMINDER = 6;

    // Preview types
    public static final int PREVIEW_TYPE_COMPACT = 0;
    public static final int PREVIEW_TYPE_SUMMARY = 1;
    public static final int PREVIEW_TYPE_SMART = 2;

    private Context mContext;
    private Map<String, Integer> mAppCategoryCache = new HashMap<>();

    public NotificationPreviewManager(Context context) {
        mContext = context;
    }

    /**
     * Analyze notification and return smart preview data
     */
    public NotificationPreview analyzeNotification(StatusBarNotification sbn) {
        NotificationPreview preview = new NotificationPreview();

        try {
            // Basic notification data
            preview.packageName = sbn.getPackageName();
            preview.title = sbn.getNotification().extras.getString("android.title", "");
            preview.text = sbn.getNotification().extras.getString("android.text", "");
            preview.subText = sbn.getNotification().extras.getString("android.subText", "");

            // Determine category
            preview.category = determineCategory(sbn);

            // Generate smart preview based on category
            generateSmartPreview(preview);

            // Apply privacy filtering
            applyPrivacyFilter(preview);

            Log.d(TAG, "Analyzed notification: " + preview.packageName +
                  " -> category: " + preview.category + ", preview: " + preview.smartText);

        } catch (Exception e) {
            Log.e(TAG, "Error analyzing notification", e);
            // Return basic fallback
            preview.smartText = preview.title != null ? preview.title : "Notification";
            preview.category = CATEGORY_UNKNOWN;
        }

        return preview;
    }

    /**
     * Determine notification category based on package and content
     */
    private int determineCategory(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString("android.title", "").toLowerCase();
        String text = sbn.getNotification().extras.getString("android.text", "").toLowerCase();

        // Check cache first
        if (mAppCategoryCache.containsKey(packageName)) {
            return mAppCategoryCache.get(packageName);
        }

        int category = CATEGORY_UNKNOWN;

        // Messaging apps
        if (packageName.contains("messenger") || packageName.contains("messages") ||
            packageName.contains("whatsapp") || packageName.contains("telegram") ||
            packageName.contains("signal") || packageName.contains("sms")) {
            category = CATEGORY_MESSAGE;
        }
        // Email apps
        else if (packageName.contains("email") || packageName.contains("gmail") ||
                 packageName.contains("outlook") || packageName.contains("k9") ||
                 packageName.contains("mail")) {
            category = CATEGORY_EMAIL;
        }
        // Social apps
        else if (packageName.contains("facebook") || packageName.contains("instagram") ||
                 packageName.contains("twitter") || packageName.contains("tiktok") ||
                 packageName.contains("snapchat")) {
            category = CATEGORY_SOCIAL;
        }
        // Call/dialer apps
        else if (packageName.contains("dialer") || packageName.contains("phone") ||
                 packageName.contains("contacts") || packageName.contains("call")) {
            category = CATEGORY_CALL;
        }
        // System notifications
        else if (packageName.startsWith("android") || packageName.startsWith("com.android") ||
                 packageName.contains("system") || packageName.contains("settings")) {
            category = CATEGORY_SYSTEM;
        }
        // Calendar/reminder apps
        else if (packageName.contains("calendar") || packageName.contains("clock") ||
                 packageName.contains("alarm") || title.contains("reminder") ||
                 title.contains("appointment") || title.contains("meeting")) {
            category = CATEGORY_REMINDER;
        }
        // Content-based detection
        else {
            if (title.contains("message") || title.contains("chat") || text.contains("message")) {
                category = CATEGORY_MESSAGE;
            } else if (title.contains("call") || title.contains("phone") || text.contains("call")) {
                category = CATEGORY_CALL;
            } else if (title.contains("email") || text.contains("email")) {
                category = CATEGORY_EMAIL;
            }
        }

        // Cache the result
        mAppCategoryCache.put(packageName, category);

        return category;
    }

    /**
     * Generate smart preview text based on notification category
     */
    private void generateSmartPreview(NotificationPreview preview) {
        String originalText = preview.text;
        if (TextUtils.isEmpty(originalText)) {
            originalText = preview.subText;
        }

        switch (preview.category) {
            case CATEGORY_MESSAGE:
                generateMessagePreview(preview, originalText);
                break;
            case CATEGORY_CALL:
                generateCallPreview(preview, originalText);
                break;
            case CATEGORY_EMAIL:
                generateEmailPreview(preview, originalText);
                break;
            case CATEGORY_SOCIAL:
                generateSocialPreview(preview, originalText);
                break;
            case CATEGORY_REMINDER:
                generateReminderPreview(preview, originalText);
                break;
            case CATEGORY_SYSTEM:
                generateSystemPreview(preview, originalText);
                break;
            default:
                generateGenericPreview(preview, originalText);
                break;
        }
    }

    private void generateMessagePreview(NotificationPreview preview, String text) {
        if (TextUtils.isEmpty(text)) {
            preview.smartText = "💬 New message";
            return;
        }

        // For messages, show first line or summarize
        String[] lines = text.split("\\n");
        if (lines.length > 1) {
            // Multi-line message - show sender and preview
            preview.smartText = "💬 " + lines[0];
            if (lines[0].length() > 25) {
                preview.smartText = "💬 " + lines[0].substring(0, 22) + "...";
            }
        } else {
            // Single line - show as is
            preview.smartText = "💬 " + (text.length() > 25 ? text.substring(0, 22) + "..." : text);
        }
    }

    private void generateCallPreview(NotificationPreview preview, String text) {
        preview.smartText = "📞 Incoming call";
        if (!TextUtils.isEmpty(preview.title) && !preview.title.equals("Phone")) {
            preview.smartText = "📞 " + preview.title;
        }
    }

    private void generateEmailPreview(NotificationPreview preview, String text) {
        if (TextUtils.isEmpty(text)) {
            preview.smartText = "📧 New email";
            return;
        }

        // Extract sender if possible
        String sender = extractEmailSender(text);
        preview.smartText = "📧 " + (sender != null ? sender : "New email");
    }

    private void generateSocialPreview(NotificationPreview preview, String text) {
        if (TextUtils.isEmpty(text)) {
            preview.smartText = "📱 Social update";
            return;
        }

        // Social notifications are often short
        preview.smartText = "📱 " + (text.length() > 25 ? text.substring(0, 22) + "..." : text);
    }

    private void generateReminderPreview(NotificationPreview preview, String text) {
        preview.smartText = "⏰ Reminder";
        if (!TextUtils.isEmpty(preview.title)) {
            preview.smartText = "⏰ " + preview.title;
        }
    }

    private void generateSystemPreview(NotificationPreview preview, String text) {
        if (TextUtils.isEmpty(text)) {
            preview.smartText = "🔧 System";
            return;
        }

        // System notifications are usually important but concise
        preview.smartText = "🔧 " + (text.length() > 25 ? text.substring(0, 22) + "..." : text);
    }

    private void generateGenericPreview(NotificationPreview preview, String text) {
        if (TextUtils.isEmpty(text)) {
            preview.smartText = preview.title != null ? preview.title : "Notification";
        } else {
            preview.smartText = text.length() > 25 ? text.substring(0, 22) + "..." : text;
        }
    }

    /**
     * Extract email sender from email notification text
     */
    private String extractEmailSender(String text) {
        if (TextUtils.isEmpty(text)) return null;

        // Look for common email patterns
        Pattern pattern = Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
        java.util.regex.Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String email = matcher.group(1);
            // Extract name part before @
            int atIndex = email.indexOf('@');
            if (atIndex > 0) {
                return email.substring(0, atIndex);
            }
            return email;
        }

        return null;
    }

    /**
     * Apply privacy filtering to notification preview
     */
    private void applyPrivacyFilter(NotificationPreview preview) {
        // Remove sensitive information patterns
        if (preview.smartText != null) {
            // Remove phone numbers
            preview.smartText = preview.smartText.replaceAll("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b", "[phone]");
            // Remove email addresses (additional filtering)
            preview.smartText = preview.smartText.replaceAll("\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b", "[email]");
            // Remove potential credit card numbers
            preview.smartText = preview.smartText.replaceAll("\\b\\d{4}[ -]?\\d{4}[ -]?\\d{4}[ -]?\\d{4}\\b", "[card]");
        }
    }

    /**
     * Get preview type recommendation based on notification
     */
    public int getRecommendedPreviewType(NotificationPreview preview) {
        switch (preview.category) {
            case CATEGORY_MESSAGE:
                return PREVIEW_TYPE_SUMMARY; // Messages need summarization
            case CATEGORY_CALL:
                return PREVIEW_TYPE_COMPACT; // Calls are urgent, keep compact
            case CATEGORY_EMAIL:
                return PREVIEW_TYPE_SMART; // Emails benefit from smart extraction
            case CATEGORY_SYSTEM:
                return PREVIEW_TYPE_COMPACT; // System notifications are usually concise
            default:
                return PREVIEW_TYPE_SMART;
        }
    }

    /**
     * Notification preview data class
     */
    public static class NotificationPreview {
        public String packageName;
        public String title;
        public String text;
        public String subText;
        public int category;
        public String smartText;
        public boolean isInteractive;
        public long timestamp;

        public NotificationPreview() {
            timestamp = System.currentTimeMillis();
            isInteractive = false;
        }
    }
}



