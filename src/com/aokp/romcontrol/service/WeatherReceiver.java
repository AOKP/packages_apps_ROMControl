
package com.aokp.romcontrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.widget.Toast;

import com.android.internal.statusbar.IStatusBarService;

import com.aokp.romcontrol.util.WeatherPrefs;

import java.net.URISyntaxException;

public class WeatherReceiver extends BroadcastReceiver {

    @Override
    public void onReceive (Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(WeatherService.INTENT_REQUEST_WEATHER)) {

            boolean updateweather = true;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String type = extras.getString(Intent.EXTRA_TEXT);
                if (type != null) {
                    if (type.equals("startapp")) {

                        if (WeatherPrefs.getUseCustomApp(context.getApplicationContext())) {
                            Intent appintent = null;
                            try {
                                appintent = Intent.parseUri(WeatherPrefs.getCustomApp(
                                context.getApplicationContext()),0);
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
            if (updateweather) {
                // TODO create a resource string
                Toast.makeText(context, "Requesting weather update!", Toast.LENGTH_LONG).show();
                Intent getWeatherNow = new Intent(context, WeatherService.class);
                getWeatherNow.setAction(action);
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
