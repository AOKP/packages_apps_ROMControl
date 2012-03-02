
package com.roman.romcontrol.service;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import com.roman.romcontrol.service.WeatherService;

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