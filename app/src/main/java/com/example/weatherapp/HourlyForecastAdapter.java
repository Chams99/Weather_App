package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * RecyclerView adapter for displaying hourly weather forecasts
 * Implements Samsung Weather app-style horizontal scrolling forecast cards
 */
public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {
    
    private List<WeatherForecast.HourlyForecast> forecasts;
    
    public HourlyForecastAdapter(List<WeatherForecast.HourlyForecast> forecasts) {
        this.forecasts = forecasts;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly_forecast, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherForecast.HourlyForecast forecast = forecasts.get(position);
        holder.bind(forecast);
    }
    
    @Override
    public int getItemCount() {
        return forecasts != null ? forecasts.size() : 0;
    }
    
    public void updateForecasts(List<WeatherForecast.HourlyForecast> newForecasts) {
        this.forecasts = newForecasts;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTime;
        private WebViewWeatherIcon ivIcon;
        private TextView tvTemp;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTemp = itemView.findViewById(R.id.tvTemp);
        }
        
        void bind(WeatherForecast.HourlyForecast forecast) {
            tvTime.setText(forecast.timeString);
            tvTemp.setText(String.format("%.0f°", forecast.temperature));
            
            // Load animated SVG icon based on condition
            // For hourly forecasts, assume day time (can be enhanced with actual time check)
            String svgFileName = SvgUtils.getWeatherSvgFileName(forecast.condition, false);
            ivIcon.loadWeatherIcon(svgFileName);
        }
    }
}

