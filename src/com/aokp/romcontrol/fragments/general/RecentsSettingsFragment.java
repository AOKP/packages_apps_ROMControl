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
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.preference.SlimSeekBarPreference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.android.internal.util.slim.DeviceUtils;

import com.aokp.romcontrol.R;

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

        private static final String SHOW_RECENTS_SEARCHBAR = "recents_show_search_bar";
        private static final String SHOW_FULLSCREEN_RECENTS = "recents_full_screen";
        private static final String SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents";
        private static final String RECENTS_DISMISS_ALL = "recents_clear_all_dismiss_all";
        private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";

        // Preferences
        private static final String USE_SLIM_RECENTS = "use_slim_recents";
        private static final String ONLY_SHOW_RUNNING_TASKS = "only_show_running_tasks";
        private static final String RECENTS_MAX_APPS = "max_apps";
        private static final String RECENT_PANEL_SHOW_TOPMOST =
                "recent_panel_show_topmost";
        private static final String RECENT_PANEL_LEFTY_MODE =
                "recent_panel_lefty_mode";
        private static final String RECENT_PANEL_SCALE =
                "recent_panel_scale";
        private static final String RECENT_PANEL_EXPANDED_MODE =
                "recent_panel_expanded_mode";
        private static final String RECENT_PANEL_BG_COLOR =
                "recent_panel_bg_color";
        private static final String RECENT_CARD_BG_COLOR =
            "recent_card_bg_color";
        private static final String RECENT_CARD_TEXT_COLOR =
            "recent_card_text_color";

        private SwitchPreference mRecentsSearchBar;
        private SwitchPreference mRecentsFullscreen;
        private SwitchPreference mRecentsClearAll;
        private SwitchPreference mRecentsDismissAll;
        private ListPreference mRecentsClearAllLocation;
        private SwitchPreference mUseSlimRecents;
        private SwitchPreference mShowRunningTasks;
        private SlimSeekBarPreference mMaxApps;
        private SwitchPreference mRecentsShowTopmost;
        private SwitchPreference mRecentPanelLeftyMode;
        private SlimSeekBarPreference mRecentPanelScale;
        private ListPreference mRecentPanelExpandedMode;
        private ColorPickerPreference mRecentPanelBgColor;
        private ColorPickerPreference mRecentCardBgColor;
        private ColorPickerPreference mRecentCardTextColor;

        private static final int MENU_RESET = Menu.FIRST;
        private static final int DEFAULT_BACKGROUND_COLOR = 0x00ffffff;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_recents_settings);

            PreferenceScreen prefSet = getPreferenceScreen();
            Activity activity = getActivity();
            ContentResolver resolver = getActivity().getContentResolver();

            mRecentsSearchBar = (SwitchPreference) prefSet.findPreference(SHOW_RECENTS_SEARCHBAR);
            mRecentsFullscreen = (SwitchPreference) prefSet.findPreference(SHOW_FULLSCREEN_RECENTS);
            mRecentsDismissAll = (SwitchPreference) prefSet.findPreference(RECENTS_DISMISS_ALL);

            mRecentsClearAll = (SwitchPreference) prefSet.findPreference(SHOW_CLEAR_ALL_RECENTS);
            mRecentsClearAll.setChecked(Settings.System.getIntForUser(resolver,
                    Settings.System.SHOW_CLEAR_ALL_RECENTS, 1, UserHandle.USER_CURRENT) == 1);
            mRecentsClearAll.setOnPreferenceChangeListener(this);

            mRecentsClearAllLocation = (ListPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_LOCATION);
            int location = Settings.System.getIntForUser(resolver,
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setValue(String.valueOf(location));
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
            mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

            mUseSlimRecents = (SwitchPreference) prefSet.findPreference(USE_SLIM_RECENTS);
            mUseSlimRecents.setChecked(Settings.System.getInt(resolver,
                    Settings.System.USE_SLIM_RECENTS, 0) == 1);
            mUseSlimRecents.setOnPreferenceChangeListener(this);

            updatePreference();
            return prefSet;
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
            updatePreference();
        }

        private void updatePreference() {
            boolean slimRecent = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.USE_SLIM_RECENTS, 0) == 1;

            if (slimRecent) {
                mRecentsSearchBar.setEnabled(false);
                mRecentsFullscreen.setEnabled(false);
                mRecentsClearAll.setEnabled(false);
                mRecentsDismissAll.setEnabled(false);
                mRecentsClearAllLocation.setEnabled(false);
                initializeAllPreferences();
                updateRecentPanelPreferences();
            } else {
                mRecentsSearchBar.setEnabled(true);
                mRecentsFullscreen.setEnabled(true);
                mRecentsClearAll.setEnabled(true);
                mRecentsDismissAll.setEnabled(true);
                mRecentsClearAllLocation.setEnabled(true);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
                ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mRecentsClearAll) {
                Settings.System.putInt(resolver, Settings.System.SHOW_CLEAR_ALL_RECENTS,
                        ((Boolean) newValue) ? 1 : 0);
                return true;
            } else if (preference == mRecentsClearAllLocation) {
                int location = Integer.valueOf((String) newValue);
                int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION,
                        location, UserHandle.USER_CURRENT);
                mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
                return true;
            } else if (preference == mUseSlimRecents) {
                Settings.System.putInt(resolver, Settings.System.USE_SLIM_RECENTS,
                        ((Boolean) newValue) ? 1 : 0);
                updatePreference();
                return true;
            } else if (preference == mShowRunningTasks) {
                Settings.System.putInt(resolver, Settings.System.RECENT_SHOW_RUNNING_TASKS,
                        ((Boolean) newValue) ? 1 : 0);
                return true;
            } else if (preference == mRecentPanelScale) {
                int value = Integer.parseInt((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.RECENT_PANEL_SCALE_FACTOR, value);
                return true;
            } else if (preference == mRecentPanelExpandedMode) {
                int value = Integer.parseInt((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.RECENT_PANEL_EXPANDED_MODE, value);
                return true;
            } else if (preference == mRecentPanelBgColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                if (hex.equals("#00ffffff")) {
                    preference.setSummary(R.string.default_string);
                } else {
                    preference.setSummary(hex);
                }
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.RECENT_PANEL_BG_COLOR,
                        intHex);
                return true;
            } else if (preference == mRecentCardBgColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                if (hex.equals("#00ffffff")) {
                    preference.setSummary(R.string.default_string);
                } else {
                    preference.setSummary(hex);
                }
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.RECENT_CARD_BG_COLOR,
                        intHex);
                return true;
            } else if (preference == mRecentCardTextColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                if (hex.equals("#00ffffff")) {
                    preference.setSummary(R.string.default_string);
                } else {
                    preference.setSummary(hex);
                }
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.RECENT_CARD_TEXT_COLOR,
                        intHex);
                return true;
            } else if (preference == mRecentPanelLeftyMode) {
                Settings.System.putInt(resolver,
                       Settings.System.RECENT_PANEL_GRAVITY,
                        ((Boolean) newValue) ? Gravity.LEFT : Gravity.RIGHT);
                return true;
            } else if (preference == mRecentsShowTopmost) {
                Settings.System.putInt(resolver,
                        Settings.System.RECENT_PANEL_SHOW_TOPMOST,
                        ((Boolean) newValue) ? 1 : 0);
                return true;
            } else if (preference == mMaxApps) {
                int value = Integer.parseInt((String) newValue);
                Settings.System.putInt(resolver,
                    Settings.System.RECENTS_MAX_APPS, value);
                return true;
            }
            return false;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            menu.add(0, MENU_RESET, 0, R.string.reset)
                    .setIcon(R.drawable.ic_settings_reset)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_RESET:
                    resetToDefault();
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }

        private void resetToDefault() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle(R.string.shortcut_action_reset);
            alertDialog.setMessage(R.string.reset_message);
            alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    resetValues();
                }
            });
            alertDialog.setNegativeButton(R.string.cancel, null);
            alertDialog.create().show();
        }

        private void resetValues() {
            ContentResolver resolver = getActivity().getContentResolver();
            Settings.System.putInt(resolver,
                    Settings.System.RECENT_PANEL_BG_COLOR, DEFAULT_BACKGROUND_COLOR);
            mRecentPanelBgColor.setNewPreviewColor(DEFAULT_BACKGROUND_COLOR);
            mRecentPanelBgColor.setSummary(R.string.default_string);
            Settings.System.putInt(resolver,
                    Settings.System.RECENT_CARD_BG_COLOR, DEFAULT_BACKGROUND_COLOR);
            mRecentCardBgColor.setNewPreviewColor(DEFAULT_BACKGROUND_COLOR);
            mRecentCardBgColor.setSummary(R.string.default_string);
            Settings.System.putInt(resolver,
                    Settings.System.RECENT_CARD_TEXT_COLOR, DEFAULT_BACKGROUND_COLOR);
            mRecentCardTextColor.setNewPreviewColor(DEFAULT_BACKGROUND_COLOR);
            mRecentCardTextColor.setSummary(R.string.default_string);
        }

        private void updateRecentPanelPreferences() {
            ContentResolver resolver = getActivity().getContentResolver();
            final boolean recentLeftyMode = Settings.System.getInt(resolver,
                    Settings.System.RECENT_PANEL_GRAVITY, Gravity.RIGHT) == Gravity.LEFT;
            mRecentPanelLeftyMode.setChecked(recentLeftyMode);

                final int recentScale = Settings.System.getInt(resolver,
                    Settings.System.RECENT_PANEL_SCALE_FACTOR, 100);
            mRecentPanelScale.setInitValue(recentScale - 60);

            final int recentExpandedMode = Settings.System.getInt(resolver,
                    Settings.System.RECENT_PANEL_EXPANDED_MODE, 0);
            mRecentPanelExpandedMode.setValue(recentExpandedMode + "");
        }

        private void initializeAllPreferences() {
            ContentResolver resolver = getActivity().getContentResolver();
            mUseSlimRecents = (SwitchPreference) findPreference(USE_SLIM_RECENTS);
            mUseSlimRecents.setOnPreferenceChangeListener(this);

            mShowRunningTasks = (SwitchPreference) findPreference(ONLY_SHOW_RUNNING_TASKS);
            mShowRunningTasks.setOnPreferenceChangeListener(this);

            mMaxApps = (SlimSeekBarPreference) findPreference(RECENTS_MAX_APPS);
            mMaxApps.setOnPreferenceChangeListener(this);
            mMaxApps.minimumValue(5);
            mMaxApps.setInitValue(Settings.System.getIntForUser(resolver,
                    Settings.System.RECENTS_MAX_APPS, ActivityManager.getMaxRecentTasksStatic(),
                    UserHandle.USER_CURRENT) - 5);
            mMaxApps.disablePercentageValue(true);

            // Recent panel background color
            mRecentPanelBgColor =
                    (ColorPickerPreference) findPreference(RECENT_PANEL_BG_COLOR);
            mRecentPanelBgColor.setOnPreferenceChangeListener(this);
            final int intColor = Settings.System.getInt(resolver,
                    Settings.System.RECENT_PANEL_BG_COLOR, 0x00ffffff);
            String hexColor = String.format("#%08x", (0x00ffffff & intColor));
            if (hexColor.equals("#00ffffff")) {
                mRecentPanelBgColor.setSummary(R.string.default_string);
            } else {
                mRecentPanelBgColor.setSummary(hexColor);
                }
            mRecentPanelBgColor.setNewPreviewColor(intColor);

            // Recent card background color
            mRecentCardBgColor =
                    (ColorPickerPreference) findPreference(RECENT_CARD_BG_COLOR);
            mRecentCardBgColor.setOnPreferenceChangeListener(this);
            final int intColorCard = Settings.System.getInt(resolver,
                    Settings.System.RECENT_CARD_BG_COLOR, 0x00ffffff);
            String hexColorCard = String.format("#%08x", (0x00ffffff & intColorCard));
            if (hexColorCard.equals("#00ffffff")) {
                mRecentCardBgColor.setSummary(R.string.default_string);
            } else {
                mRecentCardBgColor.setSummary(hexColorCard);
            }
            mRecentCardBgColor.setNewPreviewColor(intColorCard);

            // Recent card text color
            mRecentCardTextColor =
                    (ColorPickerPreference) findPreference(RECENT_CARD_TEXT_COLOR);
            mRecentCardTextColor.setOnPreferenceChangeListener(this);
            final int intColorText = Settings.System.getInt(resolver,
                    Settings.System.RECENT_CARD_TEXT_COLOR, 0x00ffffff);
            String hexColorText = String.format("#%08x", (0x00ffffff & intColorText));
            if (hexColorText.equals("#00ffffff")) {
                mRecentCardTextColor.setSummary(R.string.default_string);
            } else {
                mRecentCardTextColor.setSummary(hexColorText);
            }
            mRecentCardTextColor.setNewPreviewColor(intColorText);

            // Enable options menu for color reset
            setHasOptionsMenu(true);

            boolean enableRecentsShowTopmost = Settings.System.getInt(resolver,
                                          Settings.System.RECENT_PANEL_SHOW_TOPMOST, 0) == 1;
            mRecentsShowTopmost = (SwitchPreference) findPreference(RECENT_PANEL_SHOW_TOPMOST);
            mRecentsShowTopmost.setChecked(enableRecentsShowTopmost);
            mRecentsShowTopmost.setOnPreferenceChangeListener(this);

            mRecentPanelLeftyMode =
                    (SwitchPreference) findPreference(RECENT_PANEL_LEFTY_MODE);
            mRecentPanelLeftyMode.setOnPreferenceChangeListener(this);

            mRecentPanelScale =
                    (SlimSeekBarPreference) findPreference(RECENT_PANEL_SCALE);
            mRecentPanelScale.setInterval(5);
            mRecentPanelScale.setDefault(100);
            mRecentPanelScale.minimumValue(60);
            mRecentPanelScale.setOnPreferenceChangeListener(this);
            mRecentPanelScale.setInitValue(Settings.System.getInt(resolver,
                    Settings.System.RECENT_PANEL_SCALE_FACTOR, 100) - 60);

            mRecentPanelExpandedMode =
                    (ListPreference) findPreference(RECENT_PANEL_EXPANDED_MODE);
            mRecentPanelExpandedMode.setOnPreferenceChangeListener(this);
        }
    }
}
