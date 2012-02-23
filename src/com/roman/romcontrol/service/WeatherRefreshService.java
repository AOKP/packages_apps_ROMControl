
package com.roman.romcontrol.service;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.roman.romcontrol.util.WeatherPrefs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

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
        // Log.i("Refresher", "service started with refresh: " + refreshIntervalInMinutes);
        prefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(WeatherPrefs.KEY_REFRESH)) {
                    refreshIntervalInMinutes = WeatherPrefs.getRefreshInterval(mContext);
                    // Log.i("Refresher", "new value: " + refreshIntervalInMinutes);
                    scheduleRefresh();
                }
                // Log.i("Refresher", "new key: " + key);
            }
        });
    }

    private void scheduleRefresh() {
        cancelRefresh();
        if (refreshIntervalInMinutes == 0) {
            Log.i(TAG, "Did not schedule refresh.");
            return;
        }

        // Log.i(TAG, "scheduling with refresh interval : " + refreshIntervalInMinutes + " minutes");

        Intent i = new Intent(getApplicationContext(), WeatherRefreshService.class);
        i.setAction(WeatherService.INTENT_REQUEST_WEATHER);

        weatherRefreshIntent = PendingIntent.getService(getApplicationContext(), 0, i,
                0);

        Calendar timeToStart = Calendar.getInstance();
        timeToStart.setTimeInMillis(System.currentTimeMillis());
        timeToStart.add(Calendar.MINUTE, 1);

        long interval = TimeUnit.MILLISECONDS.convert(refreshIntervalInMinutes,
                TimeUnit.MINUTES);

        alarms.setInexactRepeating(AlarmManager.RTC, timeToStart.getTimeInMillis(), interval,
                weatherRefreshIntent);
        stopSelf(); // so it won't run in the background eatin up RAM, ^ alarm will restart it
    }

    public void cancelRefresh() {
        if (weatherRefreshIntent != null)
            alarms.cancel(weatherRefreshIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Log.i("LocalService", "Received start id " + startId + ": " +
        // intent);
        refreshIntervalInMinutes = WeatherPrefs.getRefreshInterval(mContext);
        if (intent.getAction() != null) {
            if (intent.getAction().equals(WeatherService.INTENT_REQUEST_WEATHER)) {
                Intent i = new Intent(getApplicationContext(), WeatherService.class);
                i.setAction(WeatherService.INTENT_REQUEST_WEATHER);
                getApplicationContext().startService(i);
            }
        }
        if (weatherRefreshIntent == null)
            scheduleRefresh();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
