
package com.aokp.romcontrol.weather;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class HttpRetriever {

    private final String TAG = getClass().getSimpleName();

    private DefaultHttpClient client = new DefaultHttpClient();
    private HttpURLConnection httpConnection;

    public String retrieve(String url) {

        HttpGet get = new HttpGet(url);

        try {

            HttpResponse getResponse = client.execute(get);
            HttpEntity getResponseEntity = getResponse.getEntity();

            if (getResponseEntity != null) {
                String response = EntityUtils.toString(getResponseEntity);
                return response;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void requestConnectServer(String strURL) throws IOException {
        httpConnection = (HttpURLConnection) new URL(strURL).openConnection();
        httpConnection.connect();

        if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            Log.e(TAG, "Something wrong with connection");
            httpConnection.disconnect();
            throw new IOException("Error in connection: " + httpConnection.getResponseCode());
        }
    }

    private void requestDisconnect() {
        if (httpConnection != null) {
            httpConnection.disconnect();
        }
    }

    public Document getDocumentFromURL(String strURL) throws IOException {
        /* Verify URL */
        if (strURL == null) {
            Log.e(TAG, "Invalid input URL");
            return null;
        }

        /* Connect to server */
        requestConnectServer(strURL);

        /* Get data from server */
        String strDocContent = getDataFromConnection();

        /* Close connection */
        requestDisconnect();

        if (strDocContent == null) {
            Log.e(TAG, "Can not get xml content");
            return null;
        }

        int strContentSize = strDocContent.length();
        StringBuffer strBuff = new StringBuffer();
        strBuff.setLength(strContentSize + 1);
        strBuff.append(strDocContent);
        ByteArrayInputStream is = new ByteArrayInputStream(strDocContent.getBytes());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document docData = null;

        try {
            db = dbf.newDocumentBuilder();
            docData = db.parse(is);
        } catch (Exception e) {
            Log.e(TAG, "Parser data error");
            return null;
        }
        return docData;
    }

    private String getDataFromConnection() throws IOException {
        if (httpConnection == null) {
            Log.e(TAG, "connection is null");
            return null;
        }

        String strValue = null;
        InputStream inputStream = httpConnection.getInputStream();
        if (inputStream == null) {
            Log.e(TAG, "Get input tream error");
            return null;
        }

        StringBuffer strBuf = new StringBuffer();
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(inputStream));
        String strLine = "";

        while ((strLine = buffReader.readLine()) != null) {
            strBuf.append(strLine + "\n");
            strValue += strLine + "\n";
        }

        /* Release resource to system */
        buffReader.close();
        inputStream.close();

        return strBuf.toString();
    }

}