
package com.aokp.romcontrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WeatherReceiver extends BroadcastReceiver {
    @Override
    public void onReceive (Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(WeatherService.INTENT_REQUEST_WEATHER)) {
            Intent getWeatherNow = new Intent(context, WeatherService.class);
            getWeatherNow.setAction(action);
            context.startService(getWeatherNow);
        }
    }
}