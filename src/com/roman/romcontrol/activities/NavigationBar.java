
package com.roman.romcontrol.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.roman.romcontrol.R;

public class NavigationBar extends Activity {

    private static final String PREF_MENU_UNLOCK = "pref_menu_display";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment implements
            OnPreferenceChangeListener {

        ListPreference menuDisplayLocation;

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
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {

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
