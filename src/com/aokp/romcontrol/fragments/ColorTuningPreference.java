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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aokp.romcontrol.util.KernelUtils;
import com.aokp.romcontrol.R;


public class ColorTuningPreference extends DialogPreference {

    private static final String TAG = "COLOR...";
    private static final int DEFAULT_COLOR_MULT = 1002159035;

    enum Colors {
        RED, GREEN, BLUE
    };

    private static final int[] SEEKBAR_ID = new int[] {
            R.id.color_red_seekbar, R.id.color_green_seekbar, R.id.color_blue_seekbar
    };

    private static final int[] VALUE_DISPLAY_ID = new int[] {
            R.id.color_red_value, R.id.color_green_value, R.id.color_blue_value
    };

    private static final String[] FILE_PATH = new String[] {
            "/sys/class/misc/samoled_color/red_multiplier",
            "/sys/class/misc/samoled_color/green_multiplier",
            "/sys/class/misc/samoled_color/blue_multiplier"
    };

    private Button mReset_button;
    private ColorSeekBar mSeekBars[] = new ColorSeekBar[3];

    // Align MAX_VALUE with Voodoo Control settings
    private static final int MAX_VALUE = Integer.MAX_VALUE - 2;

    // Track instances to know when to restore original color
    // (when the orientation changes, a new dialog is created before the old one
    // is destroyed)
    private static int sInstances = 0;

    public ColorTuningPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.preference_dialog_color_tuning);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        sInstances++;

        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            TextView valueDisplay = (TextView) view.findViewById(VALUE_DISPLAY_ID[i]);
            mSeekBars[i] = new ColorSeekBar(seekBar, valueDisplay, FILE_PATH[i]);
        }
        mReset_button = (Button) view.findViewById(R.id.reset_button);
        mReset_button.setOnClickListener(new Button.OnClickListener() {  
            public void onClick(View v) {
            	for (ColorSeekBar seekbar : mSeekBars){
            		seekbar.mSeekBar.setProgress(DEFAULT_COLOR_MULT);
                }
            }
            });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        sInstances--;

        if (positiveResult) {
            for (ColorSeekBar csb : mSeekBars) {
                csb.save();
            }
        } else if (sInstances == 0) {
            for (ColorSeekBar csb : mSeekBars) {
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
        int iValue, iValue2;
        if (!isSupported()) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (String filePath : FILE_PATH) {
            String sDefaultValue = KernelUtils.readOneLine(filePath);
            Log.d(TAG,"INIT: " + sDefaultValue);
            try {
                iValue2 = Integer.parseInt(sDefaultValue);
            } catch (NumberFormatException e) {
                iValue2 = MAX_VALUE;
            }
            try {
                iValue = sharedPrefs.getInt(filePath, iValue2);
                Log.d(TAG, "restore: iValue: " + iValue + " File: " + filePath);
            } catch (NumberFormatException e) {
                iValue = iValue2;
                Log.e(TAG, "restore ERROR: iValue: " + iValue + " File: " + filePath);
            }
            KernelUtils.writeColor(filePath, (int) iValue);
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
    
    class ColorSeekBar implements SeekBar.OnSeekBarChangeListener {

        private String mFilePath;

        private int mOriginal;

        private SeekBar mSeekBar;

        private TextView mValueDisplay;

        public ColorSeekBar(SeekBar seekBar, TextView valueDisplay, String filePath) {
            int iValue;

            mSeekBar = seekBar;
            mValueDisplay = valueDisplay;
            mFilePath = filePath;

            SharedPreferences sharedPreferences = getSharedPreferences();

            // Read original value
            if (KernelUtils.fileExists(mFilePath)) {
                String sDefaultValue = KernelUtils.readOneLine(mFilePath);
                iValue = (int) (Long.valueOf(sDefaultValue) / 2);
            } else {
                iValue = sharedPreferences.getInt(mFilePath, MAX_VALUE);
            }
            mOriginal = iValue;

            mSeekBar.setMax(MAX_VALUE);
            reset();
            mSeekBar.setOnSeekBarChangeListener(this);
        }

        public void reset() {
            mSeekBar.setProgress(mOriginal);
            updateValue(mOriginal);
        }

        public void save() {
            Editor editor = getEditor();
            editor.putInt(mFilePath, mSeekBar.getProgress());
            editor.commit();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            KernelUtils.writeColor(mFilePath, progress);
            updateValue(progress);
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
            mValueDisplay.setText(String.format("%.10f", (double) progress / MAX_VALUE));
        }

    }

}
