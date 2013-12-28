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
import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.NavRingHelpers;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;

public class RibbonSettings extends Fragment {
    private CheckboxSetting mEnable;
    private SingleChoiceSetting mLongSwipe;
    private SingleChoiceSetting mLongPress;
    private Context mContext;
    private String mRibbon;
    private String[] SETTINGS_AOKP;

    private String[] mActions;
    private String[] mActionCodes;

    public RibbonSettings(Context context, String ribbon) {
        mContext = context;
        mRibbon = ribbon;
        SETTINGS_AOKP = mRibbon.equals("left") ? Settings.AOKP.AOKP_LEFT_RIBBON : Settings.AOKP.AOKP_RIGHT_RIBBON;

    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get NavRing Actions
        mActionCodes = NavRingHelpers.getNavRingActions();
        mActions = new String[mActionCodes.length];
        int actionqty = mActions.length;
        for (int i = 0; i < actionqty; i++) {
            mActions[i] = AwesomeConstants.getProperName(mContext,
                    mActionCodes[i]);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstanceState) {
        View main = inflater.inflate(R.layout.fragment_ribbons_settings, container, false);

        mEnable = (CheckboxSetting) main.findViewById(R.id.enable_ribbon);
        mEnable.setKey(SETTINGS_AOKP[AokpRibbonHelper.ENABLE_RIBBON]);
        mEnable.setChecked(Settings.AOKP.getBoolean(mContext.getContentResolver(), SETTINGS_AOKP[AokpRibbonHelper.ENABLE_RIBBON], false));


        mLongSwipe = (SingleChoiceSetting) main.findViewById(R.id.ribbon_long_swipe);
        mLongSwipe.setKey(SETTINGS_AOKP[AokpRibbonHelper.LONG_SWIPE]);
        mLongSwipe.setValues(mActionCodes);
        mLongSwipe.setEntries(mActions);
        mLongSwipe.updateSummary();
        
        mLongPress = (SingleChoiceSetting) main.findViewById(R.id.ribbon_long_press);
        mLongPress.setKey(SETTINGS_AOKP[AokpRibbonHelper.LONG_PRESS]);
        mLongPress.setValues(mActionCodes);
        mLongPress.setEntries(mActions);
        mLongPress.updateSummary();
        
        return main;
    }
}
