package com.vnspectre.sunofabeach;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vnspectre.sunofabeach.utilities.SunOfABeachDateUtils;
import com.vnspectre.sunofabeach.utilities.SunOfABeachWeatherUtils;

/**
 * Created by Spectre on 10/19/17.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final ForecastAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick massages.
     */
    public interface ForecastAdapterOnClickHandler {
        void onClick(String weatherForDay);
    }

    private Cursor mCursor;

    /**
     * Creates ForecastAdapter.
     *
     * @param clickHandler the on=click handler for this adapter. This single handler is called
     *                     when aa item is clicked.
     */
    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        final TextView weatherSummary;

        ForecastAdapterViewHolder(View view) {
            super(view);
            weatherSummary = view.findViewById(R.id.tv_weather_data);

            //setOnClickListener on the view passed into the constructor
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String weatherForDay = weatherSummary.getText().toString();
            mClickHandler.onClick(weatherForDay);
        }
    }

    /**
     * This get called when each new ViewHolder is created. This happens when RecyclerView
     * is laid out. Enough ViewHolder will be created to fill the screen and allow scrolling.
     *
     * @param viewGroup The viewGroup that these ViewHolders are contained within.
     * @param viewType If RecyclerView has more than one type of item (which our doesn't) we
     *                 can use this viewType integer to provide different layout.
     * @return A new ForecastAdapterViewHolder that holds the view for each list item.
     */
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(R.layout.forecast_list_item, viewGroup, false);

        return new ForecastAdapterViewHolder(view);
    }


    /**
     * onBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at
     *               the given position in the data set.
     * @param position The position of item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {
        // Move the cursor to the appropriate position
        mCursor.moveToPosition(position);

        /* Read date from the cursor */
        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        /* Get human readable string using our utility method */
        String dateString = SunOfABeachDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        /* Use the weatherId to obtain the proper description */
        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        String description = SunOfABeachWeatherUtils.getStringForWeatherCondition(mContext, weatherId);
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);

        String highAndLowTemperature = SunOfABeachWeatherUtils.formatHighLows(mContext, highInCelsius, lowInCelsius);
        String weatherSummary = dateString + " - " + description + " - " + highAndLowTemperature;

        // Display the summary that I created above
        holder.weatherSummary.setText(weatherSummary);
    }

    /**
     * The method simply returns the number of items to display. It is used to behind the scenes to
     * help layout our Views for animations.
     *
     * @return The numbers of items available in our forecast.
     */
    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}
