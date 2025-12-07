package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * RecyclerView adapter for displaying daily weather forecasts
 * Implements Samsung Weather app-style daily forecast list
 */
public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.ViewHolder> {
    
    private List<WeatherForecast.DailyForecast> forecasts;
    
    public DailyForecastAdapter(List<WeatherForecast.DailyForecast> forecasts) {
        this.forecasts = forecasts;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_forecast, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherForecast.DailyForecast forecast = forecasts.get(position);
        holder.bind(forecast);
    }
    
    @Override
    public int getItemCount() {
        return forecasts != null ? forecasts.size() : 0;
    }
    
    public void updateForecasts(List<WeatherForecast.DailyForecast> newForecasts) {
        this.forecasts = newForecasts;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDay;
        private TextView tvDate;
        private WebViewWeatherIcon ivIcon;
        private TextView tvMinTemp;
        private TextView tvMaxTemp;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvMinTemp = itemView.findViewById(R.id.tvMinTemp);
            tvMaxTemp = itemView.findViewById(R.id.tvMaxTemp);
        }
        
        void bind(WeatherForecast.DailyForecast forecast) {
            // Format day name - show "Today" for first item
            if (getAdapterPosition() == 0) {
                tvDay.setText("Today");
            } else {
                tvDay.setText(forecast.dayName);
            }
            
            tvDate.setText(forecast.dateString);
            tvMinTemp.setText(String.format("%.0f°", forecast.minTemp));
            tvMaxTemp.setText(String.format("%.0f°", forecast.maxTemp));
            
            // Load animated SVG icon based on condition
            // For daily forecasts, assume day time (can be enhanced with actual time check)
            String svgFileName = SvgUtils.getWeatherSvgFileName(forecast.condition, false);
            ivIcon.loadWeatherIcon(svgFileName);
        }
    }
}

