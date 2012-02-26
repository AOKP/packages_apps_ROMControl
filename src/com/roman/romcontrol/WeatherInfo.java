
package com.roman.romcontrol;

import android.content.Context;

public class WeatherInfo {

    private static final String NODATA = "No data";

    public String city, forecast_date, condition, condition_code, temp, temp_unit, humidity, wind, speed_unit, low, high;

    private Context mContext;

    public WeatherInfo() {
        this.city = this.forecast_date = this.condition = this.condition_code = this.temp = this.temp_unit = this.humidity = this.wind = this.speed_unit = this.low = this.high = NODATA;
    }

    public WeatherInfo(String city, String fdate, String condition,String condition_code, String temp, String temp_unit, String humidity,
            String wind, String speed_unit, String low, String high) {
        this.city = city;
        this.forecast_date = fdate;
        this.condition = condition;
        this.condition_code = condition_code;
        this.temp = temp + "°" + temp_unit;
        this.temp_unit = temp_unit;
        this.humidity = humidity;
        this.wind = wind + speed_unit;
        this.speed_unit = speed_unit;
        this.low = low + "°" + temp_unit;
        this.high = high + "°" + temp_unit;
    }
}
