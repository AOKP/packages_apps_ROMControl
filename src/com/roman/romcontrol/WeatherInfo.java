
package com.roman.romcontrol;

import android.content.Context;

public class WeatherInfo {

    public String city, postal_code, forecast_date, condition, temp_f, temp_c, humidify, wind;
    public String todaysLow, todaysHigh;

    private Context mContext;

    public WeatherInfo(Context c) {
        mContext = c;
    }

    public WeatherInfo(Context c, String city) {
        mContext = c;
        this.city = city;
    }

    public WeatherInfo(Context c, int postalCode) {
        mContext = c;
        postal_code = Integer.toString(postalCode);
    }

}
