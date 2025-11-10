/*
 * Copyright (C) 2025 BashaMobile
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This is part of an independent card navigation system that can be
 * transferred to any Android ROM. See CardNavigationHelper.java for
 * transfer instructions.
 */

package com.epic.fragments;

/**
 * Configuration for a single card in the card navigation layout.
 * 
 * This is a simple data class that holds configuration for one card.
 * Create instances of this to configure which fragments each card opens.
 */
public class CardNavigationConfig {
    public final int cardId;           // R.id.card_1, R.id.card_2, etc.
    public final String fragmentClass; // Full class name of fragment to launch
    public final int titleResId;      // String resource ID for title

    public CardNavigationConfig(int cardId, String fragmentClass, int titleResId) {
        this.cardId = cardId;
        this.fragmentClass = fragmentClass;
        this.titleResId = titleResId;
    }
}

