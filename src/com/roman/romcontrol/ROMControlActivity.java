
package com.roman.romcontrol;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class ROMControlActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs);

            // ((PreferenceGroup)
            // findPreference("rom_ui")).removePreference(findPreference("lockscreen_pref"));
        }
    }
}
