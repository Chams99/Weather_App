package com.example.weatherapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Activity for Weather App
 * Implements Samsung Weather app-style UI with hourly and daily forecasts
 * Uses modern Android practices: RecyclerView, smooth animations, pull-to-refresh
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvCity, tvTemp, tvDescription, tvHumidity, tvWind, tvPressure, tvVisibility, tvFeelsLike, tvUVIndex;
    private TextView tvHourlyLabel, tvDailyLabel;
    private android.widget.AutoCompleteTextView etCity;
    private android.widget.ImageButton btnSearch, btnMic;
    private WebViewWeatherIcon ivWeatherIcon;
    private android.widget.ProgressBar progressBar;
    private androidx.constraintlayout.widget.ConstraintLayout rootLayout;
    private RecyclerView rvHourlyForecast, rvDailyForecast;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HourlyForecastAdapter hourlyAdapter;
    private DailyForecastAdapter dailyAdapter;
    private View hourlySection, dailySection;
    private CitySearchAdapter citySearchAdapter;
    
    private final String API_KEY = "YOUR_OPENWEATHER_API_KEY";
    private String city = "Gafsa";
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        
        // Handle system window insets to avoid collision with status bar and navigation bar
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            int horizontalPadding = (int) (20 * getResources().getDisplayMetrics().density); // 20dp to match layout
            int topPadding = systemBars.top + (int) (16 * getResources().getDisplayMetrics().density); // status bar + 16dp
            int bottomPadding = systemBars.bottom + (int) (24 * getResources().getDisplayMetrics().density); // nav bar + 24dp
            v.setPadding(horizontalPadding, topPadding, horizontalPadding, bottomPadding);
            return insets;
        });
        
        setupRecyclerViews();
        setupSearch();
        setupSwipeRefresh();
        fetchWeatherData();
    }
    
    private void initializeViews() {
        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvDescription = findViewById(R.id.tvDescription);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvPressure = findViewById(R.id.tvPressure);
        tvVisibility = findViewById(R.id.tvVisibility);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvUVIndex = findViewById(R.id.tvUVIndex);
        tvHourlyLabel = findViewById(R.id.tvHourlyLabel);
        tvDailyLabel = findViewById(R.id.tvDailyLabel);
        etCity = findViewById(R.id.etCity);
        btnSearch = findViewById(R.id.btnSearch);
        btnMic = findViewById(R.id.btnMic);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        progressBar = findViewById(R.id.progressBar);
        rootLayout = findViewById(R.id.rootLayout);
        rvHourlyForecast = findViewById(R.id.rvHourlyForecast);
        rvDailyForecast = findViewById(R.id.rvDailyForecast);
        hourlySection = findViewById(R.id.hourlySection);
        dailySection = findViewById(R.id.dailySection);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }
    
    private void setupRecyclerViews() {
        // Hourly forecast RecyclerView - horizontal scrolling
        hourlyAdapter = new HourlyForecastAdapter(java.util.Collections.emptyList());
        LinearLayoutManager hourlyLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvHourlyForecast.setLayoutManager(hourlyLayoutManager);
        rvHourlyForecast.setAdapter(hourlyAdapter);
        
        // Daily forecast RecyclerView - vertical list
        dailyAdapter = new DailyForecastAdapter(java.util.Collections.emptyList());
        LinearLayoutManager dailyLayoutManager = new LinearLayoutManager(this);
        rvDailyForecast.setLayoutManager(dailyLayoutManager);
        rvDailyForecast.setAdapter(dailyAdapter);
    }
    
    private void setupSearch() {
        // Setup custom city search adapter with API support
        citySearchAdapter = new CitySearchAdapter(this);
        etCity.setAdapter(citySearchAdapter);
        etCity.setThreshold(1); // Start showing suggestions after 1 character

        btnSearch.setOnClickListener(v -> {
            String newCity = etCity.getText().toString().trim();
            if (!newCity.isEmpty()) {
                city = newCity;
                fetchWeatherData();
            }
        });

        btnMic.setOnClickListener(v -> {
            // Voice search placeholder - can be implemented with SpeechRecognizer
            android.widget.Toast.makeText(this, "Voice search coming soon", android.widget.Toast.LENGTH_SHORT).show();
        });

        etCity.setOnItemClickListener((parent, view, position, id) -> {
            City selectedCity = (City) parent.getItemAtPosition(position);
            if (selectedCity != null) {
                city = selectedCity.getSearchQuery();
                etCity.setText(selectedCity.getDisplayName());
                fetchWeatherData();
            }
        });
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.accent_teal, null),
                getResources().getColor(R.color.accent_blue, null)
        );
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchWeatherData();
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
        if (citySearchAdapter != null) {
            citySearchAdapter.cleanup();
        }
    }

    private void fetchWeatherData() {
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        Handler handler = new Handler(Looper.getMainLooper());

        // Fetch current weather
        executorService.execute(() -> {
            String currentWeatherResponse = fetchFromUrl("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric");
            String hourlyForecastResponse = fetchFromUrl("https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + API_KEY + "&units=metric");
            String dailyForecastResponse = fetchFromUrl("https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + API_KEY + "&units=metric&cnt=40");

            handler.post(() -> {
                progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (currentWeatherResponse != null) {
                    updateUI(currentWeatherResponse);
                } else {
                    showError("Unable to fetch weather data");
                }
                
                if (hourlyForecastResponse != null) {
                    updateHourlyForecast(hourlyForecastResponse);
                }
                
                if (dailyForecastResponse != null) {
                    updateDailyForecast(dailyForecastResponse);
                }
            });
        });
    }
    
    private String fetchFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                return stringBuilder.toString();
            } else {
                Log.e("WeatherApp", "Error: " + responseCode + " for URL: " + urlString);
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e("WeatherApp", "Exception: " + e.getMessage());
        }
        return null;
    }
    
    private void updateHourlyForecast(String jsonResponse) {
        try {
            WeatherForecast forecast = new WeatherForecast(jsonResponse);
            if (forecast.hourlyForecasts != null && !forecast.hourlyForecasts.isEmpty()) {
                hourlyAdapter.updateForecasts(forecast.hourlyForecasts);
                animateViewIn(hourlySection);
                // Animate RecyclerView items with delay
                animateRecyclerViewItems(rvHourlyForecast, 100);
            }
        } catch (Exception e) {
            Log.e("WeatherApp", "Error updating hourly forecast: " + e.getMessage());
        }
    }
    
    private void updateDailyForecast(String jsonResponse) {
        try {
            WeatherForecast forecast = WeatherForecast.parseDailyForecast(jsonResponse);
            if (forecast.dailyForecasts != null && !forecast.dailyForecasts.isEmpty()) {
                dailyAdapter.updateForecasts(forecast.dailyForecasts);
                animateViewIn(dailySection);
                // Animate RecyclerView items with delay
                animateRecyclerViewItems(rvDailyForecast, 80);
            }
        } catch (Exception e) {
            Log.e("WeatherApp", "Error updating daily forecast: " + e.getMessage());
        }
    }
    
    private void animateRecyclerViewItems(RecyclerView recyclerView, int delayPerItem) {
        // Animate RecyclerView items with staggered effect
        recyclerView.post(() -> {
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View child = recyclerView.getChildAt(i);
                if (child != null) {
                    child.setAlpha(0f);
                    child.setTranslationX(-50f);
                    
                    ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(child, "alpha", 0f, 1f);
                    ObjectAnimator translationAnim = ObjectAnimator.ofFloat(child, "translationX", -50f, 0f);
                    
                    android.animation.AnimatorSet itemAnimSet = new android.animation.AnimatorSet();
                    itemAnimSet.playTogether(alphaAnim, translationAnim);
                    itemAnimSet.setDuration(400);
                    itemAnimSet.setStartDelay(i * delayPerItem);
                    itemAnimSet.setInterpolator(new android.view.animation.DecelerateInterpolator());
                    itemAnimSet.start();
                }
            }
        });
    }
    
    private void showError(String message) {
        tvCity.setText("Error");
        tvDescription.setText(message);
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
    
    private void animateViewIn(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setAlpha(0f);
            view.setTranslationY(30f);
            view.setVisibility(View.VISIBLE);
            
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(view, "translationY", 30f, 0f);
            
            android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.setDuration(600);
            animatorSet.setInterpolator(new android.view.animation.DecelerateInterpolator(1.5f));
            animatorSet.start();
        }
    }
    
    private void animateCardsIn() {
        // Animate weather detail cards with staggered effect
        View detailsGrid = findViewById(R.id.detailsGrid);
        if (detailsGrid != null && detailsGrid instanceof android.view.ViewGroup) {
            android.view.ViewGroup grid = (android.view.ViewGroup) detailsGrid;
            for (int i = 0; i < grid.getChildCount(); i++) {
                View child = grid.getChildAt(i);
                if (child != null) {
                    child.setAlpha(0f);
                    child.setScaleX(0.8f);
                    child.setScaleY(0.8f);
                    
                    ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(child, "alpha", 0f, 1f);
                    ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(child, "scaleX", 0.8f, 1f);
                    ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(child, "scaleY", 0.8f, 1f);
                    
                    android.animation.AnimatorSet cardAnimSet = new android.animation.AnimatorSet();
                    cardAnimSet.playTogether(alphaAnim, scaleXAnim, scaleYAnim);
                    cardAnimSet.setDuration(400);
                    cardAnimSet.setStartDelay(i * 50); // Staggered delay
                    cardAnimSet.setInterpolator(new android.view.animation.OvershootInterpolator(1.2f));
                    cardAnimSet.start();
                }
            }
        }
    }

    private void updateUI(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String cityName = jsonObject.getString("name");
            JSONObject sys = jsonObject.getJSONObject("sys");
            String country = sys.getString("country");
            long sunrise = sys.getLong("sunrise");
            long sunset = sys.getLong("sunset");
            
            JSONObject main = jsonObject.getJSONObject("main");
            double temp = main.getDouble("temp");
            int humidity = main.getInt("humidity");
            int pressure = main.getInt("pressure");
            double feelsLike = main.getDouble("feels_like");
            
            JSONObject wind = jsonObject.getJSONObject("wind");
            double windSpeed = wind.getDouble("speed") * 3.6; // Convert m/s to km/h
            
            int visibility = jsonObject.optInt("visibility", 10000) / 1000; // Convert to km

            JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
            String description = weather.getString("description");
            String weatherMain = weather.getString("main").toLowerCase();
            
            // Capitalize first letter of each word in description
            String[] words = description.split(" ");
            StringBuilder capitalizedDesc = new StringBuilder();
            for (String word : words) {
                if (word.length() > 0) {
                    capitalizedDesc.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        capitalizedDesc.append(word.substring(1));
                    }
                    capitalizedDesc.append(" ");
                }
            }

            tvCity.setText(cityName + ", " + country);
            tvTemp.setText(String.format("%.0f°", temp));
            tvDescription.setText(capitalizedDesc.toString().trim());
            tvHumidity.setText(humidity + "%");
            tvWind.setText(String.format("%.0f km/h", windSpeed));
            tvPressure.setText(pressure + " hPa");
            tvVisibility.setText(visibility + " km");
            tvFeelsLike.setText(String.format("%.0f°", feelsLike));
            
            // Calculate UV Index (approximation based on time and weather)
            int uvIndex = calculateUVIndex(sunrise, sunset, weatherMain);
            tvUVIndex.setText(String.valueOf(uvIndex));

            // Map weather condition to SVG and load it
            long currentTime = System.currentTimeMillis() / 1000;
            boolean isNight = currentTime < sunrise || currentTime > sunset;
            String svgFileName = SvgUtils.getWeatherSvgFileName(weatherMain, isNight);
            ivWeatherIcon.loadWeatherIcon(svgFileName);
            
            // Animate weather icon
            animateWeatherIcon();
            
            // Animate cards with staggered effect
            animateCardsIn();
            
            // Update background based on weather and time
            updateBackground(weatherMain, sunrise, sunset);

        } catch (Exception e) {
            Log.e("WeatherApp", "JSON Parsing Error: " + e.getMessage());
            tvDescription.setText("Error parsing data");
        }
    }

    
    private void updateBackground(String condition, long sunrise, long sunset) {
        long currentTime = System.currentTimeMillis() / 1000;
        boolean isNight = currentTime < sunrise || currentTime > sunset;
        
        int backgroundResource;
        
        if (condition.equals("clear")) {
            backgroundResource = isNight ? R.drawable.bg_clear_night : R.drawable.bg_clear_day;
        } else if (condition.equals("clouds")) {
            backgroundResource = R.drawable.bg_cloudy;
        } else if (condition.equals("rain") || condition.equals("drizzle")) {
            backgroundResource = R.drawable.bg_rainy;
        } else if (condition.equals("thunderstorm")) {
            backgroundResource = R.drawable.bg_stormy;
        } else if (condition.equals("snow")) {
            backgroundResource = R.drawable.bg_snowy;
        } else {
            backgroundResource = R.drawable.bg_cloudy;
        }
        
        rootLayout.setBackgroundResource(backgroundResource);
    }
    
    private int calculateUVIndex(long sunrise, long sunset, String condition) {
        long currentTime = System.currentTimeMillis() / 1000;
        boolean isNight = currentTime < sunrise || currentTime > sunset;
        
        if (isNight) {
            return 0; // No UV at night
        }
        
        // Calculate time of day (0-1, where 0.5 is noon)
        long dayStart = sunrise;
        long dayEnd = sunset;
        long dayLength = dayEnd - dayStart;
        long timeSinceSunrise = currentTime - dayStart;
        double timeOfDay = (double) timeSinceSunrise / dayLength;
        
        // Peak UV around noon (timeOfDay = 0.5)
        double peakFactor = 1.0 - Math.abs(timeOfDay - 0.5) * 2.0;
        peakFactor = Math.max(0.3, peakFactor); // Minimum 30% even at sunrise/sunset
        
        // Base UV based on weather condition
        int baseUV;
        switch (condition.toLowerCase()) {
            case "clear":
                baseUV = 8; // High UV on clear days
                break;
            case "clouds":
                baseUV = 5; // Moderate UV with clouds
                break;
            case "rain":
            case "drizzle":
            case "thunderstorm":
                baseUV = 2; // Low UV with rain
                break;
            case "mist":
            case "fog":
            case "haze":
            case "smoke":
                baseUV = 3; // Low UV with mist/fog
                break;
            default:
                baseUV = 5; // Default moderate
        }
        
        // Calculate final UV index
        int uvIndex = (int) Math.round(baseUV * peakFactor);
        return Math.max(0, Math.min(11, uvIndex)); // Clamp between 0-11
    }
    
    private void animateWeatherIcon() {
        // Enhanced weather icon animation with rotation and bounce effect
        ivWeatherIcon.setAlpha(0f);
        ivWeatherIcon.setScaleX(0.3f);
        ivWeatherIcon.setScaleY(0.3f);
        ivWeatherIcon.setRotation(-30f);
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivWeatherIcon, "scaleX", 0.3f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivWeatherIcon, "scaleY", 0.3f, 1.1f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(ivWeatherIcon, "alpha", 0f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(ivWeatherIcon, "rotation", -30f, 10f, 0f);
        
        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha, rotation);
        animatorSet.setDuration(800);
        animatorSet.setInterpolator(new android.view.animation.OvershootInterpolator(1.5f));
        animatorSet.start();
        
        // Add subtle continuous animation for weather icon (like Samsung Meteo)
        startWeatherIconPulse();
    }
    
    private void startWeatherIconPulse() {
        // Subtle pulse animation for immersive effect
        ObjectAnimator pulseScaleX = ObjectAnimator.ofFloat(ivWeatherIcon, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator pulseScaleY = ObjectAnimator.ofFloat(ivWeatherIcon, "scaleY", 1f, 1.05f, 1f);
        
        // Set repeat count on individual animators (AnimatorSet doesn't support setRepeatCount)
        pulseScaleX.setRepeatCount(ObjectAnimator.INFINITE);
        pulseScaleY.setRepeatCount(ObjectAnimator.INFINITE);
        pulseScaleX.setRepeatMode(ObjectAnimator.REVERSE);
        pulseScaleY.setRepeatMode(ObjectAnimator.REVERSE);
        
        android.animation.AnimatorSet pulseSet = new android.animation.AnimatorSet();
        pulseSet.playTogether(pulseScaleX, pulseScaleY);
        pulseSet.setDuration(3000);
        pulseSet.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseSet.setStartDelay(1000);
        pulseSet.start();
    }
}
