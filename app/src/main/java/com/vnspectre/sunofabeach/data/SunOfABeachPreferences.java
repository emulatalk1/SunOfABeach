package com.vnspectre.sunofabeach.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.vnspectre.sunofabeach.MainActivity;
import com.vnspectre.sunofabeach.R;

/**
 * Created by Spectre on 10/17/17.
 */

public class SunOfABeachPreferences {

    /*
     * Human readable location string, provided by the API.
     */
    public static final String PREF_CITY_NAME = "city_name";

    /*
     * In order to uniquely pinpoint the location on the map when we launch the
     * map intent, we store the latitude and longitude.
     */
    public static final String PREF_COORD_LAT = "coord_lat";
    public static final String PREF_COORD_LONG = "coord_long";

    /*
     * Before we implement methods to return your REAL preference for location,
     * I use some default values to test.
     */
    private static final String DEFAULT_WEATHER_LOCATION = "Việt Nam";
    private static final double[] DEFAULT_WEATHER_COORDINATES = {21.0333, 105.85};

    /**
     * Returns true if the user has selected metric temperature display.
     *
     * @param context Context used to get the SharedPreferences
     * @return true If metric display should be used
     */
    public static boolean isMetric(Context context) {

        // Return true if the user's preference for units is metric, false otherwise
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForUnits = context.getString(R.string.pref_units_key);
        String defaultUnits = context.getString(R.string.pref_units_metric);
        String preferredUnits = prefs.getString(keyForUnits, defaultUnits);
        String metric = context.getString(R.string.pref_units_metric);
        boolean userPrefersMetric;
        if (metric.equals(preferredUnits)) {
            userPrefersMetric = true;
        } else {
            userPrefersMetric = false;
        }
        return userPrefersMetric;
    }

    /**
     * Returns the location currently set in Preferences. The default location this method
     */
    public static String getPreferredWeatherLocation(MainActivity context) {
        // Return the user's preferred location
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForLocation = context.getString(R.string.pref_location_key);
        String defaultLocation = context.getString(R.string.pref_location_default);
        return prefs.getString(keyForLocation, defaultLocation);
    }

    private static String getDefaultWeatherLocation() {
        // TODO need to implement.
        return DEFAULT_WEATHER_LOCATION;
    }

    public static double[] getDefaultWeatherCoordinates() {
        // TODO need to implement.
        return DEFAULT_WEATHER_COORDINATES;
    }

    /**
     * Helper method to handle setting location details in Preferences (city name, latitude,
     * longitude)
     * 
     * When the location details are updated, the database should to be cleared.
     *
     * @param context  Context used to get the SharedPreferences
     * @param lat      the latitude of the city
     * @param lon      the longitude of the city
     */
    public static void setLocationDetails(Context context, double lat, double lon) {
        SharedPreferences sp = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(PREF_COORD_LAT, Double.doubleToRawLongBits(lat));
        editor.putLong(PREF_COORD_LONG, Double.doubleToRawLongBits(lon));
        editor.apply();
    }
}
