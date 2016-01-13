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

package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
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

import net.margaritov.preference.colorpicker.ColorPickerPreference;

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

    public static class NotificationsDrawerSettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public NotificationsDrawerSettingsPreferenceFragment() {

        }

        private static final String TAG = "NotificationsDrawer";

        private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";

        private static final String PREF_CAT_COLORS =
                "notification_cat_colors";
        private static final String PREF_MEDIA_BG_MODE =
                "notification_media_bg_mode";
        private static final String PREF_APP_ICON_BG_MODE =
                "notification_app_icon_bg_mode";
        private static final String PREF_APP_ICON_COLOR_MODE =
                "notification_app_icon_color_mode";
        private static final String PREF_BG_COLOR =
                "notification_bg_color";
        private static final String PREF_BG_GUTS_COLOR =
                "notification_bg_guts_color";
        private static final String PREF_APP_ICON_BG_COLOR =
                "notification_app_icon_bg_color";
        private static final String PREF_TEXT_COLOR =
                "notification_text_color";
        private static final String PREF_ICON_COLOR =
                "notification_icon_color";
        private static final String PREF_CLEAR_ALL_ICON_COLOR =
                "notification_drawer_clear_all_icon_color";

        private static final int CUSTOM_BLUE = 0xff1b1f23;
        private static final int SYSTEMUI_SECONDARY = 0xff384248;
        private static final int WHITE = 0xffffffff;
        private static final int BLACK = 0xff000000;
        private static final int HOLO_BLUE_LIGHT = 0xff33b5e5;
        private static final int TRANSLUCENT_HOLO_BLUE_LIGHT = 0x4d33b5e5;
        private static final int TRANSLUCENT_WHITE = 0x4dffffff;

        private static final int MENU_RESET = Menu.FIRST;
        private static final int DLG_RESET = 0;

        private ListPreference mQuickPulldown;

        private ListPreference mMediaBgMode;
        private ListPreference mAppIconBgMode;
        private ListPreference mAppIconColorMode;
        private ColorPickerPreference mBgColor;
        private ColorPickerPreference mBgGutsColor;
        private ColorPickerPreference mAppIconBgColor;
        private ColorPickerPreference mTextColor;
        private ColorPickerPreference mIconColor;
        private ColorPickerPreference mClearAllIconColor;

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
            if (prefSet != null) {
                prefSet.removeAll();
            }
            mQuickPulldown = (ListPreference) findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);

            int quickPulldown = CMSettings.System.getInt(mResolver,
                    CMSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 1);
            mQuickPulldown.setValue(String.valueOf(quickPulldown));
            updatePulldownSummary(quickPulldown);
            mQuickPulldown.setOnPreferenceChangeListener(this);

            int intColor;
            String hexColor;

            mMediaBgMode = (ListPreference) findPreference(PREF_MEDIA_BG_MODE);
            int mediaBgMode = Settings.System.getInt(mResolver,
                    Settings.System.NOTIFICATION_MEDIA_BG_MODE, 0);
            mMediaBgMode.setValue(String.valueOf(mediaBgMode));
            mMediaBgMode.setSummary(mMediaBgMode.getEntry());
            mMediaBgMode.setOnPreferenceChangeListener(this);

            mAppIconBgMode = (ListPreference) findPreference(PREF_APP_ICON_BG_MODE);
            int appIconBgMode = Settings.System.getInt(mResolver,
                    Settings.System.NOTIFICATION_APP_ICON_BG_MODE, 0);
            mAppIconBgMode.setValue(String.valueOf(appIconBgMode));
            mAppIconBgMode.setSummary(mAppIconBgMode.getEntry());
            mAppIconBgMode.setOnPreferenceChangeListener(this);

            mAppIconColorMode = (ListPreference) findPreference(PREF_APP_ICON_COLOR_MODE);
            int appIconColorMode = Settings.System.getInt(mResolver,
                    Settings.System.NOTIFICATION_APP_ICON_COLOR_MODE, 0);
            mAppIconColorMode.setValue(String.valueOf(appIconColorMode));
            mAppIconColorMode.setSummary(mAppIconColorMode.getEntry());
            mAppIconColorMode.setOnPreferenceChangeListener(this);

            mBgColor =
                    (ColorPickerPreference) findPreference(PREF_BG_COLOR);
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.NOTIFICATION_BG_COLOR, WHITE);
            mBgColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBgColor.setSummary(hexColor);
            mBgColor.setOnPreferenceChangeListener(this);

            mBgGutsColor =
                (ColorPickerPreference) findPreference(PREF_BG_GUTS_COLOR);
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.NOTIFICATION_GUTS_BG_COLOR, SYSTEMUI_SECONDARY);
            mBgGutsColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBgGutsColor.setSummary(hexColor);
            mBgGutsColor.setOnPreferenceChangeListener(this);

            PreferenceCategory colorCat =
                    (PreferenceCategory) findPreference(PREF_CAT_COLORS);
            mAppIconBgColor =
                    (ColorPickerPreference) findPreference(PREF_APP_ICON_BG_COLOR);
            if (appIconBgMode != 0) {
                intColor = Settings.System.getInt(mResolver,
                        Settings.System.NOTIFICATION_APP_ICON_BG_COLOR, TRANSLUCENT_WHITE);
                mAppIconBgColor.setNewPreviewColor(intColor);
                hexColor = String.format("#%08x", (0xffffffff & intColor));
                mAppIconBgColor.setSummary(hexColor);
                mAppIconBgColor.setOnPreferenceChangeListener(this);
            } else {
                colorCat.removePreference(mAppIconBgColor);
            }

            mTextColor =
                    (ColorPickerPreference) findPreference(PREF_TEXT_COLOR);
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.NOTIFICATION_TEXT_COLOR, BLACK);
            mTextColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mTextColor.setSummary(hexColor);
            mTextColor.setOnPreferenceChangeListener(this);

            mIconColor =
                    (ColorPickerPreference) findPreference(PREF_ICON_COLOR);
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.NOTIFICATION_ICON_COLOR, BLACK);
            mIconColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mIconColor.setSummary(hexColor);
            mIconColor.setOnPreferenceChangeListener(this);

            mClearAllIconColor =
                    (ColorPickerPreference) findPreference(PREF_CLEAR_ALL_ICON_COLOR);
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.NOTIFICATION_DRAWER_CLEAR_ALL_ICON_COLOR, WHITE);
            mClearAllIconColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mClearAllIconColor.setSummary(hexColor);
            mClearAllIconColor.setOnPreferenceChangeListener(this);

            setHasOptionsMenu(true);
            return prefSet;
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
                    showDialogInner(DLG_RESET);
                    return true;
                 default:
                    return super.onContextItemSelected(item);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean value;
            String hex;
            int intHex;

            if (preference == mMediaBgMode) {
                int mediaBgMode = Integer.valueOf((String) newValue);
                int index = mAppIconColorMode.findIndexOfValue((String) newValue);
                Settings.System.putInt(mResolver,
                    Settings.System.NOTIFICATION_MEDIA_BG_MODE, mediaBgMode);
                preference.setSummary(mMediaBgMode.getEntries()[index]);
                return true;
            } else if (preference == mAppIconBgMode) {
                int appIconBgMode = Integer.valueOf((String) newValue);
                int index = mAppIconBgMode.findIndexOfValue((String) newValue);
                Settings.System.putInt(mResolver,
                    Settings.System.NOTIFICATION_APP_ICON_BG_MODE, appIconBgMode);
                preference.setSummary(mAppIconBgMode.getEntries()[index]);
                createCustomView();
                return true;
            } else if (preference == mAppIconColorMode) {
                int appIconColorMode = Integer.valueOf((String) newValue);
                int index = mAppIconColorMode.findIndexOfValue((String) newValue);
                Settings.System.putInt(mResolver,
                    Settings.System.NOTIFICATION_APP_ICON_COLOR_MODE, appIconColorMode);
                preference.setSummary(mAppIconColorMode.getEntries()[index]);
                return true;
            } else if (preference == mBgColor) {
                hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(mResolver,
                    Settings.System.NOTIFICATION_BG_COLOR, intHex);
                preference.setSummary(hex);
                return true;
            } else if (preference == mBgGutsColor) {
                hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(mResolver,
                    Settings.System.NOTIFICATION_GUTS_BG_COLOR, intHex);
                preference.setSummary(hex);
                return true;
            } else if (preference == mAppIconBgColor) {
                hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(mResolver,
                    Settings.System.NOTIFICATION_APP_ICON_BG_COLOR, intHex);
                preference.setSummary(hex);
                return true;
            } else if (preference == mTextColor) {
                hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(mResolver,
                    Settings.System.NOTIFICATION_TEXT_COLOR, intHex);
                preference.setSummary(hex);
                return true;
            } else if (preference == mIconColor) {
                hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(mResolver,
                    Settings.System.NOTIFICATION_ICON_COLOR, intHex);
                preference.setSummary(hex);
                return true;
            } else if (preference == mClearAllIconColor) {
                hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(mResolver,
                    Settings.System.NOTIFICATION_DRAWER_CLEAR_ALL_ICON_COLOR, intHex);
                preference.setSummary(hex);
                return true;
            } else if (preference == mQuickPulldown) {
                int quickPulldown = Integer.valueOf((String) newValue);
                CMSettings.System.putInt(
                        mResolver, CMSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, quickPulldown);
                updatePulldownSummary(quickPulldown);
                return true;
            }
            return false;
        }

        private void showDialogInner(int id) {
            DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
            newFragment.setTargetFragment(this, 0);
            newFragment.show(getFragmentManager(), "dialog " + id);
        }

        public static class MyAlertDialogFragment extends DialogFragment {

            public static MyAlertDialogFragment newInstance(int id) {
                MyAlertDialogFragment frag = new MyAlertDialogFragment();
                    Bundle args = new Bundle();
                args.putInt("id", id);
                frag.setArguments(args);
                return frag;
            }

            NotificationsDrawerFragment.NotificationsDrawerSettingsPreferenceFragment getOwner() {
                return (NotificationsDrawerFragment.NotificationsDrawerSettingsPreferenceFragment) getTargetFragment();
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                int id = getArguments().getInt("id");
                switch (id) {
                    case DLG_RESET:
                        return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.reset)
                        .setMessage(R.string.dlg_reset_values_message)
                        .setNegativeButton(R.string.cancel, null)
                        .setNeutralButton(R.string.dlg_reset_android,
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_MEDIA_BG_MODE, 0);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_APP_ICON_BG_MODE, 0);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_APP_ICON_COLOR_MODE, 0);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_BG_COLOR, WHITE);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_GUTS_BG_COLOR,
                                            SYSTEMUI_SECONDARY);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_APP_ICON_BG_COLOR,
                                            TRANSLUCENT_WHITE);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_TEXT_COLOR, BLACK);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_ICON_COLOR, BLACK);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_DRAWER_CLEAR_ALL_ICON_COLOR,
                                            WHITE);
                                getOwner().createCustomView();
                            }
                        })
                        .setPositiveButton(R.string.dlg_reset_aokp,
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_MEDIA_BG_MODE, 1);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_APP_ICON_BG_MODE, 1);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_APP_ICON_COLOR_MODE, 1);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_BG_COLOR, WHITE);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_GUTS_BG_COLOR,
                                            SYSTEMUI_SECONDARY);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_APP_ICON_BG_COLOR,
                                            TRANSLUCENT_HOLO_BLUE_LIGHT);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_TEXT_COLOR,
                                            HOLO_BLUE_LIGHT);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_ICON_COLOR,
                                            HOLO_BLUE_LIGHT);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.NOTIFICATION_DRAWER_CLEAR_ALL_ICON_COLOR,
                                            HOLO_BLUE_LIGHT);
                                getOwner().createCustomView();
                            }
                        })
                        .create();
                }
                throw new IllegalArgumentException("unknown id " + id);
            }

            @Override
            public void onCancel(DialogInterface dialog) {

            }
        }

        private void updatePulldownSummary(int value) {
            Resources res = getResources();

            if (value == 0) {
                // quick pulldown deactivated
                mQuickPulldown.setSummary(res.getString(R.string.status_bar_quick_qs_pulldown_off));
            } else {
                String direction = res.getString(value == 2
                        ? R.string.status_bar_quick_qs_pulldown_summary_left
                        : R.string.status_bar_quick_qs_pulldown_summary_right);
                mQuickPulldown.setSummary(res.getString(R.string.status_bar_quick_qs_pulldown_summary, direction));
            }
        }
    }
}
