/*
 * Copyright (C) 2011 The CyanogenMod Project
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

package com.aokp.romcontrol.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aokp.romcontrol.util.KernelUtils;
import com.aokp.romcontrol.R;

/**
 * Special preference type that allows configuration of both the ring volume and
 * notification volume.
 */
public class GammaTuningPreference extends DialogPreference {

    private static final String TAG = "GAMMA...";

    enum Colors {
        RED, GREEN, BLUE
    };

    private static final int[] SEEKBAR_ID = new int[] {
            R.id.gamma_red_seekbar, R.id.gamma_green_seekbar, R.id.gamma_blue_seekbar, R.id.gamma_dss_seekbar
    };

    private static final int[] VALUE_DISPLAY_ID = new int[] {
            R.id.gamma_red_value, R.id.gamma_green_value, R.id.gamma_blue_value, R.id.gamma_dss_value
    };

    private static final String[] FILE_PATH = new String[] {
            "/sys/class/misc/samoled_color/red_v1_offset",
            "/sys/class/misc/samoled_color/green_v1_offset",
            "/sys/class/misc/samoled_color/blue_v1_offset",
            "/sys/devices/platform/omapdss/manager0/gamma"
    };

    private GammaSeekBar mSeekBars[] = new GammaSeekBar[4];
    
    private static final int DEFAULT_GAMMA = 100;

    private static final int MAX_VALUE = 200;

    private static final int OFFSET_VALUE = 100;
    
    private Button mReset_button;

    // Track instances to know when to restore original color
    // (when the orientation changes, a new dialog is created before the old one
    // is destroyed)
    private static int sInstances = 0;

    public GammaTuningPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.preference_dialog_gamma_tuning);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        sInstances++;

        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            TextView valueDisplay = (TextView) view.findViewById(VALUE_DISPLAY_ID[i]);
            if (i < 3)
                mSeekBars[i] = new GammaSeekBar(seekBar, valueDisplay, FILE_PATH[i], OFFSET_VALUE, MAX_VALUE);
            else
                mSeekBars[i] = new GammaSeekBar(seekBar, valueDisplay, FILE_PATH[i], 0, 10);
        }
        mReset_button = (Button) view.findViewById(R.id.reset_button);
        mReset_button.setOnClickListener(new Button.OnClickListener() {  
            public void onClick(View v) {
            for (GammaSeekBar seekbar : mSeekBars){
            seekbar.mSeekBar.setProgress(DEFAULT_GAMMA);
                }
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        sInstances--;

        if (positiveResult) {
            for (GammaSeekBar csb : mSeekBars) {
                csb.save();
            }
        } else if (sInstances == 0) {
            for (GammaSeekBar csb : mSeekBars) {
                csb.reset();
            }
        }
    }

    /**
     * Restore screen color tuning from SharedPreferences. (Write to kernel.)
     * 
     * @param context The context to read the SharedPreferences from
     */
    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean bFirstTime = sharedPrefs.getBoolean("FirstTimeGamma", true);
        for (String filePath : FILE_PATH) {
            String sDefaultValue = KernelUtils.readOneLine(filePath);
            int iValue = sharedPrefs.getInt(filePath, Integer.valueOf(sDefaultValue));
            if (bFirstTime)
                KernelUtils.writeValue(filePath, "0");
            else
                KernelUtils.writeValue(filePath, String.valueOf((long) iValue));
        }
        if (bFirstTime)
        {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("FirstTimeGamma", false);
            editor.commit();
        }
    }

    /**
     * Check whether the running kernel supports color tuning or not.
     * 
     * @return Whether color tuning is supported or not
     */
    public static boolean isSupported() {
        boolean supported = true;
        for (String filePath : FILE_PATH) {
            if (!KernelUtils.fileExists(filePath)) {
                supported = false;
            }
        }

        return supported;
    }

    class GammaSeekBar implements SeekBar.OnSeekBarChangeListener {

        private String mFilePath;

        private int mOriginal;

        public SeekBar mSeekBar;

        private TextView mValueDisplay;

        private int OFFSET_VALUE;

        private int MAX_VALUE;

        public GammaSeekBar(SeekBar seekBar, TextView valueDisplay, String filePath, Integer offsetValue, Integer maxValue) {
            int iValue;

            mSeekBar = seekBar;
            mValueDisplay = valueDisplay;
            mFilePath = filePath;
            OFFSET_VALUE = offsetValue;
            MAX_VALUE = maxValue;

            SharedPreferences sharedPreferences = getSharedPreferences();

            // Read original value
            if (KernelUtils.fileExists(mFilePath)) {
                String sDefaultValue = KernelUtils.readOneLine(mFilePath);
                iValue = Integer.valueOf(sDefaultValue);
            } else {
                iValue = MAX_VALUE - OFFSET_VALUE;
            }
            mOriginal = iValue;

            mSeekBar.setMax(MAX_VALUE);
            reset();
            mSeekBar.setOnSeekBarChangeListener(this);
        }

        public void reset() {
            int iValue;

            iValue = mOriginal + OFFSET_VALUE;
            mSeekBar.setProgress(iValue);
            updateValue(mOriginal);
        }

        public void save() {
            int iValue;

            iValue = mSeekBar.getProgress() - OFFSET_VALUE;
            Editor editor = getEditor();
            editor.putInt(mFilePath, iValue);
            editor.commit();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int iValue;

            iValue = progress - OFFSET_VALUE;
            KernelUtils.writeValue(mFilePath, String.valueOf((long) iValue));
            updateValue(iValue);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Do nothing
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // Do nothing
        }

        public void updateValue(int progress) {
            mValueDisplay.setText(String.format("%d", (int) progress));
        }

    }

}
