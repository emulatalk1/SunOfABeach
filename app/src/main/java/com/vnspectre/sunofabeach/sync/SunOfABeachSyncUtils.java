package com.vnspectre.sunofabeach.sync;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Created by Spectre on 11/7/17.
 */

public class SunOfABeachSyncUtils {
    /**
     * Helper method to perform a sync immediately using an IntentService for asynchronous
     * execution.
     *
     * @param context The Context used to start the IntentService for the sync.
     */
    public static void startImmediateSync(final Context context) {
        Intent intentToSyncImmediately = new Intent(context, SunOfABeachSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}
