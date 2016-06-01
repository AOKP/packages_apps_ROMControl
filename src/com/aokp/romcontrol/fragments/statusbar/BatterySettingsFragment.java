/*
* Copyright (C) 2015 The Android Open Kang Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.aokp.romcontrol.fragments.statusbar;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import cyanogenmod.providers.CMSettings;
import org.cyanogenmod.internal.logging.CMMetricsLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.SeekBarPreference;

public class BatterySettingsFragment extends Fragment {

    public BatterySettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_battery_settings_main, container, false);

        Resources res = getResources();

        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.battery_settings_main, new BatterySettingsPreferenceFragment())
                .commit();

        return v;
    }



    public static class BatterySettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public BatterySettingsPreferenceFragment() {

        }

        private static final String TAG = "BatterySettings";

        private ContentResolver mContentResolver;

        private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
        private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

        private static final String PREF_BATT_BAR = "battery_bar_list";
        private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
        private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
        private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
        private static final String PREF_BATT_ANIMATE = "battery_bar_animate";

        private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;
        private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;

        private ListPreference mStatusBarBattery;
        private ListPreference mStatusBarBatteryShowPercent;
        private ListPreference mBatteryBar;
        private ListPreference mBatteryBarStyle;
        private ListPreference mBatteryBarThickness;
        private SwitchPreference mBatteryBarChargingAnimation;
        private ColorPickerPreference mBatteryBarColor;

        private boolean mCheckPreferences;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            mCheckPreferences = false;
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_battery_settings);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            int intColorCarrierColor;
            int intColor;
            String hexColor;
            String hexColorCarrierColor;

            mBatteryBar = (ListPreference) findPreference(PREF_BATT_BAR);
            mBatteryBar.setOnPreferenceChangeListener(this);
            mBatteryBar.setValue((Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR, 0)) + "");
            mBatteryBar.setSummary(mBatteryBar.getEntry());

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

            mBatteryBarChargingAnimation = (SwitchPreference) findPreference(PREF_BATT_ANIMATE);
            mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, 0) == 1);

            mBatteryBarThickness = (ListPreference) findPreference(PREF_BATT_BAR_WIDTH);
            mBatteryBarThickness.setOnPreferenceChangeListener(this);
            mBatteryBarThickness.setValue((Settings.System.getInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, 1)) + "");
            mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntry());
            updateBatteryBarOptions();

            mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
            mStatusBarBatteryShowPercent =
                    (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

            int batteryStyle = CMSettings.System.getInt(resolver,
                CMSettings.System.STATUS_BAR_BATTERY_STYLE, 0);
            mStatusBarBattery.setValue(String.valueOf(batteryStyle));
                mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
            mStatusBarBattery.setOnPreferenceChangeListener(this);

            int batteryShowPercent = CMSettings.System.getInt(resolver,
                    CMSettings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
            mStatusBarBatteryShowPercent.setValue(String.valueOf(batteryShowPercent));
            mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
            enableStatusBarBatteryDependents(batteryStyle);
            mStatusBarBatteryShowPercent.setOnPreferenceChangeListener(this);

            setHasOptionsMenu(true);
            mCheckPreferences = true;
            return prefSet;
        }

        protected ContentResolver getContentResolver() {
            Context context = getActivity();
            if (context != null) {
                mContentResolver = context.getContentResolver();
            }
            return mContentResolver;
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        protected int getMetricsCategory() {
            // todo add a constant in MetricsLogger.java
            return CMMetricsLogger.DONT_LOG;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (!mCheckPreferences) {
                return false;
            }
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mStatusBarBattery) {
                int batteryStyle = Integer.valueOf((String) newValue);
                int index = mStatusBarBattery.findIndexOfValue((String) newValue);
                CMSettings.System.putInt(
                        resolver, CMSettings.System.STATUS_BAR_BATTERY_STYLE, batteryStyle);
                mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
                enableStatusBarBatteryDependents(batteryStyle);
                return true;
            } else if (preference == mStatusBarBatteryShowPercent) {
                int batteryShowPercent = Integer.valueOf((String) newValue);
                int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) newValue);
                CMSettings.System.putInt(
                        resolver, CMSettings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, batteryShowPercent);
                mStatusBarBatteryShowPercent.setSummary(
                        mStatusBarBatteryShowPercent.getEntries()[index]);
                return true;
            } else if (preference == mBatteryBarColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                        .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.STATUSBAR_BATTERY_BAR_COLOR, intHex);
                return true;
            } else if (preference == mBatteryBar) {
                int val = Integer.valueOf((String) newValue);
                int index = mBatteryBar.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.STATUSBAR_BATTERY_BAR, val);
                mBatteryBar.setSummary(mBatteryBar.getEntries()[index]);
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
             } else {
                 mBatteryBarStyle.setEnabled(true);
                 mBatteryBarThickness.setEnabled(true);
                 mBatteryBarChargingAnimation.setEnabled(true);
                 mBatteryBarColor.setEnabled(true);
             }
         }

        private void enableStatusBarBatteryDependents(int batteryIconStyle) {
            if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_HIDDEN ||
                    batteryIconStyle == STATUS_BAR_BATTERY_STYLE_TEXT) {
                mStatusBarBatteryShowPercent.setEnabled(false);
            } else {
                mStatusBarBatteryShowPercent.setEnabled(true);
            }
        }
    }
}
