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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.widgets.SeekBarPreferenceCham;
import cyanogenmod.providers.CMSettings;

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

        private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
        private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
        private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
        private static final String PREF_SYSUI_QQS_COUNT = "sysui_qqs_count_key";
        private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
        private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";
        private static final String PREF_COLUMNS = "qs_columns";
        private static final String PREF_SMART_PULLDOWN = "smart_pulldown";
        private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";

        private static final String CUSTOM_HEADER_IMAGE = "status_bar_custom_header";
        private static final String DAYLIGHT_HEADER_PACK = "daylight_header_pack";
        private static final String DEFAULT_HEADER_PACKAGE = "com.android.systemui";
        private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
        private static final String CUSTOM_HEADER_PROVIDER = "custom_header_provider";
        private static final String CUSTOM_HEADER_BROWSE = "custom_header_browse";
        private static final String NOTIFICATION_GUTS_KILL_APP_BUTTON = "notification_guts_kill_app_button";

        private static final String WEATHER_CATEGORY = "weather_category";
        private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
        private static final String PREF_STATUS_BAR_WEATHER = "status_bar_weather";

        private ListPreference mDaylightHeaderPack;
        private ListPreference mTileAnimationStyle;
        private ListPreference mTileAnimationDuration;
        private ListPreference mTileAnimationInterpolator;
        private ListPreference mSysuiQqsCount;
        private ListPreference mRowsPortrait;
        private ListPreference mRowsLandscape;
        private ListPreference mQsColumns;
        private ListPreference mSmartPulldown;
        private ListPreference mQuickPulldown;
        private SeekBarPreferenceCham mHeaderShadow;
        private ListPreference mHeaderProvider;
        private PreferenceCategory mWeatherCategory;

        private String mDaylightHeaderProvider;
        private PreferenceScreen mHeaderBrowse;
        private Preference mNotificationKill;

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
            PackageManager pm = getActivity().getPackageManager();
            int defaultValue;
            mWeatherCategory = (PreferenceCategory) prefSet.findPreference(WEATHER_CATEGORY);

            // QS tile animation
            mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
            int tileAnimationStyle = Settings.System.getIntForUser(mResolver,
                    Settings.System.ANIM_TILE_STYLE, 0,
                    UserHandle.USER_CURRENT);
            mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
            updateTileAnimationStyleSummary(tileAnimationStyle);
            updateAnimTileStyle(tileAnimationStyle);
            mTileAnimationStyle.setOnPreferenceChangeListener(this);

            mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
            int tileAnimationDuration = Settings.System.getIntForUser(mResolver,
                    Settings.System.ANIM_TILE_DURATION, 1500,
                    UserHandle.USER_CURRENT);
            mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
            updateTileAnimationDurationSummary(tileAnimationDuration);
            mTileAnimationDuration.setOnPreferenceChangeListener(this);

            mTileAnimationInterpolator = (ListPreference) findPreference(PREF_TILE_ANIM_INTERPOLATOR);
            int tileAnimationInterpolator = Settings.System.getIntForUser(mResolver,
                    Settings.System.ANIM_TILE_INTERPOLATOR, 0,
                    UserHandle.USER_CURRENT);
            mTileAnimationInterpolator.setValue(String.valueOf(tileAnimationInterpolator));
            updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
            mTileAnimationInterpolator.setOnPreferenceChangeListener(this);

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

            mSmartPulldown = (ListPreference) findPreference(PREF_SMART_PULLDOWN);
            mSmartPulldown.setOnPreferenceChangeListener(this);
            int smartPulldown = Settings.System.getInt(mResolver,
                    Settings.System.QS_SMART_PULLDOWN, 0);
            mSmartPulldown.setValue(String.valueOf(smartPulldown));
            updateSmartPulldownSummary(smartPulldown);

            mQuickPulldown = (ListPreference) findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);
            int quickPulldown = CMSettings.System.getInt(mResolver,
                    CMSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 1);
            mQuickPulldown.setValue(String.valueOf(quickPulldown));
            updatePulldownSummary(quickPulldown);
            mQuickPulldown.setOnPreferenceChangeListener(this);

            String settingHeaderPackage = Settings.System.getString(mResolver,
                    Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK);
            if (settingHeaderPackage == null) {
                settingHeaderPackage = DEFAULT_HEADER_PACKAGE;
            }
            mDaylightHeaderPack = (ListPreference) findPreference(DAYLIGHT_HEADER_PACK);

            List<String> entries = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            getAvailableHeaderPacks(entries, values);
            mDaylightHeaderPack.setEntries(entries.toArray(new String[entries.size()]));
            mDaylightHeaderPack.setEntryValues(values.toArray(new String[values.size()]));

            int valueIndex = mDaylightHeaderPack.findIndexOfValue(settingHeaderPackage);
            if (valueIndex == -1) {
                // no longer found
                settingHeaderPackage = DEFAULT_HEADER_PACKAGE;
                Settings.System.putString(mResolver,
                        Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK, settingHeaderPackage);
                valueIndex = mDaylightHeaderPack.findIndexOfValue(settingHeaderPackage);
            }
            mDaylightHeaderPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
            mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntry());
            mDaylightHeaderPack.setOnPreferenceChangeListener(this);

            mHeaderShadow = (SeekBarPreferenceCham) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
            final int headerShadow = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, 80);
            mHeaderShadow.setValue((int)(((double) headerShadow / 255) * 100));
            mHeaderShadow.setOnPreferenceChangeListener(this);

            mDaylightHeaderProvider = getResources().getString(R.string.daylight_header_provider);
            String providerName = Settings.System.getString(mResolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER);
            if (providerName == null) {
                providerName = mDaylightHeaderProvider;
            }
            mHeaderProvider = (ListPreference) findPreference(CUSTOM_HEADER_PROVIDER);
            valueIndex = mHeaderProvider.findIndexOfValue(providerName);
            mHeaderProvider.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
            mHeaderProvider.setSummary(mHeaderProvider.getEntry());
            mHeaderProvider.setOnPreferenceChangeListener(this);
            mDaylightHeaderPack.setEnabled(providerName.equals(mDaylightHeaderProvider));
            mHeaderBrowse = (PreferenceScreen) findPreference(CUSTOM_HEADER_BROWSE);
            mHeaderBrowse.setEnabled(isBrowseHeaderAvailable());

            mNotificationKill = findPreference(NOTIFICATION_GUTS_KILL_APP_BUTTON);
            mNotificationKill.setOnPreferenceChangeListener(this);

            // Status bar weather category
            if (mWeatherCategory != null && (!Helpers.isPackageInstalled(WEATHER_SERVICE_PACKAGE, pm))) {
                prefSet.removePreference(mWeatherCategory);
            }
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

            if (preference == mTileAnimationStyle) {
                int tileAnimationStyle = Integer.valueOf((String) newValue);
                Settings.System.putIntForUser(mResolver, Settings.System.ANIM_TILE_STYLE,
                        tileAnimationStyle, UserHandle.USER_CURRENT);
                updateTileAnimationStyleSummary(tileAnimationStyle);
                updateAnimTileStyle(tileAnimationStyle);
                return true;
            } else if (preference == mTileAnimationDuration) {
                int tileAnimationDuration = Integer.valueOf((String) newValue);
                Settings.System.putIntForUser(mResolver, Settings.System.ANIM_TILE_DURATION,
                        tileAnimationDuration, UserHandle.USER_CURRENT);
                updateTileAnimationDurationSummary(tileAnimationDuration);
                return true;
            } else if (preference == mTileAnimationInterpolator) {
                int tileAnimationInterpolator = Integer.valueOf((String) newValue);
                Settings.System.putIntForUser(mResolver, Settings.System.ANIM_TILE_INTERPOLATOR,
                        tileAnimationInterpolator, UserHandle.USER_CURRENT);
                updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
                return true;
            } else if (preference == mSysuiQqsCount) {
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
            } else if (preference == mSmartPulldown) {
                int smartPulldown = Integer.valueOf((String) newValue);
                Settings.System.putInt(mResolver,
                        Settings.System.QS_SMART_PULLDOWN, smartPulldown);
                updateSmartPulldownSummary(smartPulldown);
                return true;
            } else if (preference == mQuickPulldown) {
                int quickPulldown = Integer.valueOf((String) newValue);
                CMSettings.System.putInt(mResolver,
                        CMSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, quickPulldown);
                updatePulldownSummary(quickPulldown);
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
                return true;
            } else if (preference == mNotificationKill) {
                // Setting will only apply to new created notifications.
                // By restarting SystemUI, we can re-create all notifications
                Helpers.showSystemUIrestartDialog(getActivity());
                return true;
            }
            return false;
        }

        private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
            String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                    .valueOf(tileAnimationStyle))];
            mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
        }

        private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
            String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                    .valueOf(tileAnimationDuration))];
            mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
        }

        private void updateTileAnimationInterpolatorSummary(int tileAnimationInterpolator) {
            String prefix = (String) mTileAnimationInterpolator.getEntries()[mTileAnimationInterpolator.findIndexOfValue(String
                    .valueOf(tileAnimationInterpolator))];
            mTileAnimationInterpolator.setSummary(getResources().getString(R.string.qs_set_animation_interpolator, prefix));
        }

        private void updateAnimTileStyle(int tileAnimationStyle) {
            if (mTileAnimationDuration != null) {
                if (tileAnimationStyle == 0) {
                    mTileAnimationDuration.setSelectable(false);
                    mTileAnimationInterpolator.setSelectable(false);
                } else {
                    mTileAnimationDuration.setSelectable(true);
                    mTileAnimationInterpolator.setSelectable(true);
                }
            }
        }

        private void updateSmartPulldownSummary(int value) {
            Resources res = getResources();
            if (value == 0) {
                // Smart pulldown deactivated
                mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_off));
            } else if (value == 3) {
                mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_none_summary));
            } else {
                String type = res.getString(value == 1
                        ? R.string.smart_pulldown_dismissable
                        : R.string.smart_pulldown_ongoing);
                mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_summary, type));
            }
        }

        private void updatePulldownSummary(int value) {
            Resources res = getResources();

            if (value == 0) {
                // quick pulldown deactivated
                mQuickPulldown.setSummary(res.getString(R.string.status_bar_quick_qs_pulldown_off));
            } else {
                String direction = res.getString(value == 2
                        ? R.string.status_bar_quick_qs_pulldown_summary_left
                        : R.string.status_bar_quick_qs_pulldown_summary_right);
                mQuickPulldown.setSummary(res.getString(R.string.status_bar_quick_qs_pulldown_summary, direction));
            }
        }

        private void getAvailableHeaderPacks(List<String> entries, List<String> values) {
            String defaultLabel = null;
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
                if (packageName.equals(DEFAULT_HEADER_PACKAGE)) {
                    defaultLabel = label;
                } else {
                    headerMap.put(label, packageName);
                }
            }
            i.setAction("org.omnirom.DaylightHeaderPack1");
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                String packageName = r.activityInfo.packageName;
                String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
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
            entries.add(0, defaultLabel);
            values.add(0, DEFAULT_HEADER_PACKAGE);
        }

        private boolean isBrowseHeaderAvailable() {
            PackageManager pm = getActivity().getPackageManager();
            Intent browse = new Intent();
            browse.setClassName("org.omnirom.omnistyle", "org.omnirom.omnistyle.BrowseHeaderActivity");
            return pm.resolveActivity(browse, 0) != null;
        }
    }
}
