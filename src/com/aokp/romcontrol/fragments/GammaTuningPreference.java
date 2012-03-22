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
            R.id.gamma_red_seekbar, R.id.gamma_green_seekbar, R.id.gamma_blue_seekbar
    };

    private static final int[] VALUE_DISPLAY_ID = new int[] {
            R.id.gamma_red_value, R.id.gamma_green_value, R.id.gamma_blue_value
    };

    private static final String[] FILE_PATH = new String[] {
            "/sys/class/misc/samoled_color/red_v1_offset",
            "/sys/class/misc/samoled_color/green_v1_offset",
            "/sys/class/misc/samoled_color/blue_v1_offset"
    };

    private GammaSeekBar mSeekBars[] = new GammaSeekBar[3];
    
    private static final int DEFAULT_GAMMA = 60;

    private static final int MAX_VALUE = 80;

    private static final int OFFSET_VALUE = 0;
    
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
            mSeekBars[i] = new GammaSeekBar(seekBar, valueDisplay, FILE_PATH[i]);
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
        for (String filePath : FILE_PATH) {
            String sDefaultValue = KernelUtils.readOneLine(filePath);
            int iValue = sharedPrefs.getInt(filePath, Integer.valueOf(sDefaultValue));
            KernelUtils.writeValue(filePath, String.valueOf((long) iValue));
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

        public GammaSeekBar(SeekBar seekBar, TextView valueDisplay, String filePath) {
            int iValue;

            mSeekBar = seekBar;
            mValueDisplay = valueDisplay;
            mFilePath = filePath;

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
