/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.homepage;

import static com.android.settings.search.actionbar.SearchMenuController.NEED_SEARCH_ICON_IN_ACTION_BAR;
import static com.android.settingslib.search.SearchIndexable.MOBILE;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;
import android.app.Activity;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;
import androidx.window.embedding.ActivityEmbeddingController;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.activityembedding.ActivityEmbeddingRulesController;
import com.android.settings.activityembedding.ActivityEmbeddingUtils;
import com.android.settings.core.RoundCornerPreferenceAdapter;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.flags.Flags;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.support.SupportPreferenceController;
import com.android.settings.widget.HomepagePreference;
import com.android.settings.widget.HomepagePreferenceLayoutHelper.HomepagePreferenceLayout;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.LayoutPreference;
import com.android.settings.widget.EntityHeaderController;

@SearchIndexable(forTarget = MOBILE)
public class TopLevelSettings extends DashboardFragment implements SplitLayoutListener,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TAG = "TopLevelSettings";
    private static final String SAVED_HIGHLIGHT_MIXIN = "highlight_mixin";
    private static final String PREF_KEY_SUPPORT = "top_level_support";
    private static final String KEY_USER_CARD = "top_level_usercard";
    private static final int MAX_MATERIAL_GRID_SETUP_RETRIES = 5;
    private static final int MAX_FUN_DISPLAY_GRID_SETUP_RETRIES = 5;

    private int mDashBoardStyle = -1; // -1 means not initialized yet, will be read from Settings
    private int mMaterialGridSetupRetries = 0;
    private int mFunDisplayGridSetupRetries = 0;
    private boolean mIsEmbeddingActivityEnabled;
    private TopLevelHighlightMixin mHighlightMixin;
    private int mPaddingHorizontal;
    private boolean mScrollNeeded = true;
    private boolean mFirstStarted = true;
    private ActivityEmbeddingController mActivityEmbeddingController;

    public TopLevelSettings() {
        final Bundle args = new Bundle();
        // Disable the search icon because this page uses a full search view in actionbar.
        // For classic style (5), hide the search bar completely
        args.putBoolean(NEED_SEARCH_ICON_IN_ACTION_BAR, false);
        setArguments(args);
    }

    /** Dependency injection ctor only for testing. */
    @VisibleForTesting
    public TopLevelSettings(TopLevelHighlightMixin highlightMixin) {
        this();
        mHighlightMixin = highlightMixin;
    }

    @Override
    protected int getPreferenceScreenResId() {
        // Always respect custom dashboard style - don't let Flags.homepageRevamp() override it
        // Ensure dashboard style is set - read from Settings if not already cached
        // This handles cases where getPreferenceScreenResId() is called before onAttach()
        Context context = getContext();
        if (context == null && getActivity() != null) {
            context = getActivity();
        }
        if (context != null) {
            // Always read fresh from Settings to ensure we have the correct value
            // This is critical for the custom dashboard to work correctly
            int currentStyle = DashboardStyleHelper.getDashboardStyle(context);
            if (mDashBoardStyle == -1 || currentStyle != mDashBoardStyle) {
                mDashBoardStyle = currentStyle;
                Log.d(TAG, "Dashboard style set/updated in getPreferenceScreenResId: " + mDashBoardStyle);
            }
        } else if (mDashBoardStyle == -1) {
            // Fallback if context is not available and style not initialized
            mDashBoardStyle = 2; // Default to V2 style
            Log.w(TAG, "Context not available, using default style: " + mDashBoardStyle);
        }
        // Return the correct XML based on cached style value
        int resId = DashboardStyleHelper.getPreferenceScreenResIdForStyle(mDashBoardStyle);
        Log.d(TAG, "getPreferenceScreenResId returning: " + resId + " for style: " + mDashBoardStyle);
        return resId;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onStart() {
        super.onStart();
        onUserCard();
        
        if (mFirstStarted) {
            mFirstStarted = false;
            FeatureFactory.getFeatureFactory().getSearchFeatureProvider().sendPreIndexIntent(
                    getContext());
        } else if (mIsEmbeddingActivityEnabled && isOnlyOneActivityInTask()
                && !isActivityEmbedded()) {
            // Set default highlight menu key for 1-pane homepage since it will show the placeholder
            // page once changing back to 2-pane.
            Log.i(TAG, "Set default menu key");
            setHighlightMenuKey(getString(SettingsHomepageActivity.DEFAULT_HIGHLIGHT_MENU_KEY),
                    /* scrollNeeded= */ false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Hide search bar for classic style (5)
        Context context = getContext();
        if (context != null) {
            int currentStyle = DashboardStyleHelper.getDashboardStyle(context);
            if (currentStyle == 5) { // Classic style
                hideSearchBar();
            }
        }
        
        // Set up compact dashboard grid if enabled (independent toggle)
        // Set up material dashboard grid if style is material (4)
        // Delay setup to ensure view hierarchy is fully created
        if (context != null) {
            // Check compact dashboard toggle (independent of style)
            boolean compactEnabled = Settings.System.getIntForUser(
                    context.getContentResolver(),
                    Settings.System.SETTINGS_COMPACT_DASHBOARD_ENABLED,
                    0,
                    android.os.UserHandle.USER_CURRENT) == 1;
            
            int currentStyle = DashboardStyleHelper.getDashboardStyle(context);
            
            if (compactEnabled) {
                // Post to main thread to ensure view is fully inflated
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        setupCompactDashboardGrid();
                    }
                });
            } else if (currentStyle == 4) {
                mDashBoardStyle = 4;
                // Post to main thread with a small delay to ensure view is fully inflated
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setupMaterialDashboardGrid();
                    }
                }, 50); // Small delay to ensure layout is fully inflated
            } else if (currentStyle == 7) {
                mDashBoardStyle = 7;
                // Post to main thread with a small delay to ensure view is fully inflated
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setupFunDisplayGrid();
                    }
                }, 50); // Small delay to ensure layout is fully inflated
            } else if (currentStyle == 8) {
                mDashBoardStyle = 8;
                view.postDelayed(() -> setupBMobileExpressiveGrid(), 50);
            } else if (currentStyle == 12) {
                mDashBoardStyle = 12;
                view.postDelayed(() -> {
                    setupAfterlabsTabStrip();
                }, 50);
            } else if (currentStyle == 13) {
                mDashBoardStyle = 13;
                view.postDelayed(() -> setupAfterlabsGrid(), 50);
            } else if (currentStyle == 14) {
                mDashBoardStyle = 14;
                view.postDelayed(() -> setupInfinityHomeGrid(), 50);
            }
        } else {
            // Fallback: check if compact was enabled or material style
            View rootView = getView();
            if (rootView != null) {
                Context fallbackContext = getActivity();
                if (fallbackContext != null) {
                    boolean compactEnabled = Settings.System.getIntForUser(
                            fallbackContext.getContentResolver(),
                            Settings.System.SETTINGS_COMPACT_DASHBOARD_ENABLED,
                            0,
                            android.os.UserHandle.USER_CURRENT) == 1;
                    
                    if (compactEnabled) {
                        rootView.post(new Runnable() {
                            @Override
                            public void run() {
                                setupCompactDashboardGrid();
                            }
                        });
                    } else if (mDashBoardStyle == 4) {
                        rootView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setupMaterialDashboardGrid();
                            }
                        }, 50);
                    } else if (mDashBoardStyle == 7) {
                        rootView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setupFunDisplayGrid();
                            }
                        }, 50);
                    }
                }
            }
        }
        
        // Setup click listeners for homepage widgets
        setupHomepageWidgetsClickListeners(view);
    }
    
    /**
     * Sets up click listeners for homepage widgets (battery, storage, search, bluetooth)
     */
    private void setupHomepageWidgetsClickListeners(View rootView) {
        try {
            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                return;
            }
            
            // Setup regular homepage widgets
            com.android.settingslib.widget.LayoutPreference homepageWidgetsPref = 
                    (com.android.settingslib.widget.LayoutPreference) screen.findPreference("top_level_homepage_widgets");
            if (homepageWidgetsPref != null) {
                setupHomepageWidgetsClickListenersInternal(homepageWidgetsPref, rootView);
            }
            
            // Setup extended homepage widgets (for BMobile Expressive)
            com.android.settingslib.widget.LayoutPreference extendedHomepageWidgetsPref = 
                    (com.android.settingslib.widget.LayoutPreference) screen.findPreference("extended_homepage_widgets");
            if (extendedHomepageWidgetsPref != null) {
                setupHomepageWidgetsClickListenersInternal(extendedHomepageWidgetsPref, rootView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setupHomepageWidgetsClickListeners", e);
        }
    }
    
    /**
     * Internal method to set up click listeners for homepage widgets
     */
    private void setupHomepageWidgetsClickListenersInternal(com.android.settingslib.widget.LayoutPreference homepageWidgetsPref, View rootView) {
        try {
            if (homepageWidgetsPref == null) {
                return;
            }
            
            android.app.Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            
            // Post to ensure layout is inflated
            rootView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Make the LayoutPreference container background transparent so cards appear floating
                        // Access the root view of the LayoutPreference
                        View layoutRootView = homepageWidgetsPref.findViewById(android.R.id.widget_frame);
                        if (layoutRootView == null) {
                            // Try to find the root LinearLayout from homepage_widgets.xml
                            layoutRootView = homepageWidgetsPref.findViewById(R.id.battery_widget);
                            if (layoutRootView != null) {
                                ViewParent parent = layoutRootView.getParent();
                                while (parent != null && parent instanceof View) {
                                    View parentView = (View) parent;
                                    if (parentView.getId() == android.R.id.widget_frame || 
                                        parentView.getBackground() != null) {
                                        parentView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                                        break;
                                    }
                                    parent = parent.getParent();
                                }
                            }
                        } else {
                            layoutRootView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                        }
                        
                        // Battery widget -> Battery page
                        View batteryWidget = homepageWidgetsPref.findViewById(R.id.battery_widget);
                        if (batteryWidget != null) {
                            batteryWidget.setClickable(true);
                            batteryWidget.setFocusable(true);
                            batteryWidget.setEnabled(true);
                            batteryWidget.setOnClickListener(null);
                            batteryWidget.setOnClickListener(v -> {
                                if (!v.isEnabled()) return;
                                v.setEnabled(false);
                                try {
                                    new com.android.settings.core.SubSettingLauncher(activity)
                                            .setDestination("com.android.settings.fuelgauge.batteryusage.PowerUsageSummary")
                                            .setTitleRes(R.string.power_usage_summary_title)
                                            .setSourceMetricsCategory(getMetricsCategory())
                                            .launch();
                                } catch (Exception e) {
                                    Log.e(TAG, "Error launching battery page", e);
                                } finally {
                                    v.postDelayed(() -> v.setEnabled(true), 500);
                                }
                            });
                        }
                        
                        // Storage widget -> Storage page
                        View storageWidget = homepageWidgetsPref.findViewById(R.id.storage_widget);
                        if (storageWidget != null) {
                            // Remove all existing listeners
                            storageWidget.setOnClickListener(null);
                            // Make sure all child views don't intercept clicks
                            if (storageWidget instanceof ViewGroup) {
                                ViewGroup group = (ViewGroup) storageWidget;
                                for (int i = 0; i < group.getChildCount(); i++) {
                                    View child = group.getChildAt(i);
                                    child.setClickable(false);
                                    child.setFocusable(false);
                                }
                            }
                            storageWidget.setClickable(true);
                            storageWidget.setFocusable(true);
                            storageWidget.setEnabled(true);
                            storageWidget.setOnClickListener(v -> {
                                try {
                                    com.android.settings.core.SubSettingLauncher launcher = 
                                            new com.android.settings.core.SubSettingLauncher(activity);
                                    launcher.setDestination("com.android.settings.deviceinfo.StorageDashboardFragment")
                                            .setTitleRes(R.string.storage_settings)
                                            .setSourceMetricsCategory(getMetricsCategory());
                                    launcher.launch();
                                } catch (android.content.ActivityNotFoundException e) {
                                    Log.e(TAG, "Storage fragment not found", e);
                                    try {
                                        android.content.Intent intent = new android.content.Intent(
                                                android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
                                        activity.startActivity(intent);
                                    } catch (Exception e2) {
                                        Log.e(TAG, "Error opening storage via intent", e2);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error launching storage page", e);
                                }
                            });
                        }
                        
                        // Search widget -> Open search
                        View searchWidget = homepageWidgetsPref.findViewById(R.id.search_widget);
                        if (searchWidget != null) {
                            // Remove all existing listeners
                            searchWidget.setOnClickListener(null);
                            // Make sure all child views don't intercept clicks
                            if (searchWidget instanceof ViewGroup) {
                                ViewGroup group = (ViewGroup) searchWidget;
                                for (int i = 0; i < group.getChildCount(); i++) {
                                    View child = group.getChildAt(i);
                                    child.setClickable(false);
                                    child.setFocusable(false);
                                }
                            }
                            searchWidget.setClickable(true);
                            searchWidget.setFocusable(true);
                            searchWidget.setEnabled(true);
                            searchWidget.setOnClickListener(v -> {
                                try {
                                    android.content.Intent searchIntent = new android.content.Intent();
                                    searchIntent.setAction(android.app.SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
                                    searchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                                    activity.startActivity(searchIntent);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error opening search", e);
                                }
                            });
                        }
                        
                        // System widget (bluetooth icon) -> Connected devices
                        View systemWidget = homepageWidgetsPref.findViewById(R.id.system_widget);
                        if (systemWidget != null) {
                            // Remove all existing listeners
                            systemWidget.setOnClickListener(null);
                            // Make sure all child views don't intercept clicks
                            if (systemWidget instanceof ViewGroup) {
                                ViewGroup group = (ViewGroup) systemWidget;
                                for (int i = 0; i < group.getChildCount(); i++) {
                                    View child = group.getChildAt(i);
                                    child.setClickable(false);
                                    child.setFocusable(false);
                                }
                            }
                            systemWidget.setClickable(true);
                            systemWidget.setFocusable(true);
                            systemWidget.setEnabled(true);
                            systemWidget.setOnClickListener(v -> {
                                try {
                                    new com.android.settings.core.SubSettingLauncher(activity)
                                            .setDestination("com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment")
                                            .setTitleRes(R.string.connected_devices_dashboard_title)
                                            .setSourceMetricsCategory(getMetricsCategory())
                                            .launch();
                                } catch (Exception e) {
                                    Log.e(TAG, "Error launching connected devices", e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting up homepage widgets click listeners", e);
                    }
                }
            }, 200); // Delay to ensure layout is fully inflated
        } catch (Exception e) {
            Log.e(TAG, "Error in setupHomepageWidgetsClickListeners", e);
        }
    }
    
    /**
     * Hides the search bar for classic style
     */
    private void hideSearchBar() {
        try {
            android.app.Activity activity = getActivity();
            if (activity != null) {
                android.view.View searchBar = activity.findViewById(R.id.search_action_bar);
                if (searchBar != null) {
                    searchBar.setVisibility(android.view.View.GONE);
                }
                android.view.View searchBarTwoPane = activity.findViewById(R.id.search_action_bar_two_pane);
                if (searchBarTwoPane != null) {
                    searchBarTwoPane.setVisibility(android.view.View.GONE);
                }
                android.view.View appBarContainer = activity.findViewById(R.id.app_bar_container);
                if (appBarContainer != null) {
                    appBarContainer.setVisibility(android.view.View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding search bar", e);
        }
    }

    /**
     * Sets up the RecyclerView grid for compact dashboard style.
     * Based on DisplayPageGrid implementation.
     */
    private void setupCompactDashboardGrid() {
        try {
            // Check if compact dashboard is enabled (independent toggle)
            Context context = getContext();
            if (context == null) {
                context = getActivity();
            }
            if (context == null) {
                Log.w(TAG, "Context is null, cannot setup compact grid");
                return;
            }
            
            boolean compactEnabled = Settings.System.getIntForUser(
                    context.getContentResolver(),
                    Settings.System.SETTINGS_COMPACT_DASHBOARD_ENABLED,
                    0,
                    android.os.UserHandle.USER_CURRENT) == 1;
            
            if (!compactEnabled) {
                Log.d(TAG, "Compact dashboard is disabled, skipping setup");
                return;
            }
            
            // Ensure we're on the main thread
            if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
                View view = getView();
                if (view != null) {
                    view.post(() -> setupCompactDashboardGrid());
                }
                return;
            }
            
            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                Log.w(TAG, "PreferenceScreen is null, cannot setup compact grid");
                return;
            }
            
            // Find compact dashboard grid in current screen - following DisplayPageGrid pattern
            androidx.preference.Preference layoutPrefPref = screen.findPreference("compact_dashboard_grid");
            if (layoutPrefPref == null || !(layoutPrefPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                Log.w(TAG, "compact_dashboard_grid LayoutPreference not found");
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference layoutPref = 
                    (com.android.settingslib.widget.LayoutPreference) layoutPrefPref;
            
            // Find RecyclerView - following DisplayPageGrid pattern
            androidx.recyclerview.widget.RecyclerView rv = layoutPref.findViewById(R.id.compact_dashboard_grid_recycler);
            if (rv == null) {
                Log.w(TAG, "compact_dashboard_grid_recycler RecyclerView not found");
                return;
            }
            
            android.app.Activity activity = getActivity();
            if (activity == null) {
                Log.w(TAG, "Activity is null, cannot setup compact grid");
                return;
            }
            
            // Setup layout manager with error handling
            try {
                androidx.recyclerview.widget.GridLayoutManager layoutManager = 
                        new androidx.recyclerview.widget.GridLayoutManager(context, 2);
                rv.setLayoutManager(layoutManager);
            } catch (Exception e) {
                Log.e(TAG, "Error setting up GridLayoutManager", e);
                return;
            }

            java.util.List<com.epic.fragments.CompactDashboardGridAdapter.CardItem> items = new java.util.ArrayList<>();
            
            // Row 1: Network and Connected Devices
            items.add(new com.epic.fragments.CompactDashboardGridAdapter.CardItem(
                    com.epic.fragments.CompactDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.network_dashboard_title,
                    R.string.summary_placeholder,
                    "com.android.settings.network.NetworkDashboardFragment",
                    R.drawable.ic_settings_wireless_filled));
            items.add(new com.epic.fragments.CompactDashboardGridAdapter.CardItem(
                    com.epic.fragments.CompactDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.custom_dashboard_title,
                    R.string.custom_dashboard_summary,
                    "com.epic.fragments.CustomDashboardSettings",
                    R.drawable.ic_custom_dashboard));
            items.add(new com.epic.fragments.CompactDashboardGridAdapter.CardItem(
                    com.epic.fragments.CompactDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.connected_devices_dashboard_title,
                    R.string.summary_placeholder,
                    "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment",
                    R.drawable.ic_devices_other_filled));
            
            // Row 2: Apps and Notifications
            items.add(new com.epic.fragments.CompactDashboardGridAdapter.CardItem(
                    com.epic.fragments.CompactDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.apps_dashboard_title,
                    R.string.summary_placeholder,
                    "com.android.settings.applications.AppDashboardFragment",
                    R.drawable.ic_apps));
            items.add(new com.epic.fragments.CompactDashboardGridAdapter.CardItem(
                    com.epic.fragments.CompactDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.configure_notification_settings,
                    R.string.summary_placeholder,
                    "com.android.settings.notification.ConfigureNotificationSettings",
                    R.drawable.ic_notifications));
            
            // Row 3: Battery and Display
            items.add(new com.epic.fragments.CompactDashboardGridAdapter.CardItem(
                    com.epic.fragments.CompactDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.power_usage_summary_title,
                    R.string.summary_placeholder,
                    "com.android.settings.fuelgauge.batteryusage.PowerUsageSummary",
                    R.drawable.ic_settings_battery_white));
            items.add(new com.epic.fragments.CompactDashboardGridAdapter.CardItem(
                    com.epic.fragments.CompactDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.display_settings,
                    R.string.summary_placeholder,
                    "com.android.settings.DisplaySettings",
                    R.drawable.ic_settings_display_white));
            
            // Row 4: Privacy (Storage, About Phone, Tips and Support, Anatolia Settings removed per user request)
            items.add(new com.epic.fragments.CompactDashboardGridAdapter.CardItem(
                    com.epic.fragments.CompactDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.security_settings_title,
                    R.string.security_dashboard_summary,
                    "com.android.settings.security.SecuritySettings",
                    R.drawable.ic_settings_security_filled));
            items.add(new com.epic.fragments.CompactDashboardGridAdapter.CardItem(
                    com.epic.fragments.CompactDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.privacy_dashboard_title,
                    R.string.summary_placeholder,
                    "com.android.settings.privacy.PrivacyDashboardFragment",
                    R.drawable.ic_settings_privacy_filled));
            
            // Row 5: Accessibility (Anatolia Settings, About Phone, Tips and Support removed per user request)
            items.add(new com.epic.fragments.CompactDashboardGridAdapter.CardItem(
                    com.epic.fragments.CompactDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.accessibility_settings,
                    R.string.summary_placeholder,
                    "com.android.settings.accessibility.AccessibilitySettings",
                    R.drawable.ic_settings_accessibility));

            if (items.isEmpty()) {
                Log.w(TAG, "No items to display in compact grid");
                return;
            }
            
            // Create adapter with error handling
            com.epic.fragments.CompactDashboardGridAdapter adapter = null;
            try {
                adapter = new com.epic.fragments.CompactDashboardGridAdapter(activity, items, getMetricsCategory());
            } catch (Exception e) {
                Log.e(TAG, "Error creating CompactDashboardGridAdapter", e);
                return;
            }
            
            if (adapter == null) {
                Log.e(TAG, "Adapter is null after creation");
                return;
            }
            
            // Set adapter with error handling - following DisplayPageGrid pattern
            try {
                rv.setAdapter(adapter);
                Log.d(TAG, "Compact dashboard grid setup completed successfully with " + items.size() + " items");
            } catch (Exception e) {
                Log.e(TAG, "Error setting adapter on RecyclerView", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up compact dashboard grid", e);
        }
    }

    /**
     * Sets up the RecyclerView grid for material dashboard style.
     * Based on DisplayPageGrid implementation.
     * All preferences from top_level_settings_v2.xml are converted to cards.
     */
    private void setupMaterialDashboardGrid() {
        try {
            // Ensure we're on the main thread
            if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
                View view = getView();
                if (view != null) {
                    view.post(() -> setupMaterialDashboardGrid());
                }
                return;
            }
            
            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                Log.w(TAG, "PreferenceScreen is null, cannot setup material grid");
                return;
            }
            
            Context context = getContext();
            if (context == null) {
                Log.w(TAG, "Context is null, cannot setup material grid");
                return;
            }
            
            android.app.Activity activity = getActivity();
            if (activity == null) {
                Log.w(TAG, "Activity is null, cannot setup material grid");
                return;
            }
            
            // Find material dashboard grid - following DisplayPageGrid pattern
            androidx.preference.Preference layoutPrefPref = screen.findPreference("material_dashboard_grid");
            if (layoutPrefPref == null || !(layoutPrefPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                Log.w(TAG, "material_dashboard_grid LayoutPreference not found");
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference layoutPref = 
                    (com.android.settingslib.widget.LayoutPreference) layoutPrefPref;
            
            // Find RecyclerView - following DisplayPageGrid pattern
            androidx.recyclerview.widget.RecyclerView rv = layoutPref.findViewById(R.id.material_dashboard_grid_recycler);
            if (rv == null) {
                Log.w(TAG, "material_dashboard_grid_recycler RecyclerView not found");
                return;
            }

            java.util.List<com.epic.fragments.MaterialDashboardGridAdapter.CardItem> items = new java.util.ArrayList<>();
            
            // All preferences from top_level_settings_v2.xml as cards
            // Connectivity Category
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.network_dashboard_title,
                    R.string.summary_placeholder,
                    "com.android.settings.network.NetworkDashboardFragment",
                    R.drawable.ic_settings_wireless_filled));
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.custom_dashboard_title,
                    R.string.custom_dashboard_summary,
                    "com.epic.fragments.CustomDashboardSettings",
                    R.drawable.ic_custom_dashboard));
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.connected_devices_dashboard_title,
                    R.string.connected_devices_dashboard_default_summary,
                    "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment",
                    R.drawable.ic_devices_other_filled));
            
            // Personalize Category
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.apps_dashboard_title,
                    R.string.app_and_notification_dashboard_summary,
                    "com.android.settings.applications.AppDashboardFragment",
                    R.drawable.ic_apps_filled));
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.configure_notification_settings,
                    R.string.notification_dashboard_summary,
                    "com.android.settings.notification.ConfigureNotificationSettings",
                    R.drawable.ic_notifications_filled));
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.sound_settings,
                    R.string.sound_dashboard_summary_with_dnd,
                    "com.android.settings.notification.SoundSettings",
                    R.drawable.ic_volume_up_filled));
            // Modes and Communal removed per user request
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.display_settings,
                    R.string.display_dashboard_summary,
                    "com.android.settings.DisplaySettings",
                    R.drawable.ic_settings_display_filled));
            // Wallpaper removed per user request
            
            // System Info Category
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.storage_settings,
                    R.string.summary_placeholder,
                    "com.android.settings.deviceinfo.StorageDashboardFragment",
                    R.drawable.ic_storage_filled));
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.power_usage_summary_title,
                    R.string.summary_placeholder,
                    "com.android.settings.fuelgauge.batteryusage.PowerUsageSummary",
                    R.drawable.ic_settings_battery_filled));
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.header_category_system,
                    R.string.system_dashboard_summary,
                    "com.android.settings.system.SystemDashboardFragment",
                    R.drawable.ic_settings_system_dashboard_filled));
            // About Phone, Safety Center, Security, Privacy, Wallpaper, Modes, Communal, Tips and Support removed per user request
            
            // Security & Privacy Category (aligned with V2)
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.security_settings_title,
                    R.string.security_dashboard_summary,
                    "com.android.settings.security.SecuritySettings",
                    R.drawable.ic_settings_security_filled));
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.privacy_controls_title,
                    R.string.privacy_controls_summary,
                    "com.android.settings.privacy.PrivacyControlsFragment",
                    R.drawable.ic_settings_privacy_filled));
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.privacy_dashboard_title,
                    R.string.privacy_dashboard_summary,
                    "com.android.settings.privacy.PrivacyDashboardFragment",
                    R.drawable.ic_settings_privacy_filled));
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.location_settings_title,
                    R.string.location_settings_loading_app_permission_stats,
                    "com.android.settings.location.LocationSettings",
                    R.drawable.ic_settings_location_filled));
            
            // Support Category (only Accessibility remains)
            items.add(new com.epic.fragments.MaterialDashboardGridAdapter.CardItem(
                    com.epic.fragments.MaterialDashboardGridAdapter.CARD_TYPE_STANDARD,
                    R.string.accessibility_settings,
                    R.string.accessibility_settings_summary,
                    "com.android.settings.accessibility.AccessibilitySettings",
                    R.drawable.ic_settings_accessibility_filled));

            // Setup layout manager - following DisplayPageGrid pattern
            try {
                androidx.recyclerview.widget.GridLayoutManager layoutManager = 
                        new androidx.recyclerview.widget.GridLayoutManager(context, 2);
                rv.setLayoutManager(layoutManager);
            } catch (Exception e) {
                Log.e(TAG, "Error setting up GridLayoutManager", e);
                return;
            }
            
            // Create adapter with error handling - following DisplayPageGrid pattern
            com.epic.fragments.MaterialDashboardGridAdapter adapter = null;
            try {
                adapter = new com.epic.fragments.MaterialDashboardGridAdapter(activity, items, getMetricsCategory());
            } catch (Exception e) {
                Log.e(TAG, "Error creating MaterialDashboardGridAdapter", e);
                return;
            }
            
            if (adapter == null) {
                Log.e(TAG, "Adapter is null after creation");
                return;
            }
            
            // Set adapter with error handling - following DisplayPageGrid pattern
            try {
                rv.setAdapter(adapter);
                Log.d(TAG, "Material dashboard grid setup completed successfully with " + items.size() + " items");
            } catch (Exception e) {
                Log.e(TAG, "Error setting adapter on RecyclerView", e);
            }
        } catch (Exception e) {
                Log.e(TAG, "Error setting up material dashboard grid", e);
            }
        }

    /**
     * Sets up the RecyclerView grid for BMobile Expressive dashboard style.
     * Based on BMobileExpressiveSettings implementation.
     */
    private void setupBMobileExpressiveGrid() {
        try {
            Context context = getContext();
            if (context == null) {
                context = getActivity();
            }
            if (context == null) {
                Log.w(TAG, "Context is null, cannot setup BMobile Expressive grid");
                return;
            }
            
            final Context finalContext = context; // Make final for lambda
            
            int currentStyle = DashboardStyleHelper.getDashboardStyle(context);
            if (currentStyle != 8) {
                Log.d(TAG, "Not BMobile Expressive style, skipping setup");
                return;
            }
            
            // Ensure we're on the main thread
            if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
                View view = getView();
                if (view != null) {
                    view.post(() -> setupBMobileExpressiveGrid());
                }
                return;
            }
            
            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                Log.w(TAG, "PreferenceScreen is null");
                return;
            }
            
            androidx.preference.Preference layoutPref = screen.findPreference("bmobile_expressive_grid");
            if (layoutPref == null || !(layoutPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                Log.w(TAG, "BMobile Expressive grid LayoutPreference not found");
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            
            // Find RecyclerView
            androidx.recyclerview.widget.RecyclerView rv = lp.findViewById(R.id.bmobile_expressive_grid_recycler);
            if (rv == null) {
                Log.w(TAG, "BMobile Expressive grid RecyclerView not found, retrying...");
                View rootView = getView();
                if (rootView != null) {
                    rootView.postDelayed(() -> {
                        try {
                            androidx.recyclerview.widget.RecyclerView delayedRv = lp.findViewById(R.id.bmobile_expressive_grid_recycler);
                            if (delayedRv != null) {
                                setupBMobileExpressiveGridInternal(delayedRv, finalContext);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error in retry setupBMobileExpressiveGrid", e);
                        }
                    }, 300);
                }
                return;
            }
            
            setupBMobileExpressiveGridInternal(rv, finalContext);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up BMobile Expressive grid", e);
        }
    }
    
    private void setupBMobileExpressiveGridInternal(androidx.recyclerview.widget.RecyclerView rv, Context context) {
        try {
            android.app.Activity activity = getActivity();
            if (activity == null) {
                Log.w(TAG, "Activity is null");
                return;
            }
            
            // Setup layout manager
            androidx.recyclerview.widget.GridLayoutManager layoutManager = 
                    new androidx.recyclerview.widget.GridLayoutManager(context, 2);
            rv.setLayoutManager(layoutManager);

            // Create items list - same as BMobileExpressiveSettings
            java.util.List<com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem> items = new java.util.ArrayList<>();
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.network_dashboard_title,
                    R.string.summary_placeholder,
                    "com.android.settings.network.NetworkDashboardFragment",
                    R.drawable.ic_settings_wireless_filled));
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.custom_dashboard_title,
                    R.string.custom_dashboard_summary,
                    "com.epic.fragments.CustomDashboardSettings",
                    R.drawable.ic_settings_system_dashboard_filled));
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.connected_devices_dashboard_title,
                    R.string.connected_devices_dashboard_default_summary,
                    "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment",
                    R.drawable.ic_devices_other_filled));
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.configure_notification_settings,
                    R.string.notification_dashboard_summary,
                    "com.android.settings.notification.ConfigureNotificationSettings",
                    R.drawable.ic_notifications_filled));
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.sound_settings,
                    R.string.sound_dashboard_summary_with_dnd,
                    "com.android.settings.notification.SoundSettings",
                    R.drawable.ic_volume_up_filled));
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.display_settings,
                    R.string.display_dashboard_summary,
                    "com.android.settings.DisplaySettings",
                    R.drawable.ic_settings_display_filled));
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.power_usage_summary_title,
                    R.string.summary_placeholder,
                    "com.android.settings.fuelgauge.batteryusage.PowerUsageSummary",
                    R.drawable.ic_settings_battery_filled));
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.header_category_system,
                    R.string.system_dashboard_summary,
                    "com.android.settings.system.SystemDashboardFragment",
                    R.drawable.ic_settings_system_dashboard_filled));
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.security_settings_title,
                    R.string.security_dashboard_summary,
                    "com.android.settings.security.SecuritySettings",
                    R.drawable.ic_settings_security_filled));
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.privacy_dashboard_title,
                    R.string.privacy_dashboard_summary,
                    "com.android.settings.privacy.PrivacyDashboardFragment",
                    R.drawable.ic_settings_privacy_filled));
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.location_settings_title,
                    R.string.location_settings_loading_app_permission_stats,
                    "com.android.settings.location.LocationSettings",
                    R.drawable.ic_settings_location_filled));
            
            items.add(new com.epic.fragments.BMobileExpressiveSettingsAdapter.CardItem(
                    com.epic.fragments.BMobileExpressiveSettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.accessibility_settings,
                    R.string.accessibility_settings_summary,
                    "com.android.settings.accessibility.AccessibilitySettings",
                    R.drawable.ic_settings_accessibility_filled));

            rv.setAdapter(new com.epic.fragments.BMobileExpressiveSettingsAdapter(activity, items, getMetricsCategory()));
            Log.d(TAG, "BMobile Expressive grid setup completed successfully with " + items.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up BMobile Expressive grid internal", e);
        }
    }

    /**
     * Sets up the RecyclerView grid for Fun Display dashboard style.
     * Based on FunDisplaySettings implementation.
     */
    private void setupFunDisplayGrid() {
        try {
            Context context = getContext();
            if (context == null) {
                context = getActivity();
            }
            if (context == null) {
                Log.w(TAG, "Context is null, cannot setup fun display grid");
                return;
            }
            
            int currentStyle = DashboardStyleHelper.getDashboardStyle(context);
            final String gridPrefKey;
            final int gridRecyclerId;
            if (currentStyle == 7) {
                gridPrefKey = "fun_display_grid";
                gridRecyclerId = R.id.fun_display_grid_recycler;
            } else if (currentStyle == 13) {
                gridPrefKey = "afterlabs_grid";
                gridRecyclerId = R.id.afterlabs_grid_recycler;
            } else if (currentStyle == 14) {
                gridPrefKey = "infinity_home_grid";
                gridRecyclerId = R.id.bmobile_expressive_grid_recycler;
            } else {
                Log.d(TAG, "Not a grid homepage style, skipping setup");
                mFunDisplayGridSetupRetries = 0;
                return;
            }
            
            // Ensure we're on the main thread
            if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
                View view = getView();
                if (view != null) {
                    view.post(() -> setupFunDisplayGrid());
                }
                return;
            }
            
            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                Log.w(TAG, "PreferenceScreen is null");
                return;
            }
            
            androidx.preference.Preference layoutPref = screen.findPreference(gridPrefKey);
            if (layoutPref == null || !(layoutPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                Log.w(TAG, "Grid LayoutPreference not found: " + gridPrefKey);
                return;
            }
            
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) layoutPref;
            
            // Find RecyclerView - following MaterialDashboardGrid pattern
            androidx.recyclerview.widget.RecyclerView rv = lp.findViewById(gridRecyclerId);
            if (rv == null) {
                mFunDisplayGridSetupRetries++;
                if (mFunDisplayGridSetupRetries < MAX_FUN_DISPLAY_GRID_SETUP_RETRIES) {
                    Log.w(TAG, "Fun display grid RecyclerView not found, retrying... (" + mFunDisplayGridSetupRetries + "/" + MAX_FUN_DISPLAY_GRID_SETUP_RETRIES + ")");
                    // Retry after a short delay to ensure view is inflated
                    View rootView = getView();
                    if (rootView != null) {
                        rootView.postDelayed(() -> {
                            try {
                                setupFunDisplayGrid();
                            } catch (Exception e) {
                                Log.e(TAG, "Error in retry setupFunDisplayGrid", e);
                                mFunDisplayGridSetupRetries = 0; // Reset on error
                            }
                        }, 100);
                    }
                } else {
                    Log.e(TAG, "Fun display grid RecyclerView not found after " + MAX_FUN_DISPLAY_GRID_SETUP_RETRIES + " retries");
                    mFunDisplayGridSetupRetries = 0; // Reset after max retries
                }
                return;
            }
            
            // Reset retry counter on success
            mFunDisplayGridSetupRetries = 0;
            
            android.app.Activity activity = getActivity();
            if (activity == null) {
                Log.w(TAG, "Activity is null");
                return;
            }
            
            // Setup layout manager with error handling
            try {
                androidx.recyclerview.widget.GridLayoutManager layoutManager = 
                        new androidx.recyclerview.widget.GridLayoutManager(context, 2);
                rv.setLayoutManager(layoutManager);
            } catch (Exception e) {
                Log.e(TAG, "Error setting up GridLayoutManager for fun display grid", e);
                return;
            }

            // Create items list with error handling
            java.util.List<com.epic.fragments.FunDisplaySettingsAdapter.CardItem> items = new java.util.ArrayList<>();
            
            // Map all HomepagePreferences from top_level_settings_v2.xml to grid cards
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.network_dashboard_title,
                    R.string.summary_placeholder,
                    "com.android.settings.network.NetworkDashboardFragment",
                    R.drawable.ic_settings_wireless_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.custom_dashboard_title,
                    R.string.custom_dashboard_summary,
                    "com.epic.fragments.CustomDashboardSettings",
                    R.drawable.ic_settings_system_dashboard_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.connected_devices_dashboard_title,
                    R.string.connected_devices_dashboard_default_summary,
                    "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment",
                    R.drawable.ic_devices_other_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.configure_notification_settings,
                    R.string.notification_dashboard_summary,
                    "com.android.settings.notification.ConfigureNotificationSettings",
                    R.drawable.ic_notifications_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.sound_settings,
                    R.string.sound_dashboard_summary_with_dnd,
                    "com.android.settings.notification.SoundSettings",
                    R.drawable.ic_volume_up_filled));
            // Communal removed per user request
            // items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
            //         com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
            //         R.string.communal_settings_title,
            //         R.string.communal_settings_summary,
            //         "com.android.settings.communal.CommunalDashboardFragment",
            //         R.drawable.ia_settings_communal));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.display_settings,
                    R.string.display_dashboard_summary,
                    "com.android.settings.DisplaySettings",
                    R.drawable.ic_settings_display_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.storage_settings,
                    R.string.summary_placeholder,
                    "com.android.settings.deviceinfo.StorageDashboardFragment",
                    R.drawable.ic_storage_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.power_usage_summary_title,
                    R.string.summary_placeholder,
                    "com.android.settings.fuelgauge.batteryusage.PowerUsageSummary",
                    R.drawable.ic_settings_battery_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.header_category_system,
                    R.string.system_dashboard_summary,
                    "com.android.settings.system.SystemDashboardFragment",
                    R.drawable.ic_settings_system_dashboard_filled));
            // Safety Center removed per user request
            // items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
            //         com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
            //         R.string.safety_center_title,
            //         R.string.safety_center_summary,
            //         null,
            //         R.drawable.ic_settings_safety_center_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.security_settings_title,
                    R.string.security_dashboard_summary,
                    "com.android.settings.security.SecuritySettings",
                    R.drawable.ic_settings_security_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.privacy_dashboard_title,
                    R.string.privacy_dashboard_summary,
                    "com.android.settings.privacy.PrivacyDashboardFragment",
                    R.drawable.ic_settings_privacy_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.location_settings_title,
                    R.string.location_settings_loading_app_permission_stats,
                    "com.android.settings.location.LocationSettings",
                    R.drawable.ic_settings_location_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.emergency_settings_preference_title,
                    R.string.emergency_dashboard_summary,
                    "com.android.settings.emergency.EmergencyDashboardFragment",
                    R.drawable.ic_settings_emergency_filled));
            items.add(new com.epic.fragments.FunDisplaySettingsAdapter.CardItem(
                    com.epic.fragments.FunDisplaySettingsAdapter.CARD_TYPE_STANDARD,
                    R.string.accessibility_settings,
                    R.string.accessibility_settings_summary,
                    "com.android.settings.accessibility.AccessibilitySettings",
                    R.drawable.ic_settings_accessibility_filled));
            // Tips and Support removed per user request

            // Create adapter with error handling
            com.epic.fragments.FunDisplaySettingsAdapter adapter = null;
            try {
                adapter = new com.epic.fragments.FunDisplaySettingsAdapter(activity, items, getMetricsCategory());
            } catch (Exception e) {
                Log.e(TAG, "Error creating FunDisplaySettingsAdapter", e);
                mFunDisplayGridSetupRetries = 0; // Reset on error
                return;
            }
            
            if (adapter == null) {
                Log.e(TAG, "Adapter is null after creation");
                mFunDisplayGridSetupRetries = 0; // Reset on error
                return;
            }
            
            // Set adapter with error handling
            try {
                rv.setAdapter(adapter);
                Log.d(TAG, "Fun Display grid setup completed successfully with " + items.size() + " items");
            } catch (Exception e) {
                Log.e(TAG, "Error setting adapter on RecyclerView for fun display grid", e);
                mFunDisplayGridSetupRetries = 0; // Reset on error
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up fun display grid", e);
            mFunDisplayGridSetupRetries = 0; // Reset on error
        }
    }

    /** AfterLabs tab strip on V2 categories (style 12). */
    private void setupAfterlabsTabStrip() {
        try {
            if (DashboardStyleHelper.getDashboardStyle(getContext()) != 12) {
                return;
            }
            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                return;
            }
            androidx.preference.Preference tabPref = screen.findPreference("afterlabs_tab_strip");
            if (!(tabPref instanceof com.android.settingslib.widget.LayoutPreference)) {
                return;
            }
            com.android.settingslib.widget.LayoutPreference lp =
                    (com.android.settingslib.widget.LayoutPreference) tabPref;
            com.google.android.material.tabs.TabLayout tabLayout =
                    lp.findViewById(R.id.bmobile_afterlabs_tab_layout);
            if (tabLayout == null) {
                return;
            }
            final String[] categoryKeys = {
                    "top_level_connectivity_category",
                    "top_level_personalize_category",
                    "top_level_system_info_category",
                    "top_level_security_privacy_category"
            };
            final int[] tabTitles = {
                    R.string.afterlabs_tab_connectivity,
                    R.string.afterlabs_tab_personalize,
                    R.string.afterlabs_tab_system,
                    R.string.afterlabs_tab_security
            };
            tabLayout.removeAllTabs();
            for (int titleRes : tabTitles) {
                tabLayout.addTab(tabLayout.newTab().setText(titleRes));
            }
            androidx.preference.Preference accountCat =
                    screen.findPreference("top_level_account_category");
            androidx.preference.Preference supportCat =
                    screen.findPreference("top_level_support_category");
            java.util.function.Consumer<Integer> showTab = index -> {
                for (int i = 0; i < categoryKeys.length; i++) {
                    androidx.preference.Preference cat = screen.findPreference(categoryKeys[i]);
                    if (cat != null) {
                        if (i == index) {
                            screen.addPreference(cat);
                        } else {
                            screen.removePreference(cat);
                        }
                    }
                }
                if (accountCat != null) {
                    screen.removePreference(accountCat);
                }
                if (supportCat != null) {
                    screen.removePreference(supportCat);
                }
            };
            showTab.accept(0);
            tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout
                    .OnTabSelectedListener() {
                @Override
                public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    showTab.accept(tab.getPosition());
                }
                @Override
                public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                }
                @Override
                public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up AfterLabs tab strip", e);
        }
    }

    private void setupAfterlabsGrid() {
        setupFunDisplayGrid();
    }

    private void setupInfinityHomeGrid() {
        setupFunDisplayGrid();
        setupHomepageWidgetsClickListeners(getView());
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DASHBOARD_SUMMARY;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Set dashboard style before calling getPreferenceScreenResId()
        // Based on RisingOS pattern: https://github.com/RisingOS-Revived/android_packages_apps_Settings/commit/013b309d1f88ac0bfd1645b71c362501515cd34f
        setDashboardStyle(context);
        HighlightableMenu.fromXml(context, getPreferenceScreenResId());
        // SupportPreferenceController may not exist for all dashboard styles (e.g., Fun Display)
        try {
            com.android.settings.support.SupportPreferenceController supportController = 
                    use(com.android.settings.support.SupportPreferenceController.class);
            if (supportController != null) {
                supportController.setActivity(getActivity());
            }
        } catch (Exception e) {
            Log.w(TAG, "SupportPreferenceController not available for this dashboard style", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Check if dashboard style changed while fragment was paused
        // Use helper to detect changes and recreate activity if needed
        Context context = getContext();
        
        // Check compact dashboard setting again in case it was changed
        if (context != null) {
            boolean compactEnabled = Settings.System.getIntForUser(
                    context.getContentResolver(),
                    Settings.System.SETTINGS_COMPACT_DASHBOARD_ENABLED,
                    0,
                    android.os.UserHandle.USER_CURRENT) == 1;
            
            View rootView = getView();
            if (rootView != null && compactEnabled) {
                rootView.post(new Runnable() {
                    @Override
                    public void run() {
                        setupCompactDashboardGrid();
                    }
                });
            }
        }
        
        if (context != null) {
            int currentStyle = DashboardStyleHelper.getDashboardStyle(context);
            if (currentStyle != mDashBoardStyle) {
                // Style changed, need to reload the activity to apply new XML
                Log.d(TAG, "Dashboard style changed in onResume: " + mDashBoardStyle + " -> " + currentStyle);
                mDashBoardStyle = currentStyle;
                // Recreate activity to fully reload with new dashboard style
                // This ensures getPreferenceScreenResId() is called again with the new style
                if (getActivity() != null) {
                    getActivity().recreate();
                    return; // Don't continue after recreate
                }
            }
            
            // Center text for AOSP style (0) after views are created
            if (currentStyle == 0) {
                View rootView = getView();
                if (rootView != null) {
                    rootView.post(new Runnable() {
                        @Override
                        public void run() {
                            centerTextInViews();
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getHelpResource() {
        // Disable the help icon because this page uses a full search view in actionbar.
        return 0;
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        // Handle MicroG Settings intent launch
        if ("top_level_microg".equals(preference.getKey())) {
            try {
                Intent microgIntent = new Intent();
                microgIntent.setAction("android.intent.action.VIEW");
                microgIntent.setComponent(new ComponentName("com.google.android.gms",
                        "org.microg.gms.ui.SettingsActivity"));
                microgIntent.addCategory("android.intent.category.DEFAULT");
                startActivity(microgIntent);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error launching MicroG Settings", e);
                return false;
            }
        }
        if (isDuplicateClick(preference)) {
            return true;
        }

        // Register SplitPairRule for SubSettings.
        ActivityEmbeddingRulesController.registerSubSettingsPairRule(getContext(),
                true /* clearTop */);

        setHighlightPreferenceKey(preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        new SubSettingLauncher(getActivity())
                .setDestination(pref.getFragment())
                .setArguments(pref.getExtras())
                .setSourceMetricsCategory(caller instanceof Instrumentable
                        ? ((Instrumentable) caller).getMetricsCategory()
                        : Instrumentable.METRICS_CATEGORY_UNKNOWN)
                .setTitleRes(-1)
                .setIsSecondLayerPage(true)
                .launch();
        return true;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mIsEmbeddingActivityEnabled =
                ActivityEmbeddingUtils.isEmbeddingActivityEnabled(getContext());
        if (!mIsEmbeddingActivityEnabled) {
            return;
        }

        boolean activityEmbedded = isActivityEmbedded();
        if (icicle != null) {
            mHighlightMixin = icicle.getParcelable(SAVED_HIGHLIGHT_MIXIN);
            if (mHighlightMixin != null) {
                mScrollNeeded = !mHighlightMixin.isActivityEmbedded() && activityEmbedded;
                mHighlightMixin.setActivityEmbedded(activityEmbedded);
            }
        }
        if (mHighlightMixin == null) {
            mHighlightMixin = new TopLevelHighlightMixin(activityEmbedded);
        }
    }

    /** Wrap ActivityEmbeddingController#isActivityEmbedded for testing. */
    @VisibleForTesting
    public boolean isActivityEmbedded() {
        if (mActivityEmbeddingController == null) {
            mActivityEmbeddingController = ActivityEmbeddingController.getInstance(getActivity());
        }
        return mActivityEmbeddingController.isActivityEmbedded(getActivity());
    }


    private boolean isOnlyOneActivityInTask() {
        final ActivityManager.RunningTaskInfo taskInfo = getSystemService(ActivityManager.class)
                .getRunningTasks(1).get(0);
        return taskInfo.numActivities == 1;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mHighlightMixin != null) {
            outState.putParcelable(SAVED_HIGHLIGHT_MIXIN, mHighlightMixin);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // getPreferenceScreenResId() will be called by super.onCreatePreferences()
        // It now reads from Settings directly if needed, so we don't need to set it here
        // This simplifies the code and ensures consistency
        super.onCreatePreferences(savedInstanceState, rootKey);
        if (Flags.homepageRevamp()) {
            return;
        }
        
        // Center text for AOSP style (style 0)
        Context context = getContext();
        if (context != null) {
            int currentStyle = DashboardStyleHelper.getDashboardStyle(context);
            if (currentStyle == 0) {
                centerTextForAOSP();
            }
        }
        
        // Hide icons and summaries for epic style (style 1)
        if (context != null) {
            int currentStyle = DashboardStyleHelper.getDashboardStyle(context);
            if (currentStyle == 1) { // Epic style
                iteratePreferences(preference -> {
                    // Hide icon
                    if (preference instanceof com.android.settings.widget.HomepagePreference) {
                        ((com.android.settings.widget.HomepagePreference) preference).getHelper().setIconVisible(false);
                    } else {
                        preference.setIcon(null);
                    }
                    // Hide summary
                    preference.setSummary("");
                });
            } else {
                // For other styles, apply normal icon tinting
                int tintColor = Utils.getHomepageIconColor(getContext());
                iteratePreferences(preference -> {
                    Drawable icon = preference.getIcon();
                    if (icon != null) {
                        icon.setTint(tintColor);
                    }
                });
            }
        } else {
            // Fallback if context is null
            int tintColor = Utils.getHomepageIconColor(getContext());
            iteratePreferences(preference -> {
                Drawable icon = preference.getIcon();
                if (icon != null) {
                    icon.setTint(tintColor);
                }
            });
        }
        
        onSetPrefCard();
    }
    
    /**
     * Centers all text in preferences for AOSP style
     * This is done by iterating through preferences after they're bound to views
     */
    private void centerTextForAOSP() {
        try {
            PreferenceScreen screen = getPreferenceScreen();
            if (screen != null) {
                // Text centering will be handled in onResume after views are created
                // We'll use a post to ensure views are bound
                View rootView = getView();
                if (rootView != null) {
                    rootView.post(new Runnable() {
                        @Override
                        public void run() {
                            centerTextInViews();
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error centering text for AOSP style", e);
        }
    }
    
    /**
     * Centers text in all preference views for AOSP style
     */
    private void centerTextInViews() {
        try {
            PreferenceScreen screen = getPreferenceScreen();
            if (screen != null) {
                iteratePreferences(preference -> {
                    if (preference instanceof com.android.settings.widget.HomepagePreference) {
                        // Find the preference view and center its text
                        android.view.View view = getListView().findViewWithTag(preference);
                        if (view != null) {
                            android.widget.TextView titleView = view.findViewById(android.R.id.title);
                            if (titleView != null) {
                                titleView.setGravity(android.view.Gravity.CENTER);
                                titleView.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
                            }
                            android.widget.TextView summaryView = view.findViewById(android.R.id.summary);
                            if (summaryView != null) {
                                summaryView.setGravity(android.view.Gravity.CENTER);
                                summaryView.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error centering text in views", e);
        }
    }

    private void onSetPrefCard() {
        // Epic style (1) uses its own XML structure, no layout changes needed
        // V2 style (2) uses its own XML structure, no layout changes needed
        // AOSP style (0) uses default XML structure
        // This method is kept for potential future use
        return;
    }
    
    private void setDashboardStyle(Context context) {
        // Use helper to read dashboard style
        mDashBoardStyle = DashboardStyleHelper.getDashboardStyle(context);
        Log.d(TAG, "Dashboard style set to: " + mDashBoardStyle);
    }

    private void onUserCard() {
        final Preference pref = getPreferenceScreen() != null
                ? getPreferenceScreen().findPreference(KEY_USER_CARD)
                : null;
        if (!(pref instanceof LayoutPreference)) {
            return;
        }

        final LayoutPreference headerPreference = (LayoutPreference) pref;
        final View userCard = headerPreference.findViewById(R.id.entity_header);
        if (userCard == null) {
            return;
        }

        final Activity context = getActivity();
        if (context == null) {
            return;
        }

        final TextView textview = headerPreference.findViewById(R.id.summary);
        final Bundle bundle = getArguments();

        final EntityHeaderController controller = EntityHeaderController
                .newInstance(context, this, userCard)
                .setButtonActions(EntityHeaderController.ActionType.ACTION_NONE,
                        EntityHeaderController.ActionType.ACTION_NONE);

        userCard.setOnClickListener(v -> {
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings$UserSettingsActivity"));
            startActivity(intent);
        });

        final int iconId = bundle != null ? bundle.getInt("icon_id", 0) : 0;
        if (iconId == 0) {
            final UserManager userManager = (UserManager) context.getSystemService(
                    Context.USER_SERVICE);
            if (userManager != null) {
                final UserInfo info = Utils.getExistingUser(userManager,
                        android.os.Process.myUserHandle());
                if (info != null) {
                    controller.setLabel(info.name);
                    controller.setIcon(
                            com.android.settingslib.Utils.getUserIcon(context, userManager, info));
                }
            }
        }

        controller.done(context);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        highlightPreferenceIfNeeded();
    }

    @Override
    public void onSplitLayoutChanged(boolean isRegularLayout) {
        iteratePreferences(preference -> {
            if (preference instanceof HomepagePreferenceLayout) {
                ((HomepagePreferenceLayout) preference).getHelper().setIconVisible(isRegularLayout);
            }
        });
    }

    @Override
    public void highlightPreferenceIfNeeded() {
        if (mHighlightMixin != null) {
            mHighlightMixin.highlightPreferenceIfNeeded();
        }
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState) {
        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent,
                savedInstanceState);
        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.setHorizontalScrollBarEnabled(false);
        recyclerView.setPadding(mPaddingHorizontal, 0, mPaddingHorizontal, 0);
        return recyclerView;
    }

    /** Sets the horizontal padding */
    public void setPaddingHorizontal(int padding) {
        mPaddingHorizontal = padding;
        RecyclerView recyclerView = getListView();
        if (recyclerView != null) {
            recyclerView.setPadding(padding, 0, padding, 0);
        }
    }

    /** Updates the preference internal paddings */
    public void updatePreferencePadding(boolean isTwoPane) {
        iteratePreferences(new PreferenceJob() {
            private int mIconPaddingStart;
            private int mTextPaddingStart;

            @Override
            public void init() {
                mIconPaddingStart = getResources().getDimensionPixelSize(isTwoPane
                        ? R.dimen.homepage_preference_icon_padding_start_two_pane
                        : R.dimen.homepage_preference_icon_padding_start);
                mTextPaddingStart = getResources().getDimensionPixelSize(isTwoPane
                        ? R.dimen.homepage_preference_text_padding_start_two_pane
                        : R.dimen.homepage_preference_text_padding_start);
            }

            @Override
            public void doForEach(Preference preference) {
                if (preference instanceof HomepagePreferenceLayout) {
                    ((HomepagePreferenceLayout) preference).getHelper()
                            .setIconPaddingStart(mIconPaddingStart);
                    ((HomepagePreferenceLayout) preference).getHelper()
                            .setTextPaddingStart(mTextPaddingStart);
                }
            }
        });
    }

    /** Returns a {@link TopLevelHighlightMixin} that performs highlighting */
    public TopLevelHighlightMixin getHighlightMixin() {
        return mHighlightMixin;
    }

    /** Highlight a preference with specified preference key */
    public void setHighlightPreferenceKey(String prefKey) {
        // Skip Tips & support since it's full screen
        if (mHighlightMixin != null && !TextUtils.equals(prefKey, PREF_KEY_SUPPORT)) {
            mHighlightMixin.setHighlightPreferenceKey(prefKey);
        }
    }

    /** Returns whether clicking the specified preference is considered as a duplicate click. */
    public boolean isDuplicateClick(Preference pref) {
        /* Return true if
         * 1. the device supports activity embedding, and
         * 2. the target preference is highlighted, and
         * 3. the current activity is embedded */
        return mHighlightMixin != null
                && TextUtils.equals(pref.getKey(), mHighlightMixin.getHighlightPreferenceKey())
                && isActivityEmbedded();
    }

    /** Show/hide the highlight on the menu entry for the search page presence */
    public void setMenuHighlightShowed(boolean show) {
        if (mHighlightMixin != null) {
            mHighlightMixin.setMenuHighlightShowed(show);
        }
    }

    /** Highlight and scroll to a preference with specified menu key */
    public void setHighlightMenuKey(String menuKey, boolean scrollNeeded) {
        if (mHighlightMixin != null) {
            mHighlightMixin.setHighlightMenuKey(menuKey, scrollNeeded);
        }
    }

    @Override
    protected boolean shouldForceRoundedIcon() {
        return getContext().getResources()
                .getBoolean(R.bool.config_force_rounded_icon_TopLevelSettings);
    }

    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        if (mIsEmbeddingActivityEnabled && (getActivity() instanceof SettingsHomepageActivity)) {
            return mHighlightMixin.onCreateAdapter(this, preferenceScreen, mScrollNeeded);
        }

        if (Flags.homepageRevamp()) {
            return new RoundCornerPreferenceAdapter(preferenceScreen);
        }
        return super.onCreateAdapter(preferenceScreen);
    }

    @Override
    protected Preference createPreference(Tile tile) {
        return new HomepagePreference(getPrefContext());
    }

    void reloadHighlightMenuKey() {
        if (mHighlightMixin != null) {
            mHighlightMixin.reloadHighlightMenuKey(getArguments());
        }
    }

    private void iteratePreferences(PreferenceJob job) {
        if (job == null || getPreferenceManager() == null) {
            return;
        }
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            return;
        }

        job.init();
        iteratePreferences(screen, job);
    }

    private void iteratePreferences(PreferenceGroup group, PreferenceJob job) {
        int count = group.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference preference = group.getPreference(i);
            job.doForEach(preference);
            if (preference instanceof PreferenceCategory) {
                iteratePreferences((PreferenceCategory) preference, job);
            }
        }
    }

    private interface PreferenceJob {
        default void init() {
        }

        void doForEach(Preference preference);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.top_level_settings) {

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    // Never searchable, all entries in this page are already indexed elsewhere.
                    return false;
                }
            };
}
