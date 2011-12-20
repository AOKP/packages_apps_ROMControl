
package com.roman.romcontrol.fragment;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;

import com.roman.romcontrol.R;

public class LEDPreferences extends PreferenceFragment implements OnPreferenceChangeListener {

    public static final String TAG = "LEDPreferences";

    private static final String PREF_LED_OFF = "led_off";
    private static final String PREF_LED_ON = "led_on";

    ListPreference mLedOffTime;
    ListPreference mLedOnTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_led);

        mLedOffTime = (ListPreference) findPreference(PREF_LED_OFF);
        mLedOffTime.setOnPreferenceChangeListener(this);
        String ledOffTime = Settings.System
                .getInt(getActivity().getContentResolver(),
                        Settings.System.NOTIFICATION_LIGHT_OFF,
                        getActivity().getResources().getInteger(
                                com.android.internal.R.integer.config_defaultNotificationLedOff))
                + "";
        mLedOffTime.setValue(ledOffTime);
        Log.i(TAG, "led off time set to: " + ledOffTime);
        
        mLedOnTime = (ListPreference) findPreference(PREF_LED_ON);
        mLedOnTime.setOnPreferenceChangeListener(this);
        String ledOnTime = Settings.System
                .getInt(getActivity().getContentResolver(),
                        Settings.System.NOTIFICATION_LIGHT_ON,
                        getActivity().getResources().getInteger(
                                com.android.internal.R.integer.config_defaultNotificationLedOn))
                + "";
        mLedOnTime.setValue(ledOnTime);
        Log.i(TAG, "led on time set to: " + ledOnTime);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        if (preference == mLedOffTime) {

            int val = Integer.parseInt((String) newValue);
            Log.i(TAG, "led off time new value: " + val);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NOTIFICATION_LIGHT_OFF, val);

        } else if (preference == mLedOnTime) {

            int val = Integer.parseInt((String) newValue);
            Log.i(TAG, "led on time new value: " + val);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NOTIFICATION_LIGHT_ON, val);
        }

        return result;
    }

}
