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

import com.aokp.romcontrol.widgets.SeekBarPreferenceCham; 
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
        
        private static final String KEY_CUSTOM_DIMEN = "pulse_custom_dimen";
        private static final String KEY_CUSTOM_DIV = "pulse_custom_div";
        private static final String KEY_FILLED_BLOCK_SIZE = "pulse_filled_block_size";
        private static final String KEY_EMPTY_BLOCK_SIZE = "pulse_empty_block_size";
        private static final String KEY_CUSTOM_FUDGE_FACTOR = "pulse_custom_fudge_factor";
        private static final String KEY_PULSE_LAVALAMP_SPEED = "fling_pulse_lavalamp_speed";
        
        private static final String KEY_PULSE_SOLID_UNITS_OPACITY = "pulse_solid_units_opacity";
        private static final String KEY_PULSE_SOLID_UNITS_COUNT = "pulse_solid_units_count";
        private static final String KEY_PULSE_SOLID_FUDGE_FACTOR = "pulse_solid_fudge_factor";
        private static final String KEY_LAVA_LAMP_SOLID_SPEED = "lava_lamp_solid_speed";

        private ColorPickerPreference mPulseColor;
        
        private SeekBarPreferenceCham mCustomDimen;
        private SeekBarPreferenceCham mCustomDiv;
        private SeekBarPreferenceCham mPulseFilledBlockSize;
        private SeekBarPreferenceCham mPulseEmptyBlockSize;
        private SeekBarPreferenceCham mPulseCustomFudgeFactor;
        private SeekBarPreferenceCham mFlingPulseLavalampSpeed;
        
        private SeekBarPreferenceCham mPulseSolidUnitsOpacity;
        private SeekBarPreferenceCham mPulseSolidUnitsCount;
        private SeekBarPreferenceCham mPulseSolidFudgeFactor;
        private SeekBarPreferenceCham mLavaLampSolidSpeed;
        
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
            
            mCustomDimen = (SeekBarPreferenceCham) findPreference(KEY_CUSTOM_DIMEN);
            int value1 = Settings.System.getInt(resolver,
				Settings.System.PULSE_CUSTOM_DIMEN, 14);
            mCustomDimen.setValue(value1);
            mCustomDimen.setOnPreferenceChangeListener(this);
            
            mCustomDiv = (SeekBarPreferenceCham) findPreference(KEY_CUSTOM_DIV);
            int value2 = Settings.System.getInt(resolver,
				Settings.System.PULSE_CUSTOM_DIV, 16);
            mCustomDiv.setValue(value2);
            mCustomDiv.setOnPreferenceChangeListener(this);
            
            mPulseFilledBlockSize = (SeekBarPreferenceCham) findPreference(KEY_FILLED_BLOCK_SIZE);
            int value3 = Settings.System.getInt(resolver,
				Settings.System.PULSE_FILLED_BLOCK_SIZE, 4);
            mPulseFilledBlockSize.setValue(value3);
            mPulseFilledBlockSize.setOnPreferenceChangeListener(this);
            
            mPulseEmptyBlockSize = (SeekBarPreferenceCham) findPreference(KEY_EMPTY_BLOCK_SIZE);
            int value4 = Settings.System.getInt(resolver,
				Settings.System.PULSE_EMPTY_BLOCK_SIZE, 1);
            mPulseEmptyBlockSize.setValue(value4);
            mPulseEmptyBlockSize.setOnPreferenceChangeListener(this);
            
            mPulseCustomFudgeFactor = (SeekBarPreferenceCham) findPreference(KEY_CUSTOM_FUDGE_FACTOR);
            int value5 = Settings.System.getInt(resolver,
				Settings.System.PULSE_CUSTOM_FUDGE_FACTOR, 4);
            mPulseCustomFudgeFactor.setValue(value5);
            mPulseCustomFudgeFactor.setOnPreferenceChangeListener(this);
            
            mFlingPulseLavalampSpeed = (SeekBarPreferenceCham) findPreference(KEY_PULSE_LAVALAMP_SPEED);
            int value6 = Settings.System.getInt(resolver,
				Settings.System.PULSE_LAVALAMP_SOLID_SPEED, 10000);
            mFlingPulseLavalampSpeed.setValue(value6);
            mFlingPulseLavalampSpeed.setOnPreferenceChangeListener(this);
            
            mPulseSolidUnitsOpacity = (SeekBarPreferenceCham) findPreference(KEY_PULSE_SOLID_UNITS_OPACITY);
            int value7 = Settings.System.getInt(resolver,
				Settings.System.PULSE_SOLID_UNITS_OPACITY, 200);
            mPulseSolidUnitsOpacity.setValue(value7);
            mPulseSolidUnitsOpacity.setOnPreferenceChangeListener(this);
            
            mPulseSolidUnitsCount = (SeekBarPreferenceCham) findPreference(KEY_PULSE_SOLID_UNITS_COUNT);
            int value8 = Settings.System.getInt(resolver,
				Settings.System.PULSE_SOLID_UNITS_COUNT, 64);
            mPulseSolidUnitsCount.setValue(value8);
            mPulseSolidUnitsCount.setOnPreferenceChangeListener(this);
            
            mPulseSolidFudgeFactor = (SeekBarPreferenceCham) findPreference(KEY_PULSE_SOLID_FUDGE_FACTOR);
            int value9 = Settings.System.getInt(resolver,
				Settings.System.PULSE_SOLID_FUDGE_FACTOR, 5);
            mPulseSolidFudgeFactor.setValue(value9);
            mPulseSolidFudgeFactor.setOnPreferenceChangeListener(this);
            
            mLavaLampSolidSpeed = (SeekBarPreferenceCham) findPreference(KEY_LAVA_LAMP_SOLID_SPEED);
            int value10 = Settings.System.getInt(resolver,
				Settings.System.PULSE_LAVALAMP_SOLID_SPEED, 10000);
            mLavaLampSolidSpeed.setValue(value10);
            mLavaLampSolidSpeed.setOnPreferenceChangeListener(this);
            
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
            }else if (preference == mCustomDimen) {
                int value = (Integer) objValue;
                Settings.System.putInt(resolver,
                        Settings.System.PULSE_CUSTOM_DIMEN, value);
                return true;
            }else if (preference == mCustomDiv) {
                int value = (Integer) objValue;
                Settings.System.putInt(resolver,
                        Settings.System.PULSE_CUSTOM_DIV, value);
                return true;
            }else if (preference == mPulseFilledBlockSize) {
                int value = (Integer) objValue;
                Settings.System.putInt(resolver,
                        Settings.System.PULSE_FILLED_BLOCK_SIZE, value);
                return true;
            }else if (preference == mPulseEmptyBlockSize) {
                int value = (Integer) objValue;
                Settings.System.putInt(resolver,
                        Settings.System.PULSE_EMPTY_BLOCK_SIZE, value);
                return true;
            }else if (preference == mPulseCustomFudgeFactor) {
                int value = (Integer) objValue;
                Settings.System.putInt(resolver,
                        Settings.System.PULSE_CUSTOM_FUDGE_FACTOR, value);
                return true;
            }else if (preference == mFlingPulseLavalampSpeed) {
                int value = (Integer) objValue;
                Settings.System.putInt(resolver,
                        Settings.System.PULSE_LAVALAMP_SOLID_SPEED, value);
                return true;
            }else if (preference == mPulseSolidUnitsOpacity) {
                int value = (Integer) objValue;
                Settings.System.putInt(resolver,
                        Settings.System.PULSE_SOLID_UNITS_OPACITY, value);
                return true;
            }else if (preference == mPulseSolidUnitsCount) {
                int value = (Integer) objValue;
                Settings.System.putInt(resolver,
                        Settings.System.PULSE_SOLID_UNITS_COUNT, value);
                return true;
            }else if (preference == mPulseSolidFudgeFactor) {
                int value = (Integer) objValue;
                Settings.System.putInt(resolver,
                        Settings.System.PULSE_SOLID_FUDGE_FACTOR, value);
                return true;
            }else if (preference == mLavaLampSolidSpeed) {
                int value = (Integer) objValue;
                Settings.System.putInt(resolver,
                        Settings.System.PULSE_LAVALAMP_SOLID_SPEED, value);
                return true;
            }
            return false;
		}
        
    }
}
