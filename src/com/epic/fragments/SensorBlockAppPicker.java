/*
 * Copyright (C) 2025 BashaMobile
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
 * TRANSFER TO OTHER ROMS:
 * =======================
 * This fragment allows selecting apps for sensor block per-package feature.
 * Based on: https://github.com/BootleggersROM/packages_apps_BootlegDumpster/commit/431482bace0109c0c34914e1988d6fb6c2dde434
 * 
 * To transfer to other ROMs:
 * 1. Copy this file and all related layout/menu files
 * 2. Update package name if needed
 * 3. Ensure Settings.Secure is available
 * 4. Framework-side implementation may be needed for actual sensor blocking
 */

package com.epic.fragments;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sensor Block App Picker
 * 
 * Allows selecting apps to block sensors for.
 * Displays a list of installed apps with toggle switches.
 */
public class SensorBlockAppPicker extends SettingsPreferenceFragment {

    private static final String TAG = "SensorBlockAppPicker";
    private static final String SETTING_KEY = "sensor_block_packages";

    private RecyclerView mRecyclerView;
    private AppAdapter mAdapter;
    private List<AppInfo> mAppList = new ArrayList<>();
    private List<AppInfo> mFilteredList = new ArrayList<>();
    private Set<String> mSelectedPackages = new HashSet<>();
    private boolean mShowSystemApps = false;
    private String mSearchQuery = "";
    private Menu mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.hide_developer_status_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() == null) {
            Log.e(TAG, "Activity is null in onViewCreated");
            return;
        }

        mRecyclerView = view.findViewById(R.id.apps_list);
        if (mRecyclerView == null) {
            Log.e(TAG, "RecyclerView not found in layout");
            return;
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load selected packages first (before loading apps)
        loadSelectedPackages();
        Log.d(TAG, "Selected packages loaded: " + mSelectedPackages.size());
        
        // Load apps after view is ready
        loadApps();
        
        // Setup adapter after apps are loaded
        mAdapter = new AppAdapter();
        mRecyclerView.setAdapter(mAdapter);
        
        // Filter and display apps
        filterApps();
        
        Log.d(TAG, "App list initialized. Total apps: " + mAppList.size() + 
              ", Filtered: " + mFilteredList.size() + 
              ", Selected: " + mSelectedPackages.size());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload selected packages in case they were changed elsewhere
        loadSelectedPackages();
        // Refresh the adapter to show updated switch states
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        Log.d(TAG, "onResume: Reloaded " + mSelectedPackages.size() + " selected packages");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.hide_developer_status_menu, menu);
        mMenu = menu;
        
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.search_apps));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    mSearchQuery = newText != null ? newText.toLowerCase() : "";
                    filterApps();
                    return true;
                }
            });
        }

        updateMenuItems();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.show_system) {
            mShowSystemApps = true;
            filterApps();
            updateMenuItems();
            return true;
        } else if (id == R.id.hide_system) {
            mShowSystemApps = false;
            filterApps();
            updateMenuItems();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMenuItems() {
        if (mMenu == null) return;
        MenuItem showSystem = mMenu.findItem(R.id.show_system);
        MenuItem hideSystem = mMenu.findItem(R.id.hide_system);
        if (showSystem != null) showSystem.setVisible(!mShowSystemApps);
        if (hideSystem != null) hideSystem.setVisible(mShowSystemApps);
    }

    private void loadApps() {
        if (getActivity() == null) {
            Log.e(TAG, "Activity is null, cannot load apps");
            return;
        }

        try {
            PackageManager pm = getActivity().getPackageManager();
            if (pm == null) {
                Log.e(TAG, "PackageManager is null");
                return;
            }

            List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            if (installedApps == null || installedApps.isEmpty()) {
                Log.w(TAG, "No installed apps found");
                return;
            }
            
            mAppList.clear();
            for (ApplicationInfo info : installedApps) {
                try {
                    if (info != null) {
                        mAppList.add(new AppInfo(info, pm));
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error loading app: " + (info != null ? info.packageName : "null"), e);
                }
            }
            
            // Sort alphabetically
            Collections.sort(mAppList, Comparator.comparing(app -> app.label.toString()));
            
            Log.d(TAG, "Loaded " + mAppList.size() + " apps");
        } catch (Exception e) {
            Log.e(TAG, "Error loading apps", e);
        }
    }

    private void loadSelectedPackages() {
        if (getActivity() == null) {
            Log.e(TAG, "Activity is null, cannot load selected packages");
            return;
        }

        mSelectedPackages.clear();
        try {
            String packages = Settings.Secure.getString(getActivity().getContentResolver(), SETTING_KEY);
            Log.d(TAG, "Raw packages string from Settings: " + (packages != null ? packages : "null"));
            
            if (packages != null && !packages.isEmpty()) {
                String[] packageArray = packages.split(",");
                for (String pkg : packageArray) {
                    String trimmed = pkg.trim();
                    if (!trimmed.isEmpty()) {
                        mSelectedPackages.add(trimmed);
                        Log.v(TAG, "Added package to selection: " + trimmed);
                    }
                }
                Log.d(TAG, "Loaded " + mSelectedPackages.size() + " selected packages: " + mSelectedPackages);
            } else {
                Log.d(TAG, "No packages found in Settings.Secure for key: " + SETTING_KEY);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading selected packages", e);
        }
    }

    private void saveSelectedPackages() {
        String packages = TextUtils.join(",", mSelectedPackages);
        Settings.Secure.putString(getActivity().getContentResolver(), SETTING_KEY, packages);
        Log.d(TAG, "Saved selected packages: " + packages);
    }

    private void filterApps() {
        mFilteredList.clear();
        for (AppInfo app : mAppList) {
            try {
                boolean matchesSearch = TextUtils.isEmpty(mSearchQuery) ||
                                        app.label.toString().toLowerCase().contains(mSearchQuery) ||
                                        app.packageName.toLowerCase().contains(mSearchQuery);
                
                boolean isSystemApp = (app.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                boolean matchesSystemFilter = mShowSystemApps || !isSystemApp;

                if (matchesSearch && matchesSystemFilter) {
                    mFilteredList.add(app);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error filtering app: " + app.packageName, e);
            }
        }
        
        Log.d(TAG, "Filtered apps: " + mFilteredList.size() + " out of " + mAppList.size() + " total");
        
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        } else {
            Log.w(TAG, "Adapter is null, cannot notify data set changed");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveSelectedPackages();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.hide_developer_status_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position < 0 || position >= mFilteredList.size()) {
                Log.w(TAG, "Invalid position in onBindViewHolder: " + position);
                return;
            }
            
            AppInfo app = mFilteredList.get(position);
            if (app == null) {
                Log.w(TAG, "AppInfo is null at position: " + position);
                return;
            }
            
            holder.icon.setImageDrawable(app.icon);
            holder.title.setText(app.label);
            holder.summary.setText(app.packageName);
            
            // Check if this app is in the selected packages set
            boolean isSelected = mSelectedPackages.contains(app.packageName);
            holder.switchWidget.setChecked(isSelected);
            
            Log.v(TAG, "Binding app: " + app.packageName + ", Selected: " + isSelected);

            holder.itemView.setOnClickListener(v -> {
                boolean currentState = holder.switchWidget.isChecked();
                boolean newState = !currentState;
                holder.switchWidget.setChecked(newState);
                
                if (newState) {
                    mSelectedPackages.add(app.packageName);
                    Log.d(TAG, "Added to selection: " + app.packageName);
                } else {
                    mSelectedPackages.remove(app.packageName);
                    Log.d(TAG, "Removed from selection: " + app.packageName);
                }
                // No need to save immediately, onPause will handle it
            });
        }

        @Override
        public int getItemCount() {
            return mFilteredList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
            Switch switchWidget;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                title = itemView.findViewById(R.id.title);
                summary = itemView.findViewById(R.id.summary);
                switchWidget = itemView.findViewById(R.id.switch_widget);
            }
        }
    }

    private static class AppInfo {
        ApplicationInfo info;
        CharSequence label;
        String packageName;
        Drawable icon;

        AppInfo(ApplicationInfo info, PackageManager pm) {
            this.info = info;
            this.label = info.loadLabel(pm);
            this.packageName = info.packageName;
            this.icon = info.loadIcon(pm);
        }
    }
}

