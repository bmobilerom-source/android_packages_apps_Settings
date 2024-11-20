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
import android.os.UserManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.HashMap;
import java.util.Map;

@SearchIndexable(forTarget = MOBILE)
public class TopLevelSettings extends DashboardFragment implements SplitLayoutListener,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TAG = "TopLevelSettings";
    private static final String SAVED_HIGHLIGHT_MIXIN = "highlight_mixin";
    private static final String PREF_KEY_SUPPORT = "top_level_support";
    private static final String KEY_USER_CARD = "top_level_usercard";

    private boolean mIsEmbeddingActivityEnabled;
    private TopLevelHighlightMixin mHighlightMixin;
    private int mPaddingHorizontal;
    private boolean mScrollNeeded = true;
    private boolean mFirstStarted = true;
    private ActivityEmbeddingController mActivityEmbeddingController;

    public TopLevelSettings() {
        final Bundle args = new Bundle();
        // Disable the search icon because this page uses a full search view in actionbar.
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
        if (declanXafterlifeStyle() == 1) {
			return R.xml.top_level_settings_second;/*  */
		} else {
			return R.xml.top_level_settings_v2;
		}
    }
    
    private int declanXafterlifeStyle() {
		return Settings.System.getInt(getContentResolver(), "afl_dashboard_style", 0);
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
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
    public int getMetricsCategory() {
        return SettingsEnums.DASHBOARD_SUMMARY;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        HighlightableMenu.fromXml(context, getPreferenceScreenResId());
        use(SupportPreferenceController.class).setActivity(getActivity());
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
        super.onCreatePreferences(savedInstanceState, rootKey);
        if (Flags.homepageRevamp()) {
            return;
        }
        int tintColor = Utils.getHomepageIconColor(getContext());
        iteratePreferences(preference -> {
            Drawable icon = preference.getIcon();
            if (icon != null) {
                icon.setTint(tintColor);
            }
        });
        
        onSetPrefCard();
    }

    private void onSetPrefCard() {
    final PreferenceScreen screen = getPreferenceScreen();
    final int count = screen.getPreferenceCount();
    
    // Mapping key to layouts
    Map<String, Integer> layoutMapStyle0 = new HashMap<>();
    layoutMapStyle0.put("top_level_usercard", R.layout.preference_usercard);
    layoutMapStyle0.put("top_level_about_device", R.layout.afterlife_cardview_single_left);
    layoutMapStyle0.put("top_level_afterlab", R.layout.afterlife_cardview_single_right);
    layoutMapStyle0.put("airplane_mode", R.layout.afterlife_cardview_top_switch);
    layoutMapStyle0.put("internet_settings", R.layout.afterlife_cardview_middle);
    layoutMapStyle0.put("tether_settings", R.layout.afterlife_cardview_middle);
    layoutMapStyle0.put("top_level_connected_devices", R.layout.afterlife_cardview_bottom);
    layoutMapStyle0.put("top_level_apps", R.layout.afterlife_cardview_top);
	layoutMapStyle0.put("top_level_notifications", R.layout.afterlife_cardview_middle);
	layoutMapStyle0.put("top_level_sound", R.layout.afterlife_cardview_middle);
	layoutMapStyle0.put("top_level_display", R.layout.afterlife_cardview_middle);
	layoutMapStyle0.put("top_level_wallpaper", R.layout.afterlife_cardview_bottom);
	layoutMapStyle0.put("top_level_battery", R.layout.afterlife_cardview_top);
	layoutMapStyle0.put("top_level_storage", R.layout.afterlife_cardview_bottom);
	layoutMapStyle0.put("top_level_accessibility", R.layout.afterlife_cardview_top);
	layoutMapStyle0.put("top_level_security", R.layout.afterlife_cardview_middle);
	layoutMapStyle0.put("top_level_privacy", R.layout.afterlife_cardview_middle);
	layoutMapStyle0.put("top_level_location", R.layout.afterlife_cardview_bottom);
	layoutMapStyle0.put("top_level_emergency", R.layout.afterlife_cardview_top);
	layoutMapStyle0.put("top_level_accounts", R.layout.afterlife_cardview_middle);
	layoutMapStyle0.put("top_level_system", R.layout.afterlife_cardview_bottom);
	layoutMapStyle0.put("dashboard_tile_pref_com.google.android.apps.wellbeing.settings.TopLevelSettingsActivity", R.layout.afterlife_cardview_middle);
	layoutMapStyle0.put("dashboard_tile_pref_com.google.android.gms.app.settings.GoogleSettingsIALink", R.layout.afterlife_cardview_middle);
	layoutMapStyle0.put("top_level_google", R.layout.afterlife_cardview_middle);
	layoutMapStyle0.put("dashboard_tile_pref_com.google.android.apps.wellbeing.home.TopLevelSettingsActivity", R.layout.afterlife_cardview_middle);
	layoutMapStyle0.put("top_level_wellbeing", R.layout.afterlife_cardview_middle);

    Map<String, Integer> layoutMapStyle1 = new HashMap<>();
    layoutMapStyle1.put("airplane_mode", R.layout.afterlife_card_homepage_top_switch);
    layoutMapStyle1.put("internet_settings", R.layout.afterlife_card_homepage_middle);
    layoutMapStyle1.put("tether_settings", R.layout.afterlife_card_homepage_middle);
    layoutMapStyle1.put("top_level_connected_devices", R.layout.afterlife_card_homepage_bottom);
	layoutMapStyle1.put("top_level_apps", R.layout.afterlife_card_homepage_top);
	layoutMapStyle1.put("top_level_notifications", R.layout.afterlife_card_homepage_middle);
	layoutMapStyle1.put("top_level_sound", R.layout.afterlife_card_homepage_middle);
	layoutMapStyle1.put("top_level_display", R.layout.afterlife_card_homepage_middle);
	layoutMapStyle1.put("top_level_wallpaper", R.layout.afterlife_card_homepage_bottom);
	layoutMapStyle1.put("top_level_battery", R.layout.afterlife_card_homepage_top);
	layoutMapStyle1.put("top_level_storage", R.layout.afterlife_card_homepage_bottom);
	layoutMapStyle1.put("top_level_accessibility", R.layout.afterlife_card_homepage_top);
	layoutMapStyle1.put("top_level_security", R.layout.afterlife_card_homepage_middle);
	layoutMapStyle1.put("top_level_privacy", R.layout.afterlife_card_homepage_middle);
	layoutMapStyle1.put("top_level_location", R.layout.afterlife_card_homepage_bottom);
	layoutMapStyle1.put("top_level_emergency", R.layout.afterlife_card_homepage_top);
	layoutMapStyle1.put("top_level_accounts", R.layout.afterlife_card_homepage_middle);
	layoutMapStyle1.put("top_level_system", R.layout.afterlife_card_homepage_middle);
	layoutMapStyle1.put("top_level_about_settings", R.layout.afterlife_card_homepage_bottom);
	layoutMapStyle1.put("dashboard_tile_pref_com.google.android.apps.wellbeing.settings.TopLevelSettingsActivity", R.layout.afterlife_card_homepage_middle);
	layoutMapStyle1.put("dashboard_tile_pref_com.google.android.gms.app.settings.GoogleSettingsIALink", R.layout.afterlife_card_homepage_middle);
	layoutMapStyle1.put("top_level_google", R.layout.afterlife_card_homepage_middle);
	layoutMapStyle1.put("dashboard_tile_pref_com.google.android.apps.wellbeing.home.TopLevelSettingsActivity", R.layout.afterlife_card_homepage_middle);
	layoutMapStyle1.put("top_level_wellbeing", R.layout.afterlife_card_homepage_middle);
	

    int defaultLayoutStyle0 = R.layout.afterlife_cardview_middle;
    int defaultLayoutStyle1 = R.layout.afterlife_card_homepage_middle;

    for (int i = 0; i < count; i++) {
        final Preference preference = screen.getPreference(i);
        String key = preference.getKey();

        if (declanXafterlifeStyle() == 0) {
            // Get layout for style 0 or fallback to default
            int layout = layoutMapStyle0.getOrDefault(key, defaultLayoutStyle0);
            preference.setLayoutResource(layout);
        } else if (declanXafterlifeStyle() == 1) {
            // Get layout for style 1 or fallback to default
            int layout = layoutMapStyle1.getOrDefault(key, defaultLayoutStyle1);
            preference.setLayoutResource(layout);
        } else {
            // Fallback for unknown styles
            preference.setLayoutResource(defaultLayoutStyle0);
        }
    }
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
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
		layoutManager.setSpanSizeLookup(new DeclanSpanSizeOP());
		if (declanXafterlifeStyle() == 0) {
			recyclerView.setLayoutManager(layoutManager);
		}
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

    private void onUserCard() {
        if (declanXafterlifeStyle() == 0) {
            final LayoutPreference headerPreference =
                    (LayoutPreference) getPreferenceScreen().findPreference(KEY_USER_CARD);
            final View userCard = headerPreference.findViewById(R.id.entity_header);
            final TextView textview = headerPreference.findViewById(R.id.summary);
            final Activity context = getActivity(); final Bundle bundle = getArguments();
            final EntityHeaderController controller = EntityHeaderController
                    .newInstance(context, this, userCard)
                    .setRecyclerView(getListView(), getSettingsLifecycle())
                    .setButtonActions(EntityHeaderController.ActionType.ACTION_NONE,
                            EntityHeaderController.ActionType.ACTION_NONE);

            userCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$UserSettingsActivity"));
                    startActivity(intent);

                }
            });

            final int iconId = bundle.getInt("icon_id", 0);
            if (iconId == 0) {
                final UserManager userManager = (UserManager) getActivity().getSystemService(
                    Context.USER_SERVICE);
                final UserInfo info = Utils.getExistingUser(userManager,
                    android.os.Process.myUserHandle());
                controller.setLabel(info.name);
                controller.setIcon(
                    com.android.settingslib.Utils.getUserIcon(getActivity(), userManager, info));
            }

            controller.done(true /* rebindActions */);
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
            new BaseSearchIndexProvider(
                    Flags.homepageRevamp()
                            ? R.xml.top_level_settings_v2
                            : R.xml.top_level_settings) {

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    // Never searchable, all entries in this page are already indexed elsewhere.
                    return false;
                }
            };
}