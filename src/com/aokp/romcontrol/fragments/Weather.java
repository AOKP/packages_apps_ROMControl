
package com.aokp.romcontrol.fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.service.WeatherRefreshService;
import com.aokp.romcontrol.service.WeatherService;
import com.aokp.romcontrol.util.WeatherPrefs;
import com.aokp.romcontrol.R;

public class Weather extends AOKPPreferenceFragment implements OnPreferenceChangeListener {

    public static final String TAG = "Weather";

    CheckBoxPreference mEnableWeather;
    CheckBoxPreference mUseCustomLoc;
    CheckBoxPreference mShowLoc;
    CheckBoxPreference mUseCelcius;
    ListPreference mStatusBarLocation;
    ListPreference mWeatherSyncInterval;
    EditTextPreference mCustomWeatherLoc;

    SharedPreferences prefs;

    private static final int LOC_WARNING = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_weather);

        prefs = getActivity().getSharedPreferences("weather", Context.MODE_PRIVATE);

        mWeatherSyncInterval = (ListPreference) findPreference("refresh_interval");
        mWeatherSyncInterval.setOnPreferenceChangeListener(this);
        mWeatherSyncInterval.setSummary(Integer.toString(WeatherPrefs.getRefreshInterval(mContext))
                + " minutes");

        mStatusBarLocation = (ListPreference) findPreference("statusbar_location");
        mStatusBarLocation.setOnPreferenceChangeListener(this);
        mStatusBarLocation.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.WEATHER_STATUSBAR_STYLE, 1) + "");

        mCustomWeatherLoc = (EditTextPreference) findPreference("custom_location");
        mCustomWeatherLoc.setOnPreferenceChangeListener(this);
        mCustomWeatherLoc
                .setSummary(WeatherPrefs.getCustomLocation(mContext));

        mEnableWeather = (CheckBoxPreference) findPreference("enable_weather");
        mEnableWeather.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.USE_WEATHER, 0) == 1);

        mUseCustomLoc = (CheckBoxPreference) findPreference(WeatherPrefs.KEY_USE_CUSTOM_LOCATION);
        mUseCustomLoc.setChecked(WeatherPrefs.getUseCustomLocation(mContext));

        mShowLoc = (CheckBoxPreference) findPreference("show_location");
        mShowLoc.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.WEATHER_SHOW_LOCATION, 0) == 1);

        mUseCelcius = (CheckBoxPreference) findPreference(WeatherPrefs.KEY_USE_CELCIUS);
        mUseCelcius.setChecked(WeatherPrefs.getUseCelcius(mContext));

        setHasOptionsMenu(true);

        if (!Settings.Secure.isLocationProviderEnabled(
                getContentResolver(), LocationManager.NETWORK_PROVIDER)
                && !mUseCustomLoc.isChecked()) {
            showDialog(LOC_WARNING);
        }

    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        LayoutInflater factory = LayoutInflater.from(mContext);

        switch (dialogId) {
            case LOC_WARNING:
                return new AlertDialog.Builder(getActivity())
                        .setTitle("Cannot retrieve location!")
                        .setMessage(
                                "Obtaining location from network is currently disabled. This breaks the ability to get the current weather in your current location. Please set a custom location or enable obtaining your location from your network.")
                        .setCancelable(false)
                        .setPositiveButton("Enable network location",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Settings.Secure.setLocationProviderEnabled(
                                                getContentResolver(),
                                                LocationManager.NETWORK_PROVIDER, true);
                                    }
                                })
                        .setNegativeButton("Don't enable.", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }).create();
        }
        return null;
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
                Intent i = new Intent(getActivity().getApplicationContext(),
                        WeatherRefreshService.class);
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
            // _stop_ alarm or start service
            boolean check = ((CheckBoxPreference) preference).isChecked();
            Intent i = new Intent(getActivity().getApplicationContext(),
                    WeatherRefreshService.class);
            i.setAction(WeatherService.INTENT_REQUEST_WEATHER);
            PendingIntent weatherRefreshIntent = PendingIntent.getService(getActivity(), 0, i, 0);
            if (!check) {
                AlarmManager alarms = (AlarmManager) getActivity().getSystemService(
                        Context.ALARM_SERVICE);
                alarms.cancel(weatherRefreshIntent);
            } else {
                getActivity().startService(i);
            }
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.USE_WEATHER,
                    check ? 1 : 0);
            return true;
        } else if (preference == mUseCustomLoc) {
            return WeatherPrefs.setUseCustomLocation(mContext,
                    ((CheckBoxPreference) preference).isChecked());
        } else if (preference == mShowLoc) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.WEATHER_SHOW_LOCATION,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mUseCelcius) {
            return WeatherPrefs.setUseCelcius(mContext,
                    ((CheckBoxPreference) preference).isChecked());
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mWeatherSyncInterval) {
            int newVal = Integer.parseInt((String) newValue);
            preference.setSummary(newValue + " minutes");

            return WeatherPrefs.setRefreshInterval(mContext, newVal);

        } else if (preference == mCustomWeatherLoc) {

            String newVal = (String) newValue;

            Intent i = new Intent(getActivity().getApplicationContext(),
                    WeatherRefreshService.class);
            getActivity().getApplicationContext().startService(i);
            preference.setSummary(newVal);
            return WeatherPrefs.setCustomLocation(mContext, newVal);

        } else if (preference == mStatusBarLocation) {

            String newVal = (String) newValue;
            return Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.WEATHER_STATUSBAR_STYLE,
                    Integer.parseInt(newVal));
        }
        return false;
    }

}
