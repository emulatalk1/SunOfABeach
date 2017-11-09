package com.vnspectre.sunofabeach.sync;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class SunOfABeachSyncIntentService extends IntentService {

    public SunOfABeachSyncIntentService() {
        super("SunOfABeachSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SunOfABeachSyncTask.syncWeather(this);
    }
}
