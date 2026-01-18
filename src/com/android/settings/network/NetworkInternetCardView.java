/*
 * Copyright (C) 2025 BashaMobile
 *
 * Network Internet Settings Card View
 * Independent card layout for network settings navigation
 */

package com.android.settings.network;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.network.NetworkProviderSettings;
import com.android.settings.network.NetworkProviderCallsSmsFragment;
import com.android.settings.network.tether.TetherSettings;
import com.android.settings.datausage.DataSaverSummary;

/**
 * Custom view for network internet settings card navigation
 * Loads preferences from network_provider_internet.xml
 */
public class NetworkInternetCardView extends LinearLayout {
    private static final String TAG = "NetworkInternetCardView";

    private View mInternetCard;
    private View mCallsSmsCard;
    private View mMobileNetworkCard;
    private View mTetherCard;

    public NetworkInternetCardView(Context context) {
        this(context, null);
    }

    public NetworkInternetCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkInternetCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setupCardClicks();
    }

    private void setupCardClicks() {
        // Internet Settings Card
        mInternetCard = findViewById(R.id.network_card_internet);
        if (mInternetCard != null) {
            mInternetCard.setOnClickListener(v -> {
                Log.d(TAG, "Internet Settings card clicked");
                launchInternetSettings();
            });
        }

        // Calls and SMS Card
        mCallsSmsCard = findViewById(R.id.network_card_calls_sms);
        if (mCallsSmsCard != null) {
            mCallsSmsCard.setOnClickListener(v -> {
                Log.d(TAG, "Calls and SMS card clicked");
                launchCallsSmsSettings();
            });
        }

        // Mobile Network Card
        mMobileNetworkCard = findViewById(R.id.network_card_mobile_network);
        if (mMobileNetworkCard != null) {
            mMobileNetworkCard.setOnClickListener(v -> {
                Log.d(TAG, "Mobile Network card clicked");
                launchMobileNetworkSettings();
            });
        }

        // Tether Settings Card
        mTetherCard = findViewById(R.id.network_card_tether);
        if (mTetherCard != null) {
            mTetherCard.setOnClickListener(v -> {
                Log.d(TAG, "Tether Settings card clicked");
                launchTetherSettings();
            });
        }
    }

    /**
     * Launch Internet Settings (NetworkProviderSettings)
     * Corresponds to network_provider_internet.xml lines 22-35
     */
    private void launchInternetSettings() {
        try {
            new SubSettingLauncher(getContext())
                    .setDestination(NetworkProviderSettings.class.getName())
                    .setSourceMetricsCategory(0)
                    .launch();
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Internet Settings", e);
        }
    }

    /**
     * Launch Data Saver Settings (DataSaverSummary)
     * Changed from Calls and SMS Settings to Data Saver page
     * Uses proper intent action to ensure Data Saver opens correctly
     */
    private void launchCallsSmsSettings() {
        try {
            // Use the proper intent action for Data Saver settings
            Intent intent = new Intent(android.provider.Settings.ACTION_DATA_SAVER_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Data Saver Settings with intent action", e);
            // Fallback: try direct activity launch
            try {
                Intent intent = new Intent(getContext(), com.android.settings.Settings.DataSaverSummaryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            } catch (Exception e2) {
                Log.e(TAG, "Failed to launch Data Saver Settings with direct activity", e2);
                // Final fallback: try fragment launcher
                try {
                    new SubSettingLauncher(getContext())
                            .setDestination(DataSaverSummary.class.getName())
                            .setSourceMetricsCategory(0)
                            .launch();
                } catch (Exception e3) {
                    Log.e(TAG, "All Data Saver launch methods failed", e3);
                }
            }
        }
    }

    /**
     * Launch Mobile Network Settings
     * Corresponds to network_provider_internet.xml lines 47-58
     * Note: This uses MobileNetworkSummaryController which navigates to mobile network list
     */
    private void launchMobileNetworkSettings() {
        try {
            // Launch MobileNetworkListFragment which shows the list of mobile networks
            new SubSettingLauncher(getContext())
                    .setDestination("com.android.settings.network.MobileNetworkListFragment")
                    .setSourceMetricsCategory(0)
                    .launch();
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Mobile Network Settings", e);
            // Fallback to system settings
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            } catch (Exception e2) {
                Log.e(TAG, "Failed to launch fallback Mobile Network Settings", e2);
            }
        }
    }

    /**
     * Launch Tether Settings (TetherSettings)
     * Corresponds to network_provider_internet.xml lines 66-77
     */
    private void launchTetherSettings() {
        try {
            new SubSettingLauncher(getContext())
                    .setDestination(TetherSettings.class.getName())
                    .setSourceMetricsCategory(0)
                    .launch();
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Tether Settings", e);
        }
    }
}

