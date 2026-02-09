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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;

import java.util.List;

/**
 * Activity for selecting Monet color presets from a gallery grid
 */
public class MonetPresetGalleryActivity extends Activity {
    private static final String TAG = "MonetPresetGalleryActivity";

    private RecyclerView mRecyclerView;
    private MonetPresetAdapter mAdapter;
    private List<MonetPresetInfo> mPresets;
    private int mSelectedPosition = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monet_preset_gallery);

        mRecyclerView = findViewById(R.id.monet_preset_grid);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4)); // 4 columns

        mPresets = loadMonetPresets();
        mAdapter = new MonetPresetAdapter(this, mPresets, this::onPresetSelected);
        mRecyclerView.setAdapter(mAdapter);

        // Set selection based on current setting
        updatePresetSelection();

        Button applyButton = findViewById(R.id.btn_apply);
        Button cancelButton = findViewById(R.id.btn_cancel);

        applyButton.setOnClickListener(v -> applySelection());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void onPresetSelected(MonetPresetInfo presetInfo, int position) {
        mSelectedPosition = position;
        mAdapter.setSelectedPosition(position);
    }

    private void applySelection() {
        if (mSelectedPosition >= 0 && mSelectedPosition < mPresets.size()) {
            MonetPresetInfo selectedPreset = mPresets.get(mSelectedPosition);

            // Update the preference controller
            MonetColorPresetsController controller = new MonetColorPresetsController(this, "temp");
            controller.onPreferenceChange(null, selectedPreset.getValue());
        }

        Intent result = new Intent();
        setResult(RESULT_OK, result);
        finish();
    }

    private void updatePresetSelection() {
        if (mAdapter == null) return;

        String currentPreset = Settings.System.getString(
                getContentResolver(), "monet_color_preset");

        if (currentPreset == null || currentPreset.isEmpty()) {
            currentPreset = "french_violet"; // Default
        }

        // Find the position of the current preset
        for (int i = 0; i < mPresets.size(); i++) {
            if (mPresets.get(i).getValue().equals(currentPreset)) {
                mSelectedPosition = i;
                mAdapter.setSelectedPosition(i);
                break;
            }
        }
    }

    private List<MonetPresetInfo> loadMonetPresets() {
        return MonetColorSettings.loadMonetPresets(this);
    }
}