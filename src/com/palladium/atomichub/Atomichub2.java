package com.palladium.atomichub;

import android.os.Bundle;

import com.android.internal.logging.nano.MetricsProto;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

/**
 * Atomichub2 fragment - displays the atomichub2.xml layout.
 * Click handling is done by Atomichub2View in onFinishInflate().
 */
public class Atomichub2 extends SettingsPreferenceFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.atomichub2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().hide();
        }
        // Click handling is done by Atomichub2View in onFinishInflate()
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }
}
