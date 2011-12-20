
package com.roman.romcontrol.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.server.PowerSaverService;
import android.util.Log;

import com.roman.romcontrol.R;

public class PowerSaver extends Activity {

    private static final String TAG = "PowerSaver";

    private static final String PREF_MODE = "pref_mode";
    private static final String PREF_DATA_MODE = "pref_powersaving_data_screen_off";
    private static final String PREF_DATA_DELAY = "pref_powersaving_data_screen_off_delay";
    private static final String PREF_SYNC_MODE = "pref_powersaving_sync_screen_off";
    private static final String PREF_SYNC_INTERVAL = "pref_powersaving_sync_screen_off_interval";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment implements
            OnPreferenceChangeListener {

        CheckBoxPreference mPowerSaverEnabled;
        ListPreference mDataMode;
        ListPreference mDataDelay;
        ListPreference mSyncMode;
        ListPreference mSyncInterval;

        Handler handler = new Handler();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs_powersaver);

            mPowerSaverEnabled = (CheckBoxPreference) findPreference(PREF_MODE);
            mPowerSaverEnabled
                    .setChecked(Settings.Secure.getInt(
                            getActivity().getContentResolver(), Settings.Secure.POWER_SAVER_MODE,
                            PowerSaverService.POWER_SAVER_MODE_OFF) == PowerSaverService.POWER_SAVER_MODE_ON);

            mDataMode = (ListPreference) findPreference(PREF_DATA_MODE);
            mDataMode.setOnPreferenceChangeListener(this);
            int dataModeValue = Settings.Secure.getInt(
                    getActivity().getContentResolver(), Settings.Secure.POWER_SAVER_DATA_MODE,
                    PowerSaverService.DATA_UNTOUCHED);
            Log.i(TAG, "data mode value onCreate: " + dataModeValue);
            mDataMode.setValue(Integer.toString(dataModeValue));

            mDataDelay = (ListPreference) findPreference(PREF_DATA_DELAY);
            mDataDelay.setOnPreferenceChangeListener(this);
            mDataDelay.setValue(Settings.Secure.getInt(
                    getActivity().getContentResolver(), Settings.Secure.POWER_SAVER_DATA_DELAY,
                    0) + "");

            mSyncMode = (ListPreference) findPreference(PREF_SYNC_MODE);
            mSyncMode.setOnPreferenceChangeListener(this);
            String mSyncModeValue = Settings.Secure.getInt(
                    getActivity().getContentResolver(), Settings.Secure.POWER_SAVER_SYNC_MODE,
                    PowerSaverService.SYNC_UNTOUCHED) + "";
            // Log.i(TAG, "sync mode value onCreate: " + mSyncModeValue);
            mSyncMode.setValue(mSyncModeValue);

            mSyncInterval = (ListPreference) findPreference(PREF_SYNC_INTERVAL);
            mSyncInterval.setOnPreferenceChangeListener(this);
            mSyncInterval.setValue(Settings.Secure.getInt(
                    getActivity().getContentResolver(), Settings.Secure.POWER_SAVER_SYNC_INTERVAL,
                    3600) + "");
        }

        @Override
        public void onResume() {
            super.onResume();
            refreshSettings();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            if (preference == mPowerSaverEnabled) {
                boolean checked = ((CheckBoxPreference) preference).isChecked();

                int newVal = checked ? PowerSaverService.POWER_SAVER_MODE_ON
                        : PowerSaverService.POWER_SAVER_MODE_OFF;

                Log.i(TAG, "putting: " + newVal);
                Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.POWER_SAVER_MODE, newVal);
                return true;
            }
            refreshSettings();
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean result = false;

            if (preference == mDataMode) {
                int val = Integer.parseInt((String) newValue);
                Log.i(TAG, "new value: " + val);
                result = Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.POWER_SAVER_DATA_MODE, val);

            } else if (preference == mDataDelay) {
                int val = Integer.parseInt((String) newValue);
                Log.i(TAG, "new mDataDelay value: " + val);
                result = Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.POWER_SAVER_DATA_DELAY, val);

            } else if (preference == mSyncMode) {
                int val = Integer.parseInt((String) newValue);
                Log.i(TAG, "new value: " + val);

                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        refreshSettings();
                    }
                }, 500);

                result = Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.POWER_SAVER_SYNC_MODE, val);

            } else if (preference == mSyncInterval) {
                int val = Integer.parseInt((String) newValue);
                Log.i(TAG, "new value: " + val);
                result = Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.POWER_SAVER_SYNC_INTERVAL, val);

            }
            refreshSettings();
            return result;
        }

        private void refreshSettings() {
            // Log.i(TAG, "sync mode val: " + mSyncMode.getValue());
            if (mSyncMode.getValue().equals(Integer.toString(PowerSaverService.SYNC_INTERVAL)))
                mSyncInterval.setEnabled(true);
            else
                mSyncInterval.setEnabled(false);
        }
    }

}
