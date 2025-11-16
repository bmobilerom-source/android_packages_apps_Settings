/*
 * Copyright (C) 2025 LineageOS
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

package com.android.settings.kidssafe;

import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

import java.util.List;

/** Pastel gradient grid cards for KidsSafe / KS Fun dashboards. */
public class KidsSafeGridAdapter extends RecyclerView.Adapter<KidsSafeGridAdapter.CardVH> {

    public static final int CARD_TYPE_STANDARD = 0;

    private static final int[] LIGHT_GRADIENTS = {
            R.drawable.pastel_gradient_amber_light,
            R.drawable.pastel_gradient_blue_light,
            R.drawable.pastel_gradient_green_light,
            R.drawable.pastel_gradient_purple_light,
            R.drawable.pastel_gradient_pink_light,
            R.drawable.pastel_gradient_teal_light,
            R.drawable.pastel_gradient_orange_light,
            R.drawable.pastel_gradient_indigo_light,
            R.drawable.pastel_gradient_cyan_light,
            R.drawable.pastel_gradient_lime_light,
    };

    private static final int[] DARK_GRADIENTS = {
            R.drawable.pastel_gradient_amber_dark,
            R.drawable.pastel_gradient_blue_dark,
            R.drawable.pastel_gradient_green_dark,
            R.drawable.pastel_gradient_purple_dark,
            R.drawable.pastel_gradient_pink_dark,
            R.drawable.pastel_gradient_teal_dark,
            R.drawable.pastel_gradient_orange_dark,
            R.drawable.pastel_gradient_indigo_dark,
            R.drawable.pastel_gradient_cyan_dark,
            R.drawable.pastel_gradient_lime_dark,
    };

    public static class CardItem {
        public final int cardType;
        public final int titleResId;
        public final int summaryResId;
        public final String destFragment;
        public final Integer iconResId;

        public CardItem(int cardType, int titleResId, int summaryResId,
                String destFragment, Integer iconResId) {
            this.cardType = cardType;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.destFragment = destFragment;
            this.iconResId = iconResId;
        }
    }

    static class CardVH extends RecyclerView.ViewHolder {
        final ImageView iconView;
        final TextView titleView;
        final TextView summaryView;
        final View cardView;

        CardVH(@NonNull View itemView) {
            super(itemView);
            cardView = itemView;
            iconView = itemView.findViewById(android.R.id.icon);
            titleView = itemView.findViewById(android.R.id.title);
            summaryView = itemView.findViewById(android.R.id.summary);
        }
    }

    private final android.app.Activity mActivity;
    private final List<CardItem> mItems;
    private final int mSourceMetrics;

    public KidsSafeGridAdapter(android.app.Activity activity, List<CardItem> items,
            int sourceMetrics) {
        mActivity = activity;
        mItems = items;
        mSourceMetrics = sourceMetrics;
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fun_display_grid_card_standard, parent, false);
        return new CardVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardVH holder, int position) {
        if (position < 0 || position >= mItems.size()) {
            return;
        }

        CardItem item = mItems.get(position);
        if (item == null) {
            return;
        }

        int nightModeFlags = mActivity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        if (holder.cardView != null) {
            int gradientIndex = position % LIGHT_GRADIENTS.length;
            int gradientRes = isDarkMode ? DARK_GRADIENTS[gradientIndex]
                    : LIGHT_GRADIENTS[gradientIndex];
            try {
                holder.cardView.setBackgroundResource(gradientRes);
            } catch (Exception e) {
                Log.w("KidsSafeGridAdapter", "Failed to set gradient background", e);
            }
        }

        if (holder.titleView != null) {
            holder.titleView.setText(item.titleResId);
            holder.titleView.setTextColor(mActivity.getResources().getColor(
                    isDarkMode ? android.R.color.white : android.R.color.black, null));
        }
        if (holder.summaryView != null) {
            holder.summaryView.setText(item.summaryResId);
            holder.summaryView.setTextColor(mActivity.getResources().getColor(
                    isDarkMode ? android.R.color.white : android.R.color.black, null));
            holder.summaryView.setAlpha(0.8f);
        }

        if (holder.iconView != null) {
            if (item.iconResId != null) {
                try {
                    holder.iconView.setImageResource(item.iconResId);
                    holder.iconView.setVisibility(View.VISIBLE);
                    holder.iconView.setColorFilter(
                            mActivity.getResources().getColor(
                                    isDarkMode ? android.R.color.white : android.R.color.black,
                                    null),
                            android.graphics.PorterDuff.Mode.SRC_IN);
                } catch (android.content.res.Resources.NotFoundException e) {
                    holder.iconView.setVisibility(View.GONE);
                }
            } else {
                holder.iconView.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(
                v -> launchDestination(item.destFragment, item.titleResId));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private void launchDestination(String destFragment, int titleResId) {
        if (destFragment == null || destFragment.isEmpty()) {
            return;
        }
        try {
            new SubSettingLauncher(mActivity)
                    .setDestination(destFragment)
                    .setTitleRes(titleResId)
                    .setArguments(new android.os.Bundle())
                    .setSourceMetricsCategory(mSourceMetrics)
                    .launch();
        } catch (Exception e) {
            Log.e("KidsSafeGridAdapter", "Failed to launch: " + destFragment, e);
            Toast.makeText(mActivity, R.string.system_tuner_not_available, Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
