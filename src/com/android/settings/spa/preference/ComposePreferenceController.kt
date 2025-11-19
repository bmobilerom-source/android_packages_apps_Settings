/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.settings.spa.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.preference.PreferenceScreen
import com.android.settings.core.BasePreferenceController

abstract class ComposePreferenceController(context: Context, preferenceKey: String) :
    BasePreferenceController(context, preferenceKey) {

    protected lateinit var preference: ComposePreference

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        val foundPreference = screen.findPreference<androidx.preference.Preference>(preferenceKey)
        // Only proceed if the preference is actually a ComposePreference
        // This prevents ClassCastException when other preference types (e.g., AdaptivePreference)
        // have the same key
        if (foundPreference is ComposePreference) {
            preference = foundPreference
            preference.setContent { Content() }
        }
    }

    @Composable
    abstract fun Content()
}
