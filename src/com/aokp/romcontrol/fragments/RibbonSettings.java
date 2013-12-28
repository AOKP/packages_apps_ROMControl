package com.aokp.romcontrol.fragments;

import net.margaritov.preference.colorpicker.ColorPickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.internal.util.aokp.AokpRibbonHelper;
import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.NavRingHelpers;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.ColorPickerSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;
import net.margaritov.preference.colorpicker.ColorPickerDialog.OnColorChangedListener;

public class RibbonSettings extends Fragment implements OnSeekBarChangeListener, OnColorChangedListener {
    private CheckboxSetting mEnable;
    private SingleChoiceSetting mLongSwipe;
    private SingleChoiceSetting mLongPress;
    private CheckboxSetting mSwipeVibrate;
    private SingleChoiceSetting mHandleLocation;
    private SingleChoiceSetting mAutoTimeout;
    private Button mHandleColor;
    private SeekBar mHandleHeight;
    private SeekBar mHandleWidth;
    private Context mContext;
    private String mRibbon;
    private String[] SETTINGS_AOKP;
    private int mSeekBarProgress;

    private String[] mActions;
    private String[] mActionCodes;
    
    public RibbonSettings() {
    }
    
    public RibbonSettings(Context context, String ribbon) {
       // mContext = context;
        mRibbon = ribbon;
        SETTINGS_AOKP = mRibbon.equals("left") ? Settings.AOKP.AOKP_LEFT_RIBBON : Settings.AOKP.AOKP_RIGHT_RIBBON;

    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get NavRing Actions
        mContext = getActivity();
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
        
        mSwipeVibrate = (CheckboxSetting) main.findViewById(R.id.enable_gesture_vibrate);
        mSwipeVibrate.setKey(SETTINGS_AOKP[AokpRibbonHelper.HANDLE_VIBRATE]);
        mSwipeVibrate.setChecked(Settings.AOKP.getBoolean(mContext.getContentResolver(), SETTINGS_AOKP[AokpRibbonHelper.HANDLE_VIBRATE], false));

        mHandleLocation = (SingleChoiceSetting) main.findViewById(R.id.ribbon_swipe_location);
        mHandleLocation.setKey(SETTINGS_AOKP[AokpRibbonHelper.HANDLE_LOCATION]);
        mHandleLocation.setValues(mContext.getResources().getStringArray(R.array.ribbon_handle_location_values));
        mHandleLocation.setEntries(mContext.getResources().getStringArray(R.array.ribbon_handle_location_entries));
        mHandleLocation.updateSummary();

        mAutoTimeout = (SingleChoiceSetting) main.findViewById(R.id.auto_hide_duration);
        mAutoTimeout.setKey(SETTINGS_AOKP[AokpRibbonHelper.AUTO_HIDE_DURATION]);
        mAutoTimeout.setValues(mContext.getResources().getStringArray(R.array.hide_ribbon_timeout_values));
        mAutoTimeout.setEntries(mContext.getResources().getStringArray(R.array.hide_ribbon_timeout_entries));
        mAutoTimeout.updateSummary();
        
        mHandleColor = (Button) main.findViewById(R.id.handle_color);
        final int handleColor = Settings.AOKP.getInt(mContext.getContentResolver(), SETTINGS_AOKP[AokpRibbonHelper.HANDLE_COLOR], Color.BLACK);
        mHandleColor.setBackgroundColor(handleColor);
        mHandleColor.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog picker = new ColorPickerDialog(mContext, handleColor);
                picker.setAlphaSliderVisible(true);
                picker.setOnColorChangedListener(RibbonSettings.this);
                picker.show();
            }
        });
      
        mHandleWidth = (SeekBar) main.findViewById(R.id.drag_handle_width);
        mHandleWidth.setProgress(Settings.AOKP.getInt(mContext.getContentResolver(), SETTINGS_AOKP[AokpRibbonHelper.HANDLE_WEIGHT], 30));
        mHandleWidth.setOnSeekBarChangeListener(this);

        mHandleHeight = (SeekBar) main.findViewById(R.id.drag_handle_height);
        mHandleHeight.setProgress(Settings.AOKP.getInt(mContext.getContentResolver(), SETTINGS_AOKP[AokpRibbonHelper.HANDLE_HEIGHT], 50));
        mHandleHeight.setOnSeekBarChangeListener(this);
        
        return main;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mSeekBarProgress = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == mHandleWidth) {
            Settings.AOKP.putInt(mContext.getContentResolver(), SETTINGS_AOKP[AokpRibbonHelper.HANDLE_WEIGHT], mSeekBarProgress);
        } else if (seekBar == mHandleHeight) {
            Settings.AOKP.putInt(mContext.getContentResolver(), SETTINGS_AOKP[AokpRibbonHelper.HANDLE_HEIGHT], mSeekBarProgress);          
        }
        
    }


    @Override
    public void onColorChanged(int color) {
        mHandleColor.setBackgroundColor(color);
        Settings.AOKP.putInt(mContext.getContentResolver(), SETTINGS_AOKP[AokpRibbonHelper.HANDLE_COLOR], color);
    }
}
