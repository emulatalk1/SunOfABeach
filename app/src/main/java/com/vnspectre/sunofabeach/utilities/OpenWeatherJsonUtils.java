package com.vnspectre.sunofabeach.utilities;

/**
 * Created by Spectre on 10/15/17.
 */

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Utility functions to handle OpenWeatherMap JSON data.
 */
public final class OpenWeatherJsonUtils {

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     *
     * In the future, I'll be parsing the JSON into structured data within the
     * getFullWeatherDataFromJson function, leveraging the data we have stored in the JSON. For
     * now, I just convert the JSON into human-readable strings.
     *
     * @param forecastJsonStr JSON response from server
     *
     * @return Array of Strings describing weather data
     */
    public static String[] getSimpleWeatherStringsFromJson(Context context, String forecastJsonStr) throws JSONException {
        /* Weather information. Each day's forecast info is an element of the "list" array */
        final String OWM_LIST = "list";

        /* All temperatures are children of the "temp" object */
        final String OWM_TEMPERATURE = "temp";

        /* Max and min temperature for the day */
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";

        final String OWM_MESSAGE_CODE = "cod";

        /* String array to hold each day's weather String */
        String[] parsedWeatherData = null;

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        /* Is there an error? */
        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);
            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid*/
                    return null;
                default:
                    /* Server down */
                    return null;
            }
        }

        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        parsedWeatherData = new String[weatherArray.length()];

        long localDate = System.currentTimeMillis();
        long utcDate = SunOfABeachDateUtils.getUTCDateFromLocal(localDate);
        long startDay = SunOfABeachDateUtils.normalizeDate(utcDate);

        for (int i = 0; i < weatherArray.length(); i++) {
            String date;
            String highAndLow;

            /* These are the values that will be collected*/
            long dateTimeMillis;
            double high;
            double low;
            String description;

            /* Get the JSON object representing the day */
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            /*
             * we ignore all the datetime values embedded in the JSON and assume that
             * the values are returned in-order by day. (which is not guaranteed to be correct)
             */
            dateTimeMillis = startDay + SunOfABeachDateUtils.DAY_IN_MILLIS * i;
            date = SunOfABeachDateUtils.getFriendlyDateString(context, dateTimeMillis, false);

            /*
             * Description is in a child array called "weather", which is 1 element long.
             * That element also contains a weather code.
             */
            // TODO .getJSONObject(0) - not sure at "0"
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);
            highAndLow = SunOfABeachWeatherUtils.formatHighLows(context, high, low);

            parsedWeatherData[i] = date + " - " + description + " - " + highAndLow;
        }
        return parsedWeatherData;
    }

    /**
     * Parse the JSON and convert it into ContentValues that can be inserted into our database.
     *
     * @param context         An application context, such as a service or activity context.
     * @param forecastJsonStr The JSON to parse into ContentValues.
     *
     * @return An array of ContentValues parsed from the JSON.
     */
    public static ContentValues[] getFullWeatherDataFromJson(Context context, String forecastJsonStr) {
        // TODO This will be implemented in a future. I'm thinking about this.
        return null;
    }
}
