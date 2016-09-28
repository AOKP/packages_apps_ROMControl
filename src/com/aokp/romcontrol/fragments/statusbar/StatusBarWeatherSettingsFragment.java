/*
 * Copyright (C) 2017 The Android Open Kang Project
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
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.SeekBarPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarWeatherSettingsFragment extends Fragment {

    public StatusBarWeatherSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_statusbarweather_settings_main, container, false);

        Resources res = getResources();

        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.statusbarweather_settings_main, new StatusBarWeatherSettingsPreferenceFragment())
                .commit();

        return v;
    }

    public static class StatusBarWeatherSettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {
        public StatusBarWeatherSettingsPreferenceFragment() {

        }

        private static final String TAG = "StatusBarWeather";

        private static final String STATUS_BAR_TEMPERATURE = "status_bar_temperature";
        private static final String PREF_STATUS_BAR_WEATHER_SIZE = "status_bar_weather_size";
        private static final String PREF_STATUS_BAR_WEATHER_FONT_STYLE = "status_bar_weather_font_style";


        private ListPreference mStatusBarTemperature;
        private SeekBarPreference mStatusBarTemperatureSize;
        private ListPreference mStatusBarTemperatureFontStyle;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
            updateWeatherOptions();
        }

        private PreferenceScreen createCustomView() {
            addPreferencesFromResource(R.xml.fragment_status_bar_weather);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mStatusBarTemperature = (ListPreference) findPreference(STATUS_BAR_TEMPERATURE);
            int temperatureShow = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, 0,
                    UserHandle.USER_CURRENT);
            mStatusBarTemperature.setValue(String.valueOf(temperatureShow));
            mStatusBarTemperature.setSummary(mStatusBarTemperature.getEntry());
            mStatusBarTemperature.setOnPreferenceChangeListener(this);

            mStatusBarTemperatureSize = (SeekBarPreference) findPreference(PREF_STATUS_BAR_WEATHER_SIZE);
            mStatusBarTemperatureSize.setValue(Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_WEATHER_SIZE, 14));
            mStatusBarTemperatureSize.setOnPreferenceChangeListener(this);

            mStatusBarTemperatureFontStyle = (ListPreference) findPreference(PREF_STATUS_BAR_WEATHER_FONT_STYLE);
            mStatusBarTemperatureFontStyle.setOnPreferenceChangeListener(this);
            mStatusBarTemperatureFontStyle.setValue(Integer.toString(Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_WEATHER_FONT_STYLE, 0)));
            mStatusBarTemperatureFontStyle.setSummary(mStatusBarTemperatureFontStyle.getEntry());

            return prefSet;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mStatusBarTemperature) {
                int temperatureShow = Integer.valueOf((String) newValue);
                int index = mStatusBarTemperature.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(
                        resolver, Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, temperatureShow,
                        UserHandle.USER_CURRENT);
                mStatusBarTemperature.setSummary(
                        mStatusBarTemperature.getEntries()[index]);
                updateWeatherOptions();
                return true;
            } else if (preference == mStatusBarTemperatureSize) {
                int width = ((Integer)newValue).intValue();
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_WEATHER_SIZE, width);
                return true;
            } else if (preference == mStatusBarTemperatureFontStyle) {
                int val = Integer.parseInt((String) newValue);
                int index = mStatusBarTemperatureFontStyle.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_WEATHER_FONT_STYLE, val);
                mStatusBarTemperatureFontStyle.setSummary(mStatusBarTemperatureFontStyle.getEntries()[index]);
                return true;
            }
            return false;
        }

        private void updateWeatherOptions() {
            if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, 0) == 0) {
                mStatusBarTemperatureSize.setEnabled(false);
                mStatusBarTemperatureFontStyle.setEnabled(false);
            } else {
                mStatusBarTemperatureSize.setEnabled(true);
                mStatusBarTemperatureFontStyle.setEnabled(true);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }
}

