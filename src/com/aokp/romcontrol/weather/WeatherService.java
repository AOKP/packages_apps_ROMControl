
package com.aokp.romcontrol.weather;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;

import org.w3c.dom.Document;

import java.io.IOException;

public class WeatherService extends IntentService {
    Handler mMainThreadHandler = null;

    public static final String TAG = "WeatherService";

    public static final String PREFS_NAME = "WeatherServicePreferences";

    public static final String INTENT_WEATHER_REQUEST = "com.aokp.romcontrol.INTENT_WEATHER_REQUEST";
    public static final String INTENT_WEATHER_UPDATE = "com.aokp.romcontrol.INTENT_WEATHER_UPDATE";
    public static final String INTENT_EXTRA_ISMANUAL = "com.aokp.romcontrol.INTENT_EXTRA_ISMANUAL";
    public static final String INTENT_EXTRA_TYPE = "com.aokp.romcontrol.INTENT_EXTRA_TYPE";
    public static final String INTENT_EXTRA_NEWLOCATION = "com.aokp.romcontrol.INTENT_EXTRA_NEWLOCATION";

    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_FORECAST_DATE = "forecast_date";
    public static final String EXTRA_CONDITION = "condition";
    public static final String EXTRA_LAST_UPDATE = "datestamp";
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

        mMainThreadHandler = new Handler();
    }

    // Fix for a stupid AsyncTask bug
    // See http://code.google.com/p/android/issues/detail?id=20915
    private void makeToast(final String msg) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Helpers.msgShort(getApplicationContext(), msg);
            }
        });
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WeatherInfo w = null;
        String extra = null;
        String action = intent.getAction();
        String woeid = null;
        Context context = getApplicationContext();

        if (Settings.System.getInt(getContentResolver(), Settings.System.USE_WEATHER, 0) == 0) {
            stopSelf();
            return;
        }

        if (!Settings.Secure.isLocationProviderEnabled(
                getContentResolver(), LocationManager.NETWORK_PROVIDER)
                && !WeatherPrefs.getUseCustomLocation(getApplicationContext())) {
            stopSelf();
            return;
        }

        if (action != null && action.equals(INTENT_WEATHER_REQUEST)) {
            // custom location
            boolean useCustomLoc = WeatherPrefs.getUseCustomLocation(getApplicationContext());
            String customLoc = WeatherPrefs.getCustomLocation(getApplicationContext());
            boolean manual = false;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                manual = extras.getBoolean(INTENT_EXTRA_ISMANUAL, false);
            }
            if (customLoc != null && useCustomLoc) {
                woeid = YahooPlaceFinder.GeoCode(getApplicationContext(), customLoc);
                // network location
            } else {
                // do not attempt to get a location without data
                boolean networkAvailable = Helpers.isNetworkAvailable(getApplicationContext());
                if (networkAvailable) {
                    final LocationManager locationManager = (LocationManager) this
                            .getSystemService(Context.LOCATION_SERVICE);

                    Criteria crit = new Criteria();
                    crit.setAccuracy(Criteria.ACCURACY_COARSE);
                    String bestProvider = locationManager.getBestProvider(crit, true);

                    if (!intent.hasExtra(INTENT_EXTRA_NEWLOCATION)) {
                        intent.putExtra(INTENT_EXTRA_NEWLOCATION, true);
                        PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0,
                                intent,
                                PendingIntent.FLAG_CANCEL_CURRENT);
                        if (bestProvider != null) {
                            locationManager.requestSingleUpdate(bestProvider, pi);
                        } else {
                            if (manual) {
                                makeToast(context.getString(R.string.location_unavailable));
                            }
                        }
                        return;
                    }

                    Location loc = null;
                    if (bestProvider != null) {
                        loc = locationManager.getLastKnownLocation(bestProvider);
                    } else {
                        loc = locationManager
                                .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    }
                    try {
                        woeid = YahooPlaceFinder.reverseGeoCode(getApplicationContext(),
                                loc.getLatitude(),
                                loc.getLongitude());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (manual) {
                        makeToast(context.getString(R.string.location_unavailable));
                    }
                    stopSelf();
                    return;
                }
            }
            try {
                w = parseXml(getDocument(woeid));
                if (w != null) {
                    sendBroadcast(w);
                    updateLatest(w);
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
            return new WeatherXmlParser(getApplicationContext()).parseWeatherResponse(wDoc);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Couldn't connect to Yahoo to get weather data.");
        }
        return null;
    }

    private void sendBroadcast(WeatherInfo w) {
        Intent broadcast = new Intent(INTENT_WEATHER_UPDATE);
        w.timestamp = Helpers.getTimestamp(getApplicationContext());
        try {
            broadcast.putExtra(EXTRA_CITY, w.city);
            broadcast.putExtra(EXTRA_CONDITION, w.condition);
            broadcast.putExtra(EXTRA_LAST_UPDATE, w.timestamp);
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

    private void updateLatest(WeatherInfo w) {
        SharedPreferences settings =
                getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("city", w.city);
        editor.putString("condition", w.condition);
        editor.putString("timestamp", w.timestamp);
        editor.putString("condition_code", w.condition_code);
        editor.putString("forecast_date", w.forecast_date);
        editor.putString("humidity", w.humidity);
        editor.putString("temp", w.temp);
        editor.putString("wind", w.wind);
        editor.putString("low", w.low);
        editor.putString("high", w.high);

        editor.commit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
