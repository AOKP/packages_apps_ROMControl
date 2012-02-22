
package com.roman.romcontrol.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;

import org.xml.sax.SAXException;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import org.w3c.dom.Document;

import com.roman.romcontrol.WeatherInfo;
import com.roman.romcontrol.util.WeatherPrefs;
import com.roman.romcontrol.xml.WeatherXmlParser;

public class WeatherService extends IntentService {

    public static final String TAG = "WeatherService";

    public static final String INTENT_REQUEST_WEATHER = "com.aokp.romcontrol.INTENT_WEATHER_REQUEST";
    public static final String INTENT_UPDATE_WEATHER = "com.aokp.romcontrol.INTENT_WEATHER_UPDATE";
    public static final String INTENT_UPDATE_WEATHER_AUTO_OBTAINED = "com.aokp.romcontrol.INTENT_WEATHER_UPDATE";

    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_FORECAST_DATE = "forecast_date";
    public static final String EXTRA_CONDITION = "condition";
    public static final String EXTRA_TEMP = "temp";
    public static final String EXTRA_TEMP_F = "temp_f";
    public static final String EXTRA_TEMP_C = "temp_c";
    public static final String EXTRA_HUMIDITY = "humidity";
    public static final String EXTRA_WIND = "wind";

    private static final String URL_YAHOO_API_WEATHER = "http://weather.yahooapis.com/forecastrss?w=%s&u=c";

    private HttpRetriever httpRetriever = null;

    public WeatherService() {
        super("WeatherService");
        httpRetriever = new HttpRetriever();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WeatherInfo w = null;
        String extra = null;
        String action = intent.getAction();
        String woeid = null;

        if (Settings.System.getInt(getContentResolver(), Settings.System.USE_WEATHER, 0) == 0) {
            return;
        }

        if (action != null && action.equals(INTENT_UPDATE_WEATHER_AUTO_OBTAINED)) {
            Log.i(TAG, "Location updated, sending weather update intent");
            // don't use the bundle as the location extra may be empty. select
            // best provider and use getLastKnownLocation instead
            final LocationManager locationManager = (LocationManager) this
                    .getSystemService(Context.LOCATION_SERVICE);
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_COARSE);
            String bestProvider = locationManager.getBestProvider(crit, true);
            Location loc = null;
            Log.i(TAG, "using " + bestProvider + " provider");
            if (bestProvider != null)
                loc = locationManager.getLastKnownLocation(bestProvider);
            else
                loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            try {
                woeid = YahooPlaceFinder.reverseGeoCode(loc.getLatitude(),
                        loc.getLongitude());
                Log.i(TAG, "got woeid: " + woeid);
                sendBroadcast(parseXml(getDocument(woeid)));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (action != null && action.equals(INTENT_REQUEST_WEATHER)) {
            /*
             * if a zip or location is sent as an extra with the intent, it will
             * use that as the location instead of trying to acquire it via the
             * network
             */
            Log.i(TAG, "Requesting weather data.");
            if (intent.hasExtra(EXTRA_CITY)) {
                extra = intent.getCharSequenceExtra(EXTRA_CITY).toString();
            }
            if (extra != null) {
                woeid = YahooPlaceFinder.GeoCode(extra);
                w = parseXml(getDocument(woeid));
                if (w != null)
                    sendBroadcast(w);
            } else {
                getLocationAndStartService();
            }

        }
    }
    
    private Document getDocument(String woeid) {
        try {
        return httpRetriever.getDocumentFromURL(String.format(URL_YAHOO_API_WEATHER,
                woeid));
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    private WeatherInfo parseXml(Document wDoc) {
        try {
            return new WeatherXmlParser().parseWeatherResponse(wDoc);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Couldn't connect to Google to get weather data.");
        }
        return null;
    }

    private void sendBroadcast(WeatherInfo w) {
        Intent broadcast = new Intent(INTENT_UPDATE_WEATHER);
        try {
            broadcast.putExtra(EXTRA_CITY, w.city);
            broadcast.putExtra(EXTRA_CONDITION, w.condition);
            broadcast.putExtra(EXTRA_FORECAST_DATE, w.forecast_date);
            broadcast.putExtra(EXTRA_HUMIDITY, w.humidity);
            broadcast.putExtra(EXTRA_TEMP_C, w.temp_c);
            broadcast.putExtra(EXTRA_TEMP_F, w.temp_f);
            broadcast.putExtra(EXTRA_WIND, w.wind);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean celcius = WeatherPrefs.getUseCelcius(getApplicationContext());
        if (celcius) {
            broadcast.putExtra(EXTRA_TEMP, w.temp_c + "°C");
        } else {
            broadcast.putExtra(EXTRA_TEMP, w.temp_f + "°F");
        }

        getApplicationContext().sendBroadcast(broadcast);

    }

    private void getLocationAndStartService() {
        boolean useCustomLoc = WeatherPrefs.getUseCustomLocation(getApplicationContext());
        String loc = WeatherPrefs.getCustomLocation(getApplicationContext());

        if (useCustomLoc && !loc.equals("")) {
            Log.i(TAG, "Using custom-set location.");
            Intent i = new Intent(getApplicationContext(), WeatherService.class);
            i.setAction(WeatherService.INTENT_REQUEST_WEATHER);
            i.putExtra(WeatherService.EXTRA_CITY, loc);
            getApplicationContext().startService(i);

        } else if (!useCustomLoc) {
            Log.i(TAG, "Requesting location from network.");
            final LocationManager locationManager = (LocationManager) this
                    .getSystemService(Context.LOCATION_SERVICE);

            Intent i = new Intent(getApplicationContext(), WeatherService.class);
            i.setAction(INTENT_UPDATE_WEATHER_AUTO_OBTAINED);
            PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, i, 0);

            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, pi);
        }
    }

}
