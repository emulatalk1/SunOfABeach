package com.vnspectre.sunofabeach;



import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.*;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;


import com.vnspectre.sunofabeach.data.WeatherContract;
import com.vnspectre.sunofabeach.utilities.SunOfABeachDateUtils;
import com.vnspectre.sunofabeach.utilities.SunOfABeachWeatherUtils;

import android.view.MenuItem;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String HASH_TAG = "#vnspectre.com";

    /*
     * The columns of data that we are interested in displaying within our DetailActivity's
     * weather display.
     */
    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_HUMIDITY = 3;
    public static final int INDEX_WEATHER_PRESSURE = 4;
    public static final int INDEX_WEATHER_WIND_SPEED = 5;
    public static final int INDEX_WEATHER_DEGREES = 6;
    public static final int INDEX_WEATHER_CONDITION_ID = 7;

    /*
     * This ID will be used to identify the Loader responsible for loading the weather details
     * for a particular day.
     */
    private static final int ID_DETAIL_LOADER = 353;

    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTemperatureView;
    private TextView mLowTemperatureView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    private Uri mUri;

    private String mForecastSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDateView = (TextView) findViewById(R.id.date);
        mDescriptionView = (TextView) findViewById(R.id.weather_description);
        mHighTemperatureView = (TextView) findViewById(R.id.high_temperature);
        mLowTemperatureView = (TextView) findViewById(R.id.low_temperature);
        mHumidityView = (TextView) findViewById(R.id.humidity);
        mWindView = (TextView) findViewById(R.id.wind);
        mPressureView = (TextView) findViewById(R.id.pressure);

        mUri = getIntent().getData();
        if (mUri == null) {
            throw new NullPointerException("URI for DetailActivity cannot be null");
        }

        /* This connects our Activity into the loader lifecycle. */
        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }

    /**
     * Uses the ShareCompat Intent builder to create our Forecast intent for sharing. We set the
     * type of content that we are sharing (just regular text), the text itself, and we return the
     * newly created Intent.
     *
     * @return The Intent to use to start our share.
     */
    private Intent createShareForecastIntent() {
        Intent shareIndent = ShareCompat.IntentBuilder.from(this).setType("text/html").setText(mForecastSummary + HASH_TAG).getIntent();
        shareIndent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIndent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareItem.setIntent(createShareForecastIntent());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {
        switch (loaderId) {
            case ID_DETAIL_LOADER:
                return new CursorLoader(this, mUri, WEATHER_DETAIL_PROJECTION, null, null, null);

            default:
                throw new RuntimeException("Loader not implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;
        /* If we have valid data, we want to continue on to bind that data to the UI. If we don't
         * have any data to bind, we just return from this method.
         */
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        // Date
        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = SunOfABeachDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);
        mDateView.setText(dateText);

        // Weather description
        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        String description = SunOfABeachWeatherUtils.getStringForWeatherCondition(this, weatherId);
        mDescriptionView.setText(description);

        // High temperature
        int highInCelsius = data.getInt(INDEX_WEATHER_MAX_TEMP);
        String highString = SunOfABeachWeatherUtils.formatTemperature(this, highInCelsius);
        mHighTemperatureView.setText(highString);

        // Low temperature
        int lowInCelsius = data.getInt(INDEX_WEATHER_MIN_TEMP);
        String lowString = SunOfABeachWeatherUtils.formatTemperature(this, lowInCelsius);
        mLowTemperatureView.setText(lowString);

        // Humidity
        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);
        mHumidityView.setText(humidityString);

        // Wind and direction
        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = SunOfABeachWeatherUtils.getFormattedWind(this, windSpeed, windDirection);
        mWindView.setText(windString);

        // Pressure
        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);
        String pressureString = getString(R.string.format_pressure, pressure);
        mPressureView.setText(pressureString);

        /* Store the forecast summary String in our forecast summary field to share later */
        mForecastSummary = String.format("%s - %s - %s/%s", dateText, description, highString, lowString);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
