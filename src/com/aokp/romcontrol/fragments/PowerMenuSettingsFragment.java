/*
* Copyright (C) 2015 The Android Open Kang Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
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
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.util.cm.PowerMenuConstants;

import cyanogenmod.providers.CMSettings;

import org.cyanogenmod.internal.logging.CMMetricsLogger;

import static com.android.internal.util.cm.PowerMenuConstants.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import com.aokp.romcontrol.R;

public class PowerMenuSettingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment {
        public SettingsPreferenceFragment() {
        }

        final static String TAG = "PowerMenuActions";

        private ContentResolver mContentResolver;

        private SwitchPreference mRebootPref;
        private SwitchPreference mScreenshotPref;
        private SwitchPreference mScreenrecordPref;
        private SwitchPreference mAirplanePref;
        private SwitchPreference mUsersPref;
        private SwitchPreference mSettingsPref;
        private SwitchPreference mLockdownPref;
        private SwitchPreference mBugReportPref;
        private SwitchPreference mSilentPref;
        private SwitchPreference mVoiceAssistPref;
        private SwitchPreference mAssistPref;

        Context mContext;
        private ArrayList<String> mLocalUserConfig = new ArrayList<String>();
        private String[] mAvailableActions;
        private String[] mAllActions;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_powermenu_settings);

            mContext = getActivity().getApplicationContext();

            mAvailableActions = getActivity().getResources().getStringArray(
                    R.array.power_menu_actions_array);
            mAllActions = PowerMenuConstants.getAllActions();

            for (String action : mAllActions) {
            // Remove preferences not present in the overlay
                if (!isActionAllowed(action)) {
                    getPreferenceScreen().removePreference(findPreference(action));
                    continue;
                }

                if (action.equals(GLOBAL_ACTION_KEY_REBOOT)) {
                    mRebootPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_REBOOT);
                } else if (action.equals(GLOBAL_ACTION_KEY_SCREENSHOT)) {
                    mScreenshotPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_SCREENSHOT);
                } else if (action.equals(GLOBAL_ACTION_KEY_SCREENRECORD)) {
                    mScreenrecordPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_SCREENRECORD);
                } else if (action.equals(GLOBAL_ACTION_KEY_AIRPLANE)) {
                    mAirplanePref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_AIRPLANE);
                } else if (action.equals(GLOBAL_ACTION_KEY_USERS)) {
                    mUsersPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_USERS);
                } else if (action.equals(GLOBAL_ACTION_KEY_SETTINGS)) {
                    mSettingsPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_SETTINGS);
                } else if (action.equals(GLOBAL_ACTION_KEY_LOCKDOWN)) {
                    mLockdownPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_LOCKDOWN);
                } else if (action.equals(GLOBAL_ACTION_KEY_BUGREPORT)) {
                    mBugReportPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_BUGREPORT);
                } else if (action.equals(GLOBAL_ACTION_KEY_SILENT)) {
                    mSilentPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_SILENT);
                } else if (action.equals(GLOBAL_ACTION_KEY_VOICEASSIST)) {
                    mSilentPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_VOICEASSIST);
                } else if (action.equals(GLOBAL_ACTION_KEY_ASSIST)) {
                    mSilentPref = (SwitchPreference) findPreference(GLOBAL_ACTION_KEY_ASSIST);
                }
            }

            getUserConfig();
        }

        protected ContentResolver getContentResolver() {
            Context context = getActivity();
            if (context != null) {
                mContentResolver = context.getContentResolver();
            }
            return mContentResolver;
        }

        @Override
        public void onStart() {
            super.onStart();

            if (mRebootPref != null) {
                mRebootPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_REBOOT));
            }

            if (mScreenshotPref != null) {
                mScreenshotPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SCREENSHOT));
            }

            if (mScreenrecordPref != null) {
                mScreenrecordPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SCREENRECORD));
            }

            if (mAirplanePref != null) {
                mAirplanePref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_AIRPLANE));
            }

            if (mUsersPref != null) {
                if (!UserHandle.MU_ENABLED || !UserManager.supportsMultipleUsers()) {
                    getPreferenceScreen().removePreference(findPreference(GLOBAL_ACTION_KEY_USERS));
                    mUsersPref = null;
                } else {
                    List<UserInfo> users = ((UserManager) mContext.getSystemService(
                            Context.USER_SERVICE)).getUsers();
                    boolean enabled = (users.size() > 1);
                    mUsersPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_USERS) && enabled);
                    mUsersPref.setEnabled(enabled);
                }
            }

            if (mSettingsPref != null) {
                mSettingsPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SETTINGS));
            }

            if (mLockdownPref != null) {
                mLockdownPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_LOCKDOWN));
            }

            if (mBugReportPref != null) {
                mBugReportPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_BUGREPORT));
            }

            if (mSilentPref != null) {
                mSilentPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SILENT));
            }

            if (mVoiceAssistPref != null) {
                mVoiceAssistPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_VOICEASSIST));
            }

            if (mAssistPref != null) {
                mAssistPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_ASSIST));
            }

            updatePreferences();
        }

        @Override
        public void onResume() {
            super.onResume();
            updatePreferences();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            boolean value;

            if (preference == mRebootPref) {
                value = mRebootPref.isChecked();
                updateUserConfig(value, GLOBAL_ACTION_KEY_REBOOT);

            } else if (preference == mScreenshotPref) {
                value = mScreenshotPref.isChecked();
                updateUserConfig(value, GLOBAL_ACTION_KEY_SCREENSHOT);

            } else if (preference == mScreenrecordPref) {
                value = mScreenrecordPref.isChecked();
                updateUserConfig(value, GLOBAL_ACTION_KEY_SCREENRECORD);

            } else if (preference == mAirplanePref) {
                value = mAirplanePref.isChecked();
                updateUserConfig(value, GLOBAL_ACTION_KEY_AIRPLANE);

            } else if (preference == mUsersPref) {
                value = mUsersPref.isChecked();
                updateUserConfig(value, GLOBAL_ACTION_KEY_USERS);

            } else if (preference == mSettingsPref) {
                value = mSettingsPref.isChecked();
                updateUserConfig(value, GLOBAL_ACTION_KEY_SETTINGS);

            } else if (preference == mLockdownPref) {
                value = mLockdownPref.isChecked();
                updateUserConfig(value, GLOBAL_ACTION_KEY_LOCKDOWN);

            } else if (preference == mBugReportPref) {
                value = mBugReportPref.isChecked();
                updateUserConfig(value, GLOBAL_ACTION_KEY_BUGREPORT);

            } else if (preference == mSilentPref) {
                value = mSilentPref.isChecked();
                updateUserConfig(value, GLOBAL_ACTION_KEY_SILENT);

            } else if (preference == mVoiceAssistPref) {
                value = mVoiceAssistPref.isChecked();
                updateUserConfig(value, GLOBAL_ACTION_KEY_VOICEASSIST);

            } else if (preference == mAssistPref) {
                value = mAssistPref.isChecked();
                updateUserConfig(value, GLOBAL_ACTION_KEY_ASSIST);

            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return true;
        }

        private boolean settingsArrayContains(String preference) {
            return mLocalUserConfig.contains(preference);
        }

        private boolean isActionAllowed(String action) {
            if (Arrays.asList(mAvailableActions).contains(action)) {
                return true;
            }
            return false;
        }

        private void updateUserConfig(boolean enabled, String action) {
            if (enabled) {
                if (!settingsArrayContains(action)) {
                    mLocalUserConfig.add(action);
                }
            } else {
                if (settingsArrayContains(action)) {
                    mLocalUserConfig.remove(action);
                }
            }
            saveUserConfig();
        }

        private void updatePreferences() {
            boolean bugreport = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.BUGREPORT_IN_POWER_MENU, 0) != 0;

            if (mBugReportPref != null) {
                mBugReportPref.setEnabled(bugreport);
                if (bugreport) {
                    mBugReportPref.setSummary(null);
                } else {
                    mBugReportPref.setSummary(R.string.power_menu_bug_report_disabled);
                }
            }
        }

        private void getUserConfig() {
            mLocalUserConfig.clear();
            String[] defaultActions;
            String savedActions = CMSettings.Secure.getStringForUser(mContext.getContentResolver(),
                    CMSettings.Secure.POWER_MENU_ACTIONS, UserHandle.USER_CURRENT);

            if (savedActions == null) {
                defaultActions = mContext.getResources().getStringArray(
                        com.android.internal.R.array.config_globalActionsList);
                for (String action : defaultActions) {
                    mLocalUserConfig.add(action);
                }
            } else {
                for (String action : savedActions.split("\\|")) {
                    mLocalUserConfig.add(action);
                }
            }
        }

        private void saveUserConfig() {
            StringBuilder s = new StringBuilder();

            // TODO: Use DragSortListView
            ArrayList<String> setactions = new ArrayList<String>();
            for (String action : mAllActions) {
                if (settingsArrayContains(action) && isActionAllowed(action)) {
                    setactions.add(action);
                } else {
                    continue;
                }
            }

            for (int i = 0; i < setactions.size(); i++) {
                s.append(setactions.get(i).toString());
                if (i != setactions.size() - 1) {
                    s.append("|");
                }
            }

            CMSettings.Secure.putStringForUser(getContentResolver(),
                    CMSettings.Secure.POWER_MENU_ACTIONS, s.toString(), UserHandle.USER_CURRENT);
            updatePowerMenuDialog();
        }

        private void updatePowerMenuDialog() {
            Intent u = new Intent();
            u.setAction(Intent.UPDATE_POWER_MENU);
            mContext.sendBroadcastAsUser(u, UserHandle.ALL);
        }

        protected int getMetricsCategory() {
            return CMMetricsLogger.POWER_MENU_ACTIONS;
        }
    }
}
