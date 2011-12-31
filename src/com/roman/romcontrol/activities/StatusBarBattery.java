
package com.roman.romcontrol.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
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

    public class StatusBarBatteryPreferences extends PreferenceFragment {

        private static final String PREF_BATT_TEXT = "text_widget";
        private static final String PREF_BATT_TEXT_CENTER = "text_widget_center";

        CheckBoxPreference mEnableBatteryText;
        CheckBoxPreference mEnableCenterBatteryText;

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

            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

    }

}
