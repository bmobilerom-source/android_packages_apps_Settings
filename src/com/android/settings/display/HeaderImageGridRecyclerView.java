/*
 * Copyright (C) 2026 The LineageOS Project
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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView tuned for header image grids: faster flings and no sticky momentum
 * after a previous scroll gesture ends.
 */
public class HeaderImageGridRecyclerView extends RecyclerView {

    private static final float FLING_VELOCITY_MULTIPLIER = 1.35f;

    public HeaderImageGridRecyclerView(@NonNull Context context) {
        super(context);
    }

    public HeaderImageGridRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderImageGridRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        return super.fling(
                (int) (velocityX * FLING_VELOCITY_MULTIPLIER),
                (int) (velocityY * FLING_VELOCITY_MULTIPLIER));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            stopScroll();
        }
        return super.onInterceptTouchEvent(event);
    }
}
