
package com.aokp.romcontrol.service;

import com.aokp.romcontrol.xml.WeatherXmlParser;

public class YahooPlaceFinder {

    private static final String YAHOO_API_BASE_REV_URL = "http://where.yahooapis.com/geocode?appid=jYkTZp64&q=%1$s,+%2$s&gflags=R";
    private static final String YAHOO_API_BASE_URL = "http://where.yahooapis.com/geocode?appid=jYkTZp64&q=%1$s";

    private static HttpRetriever httpRetriever = new HttpRetriever();
    private static WeatherXmlParser xmlParser = new WeatherXmlParser();

    public static String reverseGeoCode(double latitude, double longitude) {

        String url = String.format(YAHOO_API_BASE_REV_URL, String.valueOf(latitude),
                String.valueOf(longitude));
        String response = httpRetriever.retrieve(url);
        return xmlParser.parsePlaceFinderResponse(response);

    }
    
    public static String GeoCode(String location) {
        String url = String.format(YAHOO_API_BASE_URL, location).replace(' ', '+');
        String response = httpRetriever.retrieve(url);
        return xmlParser.parsePlaceFinderResponse(response);
    }

}
