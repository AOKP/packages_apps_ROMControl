
package com.roman.romcontrol;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.view.IWindowManager;

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
                ((PreferenceGroup) findPreference("functionality"))
                        .removePreference(findPreference("led_prefs"));
            }

            // remove navigation bar options
            IWindowManager mWindowManager = IWindowManager.Stub.asInterface(ServiceManager
                    .getService(Context.WINDOW_SERVICE));
            try {
                if (!mWindowManager.hasNavigationBar()) {
                    ((PreferenceGroup) findPreference("rom_ui"))
                            .removePreference(findPreference("nav_bar"));
                } else {
                }
            } catch (RemoteException e) {
            }

        }
    }
}
