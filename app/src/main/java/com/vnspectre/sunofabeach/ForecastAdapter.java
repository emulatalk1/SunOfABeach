package com.vnspectre.sunofabeach;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vnspectre.sunofabeach.utilities.SunOfABeachDateUtils;
import com.vnspectre.sunofabeach.utilities.SunOfABeachWeatherUtils;

/**
 * Created by Spectre on 10/19/17.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    // The context we use to utility methods, app resources and layout inflaters.
    private final Context mContext;

    private final ForecastAdapterOnClickHandler mClickHandler;


    // The interface that receives onClick massages.
    public interface ForecastAdapterOnClickHandler {
        void onClick(long date);
    }

    private Cursor mCursor;

    // Creates a ForecastAdapter.
    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    // Cache of the children views for a forecast list item.
    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;
        final ImageView iconView;

        ForecastAdapterViewHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.weather_icon);
            dateView = (TextView) view.findViewById(R.id.date);
            descriptionView = (TextView) view.findViewById(R.id.weather_description);
            highTempView = (TextView) view.findViewById(R.id.high_temperature);
            lowTempView = (TextView) view.findViewById(R.id.low_temperature);

            //setOnClickListener on the view passed into the constructor
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);
        }
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(R.layout.forecast_list_item, viewGroup, false);

        return new ForecastAdapterViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {
        // Move the cursor to the appropriate position
        mCursor.moveToPosition(position);

        // Weather icon
        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId;
        weatherImageId = SunOfABeachWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId);
        holder.iconView.setImageResource(weatherImageId);

        // Weather date
        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        String dateString = SunOfABeachDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        holder.dateView.setText(dateString);

        // Weather description
        String description = SunOfABeachWeatherUtils.getStringForWeatherCondition(mContext, weatherId);
        String descriptionA11y = mContext.getString(R.string.a11y_forecast, description);
        holder.descriptionView.setText(description);
        holder.descriptionView.setContentDescription(descriptionA11y);

        // High temperature
        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        String highString = SunOfABeachWeatherUtils.formatTemperature(mContext, highInCelsius);
        String highA11y = mContext.getString(R.string.a11y_high_temp, highString);
        holder.highTempView.setText(highString);
        holder.highTempView.setContentDescription(highA11y);

        // Low temperature
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
        String lowString = SunOfABeachWeatherUtils.formatTemperature(mContext, lowInCelsius);
        String lowA11y = mContext.getString(R.string.a11y_low_temp, lowString);
        holder.lowTempView.setText(lowString);
        holder.lowTempView.setContentDescription(lowA11y);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    // Update new data to UI
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}
