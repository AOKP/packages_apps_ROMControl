
package com.aokp.romcontrol.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarClock extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_ENABLE = "clock_style";
    private static final String PREF_AM_PM_STYLE = "clock_am_pm_style";
    private static final String PREF_COLOR_PICKER = "clock_color";
    private static final String PREF_ALARM_ENABLE = "alarm";
    private static final String PREF_CLOCK_WEEKDAY = "clock_weekday";

    ListPreference mClockStyle;
    ListPreference mClockAmPmstyle;
    CheckBoxPreference mAlarm;
    ColorPickerPreference mColorPicker;
    ListPreference mClockWeekday;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_clock);

        mClockStyle = (ListPreference) findPreference(PREF_ENABLE);
        mClockStyle.setOnPreferenceChangeListener(this);
        mClockStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_STYLE,
                1)));

        mClockAmPmstyle = (ListPreference) findPreference(PREF_AM_PM_STYLE);
        mClockAmPmstyle.setOnPreferenceChangeListener(this);
        mClockAmPmstyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE,
                2)));

        mColorPicker = (ColorPickerPreference) findPreference(PREF_COLOR_PICKER);
        mColorPicker.setOnPreferenceChangeListener(this);

        mAlarm = (CheckBoxPreference) findPreference(PREF_ALARM_ENABLE);
        mAlarm.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_SHOW_ALARM,
                1) == 1);

        mClockWeekday = (ListPreference) findPreference(PREF_CLOCK_WEEKDAY);
        mClockWeekday.setOnPreferenceChangeListener(this);
        mClockWeekday.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_WEEKDAY,
                0)));

        if (mTablet) {
            PreferenceScreen prefs = getPreferenceScreen();
            prefs.removePreference(mClockAmPmstyle);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {

        if (preference == mAlarm) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_SHOW_ALARM,
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

        } else if (preference == mClockStyle) {

            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_STYLE, val);

        } else if (preference == mColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_COLOR, intHex);
            Log.e("ROMAN", intHex + "");
        } else if (preference == mClockWeekday) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_WEEKDAY, val);
        }
        return result;
    }
}
