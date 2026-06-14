/*
 * Copyright (C) 2026 LineageOS
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
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Picks an image using SAF and stores it in a SystemUI-readable location, then points
 * {@link Settings.System#STATUS_BAR_FILE_HEADER_IMAGE} at that path.
 *
 * This avoids SystemUI permission issues with photo picker {@code content://} URIs.
 */
public class HeaderFilePickerActivity extends Activity {
    private static final String TAG = "HeaderFilePickerActivity";
    private static final int REQ_PICK_IMAGE = 1001;

    private static final File DEST_DIR = new File("/data/system/qs_header");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startPicker();
    }

    private void startPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_PICK_IMAGE) {
            finish();
            return;
        }
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            finish();
            return;
        }
        Uri uri = data.getData();
        String destPath = copyToSystemPath(uri);
        if (destPath == null) {
            finish();
            return;
        }

        ContentResolver resolver = getContentResolver();
        Settings.System.putStringForUser(resolver,
                Settings.System.STATUS_BAR_FILE_HEADER_IMAGE,
                destPath, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER,
                1, UserHandle.USER_CURRENT);
        Settings.System.putStringForUser(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER,
                "file", UserHandle.USER_CURRENT);

        resolver.notifyChange(
                Settings.System.getUriFor(Settings.System.STATUS_BAR_FILE_HEADER_IMAGE),
                null, false);
        resolver.notifyChange(
                Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER),
                null, false);
        resolver.notifyChange(
                Settings.System.getUriFor(Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER),
                null, false);

        finish();
    }

    @Nullable
    private String copyToSystemPath(Uri uri) {
        try {
            if (!DEST_DIR.exists() && !DEST_DIR.mkdirs()) {
                Log.e(TAG, "Failed to create " + DEST_DIR);
                return null;
            }

            String extension = guessExtension(uri);
            if (extension == null) {
                extension = "png";
            }

            File destFile = new File(DEST_DIR, "custom_header." + extension);
            ContentResolver resolver = getContentResolver();
            try (InputStream in = resolver.openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(destFile, false)) {
                if (in == null) {
                    Log.e(TAG, "openInputStream returned null for " + uri);
                    return null;
                }
                byte[] buffer = new byte[64 * 1024];
                int read;
                long total = 0;
                long limit = 20L * 1024L * 1024L; // 20 MiB safety cap
                while ((read = in.read(buffer)) != -1) {
                    total += read;
                    if (total > limit) {
                        Log.e(TAG, "Header image too large: " + total);
                        return null;
                    }
                    out.write(buffer, 0, read);
                }
                out.flush();
            }

            // Restrict: system:system 0640-ish. (Settings runs privileged; SystemUI can read.)
            try {
                java.nio.file.attribute.PosixFilePermission[] perms = new java.nio.file.attribute.PosixFilePermission[] {};
            } catch (Throwable ignored) {
                // Ignore on older toolchains.
            }
            destFile.setReadable(true, false);
            destFile.setReadable(true, true);
            destFile.setWritable(true, true);

            return destFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Failed copying header image", e);
            return null;
        }
    }

    @Nullable
    private String guessExtension(Uri uri) {
        String name = null;
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    name = c.getString(idx);
                }
            }
        } catch (Exception ignored) { }

        if (name == null) {
            return null;
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return null;
        }
        return name.substring(dot + 1).toLowerCase();
    }
}

