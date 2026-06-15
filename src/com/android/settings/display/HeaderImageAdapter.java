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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;

import java.util.List;

/**
 * RecyclerView adapter for header image gallery
 */
public class HeaderImageAdapter extends RecyclerView.Adapter<HeaderImageAdapter.ViewHolder> {

    private static final Object PAYLOAD_SELECTION = new Object();

    private final Context mContext;
    private final List<HeaderImageInfo> mImages;
    private final OnImageSelectedListener mListener;
    private int mSelectedPosition = -1;

    public interface OnImageSelectedListener {
        void onImageSelected(HeaderImageInfo imageInfo, int position);
    }

    public HeaderImageAdapter(Context context, List<HeaderImageInfo> images,
            OnImageSelectedListener listener) {
        mContext = context;
        mImages = images;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.header_image_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            final int position = holder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION || mListener == null) {
                return;
            }
            mListener.onImageSelected(mImages.get(position), position);
            setSelectedPosition(position);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HeaderImageInfo imageInfo = mImages.get(position);
        holder.thumbnailView.setImageDrawable(imageInfo.getThumbnail(mContext));
        holder.updateSelection(position == mSelectedPosition);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position,
            @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            holder.updateSelection(position == mSelectedPosition);
            return;
        }
        onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    public void setSelectedPosition(int position) {
        int oldPosition = mSelectedPosition;
        mSelectedPosition = position;
        if (oldPosition >= 0) {
            notifyItemChanged(oldPosition, PAYLOAD_SELECTION);
        }
        if (position >= 0) {
            notifyItemChanged(position, PAYLOAD_SELECTION);
        }
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView thumbnailView;
        private final View mSelectionIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.image_thumbnail);
            mSelectionIndicator = itemView.findViewById(R.id.selection_indicator);
        }

        void updateSelection(boolean selected) {
            if (mSelectionIndicator != null) {
                mSelectionIndicator.setVisibility(selected ? View.VISIBLE : View.GONE);
            }
            itemView.setSelected(selected);
        }
    }
}
