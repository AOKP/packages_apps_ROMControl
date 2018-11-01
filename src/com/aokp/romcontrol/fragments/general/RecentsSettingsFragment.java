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
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.aokp.romcontrol.R;
import com.android.internal.util.aokp.AOKPUtils;

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
		
		private static final String RECENTS_COMPONENT_TYPE = "recents_component";
		private ListPreference mRecentsComponentType;
		
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
				}
            return false;
        }
    }
}
