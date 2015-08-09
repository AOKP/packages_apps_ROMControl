/*
* Copyright (C) 2016 The Android Open Kang Project
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

package com.aokp.romcontrol.fragments.statusbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import cyanogenmod.providers.CMSettings;
import org.cyanogenmod.internal.logging.CMMetricsLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.SeekBarPreference;

public class ClockSettingsFragment extends Fragment {

    public ClockSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clock_settings_main, container, false);

        Resources res = getResources();

        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.clock_settings_main, new ClockSettingsPreferenceFragment())
                .commit();

        return v;
    }

    public static class ClockSettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public ClockSettingsPreferenceFragment() {

        }

        private static final String TAG = "ClockSettings";

        private ContentResolver mContentResolver;

        private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
        private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
        private static final String STATUS_BAR_DATE = "status_bar_date";
        private static final String STATUS_BAR_DATE_STYLE = "status_bar_date_style";
        private static final String STATUS_BAR_DATE_FORMAT = "status_bar_date_format";
        private static final String PREF_CLOCK_DATE_POSITION = "clock_date_position";
        private static final String PREF_COLOR_PICKER = "clock_color";
        private static final String PREF_FONT_STYLE = "font_style";
        private static final String PREF_STATUS_BAR_CLOCK_FONT_SIZE  = "status_bar_clock_font_size";

        public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
        public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
        private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 23;

        private static final int MENU_RESET = Menu.FIRST;
        private static final int DLG_RESET = 0;
        static final int DEFAULT_STATUS_CARRIER_COLOR = 0xffffffff;

        private ListPreference mStatusBarClock;
        private ListPreference mStatusBarAmPm;
        private ListPreference mStatusBarDate;
        private ListPreference mStatusBarDateStyle;
        private ListPreference mStatusBarDateFormat;
        private ListPreference mClockDatePosition;
        private ColorPickerPreference mColorPicker;
        private ListPreference mFontStyle;
        private ListPreference mStatusBarClockFontSize;

        private boolean mCheckPreferences;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            mCheckPreferences = false;
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_clock_settings);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            int intColorCarrierColor;
            String hexColorCarrierColor;

            PackageManager pm = getActivity().getPackageManager();
            Resources systemUiResources;
            try {
                systemUiResources = pm.getResourcesForApplication("com.android.systemui");
            } catch (Exception e) {
                Log.e(TAG, "can't access systemui resources",e);
                return null;
            }

            mStatusBarClock = (ListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);
            mStatusBarAmPm = (ListPreference) findPreference(STATUS_BAR_AM_PM);
            mStatusBarDate = (ListPreference) findPreference(STATUS_BAR_DATE);
            mStatusBarDateStyle = (ListPreference) findPreference(STATUS_BAR_DATE_STYLE);

            mClockDatePosition = (ListPreference) findPreference(PREF_CLOCK_DATE_POSITION);
            mClockDatePosition.setOnPreferenceChangeListener(this);
            mClockDatePosition.setValue(Integer.toString(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_POSITION,
                    0)));
            mClockDatePosition.setSummary(mClockDatePosition.getEntry());

            mStatusBarDateFormat = (ListPreference) findPreference(STATUS_BAR_DATE_FORMAT);
            int clockStyle = CMSettings.System.getInt(resolver,
                    CMSettings.System.STATUS_BAR_CLOCK, 1);
            mStatusBarClock.setValue(String.valueOf(clockStyle));
            mStatusBarClock.setSummary(mStatusBarClock.getEntry());
            mStatusBarClock.setOnPreferenceChangeListener(this);

            if (DateFormat.is24HourFormat(getActivity())) {
                mStatusBarAmPm.setEnabled(false);
                mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
            } else {
                int statusBarAmPm = CMSettings.System.getInt(resolver,
                        CMSettings.System.STATUS_BAR_AM_PM, 2);
                mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
                mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());
                mStatusBarAmPm.setOnPreferenceChangeListener(this);
            }

            int showDate = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_DATE, 0);
            mStatusBarDate.setValue(String.valueOf(showDate));
            mStatusBarDate.setSummary(mStatusBarDate.getEntry());
            mStatusBarDate.setOnPreferenceChangeListener(this);

            int dateStyle = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_DATE_STYLE, 0);
            mStatusBarDateStyle.setValue(String.valueOf(dateStyle));
            mStatusBarDateStyle.setSummary(mStatusBarDateStyle.getEntry());
            mStatusBarDateStyle.setOnPreferenceChangeListener(this);

            mStatusBarDateFormat.setOnPreferenceChangeListener(this);
            mStatusBarDateFormat.setSummary(mStatusBarDateFormat.getEntry());
            if (mStatusBarDateFormat.getValue() == null) {
                mStatusBarDateFormat.setValue("EEE");
            }

            parseClockDateFormats();

            mColorPicker = (ColorPickerPreference) findPreference(PREF_COLOR_PICKER);
            mColorPicker.setOnPreferenceChangeListener(this);
            int intColorClockColor = Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_COLOR, -2);
            if (intColorClockColor == -2) {
                intColorClockColor = systemUiResources.getColor(systemUiResources.getIdentifier(
                        "com.android.systemui:color/status_bar_clock_color", null, null));
                mColorPicker.setSummary(getResources().getString(R.string.default_string));
            } else {
                String hexColorClockColor = String.format("#%08x", (0xffffffff & intColorClockColor));
                mColorPicker.setSummary(hexColorClockColor);
            }
            mColorPicker.setNewPreviewColor(intColorClockColor);

            mFontStyle = (ListPreference) findPreference(PREF_FONT_STYLE);
            mFontStyle.setOnPreferenceChangeListener(this);
            mFontStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUSBAR_CLOCK_FONT_STYLE,
                    0)));
            mFontStyle.setSummary(mFontStyle.getEntry());

            mStatusBarClockFontSize = (ListPreference) findPreference(PREF_STATUS_BAR_CLOCK_FONT_SIZE);
            mStatusBarClockFontSize.setOnPreferenceChangeListener(this);
            mStatusBarClockFontSize.setValue(Integer.toString(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUSBAR_CLOCK_FONT_SIZE,
                    14)));
            mStatusBarClockFontSize.setSummary(mStatusBarClockFontSize.getEntry());

            setHasOptionsMenu(true);
            mCheckPreferences = true;
            return prefSet;
        }

        protected ContentResolver getContentResolver() {
            Context context = getActivity();
            if (context != null) {
                mContentResolver = context.getContentResolver();
            }
            return mContentResolver;
        }

        protected int getMetricsCategory() {
            // todo add a constant in MetricsLogger.java
            return CMMetricsLogger.DONT_LOG;
        }

        @Override
        public void onResume() {
            super.onResume();
            // Adjust clock position for RTL if necessary
            Configuration config = getResources().getConfiguration();
            if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                    mStatusBarClock.setEntries(getActivity().getResources().getStringArray(
                            R.array.status_bar_clock_style_entries_rtl));
                    mStatusBarClock.setSummary(mStatusBarClock.getEntry());
            }
        }

        @Override
        public void onPause() {
            super.onPause();
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
            if (!mCheckPreferences) {
                return false;
            }
            AlertDialog dialog;
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mStatusBarClock) {
                int clockStyle = Integer.parseInt((String) newValue);
                int index = mStatusBarClock.findIndexOfValue((String) newValue);
                CMSettings.System.putInt(
                        resolver, CMSettings.System.STATUS_BAR_CLOCK, clockStyle);
                mStatusBarClock.setSummary(mStatusBarClock.getEntries()[index]);
                return true;
            } else if (preference == mStatusBarAmPm) {
                int statusBarAmPm = Integer.valueOf((String) newValue);
                int index = mStatusBarAmPm.findIndexOfValue((String) newValue);
                CMSettings.System.putInt(
                        resolver, CMSettings.System.STATUS_BAR_AM_PM, statusBarAmPm);
                mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[index]);
                return true;
            } else if (preference == mStatusBarDate) {
                int statusBarDate = Integer.valueOf((String) newValue);
                int index = mStatusBarDate.findIndexOfValue((String) newValue);
                Settings.System.putInt(
                        resolver, STATUS_BAR_DATE, statusBarDate);
                mStatusBarDate.setSummary(mStatusBarDate.getEntries()[index]);
                return true;
            } else if (preference == mStatusBarDateStyle) {
                int statusBarDateStyle = Integer.parseInt((String) newValue);
                int index = mStatusBarDateStyle.findIndexOfValue((String) newValue);
                Settings.System.putInt(
                        resolver, STATUS_BAR_DATE_STYLE, statusBarDateStyle);
                mStatusBarDateStyle.setSummary(mStatusBarDateStyle.getEntries()[index]);
                return true;
            } else if (preference ==  mStatusBarDateFormat) {
                int index = mStatusBarDateFormat.findIndexOfValue((String) newValue);
                if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle(R.string.status_bar_date_string_edittext_title);
                    alert.setMessage(R.string.status_bar_date_string_edittext_summary);

                    final EditText input = new EditText(getActivity());
                    String oldText = Settings.System.getString(
                        getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_DATE_FORMAT);
                    if (oldText != null) {
                        input.setText(oldText);
                    }
                    alert.setView(input);

                    alert.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int whichButton) {
                            String value = input.getText().toString();
                            if (value.equals("")) {
                                return;
                            }
                            Settings.System.putString(getActivity().getContentResolver(),
                                Settings.System.STATUS_BAR_DATE_FORMAT, value);

                            return;
                        }
                    });

                    alert.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int which) {
                            return;
                        }
                    });
                    dialog = alert.create();
                    dialog.show();
                } else {
                    if ((String) newValue != null) {
                        Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.STATUS_BAR_DATE_FORMAT, (String) newValue);
                    }
                }
                return true;
            } else if (preference == mColorPicker) {
                String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                        .valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_COLOR, intHex);
                return true;
            } else if (preference == mFontStyle) {
                int val = Integer.parseInt((String) newValue);
                int index = mFontStyle.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_FONT_STYLE, val);
                mFontStyle.setSummary(mFontStyle.getEntries()[index]);
                return true;
            } else if (preference == mStatusBarClockFontSize) {
                int val = Integer.parseInt((String) newValue);
                int index = mStatusBarClockFontSize.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_FONT_SIZE, val);
                mStatusBarClockFontSize.setSummary(mStatusBarClockFontSize.getEntries()[index]);
                return true;
            } else if (preference == mClockDatePosition) {
                int val = Integer.parseInt((String) newValue);
                int index = mClockDatePosition.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_DATE_POSITION, val);
                mClockDatePosition.setSummary(mClockDatePosition.getEntries()[index]);
                parseClockDateFormats();
                return true;
            }
            return false;
        }

        private void enableStatusBarClockDependents() {
            int clockStyle = CMSettings.System.getInt(getActivity()
                    .getContentResolver(), CMSettings.System.STATUS_BAR_CLOCK, 1);
            if (clockStyle == 0) {
                mStatusBarDate.setEnabled(false);
                mStatusBarDateStyle.setEnabled(false);
                mStatusBarDateFormat.setEnabled(false);
                mClockDatePosition.setEnabled(false);
            } else {
                mStatusBarDate.setEnabled(true);
                mStatusBarDateStyle.setEnabled(true);
                mStatusBarDateFormat.setEnabled(true);
                mClockDatePosition.setEnabled(true);
            }
        }

        private void parseClockDateFormats() {
            // Parse and repopulate mClockDateFormats's entries based on current date.
            String[] dateEntries = getResources().getStringArray(R.array.status_bar_date_format_entries_values);
            CharSequence parsedDateEntries[];
            parsedDateEntries = new String[dateEntries.length];
            Date now = new Date();

            int lastEntry = dateEntries.length - 1;
            int dateFormat = Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUS_BAR_DATE_STYLE, 0);
            for (int i = 0; i < dateEntries.length; i++) {
                if (i == lastEntry) {
                    parsedDateEntries[i] = dateEntries[i];
                } else {
                    String newDate;
                    CharSequence dateString = DateFormat.format(dateEntries[i], now);
                    if (dateFormat == CLOCK_DATE_STYLE_LOWERCASE) {
                        newDate = dateString.toString().toLowerCase();
                    } else if (dateFormat == CLOCK_DATE_STYLE_UPPERCASE) {
                        newDate = dateString.toString().toUpperCase();
                    } else {
                        newDate = dateString.toString();
                    }

                    parsedDateEntries[i] = newDate;
                }
            }
            mStatusBarDateFormat.setEntries(parsedDateEntries);
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

            ClockSettingsFragment.ClockSettingsPreferenceFragment getOwner() {
                return (ClockSettingsFragment.ClockSettingsPreferenceFragment) getTargetFragment();
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                int id = getArguments().getInt("id");
                switch (id) {
                    case DLG_RESET:
                        return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.reset)
                        .setMessage(R.string.status_bar_clock_style_reset_message)
                        .setNegativeButton(R.string.cancel, null)
                        .setNeutralButton(R.string.dlg_reset_android,
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Settings.System.putInt(getActivity().getContentResolver(),
                                    Settings.System.STATUSBAR_CLOCK_COLOR, -2);
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
    }
}
