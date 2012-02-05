
package com.roman.romcontrol.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.content.Context;
import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;
import android.widget.Toast;

import com.roman.romcontrol.WeatherInfo;

public class WeatherXmlParser {

    protected static final String TAG = "WeatherXmlParser";

    private static String googleWeatherPrefix = "http://www.google.com/ig/api?weather=";
    private Context context;
    private final URL feedUrl;

    public WeatherXmlParser(Context c, String zipOrCity) throws MalformedURLException {
        context = c;

        this.feedUrl = new URL(googleWeatherPrefix + zipOrCity);
    }

    protected InputStream getInputStream() {
        try {
            // Log.i("bloater:xmlparser", "returning inputstream");
            return feedUrl.openConnection().getInputStream();
        } catch (IOException e) {
            // TODO handle if there's no connectivity
            // throw new RuntimeException(e);
            return null;
        }
    }

    public WeatherInfo parse() throws IOException, SAXException {
        final WeatherInfo w = new WeatherInfo(context);

        RootElement root = new RootElement("xml_api_reply");
        Element weatherRoot = root.getChild("weather");
        Element forecastInformation = weatherRoot.getChild("forecast_information");
        Element currentConditions = weatherRoot.getChild("current_conditions");

        forecastInformation.getChild("city").setStartElementListener(
                new StartElementListener() {

                    @Override
                    public void start(Attributes attributes) {
                        w.city = attributes.getValue("data");
                    }
                });

        forecastInformation.getChild("postal_code").setStartElementListener(
                new StartElementListener() {

                    @Override
                    public void start(Attributes attributes) {
                        w.postal_code = attributes.getValue("data");
                    }
                });

        forecastInformation.getChild("forecast_date").setStartElementListener(
                new StartElementListener() {

                    @Override
                    public void start(Attributes attributes) {
                        w.forecast_date = attributes.getValue("data");
                    }
                });

        currentConditions.getChild("condition").setStartElementListener(
                new StartElementListener() {

                    @Override
                    public void start(Attributes attributes) {
                        w.condition = attributes.getValue("data");
                    }
                });

        currentConditions.getChild("temp_f").setStartElementListener(
                new StartElementListener() {

                    @Override
                    public void start(Attributes attributes) {
                        w.temp_f = attributes.getValue("data");
                    }
                });

        currentConditions.getChild("temp_c").setStartElementListener(
                new StartElementListener() {

                    @Override
                    public void start(Attributes attributes) {
                        w.temp_c = attributes.getValue("data");
                    }
                });

        currentConditions.getChild("humidity").setStartElementListener(
                new StartElementListener() {

                    @Override
                    public void start(Attributes attributes) {
                        w.humidify = attributes.getValue("data");
                    }
                });

        currentConditions.getChild("wind").setStartElementListener(
                new StartElementListener() {

                    @Override
                    public void start(Attributes attributes) {
                        w.wind = attributes.getValue("data");
                    }
                });

        Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8,
                root.getContentHandler());

        return w;

    }
}
