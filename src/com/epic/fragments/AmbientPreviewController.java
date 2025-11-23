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

package com.epic.fragments;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.widget.LayoutPreference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AmbientPreviewController extends BasePreferenceController {

    private static final String TAG = "AmbientPreviewController";
    private static final String KEY_AMBIENT_PREVIEW = "ambient_preview";

    private final List<Uri> mAmbientUris;
    private ContentObserver mObserver;
    private View mPreviewView;
    private TextView mPreviewText;
    private ImageView mPreviewImage;

    public AmbientPreviewController(Context context) {
        super(context, KEY_AMBIENT_PREVIEW);

        mAmbientUris = new ArrayList<>();
        mAmbientUris.add(Settings.System.getUriFor(Settings.System.AMBIENT_TEXT));
        mAmbientUris.add(Settings.System.getUriFor(Settings.System.AMBIENT_TEXT_STRING));
        mAmbientUris.add(Settings.System.getUriFor(Settings.System.AMBIENT_TEXT_ALIGNMENT));
        mAmbientUris.add(Settings.System.getUriFor(Settings.System.AMBIENT_TEXT_SIZE));
        mAmbientUris.add(Settings.System.getUriFor(Settings.System.AMBIENT_TEXT_TYPE_COLOR));
        mAmbientUris.add(Settings.System.getUriFor(Settings.System.AMBIENT_TEXT_COLOR));
        mAmbientUris.add(Settings.System.getUriFor(Settings.System.AMBIENT_TEXT_ANIMATION));
        mAmbientUris.add(Settings.System.getUriFor(Settings.System.AMBIENT_IMAGE));
        mAmbientUris.add(Settings.System.getUriFor(Settings.System.AMBIENT_CUSTOM_IMAGE));
        mAmbientUris.add(Settings.System.getUriFor(AmbientCustomizationsHelper.AMBIENT_IMAGE_FILE));
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    public void updateState(LayoutPreference preference) {
        super.updateState(preference);
        mPreviewView = preference.findViewById(android.R.id.widget_frame);
        if (mPreviewView != null) {
            mPreviewText = mPreviewView.findViewById(R.id.ambient_preview_text);
            mPreviewImage = mPreviewView.findViewById(R.id.ambient_preview_image);
            updatePreview();
        }
    }

    private void updatePreview() {
        if (mPreviewText == null || mPreviewImage == null) {
            return;
        }

        Context context = mContext;

        // Update text
        boolean textEnabled = AmbientCustomizationsHelper.isAmbientTextEnabled(context);
        if (textEnabled) {
            String text = AmbientCustomizationsHelper.getAmbientText(context);
            if (text == null || text.isEmpty()) {
                text = "Ambient Text";
            }
            mPreviewText.setText(text);
            mPreviewText.setVisibility(View.VISIBLE);

            // Apply text styling
            int textSize = AmbientCustomizationsHelper.getAmbientTextSize(context);
            mPreviewText.setTextSize(textSize);

            int alignment = AmbientCustomizationsHelper.getAmbientTextAlignment(context);
            applyAlignment(mPreviewText, alignment);

            int colorType = AmbientCustomizationsHelper.getAmbientTextTypeColor(context);
            int color = getTextColor(context, colorType);
            mPreviewText.setTextColor(color);

        } else {
            mPreviewText.setVisibility(View.GONE);
        }

        // Update image
        boolean imageEnabled = AmbientCustomizationsHelper.isAmbientImageEnabled(context);
        if (imageEnabled) {
            String imageFile = AmbientCustomizationsHelper.getAmbientImageFile(context);
            if (imageFile != null && !imageFile.isEmpty()) {
                File file = new File(imageFile);
                if (file.exists()) {
                    try {
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imageFile);
                        if (bitmap != null) {
                            mPreviewImage.setImageBitmap(bitmap);
                            mPreviewImage.setVisibility(View.VISIBLE);
                        } else {
                            mPreviewImage.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to load preview image", e);
                        mPreviewImage.setVisibility(View.GONE);
                    }
                } else {
                    mPreviewImage.setVisibility(View.GONE);
                }
            } else {
                mPreviewImage.setVisibility(View.GONE);
            }
        } else {
            mPreviewImage.setVisibility(View.GONE);
        }
    }

    private void applyAlignment(TextView textView, int alignment) {
        switch (alignment) {
            case 0: // Start Top
                textView.setGravity(android.view.Gravity.START | android.view.Gravity.TOP);
                break;
            case 1: // Start Center
                textView.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
                break;
            case 2: // Start Bottom
                textView.setGravity(android.view.Gravity.START | android.view.Gravity.BOTTOM);
                break;
            case 3: // Center
            default:
                textView.setGravity(android.view.Gravity.CENTER);
                break;
            case 4: // End Top
                textView.setGravity(android.view.Gravity.END | android.view.Gravity.TOP);
                break;
            case 5: // End Center
                textView.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
                break;
            case 6: // End Bottom
                textView.setGravity(android.view.Gravity.END | android.view.Gravity.BOTTOM);
                break;
        }
    }

    private int getTextColor(Context context, int colorType) {
        switch (colorType) {
            case 0: // Accent color
                return com.android.settingslib.Utils.getColorAccentDefaultColor(context);
            case 1: // Wallpaper color
                return getWallpaperDominantColor(context);
            case 2: // Custom color
                return AmbientCustomizationsHelper.getAmbientTextColor(context);
            default:
                return com.android.settingslib.Utils.getColorAccentDefaultColor(context);
        }
    }

    private int getWallpaperDominantColor(Context context) {
        try {
            android.app.WallpaperManager wallpaperManager = android.app.WallpaperManager.getInstance(context);
            android.graphics.drawable.Drawable wallpaper = wallpaperManager.getDrawable();
            if (wallpaper instanceof android.graphics.drawable.BitmapDrawable) {
                android.graphics.Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) wallpaper).getBitmap();
                if (bitmap != null) {
                    // Simple color extraction - use a pixel from the center
                    int centerColor = bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                    return centerColor;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get wallpaper color", e);
        }
        return com.android.settingslib.Utils.getColorAccentDefaultColor(context);
    }

    public void registerObserver() {
        if (mObserver == null) {
            mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    updatePreview();
                }
            };

            for (Uri uri : mAmbientUris) {
                mContext.getContentResolver().registerContentObserver(uri, false, mObserver, UserHandle.USER_ALL);
            }
        }
    }

    public void unregisterObserver() {
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
    }
}
