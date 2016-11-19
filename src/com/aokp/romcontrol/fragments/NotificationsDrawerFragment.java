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

package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import cyanogenmod.providers.CMSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aokp.romcontrol.R;

public class NotificationsDrawerFragment extends Fragment {

    public NotificationsDrawerFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.container, new NotificationsDrawerSettingsPreferenceFragment())
                .commit();
    }

    public static class NotificationsDrawerSettingsPreferenceFragment extends PreferenceFragment {

        public NotificationsDrawerSettingsPreferenceFragment() {

        }

        private static final String PREF_QS_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";

        private SwitchPreference mBrightnessSlider;
        private ContentResolver mResolver;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_notificationsdrawer_settings);
            mResolver = getActivity().getContentResolver();
            PreferenceScreen prefSet = getPreferenceScreen();

            // Brightness slider
            mBrightnessSlider = (SwitchPreference) prefSet.findPreference(PREF_QS_SHOW_BRIGHTNESS_SLIDER);
            mBrightnessSlider.setChecked(Settings.System.getIntForUser(resolver,
                    Settings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) == 1);
            mBrightnessSlider.setOnPreferenceChangeListener(this);
            int brightnessSlider = Settings.System.getIntForUser(resolver,
                    Settings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT);
            updateBrightnessSliderSummary(brightnessSlider);

            setHasOptionsMenu(true);
            return prefSet;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mBrightnessSlider) {
                Settings.System.putIntForUser(resolver,
                        Settings.System.QS_SHOW_BRIGHTNESS_SLIDER,
                        (Boolean) newValue ? 1 : 0, UserHandle.USER_CURRENT);
                int brightnessSlider = Settings.System.getIntForUser(resolver,
                        Settings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1,
                        UserHandle.USER_CURRENT);
                updateBrightnessSliderSummary(brightnessSlider);
                return true;
            }
            return false;
        }

        private void updateBrightnessSliderSummary(int value) {
            String summary = value != 0
                    ? getResources().getString(R.string.qs_brightness_slider_enabled)
                    : getResources().getString(R.string.qs_brightness_slider_disabled);
            mBrightnessSlider.setSummary(summary);
        }
    }
}
