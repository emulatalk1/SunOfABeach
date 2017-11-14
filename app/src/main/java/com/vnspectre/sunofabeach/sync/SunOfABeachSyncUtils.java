package com.vnspectre.sunofabeach.sync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.vnspectre.sunofabeach.data.WeatherContract;

import java.util.concurrent.TimeUnit;

/**
 * Created by Spectre on 11/7/17.
 */

public class SunOfABeachSyncUtils {

    private static final int SYNC_INTERVAL_HOURS = 3;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

    private static boolean sInitialized;

    // Tag to identify our sync job.
    private static final String SUNOFABEACH_SYNC_TAG = "sunofabeach-sync";

    // Schedules a repeating sync.
    static void scheduleFirebaseJobDispatcherSync(Context context) {

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        //Create the Job to periodically sync.
        Job syncSunshineJob = dispatcher.newJobBuilder()
                .setService(SunOfABeachFireBaseJobService.class)
                .setTag(SUNOFABEACH_SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(SYNC_INTERVAL_SECONDS, SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(syncSunshineJob);
    }

    // Creates periodic sync tasks and checks to see if an immediate sync is required. If an
    // immediate sync is required, this method will take care of making sure that sync occurs.
    @SuppressLint("StaticFieldLeak")
    synchronized public static void initialize(final Context context) {
        if (sInitialized) return;

        // If the method body is executed, set sInitialized to true
        sInitialized = true;

        // Call triggers SunOfABeach to create its task to synchronize weather data periodically.
        scheduleFirebaseJobDispatcherSync(context);

        /*
         * Check to see if ContentProvider has data to display in forecast. This thread will run
         * the query to check the contents of ContentProvider.
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

    // Helper method to perform a sync immediately using an IntentService for asynchronous execution.
    public static void startImmediateSync(Context context) {
        Intent intentToSyncImmediately = new Intent(context, SunOfABeachSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}
