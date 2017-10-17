package com.vnspectre.sunofabeach.data;

import android.content.Context;

import com.vnspectre.sunofabeach.MainActivity;

/**
 * Created by Spectre on 10/17/17.
 */

public class SunOfABeachPreferences {

    /*
     * Before you implement methods to return your REAL preference for location,
     * I use some default values to test.
     */
    private static final String DEFAULT_WEATHER_LOCATION = "Hanoi";

    /**
     * Returns true if the user has selected metric temperature display.
     *
     * @param context Context used to get the SharedPreferences
     * @return true If metric display should be used
     */
    public static boolean isMetric(Context context) {
        // TODO need to implement.
        return true;
    }

    /**
     * Returns the location currently set in Preferences. The default location this method
     */
    public static String getPreferredWeatherLocation(MainActivity context) {
        // TODO need to implement.
        return getDefaultWeatherLocation();
    }

    private static String getDefaultWeatherLocation() {
        // TODO need to implement.
        return DEFAULT_WEATHER_LOCATION;
    }
}
