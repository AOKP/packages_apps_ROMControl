
package com.aokp.romcontrol.weather;

import android.content.Context;
import android.net.Uri;

public class YahooPlaceFinder {

    private static final String YAHOO_API_BASE_URL = "http://query.yahooapis.com/v1/public/yql?q=" +
            Uri.encode("select woeid from geo.placefinder where text =");

    public static String reverseGeoCode(Context c, double latitude, double longitude) {

        String formattedCoordinates = String.format("\"%s %s\" and gflags=\"R\"",
                String.valueOf(latitude), String.valueOf(longitude));
        String url = YAHOO_API_BASE_URL + Uri.encode(formattedCoordinates);
        String response = new HttpRetriever().retrieve(url);
        return new WeatherXmlParser(c).parsePlaceFinderResponse(response);

    }

    public static String GeoCode(Context c, String location) {
        String url = YAHOO_API_BASE_URL + Uri.encode(String.format("\"%s\"",location));
        String response = new HttpRetriever().retrieve(url);
        return new WeatherXmlParser(c).parsePlaceFinderResponse(response);
    }

}
