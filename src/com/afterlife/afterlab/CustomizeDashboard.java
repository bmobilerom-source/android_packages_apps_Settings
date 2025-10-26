/*
 * Copyright (C) 2020 Project-Awaken
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
package com.afterlife.afterlab;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.LayoutPreference;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;

import com.android.internal.logging.nano.MetricsProto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class CustomizeDashboard extends SettingsPreferenceFragment { 
	
	private PreferenceScreen mPreferenceScreen;
	private LayoutPreference mTabPreference;
	private TabLayout mTabLayout;

    @Override
    public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (afterLabsStyle() == 1) {
			addPreferencesFromResource(R.xml.customize_dashboard_grid);
			
			mPreferenceScreen = getPreferenceScreen();
			
			//Tab Pref//
			mTabPreference = findPreference("declan_tab_layout");
			mTabLayout = mTabPreference.findViewById(R.id.afterlifexdeclan_tab_layout);
			TabLayout.Tab uiTab = mTabLayout.newTab();
			uiTab.setText("Themes");
			TabLayout.Tab systemTab = mTabLayout.newTab();
			systemTab.setText("System");
			TabLayout.Tab generalTab = mTabLayout.newTab();
			generalTab.setText("General");
			mTabLayout.addTab(uiTab, 0);
			mTabLayout.addTab(systemTab, 1);
			mTabLayout.addTab(generalTab, 2);
			
			// Find all preferences in each section
			Preference uiHeader = findPreference("ui_header");
			Preference statusbar = findPreference("statusbar_category");
			Preference quickSettings = findPreference("quick_settings_category");
			Preference battery = findPreference("battery_category");
			Preference lockscreen = findPreference("lockscreen_category");
			
			Preference systemHeader = findPreference("system_header");
			Preference notifications = findPreference("notifications_category");
			Preference buttons = findPreference("buttons_category");
			Preference gesture = findPreference("gesture_category");
			Preference powermenu = findPreference("powermenu_category");
			
			Preference generalHeader = findPreference("general_header");
			Preference misc = findPreference("misc_category");
			Preference system = findPreference("system_category");
			
			// Initially hide system and general sections
			hidePreference(systemHeader);
			hidePreference(notifications);
			hidePreference(buttons);
			hidePreference(gesture);
			hidePreference(powermenu);
			hidePreference(generalHeader);
			hidePreference(misc);
			hidePreference(system);
			
			TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
				@Override
				public void onTabSelected(Tab tab) {
					if (tab.getPosition() == 0) {
						// Themes tab
						showPreference(uiHeader);
						showPreference(statusbar);
						showPreference(quickSettings);
						showPreference(battery);
						showPreference(lockscreen);
						hidePreference(systemHeader);
						hidePreference(notifications);
						hidePreference(buttons);
						hidePreference(gesture);
						hidePreference(powermenu);
						hidePreference(generalHeader);
						hidePreference(misc);
						hidePreference(system);
					} else if (tab.getPosition() == 1) {
						// System tab
						hidePreference(uiHeader);
						hidePreference(statusbar);
						hidePreference(quickSettings);
						hidePreference(battery);
						hidePreference(lockscreen);
						showPreference(systemHeader);
						showPreference(notifications);
						showPreference(buttons);
						showPreference(gesture);
						showPreference(powermenu);
						hidePreference(generalHeader);
						hidePreference(misc);
						hidePreference(system);
					} else if (tab.getPosition() == 2) {
						// General tab
						hidePreference(uiHeader);
						hidePreference(statusbar);
						hidePreference(quickSettings);
						hidePreference(battery);
						hidePreference(lockscreen);
						hidePreference(systemHeader);
						hidePreference(notifications);
						hidePreference(buttons);
						hidePreference(gesture);
						hidePreference(powermenu);
						showPreference(generalHeader);
						showPreference(misc);
						showPreference(system);
					}
				}
				
				@Override
				public void onTabUnselected(Tab tab) {
				}
				
				@Override
				public void onTabReselected(Tab tab) {
				}
			};
			
			onTabSelectedListener.onTabSelected(mTabLayout.getTabAt(0));
			mTabLayout.addOnTabSelectedListener(onTabSelectedListener);
		} else {
			addPreferencesFromResource(R.xml.customize_dashboard);
		}
	}

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
	
	private int afterLabsStyle() {
		return Settings.System.getInt(getContentResolver(), "declanxafterlab_style", 0);
	}
	
	private void showPreference(Preference preference) {
		if (preference != null && mPreferenceScreen.findPreference(preference.getKey()) == null) {
			mPreferenceScreen.addPreference(preference);
		}
	}
	
	private void hidePreference(Preference preference) {
		if (preference != null && mPreferenceScreen.findPreference(preference.getKey()) != null) {
			mPreferenceScreen.removePreference(preference);
		}
	}

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		RecyclerView recyclerView = super.onCreateRecyclerView(inflater, container, savedInstanceState);
		GridLayoutManager gridManager = new GridLayoutManager(getActivity(), 2);
		gridManager.setSpanSizeLookup(new AfterlifeSpanSizeGD());
		GridLayoutManager semiGridManager = new GridLayoutManager(getActivity(), 2);
		semiGridManager.setSpanSizeLookup(new AfterlifeSpanSizeSG());
		if (afterLabsStyle() == 0) {
			recyclerView.setLayoutManager(gridManager);
		} else if (afterLabsStyle() == 1) {
			recyclerView.setLayoutManager(semiGridManager);
		}
		return recyclerView;
	}

    class AfterlifeSpanSizeGD extends GridLayoutManager.SpanSizeLookup {
		@Override
		public int getSpanSize(int position) {
		    if (position == 2 || position == 3 || position == 4 || position == 5 || position == 6 || position == 7 || position == 8 || position == 9 || position == 10 || position == 11) {
				return 1;
			} else {
				return 2;
			}
		}
	}
	
	class AfterlifeSpanSizeSG extends GridLayoutManager.SpanSizeLookup {
		@Override
		public int getSpanSize(int position) {
			// Full span (2 columns): header (0), tabs (1), section headers (2, 7, 12)
			if (position == 0 || position == 1 || position == 2 || position == 7 || position == 12) {
				return 2;
			} else {
				// Cards: span 1 (one column each)
				return 1;
			}
		}
	}

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.customize_dashboard;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
} 