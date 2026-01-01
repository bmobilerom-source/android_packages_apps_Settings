/*
 * Copyright (C) 2025 BashaMobile
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

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.applications.defaultapps.DefaultBrowserPicker;
import com.android.settings.applications.defaultapps.DefaultPhonePicker;
import com.android.settings.applications.defaultapps.DefaultSmsPicker;
import com.android.settings.applications.defaultapps.DefaultAutofillPicker;
import com.android.settings.applications.defaultapps.DefaultEmergencyPicker;
import com.android.settings.applications.defaultapps.DefaultHomePicker;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for System Basic Defaults RecyclerView
 * Displays cards for each default app type
 */
public class SystemBasicDefaultsAdapter extends RecyclerView.Adapter<SystemBasicDefaultsAdapter.ViewHolder> {
    
    private final Context mContext;
    private final SystemBasicDefaultsSettings mFragment;
    private final List<DefaultAppItem> mItems;

    public SystemBasicDefaultsAdapter(Context context, SystemBasicDefaultsSettings fragment) {
        mContext = context;
        mFragment = fragment;
        mItems = createDefaultAppItems();
    }

    private List<DefaultAppItem> createDefaultAppItems() {
        List<DefaultAppItem> items = new ArrayList<>();
        
        // Browser
        items.add(new DefaultAppItem(
            R.string.default_browser_title,
            R.string.default_browser_summary,
            android.R.drawable.ic_menu_view, // Fallback icon
            () -> launchDefaultAppPicker(DefaultBrowserPicker.class.getName())
        ));
        
        // Phone
        items.add(new DefaultAppItem(
            R.string.default_phone_title,
            R.string.default_phone_summary,
            android.R.drawable.ic_menu_call, // Fallback icon
            () -> launchDefaultAppPicker(DefaultPhonePicker.class.getName())
        ));
        
        // SMS
        items.add(new DefaultAppItem(
            R.string.sms_application_title,
            R.string.default_sms_summary,
            android.R.drawable.ic_menu_send, // Fallback icon
            () -> launchDefaultAppPicker(DefaultSmsPicker.class.getName())
        ));
        
        // Home
        items.add(new DefaultAppItem(
            R.string.home_app,
            R.string.default_home_summary,
            android.R.drawable.ic_menu_home, // Fallback icon
            () -> launchDefaultAppPicker(DefaultHomePicker.class.getName())
        ));
        
        // Autofill
        items.add(new DefaultAppItem(
            R.string.autofill_service_title,
            R.string.default_autofill_summary,
            android.R.drawable.ic_menu_edit, // Fallback icon
            () -> launchDefaultAppPicker(DefaultAutofillPicker.class.getName())
        ));
        
        // Emergency
        items.add(new DefaultAppItem(
            R.string.default_emergency_app,
            R.string.default_emergency_summary,
            android.R.drawable.ic_menu_help, // Fallback icon
            () -> launchDefaultAppPicker(DefaultEmergencyPicker.class.getName())
        ));
        
        return items;
    }

    private void launchDefaultAppPicker(String fragmentName) {
        new SubSettingLauncher(mContext)
            .setDestination(fragmentName)
            .setSourceMetricsCategory(mFragment.getMetricsCategory())
            .launch();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.system_basic_defaults_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DefaultAppItem item = mItems.get(position);
        holder.title.setText(item.titleRes);
        holder.summary.setText(item.summaryRes);
        holder.icon.setImageResource(item.iconRes);
        holder.itemView.setOnClickListener(v -> item.onClick.run());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView summary;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.default_app_title);
            summary = itemView.findViewById(R.id.default_app_summary);
            icon = itemView.findViewById(R.id.default_app_icon);
        }
    }

    static class DefaultAppItem {
        int titleRes;
        int summaryRes;
        int iconRes;
        Runnable onClick;

        DefaultAppItem(int titleRes, int summaryRes, int iconRes, Runnable onClick) {
            this.titleRes = titleRes;
            this.summaryRes = summaryRes;
            this.iconRes = iconRes;
            this.onClick = onClick;
        }
    }
}

