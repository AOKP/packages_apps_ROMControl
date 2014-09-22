/*
 * Copyright (C) 2013 The Android Open Kang Project
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

import android.app.Fragment;
import android.os.Bundle;
import android.content.Context;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;
import com.aokp.romcontrol.widgets.CategorySeparator;

public class HardwareKeysFragment extends Fragment implements OnSeekBarChangeListener {

    private static final int KEY_MASK_HOME = 0x01;
    private static final int KEY_MASK_BACK = 0x02;
    private static final int KEY_MASK_MENU = 0x04;
    private static final int KEY_MASK_ASSIST = 0x08;
    private static final int KEY_MASK_APP_SWITCH = 0x10;
    private static final int KEY_MASK_CAMERA = 0x20;

    private Context mContext;
    private int mSeekBarProgress;

    CheckboxSetting setting_customize;

    SingleChoiceSetting setting_key_home_long_press, setting_key_home_double_tap;
    SingleChoiceSetting setting_key_back, setting_key_back_long_press;
    SingleChoiceSetting setting_key_menu, setting_key_menu_long_press;
    SingleChoiceSetting setting_key_search, setting_key_search_long_press;
    SingleChoiceSetting setting_key_recents, setting_key_recents_long_press;
    SingleChoiceSetting setting_key_camera;

    int hardwareKeyMask;
    boolean mHasMenu, mHasBack, mHasHome, mHasAssist, mHasAppSwitch, mHasCamera;

    private SeekBar mButtonsBrightness, mButtonsBacklightTimeout;
    private TextView mButtonsBrightness_title, mButtonsBacklightTimeout_title, mTimeoutValue;
    private CategorySeparator mBacklight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        hardwareKeyMask = getActivity().getResources()
                .getInteger(com.android.internal.R.integer.config_deviceHardwareKeys);
        mHasMenu = (hardwareKeyMask & KEY_MASK_MENU) != 0;
        mHasBack = (hardwareKeyMask & KEY_MASK_BACK) != 0;
        mHasHome = (hardwareKeyMask & KEY_MASK_HOME) != 0;
        mHasAssist = (hardwareKeyMask & KEY_MASK_ASSIST) != 0;
        mHasAppSwitch = (hardwareKeyMask & KEY_MASK_APP_SWITCH) != 0;
        mHasCamera = (hardwareKeyMask & KEY_MASK_CAMERA) != 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hardware_keys, container, false);

        mButtonsBrightness = (SeekBar) v.findViewById(R.id.buttons_brightness);
        mButtonsBacklightTimeout = (SeekBar) v.findViewById(R.id.buttons_backlight_timeout);
        mButtonsBrightness_title = (TextView) v.findViewById(R.id.buttons_brightness_title);
        mButtonsBacklightTimeout_title = (TextView) v.findViewById(R.id.buttons_backlight_timeout_title);
        mTimeoutValue = (TextView) v.findViewById(R.id.timeout_value);
        mBacklight = (CategorySeparator) v.findViewById(R.id.category_backlight);

        /**
         * Hide customize hardware buttons checkbox
         */
        if (hardwareKeyMask == 0) {
            setting_customize = (CheckboxSetting) v.findViewById(R.id.setting_customize_hardware_keys);
            setting_customize.setVisibility(View.GONE);
            mButtonsBrightness.setVisibility(View.GONE);
            mButtonsBacklightTimeout.setVisibility(View.GONE);
            mButtonsBrightness_title.setVisibility(View.GONE);
            mButtonsBacklightTimeout_title.setVisibility(View.GONE);
            mBacklight.setVisibility(View.GONE);
            mTimeoutValue.setVisibility(View.GONE);
        } else {
            mButtonsBrightness.setProgress(Settings.AOKP.getInt(
                    mContext.getContentResolver(), Settings.AOKP.BUTTON_BRIGHTNESS, 255));
            mButtonsBrightness.setOnSeekBarChangeListener(this);
            mButtonsBacklightTimeout.setProgress(Settings.AOKP.getInt(
                    mContext.getContentResolver(), Settings.AOKP.BUTTON_BACKLIGHT_TIMEOUT, 5));
            mButtonsBacklightTimeout.setOnSeekBarChangeListener(this);
            handleTimeoutUpdate(mButtonsBacklightTimeout.getProgress());
            if (mButtonsBrightness.getProgress() == 0) {
                mButtonsBacklightTimeout.setVisibility(View.GONE);
                mButtonsBacklightTimeout_title.setVisibility(View.GONE);
                mTimeoutValue.setVisibility(View.GONE);
            }
        }

        /**
         * Filter out buttons
         */
        setting_key_home_long_press = (SingleChoiceSetting) v.findViewById(R.id.setting_key_home_long_press);
        setting_key_home_double_tap = (SingleChoiceSetting) v.findViewById(R.id.setting_key_home_double_tap);
        if (!mHasHome) {
            setting_key_home_long_press.setVisibility(View.GONE);
            setting_key_home_double_tap.setVisibility(View.GONE);
        }

        setting_key_back_long_press = (SingleChoiceSetting) v.findViewById(R.id.setting_key_back_long_press);
        if (!mHasBack) {
            setting_key_back_long_press.setVisibility(View.GONE);
        }

        setting_key_menu = (SingleChoiceSetting) v.findViewById(R.id.setting_key_menu);
        setting_key_menu_long_press = (SingleChoiceSetting) v.findViewById(R.id.setting_key_menu_long_press);
        if (!mHasMenu) {
            setting_key_menu.setVisibility(View.GONE);
            setting_key_menu_long_press.setVisibility(View.GONE);
        }

        setting_key_search = (SingleChoiceSetting) v.findViewById(R.id.setting_key_search);
        setting_key_search_long_press = (SingleChoiceSetting) v.findViewById(R.id.setting_key_search_long_press);
        if (!mHasAssist) {
            setting_key_search.setVisibility(View.GONE);
            setting_key_search_long_press.setVisibility(View.GONE);
        }

        setting_key_recents = (SingleChoiceSetting) v.findViewById(R.id.setting_key_recents);
        setting_key_recents_long_press = (SingleChoiceSetting) v.findViewById(R.id.setting_key_recents_long_press);
        if (!mHasAppSwitch) {
            setting_key_recents.setVisibility(View.GONE);
            setting_key_recents_long_press.setVisibility(View.GONE);
        }

        setting_key_camera = (SingleChoiceSetting) v.findViewById(R.id.setting_key_camera);
        if (!mHasCamera) {
            setting_key_camera.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == mButtonsBrightness) {
            Settings.AOKP.putInt(mContext.getContentResolver(), Settings.AOKP.BUTTON_BRIGHTNESS, progress);
            mButtonsBacklightTimeout.setVisibility(progress == 0 ? View.GONE : View.VISIBLE);
            mButtonsBacklightTimeout_title.setVisibility(progress == 0 ? View.GONE : View.VISIBLE);
            mTimeoutValue.setVisibility(progress == 0 ? View.GONE : View.VISIBLE);
        } else if (seekBar == mButtonsBacklightTimeout) {
            Settings.AOKP.putInt(mContext.getContentResolver(), Settings.AOKP.BUTTON_BACKLIGHT_TIMEOUT, progress);
            handleTimeoutUpdate(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void handleTimeoutUpdate(int timeout) {
        if (timeout == 0) {
            mTimeoutValue.setText(R.string.backlight_timeout_unlimited);
        } else {
            mTimeoutValue.setText(getTimeoutString(timeout));
        }
    }

    private String getTimeoutString(int timeout) {
        return mContext.getResources().getQuantityString(
                R.plurals.backlight_timeout_time, timeout, timeout);
    }
}
