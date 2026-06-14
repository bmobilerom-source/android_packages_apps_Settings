/*
 * Copyright (C) 2025 The Android Open Source Project
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

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;

import java.util.List;

/**
 * RecyclerView adapter for Monet color preset grid
 */
public class MonetPresetAdapter extends RecyclerView.Adapter<MonetPresetAdapter.ViewHolder> {

    private final Context mContext;
    private final List<MonetPresetInfo> mPresets;
    private final OnPresetSelectedListener mListener;
    private int mSelectedPosition = -1;

    public interface OnPresetSelectedListener {
        void onPresetSelected(MonetPresetInfo presetInfo, int position);
    }

    public MonetPresetAdapter(Context context, List<MonetPresetInfo> presets,
                             OnPresetSelectedListener listener) {
        mContext = context;
        mPresets = presets;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.monet_preset_gallery_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonetPresetInfo presetInfo = mPresets.get(position);

        // Set the color swatch
        holder.colorSwatch.setBackgroundColor(presetInfo.getColor());

        // Set preset name
        holder.presetName.setText(presetInfo.getName());

        // Handle selection indicator
        boolean isSelected = position == mSelectedPosition;
        holder.itemView.setSelected(isSelected);

        // Add subtle border for selected item
        if (isSelected) {
            // Create a border drawable for selected state
            ColorDrawable border = new ColorDrawable(Color.WHITE);
            ColorDrawable background = new ColorDrawable(presetInfo.getColor());
            LayerDrawable layer = new LayerDrawable(new Drawable[]{background, border});
            layer.setLayerInset(1, 2, 2, 2, 2); // 2dp border
            holder.colorSwatch.setBackground(layer);
        } else {
            holder.colorSwatch.setBackgroundColor(presetInfo.getColor());
        }

        // Set rounded corners
        holder.colorSwatch.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                float radius = mContext.getResources().getDimension(R.dimen.monet_color_swatch_corner_radius);
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        holder.colorSwatch.setClipToOutline(true);

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onPresetSelected(presetInfo, position);
            }
            setSelectedPosition(position);
        });
    }

    @Override
    public int getItemCount() {
        return mPresets.size();
    }

    public void setSelectedPosition(int position) {
        int oldPosition = mSelectedPosition;
        mSelectedPosition = position;
        if (oldPosition >= 0) {
            notifyItemChanged(oldPosition);
        }
        if (position >= 0) {
            notifyItemChanged(position);
        }
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View colorSwatch;
        public final TextView presetName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorSwatch = itemView.findViewById(R.id.color_swatch);
            presetName = itemView.findViewById(R.id.preset_name);
        }
    }
}