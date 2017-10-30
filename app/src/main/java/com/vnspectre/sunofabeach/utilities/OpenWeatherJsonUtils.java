package com.vnspectre.sunofabeach.utilities;

/**
 * Created by Spectre on 10/15/17.
 */

import android.content.ContentValues;
import android.content.Context;

import com.vnspectre.sunofabeach.data.SunOfABeachPreferences;
import com.vnspectre.sunofabeach.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Utility functions to handle OpenWeatherMap JSON data.
 */
public final class OpenWeatherJsonUtils {

    /* Location information */
    private static final String OWM_CITY = "city";
    private static final String OWM_COORD = "coord";

    /* Location coordinate */
    private static final String OWM_LATITUDE = "lat";
    private static final String OWM_LONGITUDE = "lon";

    /* Weather information. Each day's forecast info is an element of the "list" array */
    private static final String OWM_LIST = "list";

    private static final String OWM_PRESSURE = "pressure";
    private static final String OWM_HUMIDITY = "humidity";
    private static final String OWM_WINDSPEED = "speed";
    private static final String OWM_WIND_DIRECTION = "deg";

    /* All temperatures are children of the "temp" object */
    private static final String OWM_TEMPERATURE = "temp";

    /* Max temperature for the day */
    private static final String OWM_MAX = "max";
    private static final String OWM_MIN = "min";

    private static final String OWM_WEATHER = "weather";
    private static final String OWM_WEATHER_ID = "id";

    private static final String OWM_MESSAGE_CODE = "cod";

    /* This is For free API, sadness :( */
    private static final String FREE_CITY = "city";
    private static final String FREE_COORD = "coord";
    private static final String FREE_LATITUDE = "lat";
    private static final String FREE_LONGITUDE = "lon";
    private static final String FREE_LIST = "list";
    private static final String FREE_PRESSURE = "pressure";
    private static final String FREE_HUMIDITY = "humidity";
    private static final String FREE_WINDSPEED = "speed";
    private static final String FREE_WIND_DIRECTION = "deg";
    private static final String FREE_TEMPERATURE = "main";
    private static final String FREE_MAX = "temp_max";
    private static final String FREE_MIN = "temp_min";
    private static final String FREE_WEATHER = "weather";
    private static final String FREE_DESCRIPTION = "description";
    private static final String FREE_MESSAGE_CODE = "cod";

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

        /* String array to hold each day's weather String */
        String[] parsedWeatherData;

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        /* Is there an error? */
        if (forecastJson.has(FREE_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(FREE_MESSAGE_CODE);
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

        JSONArray weatherArray = forecastJson.getJSONArray(FREE_LIST);
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
            JSONObject weatherObject = dayForecast.getJSONArray(FREE_WEATHER).getJSONObject(0);
            description = weatherObject.getString(FREE_DESCRIPTION);

            JSONObject temperatureObject = dayForecast.getJSONObject(FREE_TEMPERATURE);
            high = temperatureObject.getDouble(FREE_MAX);
            low = temperatureObject.getDouble(FREE_MIN);
            highAndLow = SunOfABeachWeatherUtils.formatHighLows(context, high, low);

            parsedWeatherData[i] = date + " - " + description + " - " + highAndLow;
        }
        return parsedWeatherData;
    }

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     *
     * Later on, we'll be parsing the JSON into structured data within the
     * getFullWeatherDataFromJson function, leveraging the data we have stored in the JSON. For
     * now, we just convert the JSON into human-readable strings.
     *
     * @param forecastJsonStr JSON response from server
     *
     * @return Array of Strings describing weather data
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ContentValues[] getFullWeatherDataFromJson(Context context, String forecastJsonStr) throws JSONException {

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        /* Is there an error? */
        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray jsonWeatherArray = forecastJson.getJSONArray(FREE_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(FREE_CITY);

        JSONObject cityCoord = cityJson.getJSONObject(FREE_COORD);
        double cityLatitude = cityCoord.getDouble(FREE_LATITUDE);
        double cityLongitude = cityCoord.getDouble(FREE_LONGITUDE);

        SunOfABeachPreferences.setLocationDetails(context, cityLatitude, cityLongitude);

        ContentValues[] weatherContentValues = new ContentValues[jsonWeatherArray.length()];

        long normalizedUtcStartDay = SunOfABeachDateUtils.getNormalizedUtcDateForToday();

        for (int i = 0; i < jsonWeatherArray.length(); i++) {

            long dateTimeMillis;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            int weatherId;

            /* Get the JSON object representing the day */
            JSONObject dayForecast = jsonWeatherArray.getJSONObject(i);

            /*
             * We ignore all the datetime values embedded in the JSON and assume that
             * the values are returned in-order by day (which is not guaranteed to be correct).
             */
            dateTimeMillis = normalizedUtcStartDay + SunOfABeachDateUtils.DAY_IN_MILLIS * i;

            pressure = dayForecast.getDouble(FREE_PRESSURE);
            humidity = dayForecast.getInt(FREE_HUMIDITY);
            windSpeed = dayForecast.getDouble(FREE_WINDSPEED);
            windDirection = dayForecast.getDouble(FREE_WIND_DIRECTION);

            /*
             * Description is in a child array called "weather", which is 1 element long.
             * That element also contains a weather code.
             */
            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            /*
             * Temperatures are sent by Open Weather Map in a child object called "temp".
             *
             * Editor's Note: Try not to name variables "temp" when working with temperature.
             * It confuses everybody. Temp could easily mean any number of things, including
             * temperature, temporary variable, temporary folder, temporary employee, or many
             * others, and is just a bad variable name.
             */
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTimeMillis);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            weatherContentValues[i] = weatherValues;
        }

        return weatherContentValues;
    }
}
