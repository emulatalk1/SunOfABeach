package com.vnspectre.sunofabeach.utilities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;


import com.vnspectre.sunofabeach.DetailActivity;
import com.vnspectre.sunofabeach.R;
import com.vnspectre.sunofabeach.data.SunOfABeachPreferences;
import com.vnspectre.sunofabeach.data.WeatherContract;

/**
 * Created by Spectre on 11/8/17.
 */

public class NotificationUtils {

    // The columns of data will be displayed within notification.
    public static final String[] WEATHER_NOTIFICATION_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
    };

    // The indices of the values in the array of Strings above.
    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_MAX_TEMP = 1;
    public static final int INDEX_MIN_TEMP = 2;

    // This notification ID can be used to access after it is displayed.
    private static final int WEATHER_NOTIFICATION_ID = 9898;

    // Constructs and displays a notification for the newly updated weather for today.
    public static void notifyUserOfNewWeather(Context context) {

        // Build the URI for today's weather in order to show up to date data in notification.
        Uri todaysWeatherUri = WeatherContract.WeatherEntry.buildWeatherUriWithDate(SunOfABeachDateUtils.normalizeDate(System.currentTimeMillis()));

        Cursor todayWeatherCursor = context.getContentResolver().query(
                todaysWeatherUri,
                WEATHER_NOTIFICATION_PROJECTION,
                null,
                null,
                null);

        /*
         * If todayWeatherCursor is empty, moveToFirst will return false. If our cursor is not
         * empty, SunOfABeach will show the notification.
         */
        if (todayWeatherCursor.moveToFirst()) {

            // Weather ID as returned by API, used to identify the icon to be used.
            int weatherId = todayWeatherCursor.getInt(INDEX_WEATHER_ID);
            double high = todayWeatherCursor.getDouble(INDEX_MAX_TEMP);
            double low = todayWeatherCursor.getDouble(INDEX_MIN_TEMP);

            Resources resources = context.getResources();
            int largeArtResourceId = SunOfABeachWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);

            Bitmap largeIcon = BitmapFactory.decodeResource(resources, largeArtResourceId);

            String notificationTitle = context.getString(R.string.app_name);

            String notificationText = getNotificationText(context, weatherId, high, low);

            // getSmallArtResourceIdForWeatherCondition returns the proper art to show given an ID.
            int smallArtResourceId = SunOfABeachWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId);

            // Build Notification.
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setAutoCancel(true);


            // This Intent will be triggered when the user clicks the notification.
            Intent detailIntentForToday = new Intent(context, DetailActivity.class);
            detailIntentForToday.setData(todaysWeatherUri);

            // Using TaskStackBuilder to create the proper PendingIntent.
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
            taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
            PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            //  Set the content Intent of the NotificationBuilder.
            notificationBuilder.setContentIntent(resultPendingIntent);

            // Get a reference to the NotificationManager.
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Set id for notification.
            notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build()); // TODO NULL

            // When showed notification, save current time. That way, SunOfABeach can checks the next time notification.
            SunOfABeachPreferences.saveLastNotificationTime(context, System.currentTimeMillis());

        }

        todayWeatherCursor.close();
    }

    private static String getNotificationText(Context context, int weatherId, double high, double low) {

        // "sky is clear" --> "clear"
        String shortDescription = SunOfABeachWeatherUtils.getStringForWeatherCondition(context, weatherId);

        String notificationFormat = context.getString(R.string.format_notification);

        /* String's format method, it creates for the forecast summary */
        String notificationText =
                String.format(notificationFormat,
                shortDescription,
                SunOfABeachWeatherUtils.formatTemperature(context, high),
                SunOfABeachWeatherUtils.formatTemperature(context, low));

        return notificationText;
    }

}
