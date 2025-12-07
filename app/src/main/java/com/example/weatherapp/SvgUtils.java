package com.example.weatherapp;

/**
 * Utility class for weather icon file name mapping
 */
public class SvgUtils {
    
    /**
     * Get SVG file name based on weather condition and time of day
     * @param condition Weather condition (clear, clouds, rain, etc.)
     * @param isNight Whether it's night time
     * @return SVG file name
     */
    public static String getWeatherSvgFileName(String condition, boolean isNight) {
        if (condition == null) return "cloudy.svg";
        
        switch (condition.toLowerCase()) {
            case "clear":
                return isNight ? "clear-night.svg" : "clear-day.svg";
            case "clouds":
            case "cloudy":
                return "cloudy.svg";
            case "partly-cloudy":
            case "partly cloudy":
                return isNight ? "partly-cloudy-night.svg" : "partly-cloudy-day.svg";
            case "overcast":
                return "overcast.svg";
            case "rain":
            case "drizzle":
                return "rain.svg";
            case "thunderstorm":
            case "thunderstorms":
                return isNight ? "thunderstorms-night.svg" : "thunderstorms-day.svg";
            case "snow":
                return "snow.svg";
            case "mist":
            case "fog":
            case "haze":
            case "smoke":
                return "fog.svg";
            default:
                return "cloudy.svg";
        }
    }
}
