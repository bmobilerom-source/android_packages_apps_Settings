package com.epic.fragments;

import android.content.Context;
import android.app.Activity;
import android.view.View;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import com.android.settings.R;
import com.android.settingslib.widget.LayoutPreference;
import java.util.List;
import java.util.ArrayList;

/**
 * Helper class to easily add DisplayGrid to any SettingsPreferenceFragment
 */
public class DisplayGridHelper {
    
    /**
     * Initialize and setup DisplayGrid in a preference screen
     * 
     * @param context The context (should be Activity context for SubSettingLauncher)
     * @param screen The preference screen
     * @param preferenceKey The key of the LayoutPreference (default: "display_grid")
     * @param sourceMetrics The metrics category for tracking
     */
    public static void setupDisplayGrid(Context context, PreferenceScreen screen, 
            String preferenceKey, int sourceMetrics) {
        if (preferenceKey == null || preferenceKey.isEmpty()) {
            preferenceKey = "display_grid";
        }
        
        // Ensure we have an Activity context for SubSettingLauncher
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
        
        if (activity == null) {
            android.util.Log.e("DisplayGridHelper", "Context must be an Activity for SubSettingLauncher");
            return;
        }
        
        Preference layoutPref = screen.findPreference(preferenceKey);
        if (layoutPref instanceof LayoutPreference) {
            LayoutPreference lp = (LayoutPreference) layoutPref;
            RecyclerView rv = lp.findViewById(R.id.display_grid_recycler);
            if (rv != null) {
                rv.setLayoutManager(new GridLayoutManager(context, 2));
                
                List<DisplayGridAdapter.CardItem> items = createDefaultItems();
                rv.setAdapter(new DisplayGridAdapter(activity, items, sourceMetrics));
            }
        }
    }
    
    /**
     * Create default items for the display grid
     * You can customize this method or create items manually
     */
    private static List<DisplayGridAdapter.CardItem> createDefaultItems() {
        List<DisplayGridAdapter.CardItem> items = new ArrayList<>();
        
        // Large left card with Quick Settings button
        items.add(new DisplayGridAdapter.CardItem(
                DisplayGridAdapter.CARD_TYPE_LARGE_LEFT,
                R.string.display_grid_quick_settings_title,
                R.string.display_grid_quick_settings_summary,
                null,
                "com.epic.fragments.QuickSettings",
                "Quick settings"));
        
        // About Us card
        items.add(new DisplayGridAdapter.CardItem(
                DisplayGridAdapter.CARD_TYPE_ABOUT_US,
                R.string.display_grid_about_us_title,
                R.string.display_grid_about_us_summary,
                R.drawable.ic_display_grid_about_us,
                "com.epic.fragments.AboutUsSettings"));
        
        // Status bar card
        items.add(new DisplayGridAdapter.CardItem(
                DisplayGridAdapter.CARD_TYPE_STATUS_BAR,
                R.string.display_grid_status_bar_title,
                R.string.display_grid_status_bar_summary,
                R.drawable.ic_display_grid_status_bar,
                "com.epic.fragments.StatusBarSettings"));
        
        // Circular buttons (6 buttons)
        items.add(new DisplayGridAdapter.CardItem(
                DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                R.string.display_grid_lock_title,
                R.string.display_grid_lock_summary,
                R.drawable.ic_display_grid_lock,
                "com.android.settings.security.SecuritySettings"));
        
        items.add(new DisplayGridAdapter.CardItem(
                DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                R.string.display_grid_messages_title,
                R.string.display_grid_messages_summary,
                R.drawable.ic_display_grid_messages,
                "com.android.settings.notification.ConfigureNotificationSettings"));
        
        items.add(new DisplayGridAdapter.CardItem(
                DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                R.string.display_grid_connection_title,
                R.string.display_grid_connection_summary,
                R.drawable.ic_display_grid_connection,
                "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment"));
        
        items.add(new DisplayGridAdapter.CardItem(
                DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                R.string.display_grid_edit_title,
                R.string.display_grid_edit_summary,
                R.drawable.ic_display_grid_edit,
                "com.android.settings.display.DisplaySettings"));
        
        items.add(new DisplayGridAdapter.CardItem(
                DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                R.string.display_grid_location_title,
                R.string.display_grid_location_summary,
                R.drawable.ic_display_grid_location,
                "com.android.settings.location.LocationSettings"));
        
        items.add(new DisplayGridAdapter.CardItem(
                DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
                R.string.display_grid_team_title,
                R.string.display_grid_team_summary,
                R.drawable.ic_display_grid_team,
                "com.epic.fragments.TeamSettings"));
        
        return items;
    }
}

