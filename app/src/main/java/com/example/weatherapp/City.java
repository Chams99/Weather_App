package com.example.weatherapp;

/**
 * Model class representing a city with location data
 */
public class City {
    private String name;
    private String country;
    private String state;
    private double lat;
    private double lon;
    
    public City(String name, String country, String state, double lat, double lon) {
        this.name = name;
        this.country = country;
        this.state = state;
        this.lat = lat;
        this.lon = lon;
    }
    
    public City(String name, String country) {
        this(name, country, null, 0, 0);
    }
    
    public String getName() {
        return name;
    }
    
    public String getCountry() {
        return country;
    }
    
    public String getState() {
        return state;
    }
    
    public double getLat() {
        return lat;
    }
    
    public double getLon() {
        return lon;
    }
    
    /**
     * Get display name in format: "City, Country" or "City, State, Country"
     */
    public String getDisplayName() {
        if (state != null && !state.isEmpty()) {
            return name + ", " + state + ", " + country;
        }
        return name + ", " + country;
    }
    
    /**
     * Get search query for weather API (just city name)
     */
    public String getSearchQuery() {
        return name;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}
