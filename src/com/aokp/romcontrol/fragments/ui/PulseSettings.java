/*
 * Copyright (C) 2018, AOKP
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

package com.aokp.romcontrol.fragments.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.content.ContentResolver;
import android.content.Context;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.aokp.romcontrol.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PulseSettings extends Fragment {

    public PulseSettings() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pulse_settings_main, container, false);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.pulse_settings_main, new SettingsPreferenceFragment())
                .commit();
    }


    public static class SettingsPreferenceFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

        public SettingsPreferenceFragment() {

        }
        
        private ContentResolver mContentResolver;

        
        private static final String KEY_PULSE_COLOR = "fling_pulse_color";

        private ColorPickerPreference mPulseColor;
        
        private boolean mCheckPreferences;
			

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }
        
        private PreferenceScreen createCustomView() {
            mCheckPreferences = false;
			// Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_pulse_settings);
            PreferenceScreen prefSet = getPreferenceScreen();
            final ContentResolver resolver = getActivity().getContentResolver();
            
            mPulseColor =
                (ColorPickerPreference) prefSet.findPreference(KEY_PULSE_COLOR);
            mPulseColor.setOnPreferenceChangeListener(this);
            int intColor = Settings.System.getInt(resolver,
                    Settings.System.FLING_PULSE_COLOR, 0xffffffff);
            String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mPulseColor.setSummary(hexColor);
            mPulseColor.setNewPreviewColor(intColor);
            
            return prefSet;
		}

		@Override
		public void onResume() {
			super.onResume();
		}

		@Override
		public void onPause() {
			super.onPause();
		}
		
		protected ContentResolver getContentResolver() {
            Context context = getActivity();
            if (context != null) {
                mContentResolver = context.getContentResolver();
            }
            return mContentResolver;
        }

		public boolean onPreferenceChange(Preference preference, Object objValue) {
			AlertDialog dialog;
            ContentResolver resolver = getActivity().getContentResolver();			
            
            if (preference == mPulseColor) {
                String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.FLING_PULSE_COLOR, intHex);
                return true;
            }
            return false;
		}
        
    }
}
