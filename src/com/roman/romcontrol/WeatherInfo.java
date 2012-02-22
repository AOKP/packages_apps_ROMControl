
package com.roman.romcontrol;

import android.content.Context;

public class WeatherInfo {

    private static final String NODATA = "No data";

    public String city, forecast_date, condition, temp_f, temp_c, humidity, wind;

    private Context mContext;

    public WeatherInfo() {
        this.city = this.forecast_date = this.condition = this.temp_f = this.temp_c = this.humidity = this.wind = NODATA;
    }

    public WeatherInfo(String city, String fdate, String condition, String temp_c, String humidity,
            String wind) {
        this.city = city;
        this.forecast_date = fdate;
        this.condition = condition;
        this.temp_c = temp_c;
        this.temp_f = convertC2F(temp_c);
        this.humidity = humidity;
        this.wind = wind;
    }

    public static String convertC2F(String temp_c) {
        if (temp_c == null)
            return "";
        int celsius = Integer.parseInt(temp_c);
        int fahrenheit = (celsius * 9) / 5 + 32;
        return String.valueOf(fahrenheit);
    }

}
