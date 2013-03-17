
package com.aokp.romcontrol.fragments;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.util.Helpers;

public class StatusBarSignal extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    private static int STOCK_FONT_SIZE = 16;

    ListPreference mDbmStyletyle;
    ListPreference mWifiStyle;
    ListPreference mFontsize;
    ColorPickerPreference mColorPicker;
    ColorPickerPreference mWifiColorPicker;
    CheckBoxPreference mHideSignal;
    CheckBoxPreference mAltSignal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_signal);

        mDbmStyletyle = (ListPreference) findPreference("signal_style");
        mDbmStyletyle.setOnPreferenceChangeListener(this);
        mDbmStyletyle.setValue(Integer.toString(Settings.System.getInt(mContentRes,
                Settings.System.STATUSBAR_SIGNAL_TEXT, 0)));

        mColorPicker = (ColorPickerPreference) findPreference("signal_color");
        mColorPicker.setOnPreferenceChangeListener(this);
        mWifiStyle = (ListPreference) findPreference("wifi_signal_style");
        mWifiStyle.setOnPreferenceChangeListener(this);
        mWifiStyle.setValue(Integer.toString(Settings.System.getInt(mContentRes,
                Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT, 0)));

        mWifiColorPicker = (ColorPickerPreference) findPreference("wifi_signal_color");
        mWifiColorPicker.setOnPreferenceChangeListener(this);

        mHideSignal = (CheckBoxPreference) findPreference("hide_signal");
        mHideSignal.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.STATUSBAR_HIDE_SIGNAL_BARS, false));

        mAltSignal = (CheckBoxPreference) findPreference("alt_signal");
        mAltSignal.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.STATUSBAR_SIGNAL_CLUSTER_ALT,false));

        mFontsize = (ListPreference) findPreference("status_bar_fontsize");
        mFontsize.setOnPreferenceChangeListener(this);
        mFontsize.setValue(Integer.toString(Settings.System.getInt(mContentRes,
                Settings.System.STATUSBAR_FONT_SIZE, STOCK_FONT_SIZE)));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mHideSignal) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.STATUSBAR_HIDE_SIGNAL_BARS, mHideSignal.isChecked());

            return true;
        } else if (preference == mAltSignal) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.STATUSBAR_SIGNAL_CLUSTER_ALT,mAltSignal.isChecked());
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDbmStyletyle) {

            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.STATUSBAR_SIGNAL_TEXT, val);
            return true;

        } else if (preference == mColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentRes,
                    Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR, intHex);

            return true;
        } else if (preference == mWifiStyle) {

            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT, val);
            return true;
        } else if (preference == mWifiColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentRes,
                    Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT_COLOR, intHex);

            return true;
        } else if (preference == mFontsize) {

            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.STATUSBAR_FONT_SIZE, val);
            Helpers.restartSystemUI();
        }
        return false;
    }
}
