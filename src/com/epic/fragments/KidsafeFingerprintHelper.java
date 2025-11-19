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

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class for managing hierarchical fingerprint system.
 * 
 * Hierarchy:
 * - Master fingerprints (set on BMobile Fingerprint page) - highest level, can access everything
 * - Kidsafe fingerprints (set on Kidsafe Fingerprint page) - lower level, can be overridden by master
 * 
 * Similar to work profiles where owner's fingerprint is master.
 */
public class KidsafeFingerprintHelper {
    
    private static final String TAG = "KidsafeFingerprintHelper";
    
    // Settings keys for storing fingerprint IDs
    private static final String KEY_MASTER_FINGERPRINT_IDS = "master_fingerprint_ids";
    private static final String KEY_KIDSAFE_FINGERPRINT_IDS = "kidsafe_fingerprint_ids";
    
    /**
     * Get all master fingerprint IDs (set on BMobile Fingerprint page)
     */
    public static Set<Integer> getMasterFingerprintIds(Context context) {
        Set<Integer> masterIds = new HashSet<>();
        if (context == null) {
            return masterIds;
        }
        
        try {
            ContentResolver resolver = context.getContentResolver();
            String masterIdsString = Settings.Secure.getStringForUser(resolver,
                    KEY_MASTER_FINGERPRINT_IDS, UserHandle.USER_CURRENT);
            
            if (!TextUtils.isEmpty(masterIdsString)) {
                String[] ids = masterIdsString.split(",");
                for (String id : ids) {
                    try {
                        masterIds.add(Integer.parseInt(id.trim()));
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid master fingerprint ID: " + id);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting master fingerprint IDs", e);
        }
        
        return masterIds;
    }
    
    /**
     * Get all kidsafe fingerprint IDs (set on Kidsafe Fingerprint page)
     */
    public static Set<Integer> getKidsafeFingerprintIds(Context context) {
        Set<Integer> kidsafeIds = new HashSet<>();
        if (context == null) {
            return kidsafeIds;
        }
        
        try {
            ContentResolver resolver = context.getContentResolver();
            String kidsafeIdsString = Settings.Secure.getStringForUser(resolver,
                    KEY_KIDSAFE_FINGERPRINT_IDS, UserHandle.USER_CURRENT);
            
            if (!TextUtils.isEmpty(kidsafeIdsString)) {
                String[] ids = kidsafeIdsString.split(",");
                for (String id : ids) {
                    try {
                        kidsafeIds.add(Integer.parseInt(id.trim()));
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid kidsafe fingerprint ID: " + id);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting kidsafe fingerprint IDs", e);
        }
        
        return kidsafeIds;
    }
    
    /**
     * Set master fingerprint IDs (called from BMobile Fingerprint page)
     */
    public static void setMasterFingerprintIds(Context context, Set<Integer> fingerprintIds) {
        if (context == null) {
            return;
        }
        
        try {
            ContentResolver resolver = context.getContentResolver();
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Integer id : fingerprintIds) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(id);
                first = false;
            }
            Settings.Secure.putStringForUser(resolver, KEY_MASTER_FINGERPRINT_IDS,
                    sb.toString(), UserHandle.USER_CURRENT);
            Log.d(TAG, "Master fingerprint IDs set: " + sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error setting master fingerprint IDs", e);
        }
    }
    
    /**
     * Set kidsafe fingerprint IDs (called from Kidsafe Fingerprint page)
     */
    public static void setKidsafeFingerprintIds(Context context, Set<Integer> fingerprintIds) {
        if (context == null) {
            return;
        }
        
        try {
            ContentResolver resolver = context.getContentResolver();
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Integer id : fingerprintIds) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(id);
                first = false;
            }
            Settings.Secure.putStringForUser(resolver, KEY_KIDSAFE_FINGERPRINT_IDS,
                    sb.toString(), UserHandle.USER_CURRENT);
            Log.d(TAG, "Kidsafe fingerprint IDs set: " + sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error setting kidsafe fingerprint IDs", e);
        }
    }
    
    /**
     * Check if a fingerprint ID is a master fingerprint
     */
    public static boolean isMasterFingerprint(Context context, int fingerprintId) {
        return getMasterFingerprintIds(context).contains(fingerprintId);
    }
    
    /**
     * Check if a fingerprint ID is a kidsafe fingerprint
     */
    public static boolean isKidsafeFingerprint(Context context, int fingerprintId) {
        return getKidsafeFingerprintIds(context).contains(fingerprintId);
    }
    
    /**
     * Check if a fingerprint ID has access (master or kidsafe)
     */
    public static boolean hasFingerprintAccess(Context context, int fingerprintId) {
        return isMasterFingerprint(context, fingerprintId) || 
               isKidsafeFingerprint(context, fingerprintId);
    }
    
    /**
     * Get all enrolled fingerprints that are NOT master fingerprints
     * (available for selection as kidsafe fingerprints)
     */
    public static List<Fingerprint> getAvailableFingerprintsForKidsafe(Context context) {
        List<Fingerprint> available = new ArrayList<>();
        if (context == null) {
            return available;
        }
        
        try {
            FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);
            if (fingerprintManager == null) {
                return available;
            }
            
            int userId = UserHandle.myUserId();
            List<Fingerprint> allFingerprints = fingerprintManager.getEnrolledFingerprints(userId);
            Set<Integer> masterIds = getMasterFingerprintIds(context);
            
            if (allFingerprints != null) {
                for (Fingerprint fingerprint : allFingerprints) {
                    // Only include fingerprints that are NOT master fingerprints
                    if (!masterIds.contains(fingerprint.getBiometricId())) {
                        available.add(fingerprint);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting available fingerprints for kidsafe", e);
        }
        
        return available;
    }
    
    /**
     * Get all enrolled fingerprints that are master fingerprints
     */
    public static List<Fingerprint> getMasterFingerprints(Context context) {
        List<Fingerprint> master = new ArrayList<>();
        if (context == null) {
            return master;
        }
        
        try {
            FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);
            if (fingerprintManager == null) {
                return master;
            }
            
            int userId = UserHandle.myUserId();
            List<Fingerprint> allFingerprints = fingerprintManager.getEnrolledFingerprints(userId);
            Set<Integer> masterIds = getMasterFingerprintIds(context);
            
            if (allFingerprints != null) {
                for (Fingerprint fingerprint : allFingerprints) {
                    if (masterIds.contains(fingerprint.getBiometricId())) {
                        master.add(fingerprint);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting master fingerprints", e);
        }
        
        return master;
    }
    
    /**
     * Get all enrolled fingerprints that are kidsafe fingerprints
     */
    public static List<Fingerprint> getKidsafeFingerprints(Context context) {
        List<Fingerprint> kidsafe = new ArrayList<>();
        if (context == null) {
            return kidsafe;
        }
        
        try {
            FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);
            if (fingerprintManager == null) {
                return kidsafe;
            }
            
            int userId = UserHandle.myUserId();
            List<Fingerprint> allFingerprints = fingerprintManager.getEnrolledFingerprints(userId);
            Set<Integer> kidsafeIds = getKidsafeFingerprintIds(context);
            
            if (allFingerprints != null) {
                for (Fingerprint fingerprint : allFingerprints) {
                    if (kidsafeIds.contains(fingerprint.getBiometricId())) {
                        kidsafe.add(fingerprint);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting kidsafe fingerprints", e);
        }
        
        return kidsafe;
    }
}

