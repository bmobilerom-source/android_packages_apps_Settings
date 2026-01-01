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
 *
 * TRANSFER TO OTHER ROMS:
 * =======================
 * This helper makes the Material Dashboard Grid feature independent and transferable.
 * To transfer to other ROMs:
 * 1. Copy this file
 * 2. Copy MaterialDashboardGridAdapter.java
 * 3. Copy display_page_grid_card_*.xml layouts
 * 4. Update fragment class names if needed
 * 5. Update drawable references if needed
 */

package com.bmobile.fragments;

import android.content.Context;
import android.util.Log;
import android.view.View;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settings.R;
import com.android.settingslib.widget.LayoutPreference;
import java.util.List;

/**
 * Helper class for Material Dashboard Grid setup
 * Provides utility methods for safe initialization and error handling
 * Makes the feature independent and transferable
 */
public class MaterialDashboardGridHelper {
    private static final String TAG = "MaterialDashboardGridHelper";
    
    /**
     * Safely finds the RecyclerView in the material dashboard grid layout
     * @param screen The preference screen containing the grid
     * @return The RecyclerView if found, null otherwise
     */
    public static RecyclerView findRecyclerView(PreferenceScreen screen) {
        if (screen == null) {
            Log.w(TAG, "PreferenceScreen is null");
            return null;
        }
        
        try {
            LayoutPreference layoutPref = 
                    (LayoutPreference) screen.findPreference("material_dashboard_grid");
            if (layoutPref == null) {
                Log.w(TAG, "material_dashboard_grid LayoutPreference not found");
                return null;
            }
            
            // Find RecyclerView directly from LayoutPreference (like compact dashboard)
            RecyclerView rv = layoutPref.findViewById(R.id.material_dashboard_grid_recycler);
            if (rv == null) {
                Log.w(TAG, "material_dashboard_grid_recycler RecyclerView not found");
            }
            return rv;
        } catch (Exception e) {
            Log.e(TAG, "Error finding RecyclerView", e);
            return null;
        }
    }
    
    /**
     * Checks if all required resources are available
     * @param context The context to check resources
     * @return true if all resources are available, false otherwise
     */
    public static boolean areResourcesAvailable(Context context) {
        if (context == null) {
            return false;
        }
        
        try {
            // Check if required layouts exist
            int[] requiredLayouts = {
                R.layout.display_page_grid_card_standard,
                R.layout.display_page_grid_card_small,
                R.layout.material_dashboard_two_column_grid
            };
            
            for (int layoutRes : requiredLayouts) {
                try {
                    context.getResources().getLayout(layoutRes);
                } catch (android.content.res.Resources.NotFoundException e) {
                    Log.e(TAG, "Required layout not found: " + layoutRes, e);
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error checking resources", e);
            return false;
        }
    }
    
    /**
     * Validates that the adapter items list is valid
     * @param items The items list to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateItems(List<MaterialDashboardGridAdapter.CardItem> items) {
        if (items == null) {
            Log.w(TAG, "Items list is null");
            return false;
        }
        
        if (items.isEmpty()) {
            Log.w(TAG, "Items list is empty");
            return false;
        }
        
        // Check for null items
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == null) {
                Log.w(TAG, "Item at position " + i + " is null");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Safely sets up the RecyclerView with proper error handling
     * @param recyclerView The RecyclerView to set up
     * @param context The context
     * @param adapter The adapter to use
     * @return true if setup was successful, false otherwise
     */
    public static boolean setupRecyclerView(RecyclerView recyclerView, Context context, 
            RecyclerView.Adapter<?> adapter) {
        if (recyclerView == null) {
            Log.w(TAG, "RecyclerView is null");
            return false;
        }
        
        if (context == null) {
            Log.w(TAG, "Context is null");
            return false;
        }
        
        if (adapter == null) {
            Log.w(TAG, "Adapter is null");
            return false;
        }
        
        try {
            androidx.recyclerview.widget.GridLayoutManager layoutManager = 
                    new androidx.recyclerview.widget.GridLayoutManager(context, 2);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setAdapter(adapter);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
            return false;
        }
    }
}

