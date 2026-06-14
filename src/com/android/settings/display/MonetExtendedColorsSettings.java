/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.android.settings.display;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.Utils;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.LayoutPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class MonetExtendedColorsSettings extends DashboardFragment {
    private static final String TAG = "MonetExtendedColors";
    private static final int GRID_COLUMNS = 4;

    private ColorPresetAdapter mExtendedAdapter;
    private ColorPresetAdapter mNightAdapter;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.monet_extended_colors;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LayoutPreference layoutPreference = findPreference("monet_extended_colors_grid_pref");
        if (layoutPreference == null) {
            return;
        }

        TextView emptyView = layoutPreference.findViewById(R.id.monet_extended_empty);
        TextView extendedTitle = layoutPreference.findViewById(R.id.monet_extended_section_title);
        TextView nightTitle = layoutPreference.findViewById(R.id.monet_night_section_title);
        RecyclerView extendedGrid = layoutPreference.findViewById(R.id.monet_extended_colors_grid);
        RecyclerView nightGrid = layoutPreference.findViewById(R.id.monet_night_colors_grid);

        Context context = requireContext();
        if (!ThemesStubResourceLoader.isStubAvailable(context)) {
            if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
            }
            if (extendedTitle != null) {
                extendedTitle.setVisibility(View.GONE);
            }
            if (nightTitle != null) {
                nightTitle.setVisibility(View.GONE);
            }
            if (extendedGrid != null) {
                extendedGrid.setVisibility(View.GONE);
            }
            if (nightGrid != null) {
                nightGrid.setVisibility(View.GONE);
            }
            return;
        }

        int selectedSeed = MonetThemeApplier.getCurrentSeedColor(context);
        List<StubColorPreset> extendedPresets = ThemesStubResourceLoader.loadExtendedPresets(context);
        List<StubColorPreset> nightPresets = ThemesStubResourceLoader.loadNightPresets(context);

        if (extendedGrid != null) {
            extendedGrid.setLayoutManager(new GridLayoutManager(context, GRID_COLUMNS));
            mExtendedAdapter = new ColorPresetAdapter(context, extendedPresets, selectedSeed,
                    preset -> onPresetSelected(preset));
            extendedGrid.setAdapter(mExtendedAdapter);
        }

        if (nightGrid != null) {
            nightGrid.setLayoutManager(new GridLayoutManager(context, GRID_COLUMNS));
            mNightAdapter = new ColorPresetAdapter(context, nightPresets, selectedSeed,
                    preset -> onPresetSelected(preset));
            nightGrid.setAdapter(mNightAdapter);
        }
    }

    private void onPresetSelected(StubColorPreset preset) {
        boolean applied = MonetThemeApplier.applyPreset(requireContext(), preset.seedColor,
                preset.style, preset.index);
        if (applied) {
            if (mExtendedAdapter != null) {
                mExtendedAdapter.setSelectedSeedColor(preset.seedColor);
            }
            if (mNightAdapter != null) {
                mNightAdapter.setSelectedSeedColor(preset.seedColor);
            }
        }
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_display;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.monet_extended_colors);

    private static final class ColorPresetAdapter extends RecyclerView.Adapter<ColorPresetAdapter.VH> {
        interface OnPresetSelectedListener {
            void onPresetSelected(StubColorPreset preset);
        }

        private final Context mContext;
        private final List<StubColorPreset> mPresets;
        private final OnPresetSelectedListener mListener;
        private final int mCornerRadius;
        private final int mSelectedStrokeWidth;
        private final int mSelectedStrokeColor;
        private int mSelectedSeedColor;

        ColorPresetAdapter(Context context, List<StubColorPreset> presets, int selectedSeedColor,
                OnPresetSelectedListener listener) {
            mContext = context;
            mPresets = new ArrayList<>(presets);
            mListener = listener;
            mSelectedSeedColor = selectedSeedColor;
            mCornerRadius = context.getResources().getDimensionPixelSize(
                    R.dimen.monet_color_swatch_corner_radius);
            mSelectedStrokeWidth = context.getResources().getDimensionPixelSize(
                    R.dimen.monet_extended_selected_stroke);
            mSelectedStrokeColor = Utils.getColorAccentDefaultColor(context);
        }

        void setSelectedSeedColor(int seedColor) {
            mSelectedSeedColor = seedColor;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.monet_color_preset_item, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            StubColorPreset preset = mPresets.get(position);
            holder.nameView.setText(preset.displayName);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(preset.seedColor);
            boolean selected = (0xFFFFFF & preset.seedColor) == (0xFFFFFF & mSelectedSeedColor);
            if (selected) {
                drawable.setStroke(mSelectedStrokeWidth, mSelectedStrokeColor);
            }
            holder.swatchView.setImageDrawable(drawable);
            holder.swatchView.setContentDescription(preset.displayName);

            holder.itemView.setOnClickListener(v -> mListener.onPresetSelected(preset));
        }

        @Override
        public int getItemCount() {
            return mPresets.size();
        }

        static final class VH extends RecyclerView.ViewHolder {
            final ImageView swatchView;
            final TextView nameView;

            VH(@NonNull View itemView) {
                super(itemView);
                swatchView = itemView.findViewById(R.id.monet_preset_color_swatch);
                nameView = itemView.findViewById(R.id.monet_preset_name);
            }
        }
    }
}
