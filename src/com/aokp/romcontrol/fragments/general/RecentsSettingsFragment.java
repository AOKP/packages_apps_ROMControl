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
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.aokp.romcontrol.R;

import java.util.List;
import java.util.ArrayList;

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

        private static final String IMMERSIVE_RECENTS = "immersive_recents";
        private static final String SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents";
        private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";

        private static final String FAB_COLOR = "fab_button_color";
        private static final String RECENTS_STYLE = "clear_recents_style";
        private static final String MEMBAR_COLOR = "mem_bar_color";
        private static final String MEM_TEXT_COLOR = "mem_text_color";
        private static final String CLEAR_BUTTON_COLOR = "clear_button_color";
        private static final String FAB_ANIM_STYLE = "fab_animation_style";

        static final int DEFAULT = 0xffffffff;
        static final int DEFAULT_BG_ICON = 0xff4285f4;
        static final int DEFAULT_BG_MEM_BAR = 0xff009688;
        static final int DEFAULT_BG_FAB = 0xff21272b;
        private static final int MENU_RESET = Menu.FIRST;

        private ColorPickerPreference mMemTextColor;
        private ColorPickerPreference mMemBarColor;
        private ColorPickerPreference mClearButtonColor;
        private ColorPickerPreference mfabColor;

        private ListPreference mImmersiveRecents;
        private SwitchPreference mRecentsClearAll;
        private ListPreference mRecentsClearAllLocation;

        private ListPreference mClearStyle;
        private ListPreference mFabanimation;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_recents_settings);

            ContentResolver resolver = getActivity().getContentResolver();
            final PreferenceScreen prefScreen = getPreferenceScreen();
            final Resources res = getResources();

            mImmersiveRecents = (ListPreference) findPreference(IMMERSIVE_RECENTS);
            mImmersiveRecents.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.IMMERSIVE_RECENTS, 0)));
            mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
            mImmersiveRecents.setOnPreferenceChangeListener(this);

            mRecentsClearAll = (SwitchPreference) prefScreen.findPreference(SHOW_CLEAR_ALL_RECENTS);
            mRecentsClearAll.setChecked(Settings.System.getIntForUser(resolver,
                    Settings.System.SHOW_CLEAR_ALL_RECENTS, 1, UserHandle.USER_CURRENT) == 1);
            mRecentsClearAll.setOnPreferenceChangeListener(this);

            mRecentsClearAllLocation = (ListPreference) prefScreen.findPreference(RECENTS_CLEAR_ALL_LOCATION);
            int location = Settings.System.getIntForUser(resolver,
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setValue(String.valueOf(location));
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
            mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

            int intColor;
            String hexColor;

            mfabColor = (ColorPickerPreference) prefScreen.findPreference(FAB_COLOR);
            mfabColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                        Settings.System.FAB_BUTTON_COLOR, DEFAULT_BG_FAB);
            hexColor = String.format("#%08x", (0xff21272b & intColor));
            mfabColor.setSummary(hexColor);
            mfabColor.setNewPreviewColor(intColor);

            mClearStyle = (ListPreference) prefScreen.findPreference(RECENTS_STYLE);
            mClearStyle.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.CLEAR_RECENTS_STYLE, 0)));
            mClearStyle.setSummary(mClearStyle.getEntry());
            mClearStyle.setOnPreferenceChangeListener(this);

            mFabanimation = (ListPreference) prefScreen.findPreference(FAB_ANIM_STYLE);
            mFabanimation.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.FAB_ANIMATION_STYLE, 0)));
            mFabanimation.setSummary(mFabanimation.getEntry());
            mFabanimation.setOnPreferenceChangeListener(this);

            mMemTextColor = (ColorPickerPreference) prefScreen.findPreference(MEM_TEXT_COLOR);
            mMemTextColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                        Settings.System.MEM_TEXT_COLOR, DEFAULT);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mMemTextColor.setSummary(hexColor);
            mMemTextColor.setNewPreviewColor(intColor);

            mMemBarColor= (ColorPickerPreference) prefScreen.findPreference(MEMBAR_COLOR);
            mMemBarColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                        Settings.System.MEM_BAR_COLOR, DEFAULT_BG_MEM_BAR);
            hexColor = String.format("#%08x", (0xff009688 & intColor));
            mMemBarColor.setSummary(hexColor);
            mMemBarColor.setNewPreviewColor(intColor);

            mClearButtonColor= (ColorPickerPreference) prefScreen.findPreference(CLEAR_BUTTON_COLOR);
            mClearButtonColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                        Settings.System.CLEAR_BUTTON_COLOR,
                        getResources().getColor(R.color.floating_action_button_touch_tint));
            hexColor = String.format("#%08x", (0xff4285f4 & intColor));
            mClearButtonColor.setSummary(hexColor);
            mClearButtonColor.setNewPreviewColor(intColor);

            return prefScreen;
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) 	  {
            ContentResolver resolver = getActivity().getContentResolver();

            if (preference == mImmersiveRecents) {
                Settings.System.putInt(resolver, Settings.System.IMMERSIVE_RECENTS,
                        Integer.valueOf((String) newValue));
                mImmersiveRecents.setValue(String.valueOf(newValue));
                mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
                return true;
            } else if (preference == mRecentsClearAll) {
                boolean show = (Boolean) newValue;
                Settings.System.putIntForUser(resolver, Settings.System.SHOW_CLEAR_ALL_RECENTS,
                        show ? 1 : 0, UserHandle.USER_CURRENT);
                return true;
            } else if (preference == mRecentsClearAllLocation) {
                int location = Integer.valueOf((String) newValue);
                int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION,
                        location, UserHandle.USER_CURRENT);
                mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
                return true;
            } else if (preference == mClearStyle) {
                Settings.System.putInt(resolver, Settings.System.CLEAR_RECENTS_STYLE,
                        Integer.valueOf((String) newValue));
                mClearStyle.setValue(String.valueOf(newValue));
                mClearStyle.setSummary(mClearStyle.getEntry());
                return true;
            } else if (preference == mfabColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.FAB_BUTTON_COLOR, intHex);
                return true;
            } else if (preference == mMemTextColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.MEM_TEXT_COLOR, intHex);
                return true;
            } else if (preference == mMemBarColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.MEM_BAR_COLOR, intHex);
                return true;
            } else if (preference == mClearButtonColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.CLEAR_BUTTON_COLOR, intHex);
                return true;
            } else if (preference == mFabanimation) {
                Settings.System.putInt(resolver, Settings.System.FAB_ANIMATION_STYLE,
                        Integer.valueOf((String) newValue));
                mFabanimation.setValue(String.valueOf(newValue));
                mFabanimation.setSummary(mFabanimation.getEntry());
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
            alertDialog.setTitle(R.string.reset);
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
                    Settings.System.FAB_BUTTON_COLOR, DEFAULT_BG_FAB);
            mfabColor.setNewPreviewColor(DEFAULT_BG_FAB);
            mfabColor.setSummary(R.string.default_string);
            Settings.System.putInt(resolver,
                    Settings.System.FAB_BUTTON_COLOR, DEFAULT_BG_FAB);
            mMemTextColor.setNewPreviewColor(DEFAULT);
            mMemTextColor.setSummary(R.string.default_string);
            Settings.System.putInt(resolver,
                    Settings.System.MEM_BAR_COLOR,DEFAULT);
            mMemBarColor.setNewPreviewColor(DEFAULT_BG_MEM_BAR);
            mMemBarColor.setSummary(R.string.default_string);
            Settings.System.putInt(resolver,
                    Settings.System.MEM_BAR_COLOR, DEFAULT_BG_MEM_BAR);
            mClearButtonColor.setNewPreviewColor(DEFAULT);
            mClearButtonColor.setSummary(R.string.default_string);
            Settings.System.putInt(resolver,
                    Settings.System.CLEAR_BUTTON_COLOR, DEFAULT);
        }
    }
}
