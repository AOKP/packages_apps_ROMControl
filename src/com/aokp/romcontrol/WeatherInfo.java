
package com.aokp.romcontrol;

import android.content.Context;
import android.util.Log;
import java.lang.Class;

public class WeatherInfo {

    private static final String NODATA = "No data";

    public String city, forecast_date, condition, condition_code, temp, temp_unit, humidity, wind,
            wind_dir, speed_unit, low, high, timestamp;

    public WeatherInfo() {
        this.city = this.forecast_date = this.condition = this.condition_code = this.temp = this.temp_unit = this.humidity = this.wind = this.wind_dir = this.speed_unit = this.low = this.high = this.timestamp = NODATA;
    }

    public WeatherInfo(String city, String fdate, String condition, String condition_code,
            String temp, String temp_unit, String humidity,
            String wind, String wind_dir, String speed_unit, String low, String high) {
        this.city = city;
        this.forecast_date = fdate;
        this.condition = condition;
        this.condition_code = condition_code;
        this.temp = temp + "°" + temp_unit;
        this.temp_unit = temp_unit;
        this.humidity = humidity + "%";
        this.wind = calcDirection(wind_dir) + " " + trimSpeed(wind) + speed_unit;
        this.speed_unit = speed_unit;
        this.low = low + "°" + temp_unit;
        this.high = high + "°" + temp_unit;
        this.timestamp = "";
    }

    /**
     * find the optimal weather string (helper function for translation)
     * 
     * @param conditionCode condition code from Yahoo (this is the main
     *            identifier which will be used to find a matching translation
     *            in the project's resources
     * @param providedString
     * @return either the defaultString (which should be Yahoo's weather
     *         condition text), or the translated version from resources
     */
    public static String getTranslatedConditionString(Context mContext, int conditionCode,
            String providedString) {
        int resID = mContext.getResources().getIdentifier("weather_" + conditionCode, "string",
                mContext.getPackageName());
        return (resID != 0) ? mContext.getResources().getString(resID) : providedString;
    }

    private String calcDirection(String degrees) {
        int deg = Integer.parseInt(degrees);
        if (deg >= 338 || deg <= 22)
            return "N";
        else if (deg < 68)
            return "NE";
        else if (deg < 113)
            return "E";
        else if (deg < 158)
            return "SE";
        else if (deg < 203)
            return "S";
        else if (deg < 248)
            return "SW";
        else if (deg < 293)
            return "W";
        else if (deg < 338)
            return "NW";
        else
            return "";
    }

    private String trimSpeed(String speed) {
        return String.valueOf(Math.round(Float.parseFloat(speed)));
    }
}
