
package com.aokp.romcontrol.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;

import com.android.internal.statusbar.IStatusBarService;

import java.net.URISyntaxException;

public class WeatherReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(WeatherService.INTENT_WEATHER_REQUEST)) {

            boolean updateweather = true;
            boolean manual = false;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String type = extras.getString(WeatherService.INTENT_EXTRA_TYPE, "updateweather");
                manual = extras.getBoolean(WeatherService.INTENT_EXTRA_ISMANUAL, false);

            }

            // SystemUI sends the broadcast to update weather upon booting up,
            // make sure we want to refresh it
            if (updateweather
                    && Settings.System.getInt(context.getContentResolver(),
                            Settings.System.USE_WEATHER, 0) != 0) {
                Intent getWeatherNow = new Intent(context, WeatherService.class);
                getWeatherNow.setAction(action);
                getWeatherNow.putExtra(WeatherService.INTENT_EXTRA_ISMANUAL, manual);
                context.startService(getWeatherNow);
            }
        }
    }

    private void collapseStatusBar() {
        try {
            IStatusBarService sb = IStatusBarService.Stub.asInterface(
                    ServiceManager.getService(Context.STATUS_BAR_SERVICE));
            sb.collapsePanels();
        } catch (RemoteException e) {
        }
    }

}
