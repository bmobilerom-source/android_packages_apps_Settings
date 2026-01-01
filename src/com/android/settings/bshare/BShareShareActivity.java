/*
 * Copyright (C) 2025 BashaMobile
 *
 * BShare Share Activity - Receives files from other apps via Android Sharesheet
 * No special app needed on recipient - uses standard Android share
 */

package com.android.settings.bshare;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Activity that receives shared files from other apps
 * Registered in AndroidManifest to handle Intent.ACTION_SEND
 * 
 * When user shares a file from any app, BShare appears in the share menu
 * This activity receives the file and can send it via WiFi
 */
public class BShareShareActivity extends Activity {
    private static final String TAG = "BShareShareActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            handleSendFile(intent);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            handleSendMultipleFiles(intent);
        } else {
            Log.e(TAG, "Unknown action or type: " + action + " / " + type);
            finish();
        }
    }
    
    /**
     * Handle single file share
     */
    private void handleSendFile(Intent intent) {
        Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (fileUri != null) {
            Log.d(TAG, "Received file to share: " + fileUri);
            
            // Save file temporarily and prepare for BShare transfer
            File tempFile = saveUriToTempFile(fileUri);
            if (tempFile != null) {
                // Launch BShare settings with file ready to send
                launchBShareWithFile(tempFile);
            } else {
                Toast.makeText(this, 
                    getString(R.string.bshare_error_saving_file), 
                    Toast.LENGTH_LONG).show();
            }
        } else {
            // Text sharing
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                Log.d(TAG, "Received text to share: " + sharedText);
                // Could save as text file and share
                shareAsTextFile(sharedText);
            }
        }
        finish();
    }
    
    /**
     * Handle multiple files share
     */
    private void handleSendMultipleFiles(Intent intent) {
        java.util.ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (fileUris != null && !fileUris.isEmpty()) {
            Log.d(TAG, "Received " + fileUris.size() + " files to share");
            // Handle multiple files - could queue them for transfer
            for (Uri uri : fileUris) {
                File tempFile = saveUriToTempFile(uri);
                if (tempFile != null) {
                    // Queue file for transfer
                }
            }
        }
        finish();
    }
    
    /**
     * Save URI content to temporary file
     */
    private File saveUriToTempFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            
            // Create temp file
            File tempDir = new File(getCacheDir(), "bshare");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            String fileName = getFileName(uri);
            File tempFile = new File(tempDir, fileName);
            
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.close();
            inputStream.close();
            
            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "Error saving file", e);
            return null;
        }
    }
    
    /**
     * Get file name from URI
     */
    private String getFileName(Uri uri) {
        String fileName = "shared_file";
        try {
            android.database.Cursor cursor = getContentResolver().query(
                uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(
                    android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            // Use default name
        }
        
        // Ensure unique filename
        return System.currentTimeMillis() + "_" + fileName;
    }
    
    /**
     * Share text as file
     */
    private void shareAsTextFile(String text) {
        try {
            File tempDir = new File(getCacheDir(), "bshare");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            File textFile = new File(tempDir, "shared_text_" + System.currentTimeMillis() + ".txt");
            FileOutputStream fos = new FileOutputStream(textFile);
            fos.write(text.getBytes());
            fos.close();
            
            launchBShareWithFile(textFile);
        } catch (Exception e) {
            Log.e(TAG, "Error creating text file", e);
        }
    }
    
    /**
     * Launch BShare settings with file ready to send
     */
    private void launchBShareWithFile(File file) {
        // Launch BShare settings fragment via SubSettings
        Intent bshareIntent = new Intent(this, com.android.settings.SubSettings.class);
        bshareIntent.putExtra(":settings:show_fragment", 
            com.android.settings.bshare.BShareSettingsFragment.class.getName());
        bshareIntent.putExtra("bshare_file_path", file.getAbsolutePath());
        bshareIntent.putExtra("bshare_file_name", file.getName());
        bshareIntent.putExtra(":settings:show_fragment_title", 
            getString(com.android.settings.R.string.bshare_settings_title));
        startActivity(bshareIntent);
    }
}

