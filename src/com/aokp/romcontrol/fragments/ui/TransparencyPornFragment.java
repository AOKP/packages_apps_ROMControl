/*
 * Copyright (c) 2016, The AICP Project
 * Copyright (c) 2016, The AOKP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.widgets.SeekBarPreferenceCham;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class TransparencyPornFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transparencyporn_settings_main, container, false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.transparencyporn_settings_main, new TransparencyPornSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class TransparencyPornSettingsPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        public TransparencyPornSettingsPreferenceFragment() {
        }

        private static final String TAG = "TransparencyPornSettingsPreferenceFragment";

        private ListPreference mVolumeDialogStroke;
        private Preference mVolumeDialogStrokeColor;
        private Preference mVolumeDialogStrokeThickness;
        private Preference mVolumeDialogDashWidth;
        private Preference mVolumeDialogDashGap;

        static final int DEFAULT_VOLUME_DIALOG_STROKE_COLOR = 0xFF4285F4;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_transparencyporn_settings);

            PreferenceScreen prefSet = getPreferenceScreen();
            final ContentResolver resolver = getActivity().getContentResolver();

            mVolumeDialogStroke =
                    (ListPreference) findPreference(Settings.System.VOLUME_DIALOG_STROKE);
            mVolumeDialogStroke.setOnPreferenceChangeListener(this);
            mVolumeDialogStrokeColor = findPreference(Settings.System.VOLUME_DIALOG_STROKE_COLOR);
            mVolumeDialogStrokeThickness =
                    findPreference(Settings.System.VOLUME_DIALOG_STROKE_THICKNESS);
            mVolumeDialogDashWidth = findPreference(Settings.System.VOLUME_DIALOG_STROKE_DASH_WIDTH);
            mVolumeDialogDashGap = findPreference(Settings.System.VOLUME_DIALOG_STROKE_DASH_GAP);
            updateVolumeDialogDependencies(mVolumeDialogStroke.getValue());
            return prefSet;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mVolumeDialogStroke) {
                updateVolumeDialogDependencies((String) newValue);
                return true;
            } else {
                return false;
            }
        }

        private void updateVolumeDialogDependencies(String volumeDialogStroke) {
            if (volumeDialogStroke.equals("0")) {
                mVolumeDialogStrokeColor.setEnabled(false);
                mVolumeDialogStrokeThickness.setEnabled(false);
                mVolumeDialogDashWidth.setEnabled(false);
                mVolumeDialogDashGap.setEnabled(false);
            } else if (volumeDialogStroke.equals("1")) {
                mVolumeDialogStrokeColor.setEnabled(false);
                mVolumeDialogStrokeThickness.setEnabled(true);
                mVolumeDialogDashWidth.setEnabled(true);
                mVolumeDialogDashGap.setEnabled(true);
            } else {
                mVolumeDialogStrokeColor.setEnabled(true);
                mVolumeDialogStrokeThickness.setEnabled(true);
                mVolumeDialogDashWidth.setEnabled(true);
                mVolumeDialogDashGap.setEnabled(true);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }
}