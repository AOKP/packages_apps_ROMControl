
package com.aokp.romcontrol.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;

import com.aokp.romcontrol.util.WeatherPrefs;

import java.util.Calendar;

public class WeatherRefreshService extends Service {

    public static final String TAG = "WeatherRefreshService";

    Context mContext;
    SharedPreferences prefs;
    AlarmManager alarms;

    PendingIntent weatherRefreshIntent;

    int refreshIntervalInMinutes;

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        prefs = getApplicationContext().getSharedPreferences("weather", MODE_PRIVATE);
        alarms = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        refreshIntervalInMinutes = prefs.getInt(WeatherPrefs.KEY_REFRESH, 0);
        prefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(WeatherPrefs.KEY_REFRESH)) {
                    refreshIntervalInMinutes = WeatherPrefs.getRefreshInterval(mContext);
                    scheduleRefresh();
                }
            }
        });
    }

    private void scheduleRefresh() {

        Intent i = new Intent(getApplicationContext(), WeatherRefreshService.class);
        i.setAction(WeatherService.INTENT_REQUEST_WEATHER);

        weatherRefreshIntent = PendingIntent.getService(getApplicationContext(), 0, i,
                0);

        Calendar timeToStart = Calendar.getInstance();
        timeToStart.setTimeInMillis(System.currentTimeMillis());
        timeToStart.add(Calendar.MINUTE, refreshIntervalInMinutes);

        alarms.set(AlarmManager.RTC, timeToStart.getTimeInMillis(),
                weatherRefreshIntent);
    }

    public void cancelRefresh() {
        if (weatherRefreshIntent != null)
            alarms.cancel(weatherRefreshIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        refreshIntervalInMinutes = WeatherPrefs.getRefreshInterval(mContext);
        if (intent.getAction() != null) {
            if (intent.getAction().equals(WeatherService.INTENT_REQUEST_WEATHER)) {
                Intent i = new Intent(getApplicationContext(), WeatherService.class);
                i.setAction(WeatherService.INTENT_REQUEST_WEATHER);
                getApplicationContext().startService(i);
            }
        }
        cancelRefresh();
        if (refreshIntervalInMinutes != 0) {
            scheduleRefresh();
        }
        
        stopSelf(); // so it won't run in the background eatin up RAM, ^ alarm will restart it
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
