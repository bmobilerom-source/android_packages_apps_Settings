/*
 * Copyright (C) 2025 BashaMobile
 *
 * BShare settings fragment - LocalSend-inspired WiFi file sharing
 */

package com.android.settings.bshare;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * Settings fragment for BShare
 */
public class BShareSettingsFragment extends SettingsPreferenceFragment {
    private static final String TAG = "BShareSettings";
    private static final int REQUEST_CODE_PICK_FILE = 1001;
    private static final int REQUEST_CODE_PERMISSIONS = 1002;
    
    private BShareManager mManager;
    private Preference mStartServerPref;
    private Preference mStartDiscoveringPref;
    private Preference mSelectFilePref;
    private Preference mSelectDownloadPref;
    private Preference mShareToAppsPref;
    private Preference mServerStatusPref;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.bshare_settings);
        
        mManager = new BShareManager(getContext());
        mManager.setCallback(mCallback);
        
        setupPreferences();
    }
    
    private void setupPreferences() {
        PreferenceScreen screen = getPreferenceScreen();
        
        mStartServerPref = screen.findPreference("bshare_start_server");
        mStartDiscoveringPref = screen.findPreference("bshare_start_discovering");
        mSelectFilePref = screen.findPreference("bshare_select_file");
        mSelectDownloadPref = screen.findPreference("bshare_select_download");
        mShareToAppsPref = screen.findPreference("bshare_share_to_apps");
        mServerStatusPref = screen.findPreference("bshare_server_status");
        
        if (mStartServerPref != null) {
            mStartServerPref.setOnPreferenceClickListener(pref -> {
                if (checkPermissions()) {
                    startServer();
                }
                return true;
            });
        }
        
        if (mStartDiscoveringPref != null) {
            mStartDiscoveringPref.setOnPreferenceClickListener(pref -> {
                if (checkPermissions()) {
                    startDiscovering();
                }
                return true;
            });
        }
        
        if (mSelectFilePref != null) {
            mSelectFilePref.setOnPreferenceClickListener(pref -> {
                openFilePicker();
                return true;
            });
        }
        
        if (mSelectDownloadPref != null) {
            mSelectDownloadPref.setOnPreferenceClickListener(pref -> {
                openDownloadsFolder();
                return true;
            });
        }
        
        if (mShareToAppsPref != null) {
            mShareToAppsPref.setOnPreferenceClickListener(pref -> {
                openShareSheet();
                return true;
            });
        }
        
        updateServerStatus();
    }
    
    private boolean checkPermissions() {
        if (!BSharePermissionHelper.arePermissionsGranted(getContext())) {
            requestPermissions(
                BSharePermissionHelper.getRequiredPermissions(),
                REQUEST_CODE_PERMISSIONS
            );
            return false;
        }
        return true;
    }
    
    private void startServer() {
        mManager.startServer();
        updateServerStatus();
    }
    
    private void startDiscovering() {
        mManager.discoverDevices();
        if (mStartDiscoveringPref != null) {
            mStartDiscoveringPref.setSummary(getString(R.string.bshare_status_discovering));
        }
    }
    
    private void updateServerStatus() {
        if (mServerStatusPref != null) {
            if (mManager.isServerRunning()) {
                String ip = mManager.getServerIp();
                int port = mManager.getServerPort();
                mServerStatusPref.setSummary(
                    getString(R.string.bshare_server_running, ip, port));
            } else {
                mServerStatusPref.setSummary(getString(R.string.bshare_server_stopped));
            }
        }
    }
    
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }
    
    /**
     * Open Downloads folder file picker
     */
    private void openDownloadsFolder() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        // Use MediaStore to access Downloads
        intent.setData(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }
    
    /**
     * Open Android Sharesheet to share files to other apps
     * Uses Intent.ACTION_SEND - no special app needed on recipient
     */
    private void openShareSheet() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, 
            getString(R.string.bshare_share_message));
        
        // Create chooser to show all apps that can handle sharing
        Intent chooser = Intent.createChooser(shareIntent, 
            getString(R.string.bshare_share_to_apps_title));
        
        // Add BShare as an option in the share menu
        Intent bshareIntent = new Intent(getContext(), BShareShareActivity.class);
        bshareIntent.setAction(Intent.ACTION_SEND);
        bshareIntent.setType("*/*");
        
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { bshareIntent });
        
        try {
            startActivity(chooser);
        } catch (android.content.ActivityNotFoundException e) {
            Log.e(TAG, "No app can handle share intent", e);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            Log.d(TAG, "File selected: " + fileUri);
            // Would need device IP from discovery
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            updateServerStatus();
        }
    }
    
    private final BShareManager.BShareCallback mCallback = 
        new BShareManager.BShareCallback() {
            @Override
            public void onServerStarted(String ipAddress, int port) {
                Log.d(TAG, "Server started: " + ipAddress + ":" + port);
                updateServerStatus();
            }
            
            @Override
            public void onServerStopped() {
                Log.d(TAG, "Server stopped");
                updateServerStatus();
            }
            
            @Override
            public void onDeviceFound(String deviceName, String ipAddress) {
                Log.d(TAG, "Device found: " + deviceName + " @ " + ipAddress);
            }
            
            @Override
            public void onConnectionResult(boolean success) {
                Log.d(TAG, "Connection result: " + success);
            }
            
            @Override
            public void onTransferProgress(int percent) {
                Log.d(TAG, "Transfer progress: " + percent + "%");
            }
            
            @Override
            public void onTransferComplete() {
                Log.d(TAG, "Transfer complete");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
            }
        };
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mManager != null) {
            mManager.stopServer();
        }
    }
    
    @Override
    public int getMetricsCategory() {
        return com.android.internal.logging.nano.MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}

