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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.WallpaperManager;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.content.res.Configuration;
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
import android.widget.Toast;

import cyanogenmod.providers.CMSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.SeekBarPreferenceCham;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockScreenSettingsFragment extends Fragment {

    public LockScreenSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public SettingsPreferenceFragment() {

        }

        private static final String TAG = "LockScreenSettings";

        public static final int IMAGE_PICK = 1;

        private static final String KEY_WALLPAPER_SET = "lockscreen_wallpaper_set";
        private static final String KEY_WALLPAPER_CLEAR = "lockscreen_wallpaper_clear";
        private static final String KEY_LOCKSCREEN_BLUR_RADIUS = "lockscreen_blur_radius";

        private static final String PREF_CAT_COLORS =
                "weather_cat_colors";
        private static final String PREF_SHOW_WEATHER =
                "weather_show_weather";
        private static final String PREF_SHOW_LOCATION =
                "weather_show_location";
        private static final String PREF_SHOW_TIMESTAMP =
                "weather_show_timestamp";
        private static final String PREF_CONDITION_ICON =
                "weather_condition_icon";
        private static final String PREF_COLORIZE_ALL_ICONS =
                "weather_colorize_all_icons";
        private static final String PREF_TEXT_COLOR =
                "weather_text_color";
        private static final String PREF_ICON_COLOR =
                "weather_icon_color";

        private Preference mSetWallpaper;
        private Preference mClearWallpaper;
        private SeekBarPreferenceCham mBlurRadius;

        private static final int MONOCHROME_ICON = 0;
        private static final int DEFAULT_COLOR = 0xffffffff;
        private static final int HOLO_BLUE_LIGHT = 0xff33b5e5;

        private static final int MENU_RESET = Menu.FIRST;
        private static final int DLG_RESET = 0;

        private SwitchPreference mShowWeather;
        private SwitchPreference mShowLocation;
        private SwitchPreference mShowTimestamp;
        private ListPreference mConditionIcon;
        private SwitchPreference mColorizeAllIcons;
        private ColorPickerPreference mTextColor;
        private ColorPickerPreference mIconColor;

        private ContentResolver mResolver;

        protected void removePreference(String key) {
            Preference pref = findPreference(key);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            refreshSettings();
        }

        public void refreshSettings() {
            PreferenceScreen prefs = getPreferenceScreen();
            if (prefs != null) {
                prefs.removeAll();
            }
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_lockscreen_settings);
            ContentResolver resolver = getActivity().getContentResolver();

            mSetWallpaper = (Preference) findPreference(KEY_WALLPAPER_SET);
            mClearWallpaper = (Preference) findPreference(KEY_WALLPAPER_CLEAR);

            mBlurRadius = (SeekBarPreferenceCham) findPreference(KEY_LOCKSCREEN_BLUR_RADIUS);
            mBlurRadius.setValue(Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_BLUR_RADIUS, 14));
            mBlurRadius.setOnPreferenceChangeListener(this);

            mResolver = getActivity().getContentResolver();

            boolean showWeather = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_WEATHER, 0) == 1;
            int conditionIcon = Settings.System.getInt(mResolver,
                   Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, MONOCHROME_ICON);
            boolean colorizeAllIcons = Settings.System.getInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_COLORIZE_ALL_ICONS, 0) == 1;

            int intColor;
            String hexColor;

            mShowWeather =
                    (SwitchPreference) findPreference(PREF_SHOW_WEATHER);
            mShowWeather.setChecked(showWeather);
            mShowWeather.setOnPreferenceChangeListener(this);

            PreferenceCategory catColors =
                    (PreferenceCategory) findPreference(PREF_CAT_COLORS);
            mTextColor =
                    (ColorPickerPreference) findPreference(PREF_TEXT_COLOR);
            mIconColor =
                    (ColorPickerPreference) findPreference(PREF_ICON_COLOR);

            if (showWeather) {
                mShowLocation =
                        (SwitchPreference) findPreference(PREF_SHOW_LOCATION);
                mShowLocation.setChecked(Settings.System.getInt(mResolver,
                        Settings.System.LOCK_SCREEN_SHOW_WEATHER_LOCATION, 1) == 1);
                mShowLocation.setOnPreferenceChangeListener(this);

                mShowTimestamp =
                        (SwitchPreference) findPreference(PREF_SHOW_TIMESTAMP);
                mShowTimestamp.setChecked(Settings.System.getInt(mResolver,
                        Settings.System.LOCK_SCREEN_SHOW_WEATHER_TIMESTAMP, 1) == 1);
                mShowTimestamp.setOnPreferenceChangeListener(this);

                mConditionIcon =
                        (ListPreference) findPreference(PREF_CONDITION_ICON);
                mConditionIcon.setValue(String.valueOf(conditionIcon));
                mConditionIcon.setSummary(mConditionIcon.getEntry());
                mConditionIcon.setOnPreferenceChangeListener(this);

                mColorizeAllIcons =
                        (SwitchPreference) findPreference(PREF_COLORIZE_ALL_ICONS);
                mColorizeAllIcons.setChecked(colorizeAllIcons);
                mColorizeAllIcons.setOnPreferenceChangeListener(this);

                intColor = Settings.System.getInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_TEXT_COLOR,
                        DEFAULT_COLOR);
                mTextColor.setNewPreviewColor(intColor);
                hexColor = String.format("#%08x", (0xffffffff & intColor));
                mTextColor.setSummary(hexColor);
                mTextColor.setOnPreferenceChangeListener(this);
            } else {
                removePreference(PREF_SHOW_LOCATION);
                removePreference(PREF_SHOW_TIMESTAMP);
                removePreference(PREF_CONDITION_ICON);
                removePreference(PREF_COLORIZE_ALL_ICONS);
                catColors.removePreference(mTextColor);
            }
            if (showWeather && ((conditionIcon == MONOCHROME_ICON)
                    || (conditionIcon != MONOCHROME_ICON && colorizeAllIcons))) {
                intColor = Settings.System.getInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR,
                        DEFAULT_COLOR);
                mIconColor.setNewPreviewColor(intColor);
                hexColor = String.format("#%08x", (0xffffffff & intColor));
                mIconColor.setSummary(hexColor);
                mIconColor.setOnPreferenceChangeListener(this);
            } else {
                catColors.removePreference(mIconColor);
                if (!showWeather) {
                    removePreference(PREF_CAT_COLORS);
                }
            }

            setHasOptionsMenu(true);
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
            ContentResolver resolver = getActivity().getApplicationContext().getContentResolver();
            if (preference == mBlurRadius) {
                int width = ((Integer)newValue).intValue();
                Settings.System.putInt(resolver,
                        Settings.System.LOCKSCREEN_BLUR_RADIUS, width);
                return true;
            } else if (preference == mShowWeather) {
                value = (Boolean) newValue;
                Settings.System.putInt(mResolver,
                      Settings.System.LOCK_SCREEN_SHOW_WEATHER,
                      value ? 1 : 0);
                refreshSettings();
                return true;
            } else if (preference == mShowLocation) {
                value = (Boolean) newValue;
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_SHOW_WEATHER_LOCATION,
                        value ? 1 : 0);
                return true;
            } else if (preference == mShowTimestamp) {
                value = (Boolean) newValue;
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_SHOW_WEATHER_TIMESTAMP,
                    value ? 1 : 0);
                return true;
            } else if (preference == mConditionIcon) {
                int intValue = Integer.valueOf((String) newValue);
                int index = mConditionIcon.findIndexOfValue((String) newValue);
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, intValue);
                mConditionIcon.setSummary(mConditionIcon.getEntries()[index]);
                refreshSettings();
                return true;
            } else if (preference == mColorizeAllIcons) {
                value = (Boolean) newValue;
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_COLORIZE_ALL_ICONS,
                        value ? 1 : 0);
                refreshSettings();
                return true;
            } else if (preference == mTextColor) {
                hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_TEXT_COLOR, intHex);
                preference.setSummary(hex);
                return true;
            } else if (preference == mIconColor) {
                hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
                intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR, intHex);
                preference.setSummary(hex);
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

            LockScreenSettingsFragment.SettingsPreferenceFragment getOwner() {
                return (LockScreenSettingsFragment.SettingsPreferenceFragment) getTargetFragment();
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
                                        Settings.System.LOCK_SCREEN_SHOW_WEATHER, 0);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.LOCK_SCREEN_SHOW_WEATHER_LOCATION, 1);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.LOCK_SCREEN_SHOW_WEATHER_TIMESTAMP, 1);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON,
                                        MONOCHROME_ICON);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.LOCK_SCREEN_WEATHER_COLORIZE_ALL_ICONS, 0);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.LOCK_SCREEN_WEATHER_TEXT_COLOR,
                                        DEFAULT_COLOR);
                                Settings.System.putInt(getOwner().mResolver,
                                        Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR,
                                        DEFAULT_COLOR);
                                getOwner().refreshSettings();
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

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mSetWallpaper) {
                setKeyguardWallpaper();
                return true;
            } else if (preference == mClearWallpaper) {
                clearKeyguardWallpaper();
                Toast.makeText(getView().getContext(), getString(R.string.reset_lockscreen_wallpaper),
                Toast.LENGTH_LONG).show();
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    Intent intent = new Intent();
                    intent.setClassName("com.android.wallpapercropper", "com.android.wallpapercropper.WallpaperCropActivity");
                    intent.putExtra("keyguardMode", "1");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        }

        private void setKeyguardWallpaper() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK);
        }

        private void clearKeyguardWallpaper() {
            WallpaperManager wallpaperManager = null;
            wallpaperManager = WallpaperManager.getInstance(getActivity());
            wallpaperManager.clearKeyguardWallpaper();
        }
    }
}
