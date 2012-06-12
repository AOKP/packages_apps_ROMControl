
package com.aokp.romcontrol.service;

import java.net.URISyntaxException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.widget.Toast;

import com.android.internal.statusbar.IStatusBarService;
import com.aokp.romcontrol.util.WeatherPrefs;
import com.aokp.romcontrol.R;

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
                if (type != null) {
                    if (type.equals("startapp")) {
                        if (WeatherPrefs.getUseCustomApp(context.getApplicationContext())) {
                            Intent appintent = null;
                            try {
                                appintent = Intent.parseUri(WeatherPrefs.getCustomApp(
                                        context.getApplicationContext()), 0);
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                            if (appintent != null) {
                                appintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                updateweather = false;
                                context.startActivity(appintent);
                                collapseStatusBar();
                            }
                        }
                    }
                }
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
            sb.collapse();
        } catch (RemoteException e) {
        }
    }

}
