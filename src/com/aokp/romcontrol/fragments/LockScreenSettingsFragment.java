/*
 * Copyright (C) 2016 The Android Open Kang Project
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

import android.app.Activity;
import android.app.WallpaperManager;
import android.app.Fragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.content.res.Configuration;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import cyanogenmod.providers.CMSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.SeekBarPreference;

public class LockScreenSettingsFragment extends Fragment {

    public LockScreenSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public SettingsPreferenceFragment() {

        }

        private static final String TAG = "LockScreenSettings";
        private static final String LOCKSCREEN_MAX_NOTIF_CONFIG = "lockscreen_max_notif_cofig";
        private static final String PREF_CONDITION_ICON =
                "weather_condition_icon";
        private static final String PREF_HIDE_WEATHER =
                "weather_hide_panel";
        private static final String PREF_NUMBER_OF_NOTIFICATIONS =
                "weather_number_of_notifications";

        private ContentResolver mResolver;

        private FingerprintManager mFingerprintManager;
        private SwitchPreference mFingerprintVib;
        private SeekBarPreference mMaxKeyguardNotifConfig;
        private static final int MONOCHROME_ICON = 0;

        private ListPreference mConditionIcon;
        private ListPreference mHideWeather;
        private SeekBarPreference mNumberOfNotifications;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_lockscreen_settings);
            mResolver = getActivity().getContentResolver();
            PreferenceScreen prefSet = getPreferenceScreen();

            // Fingerprint vibration
            mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
            mFingerprintVib = (SwitchPreference) prefSet.findPreference("fingerprint_success_vib");
            if (!mFingerprintManager.isHardwareDetected()){
                prefSet.removePreference(mFingerprintVib);
            }

            mMaxKeyguardNotifConfig = (SeekBarPreference) findPreference(LOCKSCREEN_MAX_NOTIF_CONFIG);
            int kgconf = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, 5);
            mMaxKeyguardNotifConfig.setValue(kgconf);
            mMaxKeyguardNotifConfig.setOnPreferenceChangeListener(this);

            mConditionIcon =
                    (ListPreference) findPreference(PREF_CONDITION_ICON);
            int conditionIcon = Settings.System.getInt(mResolver,
                   Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, MONOCHROME_ICON);
            mConditionIcon.setValue(String.valueOf(conditionIcon));
            mConditionIcon.setSummary(mConditionIcon.getEntry());
            mConditionIcon.setOnPreferenceChangeListener(this);

            mHideWeather =
                    (ListPreference) findPreference(PREF_HIDE_WEATHER);
            int hideWeather = Settings.System.getInt(mResolver,
                   Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, 0);
            mHideWeather.setValue(String.valueOf(hideWeather));
            mHideWeather.setOnPreferenceChangeListener(this);

            mNumberOfNotifications =
                    (SeekBarPreference) findPreference(PREF_NUMBER_OF_NOTIFICATIONS);
            int numberOfNotifications = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_NUMBER_OF_NOTIFICATIONS, 4);
            mNumberOfNotifications.setValue(numberOfNotifications);
            mNumberOfNotifications.setOnPreferenceChangeListener(this);

            updatePreference();
            setHasOptionsMenu(true);
        }

        @Override
        public void onResume() {
            super.onResume();
            updatePreference();
        }

        private void updatePreference() {
            int hideWeather = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, 0);
            if (hideWeather == 0) {
                mNumberOfNotifications.setEnabled(false);
                mHideWeather.setSummary(R.string.weather_hide_panel_auto_summary);
            } else if (hideWeather == 1) {
                mNumberOfNotifications.setEnabled(true);
                mHideWeather.setSummary(R.string.weather_hide_panel_custom_summary);
            } else {
                mNumberOfNotifications.setEnabled(false);
                mHideWeather.setSummary(R.string.weather_hide_panel_never_summary);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean value;

            if (preference == mMaxKeyguardNotifConfig) {
                int kgconf = (Integer) newValue;
                Settings.System.putInt(mResolver,
                        Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, kgconf);
                return true;
            } else if (preference == mConditionIcon) {
                int intValue = Integer.valueOf((String) newValue);
                int index = mConditionIcon.findIndexOfValue((String) newValue);
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, intValue);
                mConditionIcon.setSummary(mConditionIcon.getEntries()[index]);
                return true;
            } else if (preference == mHideWeather) {
                int intValue = Integer.valueOf((String) newValue);
                int index = mHideWeather.findIndexOfValue((String) newValue);
                    Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, intValue);
                updatePreference();
                return true;
            } else if (preference == mNumberOfNotifications) {
                int numberOfNotifications = (Integer) newValue;
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_NUMBER_OF_NOTIFICATIONS,
                numberOfNotifications);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
