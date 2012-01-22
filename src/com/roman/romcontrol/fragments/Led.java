
package com.roman.romcontrol.fragments;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.roman.romcontrol.R;
import com.roman.romcontrol.SettingsPreferenceFragment;

public class Led extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    public static final String TAG = "LEDPreferences";

    private static final String PREF_LED_OFF = "led_off";
    private static final String PREF_LED_ON = "led_on";
    private static final String PREF_COLOR_PICKER = "led_color";

    ListPreference mLedOffTime;
    ListPreference mLedOnTime;
    ColorPickerPreference mColorPicker;

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
                        getActivity()
                                .getResources()
                                .getInteger(
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

        mColorPicker = (ColorPickerPreference) findPreference(PREF_COLOR_PICKER);
        mColorPicker.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        // if (preference == mColorPicker) {
        //
        // }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
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
        } else if (preference == mColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NOTIFICATION_LIGHT_COLOR, intHex);
            Log.e("ROMAN", intHex + "");
        }

        return result;
    }

}
