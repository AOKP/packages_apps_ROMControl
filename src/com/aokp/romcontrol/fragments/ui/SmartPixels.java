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
import android.app.Fragment;
import android.content.Context; 
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.PowerManager; 
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aokp.romcontrol.R;

import com.aokp.romcontrol.settings.SystemSettingSwitchPreference;
import com.aokp.romcontrol.settings.SystemSettingListPreference; 

import java.util.Arrays;

public class SmartPixels extends Fragment {

    public SmartPixels() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_smart_pixels_main, container, false);

        Resources res = getResources();

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.smart_pixels_main, new SettingsPreferenceFragment())
                .commit();
    }


    public static class SettingsPreferenceFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

        public SettingsPreferenceFragment() {

        }

			private static final String TAG = "SmartPixels";
			private static final String ON_POWER_SAVE = "smart_pixels_on_power_save";
			
			private static final String SMART_PIXELS_ENABLE = "smart_pixels_enable";
			private static final String SMART_PIXELS_ON_POWER_SAVE = "smart_pixels_on_power_save";
			private static final String SMART_PIXELS_PATTERN = "smart_pixels_pattern";
			private static final String SMART_PIXELS_SHIFT_TIMEOUT = "smart_pixels_shift_timeout";
			
			private SystemSettingSwitchPreference mSmartPixelsEnable;
			private SystemSettingSwitchPreference mSmartPixelsOnPowerSave;
			private SystemSettingListPreference mSmartPixelsPattern;
			private SystemSettingListPreference mSmartPixelsShiftTimeout;
			
			ContentResolver resolver;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_smart_pixels);

            PreferenceScreen prefs = getPreferenceScreen();

			resolver = getActivity().getContentResolver();
			
			mSmartPixelsEnable = (SystemSettingSwitchPreference) prefs.findPreference(SMART_PIXELS_ENABLE);
            mSmartPixelsOnPowerSave = (SystemSettingSwitchPreference) prefs.findPreference(SMART_PIXELS_ON_POWER_SAVE);
            mSmartPixelsPattern = (SystemSettingListPreference) prefs.findPreference(SMART_PIXELS_PATTERN);
            mSmartPixelsShiftTimeout = (SystemSettingListPreference) prefs.findPreference(SMART_PIXELS_SHIFT_TIMEOUT);
            boolean mSmartPixelsSupported = getResources().getBoolean(
                com.android.internal.R.bool.config_enableSmartPixels);
			if (!mSmartPixelsSupported){
			prefs.removePreference(mSmartPixelsEnable);
			prefs.removePreference(mSmartPixelsOnPowerSave);
			prefs.removePreference(mSmartPixelsPattern);
			prefs.removePreference(mSmartPixelsShiftTimeout);
			Toast toast = Toast.makeText(getActivity(), "Your device is not supported", Toast.LENGTH_SHORT);
			toast.show(); 
			}else{
			updateDependency();
			}

        }

		@Override
		public void onResume() {
			super.onResume();
		}

		@Override
		public void onPause() {
			super.onPause();
		}

		public boolean onPreferenceChange(Preference preference, Object objValue) {
			final String key = preference.getKey();
			updateDependency();
			return true;
		}
        
        private void updateDependency() {
        boolean mUseOnPowerSave = (Settings.System.getIntForUser(
                resolver, Settings.System.SMART_PIXELS_ON_POWER_SAVE,
                0, UserHandle.USER_CURRENT) == 1);
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        if (pm.isPowerSaveMode() && mUseOnPowerSave) {
            mSmartPixelsOnPowerSave.setEnabled(false);
        } else {
            mSmartPixelsOnPowerSave.setEnabled(true);
        }
    }
    }
}
