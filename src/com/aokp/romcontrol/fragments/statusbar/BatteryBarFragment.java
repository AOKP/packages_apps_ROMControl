/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.aokp.romcontrol.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class BatteryBarFragment extends Fragment {

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new BatteryBarPreferenceFragment()).commit();
    }

    public static class BatteryBarPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {
				
		private static final String TAG = "BatteryBarFragment";

        private static final String PREF_BATT_BAR = "battery_bar_list";
        private static final String PREF_BATT_BAR_NO_NAVBAR = "battery_bar_no_navbar_list";
        private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
        private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
        private static final String PREF_BATT_BAR_CHARGING_COLOR = "battery_bar_charging_color";
        private static final String PREF_BATT_BAR_BATTERY_LOW_COLOR = "battery_bar_battery_low_color";
        private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
        private static final String PREF_BATT_ANIMATE = "battery_bar_animate";

        static final int DEFAULT_STATUS_CARRIER_COLOR = 0xffffffff;

        private ListPreference mBatteryBar;
        private ListPreference mBatteryBarNoNavbar;
        private ListPreference mBatteryBarStyle;
        private ListPreference mBatteryBarThickness;
        private SwitchPreference mBatteryBarChargingAnimation;
        private ColorPickerPreference mBatteryBarColor;
        private ColorPickerPreference mBatteryBarChargingColor;
        private ColorPickerPreference mBatteryBarBatteryLowColor;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.battery_bar);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            int intColor;
            String hexColor;

            mBatteryBar = (ListPreference) findPreference(PREF_BATT_BAR);
            mBatteryBar.setOnPreferenceChangeListener(this);
            mBatteryBar.setValue((Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR, 0)) + "");
            mBatteryBar.setSummary(mBatteryBar.getEntry());

            mBatteryBarNoNavbar = (ListPreference) findPreference(PREF_BATT_BAR_NO_NAVBAR);
            mBatteryBarNoNavbar.setOnPreferenceChangeListener(this);
            mBatteryBarNoNavbar.setValue((Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR, 0)) + "");
            mBatteryBarNoNavbar.setSummary(mBatteryBarNoNavbar.getEntry());

            mBatteryBarStyle = (ListPreference) findPreference(PREF_BATT_BAR_STYLE);
            mBatteryBarStyle.setOnPreferenceChangeListener(this);
            mBatteryBarStyle.setValue((Settings.System.getInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_STYLE, 0)) + "");
            mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntry());

            mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_COLOR);
            mBatteryBarColor.setOnPreferenceChangeListener(this);
            int defaultColor = 0xffffffff;
            intColor = Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR_COLOR, defaultColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBatteryBarColor.setSummary(hexColor);

            mBatteryBarChargingColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_CHARGING_COLOR);
            mBatteryBarChargingColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR_CHARGING_COLOR, defaultColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBatteryBarChargingColor.setSummary(hexColor);

            mBatteryBarBatteryLowColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_BATTERY_LOW_COLOR);
            mBatteryBarBatteryLowColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR_BATTERY_LOW_COLOR, defaultColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBatteryBarBatteryLowColor.setSummary(hexColor);

            mBatteryBarChargingAnimation = (SwitchPreference) findPreference(PREF_BATT_ANIMATE);
            mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, 0) == 1);

            mBatteryBarThickness = (ListPreference) findPreference(PREF_BATT_BAR_WIDTH);
            mBatteryBarThickness.setOnPreferenceChangeListener(this);
            mBatteryBarThickness.setValue((Settings.System.getInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, 1)) + "");
            mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntry());

            boolean hasNavBarByDefault = getResources().getBoolean(
                    com.android.internal.R.bool.config_showNavigationBar);
            //boolean enableNavigationBar = Settings.Secure.getInt(resolver,
            //    Settings.Secure.NAVIGATION_BAR_VISIBLE, hasNavBarByDefault ? 1 : 0) == 1;

            //if (!hasNavBarByDefault || !enableNavigationBar) {
                prefSet.removePreference(mBatteryBar);
            //} else {
            //    prefSet.removePreference(mBatteryBarNoNavbar);
            //}

            updateBatteryBarOptions();

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mBatteryBarColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                        .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.STATUSBAR_BATTERY_BAR_COLOR, intHex);
                return true;
            } else if (preference == mBatteryBarChargingColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                        .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.STATUSBAR_BATTERY_BAR_CHARGING_COLOR, intHex);
                return true;
            } else if (preference == mBatteryBarBatteryLowColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                        .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.STATUSBAR_BATTERY_BAR_BATTERY_LOW_COLOR, intHex);
                return true;
            } else if (preference == mBatteryBar) {
                int val = Integer.valueOf((String) newValue);
                int index = mBatteryBar.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.STATUSBAR_BATTERY_BAR, val);
                mBatteryBar.setSummary(mBatteryBar.getEntries()[index]);
                updateBatteryBarOptions();
            } else if (preference == mBatteryBarNoNavbar) {
                int val = Integer.valueOf((String) newValue);
                int index = mBatteryBarNoNavbar.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.STATUSBAR_BATTERY_BAR, val);
                mBatteryBarNoNavbar.setSummary(mBatteryBarNoNavbar.getEntries()[index]);
                updateBatteryBarOptions();
                return true;
            } else if (preference == mBatteryBarStyle) {
                int val = Integer.valueOf((String) newValue);
                int index = mBatteryBarStyle.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.STATUSBAR_BATTERY_BAR_STYLE, val);
                mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntries()[index]);
                return true;
            } else if (preference == mBatteryBarThickness) {
                int val = Integer.valueOf((String) newValue);
                int index = mBatteryBarThickness.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, val);
                mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntries()[index]);
                return true;
            }
            return false;
        }

        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            ContentResolver resolver = getActivity().getContentResolver();
            boolean value;
            if (preference == mBatteryBarChargingAnimation) {
                value = mBatteryBarChargingAnimation.isChecked();
                Settings.System.putInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, value ? 1 : 0);
                return true;
            }
            return false;
        }

        private void updateBatteryBarOptions() {
            if (Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR, 0) == 0) {
                mBatteryBarStyle.setEnabled(false);
                mBatteryBarThickness.setEnabled(false);
                mBatteryBarChargingAnimation.setEnabled(false);
                mBatteryBarColor.setEnabled(false);
                mBatteryBarChargingColor.setEnabled(false);
                mBatteryBarBatteryLowColor.setEnabled(false);
            } else {
                mBatteryBarStyle.setEnabled(true);
                mBatteryBarThickness.setEnabled(true);
                mBatteryBarChargingAnimation.setEnabled(true);
                mBatteryBarColor.setEnabled(true);
                mBatteryBarChargingColor.setEnabled(true);
                mBatteryBarBatteryLowColor.setEnabled(true);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }
}
