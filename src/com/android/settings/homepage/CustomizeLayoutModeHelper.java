/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.homepage;

import android.content.ContentResolver;
import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Replaces Afterlife {@code declanxafterlab_style} (0 = wide grid, 1 = tabbed categories).
 * Stored in {@link Settings.Secure} so it works without a framework patch.
 */
public final class CustomizeLayoutModeHelper {

  /** Same key Afterlife uses in AfterLab; safe for migration from their ROM. */
  public static final String SECURE_KEY = "declanxafterlab_style";

  public static final int MODE_GRID = 0;
  public static final int MODE_TABBED = 1;

  @IntDef({MODE_GRID, MODE_TABBED})
  @Retention(RetentionPolicy.SOURCE)
  public @interface LayoutMode {}

  private CustomizeLayoutModeHelper() {}

  public static int getLayoutMode(Context context) {
    if (context == null) {
      return MODE_GRID;
    }
    try {
      int mode = Settings.Secure.getIntForUser(
          context.getContentResolver(), SECURE_KEY, MODE_GRID, UserHandle.USER_CURRENT);
      return mode == MODE_TABBED ? MODE_TABBED : MODE_GRID;
    } catch (Exception e) {
      return MODE_GRID;
    }
  }

  public static void setLayoutMode(Context context, @LayoutMode int mode) {
    if (context == null) {
      return;
    }
    ContentResolver resolver = context.getContentResolver();
    if (resolver == null) {
      return;
    }
    try {
      Settings.Secure.putIntForUser(resolver, SECURE_KEY,
          mode == MODE_TABBED ? MODE_TABBED : MODE_GRID, UserHandle.USER_CURRENT);
    } catch (Exception ignored) {
    }
  }

  /** Suggested homepage style when user picks a layout mode (optional shortcut). */
  public static int getSuggestedHomepageStyleForMode(@LayoutMode int mode) {
    return mode == MODE_TABBED
        ? 12 /* top_level_settings_afterlabs_tab */
        : 13 /* top_level_settings_afterlabs_grid */;
  }

  public static String getModeLabel(Context context, @LayoutMode int mode) {
    return context.getString(mode == MODE_TABBED
        ? com.android.settings.R.string.customize_layout_mode_tabbed
        : com.android.settings.R.string.customize_layout_mode_grid);
  }
}
