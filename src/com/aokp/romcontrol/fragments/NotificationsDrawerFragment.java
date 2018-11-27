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
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.View;

import com.aokp.romcontrol.R;

public class NotificationsDrawerFragment extends Fragment {

    public NotificationsDrawerFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.container, new NotificationsDrawerSettingsPreferenceFragment())
                .commit();
    }

    public static class NotificationsDrawerSettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public NotificationsDrawerSettingsPreferenceFragment() {

        }

        private static final String PREF_SYSUI_QQS_COUNT = "sysui_qqs_count_key";
        private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
        private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";
        private static final String PREF_COLUMNS = "qs_columns";

        private ListPreference mSysuiQqsCount;
        private ListPreference mRowsPortrait;
        private ListPreference mRowsLandscape;
        private ListPreference mQsColumns;

        private ContentResolver mResolver;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_notificationsdrawer_settings);
            mResolver = getActivity().getContentResolver();
            PreferenceScreen prefSet = getPreferenceScreen();
            int defaultValue;

            mSysuiQqsCount = (ListPreference) findPreference(PREF_SYSUI_QQS_COUNT);
            int SysuiQqsCount = Settings.Secure.getInt(mResolver,
                   Settings.Secure.QQS_COUNT, 5);
            mSysuiQqsCount.setValue(Integer.toString(SysuiQqsCount));
            mSysuiQqsCount.setSummary(mSysuiQqsCount.getEntry());
            mSysuiQqsCount.setOnPreferenceChangeListener(this);

            mRowsPortrait = (ListPreference) findPreference(PREF_ROWS_PORTRAIT);
            int rowsPortrait = Settings.Secure.getInt(mResolver,
                    Settings.Secure.QS_ROWS_PORTRAIT, 3);
            mRowsPortrait.setValue(String.valueOf(rowsPortrait));
            mRowsPortrait.setSummary(mRowsPortrait.getEntry());
            mRowsPortrait.setOnPreferenceChangeListener(this);

            defaultValue = getResources().getInteger(com.android.internal.R.integer.config_qs_num_rows_landscape_default);
            mRowsLandscape = (ListPreference) findPreference(PREF_ROWS_LANDSCAPE);
            int rowsLandscape = Settings.Secure.getInt(mResolver,
                    Settings.Secure.QS_ROWS_LANDSCAPE, defaultValue);
            mRowsLandscape.setValue(String.valueOf(rowsLandscape));
            mRowsLandscape.setSummary(mRowsLandscape.getEntry());
            mRowsLandscape.setOnPreferenceChangeListener(this);

            mQsColumns = (ListPreference) findPreference(PREF_COLUMNS);
            int columnsQs = Settings.Secure.getInt(mResolver,
                    Settings.Secure.QS_COLUMNS, 3);
            mQsColumns.setValue(String.valueOf(columnsQs));
            mQsColumns.setSummary(mQsColumns.getEntry());
            mQsColumns.setOnPreferenceChangeListener(this);

            setHasOptionsMenu(true);
            return prefSet;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            mResolver = getActivity().getContentResolver();
            int intValue;
            int index;

            if (preference == mSysuiQqsCount) {
                String SysuiQqsCount = (String) newValue;
                int SysuiQqsCountValue = Integer.parseInt(SysuiQqsCount);
                Settings.Secure.putInt(mResolver,
                        Settings.Secure.QQS_COUNT, SysuiQqsCountValue);
                int SysuiQqsCountIndex = mSysuiQqsCount.findIndexOfValue(SysuiQqsCount);
                mSysuiQqsCount.setSummary(mSysuiQqsCount.getEntries()[SysuiQqsCountIndex]);
                return true;
            } else if (preference == mRowsPortrait) {
                intValue = Integer.valueOf((String) newValue);
                index = mRowsPortrait.findIndexOfValue((String) newValue);
                Settings.Secure.putInt(mResolver,
                        Settings.Secure.QS_ROWS_PORTRAIT, intValue);
                preference.setSummary(mRowsPortrait.getEntries()[index]);
                return true;
            } else if (preference == mRowsLandscape) {
                intValue = Integer.valueOf((String) newValue);
                index = mRowsLandscape.findIndexOfValue((String) newValue);
                Settings.Secure.putInt(mResolver,
                        Settings.Secure.QS_ROWS_LANDSCAPE, intValue);
                preference.setSummary(mRowsLandscape.getEntries()[index]);
                return true;
            } else if (preference == mQsColumns) {
                intValue = Integer.valueOf((String) newValue);
                index = mQsColumns.findIndexOfValue((String) newValue);
                Settings.Secure.putInt(mResolver,
                        Settings.Secure.QS_COLUMNS, intValue);
                preference.setSummary(mQsColumns.getEntries()[index]);
                return true;
            }
            return false;
        }
    }
}
