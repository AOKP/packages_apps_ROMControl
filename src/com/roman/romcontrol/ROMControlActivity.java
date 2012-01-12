
package com.roman.romcontrol;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

public class ROMControlActivity extends Activity {

    private static boolean hasNotificationLed;

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

            hasNotificationLed = getResources().getBoolean(R.bool.has_notification_led);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs);

            if (!hasNotificationLed) {
                ((PreferenceGroup)
                findPreference("functionality")).removePreference(findPreference("led_prefs"));
            }
        }
    }
}
