package com.palladium.atomichub;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.android.internal.logging.nano.MetricsProto;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

public class Atomichub extends SettingsPreferenceFragment implements  View.OnClickListener{

    final String[] target = new String[1];
    FrameLayout c1,c2,c3,c4,c5,c6;
    LinearLayout c0;
    ImageView btnicon;
    TextView title,summary;
    HorizontalScrollView horizontalScrollView;
    ImageButton btntransistion;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.atomichub, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        
        super.onViewCreated(view, savedInstanceState);
		getActivity().getActionBar().hide();        
        horizontalScrollView = view.findViewById(R.id.hsv_card);
        btntransistion = view.findViewById(R.id.btn_trans);
        btntransistion.setOnClickListener(this);
        btnicon = view.findViewById(R.id.img1_icon);
        title = view.findViewById(R.id.tv_title_big);
        summary = view.findViewById(R.id.tv_summary_big);
        c0 = view.findViewById(R.id.card);
        c0.setClickable(true);
        c0.setOnClickListener(this);
        c1 = view.findViewById(R.id.card1);
        c1.setOnClickListener(this);
        c2 = view.findViewById(R.id.card2);
        c2.setOnClickListener(this);
        c3 = view.findViewById(R.id.card3);
        c3.setOnClickListener(this);
        c4 = view.findViewById(R.id.card4);
        c4.setOnClickListener(this);
        c5 = view.findViewById(R.id.card5);
        c5.setOnClickListener(this);
        c6 = view.findViewById(R.id.card6);
        c6.setOnClickListener(this);
        target[0] = "PocketMode";


    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        // Card 1: Pocket Mode
        if(id == R.id.card1){
            launchFragment("com.epic.fragments.PocketModeSettings", R.string.pocket_mode_title);
            return;
        }

        // Card 2: BMobile Fingerprint
        if(id == R.id.card2){
            launchFragment("com.epic.fragments.BMobileFingerprintSettings", R.string.bmobile_fingerprint_title);
            return;
        }

        // Card 3: Privacy & Security
        if(id == R.id.card3){
            launchFragment("com.epic.fragments.PrivacySecuritySettings", R.string.privacy_security_title);
            return;
        }

        // Card 4: Advanced Security Settings
        if(id == R.id.card4){
            launchFragment("com.epic.fragments.AdvancedSecuritySettings", R.string.advanced_security_settings_title);
            return;
        }

        // Card 5: System Optimization
        if(id == R.id.card5){
            launchFragment("com.epic.fragments.SystemOptimizationSettings", R.string.system_optimization_title);
            return;
        }

        // Card 6: Settings Backup & Restore
        if(id == R.id.card6){
            launchFragment("com.epic.fragments.SettingsBackupRestoreImproved", R.string.settings_backup_restore_title);
            return;
        }

        // Big Card: Mock Locations
        if(id == R.id.card){
            launchFragment("com.android.settings.location.MockLocationsSettings", R.string.mock_locations_title);
            return;
        }

        // Transition button (back button) - navigate back
        if(id == R.id.btn_trans){
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }

    /**
     * Launch a fragment using SubSettingLauncher.
     */
    private void launchFragment(String fragmentClass, int titleResId) {
        try {
            new SubSettingLauncher(getActivity())
                .setDestination(fragmentClass)
                .setTitleRes(titleResId)
                .setSourceMetricsCategory(getMetricsCategory())
                .launch();
        } catch (Exception e) {
            android.util.Log.e("Atomichub", "Failed to launch fragment: " + fragmentClass, e);
        }
    }



    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }



}