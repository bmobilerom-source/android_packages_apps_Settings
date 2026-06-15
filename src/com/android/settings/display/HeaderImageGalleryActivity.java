/*
 * Copyright (C) 2025 The Android Open Source Project
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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for selecting QS header images from a gallery grid
 */
public class HeaderImageGalleryActivity extends Activity {
    private static final String TAG = "HeaderImageGalleryActivity";
    private static final String SYSUI_PACKAGE_NAME = "com.android.systemui";
    private static final int MAX_HEADER_IMAGES = 200;

    private RecyclerView mRecyclerView;
    private HeaderImageAdapter mAdapter;
    private ImageView mPreviewImage;

    private Uri mSelectedImageUri = null;
    private int mSelectedPosition = -1;
    private List<HeaderImageInfo> mImages = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.header_image_gallery);

        mPreviewImage = findViewById(R.id.preview_image);
        mRecyclerView = findViewById(R.id.image_grid);

        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setInitialPrefetchItemCount(6);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(24);
        if (mRecyclerView.getItemAnimator() instanceof SimpleItemAnimator animator) {
            animator.setSupportsChangeAnimations(false);
            animator.setChangeDuration(0);
        }

        mImages = loadHeaderImages();
        preloadThumbnails();
        mAdapter = new HeaderImageAdapter(this, mImages, this::onImageSelected);
        mRecyclerView.setAdapter(mAdapter);

        Button applyButton = findViewById(R.id.btn_apply);
        Button cancelButton = findViewById(R.id.btn_cancel);
        Button noneButton = findViewById(R.id.btn_none);

        applyButton.setOnClickListener(v -> applySelection());
        cancelButton.setOnClickListener(v -> finish());
        noneButton.setOnClickListener(v -> selectNone());

        updateSelectionFromSettings();
        updatePreview();
    }

    private void onImageSelected(HeaderImageInfo imageInfo, int position) {
        mSelectedImageUri = Uri.parse(imageInfo.getUri());
        mSelectedPosition = position;
        mAdapter.setSelectedPosition(position);
        updatePreview();
    }

    private void updateSelectionFromSettings() {
        String currentUri = Settings.System.getString(getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER_IMAGE);
        if (currentUri != null && !currentUri.isEmpty()) {
            mSelectedImageUri = Uri.parse(currentUri);
            // Find the position of the current selection
            for (int i = 0; i < mImages.size(); i++) {
                if (mImages.get(i).getUri().equals(currentUri)) {
                    mSelectedPosition = i;
                    mAdapter.setSelectedPosition(i);
                    break;
                }
            }
        }
    }

    private void applySelection() {
        final ContentResolver resolver = getContentResolver();
        final String uriString = mSelectedImageUri != null ? mSelectedImageUri.toString() : null;

        Settings.System.putStringForUser(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER_IMAGE, uriString,
                UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER, uriString != null ? 1 : 0,
                UserHandle.USER_CURRENT);
        Settings.System.putStringForUser(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER, "static",
                UserHandle.USER_CURRENT);

        notifyHeaderSettingsChanged(resolver);

        Intent result = new Intent();
        result.putExtra("selected_image", uriString);
        setResult(RESULT_OK, result);
        finish();
    }

    private static void notifyHeaderSettingsChanged(ContentResolver resolver) {
        resolver.notifyChange(
                Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER_IMAGE),
                null, false);
        resolver.notifyChange(
                Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER),
                null, false);
        resolver.notifyChange(
                Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER),
                null, false);
    }

    private void selectNone() {
        mSelectedImageUri = null;
        mSelectedPosition = -1;
        mAdapter.setSelectedPosition(-1);
        updatePreview();
    }

    private void updatePreview() {
        if (mSelectedImageUri == null) {
            mPreviewImage.setImageResource(R.drawable.ic_settings_display);
            return;
        }
        Drawable drawable = loadDrawableFromHeaderUri(mSelectedImageUri.toString());
        if (drawable != null) {
            mPreviewImage.setImageDrawable(drawable);
            return;
        }
        mPreviewImage.setImageResource(R.drawable.ic_settings_display);
    }

    private List<HeaderImageInfo> loadHeaderImages() {
        List<HeaderImageInfo> images = new ArrayList<>();
        try {
            final Resources sysuiRes = getPackageManager().getResourcesForApplication(SYSUI_PACKAGE_NAME);
            for (int i = 1; i <= MAX_HEADER_IMAGES; i++) {
                final String drawableName = "qs_header_image_" + i;
                final int resId = sysuiRes.getIdentifier(drawableName, "drawable", SYSUI_PACKAGE_NAME);
                if (resId == 0) {
                    continue;
                }
                // This URI format matches what StaticHeaderProvider expects ("pkg/name").
                final String uri = SYSUI_PACKAGE_NAME + "/" + drawableName;
                images.add(new HeaderImageInfo("Header " + i, SYSUI_PACKAGE_NAME, resId, uri, false));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load SystemUI header images", e);
        }
        if (images.isEmpty()) {
            Log.w(TAG, "No qs_header_image_* drawables found in " + SYSUI_PACKAGE_NAME
                    + ". Apply the SystemUI custom-header image pack ([2/2] QS patch).");
        }
        return images;
    }

    private void preloadThumbnails() {
        final ExecutorService executor = Executors.newFixedThreadPool(4);
        for (HeaderImageInfo image : mImages) {
            executor.execute(() -> image.getThumbnail(getApplicationContext()));
        }
        executor.shutdown();
    }

    @Nullable
    private Drawable loadDrawableFromHeaderUri(@NonNull String headerUri) {
        try {
            String packageName = SYSUI_PACKAGE_NAME;
            String drawableName = headerUri;
            int slashIndex = headerUri.indexOf('/');
            if (slashIndex > 0 && slashIndex < headerUri.length() - 1) {
                packageName = headerUri.substring(0, slashIndex);
                drawableName = headerUri.substring(slashIndex + 1);
            }
            final Resources res = getPackageManager().getResourcesForApplication(packageName);
            final int resId = res.getIdentifier(drawableName, "drawable", packageName);
            if (resId == 0) {
                return null;
            }
            return res.getDrawable(resId, null);
        } catch (Exception e) {
            Log.w(TAG, "Failed to load drawable for preview: " + headerUri, e);
            return null;
        }
    }
}