/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.aokp.romcontrol.fragments.applauncher;

import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Camera;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;

import java.util.ArrayList;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.settings.AppMultiSelectListPreference;
import com.aokp.romcontrol.widgets.SeekBarPreference;

import java.io.File;
import java.lang.Thread;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;

public class AppCircleBarSettings extends Fragment {

    public AppCircleBarSettings() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_appcirclebar_main, container, false);

        Resources res = getResources();

        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.appcirclebar_main, new AppCircleSidebarSettingsPreferenceFragment())
                .commit();

        return v;
    }

    public static class AppCircleSidebarSettingsPreferenceFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

        public AppCircleSidebarSettingsPreferenceFragment() {

        }

        private static final String TAG = "AppCircleSidebarSettings";

        private static final String PREF_INCLUDE_APP_CIRCLE_BAR_KEY = "app_circle_bar_included_apps";
        private static final String KEY_TRIGGER_WIDTH = "trigger_width";
        private static final String KEY_TRIGGER_TOP = "trigger_top";
        private static final String KEY_TRIGGER_BOTTOM = "trigger_bottom";

        private AppMultiSelectListPreference mIncludedAppCircleBar;

        private SeekBarPreference mTriggerWidthPref;
        private SeekBarPreference mTriggerTopPref;
        private SeekBarPreference mTriggerBottomPref;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            ContentResolver resolver = getActivity().getContentResolver();
            Resources res = getResources();

            addPreferencesFromResource(R.xml.fragment_appcirclebar_settings);
            PreferenceScreen prefSet = getPreferenceScreen();

            mIncludedAppCircleBar = (AppMultiSelectListPreference) prefSet.findPreference(PREF_INCLUDE_APP_CIRCLE_BAR_KEY);
            Set<String> includedApps = getIncludedApps();
            if (includedApps != null) mIncludedAppCircleBar.setValues(includedApps);
            mIncludedAppCircleBar.setOnPreferenceChangeListener(this);
            mTriggerWidthPref = (SeekBarPreference) findPreference(KEY_TRIGGER_WIDTH);
            mTriggerWidthPref.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.APP_CIRCLE_BAR_TRIGGER_WIDTH, 10));
            mTriggerWidthPref.setOnPreferenceChangeListener(this);

            mTriggerTopPref = (SeekBarPreference) findPreference(KEY_TRIGGER_TOP);
            mTriggerTopPref.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.APP_CIRCLE_BAR_TRIGGER_TOP, 0));
            mTriggerTopPref.setOnPreferenceChangeListener(this);

            mTriggerBottomPref = (SeekBarPreference) findPreference(KEY_TRIGGER_BOTTOM);
            mTriggerBottomPref.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.APP_CIRCLE_BAR_TRIGGER_HEIGHT, 100));
            mTriggerBottomPref.setOnPreferenceChangeListener(this);
            return prefSet;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object objValue) {
            final String key = preference.getKey();
            if (preference == mIncludedAppCircleBar) {
                storeIncludedApps((Set<String>) objValue);
            } else if (preference == mTriggerWidthPref) {
                int width = ((Integer)objValue).intValue();
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.APP_CIRCLE_BAR_TRIGGER_WIDTH, width);
                return true;
            } else if (preference == mTriggerTopPref) {
                int top = ((Integer)objValue).intValue();
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.APP_CIRCLE_BAR_TRIGGER_TOP, top);
                return true;
            } else if (preference == mTriggerBottomPref) {
                int bottom = ((Integer)objValue).intValue();
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.APP_CIRCLE_BAR_TRIGGER_HEIGHT, bottom);
                return true;
            }

            return true;
        }

        private Set<String> getIncludedApps() {
            String included = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.WHITELIST_APP_CIRCLE_BAR);
            if (TextUtils.isEmpty(included)) {
                return null;
            }
            return new HashSet<String>(Arrays.asList(included.split("\\|")));
        }

        private void storeIncludedApps(Set<String> values) {
            StringBuilder builder = new StringBuilder();
            String delimiter = "";
            for (String value : values) {
                builder.append(delimiter);
                builder.append(value);
                delimiter = "|";
            }
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.WHITELIST_APP_CIRCLE_BAR, builder.toString());
        }

        @Override
        public void onPause() {
            super.onPause();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.APP_CIRCLE_BAR_SHOW_TRIGGER, 0);
        }

        @Override
        public void onResume() {
            super.onResume();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.APP_CIRCLE_BAR_SHOW_TRIGGER, 1);
        }
    }
}
