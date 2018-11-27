/*
 * Copyright (C) 2010-2015 ParanoidAndroid Project
 * Portions Copyright (C) 2015 Fusion & Cyanidel Project
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

package com.aokp.romcontrol.fragments.pie;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aokp.romcontrol.R;

public class PieControlFragment extends Fragment {

    public PieControlFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pie_settings_main, container, false);

        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.pie_settings_main, new PieControlPreferenceFragment())
                .commit();

        return v;
    }

    public static class PieControlPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public PieControlPreferenceFragment() {

        }

        private static final String KEY_PIE_BATTERY = "pie_battery_mode";
        private static final String KEY_PIE_THEME = "pie_theme_mode";
        private static final String KEY_PIE_STATUS = "pie_status_indicator";
        private static final String PA_PIE_GRAVITY = "pa_pie_gravity";

        private ListPreference mTheme;
        private ListPreference mBattery;
        private ListPreference mStatus;
        private ListPreference mPieGravity;

        private ContentResolver mResolver;

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_pa_pie_control);

            PreferenceScreen prefSet = getPreferenceScreen();

            Context context = getActivity();
            mResolver = context.getContentResolver();

            mTheme = (ListPreference) findPreference(KEY_PIE_THEME);
			if (mTheme != null) {
				int value = Settings.Secure.getInt(mResolver,
						Settings.Secure.PIE_THEME_MODE, 0);
				mTheme.setValue(String.valueOf(value));
				mTheme.setOnPreferenceChangeListener(this);
			}
			mBattery = (ListPreference) findPreference(KEY_PIE_BATTERY);
			if (mBattery != null) {
				int value = Settings.Secure.getInt(mResolver,
						Settings.Secure.PIE_BATTERY_MODE, 0);
				mBattery.setValue(String.valueOf(value));
				mBattery.setOnPreferenceChangeListener(this);
			}
			mStatus = (ListPreference) findPreference(KEY_PIE_STATUS);
			if (mStatus != null) {
				int value = Settings.Secure.getInt(mResolver,
						Settings.Secure.PIE_STATUS_INDICATOR, 0);
				mStatus.setValue(String.valueOf(value));
				mStatus.setOnPreferenceChangeListener(this);
			}
			mPieGravity = (ListPreference) findPreference(PA_PIE_GRAVITY);
			if (mPieGravity != null) {
				int pieGravity = Settings.Secure.getInt(mResolver,
						Settings.Secure.PIE_GRAVITY, 2);
				mPieGravity.setValue(String.valueOf(pieGravity));
				mPieGravity.setOnPreferenceChangeListener(this);
			}
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            int value = Integer.parseInt((String) newValue);
			if (preference == mBattery) {
				Settings.Secure.putInt(mResolver, Settings.Secure.PIE_BATTERY_MODE, value);
			}
			if (preference == mTheme) {
				Settings.Secure.putInt(mResolver, Settings.Secure.PIE_THEME_MODE, value);
			}
			if (preference == mStatus) {
				Settings.Secure.putInt(mResolver, Settings.Secure.PIE_STATUS_INDICATOR, value);
			} if (preference == mPieGravity) {
				Settings.Secure.putInt(mResolver, Settings.Secure.PIE_GRAVITY, value);
			}
			return true;
        }
    }
}
