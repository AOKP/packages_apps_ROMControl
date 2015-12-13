/*
 * Copyright (C) 2010-2015 ParanoidAndroid Project
 * Portions Copyright (C) 2015 Fusion & CyanideL Project
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
import android.content.res.Resources;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aokp.romcontrol.R;
import org.cyanogenmod.internal.logging.CMMetricsLogger;

public class PieTargetsFragment extends Fragment {

    public PieTargetsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pie_targets_main, container, false);

        Resources res = getResources();

        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.pie_targets_main, new PieTargetsPreferenceFragment())
                .commit();

        return v;
    }

    public static class PieTargetsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public PieTargetsPreferenceFragment() {

        }

        private static final String PA_PIE_MENU = "pa_pie_menu";
        private static final String PA_PIE_LASTAPP = "pa_pie_lastapp";
        private static final String PA_PIE_KILLTASK = "pa_pie_killtask";
        private static final String PA_PIE_NOTIFICATIONS = "pa_pie_notifications";
        private static final String PA_PIE_SETTINGS_PANEL = "pa_pie_settings_panel";
        private static final String PA_PIE_SCREENSHOT = "pa_pie_screenshot";

        private SwitchPreference mPieMenu;
        private SwitchPreference mPieLastApp;
        private SwitchPreference mPieKillTask;
        private SwitchPreference mPieNotifications;
        private SwitchPreference mPieQsPanel;
        private SwitchPreference mPieScreenshot;

        private ContentResolver mResolver;

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        protected int getMetricsCategory() {
            return CMMetricsLogger.DONT_LOG;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_pa_pie_targets);

            PreferenceScreen prefSet = getPreferenceScreen();

            Context context = getActivity();
            mResolver = context.getContentResolver();

            mPieMenu = (SwitchPreference) prefSet.findPreference(PA_PIE_MENU);
            mPieMenu.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.PA_PIE_MENU, 0) != 0);

            mPieLastApp = (SwitchPreference) prefSet.findPreference(PA_PIE_LASTAPP);
            mPieLastApp.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.PA_PIE_LAST_APP, 0) != 0);

            mPieKillTask = (SwitchPreference) prefSet.findPreference(PA_PIE_KILLTASK);
            mPieKillTask.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.PA_PIE_KILL_TASK, 0) != 0);

            mPieNotifications = (SwitchPreference) prefSet.findPreference(PA_PIE_NOTIFICATIONS);
            mPieNotifications.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.PA_PIE_NOTIFICATIONS, 0) != 0);

            mPieQsPanel = (SwitchPreference) prefSet.findPreference(PA_PIE_SETTINGS_PANEL);
            mPieQsPanel.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.PA_PIE_SETTINGS_PANEL, 0) != 0);

            mPieScreenshot = (SwitchPreference) prefSet.findPreference(PA_PIE_SCREENSHOT);
            mPieScreenshot.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.PA_PIE_SCREENSHOT, 0) != 0);

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mPieMenu) {
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PA_PIE_MENU,
                    mPieMenu.isChecked() ? 1 : 0);
            } else if (preference == mPieLastApp) {
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PA_PIE_LAST_APP,
                    mPieLastApp.isChecked() ? 1 : 0);
            } else if (preference == mPieKillTask) {
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PA_PIE_KILL_TASK,
                    mPieKillTask.isChecked() ? 1 : 0);
            } else if (preference == mPieNotifications) {
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PA_PIE_NOTIFICATIONS,
                    mPieNotifications.isChecked() ? 1 : 0);
            } else if (preference == mPieQsPanel) {
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PA_PIE_SETTINGS_PANEL,
                    mPieQsPanel.isChecked() ? 1 : 0);
            } else if (preference == mPieScreenshot) {
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PA_PIE_SCREENSHOT,
                    mPieScreenshot.isChecked() ? 1 : 0);
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return false;
        }
    }
}
