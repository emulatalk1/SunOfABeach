package com.vnspectre.sunofabeach.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;

import com.vnspectre.sunofabeach.data.SunOfABeachPreferences;
import com.vnspectre.sunofabeach.data.WeatherContract;
import com.vnspectre.sunofabeach.utilities.NetworkUtils;
import com.vnspectre.sunofabeach.utilities.NotificationUtils;
import com.vnspectre.sunofabeach.utilities.OpenWeatherJsonUtils;

import java.net.URL;

/**
 * Created by Spectre on 11/7/17.
 */

public class SunOfABeachSyncTask {

    synchronized public static void syncWeather(Context context) {
        try {
            /*
             * The getUrl method will return the URL that we need to get the forecast JSON for the
             * weather. It will decide whether to create a URL based off of the latitude and
             * longitude or off of a simple location as a String.
             */
            URL weatherRequestUrl = NetworkUtils.getUrl(context);

            // Use the URL to retrieve the JSON.
            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

            // Parse the JSON into a list of weather values.
            ContentValues[] weatherValues = OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context, jsonWeatherResponse);

            /*
             * In cases where our JSON contained an error code, getWeatherContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if (weatherValues != null && weatherValues.length != 0) {
                // Get a handle on the ContentResolver to delete and insert data.
                ContentResolver sunOfABeachContentResolver = context.getContentResolver();

                // Delete old weather data because we don't need to keep multiple days' data.
                sunOfABeachContentResolver.delete(WeatherContract.WeatherEntry.CONTENT_URI, null, null);

                /* Insert our new weather data into Sunshine's ContentProvider */
                sunOfABeachContentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, weatherValues);

                /*
                 * After insert data into the ContentProvider, determine whether or not
                 * SunOfABeach should notify the user that the weather has been refreshed.
                 */
                boolean notificationsEnabled = SunOfABeachPreferences.areNotificationsEnabled(context);

                long timeSinceLastNotification = SunOfABeachPreferences.getEllapsedTimeSinceLastNotification(context);

                boolean oneDayPassedSinceLastNotification = false;

                // Check if a day has passed since the last notification.
                if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                    oneDayPassedSinceLastNotification = true;
                }

                // If more than a day have passed and notifications are enabled, notify the user.
                if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                    NotificationUtils.notifyUserOfNewWeather(context);
                }
            }

        } catch (Exception e) {
            // Server probably invalid.
            e.printStackTrace();
        }
    }
}
