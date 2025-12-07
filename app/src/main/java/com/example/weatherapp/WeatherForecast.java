package com.example.weatherapp;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Data model for weather forecast data
 * Handles parsing of OpenWeatherMap API forecast response
 */
public class WeatherForecast {
    
    public static class HourlyForecast {
        public long timestamp;
        public double temperature;
        public String condition;
        public String icon;
        public double windSpeed;
        public int humidity;
        public String timeString;
        
        public HourlyForecast(JSONObject json) {
            try {
                this.timestamp = json.getLong("dt");
                JSONObject main = json.getJSONObject("main");
                this.temperature = main.getDouble("temp");
                this.humidity = main.getInt("humidity");
                
                JSONObject wind = json.optJSONObject("wind");
                if (wind != null) {
                    this.windSpeed = wind.getDouble("speed") * 3.6; // Convert m/s to km/h
                }
                
                JSONArray weatherArray = json.getJSONArray("weather");
                if (weatherArray.length() > 0) {
                    JSONObject weather = weatherArray.getJSONObject(0);
                    this.condition = weather.getString("main").toLowerCase();
                    this.icon = weather.getString("icon");
                }
                
                // Format time
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                this.timeString = sdf.format(new java.util.Date(timestamp * 1000));
            } catch (Exception e) {
                android.util.Log.e("WeatherForecast", "Error parsing hourly forecast: " + e.getMessage());
            }
        }
    }
    
    public static class DailyForecast {
        public long timestamp;
        public double minTemp;
        public double maxTemp;
        public String condition;
        public String icon;
        public double windSpeed;
        public int humidity;
        public String dayName;
        public String dateString;
        
        public DailyForecast(JSONObject json) {
            try {
                this.timestamp = json.getLong("dt");
                
                // 5-day forecast API uses "main" object with "temp", not "temp" with "min"/"max"
                // We'll use the temp as both min and max, or try to get from temp object if available
                JSONObject main = json.getJSONObject("main");
                double temp = main.getDouble("temp");
                this.minTemp = temp;
                this.maxTemp = temp;
                
                // Try to get min/max from temp object if it exists (One Call API structure)
                JSONObject tempObj = json.optJSONObject("temp");
                if (tempObj != null) {
                    this.minTemp = tempObj.optDouble("min", temp);
                    this.maxTemp = tempObj.optDouble("max", temp);
                }
                
                JSONObject wind = json.optJSONObject("wind");
                if (wind != null) {
                    this.windSpeed = wind.getDouble("speed") * 3.6;
                }
                
                if (main != null) {
                    this.humidity = main.getInt("humidity");
                }
                
                JSONArray weatherArray = json.getJSONArray("weather");
                if (weatherArray.length() > 0) {
                    JSONObject weather = weatherArray.getJSONObject(0);
                    this.condition = weather.getString("main").toLowerCase();
                    this.icon = weather.getString("icon");
                }
                
                // Format day name
                java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault());
                this.dayName = dayFormat.format(new java.util.Date(timestamp * 1000));
                
                // Format date
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault());
                this.dateString = dateFormat.format(new java.util.Date(timestamp * 1000));
            } catch (Exception e) {
                android.util.Log.e("WeatherForecast", "Error parsing daily forecast: " + e.getMessage());
            }
        }
    }
    
    public List<HourlyForecast> hourlyForecasts;
    public List<DailyForecast> dailyForecasts;
    
    public WeatherForecast(String jsonResponse) {
        hourlyForecasts = new ArrayList<>();
        dailyForecasts = new ArrayList<>();
        parseForecast(jsonResponse);
    }
    
    private void parseForecast(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            
            // Parse hourly forecast (next 24 hours)
            JSONArray hourlyArray = json.optJSONArray("list");
            if (hourlyArray != null) {
                int count = Math.min(24, hourlyArray.length());
                for (int i = 0; i < count; i++) {
                    JSONObject item = hourlyArray.getJSONObject(i);
                    hourlyForecasts.add(new HourlyForecast(item));
                }
            }
        } catch (Exception e) {
            android.util.Log.e("WeatherForecast", "Error parsing forecast: " + e.getMessage());
        }
    }
    
    public static WeatherForecast parseDailyForecast(String jsonResponse) {
        WeatherForecast forecast = new WeatherForecast("");
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray listArray = json.optJSONArray("list");
            if (listArray != null) {
                // Group forecasts by day and calculate min/max temperatures
                java.util.Map<String, java.util.List<JSONObject>> dayGroups = new java.util.LinkedHashMap<>();
                java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                
                // Group all forecasts by day
                for (int i = 0; i < listArray.length(); i++) {
                    JSONObject item = listArray.getJSONObject(i);
                    long timestamp = item.getLong("dt");
                    String dayKey = dayFormat.format(new java.util.Date(timestamp * 1000));
                    
                    if (!dayGroups.containsKey(dayKey)) {
                        dayGroups.put(dayKey, new java.util.ArrayList<>());
                    }
                    dayGroups.get(dayKey).add(item);
                }
                
                // Create daily forecasts with calculated min/max
                int dayCount = 0;
                for (java.util.Map.Entry<String, java.util.List<JSONObject>> entry : dayGroups.entrySet()) {
                    if (dayCount >= 7) break;
                    
                    java.util.List<JSONObject> dayForecasts = entry.getValue();
                    if (!dayForecasts.isEmpty()) {
                        // Find min and max temperatures for the day
                        double minTemp = Double.MAX_VALUE;
                        double maxTemp = Double.MIN_VALUE;
                        JSONObject representativeForecast = dayForecasts.get(0);
                        
                        for (JSONObject forecastItem : dayForecasts) {
                            try {
                                double temp = forecastItem.getJSONObject("main").getDouble("temp");
                                minTemp = Math.min(minTemp, temp);
                                maxTemp = Math.max(maxTemp, temp);
                                
                                // Use midday forecast as representative (best icon/condition)
                                long timestamp = forecastItem.getLong("dt");
                                java.util.Calendar cal = java.util.Calendar.getInstance();
                                cal.setTimeInMillis(timestamp * 1000);
                                int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
                                if (hour >= 10 && hour <= 14) {
                                    representativeForecast = forecastItem;
                                }
                            } catch (Exception e) {
                                // Skip invalid forecasts
                            }
                        }
                        
                        // Create daily forecast with calculated min/max
                        DailyForecast daily = new DailyForecast(representativeForecast);
                        daily.minTemp = minTemp;
                        daily.maxTemp = maxTemp;
                        forecast.dailyForecasts.add(daily);
                        dayCount++;
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("WeatherForecast", "Error parsing daily forecast: " + e.getMessage());
        }
        return forecast;
    }
}

