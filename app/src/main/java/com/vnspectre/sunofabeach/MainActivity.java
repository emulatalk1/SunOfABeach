package com.vnspectre.sunofabeach;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vnspectre.sunofabeach.ForecastAdapter.ForecastAdapterOnClickHandler;
import com.vnspectre.sunofabeach.data.SunOfABeachPreferences;
import com.vnspectre.sunofabeach.utilities.NetworkUtils;
import com.vnspectre.sunofabeach.utilities.OpenWeatherJsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapterOnClickHandler {

    /* Declare a TextView variable called mWeatherTextView. */
    //private TextView mWeatherTextView;
    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;

    /* Declare a TextView variable for the error message display. */
    private TextView mErrorMessageDisplay;

    /* Declare a ProgressBar variable to show and hide the progress bar. */
    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        //mWeatherTextView = (TextView) findViewById(R.id.tv_weather_data);
        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        /*
         * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if we know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
        mForecastAdapter = new ForecastAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mForecastAdapter);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        //Dummy data
//        String[] dummyWeatherdata = {
//                "Today, May 17 - Clear - 17°C / 15°C",
//                "Tomorrow - Cloudy - 19°C / 15°C",
//                "Thursday - Rainy- 30°C / 11°C",
//                "Friday - Thunderstorms - 21°C / 9°C",
//                "Saturday - Thunderstorms - 16°C / 7°C",
//                "Sunday - Rainy - 16°C / 8°C",
//                "Monday - Partly Cloudy - 15°C / 10°C",
//                "Tue, May 24 - Meatballs - 16°C / 18°C",
//                "Wed, May 25 - Cloudy - 19°C / 15°C",
//                "Thu, May 26 - Stormy - 30°C / 11°C",
//                "Fri, May 27 - Hurricane - 21°C / 9°C",
//                "Sat, May 28 - Meteors - 16°C / 7°C",
//                "Sun, May 29 - Apocalypse - 16°C / 8°C",
//                "Mon, May 30 - Post Apocalypse - 15°C / 10°C",
//        };
//
//        //Append dummyWeatherData
//        for (String weather : dummyWeatherdata) {
//            mWeatherTextView.append(weather + "\n\n\n");
//        }

        /* Once all of our views are setup, we can load the weather data. */
        loadWeatherData();
    }

    /**
     * This method will get the user's preferred location for weather, and then tell some
     * background method to get the weather data in the background.
     */
    private void loadWeatherData() {
        /* Call showWeatherDataView before executing the AsyncTask */
        showWeatherDataView();

        String location = SunOfABeachPreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    private void showWeatherDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);

        /* Then, make sure the weather data is visible */
        //mWeatherTextView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     *
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
       //mWeatherTextView.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);

        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param weatherForDay The weather for the day that was clicked
     */
    @Override
    public void onClick(String weatherForDay) {
        Context context = this;
//        Toast.makeText(context, weatherForDay, Toast.LENGTH_SHORT).show();
        Class destinationClass = DetailActivity.class;
        Intent intent = new Intent(context, destinationClass);
        startActivity(intent);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        /* Show the loading indicator, it means that data is fetching */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {

            /* If there's no zip code, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            String location = params[0];
            URL weatherRequestUrl = NetworkUtils.buildUrl(location);

            try {
                String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

                String[] simpleJsonWeatherData = OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                return  simpleJsonWeatherData;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] weatherData) {
            /* As soon as the data is finished loading, hide the loading indicator. */
            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if (weatherData != null) {
                /* If the weather data was not null, make sure the data view is visible. */
                showWeatherDataView();

                /*
                 * Iterate through the array and append the Strings to the TextView. The reason why we add
                 * the "\n\n\n" after the String is to give visual separation between each String in the
                 * TextView.
                 */
//                for (String weatherString : weatherdata) {
//                    mWeatherTextView.append(weatherString + "\n\n\n");
//                }
                mForecastAdapter.setWeatherdata(weatherData);
            } else {
                /* If the weather data was null, shows the error message. */
                showErrorMessage();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */

        MenuInflater inflater = getMenuInflater();

        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);

        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            //mWeatherTextView.setText("");
            mForecastAdapter.setWeatherdata(null);
            loadWeatherData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
