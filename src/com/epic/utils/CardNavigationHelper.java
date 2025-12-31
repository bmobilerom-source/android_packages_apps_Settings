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

package com.epic.utils;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.fragment.app.Fragment;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

/**
 * Utility class to handle navigation card clicks in Settings fragments.
 * Works with toplevel_card_navigation layout to make cards clickable.
 */
public class CardNavigationHelper {

    private Fragment mFragment;

    public CardNavigationHelper(Fragment fragment) {
        this.mFragment = fragment;
    }

    /**
     * Setup click handling for navigation cards.
     * Call this in your fragment's onCreateView or onViewCreated method.
     * For LayoutPreference, this should be called after the view is fully laid out.
     */
    public void setupCardClickHandling(View rootView) {
        if (rootView == null) return;

        android.util.Log.d("CardNavigationHelper", "Setting up card click handling");

        // For LayoutPreference, we need to find the actual layout preference view
        View layoutPrefView = findLayoutPreferenceView(rootView);
        if (layoutPrefView != null) {
            android.util.Log.d("CardNavigationHelper", "Found LayoutPreference view, setting up clicks");
            // For LayoutPreference, we need to wait for it to be fully inflated
            layoutPrefView.post(new Runnable() {
                @Override
                public void run() {
                    setupCardClicksInternal(layoutPrefView);
                }
            });
        } else {
            // Fallback: try with root view
            android.util.Log.d("CardNavigationHelper", "LayoutPreference view not found, using root view");
            rootView.post(new Runnable() {
                @Override
                public void run() {
                    setupCardClicksInternal(rootView);
                }
            });
        }
    }
    
    /**
     * Find the LayoutPreference view that contains toplevel_card_navigation
     */
    private View findLayoutPreferenceView(View root) {
        // Look for the LinearLayout that is the root of toplevel_card_navigation
        if (root instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                // Check if this is the LinearLayout from toplevel_card_navigation
                // It should have card_1 as a child
                View card1 = findViewByIdRecursive(child, R.id.card_1);
                if (card1 != null) {
                    android.util.Log.d("CardNavigationHelper", "Found toplevel_card_navigation layout");
                    return child;
                }
                // Recursively search
                View found = findLayoutPreferenceView(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void setupCardClicksInternal(View rootView) {
        android.util.Log.d("CardNavigationHelper", "Starting internal card setup");

        // Find card views by searching the entire view hierarchy
        LinearLayout card1 = findViewByIdRecursive(rootView, R.id.card_1);
        RelativeLayout card2 = findViewByIdRecursive(rootView, R.id.card_2);
        LinearLayout card3 = findViewByIdRecursive(rootView, R.id.card_3);
        RelativeLayout card4 = findViewByIdRecursive(rootView, R.id.card_4);

        android.util.Log.d("CardNavigationHelper", "Setting up card clicks - card1: " + (card1 != null) +
            ", card2: " + (card2 != null) + ", card3: " + (card3 != null) + ", card4: " + (card4 != null));

        View.OnClickListener cardClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = (String) v.getTag();
                android.util.Log.d("CardNavigationHelper", "🎯 Card clicked! Tag: " + tag + ", View: " + v.getId());
                if (tag != null) {
                    handleCardClick(tag);
                } else {
                    android.util.Log.w("CardNavigationHelper", "⚠️ Card clicked but no tag found");
                }
            }
        };

        // Set click listeners if views exist
        if (card1 != null) {
            card1.setOnClickListener(cardClickListener);
            android.util.Log.d("CardNavigationHelper", "✅ Set click listener for card1 (Fingerprint)");
        } else {
            android.util.Log.w("CardNavigationHelper", "❌ Card1 (Fingerprint) not found");
        }
        if (card2 != null) {
            card2.setOnClickListener(cardClickListener);
            android.util.Log.d("CardNavigationHelper", "✅ Set click listener for card2 (Privacy Dashboard)");
        } else {
            android.util.Log.w("CardNavigationHelper", "❌ Card2 (Privacy Dashboard) not found");
        }
        if (card3 != null) {
            card3.setOnClickListener(cardClickListener);
            android.util.Log.d("CardNavigationHelper", "✅ Set click listener for card3 (Special Access)");
        } else {
            android.util.Log.w("CardNavigationHelper", "❌ Card3 (Special Access) not found");
        }
        if (card4 != null) {
            card4.setOnClickListener(cardClickListener);
            android.util.Log.d("CardNavigationHelper", "✅ Set click listener for card4 (Sensor Block)");
        } else {
            android.util.Log.w("CardNavigationHelper", "❌ Card4 (Sensor Block) not found");
        }
    }

    /**
     * Recursively find a view by ID in the view hierarchy.
     */
    @SuppressWarnings("unchecked")
    private <T extends View> T findViewByIdRecursive(View root, int id) {
        if (root.getId() == id) {
            return (T) root;
        }
        if (root instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                T found = findViewByIdRecursive(group.getChildAt(i), id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Handle card clicks based on tag.
     * Uses SubSettingLauncher for proper Settings fragment navigation.
     */
    protected void handleCardClick(String tag) {
        android.util.Log.d("CardNavigationHelper", "🚀 Handling card click for tag: " + tag);
        
        if (mFragment == null || mFragment.getContext() == null) {
            android.util.Log.e("CardNavigationHelper", "❌ Fragment or context is null");
            return;
        }

        Context context = mFragment.getContext();
        SubSettingLauncher launcher = new SubSettingLauncher(context);

        try {
            switch (tag) {
                case "fingerprint_settings":
                    // Open Fingerprint Settings Fragment
                    android.util.Log.d("CardNavigationHelper", "👆 Launching Fingerprint Settings Fragment");
                    launcher.setDestination("com.android.settings.biometrics.fingerprint.FingerprintSettings$FingerprintSettingsFragment")
                            .setSourceMetricsCategory(mFragment instanceof com.android.settingslib.core.instrumentation.Instrumentable 
                                    ? ((com.android.settingslib.core.instrumentation.Instrumentable) mFragment).getMetricsCategory()
                                    : 0)
                            .launch();
                    break;

                case "privacy_controls":
                    // Open Privacy Controls Fragment
                    android.util.Log.d("CardNavigationHelper", "📱 Launching Privacy Controls Fragment");
                    launcher.setDestination(com.android.settings.privacy.PrivacyControlsFragment.class.getName())
                            .setSourceMetricsCategory(mFragment instanceof com.android.settingslib.core.instrumentation.Instrumentable 
                                    ? ((com.android.settingslib.core.instrumentation.Instrumentable) mFragment).getMetricsCategory()
                                    : 0)
                            .launch();
                    break;

                case "privacy_dashboard":
                    // Open Privacy Dashboard Fragment
                    android.util.Log.d("CardNavigationHelper", "📊 Launching Privacy Dashboard Fragment");
                    launcher.setDestination(com.android.settings.privacy.PrivacyDashboardFragment.class.getName())
                            .setSourceMetricsCategory(mFragment instanceof com.android.settingslib.core.instrumentation.Instrumentable 
                                    ? ((com.android.settingslib.core.instrumentation.Instrumentable) mFragment).getMetricsCategory()
                                    : 0)
                            .launch();
                    break;

                case "special_access":
                    // Open Special Access Fragment
                    android.util.Log.d("CardNavigationHelper", "🔑 Launching Special Access Fragment");
                    launcher.setDestination(com.android.settings.applications.specialaccess.SpecialAccessSettings.class.getName())
                            .setSourceMetricsCategory(mFragment instanceof com.android.settingslib.core.instrumentation.Instrumentable 
                                    ? ((com.android.settingslib.core.instrumentation.Instrumentable) mFragment).getMetricsCategory()
                                    : 0)
                            .launch();
                    break;

                case "sensor_block":
                    // Open Sensor Block Settings Fragment
                    android.util.Log.d("CardNavigationHelper", "🔒 Launching Sensor Block Settings Fragment");
                    launcher.setDestination(com.epic.fragments.SensorBlockSettings.class.getName())
                            .setSourceMetricsCategory(mFragment instanceof com.android.settingslib.core.instrumentation.Instrumentable 
                                    ? ((com.android.settingslib.core.instrumentation.Instrumentable) mFragment).getMetricsCategory()
                                    : 0)
                            .launch();
                    break;

                case "security_features":
                    // Open Security Features Settings Fragment
                    android.util.Log.d("CardNavigationHelper", "🛡️ Launching Security Features Fragment");
                    launcher.setDestination(com.epic.fragments.SecurityFeaturesSettings.class.getName())
                            .setSourceMetricsCategory(mFragment instanceof com.android.settingslib.core.instrumentation.Instrumentable 
                                    ? ((com.android.settingslib.core.instrumentation.Instrumentable) mFragment).getMetricsCategory()
                                    : 0)
                            .launch();
                    break;

                default:
                    // Unknown tag, do nothing
                    android.util.Log.w("CardNavigationHelper",
                        "Unknown card tag: " + tag);
                    return;
            }
        } catch (Exception e) {
            android.util.Log.e("CardNavigationHelper",
                "❌ Failed to launch fragment for tag: " + tag, e);
        }
    }

    /**
     * Allow fragments to customize card navigation by overriding handleCardClick.
     * This is a hook for subclasses.
     */
    public void setCustomCardHandler(CardClickHandler handler) {
        // Implementation for custom handlers if needed
    }

    /**
     * Interface for custom card click handling
     */
    public interface CardClickHandler {
        void onCardClicked(String tag);
    }
}
