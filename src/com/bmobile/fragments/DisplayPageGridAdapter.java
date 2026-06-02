package com.bmobile.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

import java.util.List;

class DisplayPageGridAdapter extends RecyclerView.Adapter<DisplayPageGridAdapter.CardVH> {
    static final int CARD_TYPE_STANDARD = 0;
    static final int CARD_TYPE_MONET_COLOR = 1;
    static final int CARD_TYPE_LOCKSCREEN = 2;
    static final int CARD_TYPE_THEME_PACKS = 3;
    static final int CARD_TYPE_WIDE = 4;
    static final int CARD_TYPE_SMALL = 5;

    static class CardItem {
        final int cardType;
        final int titleResId;
        final int summaryResId;
        final String destFragment;
        final Integer iconResId;

        CardItem(int cardType, int titleResId, int summaryResId, String destFragment,
                Integer iconResId) {
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
        final LinearLayout colorSwatchesLayout;
        final android.widget.TextClock lockscreenClock;
        final android.widget.TextClock aodClock;
        final LinearLayout themePacksButtons;
        final View themePackButtonLeft;
        final View themePackButtonRight;

        CardVH(@NonNull View itemView) {
            super(itemView);
            this.iconView = itemView.findViewById(android.R.id.icon);
            this.titleView = itemView.findViewById(android.R.id.title);
            this.summaryView = itemView.findViewById(android.R.id.summary);
            this.colorSwatchesLayout = itemView.findViewById(R.id.color_swatches_layout);
            this.lockscreenClock = itemView.findViewById(R.id.lockscreen_clock_preview);
            this.aodClock = itemView.findViewById(R.id.aod_clock);
            this.themePacksButtons = itemView.findViewById(R.id.theme_pack_buttons_layout);
            this.themePackButtonLeft = itemView.findViewById(R.id.theme_pack_button_left);
            this.themePackButtonRight = itemView.findViewById(R.id.theme_pack_button_right);
        }
    }

    private final android.app.Activity activity;
    private final List<CardItem> items;
    private final int sourceMetrics;

    DisplayPageGridAdapter(android.app.Activity activity, List<CardItem> items, int sourceMetrics) {
        this.activity = activity;
        this.items = items;
        this.sourceMetrics = sourceMetrics;
    }

    @NonNull
    @Override
    public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
            case CARD_TYPE_MONET_COLOR:
                layoutRes = R.layout.display_page_grid_card_monet_color;
                break;
            case CARD_TYPE_LOCKSCREEN:
                layoutRes = R.layout.display_page_grid_card_lockscreen;
                break;
            case CARD_TYPE_THEME_PACKS:
                layoutRes = R.layout.display_page_grid_card_theme_packs;
                break;
            case CARD_TYPE_WIDE:
                layoutRes = R.layout.display_page_grid_card_wide;
                break;
            case CARD_TYPE_SMALL:
                layoutRes = R.layout.display_page_grid_card_small;
                break;
            default:
                layoutRes = R.layout.display_page_grid_card_standard;
                break;
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new CardVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardVH holder, int position) {
        try {
            if (position < 0 || position >= items.size()) {
                return;
            }

            CardItem item = items.get(position);
            if (item == null) {
                return;
            }

            if (holder.titleView != null) {
                holder.titleView.setText(item.titleResId);
            }
            if (holder.summaryView != null) {
                holder.summaryView.setText(item.summaryResId);
            }

            if (holder.iconView != null) {
                if (item.cardType == CARD_TYPE_SMALL && item.iconResId != null) {
                    try {
                        holder.iconView.setImageResource(item.iconResId);
                        holder.iconView.setVisibility(View.VISIBLE);
                    } catch (android.content.res.Resources.NotFoundException e) {
                        Log.w("DisplayPageGridAdapter", "Icon not found: " + item.iconResId, e);
                        holder.iconView.setVisibility(View.GONE);
                    }
                } else {
                    holder.iconView.setVisibility(View.GONE);
                }
            }

            if (item.cardType == CARD_TYPE_MONET_COLOR && holder.colorSwatchesLayout != null) {
                holder.colorSwatchesLayout.setVisibility(View.GONE);
            }

            if (item.cardType == CARD_TYPE_LOCKSCREEN && holder.lockscreenClock != null) {
                holder.lockscreenClock.setVisibility(View.VISIBLE);
            }

            if (item.cardType == CARD_TYPE_THEME_PACKS) {
                if (holder.themePacksButtons != null) {
                    holder.themePacksButtons.setVisibility(View.GONE);
                }
                holder.itemView.setClickable(true);
                holder.itemView.setFocusable(true);
            }

            if (item.cardType == CARD_TYPE_WIDE && holder.aodClock != null) {
                holder.aodClock.setVisibility(View.VISIBLE);
            }

            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(v -> launchDestination(item.destFragment, item.titleResId));
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Error in onBindViewHolder at position " + position, e);
        }
    }

    @Override
    public int getItemViewType(int position) {
        try {
            if (position >= 0 && position < items.size()) {
                CardItem item = items.get(position);
                if (item != null) {
                    return item.cardType;
                }
            }
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Error in getItemViewType", e);
        }
        return CARD_TYPE_STANDARD;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        try {
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        try {
                            if (position >= 0 && position < items.size()) {
                                CardItem item = items.get(position);
                                if (item != null && item.cardType == CARD_TYPE_WIDE) {
                                    return 2;
                                }
                            }
                        } catch (Exception e) {
                            Log.e("DisplayPageGridAdapter", "Error in getSpanSize", e);
                        }
                        return 1;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Error in onAttachedToRecyclerView", e);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void launchDestination(String destFragment, int titleResId) {
        if (destFragment == null || destFragment.isEmpty()) {
            Log.w("DisplayPageGridAdapter", "Empty destination fragment");
            showErrorToast();
            return;
        }

        try {
            if ("auto_rotate".equals(destFragment)) {
                launchAutoRotate();
                return;
            }

            if ("ambient_display".equals(destFragment)) {
                launchAmbientDisplay();
                return;
            }

            if ("livedisplay".equals(destFragment)) {
                launchLiveDisplay();
                return;
            }

            if ("image_toolbox".equals(destFragment)) {
                launchImageToolbox();
                return;
            }

            if (destFragment.contains("WallpaperSettings") || destFragment.contains("wallpaper")) {
                launchWallpaperPickerActivity();
                return;
            }

            if (destFragment.contains("DashboardStyleSettings")) {
                launchSettingsFragment(destFragment, R.string.dashboard_style_title);
                return;
            }

            if (destFragment.startsWith("org.lineageos.lineageparts.")) {
                launchLineagePartsActivity(destFragment);
            } else if (destFragment.contains("UserBackupSettingsActivity")) {
                launchUserBackupActivity();
            } else {
                launchSettingsFragment(destFragment, titleResId);
            }
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch: " + destFragment, e);
            showErrorToast();
        }
    }

    private void launchAutoRotate() {
        try {
            new SubSettingLauncher(activity)
                .setDestination("com.android.settings.display.SmartAutoRotatePreferenceFragment")
                .setTitleRes(R.string.accelerometer_title)
                .setSourceMetricsCategory(sourceMetrics)
                .launch();
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch SmartAutoRotatePreferenceFragment", e);
            try {
                Bundle args = new Bundle();
                args.putString(":settings:fragment_args_key", "auto_rotate");
                new SubSettingLauncher(activity)
                    .setDestination("com.android.settings.DisplaySettings")
                    .setTitleRes(R.string.accelerometer_title)
                    .setArguments(args)
                    .setSourceMetricsCategory(sourceMetrics)
                    .launch();
            } catch (Exception fallbackException) {
                Log.e("DisplayPageGridAdapter", "All auto rotate launch methods failed",
                        fallbackException);
                showErrorToast();
            }
        }
    }

    private void launchAmbientDisplay() {
        try {
            Intent intent = new Intent("org.lineageos.settings.device.DOZE_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PackageManager pm = activity.getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                activity.startActivity(intent);
                return;
            }

            launchSettingsFragment("com.android.settings.security.LockscreenDashboardFragment",
                R.string.ambient_display_screen_title);
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch ambient display", e);
            showErrorToast();
        }
    }

    private void launchLiveDisplay() {
        try {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("org.lineageos.lineageparts",
                    "org.lineageos.lineageparts.PartsActivity"));
                intent.putExtra(":settings:show_fragment",
                    "org.lineageos.lineageparts.livedisplay.LiveDisplaySettings");
                intent.putExtra(":settings:show_fragment_title",
                    activity.getString(R.string.color_mode_title));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                PackageManager pm = activity.getPackageManager();
                if (pm.resolveActivity(intent, 0) != null) {
                    activity.startActivity(intent);
                    return;
                }
            } catch (Exception e) {
                Log.d("DisplayPageGridAdapter", "PartsActivity LiveDisplay launch failed", e);
            }

            try {
                Intent intent = new Intent("org.lineageos.lineageparts.PART.livedisplay");
                intent.setComponent(new ComponentName("org.lineageos.lineageparts",
                    "org.lineageos.lineageparts.PartsActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                PackageManager pm = activity.getPackageManager();
                if (pm.resolveActivity(intent, 0) != null) {
                    activity.startActivity(intent);
                    return;
                }
            } catch (Exception e) {
                Log.d("DisplayPageGridAdapter", "PartsList action launch failed", e);
            }

            Bundle args = new Bundle();
            args.putString(":settings:fragment_args_key", "color_mode");
            new SubSettingLauncher(activity)
                .setDestination("com.android.settings.DisplaySettings")
                .setTitleRes(R.string.color_mode_title)
                .setArguments(args)
                .setSourceMetricsCategory(sourceMetrics)
                .launch();
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch live display", e);
            showErrorToast();
        }
    }

    private void launchWallpaperPickerActivity() {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.android.wallpaper",
                "com.android.customization.picker.CustomizationPickerActivity");
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch wallpaper picker", e);
            showErrorToast();
        }
    }

    private void launchLineagePartsActivity(String className) {
        try {
            if (className.contains("DisplayRotation")
                    || className.equals("org.lineageos.lineageparts.hardware.DisplayRotation")) {
                try {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("org.lineageos.lineageparts",
                        "org.lineageos.lineageparts.PartsActivity"));
                    intent.putExtra(":settings:show_fragment",
                        "org.lineageos.lineageparts.hardware.DisplayRotation");
                    intent.putExtra(":settings:show_fragment_title_resid",
                        R.string.display_rotation_title);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    PackageManager pm = activity.getPackageManager();
                    if (pm.resolveActivity(intent, 0) != null) {
                        activity.startActivity(intent);
                        return;
                    }
                } catch (Exception e) {
                    Log.w("DisplayPageGridAdapter", "PartsActivity launch failed", e);
                }

                new SubSettingLauncher(activity)
                    .setDestination("com.android.settings.DisplaySettings")
                    .setTitleRes(R.string.display_rotation_title)
                    .setSourceMetricsCategory(sourceMetrics)
                    .launch();
                return;
            }

            ComponentName component = new ComponentName("org.lineageos.lineageparts", className);
            PackageManager pm = activity.getPackageManager();
            try {
                pm.getActivityInfo(component, 0);
                Intent intent = new Intent();
                intent.setComponent(component);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                return;
            } catch (PackageManager.NameNotFoundException e) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("org.lineageos.lineageparts",
                    "org.lineageos.lineageparts.PartsActivity"));
                intent.putExtra(":settings:show_fragment", className);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (pm.resolveActivity(intent, 0) != null) {
                    activity.startActivity(intent);
                    return;
                }
            }

            if (className.contains("Display") || className.contains("display")) {
                new SubSettingLauncher(activity)
                    .setDestination("com.android.settings.DisplaySettings")
                    .setSourceMetricsCategory(sourceMetrics)
                    .launch();
            } else {
                showErrorToast();
            }
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch LineageParts: " + className, e);
            showErrorToast();
        }
    }

    private void launchUserBackupActivity() throws Exception {
        ComponentName component = new ComponentName("com.android.settings",
            "com.android.settings.backup.UserBackupSettingsActivity");
        PackageManager pm = activity.getPackageManager();
        pm.getActivityInfo(component, 0);
        Intent intent = new Intent();
        intent.setComponent(component);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    private void launchImageToolbox() {
        try {
            String packageName = "ru.tech.imageresizershrinker";
            String mainActivity = "com.t8rin.imagetoolbox.app.presentation.AppActivity";

            PackageManager pm = activity.getPackageManager();

            try {
                ComponentName component = new ComponentName(packageName, mainActivity);
                pm.getActivityInfo(component, 0);
                Intent intent = new Intent();
                intent.setComponent(component);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                return;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w("DisplayPageGridAdapter", "Image Toolbox activity not found", e);
            }

            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setPackage(packageName);

            ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
            if (resolveInfo != null && resolveInfo.activityInfo != null) {
                ComponentName component = new ComponentName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name);
                intent = new Intent();
                intent.setComponent(component);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                return;
            }

            showErrorToast();
        } catch (Exception e) {
            Log.e("DisplayPageGridAdapter", "Failed to launch Image Toolbox", e);
            showErrorToast();
        }
    }

    private void launchSettingsFragment(String destFragment, int titleResId) {
        new SubSettingLauncher(activity)
            .setDestination(destFragment)
            .setTitleRes(titleResId)
            .setArguments(new Bundle())
            .setSourceMetricsCategory(sourceMetrics)
            .launch();
    }

    private void showErrorToast() {
        Toast.makeText(activity, R.string.system_tuner_not_available,
            Toast.LENGTH_SHORT).show();
    }
}
