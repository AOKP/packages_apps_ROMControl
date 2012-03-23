
package com.aokp.romcontrol.fragments;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarSignal extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    ListPreference mDbmStyletyle;
    ListPreference mWifiStyle;
    ColorPickerPreference mColorPicker;
    ColorPickerPreference mWifiColorPicker;
//    CheckBoxPreference mHideSignal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_signal);

        mDbmStyletyle = (ListPreference) findPreference("signal_style");
        mDbmStyletyle.setOnPreferenceChangeListener(this);
        mDbmStyletyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_SIGNAL_TEXT,
                0)));

        mColorPicker = (ColorPickerPreference) findPreference("signal_color");
        mColorPicker.setOnPreferenceChangeListener(this);
        mWifiStyle = (ListPreference) findPreference("wifi_signal_style");
        mWifiStyle.setOnPreferenceChangeListener(this);
        mWifiStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT,
                0)));

        mWifiColorPicker = (ColorPickerPreference) findPreference("wifi_signal_color");
        mWifiColorPicker.setOnPreferenceChangeListener(this);

//        mHideSignal = (CheckBoxPreference) findPreference("hide_signal");
//        mHideSignal.setChecked(Settings.System.getInt(getActivity()
//                .getContentResolver(), Settings.System.STATUSBAR_HIDE_SIGNAL_BARS,
//                0) != 0);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
//        if (preference == mHideSignal) {
//            Settings.System.putInt(getActivity().getContentResolver(),
//                    Settings.System.STATUSBAR_HIDE_SIGNAL_BARS,
//                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
//
//            return true;
//        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDbmStyletyle) {

            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_SIGNAL_TEXT, val);
            return true;

        } else if (preference == mColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR, intHex);

            return true;
        } else if (preference == mWifiStyle) {

            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT, val);
            return true;
        } else if (preference == mWifiColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT_COLOR, intHex);

            return true;
        }
        return false;
    }
}
