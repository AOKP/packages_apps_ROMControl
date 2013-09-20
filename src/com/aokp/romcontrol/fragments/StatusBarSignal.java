package com.aokp.romcontrol.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarSignal extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String STATUS_BAR_NETWORK_STATS = "status_bar_show_network_stats";
    private static final String STATUS_BAR_NETWORK_STATS_UPDATE = "status_bar_network_status_update";

    ListPreference mDbmStyletyle;
    ListPreference mWifiStyle;
    private ListPreference mStatusBarNetStatsUpdate;
    ColorPickerPreference mColorPicker;
    ColorPickerPreference mWifiColorPicker;
    CheckBoxPreference mHideSignal;
    CheckBoxPreference mAltSignal;
    private CheckBoxPreference mStatusBarNetworkStats;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_statusbar_signal);
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

        mStatusBarNetworkStats = (CheckBoxPreference) findPreference(STATUS_BAR_NETWORK_STATS);
        mStatusBarNetworkStats.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS, 0) == 1));

        mWifiColorPicker = (ColorPickerPreference) findPreference("wifi_signal_color");
        mWifiColorPicker.setOnPreferenceChangeListener(this);

        mHideSignal = (CheckBoxPreference) findPreference("hide_signal");
        mHideSignal.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.STATUSBAR_HIDE_SIGNAL_BARS, false));

        mAltSignal = (CheckBoxPreference) findPreference("alt_signal");
        mAltSignal.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.STATUSBAR_SIGNAL_CLUSTER_ALT, false));

        mStatusBarNetStatsUpdate = (ListPreference) findPreference(STATUS_BAR_NETWORK_STATS_UPDATE);
        long statsUpdate = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, 500);
        mStatusBarNetStatsUpdate.setValue(String.valueOf(statsUpdate));
        mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntry());
        mStatusBarNetStatsUpdate.setOnPreferenceChangeListener(this);

        if (Integer.parseInt(mDbmStyletyle.getValue()) == 0) {
            mColorPicker.setEnabled(false);
            mColorPicker.setSummary(R.string.enable_signal_text);
        }

        if (Integer.parseInt(mWifiStyle.getValue()) == 0) {
            mWifiColorPicker.setEnabled(false);
            mWifiColorPicker.setSummary(R.string.enable_wifi_text);
        }
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
                    Settings.System.STATUSBAR_SIGNAL_CLUSTER_ALT, mAltSignal.isChecked());
            return true;
        } else if (preference == mStatusBarNetworkStats) {
            boolean value = mStatusBarNetworkStats.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS, value ? 1 : 0);
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
            mColorPicker.setEnabled(val == 0 ? false : true);
            if (val == 0) {
                mColorPicker.setSummary(R.string.enable_signal_text);
            } else {
                mColorPicker.setSummary(null);
            }
            Helpers.restartSystemUI();
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
            mWifiColorPicker.setEnabled(val == 0 ? false : true);
            if (val == 0) {
                mWifiColorPicker.setSummary(R.string.enable_wifi_text);
            } else {
                mWifiColorPicker.setSummary(null);
            }
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mWifiColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentRes,
                    Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mStatusBarNetStatsUpdate) {
            long updateInterval = Long.valueOf((String) newValue);
            int index = mStatusBarNetStatsUpdate.findIndexOfValue((String) newValue);
            Settings.System.putLong(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, updateInterval);
            mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntries()[index]);
            return true;
        }
        return false;
    }
}
