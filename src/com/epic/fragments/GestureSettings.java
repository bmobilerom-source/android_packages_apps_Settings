/*
 * Copyright (C) 2022 EpicROM-AOSP
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.epic.fragments;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import android.content.ContentResolver;
import android.content.Context;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.lifecycle.ViewModelProvider;

import com.android.internal.logging.nano.MetricsProto;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.safetycenter.SafetyHubCardPreference;

public class GestureSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SAFETY_STATUS_KEY = "safety_status";
    private static final String QUICK_ACCESS_KEY = "quick_access_grid";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.anatolia_settings_gestures);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        // Ensure the Safety Hub card is present and shows a default state even without Safety Center
        Preference basePref = prefScreen.findPreference(SAFETY_STATUS_KEY);
        if (basePref instanceof SafetyHubCardPreference) {
            ((SafetyHubCardPreference) basePref).setStatus(
                    null /* title -> default */, null /* summary -> default */, null /* intent -> default */);
        }

        // Initialize Safety Status Preference: observe Safety Center ViewModel reflectively and feed our proxy card
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            try {
                Preference safetyStatusPref = prefScreen.findPreference(SAFETY_STATUS_KEY);
                if (safetyStatusPref != null && getActivity() != null) {
                    // Get SafetyCenterViewModel using reflection
                    Class<?> viewModelFactoryClass = Class.forName(
                            "com.android.permissioncontroller.safetycenter.ui.model.LiveSafetyCenterViewModelFactory");
                    Object factory = viewModelFactoryClass.getConstructor(
                            android.app.Application.class).newInstance(getActivity().getApplication());
                    
                    Class<?> viewModelClass = Class.forName(
                            "com.android.permissioncontroller.safetycenter.ui.model.SafetyCenterViewModel");
                    ViewModelProvider provider = new ViewModelProvider(getActivity(), 
                            (androidx.lifecycle.ViewModelProvider.Factory) factory);
                    // Use reflection to call get() method with proper generic type
                    java.lang.reflect.Method getMethod = ViewModelProvider.class.getMethod("get", Class.class);
                    Object viewModel = getMethod.invoke(provider, viewModelClass);

                    // Observe StatusUiData changes
                    java.lang.reflect.Method getStatusUiLiveDataMethod = viewModelClass.getMethod("getStatusUiLiveData");
                    Object liveData = getStatusUiLiveDataMethod.invoke(viewModel);
                    
                    // Create observer using reflection
                    Class<?> statusUiDataClass = Class.forName(
                            "com.android.permissioncontroller.safetycenter.ui.model.StatusUiData");
                    java.lang.reflect.Method observeMethod = liveData.getClass().getMethod("observe",
                            androidx.lifecycle.LifecycleOwner.class, androidx.lifecycle.Observer.class);
                    
                    // Create observer lambda mapping into our proxy card
                    androidx.lifecycle.Observer observer = (androidx.lifecycle.Observer<Object>) statusUiData -> {
                        try {
                            CharSequence title = "Safety status";
                            CharSequence summary = null;
                            android.content.Intent primaryIntent = new android.content.Intent("android.settings.SAFETY_CENTER")
                                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

                            try {
                                java.lang.reflect.Method getTitle = statusUiDataClass.getMethod("getTitle");
                                Object t = getTitle.invoke(statusUiData);
                                if (t instanceof CharSequence) title = (CharSequence) t;
                            } catch (NoSuchMethodException ignored) {}
                            try {
                                java.lang.reflect.Method getSummary = statusUiDataClass.getMethod("getSummary");
                                Object s = getSummary.invoke(statusUiData);
                                if (s instanceof CharSequence) summary = (CharSequence) s;
                            } catch (NoSuchMethodException ignored) {}
                            try {
                                java.lang.reflect.Method getPrimaryActionIntent = statusUiDataClass.getMethod("getPrimaryActionIntent");
                                Object i = getPrimaryActionIntent.invoke(statusUiData);
                                if (i instanceof android.content.Intent) primaryIntent = (android.content.Intent) i;
                            } catch (NoSuchMethodException ignored) {}

                            if (safetyStatusPref instanceof com.android.settings.safetycenter.SafetyHubCardPreference) {
                                ((com.android.settings.safetycenter.SafetyHubCardPreference) safetyStatusPref)
                                        .setStatus(title, summary, primaryIntent);
                            }
                        } catch (Exception e) {
                            // Ignore reflection errors
                        }
                    };
                    
                    observeMethod.invoke(liveData, this, observer);
                }
            } catch (Exception e) {
                // Do not remove the card; leave default content and action
            }
        }
    }

    @Override
    public void onViewCreated(android.view.View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Wire up the quick access grid (2x2) so it becomes visible and populated
        try {
            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) return;
            androidx.preference.Preference layoutPref = screen.findPreference(QUICK_ACCESS_KEY);
            if (layoutPref instanceof com.android.settingslib.widget.LayoutPreference) {
                com.android.settingslib.widget.LayoutPreference lp =
                        (com.android.settingslib.widget.LayoutPreference) layoutPref;
                androidx.recyclerview.widget.RecyclerView rv =
                        lp.findViewById(R.id.quick_access_grid_recycler);
                if (rv != null && getContext() != null && getActivity() != null) {
                    androidx.recyclerview.widget.GridLayoutManager lm =
                            new androidx.recyclerview.widget.GridLayoutManager(getContext(), 2);
                    rv.setLayoutManager(lm);

                    java.util.List<DisplayGridAdapter.CardItem> items = new java.util.ArrayList<>();
                    // Reuse circular button card + adapter from DisplayGrid for consistency
                    items.add(new DisplayGridAdapter.CardItem(
                            DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                            R.string.display_grid_lock_title,
                            R.string.display_grid_lock_summary,
                            R.drawable.ic_display_grid_lock,
                            "com.android.settings.security.SecuritySettings"));
                    items.add(new DisplayGridAdapter.CardItem(
                            DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                            R.string.display_grid_messages_title,
                            R.string.display_grid_messages_summary,
                            R.drawable.ic_display_grid_messages,
                            "com.android.settings.notification.ConfigureNotificationSettings"));
                    items.add(new DisplayGridAdapter.CardItem(
                            DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                            R.string.display_grid_edit_title,
                            R.string.display_grid_edit_summary,
                            R.drawable.ic_display_grid_edit,
                            "com.android.settings.display.DisplaySettings"));
                    items.add(new DisplayGridAdapter.CardItem(
                            DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                            R.string.display_grid_location_title,
                            R.string.display_grid_location_summary,
                            R.drawable.ic_display_grid_location,
                            "com.android.settings.location.LocationSettings"));

                    rv.setAdapter(new DisplayGridAdapter(getActivity(), items, getMetricsCategory()));
                }
            }
        } catch (Exception ignored) { }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
