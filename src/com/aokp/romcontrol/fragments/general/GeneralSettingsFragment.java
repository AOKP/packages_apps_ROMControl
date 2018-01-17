/*
 * Copyright (C) 2015 The Android Open Kang Project
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
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;

public class GeneralSettingsFragment extends Fragment {

    public GeneralSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_general_settings_main, container, false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.general_settings_main, new GeneralSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class GeneralSettingsPreferenceFragment extends PreferenceFragment {

        public GeneralSettingsPreferenceFragment() {

        }

        private static final String TAG = "GeneralSettingsPreferenceFragment";
        private static final String KEY_LOCKCLOCK = "lock_clock";
        // Package name of the cLock app
        public static final String LOCKCLOCK_PACKAGE_NAME = "com.cyanogenmod.lockclock";

        private Context mContext;
        private Preference mLockClock;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_general_settings);
            PreferenceScreen prefSet = getPreferenceScreen();
            mContext = getActivity().getApplicationContext();
            PackageManager pm = getActivity().getPackageManager();

            // cLock app check
            mLockClock = (Preference)
                    prefSet.findPreference(KEY_LOCKCLOCK);
            if (!Helpers.isPackageInstalled(LOCKCLOCK_PACKAGE_NAME, pm)) {
                prefSet.removePreference(mLockClock);
            }
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
    }
}
