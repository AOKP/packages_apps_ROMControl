
package com.aokp.romcontrol.util;

import android.content.Context;
import android.content.SharedPreferences;

public class WeatherPrefs {

    public static final String PREF_NAME = "aokp_weather";

    public static final String KEY_USE_CELCIUS = "use_celcius";
    public static final String KEY_REFRESH = "refresh_interval";
    public static final String KEY_USE_CUSTOM_LOCATION = "use_custom_location";
    public static final String KEY_CUSTOM_LOCATION = "custom_location";

    public static int getRefreshInterval(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_REFRESH, 60);
    }

    public static boolean setRefreshInterval(Context c, int interval) {
        SharedPreferences prefs = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.edit().putInt(KEY_REFRESH, interval).commit();
    }

    public static String getCustomLocation(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CUSTOM_LOCATION, "");
    }

    public static boolean setCustomLocation(Context c, String loc) {
        SharedPreferences prefs = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.edit().putString(KEY_CUSTOM_LOCATION, loc).commit();
    }

    public static boolean getUseCustomLocation(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_USE_CUSTOM_LOCATION, false);
    }

    public static boolean setUseCustomLocation(Context c, boolean use) {
        SharedPreferences prefs = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.edit().putBoolean(KEY_USE_CUSTOM_LOCATION, use).commit();
    }

    public static boolean getUseCelcius(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_USE_CELCIUS, false);
    }

    public static boolean setUseCelcius(Context c, boolean use) {
        SharedPreferences prefs = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.edit().putBoolean(KEY_USE_CELCIUS, use).commit();
    }

}
