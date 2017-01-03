/*
 * Copyright (C) 2018 The Android Open Kang Project
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
import android.app.Fragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.View;

import com.aokp.romcontrol.R;

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

        private static final String PREF_LS_CLOCK = "hide_lockscreen_clock";
        private static final String PREF_LS_DATE = "hide_lockscreen_date";

        private SwitchPreference mLockscreenClock;
        private SwitchPreference mLockscreenDate;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_lockscreen_settings);
            ContentResolver resolver = getActivity().getContentResolver();
            PreferenceScreen prefSet = getPreferenceScreen();

            mLockscreenClock = (SwitchPreference) findPreference(PREF_LS_CLOCK);
            mLockscreenClock.setChecked(Settings.System.getInt(resolver,
                    Settings.System.HIDE_LOCKSCREEN_CLOCK, 1) != 0);
            mLockscreenClock.setOnPreferenceChangeListener(this);

            mLockscreenDate = (SwitchPreference) findPreference(PREF_LS_DATE);
            mLockscreenDate.setChecked(Settings.System.getInt(resolver,
                    Settings.System.HIDE_LOCKSCREEN_DATE, 1) != 0);
            mLockscreenDate.setOnPreferenceChangeListener(this);

            return prefSet;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mLockscreenClock) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.HIDE_LOCKSCREEN_CLOCK, value ? 1 : 0);
                return true;
            } else if (preference == mLockscreenDate) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.HIDE_LOCKSCREEN_DATE, value ? 1 : 0);
                return true;
            }
            return false;
        }
    }
}