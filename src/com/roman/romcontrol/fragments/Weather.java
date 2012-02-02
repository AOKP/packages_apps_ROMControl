
package com.roman.romcontrol.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.roman.romcontrol.R;
import com.roman.romcontrol.SettingsPreferenceFragment;
import com.roman.romcontrol.service.WeatherRefreshService;
import com.roman.romcontrol.service.WeatherService;

public class Weather extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    public static final String TAG = "Weather";

    Preference mGetWeather;
    ListPreference mWeatherSyncInterval;

    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_weather);

        prefs = getActivity().getSharedPreferences("weather", Context.MODE_PRIVATE);

        mGetWeather = findPreference("get_weather");
        mWeatherSyncInterval = (ListPreference) findPreference("refresh_interval");
        mWeatherSyncInterval.setOnPreferenceChangeListener(this);

        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        LocationProvider locationProvider = locationManager
                .getProvider(LocationManager.NETWORK_PROVIDER);

        Intent i = new Intent(getActivity().getApplicationContext(), WeatherRefreshService.class);
        getActivity().getApplicationContext().startService(i);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mGetWeather) {
            Intent i = new Intent(getActivity().getApplicationContext(), WeatherService.class);
            getActivity().getApplicationContext().startService(i);
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
            Log.i("new val", newVal + "");
            return true;
        }
        return false;
    }

}
