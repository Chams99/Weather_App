# Weather App

A modern Android weather application with a Samsung Weather app-inspired UI, featuring real-time weather data, hourly and daily forecasts, and smooth animations.

## 📱 Overview

This Android application provides comprehensive weather information for cities worldwide, with a focus on Tunisian cities. The app displays current weather conditions, hourly forecasts for the next 24 hours, and daily forecasts for up to 7 days. The UI features smooth animations, dynamic backgrounds that change based on weather conditions, and a glassmorphism design aesthetic.

## ✨ Features

### Core Functionality
- **Current Weather Display**: Shows temperature, weather description, and key metrics
- **Hourly Forecast**: Horizontal scrolling list of weather conditions for the next 24 hours
- **Daily Forecast**: 7-day weather forecast with min/max temperatures
- **City Search**: Autocomplete search with support for 100+ cities (focus on Tunisia)
- **Pull-to-Refresh**: Swipe down to refresh weather data
- **Dynamic Backgrounds**: Background changes based on weather condition and time of day

### Weather Metrics
- Temperature (current and feels-like)
- Humidity percentage
- Wind speed (km/h)
- Atmospheric pressure (hPa)
- Visibility (km)
- UV Index (calculated based on time and weather conditions)
- Weather condition icons

### UI/UX Features
- **Smooth Animations**: Staggered animations for forecast items and weather cards
- **Weather Icon Animations**: Animated weather icons with pulse effects
- **Glassmorphism Design**: Modern glass-effect cards and search bar
- **Responsive Layout**: Adapts to different screen sizes
- **System Window Insets**: Proper handling of status bar and navigation bar
- **Color-coded UI**: Accent colors for different UI elements

## 🏗️ Project Structure

```
weather_app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/weatherapp/
│   │   │   │   ├── MainActivity.java          # Main activity with UI logic
│   │   │   │   ├── WeatherForecast.java       # Data models for forecasts
│   │   │   │   ├── HourlyForecastAdapter.java # RecyclerView adapter for hourly forecast
│   │   │   │   └── DailyForecastAdapter.java  # RecyclerView adapter for daily forecast
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml      # Main activity layout
│   │   │   │   │   ├── item_hourly_forecast.xml
│   │   │   │   │   └── item_daily_forecast.xml
│   │   │   │   ├── drawable/                  # Weather icons and backgrounds
│   │   │   │   ├── values/                    # Colors, strings, themes
│   │   │   │   └── xml/                       # Backup rules, data extraction
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle                       # App-level dependencies
├── build.gradle                               # Project-level build config
└── gradle/                                    # Gradle wrapper
```

## 🔑 Key Components

### MainActivity.java
The main activity that handles:
- **UI Initialization**: Sets up all views, RecyclerViews, and adapters
- **Weather Data Fetching**: Makes API calls to OpenWeatherMap in background threads
- **Data Parsing**: Parses JSON responses and updates UI
- **Animations**: Manages all view animations (cards, icons, RecyclerView items)
- **Search Functionality**: Handles city search with autocomplete
- **Background Updates**: Dynamically changes background based on weather and time

**Key Methods:**
- `fetchWeatherData()`: Fetches current weather, hourly, and daily forecasts
- `updateUI()`: Updates current weather display with parsed data
- `updateHourlyForecast()`: Updates hourly forecast RecyclerView
- `updateDailyForecast()`: Updates daily forecast RecyclerView
- `animateViewIn()`: Animates forecast sections into view
- `animateRecyclerViewItems()`: Staggered animation for RecyclerView items
- `updateBackground()`: Changes background based on weather condition
- `calculateUVIndex()`: Calculates UV index based on time and weather

### WeatherForecast.java
Data model classes for weather forecasts:

**HourlyForecast**: Represents a single hourly forecast entry
- `timestamp`: Unix timestamp
- `temperature`: Temperature in Celsius
- `condition`: Weather condition (clear, clouds, rain, etc.)
- `icon`: Weather icon identifier
- `windSpeed`: Wind speed in km/h
- `humidity`: Humidity percentage
- `timeString`: Formatted time (HH:mm)

**DailyForecast**: Represents a single daily forecast entry
- `timestamp`: Unix timestamp
- `minTemp`/`maxTemp`: Min and max temperatures
- `condition`: Weather condition
- `icon`: Weather icon identifier
- `windSpeed`: Wind speed in km/h
- `humidity`: Humidity percentage
- `dayName`: Day of week (e.g., "Monday")
- `dateString`: Formatted date (MMM dd)

**Methods:**
- `parseForecast()`: Parses hourly forecast from JSON
- `parseDailyForecast()`: Groups forecast data by day and calculates min/max temps

### HourlyForecastAdapter.java
RecyclerView adapter for horizontal scrolling hourly forecast:
- Displays time, weather icon, and temperature
- Maps weather conditions to appropriate icons
- Updates forecast list dynamically

### DailyForecastAdapter.java
RecyclerView adapter for vertical daily forecast list:
- Displays day name, date, weather icon, min/max temperatures
- Shows "Today" for the first item
- Maps weather conditions to appropriate icons

## 🌐 API Integration

### OpenWeatherMap API
The app uses OpenWeatherMap's free tier API:
- **Current Weather**: `https://api.openweathermap.org/data/2.5/weather`
- **Forecast**: `https://api.openweathermap.org/data/2.5/forecast`

**API Key**: The app includes an API key (stored in `MainActivity.java`). For production, this should be moved to a secure location or environment variables.

**API Parameters:**
- `q`: City name
- `appid`: API key
- `units`: metric (for Celsius)
- `cnt`: Number of forecast items (for daily forecast)

### Network Implementation
- Uses `HttpURLConnection` for API calls
- Executes network requests in background thread pool (`ExecutorService`)
- 10-second timeout for connections
- Error handling with user-friendly messages

## 🎨 UI Design

### Layout Structure
1. **Search Container**: Glass-effect search bar with autocomplete and voice search button
2. **Current Weather Section**: 
   - Large weather icon with animation
   - City name and country
   - Temperature display
   - Weather description
3. **Weather Details Grid**: Cards showing humidity, wind, pressure, visibility, feels-like, and UV index
4. **Hourly Forecast Section**: Horizontal RecyclerView with scrollable forecast cards
5. **Daily Forecast Section**: Vertical RecyclerView with daily forecast items

### Animations
- **Weather Icon**: Scale, rotation, and alpha animation with overshoot effect
- **Icon Pulse**: Continuous subtle pulse animation
- **Forecast Sections**: Fade-in with translation animation
- **RecyclerView Items**: Staggered fade-in with horizontal translation
- **Detail Cards**: Staggered scale and fade-in with overshoot effect

### Backgrounds
Dynamic backgrounds change based on:
- Weather condition (clear, clouds, rain, thunderstorm, snow)
- Time of day (day/night for clear weather)
- Background drawables: `bg_clear_day`, `bg_clear_night`, `bg_cloudy`, `bg_rainy`, `bg_stormy`, `bg_snowy`

### Weather Icons
Vector drawables for different conditions:
- `ic_weather_clear`: Clear/sunny weather
- `ic_weather_clouds`: Cloudy weather
- `ic_weather_rain`: Rain/drizzle
- `ic_weather_storm`: Thunderstorm
- `ic_weather_snow`: Snow
- `ic_weather_mist`: Mist/fog/haze

## 🛠️ Technical Details

### Dependencies
```gradle
- androidx.appcompat:appcompat:1.6.1
- com.google.android.material:material:1.11.0
- androidx.constraintlayout:constraintlayout:2.1.4
- androidx.recyclerview:recyclerview:1.3.2
- androidx.cardview:cardview:1.0.0
- androidx.swiperefreshlayout:swiperefreshlayout:1.1.0
- retrofit2 (included but not actively used - using HttpURLConnection instead)
```

### Build Configuration
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Java Version**: 17
- **Build Tools**: Android Gradle Plugin 8.13.1

### Permissions
- `INTERNET`: Required for API calls

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version recommended)
- JDK 17 or higher
- Android SDK with API level 24+
- Internet connection for API calls

### Setup Instructions

1. **Clone or Download** the project
   ```bash
   git clone <repository-url>
   cd weather_app
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `weather_app` folder

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle dependencies
   - Wait for the sync to complete

4. **API Key** (Optional)
   - The app includes a default API key
   - For production, get your own API key from [OpenWeatherMap](https://openweathermap.org/api)
   - Replace the API key in `MainActivity.java` line 45:
     ```java
     private final String API_KEY = "your_api_key_here";
     ```

5. **Build and Run**
   - Connect an Android device or start an emulator
   - Click "Run" or press `Shift+F10`
   - The app will build and install on your device

### Running the App
1. Launch the app on your device
2. The app will load weather for the default city (Gafsa, Tunisia)
3. Use the search bar to find other cities
4. Pull down to refresh weather data
5. Scroll horizontally through hourly forecasts
6. Scroll vertically through daily forecasts

## 📝 Code Architecture

### Threading Model
- **Main Thread**: UI updates only
- **Background Threads**: Network requests executed in `ExecutorService` thread pool
- **Handler**: Posts results back to main thread for UI updates

### Data Flow
1. User action (search, refresh) triggers `fetchWeatherData()`
2. Background thread makes API calls
3. JSON responses are parsed into model objects
4. UI is updated on main thread via `Handler.post()`
5. RecyclerView adapters are updated with new data
6. Animations are triggered for visual feedback

### Error Handling
- Network errors are caught and logged
- User-friendly error messages displayed via Toast
- UI shows "Error" state when data fetch fails
- Graceful degradation if forecast data is unavailable

## 🎯 Supported Cities

The app includes autocomplete support for:
- **Tunisian Cities**: All major cities and towns (Tunis, Sfax, Sousse, Kairouan, Gafsa, etc.)
- **World Cities**: Major international cities (London, Paris, New York, Tokyo, Dubai, etc.)

You can search for any city supported by OpenWeatherMap API, even if not in the autocomplete list.

## 🔮 Future Enhancements

Potential improvements (not yet implemented):
- Voice search functionality (button exists but shows placeholder)
- Location-based weather using GPS
- Weather alerts and notifications
- Weather maps
- Historical weather data
- Multiple location support
- Widget support
- Dark mode toggle
- Unit preferences (Celsius/Fahrenheit)
- Weather data caching

## 🐛 Known Issues

- Voice search button shows placeholder message
- UV Index is calculated/estimated, not from actual API data
- Daily forecast min/max temperatures are calculated from hourly data (OpenWeatherMap free tier limitation)

## 📄 License

This project is provided as-is for educational and development purposes.

## 👨‍💻 Development Notes

### Code Style
- Java 17 syntax
- Modern Android practices (RecyclerView, Material Design)
- Comprehensive comments and documentation
- Clean separation of concerns

### Performance Considerations
- RecyclerView for efficient list rendering
- Background threading for network operations
- Efficient JSON parsing
- View recycling in adapters

### Best Practices Implemented
- Proper lifecycle management (executor shutdown in `onDestroy()`)
- System window insets handling
- Error handling and logging
- Resource management
- Smooth animations without blocking UI thread

## 📞 Support

For issues or questions:
1. Check the code comments for implementation details
2. Review the OpenWeatherMap API documentation
3. Ensure you have internet connectivity
4. Verify API key is valid (if using custom key)

---

**Note**: This is an Android native application written in Java. Despite the folder name suggesting Flutter, this is a pure Android project using Java and XML layouts.

