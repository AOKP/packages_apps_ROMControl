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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
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

import cyanogenmod.providers.CMSettings;

import java.util.Date;

import com.aokp.romcontrol.R;
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

        private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
        private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
        private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
        private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
        private static final String STATUS_BAR_CHARGE_COLOR = "status_bar_charge_color";
        private static final String PREF_CLOCK_DATE_POSITION = "clock_date_position";
        private static final String SMS_BREATH = "sms_breath";
        private static final String MISSED_CALL_BREATH = "missed_call_breath";
        private static final String VOICEMAIL_BREATH = "voicemail_breath";

        public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
        public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
        private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18;

        private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;
        private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;

        private static final String KEY_AOKP_LOGO_COLOR = "status_bar_aokp_logo_color";
        private static final String KEY_AOKP_LOGO_STYLE = "status_bar_aokp_logo_style";

        private ListPreference mStatusBarDate;
        private ListPreference mStatusBarDateStyle;
        private ListPreference mStatusBarDateFormat;
        private ListPreference mClockDatePosition;

        private ListPreference mStatusBarClock;
        private ListPreference mStatusBarAmPm;
        private ListPreference mStatusBarBattery;
        private ListPreference mStatusBarBatteryShowPercent;
        private SwitchPreference mSmsBreath;
        private SwitchPreference mMissedCallBreath;
        private SwitchPreference mVoicemailBreath;

        private ColorPickerPreference mAokpLogoColor;
        private ListPreference mAokpLogoStyle;
        private ColorPickerPreference mChargeColor;
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

            PackageManager pm = getActivity().getPackageManager();
            Resources systemUiResources;
            try {
                systemUiResources = pm.getResourcesForApplication("com.android.systemui");
            } catch (Exception e) {
                Log.e(TAG, "can't access systemui resources",e);
                return null;
            }

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

            mStatusBarClock = (ListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);
            mStatusBarAmPm = (ListPreference) findPreference(STATUS_BAR_AM_PM);
            mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
            mStatusBarBatteryShowPercent = (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

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

            mClockDatePosition = (ListPreference) findPreference(PREF_CLOCK_DATE_POSITION);
            mClockDatePosition.setOnPreferenceChangeListener(this);
            mClockDatePosition.setValue(Integer.toString(Settings.System.getInt(resolver,
                    Settings.System.STATUSBAR_CLOCK_DATE_POSITION,
                    0)));
            mClockDatePosition.setSummary(mClockDatePosition.getEntry());

            int batteryStyle = CMSettings.System.getInt(resolver,
                CMSettings.System.STATUS_BAR_BATTERY_STYLE, 0);
            mStatusBarBattery.setValue(String.valueOf(batteryStyle));
                mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
            mStatusBarBattery.setOnPreferenceChangeListener(this);

            int batteryShowPercent = CMSettings.System.getInt(resolver,
                    CMSettings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
            mStatusBarBatteryShowPercent.setValue(String.valueOf(batteryShowPercent));
            mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
            enableStatusBarBatteryDependents(batteryStyle);
            mStatusBarBatteryShowPercent.setOnPreferenceChangeListener(this);

            // Aokp logo color & Style
            mAokpLogoColor =
                (ColorPickerPreference) prefSet.findPreference(KEY_AOKP_LOGO_COLOR);
            mAokpLogoColor.setOnPreferenceChangeListener(this);
            int intColor = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_AOKP_LOGO_COLOR, 0xffffffff);
            String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mAokpLogoColor.setSummary(hexColor);
            mAokpLogoColor.setNewPreviewColor(intColor);
            mAokpLogoStyle = (ListPreference) findPreference(KEY_AOKP_LOGO_STYLE);
            int AokpLogoStyle = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_AOKP_LOGO_STYLE, 0,
                    UserHandle.USER_CURRENT);
            mAokpLogoStyle.setValue(String.valueOf(AokpLogoStyle));
            mAokpLogoStyle.setSummary(mAokpLogoStyle.getEntry());
            mAokpLogoStyle.setOnPreferenceChangeListener(this);

            // Breathing Notifications
            mSmsBreath = (SwitchPreference) findPreference(SMS_BREATH);
            mMissedCallBreath = (SwitchPreference) findPreference(MISSED_CALL_BREATH);
            mVoicemailBreath = (SwitchPreference) findPreference(VOICEMAIL_BREATH);

            ConnectivityManager cm = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {

                mSmsBreath.setChecked(Settings.System.getInt(resolver,
                        Settings.System.KEY_SMS_BREATH, 0) == 1);
                mSmsBreath.setOnPreferenceChangeListener(this);

                mMissedCallBreath.setChecked(Settings.System.getInt(resolver,
                        Settings.System.KEY_MISSED_CALL_BREATH, 0) == 1);
                mMissedCallBreath.setOnPreferenceChangeListener(this);

                mVoicemailBreath.setChecked(Settings.System.getInt(resolver,
                        Settings.System.KEY_VOICEMAIL_BREATH, 0) == 1);
                mVoicemailBreath.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mSmsBreath);
                prefSet.removePreference(mMissedCallBreath);
                prefSet.removePreference(mVoicemailBreath);
            }

            int chargeColor = Settings.Secure.getInt(resolver,
                    Settings.Secure.STATUS_BAR_CHARGE_COLOR, Color.WHITE);
            mChargeColor = (ColorPickerPreference) findPreference("status_bar_charge_color");
            mChargeColor.setNewPreviewColor(chargeColor);
            mChargeColor.setOnPreferenceChangeListener(this);

            return prefSet;
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

        protected ContentResolver getContentResolver() {
            Context context = getActivity();
            if (context != null) {
                mContentResolver = context.getContentResolver();
            }
            return mContentResolver;
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            AlertDialog dialog;
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
            } else if (preference == mStatusBarBattery) {
                int batteryStyle = Integer.valueOf((String) newValue);
                int index = mStatusBarBattery.findIndexOfValue((String) newValue);
                CMSettings.System.putInt(
                        resolver, CMSettings.System.STATUS_BAR_BATTERY_STYLE, batteryStyle);
                mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
                enableStatusBarBatteryDependents(batteryStyle);
                return true;
            } else if (preference == mStatusBarBatteryShowPercent) {
                int batteryShowPercent = Integer.valueOf((String) newValue);
                int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) newValue);
                CMSettings.System.putInt(
                        resolver, CMSettings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, batteryShowPercent);
                mStatusBarBatteryShowPercent.setSummary(
                        mStatusBarBatteryShowPercent.getEntries()[index]);
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
            } else if (preference == mClockDatePosition) {
                int val = Integer.parseInt((String) newValue);
                int index = mClockDatePosition.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_DATE_POSITION, val);
                mClockDatePosition.setSummary(mClockDatePosition.getEntries()[index]);
                parseClockDateFormats();
                 return true;
            } else if (preference == mAokpLogoColor) {
                String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_AOKP_LOGO_COLOR, intHex);
                return true;
            } else if (preference == mAokpLogoStyle) {
                int AokpLogoStyle = Integer.valueOf((String) newValue);
                int index = mAokpLogoStyle.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(
                        resolver, Settings.System.STATUS_BAR_AOKP_LOGO_STYLE, AokpLogoStyle,
                        UserHandle.USER_CURRENT);
                mAokpLogoStyle.setSummary(
                        mAokpLogoStyle.getEntries()[index]);
                return true;
            } else if (preference == mSmsBreath) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver, Settings.System.KEY_SMS_BREATH, value ? 1 : 0);
                return true;
            } else if (preference == mMissedCallBreath) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver, Settings.System.KEY_MISSED_CALL_BREATH, value ? 1 : 0);
                return true;
            } else if (preference == mVoicemailBreath) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver, Settings.System.KEY_VOICEMAIL_BREATH, value ? 1 : 0);
                return true;
            } else if (preference.equals(mChargeColor)) {
                int color = ((Integer) newValue).intValue();
                Settings.Secure.putInt(resolver,
                        Settings.Secure.STATUS_BAR_CHARGE_COLOR, color);
                return true;
            }
            return false;
        }

        private void enableStatusBarBatteryDependents(int batteryIconStyle) {
            if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_HIDDEN ||
                    batteryIconStyle == STATUS_BAR_BATTERY_STYLE_TEXT) {
                mStatusBarBatteryShowPercent.setEnabled(false);
            } else {
                mStatusBarBatteryShowPercent.setEnabled(true);
            }
        }

        private void enableStatusBarClockDependents() {
            int clockStyle = CMSettings.System.getInt(getActivity()
                    .getContentResolver(), CMSettings.System.STATUS_BAR_CLOCK, 1);
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
    }
}
