/*
 * Copyright (C) 2025 LineageOS
 * Licensed under the Apache License, Version 2.0
 */
package com.epic.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settings.R;

public class AmbientCustomizationsAdapter extends RecyclerView.Adapter<AmbientCustomizationsAdapter.ViewHolder> {

    private Context mContext;
    private String[] mAlignmentOptions;
    private String[] mColorTypeOptions;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onAlignmentSelected(int position);
        void onColorTypeSelected(int position);
    }

    public AmbientCustomizationsAdapter(Context context, OnItemClickListener listener) {
        mContext = context;
        mListener = listener;
        mAlignmentOptions = context.getResources().getStringArray(R.array.ambient_text_alignment_entries);
        mColorTypeOptions = context.getResources().getStringArray(R.array.ambient_text_type_color_entries);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Use a simple layout that exists
        View view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < mAlignmentOptions.length) {
            // Alignment item
            holder.title.setText(mAlignmentOptions[position]);
            holder.summary.setText(getAlignmentDescription(position));
            holder.itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onAlignmentSelected(position);
                }
            });
        } else {
            // Color type item
            int colorTypeIndex = position - mAlignmentOptions.length;
            if (colorTypeIndex < mColorTypeOptions.length) {
                holder.title.setText(mColorTypeOptions[colorTypeIndex]);
                holder.summary.setText(getColorTypeDescription(colorTypeIndex));
                holder.itemView.setOnClickListener(v -> {
                    if (mListener != null) {
                        mListener.onColorTypeSelected(colorTypeIndex);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mAlignmentOptions.length + mColorTypeOptions.length;
    }

    private String getAlignmentDescription(int position) {
        switch (position) {
            case 0: return "Top left alignment";
            case 1: return "Top center alignment";
            case 2: return "Center alignment";
            case 3: return "Top right alignment";
            case 4: return "Bottom right alignment";
            default: return "";
        }
    }

    private String getColorTypeDescription(int position) {
        switch (position) {
            case 0: return "Use accent color";
            case 1: return "Use wallpaper color";
            case 2: return "Use custom color";
            default: return "";
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView summary;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.text1);
            summary = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }
}

