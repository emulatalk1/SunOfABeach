package com.vnspectre.sunofabeach.sync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.vnspectre.sunofabeach.data.WeatherContract;

/**
 * Created by Spectre on 11/7/17.
 */

public class SunOfABeachSyncUtils {

    private static boolean sInitialized;

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     *
     * @param context Context that will be passed to other methods and used to access the
     *                ContentResolver.
     */
    @SuppressLint("StaticFieldLeak")
    synchronized public static void initialize(final Context context) {
        if (sInitialized) return;

        // If the method body is executed, set sInitialized to true
        sInitialized = true;

        /*
         * We need to check to see if our ContentProvider has data to display in our forecast
         * list. However, performing a query on the main thread is a bad idea as this may
         * cause our UI to lag. Therefore, I create a thread in which we will run the query
         * to check the contents of our ContentProvider.
         */
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                /* URI for every row of weather data in our weather table*/
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;

                String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
                String selectionStatement = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                /* perform the query to check to see if we have any weather data */
                Cursor cursor = context.getContentResolver().query(forecastQueryUri, projectionColumns, selectionStatement, null, null);

                // If it is empty or we have a null Cursor, sync the weather now!
                if (null == cursor || cursor.getCount() == 0) {
                    startImmediateSync(context);
                }

                // Make sure to close the Cursor to avoid memory leaks.
                cursor.close();
                return null;
            }
        }.execute();
    }

    /**
     * Helper method to perform a sync immediately using an IntentService for asynchronous
     * execution.
     *
     * @param context The Context used to start the IntentService for the sync.
     */
    public static void startImmediateSync(Context context) {
        Intent intentToSyncImmediately = new Intent(context, SunOfABeachSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}
