package com.aokp.romcontrol.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.internal.util.aokp.AokpRibbonHelper;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.CheckboxSetting;

public class RibbonSettings extends Fragment {
    private CheckboxSetting mEnable;
    private Context mContext;
    private String mRibbon;
    private String[] SETTINGS_AOKP;

    public RibbonSettings(Context context, String ribbon) {
        mContext = context;
        mRibbon = ribbon;
        SETTINGS_AOKP = mRibbon.equals("left") ? Settings.AOKP.AOKP_LEFT_RIBBON : Settings.AOKP.AOKP_RIGHT_RIBBON;

    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstanceState) {
        View main = inflater.inflate(R.layout.fragment_ribbons_settings, container, false);
        mEnable = (CheckboxSetting) main.findViewById(R.id.enable_ribbon);
        mEnable.putKey(SETTINGS_AOKP[AokpRibbonHelper.ENABLE_RIBBON]);
        mEnable.setChecked(Settings.AOKP.getBoolean(mContext.getContentResolver(), SETTINGS_AOKP[AokpRibbonHelper.ENABLE_RIBBON], false));

        return main;
    }
}
