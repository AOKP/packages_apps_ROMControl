
package com.aokp.romcontrol.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;

import com.aokp.romcontrol.weather.WeatherRefreshService;
import com.aokp.romcontrol.weather.WeatherService;

public class BootService extends Service {

    public static boolean servicesStarted = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new BootWorker().execute();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class BootWorker extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Context c = getApplicationContext();

            if (HeadphoneService.getUserHeadphoneAudioMode(c) != -1
                    || HeadphoneService.getUserBTAudioMode(c) != -1) {
                c.startService(new Intent(c, HeadphoneService.class));
            }

            if (FlipService.getUserFlipAudioMode(c) != -1)
                c.startService(new Intent(c, FlipService.class));

            if (Settings.System
                    .getBoolean(getContentResolver(), Settings.System.USE_WEATHER, false)) {
                sendLastWeatherBroadcast();
                getApplicationContext().startService(new Intent(c, WeatherRefreshService.class));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            servicesStarted = true;
            stopSelf();
        }

    }

    private void sendLastWeatherBroadcast() {
        SharedPreferences settings =
                getApplicationContext().getSharedPreferences(WeatherService.PREFS_NAME, 0);

        Intent broadcast = new Intent(WeatherService.INTENT_WEATHER_UPDATE);
        try {
            broadcast.putExtra(WeatherService.EXTRA_CITY, settings.getString("city", ""));
            broadcast.putExtra(WeatherService.EXTRA_CONDITION, settings.getString("condition", ""));
            broadcast.putExtra(WeatherService.EXTRA_LAST_UPDATE,
                    settings.getString("timestamp", ""));
            broadcast.putExtra(WeatherService.EXTRA_CONDITION_CODE,
                    settings.getString("condition_code", ""));
            broadcast.putExtra(WeatherService.EXTRA_FORECAST_DATE,
                    settings.getString("forecast_date", ""));
            broadcast.putExtra(WeatherService.EXTRA_HUMIDITY, settings.getString("humidity", ""));
            broadcast.putExtra(WeatherService.EXTRA_TEMP, settings.getString("temp", ""));
            broadcast.putExtra(WeatherService.EXTRA_WIND, settings.getString("wind", ""));
            broadcast.putExtra(WeatherService.EXTRA_LOW, settings.getString("low", ""));
            broadcast.putExtra(WeatherService.EXTRA_HIGH, settings.getString("high", ""));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        getApplicationContext().sendBroadcast(broadcast);
    }

}
