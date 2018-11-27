/*
 * Copyright (C) 2018, The Android Open Kang Project
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
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.util.aokp.OmniSwitchConstants;

import com.aokp.romcontrol.R;
import com.android.internal.util.aokp.AOKPUtils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class RecentsSettingsFragment extends Fragment {

    public RecentsSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recents_settings_main, container, false);

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

        private static final String RECENTS_COMPONENT_TYPE = "recents_component";
        private static final String NAVIGATION_BAR_RECENTS_STYLE = "navbar_recents_style";
        private ListPreference mRecentsComponentType;
        private ListPreference mNavbarRecentsStyle;
        private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";
                private ListPreference mRecentsClearAllLocation;
                private SwitchPreference mRecentsClearAll;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_recents_settings);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            // recents component type
            mRecentsComponentType = (ListPreference) findPreference(RECENTS_COMPONENT_TYPE);
            int type = Settings.System.getInt(resolver,
                    Settings.System.RECENTS_COMPONENT, 0);
            mRecentsComponentType.setValue(String.valueOf(type));
            mRecentsComponentType.setSummary(mRecentsComponentType.getEntry());
            mRecentsComponentType.setOnPreferenceChangeListener(this);

            mNavbarRecentsStyle = (ListPreference) findPreference(NAVIGATION_BAR_RECENTS_STYLE);
            int recentsStyle = Settings.System.getInt(resolver,
                    Settings.System.OMNI_NAVIGATION_BAR_RECENTS, 0);
            mNavbarRecentsStyle.setValue(Integer.toString(recentsStyle));
            mNavbarRecentsStyle.setSummary(mNavbarRecentsStyle.getEntry());
            mNavbarRecentsStyle.setOnPreferenceChangeListener(this);

            // clear all recents
            mRecentsClearAllLocation = (ListPreference) findPreference(RECENTS_CLEAR_ALL_LOCATION);
                        int location = Settings.System.getIntForUser(resolver,
                        Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
                        mRecentsClearAllLocation.setValue(String.valueOf(location));
                        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
                        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

            return prefSet;
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

 @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
                ContentResolver resolver = getActivity().getContentResolver();
                if (preference == mRecentsComponentType) {
                int type = Integer.valueOf((String) newValue);
                int index = mRecentsComponentType.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.RECENTS_COMPONENT, type);
                mRecentsComponentType.setSummary(mRecentsComponentType.getEntries()[index]);
                if (type == 1) { // Disable swipe up gesture, if oreo type selected
                Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.SWIPE_UP_TO_SWITCH_APPS_ENABLED, 0);
                }
                AOKPUtils.showSystemUiRestartDialog(getContext());
                return true;
                } else if (preference == mNavbarRecentsStyle) {
                int value = Integer.valueOf((String) newValue);
                if (value == 1) {
                    if (!isOmniSwitchInstalled()){
                        doOmniSwitchUnavail();
                    } else if (!OmniSwitchConstants.isOmniSwitchRunning(getActivity())) {
                        doOmniSwitchConfig();
                    }
                }
                int index = mNavbarRecentsStyle.findIndexOfValue((String) newValue);
                mNavbarRecentsStyle.setSummary(mNavbarRecentsStyle.getEntries()[index]);
                Settings.System.putInt(resolver, Settings.System.OMNI_NAVIGATION_BAR_RECENTS, value);
                return true;
                }
         else if (preference == mRecentsClearAllLocation) {
             int location = Integer.valueOf((String) newValue);
             int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
             Settings.System.putIntForUser(getActivity().getContentResolver(),
                     Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
             mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
         return true;
         }
            return false;
        }


        private void doOmniSwitchConfig() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(R.string.omniswitch_title);
            alertDialogBuilder.setMessage(R.string.omniswitch_dialog_running_new)
                .setPositiveButton(R.string.omniswitch_settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        startActivity(OmniSwitchConstants.INTENT_LAUNCH_APP);
                    }
                });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        private void doOmniSwitchUnavail() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(R.string.omniswitch_title);
            alertDialogBuilder.setMessage(R.string.omniswitch_dialog_unavail);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        private boolean isOmniSwitchInstalled() {
            return AOKPUtils.isAvailableApp(OmniSwitchConstants.APP_PACKAGE_NAME, getActivity());
        }
    }
}
