
package com.roman.romcontrol.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.roman.romcontrol.R;
import com.roman.romcontrol.SettingsPreferenceFragment;
import com.roman.romcontrol.service.WeatherRefreshService;
import com.roman.romcontrol.service.WeatherService;

public class Weather extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    public static final String TAG = "Weather";

    CheckBoxPreference mEnableWeather;
    CheckBoxPreference mUseCustomLoc;
    ListPreference mWeatherSyncInterval;
    EditTextPreference mCustomWeatherLoc;

    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_weather);

        prefs = getActivity().getSharedPreferences("weather", Context.MODE_PRIVATE);

        mWeatherSyncInterval = (ListPreference) findPreference("refresh_interval");
        mWeatherSyncInterval.setOnPreferenceChangeListener(this);
        mWeatherSyncInterval.setSummary(Integer.toString(prefs.getInt(
                WeatherRefreshService.KEY_REFRESH, 0)) + " minutes");

        mCustomWeatherLoc = (EditTextPreference) findPreference("custom_location");
        mCustomWeatherLoc.setOnPreferenceChangeListener(this);
        mCustomWeatherLoc
                .setSummary(prefs.getString(WeatherRefreshService.KEY_CUSTOM_LOCATION, ""));

        mEnableWeather = (CheckBoxPreference) findPreference("enable_weather");
        mEnableWeather.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.USE_WEATHER, 0) == 1);

        mUseCustomLoc = (CheckBoxPreference) findPreference(WeatherRefreshService.KEY_USE_CUSTOM_LOCATION);
        mUseCustomLoc.setChecked(prefs.getBoolean(WeatherRefreshService.KEY_USE_CUSTOM_LOCATION,
                false));

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.weather, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.get_weather:
                Intent i = new Intent(getActivity().getApplicationContext(), WeatherService.class);
                i.setAction(WeatherService.INTENT_REQUEST_WEATHER);
                getActivity().getApplicationContext().startService(i);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mEnableWeather) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.USE_WEATHER,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mUseCustomLoc) {
            prefs.edit().putBoolean(WeatherRefreshService.KEY_USE_CUSTOM_LOCATION,
                    ((CheckBoxPreference) preference).isChecked()).commit();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mWeatherSyncInterval) {
            int newVal = Integer.parseInt((String) newValue);
            prefs.edit().putInt(WeatherRefreshService.KEY_REFRESH,
                    newVal).commit();
            preference.setSummary(newValue + " minutes");
            Log.i("new val", newVal + "");
            return true;

        } else if (preference == mCustomWeatherLoc) {
            String newVal = (String) newValue;
            prefs.edit().putString(WeatherRefreshService.KEY_CUSTOM_LOCATION,
                    newVal).commit();
            Intent i = new Intent(getActivity().getApplicationContext(),
                    WeatherRefreshService.class);
            getActivity().getApplicationContext().startService(i);
            preference.setSummary(newVal);
            return true;

        }
        return false;
    }

}
