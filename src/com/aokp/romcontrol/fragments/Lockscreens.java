package com.aokp.romcontrol.fragments;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

public class Lockscreens extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_LOCKSCREEN_BATTERY = "lockscreen_battery";

    CheckBoxPreference mLockscreenBattery;

    ArrayList<String> keys = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keys.add(Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_lockscreens);

        mLockscreenBattery = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_BATTERY);
        mLockscreenBattery.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_BATTERY, 0) == 1);

        for (String key : keys) {
            try {
                ((CheckBoxPreference) findPreference(key)).setChecked(Settings.System.getInt(
                        getActivity().getContentResolver(), key) == 1);
             } catch (SettingNotFoundException e) {
             }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLockscreenBattery) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_BATTERY,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        } else if (keys.contains(preference.getKey())) {
            Log.e("RC_Lockscreens", "key: " + preference.getKey());
            return Settings.System.putInt(getActivity().getContentResolver(), preference.getKey(),
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

}
