package com.vnspectre.sunofabeach.utilities;

/**
 * Created by Spectre on 10/16/17.
 */

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    /* Fake Json weather. This variable for test. */
    private static final String FAKE_JSON_URL = "http://samples.openweathermap.org/data/2.5/forecast/daily?q=München,DE&appid=b1b15e88fa797225412429c1c50c122a1";

    private static final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast";
    private static final String appid = "29dcfdb00e297f1d864f8b72d5418e07";

    /* The format we want our API to return */
    private static final String format = "json";
    /* The units we want our API to return */
    private static final String units = "metric";
    /* The cnt we want our API to return */
    private static final int cnt = 10;

    final static String QUERY_PARAM = "q";
    final static String LAT_PARAM = "lat";
    final static String LON_PARAM = "lon";
    final static String FORMAT_PARAM = "mode";
    final static String CNT_PARAM = "cnt";
    final static String UNITS_PARAM = "units";
    final static String APPID_PARAM = "APPID";

    /**
     * Builds the URL used to talk to the weather server using a location. This location is based
     * on the query capabilities of the weather provider that we are using.
     *
     * @param locationQuery The location that will be queried for.
     * @return The URL to use to query the weather server.
     */
    public static URL buildUrl(String locationQuery) {
        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM, locationQuery)
                                                               .appendQueryParameter(FORMAT_PARAM, format)
                                                               .appendQueryParameter(UNITS_PARAM, units)
                                                               .appendQueryParameter(CNT_PARAM, Integer.toString(cnt))
                                                               .appendQueryParameter(APPID_PARAM, appid)
                                                               .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        /*Fake url*/
//        try {
//            url = new URL(FAKE_JSON_URL);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
        Log.d(LOG_TAG, "buildUrl() returned: " + url);
        return url;
    }

    /**
     * Builds the URL used to talk to the weather server using latitude and longitude of a
     * location.
     *
     * @param lat The latitude of the location
     * @param lon The longitude of the location
     * @return The Url to use to query the weather server.
     */
    public static URL buildUrl(Double lat, Double lon) {
        return null;
    }

    /**
     * This method returs the result from Http response.
     *
     * @param url the URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
