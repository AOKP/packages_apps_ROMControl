
package com.aokp.romcontrol.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import com.aokp.romcontrol.WeatherInfo;
import com.aokp.romcontrol.util.WeatherPrefs;
import com.aokp.romcontrol.xml.WeatherXmlParser;

import org.w3c.dom.Document;

import java.io.IOException;

public class WeatherService extends IntentService {

    public static final String TAG = "WeatherService";

    public static final String INTENT_REQUEST_WEATHER = "com.aokp.romcontrol.INTENT_WEATHER_REQUEST";
    public static final String INTENT_UPDATE_WEATHER = "com.aokp.romcontrol.INTENT_WEATHER_UPDATE";

    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_FORECAST_DATE = "forecast_date";
    public static final String EXTRA_CONDITION = "condition";
    public static final String EXTRA_CONDITION_CODE = "condition_code";
    public static final String EXTRA_TEMP = "temp";
    public static final String EXTRA_HUMIDITY = "humidity";
    public static final String EXTRA_WIND = "wind";
    public static final String EXTRA_LOW = "todays_low";
    public static final String EXTRA_HIGH = "todays_high";

    private static final String URL_YAHOO_API_WEATHER = "http://weather.yahooapis.com/forecastrss?w=%s&u=";

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
            stopSelf();
            return;
        }
        
        if (action != null && action.equals(INTENT_REQUEST_WEATHER)) {
            // custom location
            boolean useCustomLoc = WeatherPrefs.getUseCustomLocation(getApplicationContext());
            String customLoc = WeatherPrefs.getCustomLocation(getApplicationContext());
            if (customLoc != null && useCustomLoc) {
                woeid = YahooPlaceFinder.GeoCode(customLoc);
                // network location
            } else {
                final LocationManager locationManager = (LocationManager) this
                        .getSystemService(Context.LOCATION_SERVICE);
                if (!intent.hasExtra("newlocation")) {
                    intent.putExtra("newlocation", true);
                    PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, intent,
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, pi);
                    return;
                }

                Criteria crit = new Criteria();
                crit.setAccuracy(Criteria.ACCURACY_COARSE);
                String bestProvider = locationManager.getBestProvider(crit, true);
                Location loc = null;
                if (bestProvider != null) {
                    loc = locationManager.getLastKnownLocation(bestProvider);
                } else {
                    loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                }
                try {
                    woeid = YahooPlaceFinder.reverseGeoCode(loc.getLatitude(),
                            loc.getLongitude());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                w = parseXml(getDocument(woeid));
                if (w != null) {
                    sendBroadcast(w);
                }
            } catch (Exception e) {
                Log.e(TAG, "ohnoes: " + e.getMessage());
            }
        }
        stopSelf();
    }

    private Document getDocument(String woeid) {
        try {
            boolean celcius = WeatherPrefs.getUseCelcius(getApplicationContext());
            String urlWithDegreeUnit;
            if (celcius) {
                urlWithDegreeUnit = URL_YAHOO_API_WEATHER + "c";
            } else {
                urlWithDegreeUnit = URL_YAHOO_API_WEATHER + "f";
            }
            return httpRetriever.getDocumentFromURL(String.format(urlWithDegreeUnit,
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
            Log.w(TAG, "Couldn't connect to Yahoo to get weather data.");
        }
        return null;
    }

    private void sendBroadcast(WeatherInfo w) {
        Intent broadcast = new Intent(INTENT_UPDATE_WEATHER);
        try {
            broadcast.putExtra(EXTRA_CITY, w.city);
            broadcast.putExtra(EXTRA_CONDITION, w.condition);
            broadcast.putExtra(EXTRA_CONDITION_CODE, w.condition_code);
            broadcast.putExtra(EXTRA_FORECAST_DATE, w.forecast_date);
            broadcast.putExtra(EXTRA_HUMIDITY, w.humidity);
            broadcast.putExtra(EXTRA_TEMP, w.temp);
            broadcast.putExtra(EXTRA_WIND, w.wind);
            broadcast.putExtra(EXTRA_LOW, w.low);
            broadcast.putExtra(EXTRA_HIGH, w.high);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getApplicationContext().sendBroadcast(broadcast);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
