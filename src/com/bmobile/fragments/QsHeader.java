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
package com.bmobile.fragments;

import com.android.settingslib.search.SearchIndexable;

/**
 * QS Header settings entry point for display grid navigation.
 * <p>
 * Delegates to {@link com.android.settings.awaken.fragments.QsHeader}, which loads
 * {@link com.android.settings.R.xml#qs_header} and handles provider / gallery / pack prefs.
 */
@SearchIndexable
public class QsHeader extends com.android.settings.awaken.fragments.QsHeader {
}
