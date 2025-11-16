/*
 * Copyright (C) 2025 BashaMobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.android.settings.homepage;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;

/**
 * Hides/shows the OOS11 floating bottom bar while the homepage {@link NestedScrollView} scrolls.
 *
 * <p>The bar is attached to {@code settings_homepage_container} (outside the scroll view). Scroll
 * events come from {@code main_content_scrollable_container}.
 */
public final class Oos11FloatingBottomBarScrollHelper {

    private static final String TAG = "Oos11FloatingBottomBar";

    private Oos11FloatingBottomBarScrollHelper() {}

    /**
     * Attaches scroll-driven hide/show to {@code bottomBar}.
     *
     * @return the scroll container, for cleanup in {@link #detach(NestedScrollView)}
     */
    @Nullable
    public static NestedScrollView attach(@NonNull Activity activity,
            @Nullable View bottomBar) {
        final NestedScrollView scrollContainer =
                activity.findViewById(com.android.settings.R.id.main_content_scrollable_container);
        if (scrollContainer == null || bottomBar == null) {
            Log.w(TAG, "Cannot attach floating bar scroll (scroll="
                    + (scrollContainer != null) + ", bar=" + (bottomBar != null) + ")");
            return null;
        }

        ViewCompat.setElevation(bottomBar, 12f * bottomBar.getResources().getDisplayMetrics().density);
        bottomBar.setTranslationZ(bottomBar.getElevation());
        if (bottomBar.getParent() instanceof ViewGroup) {
            bottomBar.bringToFront();
        }

        scrollContainer.setOnScrollChangeListener(
                new NestedScrollView.OnScrollChangeListener() {
                    private static final int SCROLL_THRESHOLD_PX = 12;
                    private boolean mHidden;

                    @Override
                    public void onScrollChange(@NonNull NestedScrollView v, int scrollX,
                            int scrollY, int oldScrollX, int oldScrollY) {
                        final int delta = scrollY - oldScrollY;
                        if (scrollY <= 0) {
                            show(bottomBar);
                            return;
                        }
                        if (delta > SCROLL_THRESHOLD_PX) {
                            hide(bottomBar);
                        } else if (delta < -SCROLL_THRESHOLD_PX) {
                            show(bottomBar);
                        }
                    }

                    private void hide(@NonNull View bar) {
                        if (mHidden) {
                            return;
                        }
                        mHidden = true;
                        bar.animate().translationY(hideDistancePx(bar)).setDuration(200L).start();
                    }

                    private void show(@NonNull View bar) {
                        if (!mHidden && bar.getTranslationY() == 0f) {
                            return;
                        }
                        mHidden = false;
                        bar.animate().translationY(0f).setDuration(200L).start();
                    }

                    private float hideDistancePx(@NonNull View bar) {
                        int height = bar.getHeight();
                        if (height == 0) {
                            height = (int) (72f * bar.getResources().getDisplayMetrics().density);
                        }
                        int bottomMargin = 0;
                        if (bar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                            bottomMargin = ((ViewGroup.MarginLayoutParams) bar.getLayoutParams())
                                    .bottomMargin;
                        }
                        return height + bottomMargin
                                + (16f * bar.getResources().getDisplayMetrics().density);
                    }
                });
        return scrollContainer;
    }

    public static void detach(@Nullable NestedScrollView scrollContainer) {
        if (scrollContainer != null) {
            scrollContainer.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) null);
        }
    }
}
