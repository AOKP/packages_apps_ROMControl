
package com.roman.romcontrol.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.roman.romcontrol.R;

public class StatusBarClock extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new StatusBarClockPreferences()).commit();
    }

    public class StatusBarClockPreferences extends PreferenceFragment implements
            OnPreferenceChangeListener {

        private static final String PREF_ENABLE = "clock_enable";
        private static final String PREF_AM_PM_STYLE = "clock_am_pm_style";

        CheckBoxPreference mClockEnabled;
        ListPreference mClockAmPmstyle;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs_statusbar_clock);

            mClockEnabled = (CheckBoxPreference) findPreference(PREF_ENABLE);
            mClockEnabled.setChecked(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUSBAR_CLOCK_ENABLED,
                    1) == 1);

            mClockAmPmstyle = (ListPreference) findPreference(PREF_AM_PM_STYLE);
            mClockAmPmstyle.setOnPreferenceChangeListener(this);
            mClockAmPmstyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE,
                    2)));

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            if (preference == mClockEnabled) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_ENABLED,
                        ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
                return true;

            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean result = false;

            if (preference == mClockAmPmstyle) {

                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, val);

            }
            return result;
        }
    }
}
