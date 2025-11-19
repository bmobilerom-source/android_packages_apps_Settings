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
 * This fragment allows hiding ADB and developer setting enable status from selected apps.
 * Based on: https://github.com/Phoenix-rom/android_packages_apps_PhoenixSettings/commit/bef7e1382531c3de680aee9a31b9c516b77914b8
 * 
 * To transfer to other ROMs:
 * 1. Copy this file and all related layout/menu/array files
 * 2. Update package name if needed
 * 3. Ensure Settings.Secure is available
 * 4. Framework-side implementation may be needed for actual hiding logic
 */

package com.epic.fragments;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
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
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
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
 * Hide Developer Status Settings
 * 
 * Allows selecting apps to hide ADB and developer setting enable status from.
 * Displays a list of installed apps with toggle switches.
 */
public class HideDeveloperStatusSettings extends SettingsPreferenceFragment {

    private static final String TAG = "HideDeveloperStatus";
    private static final String SETTING_KEY = "hide_developer_status_apps";

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
        loadSelectedPackages();
        loadApps();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hide_developer_status_layout, container, false);
        // Ensure wallpaper background is added
        ensureWallpaperBackground(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mRecyclerView = view.findViewById(R.id.apps_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new AppAdapter();
        mRecyclerView.setAdapter(mAdapter);
        
        filterApps();
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

    private void loadSelectedPackages() {
        String packages = Settings.Secure.getString(getActivity().getContentResolver(), SETTING_KEY);
        if (packages != null && !packages.isEmpty()) {
            String[] packageArray = packages.split(",");
            for (String pkg : packageArray) {
                if (!pkg.isEmpty()) {
                    mSelectedPackages.add(pkg.trim());
                }
            }
        }
    }

    private void saveSelectedPackages() {
        StringBuilder sb = new StringBuilder();
        for (String pkg : mSelectedPackages) {
            if (sb.length() > 0) sb.append(",");
            sb.append(pkg);
        }
        Settings.Secure.putString(getActivity().getContentResolver(), SETTING_KEY, sb.toString());
    }

    private void loadApps() {
        PackageManager pm = getActivity().getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        
        // Get default hidden apps from arrays
        String[] defaultHidden = getResources().getStringArray(R.array.hide_developer_status_hidden_apps);
        Set<String> defaultHiddenSet = new HashSet<>();
        for (String pkg : defaultHidden) {
            defaultHiddenSet.add(pkg);
        }

        mAppList.clear();
        for (PackageInfo pkgInfo : packages) {
            try {
                ApplicationInfo appInfo = pkgInfo.applicationInfo;
                if (appInfo == null) continue;

                String packageName = pkgInfo.packageName;
                String label = appInfo.loadLabel(pm).toString();
                Drawable icon = appInfo.loadIcon(pm);
                boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                boolean isDefaultHidden = defaultHiddenSet.contains(packageName);

                AppInfo app = new AppInfo(packageName, label, icon, isSystemApp);
                mAppList.add(app);
                
                // Add default hidden apps to selection
                if (isDefaultHidden && !mSelectedPackages.contains(packageName)) {
                    mSelectedPackages.add(packageName);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading app: " + pkgInfo.packageName, e);
            }
        }

        Collections.sort(mAppList, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo a, AppInfo b) {
                return a.label.compareToIgnoreCase(b.label);
            }
        });
    }

    private void filterApps() {
        mFilteredList.clear();
        for (AppInfo app : mAppList) {
            // Filter by system apps
            if (!mShowSystemApps && app.isSystemApp) {
                continue;
            }
            
            // Filter by search query
            if (!mSearchQuery.isEmpty()) {
                String searchLower = mSearchQuery.toLowerCase();
                if (!app.label.toLowerCase().contains(searchLower) &&
                    !app.packageName.toLowerCase().contains(searchLower)) {
                    continue;
                }
            }
            
            mFilteredList.add(app);
        }
        
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void toggleApp(String packageName) {
        if (mSelectedPackages.contains(packageName)) {
            mSelectedPackages.remove(packageName);
        } else {
            mSelectedPackages.add(packageName);
        }
        saveSelectedPackages();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
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
            AppInfo app = mFilteredList.get(position);
            holder.icon.setImageDrawable(app.icon);
            holder.title.setText(app.label);
            holder.summary.setText(app.packageName);
            holder.switchWidget.setChecked(mSelectedPackages.contains(app.packageName));
            
            holder.itemView.setOnClickListener(v -> {
                toggleApp(app.packageName);
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

            ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                title = itemView.findViewById(R.id.title);
                summary = itemView.findViewById(R.id.summary);
                switchWidget = itemView.findViewById(R.id.switch_widget);
            }
        }
    }

    private static class AppInfo {
        String packageName;
        String label;
        Drawable icon;
        boolean isSystemApp;

        AppInfo(String packageName, String label, Drawable icon, boolean isSystemApp) {
            this.packageName = packageName;
            this.label = label;
            this.icon = icon;
            this.isSystemApp = isSystemApp;
        }
    }
}
