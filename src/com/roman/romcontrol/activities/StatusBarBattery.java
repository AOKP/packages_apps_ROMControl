
package com.roman.romcontrol.activities;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.roman.romcontrol.R;

public class StatusBarBattery extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new StatusBarBatteryPreferences()).commit();
    }

    public class StatusBarBatteryPreferences extends PreferenceFragment implements
            OnPreferenceChangeListener {

        private static final String PREF_BATT_TEXT = "text_widget";
        private static final String PREF_BATT_TEXT_CENTER = "text_widget_center";
        private static final String PREF_BATT_BAR = "battery_bar";
        private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
        private static final String PREF_BATT = "show_battery_icon";

        CheckBoxPreference mEnableBatteryText;
        CheckBoxPreference mEnableCenterBatteryText;
        CheckBoxPreference mBatteryBar;
        CheckBoxPreference mShowBatteryIcon;
        ColorPickerPreference mBatteryBarColor;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs_statusbar_battery);

            mEnableBatteryText = (CheckBoxPreference) findPreference(PREF_BATT_TEXT);
            mEnableBatteryText.setChecked(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUSBAR_BATTERY_TEXT,
                    0) == 1);

            mEnableCenterBatteryText = (CheckBoxPreference) findPreference(PREF_BATT_TEXT_CENTER);
            mEnableCenterBatteryText.setChecked(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUSBAR_BATTERY_TEXT_STYLE,
                    0) == 1);

            mBatteryBar = (CheckBoxPreference) findPreference(PREF_BATT_BAR);
            mBatteryBar.setChecked(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUSBAR_BATTERY_BAR,
                    0) == 1);

            mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_COLOR);
            mBatteryBarColor.setOnPreferenceChangeListener(this);

            mShowBatteryIcon = (CheckBoxPreference) findPreference(PREF_BATT);
            mShowBatteryIcon.setChecked(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUSBAR_BATTERY_ICON,
                    1) == 1);

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            if (preference == mEnableBatteryText) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_BATTERY_TEXT,
                        ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
                return true;

            } else if (preference == mEnableCenterBatteryText) {

                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_BATTERY_TEXT_STYLE,
                        ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
                return true;

            } else if (preference == mBatteryBar) {

                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_BATTERY_BAR,
                        ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
                return true;

            } else if (preference == mShowBatteryIcon) {

                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_BATTERY_ICON,
                        ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
                return true;

            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mBatteryBarColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                        .valueOf(newValue)));
                preference.setSummary(hex);

                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_BATTERY_BAR_COLOR, intHex);
                return true;

            }
            return false;
        }

    }

}
