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

package com.aokp.romcontrol.fragments.general;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.aokp.romcontrol.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class RecentsSettingsFragment extends Fragment {

    public RecentsSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recents_settings_main, container, false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.recents_settings_main, new RecentsSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class RecentsSettingsPreferenceFragment extends PreferenceFragment
           implements Preference.OnPreferenceChangeListener {

        public RecentsSettingsPreferenceFragment() {

        }

        private static final String IMMERSIVE_RECENTS = "immersive_recents";
        private static final String SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents";
        private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";

        private ListPreference mImmersiveRecents;
        private SwitchPreference mRecentsClearAll;
        private ListPreference mRecentsClearAllLocation;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_recents_settings);

            ContentResolver resolver = getActivity().getContentResolver();
            final PreferenceScreen prefScreen = getPreferenceScreen();
            final Resources res = getResources();

            mImmersiveRecents = (ListPreference) findPreference(IMMERSIVE_RECENTS);
            mImmersiveRecents.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.IMMERSIVE_RECENTS, 0)));
            mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
            mImmersiveRecents.setOnPreferenceChangeListener(this);

            mRecentsClearAll = (SwitchPreference) prefScreen.findPreference(SHOW_CLEAR_ALL_RECENTS);
            mRecentsClearAll.setChecked(Settings.System.getIntForUser(resolver,
                    Settings.System.SHOW_CLEAR_ALL_RECENTS, 1, UserHandle.USER_CURRENT) == 1);
            mRecentsClearAll.setOnPreferenceChangeListener(this);

            mRecentsClearAllLocation = (ListPreference) prefScreen.findPreference(RECENTS_CLEAR_ALL_LOCATION);
            int location = Settings.System.getIntForUser(resolver,
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setValue(String.valueOf(location));
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
            mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

            return prefScreen;
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) 	  {
            ContentResolver resolver = getActivity().getContentResolver();

            if (preference == mImmersiveRecents) {
                Settings.System.putInt(resolver, Settings.System.IMMERSIVE_RECENTS,
                        Integer.valueOf((String) newValue));
                mImmersiveRecents.setValue(String.valueOf(newValue));
                mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
                return true;
            } else if (preference == mRecentsClearAll) {
                boolean show = (Boolean) newValue;
                Settings.System.putIntForUser(resolver, Settings.System.SHOW_CLEAR_ALL_RECENTS,
                        show ? 1 : 0, UserHandle.USER_CURRENT);
                return true;
            } else if (preference == mRecentsClearAllLocation) {
                int location = Integer.valueOf((String) newValue);
                int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION,
                        location, UserHandle.USER_CURRENT);
                mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
                return true;
            }
            return false;
        }
    }
}
