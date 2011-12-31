
package com.roman.romcontrol.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.roman.romcontrol.R;

public class PowerMenu extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PowerMenuPreferenceFragment()).commit();
    }

    public class PowerMenuPreferenceFragment extends android.preference.PreferenceFragment {

        private static final String PREF_POWER_SAVER = "show_power_saver";
        private static final String PREF_SCREENSHOT = "show_screenshot";

        CheckBoxPreference mShowPowerSaver;
        CheckBoxPreference mShowScreenShot;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs_powermenu);

            mShowPowerSaver = (CheckBoxPreference) findPreference(PREF_POWER_SAVER);
            mShowPowerSaver.setChecked(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_POWER_SAVER,
                    1) == 1);

            mShowScreenShot = (CheckBoxPreference) findPreference(PREF_SCREENSHOT);
            mShowScreenShot.setChecked(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_SCREENSHOT,
                    0) == 1);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            if (preference == mShowPowerSaver) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.POWER_DIALOG_SHOW_POWER_SAVER,
                        ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
                return true;
            } else if (preference == mShowScreenShot) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.POWER_DIALOG_SHOW_SCREENSHOT,
                        ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
                return true;
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

    }

}
