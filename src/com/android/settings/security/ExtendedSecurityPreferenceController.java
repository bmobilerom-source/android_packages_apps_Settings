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

package com.android.settings.security;

import android.content.Context;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.applications.specialaccess.BlockExtendedSecurityController;

/**
 * Controller for Extended Security preference in Security Settings.
 * Hides the preference if Extended Security access is blocked.
 */
public class ExtendedSecurityPreferenceController extends BasePreferenceController {

    private static final String KEY_EXTENDED_SECURITY = "extended_security_preference";

    public ExtendedSecurityPreferenceController(Context context) {
        super(context, KEY_EXTENDED_SECURITY);
    }

    @Override
    public int getAvailabilityStatus() {
        // Hide preference if Extended Security is blocked
        if (BlockExtendedSecurityController.isBlocked(mContext)) {
            return CONDITIONALLY_UNAVAILABLE;
        }
        return AVAILABLE;
    }
}





