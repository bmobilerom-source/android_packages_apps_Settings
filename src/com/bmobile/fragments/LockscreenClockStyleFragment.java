package com.bmobile.fragments;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.Utils;
import com.android.settingslib.widget.LayoutPreference;
import com.bmobile.customization.LockscreenClockStyleController;
import com.google.android.material.card.MaterialCardView;

public class LockscreenClockStyleFragment extends SettingsPreferenceFragment {

    private ClockStyleAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen_clock_style);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.lockscreen_clock_style_picker_title);
        }
        LayoutPreference layoutPreference = findPreference("lockscreen_clock_grid_pref");
        if (layoutPreference == null) {
            return;
        }
        RecyclerView recyclerView = layoutPreference.findViewById(R.id.lockscreen_clock_grid);
        if (recyclerView == null) {
            return;
        }
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mAdapter = new ClockStyleAdapter(requireContext());
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    private static final class ClockStyleAdapter extends RecyclerView.Adapter<ClockStyleAdapter.VH> {
        private final Context mContext;
        private final String[] mEntries;
        private final String[] mValues;
        private final int[] mIcons;
        private final int mSelectedStrokeWidth;
        private final int mSelectedStrokeColor;
        private int mSelectedStyle;
        private int mSelectedPosition;

        ClockStyleAdapter(Context context) {
            mContext = context;
            mSelectedStrokeWidth = context.getResources().getDimensionPixelSize(
                    R.dimen.lockscreen_clock_style_selected_stroke);
            mSelectedStrokeColor = Utils.getColorAccentDefaultColor(context);
            mEntries = context.getResources().getStringArray(
                    R.array.lockscreen_clock_style_entries);
            mValues = context.getResources().getStringArray(
                    R.array.lockscreen_clock_style_values);
            android.content.res.TypedArray icons = context.getResources().obtainTypedArray(
                    R.array.lockscreen_clock_style_icons);
            mIcons = new int[icons.length()];
            for (int i = 0; i < icons.length(); i++) {
                mIcons[i] = icons.getResourceId(i, 0);
            }
            icons.recycle();
            mSelectedStyle = LockscreenClockStyleController.readClockStyle(context);
            mSelectedPosition = findPositionForStyle(mSelectedStyle);
        }

        private int findPositionForStyle(int style) {
            for (int i = 0; i < mValues.length; i++) {
                if (parseClockStyle(mValues[i]) == style) {
                    return i;
                }
            }
            return -1;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.lockscreen_clock_style_item, parent, false);
            return new VH(item);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            if (position >= mEntries.length || position >= mValues.length) {
                return;
            }
            holder.label.setText(mEntries[position]);
            if (position < mIcons.length && mIcons[position] != 0) {
                holder.icon.setImageResource(mIcons[position]);
                holder.icon.setVisibility(View.VISIBLE);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
            final int clockStyle = parseClockStyle(mValues[position]);
            boolean selected = clockStyle == mSelectedStyle;
            MaterialCardView card = (MaterialCardView) holder.itemView;
            if (selected) {
                card.setStrokeWidth(mSelectedStrokeWidth);
                card.setStrokeColor(mSelectedStrokeColor);
            } else {
                card.setStrokeWidth(0);
            }
            card.setSelected(selected);
            holder.selected.setVisibility(selected ? View.VISIBLE : View.GONE);
            holder.itemView.setContentDescription(selected
                    ? mEntries[position] + ", "
                            + mContext.getString(R.string.lockscreen_clock_style_selected)
                    : mEntries[position]);
            holder.itemView.setOnClickListener(v -> {
                if (clockStyle == mSelectedStyle) {
                    return;
                }
                Settings.Secure.putInt(mContext.getContentResolver(),
                        LockscreenClockStyleController.SETTING_KEY, clockStyle);
                int previousPosition = mSelectedPosition;
                mSelectedStyle = clockStyle;
                mSelectedPosition = holder.getBindingAdapterPosition();
                if (previousPosition >= 0) {
                    notifyItemChanged(previousPosition);
                }
                notifyItemChanged(mSelectedPosition);
            });
        }

        @Override
        public int getItemCount() {
            return Math.min(mEntries.length, Math.min(mValues.length, mIcons.length));
        }

        static final class VH extends RecyclerView.ViewHolder {
            final ImageView icon;
            final ImageView selected;
            final TextView label;

            VH(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.clock_icon);
                selected = itemView.findViewById(R.id.clock_selected);
                label = itemView.findViewById(R.id.clock_label);
            }
        }
    }

    private static int parseClockStyle(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return LockscreenClockStyleController.getDefaultValue();
        }
    }
}
