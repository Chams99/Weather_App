package com.example.weatherapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Custom adapter for city search with Tunisian cities priority and API-based worldwide search
 */
public class CitySearchAdapter extends ArrayAdapter<City> implements Filterable {
    
    private static final String API_KEY = "YOUR_OPENWEATHER_API_KEY";
    private static final String GEOCODING_URL = "http://api.openweathermap.org/geo/1.0/direct";
    
    // Tunisian cities - always shown first
    private static final String[] TUNISIAN_CITIES = {
        "Tunis", "Sfax", "Sousse", "Kairouan", "Bizerte", "Gabes", "Ariana", "Gafsa",
        "La Marsa", "Monastir", "Ben Arous", "Kasserine", "Medenine", "Nabeul", "Tataouine",
        "Beja", "Jendouba", "Kef", "Mahdia", "Sidi Bouzid", "Siliana", "Tozeur", "Zaghouan",
        "Hammamet", "Djerba", "Zarzis", "Kebili", "Douz", "Nefta", "Mateur", "Menzel Bourguiba",
        "Metlaoui", "Redeyef", "Moknine", "Msaken", "Korba", "Kelibia", "Grombalia", "Menzel Temime"
    };
    
    private final Context context;
    private final List<City> allCities;
    private final List<City> filteredCities;
    private final ExecutorService executorService;
    private final Handler handler;
    private final CityFilter cityFilter;
    
    public CitySearchAdapter(@NonNull Context context) {
        super(context, android.R.layout.simple_dropdown_item_1line);
        this.context = context;
        this.allCities = new ArrayList<>();
        this.filteredCities = new ArrayList<>();
        this.executorService = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
        this.cityFilter = new CityFilter();
        
        // Initialize with Tunisian cities
        initializeTunisianCities();
    }
    
    private void initializeTunisianCities() {
        for (String cityName : TUNISIAN_CITIES) {
            allCities.add(new City(cityName, "TN"));
        }
        filteredCities.addAll(allCities);
    }
    
    @Override
    public int getCount() {
        return filteredCities.size();
    }
    
    @Nullable
    @Override
    public City getItem(int position) {
        return position < filteredCities.size() ? filteredCities.get(position) : null;
    }
    
    @NonNull
    @Override
    public Filter getFilter() {
        return cityFilter;
    }
    
    /**
     * Custom filter that combines Tunisian cities with API results
     */
    private class CityFilter extends Filter {
        
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            
            if (constraint == null || constraint.length() == 0) {
                // No query - show Tunisian cities only
                results.values = new ArrayList<>(allCities);
                results.count = allCities.size();
                return results;
            }
            
            String query = constraint.toString().toLowerCase().trim();
            List<City> combinedResults = new ArrayList<>();
            
            // 1. Filter Tunisian cities locally
            for (City city : allCities) {
                if (city.getName().toLowerCase().startsWith(query)) {
                    combinedResults.add(city);
                }
            }
            
            // 2. Fetch worldwide cities from API (synchronously in background)
            try {
                List<City> apiCities = fetchCitiesFromAPI(query);
                
                // Add API results that aren't already in Tunisian cities
                for (City apiCity : apiCities) {
                    boolean isDuplicate = false;
                    for (City tunisianCity : combinedResults) {
                        if (tunisianCity.getName().equalsIgnoreCase(apiCity.getName()) &&
                            tunisianCity.getCountry().equalsIgnoreCase(apiCity.getCountry())) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!isDuplicate) {
                        combinedResults.add(apiCity);
                    }
                }
            } catch (Exception e) {
                Log.e("CitySearchAdapter", "Error fetching cities from API: " + e.getMessage());
                // Continue with just Tunisian cities
            }
            
            // Limit results to 15 items
            if (combinedResults.size() > 15) {
                combinedResults = combinedResults.subList(0, 15);
            }
            
            results.values = combinedResults;
            results.count = combinedResults.size();
            return results;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredCities.clear();
            if (results.values != null) {
                filteredCities.addAll((List<City>) results.values);
            }
            
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
    
    /**
     * Fetch cities from OpenWeatherMap Geocoding API
     */
    private List<City> fetchCitiesFromAPI(String query) {
        List<City> cities = new ArrayList<>();
        
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlString = GEOCODING_URL + "?q=" + encodedQuery + "&limit=10&appid=" + API_KEY;
            
            Log.d("CitySearchAdapter", "Fetching cities for query: " + query);
            Log.d("CitySearchAdapter", "API URL: " + urlString);
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();
            
            int responseCode = conn.getResponseCode();
            Log.d("CitySearchAdapter", "Response code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                String jsonResponse = response.toString();
                Log.d("CitySearchAdapter", "API Response: " + jsonResponse);
                
                // Parse JSON response
                JSONArray jsonArray = new JSONArray(jsonResponse);
                Log.d("CitySearchAdapter", "Found " + jsonArray.length() + " cities from API");
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject cityObj = jsonArray.getJSONObject(i);
                    
                    String name = cityObj.getString("name");
                    String country = cityObj.getString("country");
                    String state = cityObj.optString("state", null);
                    double lat = cityObj.getDouble("lat");
                    double lon = cityObj.getDouble("lon");
                    
                    City city = new City(name, country, state, lat, lon);
                    cities.add(city);
                    Log.d("CitySearchAdapter", "Added city: " + city.getDisplayName());
                }
            } else {
                Log.e("CitySearchAdapter", "HTTP Error: " + responseCode);
            }
            
            conn.disconnect();
        } catch (Exception e) {
            Log.e("CitySearchAdapter", "API Error: " + e.getMessage(), e);
            e.printStackTrace();
        }
        
        Log.d("CitySearchAdapter", "Returning " + cities.size() + " cities from API");
        return cities;
    }
    
    public void cleanup() {
        executorService.shutdown();
    }
}
