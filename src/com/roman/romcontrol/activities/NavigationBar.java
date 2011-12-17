
package com.roman.romcontrol.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.roman.romcontrol.R;

public class NavigationBar extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment implements
            OnPreferenceChangeListener {

        private static final String PREF_MENU_UNLOCK = "pref_menu_display";
        private static final String PREF_CRT_ON = "crt_on";
        private static final String PREF_CRT_OFF = "crt_off";

        ListPreference menuDisplayLocation;
        CheckBoxPreference mCrtOnAnimation;
        CheckBoxPreference mCrtOffAnimation;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs_navigation_menu);

            menuDisplayLocation = (ListPreference) findPreference(PREF_MENU_UNLOCK);
            menuDisplayLocation.setOnPreferenceChangeListener(this);
            menuDisplayLocation.setValue(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.MENU_LOCATION,
                    0) + "");

            mCrtOffAnimation = (CheckBoxPreference) findPreference(PREF_CRT_OFF);
            mCrtOffAnimation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.CRT_OFF_ANIMATION, 1) == 1);

            mCrtOnAnimation = (CheckBoxPreference) findPreference(PREF_CRT_ON);
            mCrtOnAnimation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.CRT_ON_ANIMATION, 0) == 1);

            ((PreferenceGroup) findPreference("crt")).removePreference(mCrtOnAnimation);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            if (preference == mCrtOffAnimation) {

                boolean checked = ((CheckBoxPreference) preference).isChecked();
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.CRT_OFF_ANIMATION, checked ? 1 : 0);
                return true;

            } else if (preference == mCrtOnAnimation) {

                boolean checked = ((CheckBoxPreference) preference).isChecked();
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.CRT_ON_ANIMATION, checked ? 1 : 0);
                return true;
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == menuDisplayLocation) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.MENU_LOCATION, Integer.parseInt((String) newValue));
                return true;
            }
            return false;
        }
    }

}
