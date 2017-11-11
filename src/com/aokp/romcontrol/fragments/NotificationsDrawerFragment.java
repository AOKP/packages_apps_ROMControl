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
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.SystemSettingSeekBarPreference;
import com.aokp.romcontrol.settings.SystemSettingSwitchPreference;

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

        private static final String CUSTOM_HEADER_BROWSE = "custom_header_browse";
        private static final String CUSTOM_HEADER_IMAGE = "status_bar_custom_header";
        private static final String DAYLIGHT_HEADER_PACK = "daylight_header_pack";
        private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
        private static final String CUSTOM_HEADER_PROVIDER = "custom_header_provider";
        private static final String STATUS_BAR_CUSTOM_HEADER = "status_bar_custom_header";
        private static final String CUSTOM_HEADER_ENABLED = "status_bar_custom_header";

        private ListPreference mSysuiQqsCount;
        private ListPreference mRowsPortrait;
        private ListPreference mRowsLandscape;
        private ListPreference mQsColumns;

        private Preference mHeaderBrowse;
        private ListPreference mDaylightHeaderPack;
        private SystemSettingSeekBarPreference mHeaderShadow;
        private ListPreference mHeaderProvider;
        private String mDaylightHeaderProvider;
        private SwitchPreference mHeaderEnabled;

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

            mHeaderBrowse = findPreference(CUSTOM_HEADER_BROWSE);
            mHeaderBrowse.setEnabled(isBrowseHeaderAvailable());

            mHeaderEnabled = (SwitchPreference) findPreference(CUSTOM_HEADER_ENABLED);
            mHeaderEnabled.setOnPreferenceChangeListener(this);

            mDaylightHeaderPack = (ListPreference) findPreference(DAYLIGHT_HEADER_PACK);

            List<String> entries = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            getAvailableHeaderPacks(entries, values);
            mDaylightHeaderPack.setEntries(entries.toArray(new String[entries.size()]));
            mDaylightHeaderPack.setEntryValues(values.toArray(new String[values.size()]));

            boolean headerEnabled = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER, 0) != 0;
            updateHeaderProviderSummary(headerEnabled);
            mDaylightHeaderPack.setOnPreferenceChangeListener(this);

            mHeaderShadow = (SystemSettingSeekBarPreference) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
            final int headerShadow = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, 0);
            mHeaderShadow.setValue((int)(((double) headerShadow / 255) * 100));
            mHeaderShadow.setOnPreferenceChangeListener(this);

            mDaylightHeaderProvider = getResources().getString(R.string.daylight_header_provider);
            String providerName = Settings.System.getString(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER);
            if (providerName == null) {
                providerName = mDaylightHeaderProvider;
            }
            mHeaderProvider = (ListPreference) findPreference(CUSTOM_HEADER_PROVIDER);
            int valueIndex = mHeaderProvider.findIndexOfValue(providerName);
            mHeaderProvider.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
            mHeaderProvider.setSummary(mHeaderProvider.getEntry());
            mHeaderProvider.setOnPreferenceChangeListener(this);
            mDaylightHeaderPack.setEnabled(providerName.equals(mDaylightHeaderProvider));

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

            } else if (preference == mDaylightHeaderPack) {
                String value = (String) newValue;
                Settings.System.putString(mResolver,
                        Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK, value);
                int valueIndex = mDaylightHeaderPack.findIndexOfValue(value);
                mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntries()[valueIndex]);
                return true;
            } else if (preference == mHeaderShadow) {
                Integer headerShadow = (Integer) newValue;
                int realHeaderValue = (int) (((double) headerShadow / 100) * 255);
                Settings.System.putInt(mResolver,
                        Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, realHeaderValue);
                return true;
            } else if (preference == mHeaderProvider) {
                String value = (String) newValue;
                Settings.System.putString(mResolver,
                        Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER, value);
                int valueIndex = mHeaderProvider.findIndexOfValue(value);
                mHeaderProvider.setSummary(mHeaderProvider.getEntries()[valueIndex]);
                mDaylightHeaderPack.setEnabled(value.equals(mDaylightHeaderProvider));
                mHeaderBrowse.setTitle(valueIndex == 0 ? R.string.custom_header_browse_title : R.string.custom_header_pick_title);
                mHeaderBrowse.setSummary(valueIndex == 0 ? R.string.custom_header_browse_summary_new : R.string.custom_header_pick_summary);
                return true;
            } else if (preference == mHeaderEnabled) {
                Boolean headerEnabled = (Boolean) newValue;
                updateHeaderProviderSummary(headerEnabled);
                return true;
            }
            return false;
        }

        private void updateHeaderProviderSummary(boolean headerEnabled) {
            mDaylightHeaderPack.setSummary(getResources().getString(R.string.header_provider_disabled));
            if (headerEnabled) {
                String settingHeaderPackage = Settings.System.getString(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK);
                int valueIndex = mDaylightHeaderPack.findIndexOfValue(settingHeaderPackage);
                if (valueIndex == -1) {
                    // no longer found
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.STATUS_BAR_CUSTOM_HEADER, 0);
                } else {
                    mDaylightHeaderPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
                    mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntry());
                }
            }
        }

        private boolean isBrowseHeaderAvailable() {
            PackageManager pm = getActivity().getPackageManager();
            Intent browse = new Intent();
            browse.setClassName("org.omnirom.omnistyle", "org.omnirom.omnistyle.PickHeaderActivity");
            return pm.resolveActivity(browse, 0) != null;
        }

        private void getAvailableHeaderPacks(List<String> entries, List<String> values) {
            Map<String, String> headerMap = new HashMap<String, String>();
            Intent i = new Intent();
            PackageManager packageManager = getActivity().getPackageManager();
            i.setAction("org.omnirom.DaylightHeaderPack");
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                String packageName = r.activityInfo.packageName;
                String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
                if (label == null) {
                    label = r.activityInfo.packageName;
                }
                headerMap.put(label, packageName);
            }
            i.setAction("org.omnirom.DaylightHeaderPack1");
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                String packageName = r.activityInfo.packageName;
                String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
                if (r.activityInfo.name.endsWith(".theme")) {
                    continue;
                }
                if (label == null) {
                    label = packageName;
                }
                headerMap.put(label, packageName  + "/" + r.activityInfo.name);
            }
            List<String> labelList = new ArrayList<String>();
            labelList.addAll(headerMap.keySet());
            Collections.sort(labelList);
            for (String label : labelList) {
                entries.add(label);
                values.add(headerMap.get(label));
            }
        }
    }
}
