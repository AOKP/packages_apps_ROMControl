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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import lineageos.providers.LineageSettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusbarSettingsFragment extends Fragment {

    public StatusbarSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_statusbar_settings_main, container, false);

        Resources res = getResources();

        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.statusbar_settings_main, new StatusBarSettingsPreferenceFragment())
                .commit();

        return v;
    }

    public static class StatusBarSettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {
        public StatusBarSettingsPreferenceFragment() {

        }

        private static final String TAG = "StatusBar";

        private ContentResolver mContentResolver;

        private static final String STATUS_BAR_DATE = "status_bar_date";
        private static final String STATUS_BAR_DATE_STYLE = "status_bar_date_style";
        private static final String STATUS_BAR_DATE_FORMAT = "status_bar_date_format";
        private static final String PREF_CLOCK_DATE_POSITION = "clock_date_position";
        private static final String PREF_SHOWSU = "show_su_indicator";
        private static final String STATUSBAR_BATTERY_STYLE = "statusbar_battery_style";
        private static final String FORCE_BATTERY_PERCENTAGE = "keyguard_qsheader_show_battery_percent";

        private static final String CATEGORY_WEATHER = "weather_category";
        private static final String WEATHER_ICON_PACK = "weather_icon_pack";
        private static final String DEFAULT_WEATHER_ICON_PACKAGE = "org.omnirom.omnijaws";
        private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
        private static final String CHRONUS_ICON_PACK_INTENT = "com.dvtonder.chronus.ICON_PACK";

        private static final String PREF_AOKP_LOGO = "status_bar_aokp_logo";
        private static final String KEY_AOKP_LOGO_COLOR = "status_bar_aokp_logo_color";
        private static final String KEY_AOKP_LOGO_POSITION = "status_bar_aokp_logo_position";
        private static final String KEY_AOKP_LOGO_STYLE = "status_bar_aokp_logo_style";

        public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
        public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
        private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18;

        private ListPreference mStatusBarDate;
        private ListPreference mStatusBarDateStyle;
        private ListPreference mStatusBarDateFormat;
        private ListPreference mClockDatePosition;
        private ListPreference mBatteryStyle;
        private SwitchPreference mShowSU;
        private SwitchPreference mForceShowQSHeaderPercent;
        private PreferenceCategory mWeatherCategory;
        private ListPreference mWeatherIconPack;
        private String mWeatherIconPackNote;

        private SwitchPreference mAokpLogo;
        private ColorPickerPreference mAokpLogoColor;
        private ListPreference mAokpLogoPosition;
        private ListPreference mAokpLogoStyle;

        private boolean mForceShowPercent;
        private boolean mCheckPreferences;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            mCheckPreferences = false;
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_statusbar_settings);
            PreferenceScreen prefSet = getPreferenceScreen();
            final ContentResolver resolver = getActivity().getContentResolver();
            final PackageManager pm = getActivity().getPackageManager();
            mWeatherIconPackNote = getResources().getString(R.string.weather_icon_pack_note);

            mStatusBarDate = (ListPreference) findPreference(STATUS_BAR_DATE);
            mStatusBarDateStyle = (ListPreference) findPreference(STATUS_BAR_DATE_STYLE);
            mStatusBarDateFormat = (ListPreference) findPreference(STATUS_BAR_DATE_FORMAT);

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

            mClockDatePosition = (ListPreference) findPreference(PREF_CLOCK_DATE_POSITION);
            mClockDatePosition.setOnPreferenceChangeListener(this);
            mClockDatePosition.setValue(Integer.toString(Settings.System.getInt(resolver,
                    Settings.System.STATUSBAR_CLOCK_DATE_POSITION,
                    0)));
            mClockDatePosition.setSummary(mClockDatePosition.getEntry());

            mShowSU = (SwitchPreference) findPreference(PREF_SHOWSU);
            mShowSU.setChecked(Settings.System.getInt(resolver,
                    Settings.System.SHOW_SU_INDICATOR, 1) != 0);
            mShowSU.setOnPreferenceChangeListener(this);

            mBatteryStyle = (ListPreference) findPreference(STATUSBAR_BATTERY_STYLE);
            mBatteryStyle.setOnPreferenceChangeListener(this);
            mBatteryStyle.setValue(Integer.toString(Settings.Secure.getInt(resolver,
                    Settings.Secure.STATUS_BAR_BATTERY_STYLE, 0)));
            mBatteryStyle.setSummary(mBatteryStyle.getEntry());

            mForceShowQSHeaderPercent = (SwitchPreference) findPreference(FORCE_BATTERY_PERCENTAGE);
            mForceShowQSHeaderPercent.setOnPreferenceChangeListener(this);
            int forceShowQSHeaderPercent = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_BATTERY_PERCENT, 0);
            mForceShowQSHeaderPercent.setChecked(forceShowQSHeaderPercent != 0);

            mForceShowPercent = Settings.System.getInt(resolver,
                Settings.System.SHOW_BATTERY_PERCENT,0) != 0;

            updateDependencies(Integer.parseInt((String) mBatteryStyle.getValue()));

            if (Helpers.isPackageInstalled(WEATHER_SERVICE_PACKAGE, pm)) {
                String settingsJaws = Settings.System.getString(resolver,
                        Settings.System.OMNIJAWS_WEATHER_ICON_PACK);
                if (settingsJaws == null) {
                    settingsJaws = DEFAULT_WEATHER_ICON_PACKAGE;
                }
                mWeatherIconPack = (ListPreference) findPreference(WEATHER_ICON_PACK);

                List<String> entries = new ArrayList<String>();
                List<String> values = new ArrayList<String>();
                getAvailableWeatherIconPacks(entries, values);
                mWeatherIconPack.setEntries(entries.toArray(new String[entries.size()]));
                mWeatherIconPack.setEntryValues(values.toArray(new String[values.size()]));

                int valueJawsIndex = mWeatherIconPack.findIndexOfValue(settingsJaws);
                if (valueJawsIndex == -1) {
                    // no longer found
                    settingsJaws = DEFAULT_WEATHER_ICON_PACKAGE;
                    Settings.System.putString(resolver,
                            Settings.System.OMNIJAWS_WEATHER_ICON_PACK, settingsJaws);
                    valueJawsIndex = mWeatherIconPack.findIndexOfValue(settingsJaws);
                }
                mWeatherIconPack.setValueIndex(valueJawsIndex >= 0 ? valueJawsIndex : 0);
                mWeatherIconPack.setSummary(mWeatherIconPackNote + "\n\n" + mWeatherIconPack.getEntry());
                mWeatherIconPack.setOnPreferenceChangeListener(this);
            }

            // Aokp logo color & Style
            mAokpLogo = (SwitchPreference) findPreference(PREF_AOKP_LOGO);
            mAokpLogo.setChecked(Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_AOKP_LOGO, 1) != 0);
            mAokpLogo.setOnPreferenceChangeListener(this);
            mAokpLogoColor =
                (ColorPickerPreference) prefSet.findPreference(KEY_AOKP_LOGO_COLOR);
            mAokpLogoColor.setOnPreferenceChangeListener(this);
            int intColor = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_AOKP_LOGO_COLOR, 0xffffffff);
            String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mAokpLogoColor.setSummary(hexColor);
            mAokpLogoColor.setNewPreviewColor(intColor);
            mAokpLogoPosition = (ListPreference) findPreference(KEY_AOKP_LOGO_POSITION);
            int AokpLogoPosition = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_AOKP_LOGO_POSITION, 0);
            mAokpLogoPosition.setValue(String.valueOf(AokpLogoPosition));
            mAokpLogoPosition.setSummary(mAokpLogoPosition.getEntry());
            mAokpLogoPosition.setOnPreferenceChangeListener(this);
            mAokpLogoStyle = (ListPreference) findPreference(KEY_AOKP_LOGO_STYLE);
            int AokpLogoStyle = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_AOKP_LOGO_STYLE, 0);
            mAokpLogoStyle.setValue(String.valueOf(AokpLogoStyle));
            mAokpLogoStyle.setSummary(mAokpLogoStyle.getEntry());
            mAokpLogoStyle.setOnPreferenceChangeListener(this);

            return prefSet;
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        protected ContentResolver getContentResolver() {
            Context context = getActivity();
            if (context != null) {
                mContentResolver = context.getContentResolver();
            }
            return mContentResolver;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            AlertDialog dialog;
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mStatusBarDate) {
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
            } else if (preference == mClockDatePosition) {
                int val = Integer.parseInt((String) newValue);
                int index = mClockDatePosition.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_DATE_POSITION, val);
                mClockDatePosition.setSummary(mClockDatePosition.getEntries()[index]);
                parseClockDateFormats();
                return true;
            } else if (preference == mShowSU) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.SHOW_SU_INDICATOR, value ? 1 : 0);
                return true;
            } else if (preference == mBatteryStyle) {
                int val = Integer.parseInt((String) newValue);
                int index = mBatteryStyle.findIndexOfValue((String) newValue);
                Settings.Secure.putInt(resolver,
                        Settings.Secure.STATUS_BAR_BATTERY_STYLE, val);
                mBatteryStyle.setSummary(mBatteryStyle.getEntries()[index]);
                updateDependencies(val);
                return true;
            } else if (preference == mForceShowQSHeaderPercent) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver, Settings.System.QS_HEADER_BATTERY_PERCENT,
                        value ? 1 : 0);
                return true;
            } else if (preference == mWeatherIconPack) {
                String value = (String) newValue;
                Settings.System.putString(resolver,
                      Settings.System.OMNIJAWS_WEATHER_ICON_PACK, value);
                int valueIndex = mWeatherIconPack.findIndexOfValue(value);
                mWeatherIconPack.setSummary(mWeatherIconPackNote + " \n\n" + mWeatherIconPack.getEntries()[valueIndex]);
                return true;
            } else if (preference == mAokpLogo) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_AOKP_LOGO, value ? 1 : 0);
                return true;
            } else if (preference == mAokpLogoColor) {
                String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_AOKP_LOGO_COLOR, intHex);
                return true;
            } else if (preference == mAokpLogoPosition) {
                int AokpLogoPosition = Integer.valueOf((String) newValue);
                int index = mAokpLogoPosition.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_AOKP_LOGO_POSITION, AokpLogoPosition);
                mAokpLogoPosition.setSummary(
                        mAokpLogoPosition.getEntries()[index]);
                return true;
            } else if (preference == mAokpLogoStyle) {
                int AokpLogoStyle = Integer.valueOf((String) newValue);
                int index = mAokpLogoStyle.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_AOKP_LOGO_STYLE, AokpLogoStyle);
                mAokpLogoStyle.setSummary(
                        mAokpLogoStyle.getEntries()[index]);
                return true;
            }
            return false;
        }

        private void enableStatusBarClockDependents() {
            int clockStyle = LineageSettings.System.getInt(getActivity()
                    .getContentResolver(), LineageSettings.System.STATUS_BAR_CLOCK, 1);
            if (clockStyle == 0) {
                mStatusBarDate.setEnabled(false);
                mStatusBarDateStyle.setEnabled(false);
                mStatusBarDateFormat.setEnabled(false);
            } else {
                mStatusBarDate.setEnabled(true);
                mStatusBarDateStyle.setEnabled(true);
                mStatusBarDateFormat.setEnabled(true);
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

        private void updateDependencies(int index) {
            if (mForceShowPercent) {
                  if (index != 5) {
                      mForceShowQSHeaderPercent.setEnabled(false);
                  } else {
                      mForceShowQSHeaderPercent.setEnabled(true);
                  }
            } else {
                  if (index == 2 || index == 3) {
                      mForceShowQSHeaderPercent.setEnabled(false);
                  } else {
                      mForceShowQSHeaderPercent.setEnabled(true);
                  }
            }
        }

        private void getAvailableWeatherIconPacks(List<String> entries, List<String> values) {
            Intent i = new Intent();
            PackageManager packageManager = getActivity().getPackageManager();
            i.setAction("org.omnirom.WeatherIconPack");
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                String packageName = r.activityInfo.packageName;
                Log.d("IconPack package name: ", packageName);
                if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                    values.add(0, r.activityInfo.name);
                } else {
                    values.add(r.activityInfo.name);
                }
                String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
                if (label == null) {
                    label = r.activityInfo.packageName;
                }
                if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                    entries.add(0, label);
                } else {
                    entries.add(label);
                }
            }
            i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(CHRONUS_ICON_PACK_INTENT);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                String packageName = r.activityInfo.packageName;
                values.add(packageName + ".weather");
                String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
                if (label == null) {
                    label = r.activityInfo.packageName;
                }
                entries.add(label);
            }
       }

       private boolean isOmniJawsEnabled() {
            final Uri SETTINGS_URI
                = Uri.parse("content://org.omnirom.omnijaws.provider/settings");

            final String[] SETTINGS_PROJECTION = new String[] {
                "enabled"
            };

            final Cursor c = getActivity().getContentResolver().query(SETTINGS_URI, SETTINGS_PROJECTION,
                    null, null, null);
            if (c != null) {
               int count = c.getCount();
               if (count == 1) {
                    c.moveToPosition(0);
                    boolean enabled = c.getInt(0) == 1;
                    return enabled;
                }
            }
            return true;
        }
    }
}
