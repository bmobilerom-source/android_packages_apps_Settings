/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bmobile.fragments;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.AmbientDisplayConfiguration;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.AmbientDisplayNotificationsPreferenceController;
import com.android.settings.gestures.DoubleTapScreenPreferenceController;
import com.android.settings.gestures.PickupGesturePreferenceController;
import com.android.settings.notification.RedactNotificationPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class DeviceControlsSettingsFragment extends DashboardFragment {

    private static final String TAG = "DeviceControlsSettingsFragment";

    private ContentObserver mControlsContentObserver;
    private ContentObserver mLockScreenSecureContentObserver;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AmbientDisplayConfiguration config = new AmbientDisplayConfiguration(context);
        use(AmbientDisplayNotificationsPreferenceController.class).setConfig(config);
        use(DoubleTapScreenPreferenceController.class).setConfig(config);
        use(PickupGesturePreferenceController.class).setConfig(config);

        final Handler handler = new Handler(Looper.getMainLooper());
        mControlsContentObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                updatePreferenceStates();
            }
        };
        context.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.LOCKSCREEN_SHOW_CONTROLS),
                false /* notifyForDescendants */, mControlsContentObserver);

        mLockScreenSecureContentObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                updatePreferenceStates();
            }
        };
        context.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS),
                false /* notifyForDescendants */, mLockScreenSecureContentObserver);
    }

    @Override
    public void onDetach() {
        if (mControlsContentObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mControlsContentObserver);
            mControlsContentObserver = null;
        }
        if (mLockScreenSecureContentObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(
                    mLockScreenSecureContentObserver);
            mLockScreenSecureContentObserver = null;
        }
        super.onDetach();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.lockscreen_device_controls_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();

        RedactNotificationPreferenceController redactController =
                new RedactNotificationPreferenceController(context, "lock_screen_redact");
        RedactNotificationPreferenceController workRedactController =
                new RedactNotificationPreferenceController(context, "lock_screen_work_redact");
        getSettingsLifecycle().addObserver(redactController);
        getSettingsLifecycle().addObserver(workRedactController);
        controllers.add(redactController);
        controllers.add(workRedactController);
        controllers.add(new DoubleTapScreenPreferenceController(context,
                "ambient_display_double_tap"));
        controllers.add(new PickupGesturePreferenceController(context,
                "ambient_display_pick_up"));

        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.lockscreen_device_controls_settings);
}
