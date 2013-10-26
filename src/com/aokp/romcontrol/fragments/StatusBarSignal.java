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
    private ListPreference mNetStatsUpdate;
    ColorPickerPreference mColorPicker;
    ColorPickerPreference mWifiColorPicker;
    private ColorPickerPreference mNetStatsColorPicker;
    CheckBoxPreference mHideSignal;
    CheckBoxPreference mAltSignal;
    CheckBoxPreference mShow4gForLte;
    private CheckBoxPreference mNetworkStats;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_statusbar_signal);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_signal);

        PreferenceScreen prefs = getPreferenceScreen();

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

        mNetworkStats = (CheckBoxPreference) findPreference(STATUS_BAR_NETWORK_STATS);
        mNetworkStats.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS, 0) == 1));

        mWifiColorPicker = (ColorPickerPreference) findPreference("wifi_signal_color");
        mWifiColorPicker.setOnPreferenceChangeListener(this);

        mNetStatsColorPicker = (ColorPickerPreference) findPreference("status_bar_network_status_color");
        mNetStatsColorPicker.setOnPreferenceChangeListener(this);

        mHideSignal = (CheckBoxPreference) findPreference("hide_signal");
        mHideSignal.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.STATUSBAR_HIDE_SIGNAL_BARS, false));

        mAltSignal = (CheckBoxPreference) findPreference("alt_signal");
        mAltSignal.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.STATUSBAR_SIGNAL_CLUSTER_ALT, false));

        boolean check4gByDefault = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_show4GForLTE);
        mShow4gForLte = (CheckBoxPreference)findPreference("show_4g_for_lte");
        mShow4gForLte.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.STATUSBAR_SIGNAL_SHOW_4G_FOR_LTE, check4gByDefault));

       mNetStatsUpdate = (ListPreference) findPreference(STATUS_BAR_NETWORK_STATS_UPDATE);
        long statsUpdate = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, 500);
        mNetStatsUpdate.setValue(String.valueOf(statsUpdate));
        mNetStatsUpdate.setSummary(mNetStatsUpdate.getEntry());
        mNetStatsUpdate.setOnPreferenceChangeListener(this);

        if (Integer.parseInt(mDbmStyletyle.getValue()) == 0) {
            mColorPicker.setEnabled(false);
            mColorPicker.setSummary(R.string.enable_signal_text);
        }

        if (Integer.parseInt(mWifiStyle.getValue()) == 0) {
            mWifiColorPicker.setEnabled(false);
            mWifiColorPicker.setSummary(R.string.enable_wifi_text);
        }

        if (!hasPhoneAbility(mContext)) {
            prefs.removePreference(mDbmStyletyle);
            prefs.removePreference(mColorPicker);
            prefs.removePreference(mHideSignal);
            prefs.removePreference(mAltSignal);
            prefs.removePreference(mShow4gForLte);
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
        } else if (preference == mShow4gForLte) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.STATUSBAR_SIGNAL_SHOW_4G_FOR_LTE, mShow4gForLte.isChecked());
            return true;
        } else if (preference == mNetworkStats) {
            boolean value = mNetworkStats.isChecked();
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
        } else if (preference == mNetStatsUpdate) {
            long updateInterval = Long.valueOf((String) newValue);
            int index = mNetStatsUpdate.findIndexOfValue((String) newValue);
            Settings.System.putLong(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, updateInterval);
            mNetStatsUpdate.setSummary(mNetStatsUpdate.getEntries()[index]);
            return true;
        } else if (preference == mNetStatsColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentRes,
                    Settings.System.STATUS_BAR_NETWORK_STATS_TEXT_COLOR, intHex);
            return true;
        }
        return false;
    }
}
