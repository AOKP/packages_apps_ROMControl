/*
 * Copyright (C) 2018 AIM ROM
 * Copyright (C) 2018 AOKP
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
package com.aokp.romcontrol.fragments.statusbar;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;

import com.aokp.romcontrol.R;

import com.android.internal.logging.nano.MetricsProto;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class BatteryBarFragment extends Fragment {

    public BatteryBarFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_batterybar_main, container, false);

        Resources res = getResources();

        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.batterybar_main, new StatusBarSettingsPreferenceFragment())
                .commit();

        return v;
    }

    public static class BatteryBarPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public BatteryBarPreferenceFragment() {

        }

        private static final String PREF_BATT_BAR = "battery_bar_list";
        private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
        private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
        private static final String PREF_BATT_BAR_CHARGING_COLOR = "battery_bar_charging_color";
        private static final String PREF_BATT_BAR_LOW_COLOR_WARNING = "battery_bar_battery_low_color_warning";
        private static final String PREF_BATT_BAR_USE_GRADIENT_COLOR = "battery_bar_use_gradient_color";
        private static final String PREF_BATT_BAR_LOW_COLOR = "battery_bar_low_color";
        private static final String PREF_BATT_BAR_HIGH_COLOR = "battery_bar_high_color";
        private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
        private static final String PREF_BATT_ANIMATE = "battery_bar_animate";

        private ListPreference mBatteryBar;
        private ListPreference mBatteryBarStyle;
        private ListPreference mBatteryBarThickness;
        private SwitchPreference mBatteryBarChargingAnimation;
        private SwitchPreference mBatteryBarUseGradient;
        private ColorPickerPreference mBatteryBarColor;
        private ColorPickerPreference mBatteryBarChargingColor;
        private ColorPickerPreference mBatteryBarBatteryLowColor;
        private ColorPickerPreference mBatteryBarBatteryLowColorWarn;
        private ColorPickerPreference mBatteryBarBatteryHighColor;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            addPreferencesFromResource(R.xml.batterybar);
            PreferenceScreen prefSet = getPreferenceScreen();
            final ContentResolver resolver = getActivity().getContentResolver();

            int intColor;
            String hexColor;
            int highColor = 0xff99CC00;
            int lowColor = 0xffff4444;

            mBatteryBar = (ListPreference) findPreference(PREF_BATT_BAR);
            mBatteryBar.setOnPreferenceChangeListener(this);
            mBatteryBar.setValue((Settings.System.getInt(resolver, Settings.System.BATTERY_BAR_LOCATION, 0)) + "");
            mBatteryBar.setSummary(mBatteryBar.getEntry());

            mBatteryBarStyle = (ListPreference) findPreference(PREF_BATT_BAR_STYLE);
            mBatteryBarStyle.setOnPreferenceChangeListener(this);
            mBatteryBarStyle.setValue((Settings.System.getInt(resolver,
                    Settings.System.BATTERY_BAR_STYLE, 0)) + "");
            mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntry());

            mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_COLOR);
            mBatteryBarColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                    "battery_bar_color", 0xffffffff);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBatteryBarColor.setSummary(hexColor);
            mBatteryBarColor.setNewPreviewColor(intColor);
            mBatteryBarColor.setAlphaSliderEnabled(true);

            mBatteryBarChargingColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_CHARGING_COLOR);
            mBatteryBarChargingColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                    "battery_bar_charging_color", 0xFF00FF00);
            hexColor = String.format("#%08x", (0xFF00FF00 & intColor));
            mBatteryBarChargingColor.setSummary(hexColor);
            mBatteryBarChargingColor.setNewPreviewColor(intColor);
            mBatteryBarChargingColor.setAlphaSliderEnabled(true);

            mBatteryBarBatteryLowColorWarn = (ColorPickerPreference) findPreference(PREF_BATT_BAR_LOW_COLOR_WARNING);
            mBatteryBarBatteryLowColorWarn.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                    "battery_bar_battery_low_color_warning", 0xFFFF6600);
            hexColor = String.format("#%08x", (0xFFFF6600 & intColor));
            mBatteryBarBatteryLowColorWarn.setSummary(hexColor);
            mBatteryBarBatteryLowColorWarn.setNewPreviewColor(intColor);
            mBatteryBarBatteryLowColorWarn.setAlphaSliderEnabled(true);

            mBatteryBarBatteryLowColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_LOW_COLOR);
            mBatteryBarBatteryLowColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                     "battery_bar_low_color", 0xffffffff);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBatteryBarBatteryLowColor.setSummary(hexColor);
            mBatteryBarBatteryLowColor.setNewPreviewColor(intColor);
            mBatteryBarBatteryLowColor.setAlphaSliderEnabled(true);

            mBatteryBarBatteryHighColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_HIGH_COLOR);
            mBatteryBarBatteryHighColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                    "battery_bar_high_color", 0xff99CC00);
            hexColor = String.format("#%08x", (0xff99CC00 & intColor));
            mBatteryBarBatteryHighColor.setSummary(hexColor);
            mBatteryBarBatteryHighColor.setNewPreviewColor(intColor);
            mBatteryBarBatteryHighColor.setAlphaSliderEnabled(true);

            mBatteryBarUseGradient = (SwitchPreference) findPreference(PREF_BATT_BAR_USE_GRADIENT_COLOR);
            mBatteryBarUseGradient.setChecked(Settings.System.getInt(resolver,
                    Settings.System.BATTERY_BAR_USE_GRADIENT_COLOR, 0) == 1);

            mBatteryBarChargingAnimation = (SwitchPreference) findPreference(PREF_BATT_ANIMATE);
            mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(resolver,
                    Settings.System.BATTERY_BAR_ANIMATE, 0) == 1);

            mBatteryBarThickness = (ListPreference) findPreference(PREF_BATT_BAR_WIDTH);
            mBatteryBarThickness.setOnPreferenceChangeListener(this);
            mBatteryBarThickness.setValue((Settings.System.getInt(resolver,
                    Settings.System.BATTERY_BAR_THICKNESS, 1)) + "");
            mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntry());

            updateBatteryBarOptions();
            return prefSet;
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            AlertDialog dialog;
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mBatteryBarColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        "battery_bar_color", intHex);
                return true;
            } else if (preference == mBatteryBarChargingColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        "battery_bar_charging_color", intHex);
                return true;
            } else if (preference == mBatteryBarBatteryLowColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        "battery_bar_low_color", intHex);
                return true;
            } else if (preference == mBatteryBarBatteryLowColorWarn) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        "battery_bar_battery_low_color_warning", intHex);
                return true;
            } else if (preference == mBatteryBarBatteryHighColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        "battery_bar_high_color", intHex);
                return true;
            } else if (preference == mBatteryBar) {
                int val = Integer.valueOf((String) newValue);
                int index = mBatteryBar.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver, Settings.System.BATTERY_BAR_LOCATION, val);
                mBatteryBar.setSummary(mBatteryBar.getEntries()[index]);
                updateBatteryBarOptions();
                return true;
            } else if (preference == mBatteryBarStyle) {
                    int val = Integer.valueOf((String) newValue);
                int index = mBatteryBarStyle.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.BATTERY_BAR_STYLE, val);
                mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntries()[index]);
                return true;
            } else if (preference == mBatteryBarThickness) {
                int val = Integer.valueOf((String) newValue);
                int index = mBatteryBarThickness.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.BATTERY_BAR_THICKNESS, val);
                mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntries()[index]);
                return true;
            }
            return false;
        }

        public boolean onPreferenceTreeClick(Preference preference) {
            ContentResolver resolver = getActivity().getContentResolver();
            boolean value;

            if (preference == mBatteryBarChargingAnimation) {
                value = mBatteryBarChargingAnimation.isChecked();
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.BATTERY_BAR_ANIMATE, value ? 1 : 0);
                return true;
            } else if (preference == mBatteryBarUseGradient) {
                value = mBatteryBarUseGradient.isChecked();
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.BATTERY_BAR_USE_GRADIENT_COLOR, value ? 1 : 0);
                return true;
            }
            return false;
        }

        private void updateBatteryBarOptions() {
            if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BATTERY_BAR_LOCATION, 0) == 0) {
                mBatteryBarStyle.setEnabled(false);
                mBatteryBarThickness.setEnabled(false);
                mBatteryBarChargingAnimation.setEnabled(false);
                mBatteryBarColor.setEnabled(false);
                mBatteryBarChargingColor.setEnabled(false);
                mBatteryBarUseGradient.setEnabled(false);
                mBatteryBarBatteryLowColor.setEnabled(false);
                mBatteryBarBatteryHighColor.setEnabled(false);
                mBatteryBarBatteryLowColorWarn.setEnabled(false);
            } else {
                mBatteryBarStyle.setEnabled(true);
                mBatteryBarThickness.setEnabled(true);
                mBatteryBarChargingAnimation.setEnabled(true);
                mBatteryBarColor.setEnabled(true);
                mBatteryBarChargingColor.setEnabled(true);
                mBatteryBarUseGradient.setEnabled(true);
                mBatteryBarBatteryLowColor.setEnabled(true);
                mBatteryBarBatteryHighColor.setEnabled(true);
                mBatteryBarBatteryLowColorWarn.setEnabled(true);
            }
        }
    }
}
