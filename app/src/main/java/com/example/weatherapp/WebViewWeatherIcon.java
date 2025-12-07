package com.example.weatherapp;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Custom WebView for displaying animated weather SVG icons
 * Supports transparent background and proper scaling
 */
public class WebViewWeatherIcon extends WebView {
    
    public WebViewWeatherIcon(Context context) {
        super(context);
        init();
    }
    
    public WebViewWeatherIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public WebViewWeatherIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Configure WebView settings for optimal SVG rendering
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(false); // SVG animations use CSS, not JS
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        
        // Enable transparent background
        setBackgroundColor(Color.TRANSPARENT);
        setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
    }
    
    /**
     * Load an animated SVG icon from assets
     * @param svgFileName The SVG file name (e.g., "clear-day.svg")
     */
    public void loadWeatherIcon(String svgFileName) {
        try {
            // Read SVG content from assets
            String svgContent = readAssetFile(svgFileName);
            
            // Create HTML wrapper with embedded SVG
            String html = createHtmlWrapper(svgContent);
            
            // Load HTML with base URL for assets
            loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
        } catch (IOException e) {
            e.printStackTrace();
            // Load fallback icon on error
            loadFallbackIcon();
        }
    }
    
    /**
     * Read file content from assets
     */
    private String readAssetFile(String fileName) throws IOException {
        InputStream inputStream = getContext().getAssets().open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        
        reader.close();
        inputStream.close();
        
        return stringBuilder.toString();
    }
    
    /**
     * Create HTML wrapper for SVG with proper styling
     */
    private String createHtmlWrapper(String svgContent) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>" +
                "<style>" +
                "* { margin: 0; padding: 0; }" +
                "html, body { " +
                "  width: 100%; " +
                "  height: 100%; " +
                "  background: transparent; " +
                "  display: flex; " +
                "  align-items: center; " +
                "  justify-content: center; " +
                "  overflow: hidden; " +
                "}" +
                "svg { " +
                "  width: 100%; " +
                "  height: 100%; " +
                "  max-width: 100%; " +
                "  max-height: 100%; " +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                svgContent +
                "</body>" +
                "</html>";
    }
    
    /**
     * Load a fallback icon when the requested icon fails to load
     */
    private void loadFallbackIcon() {
        String fallbackHtml = createHtmlWrapper(
            "<svg viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'>" +
            "<circle cx='50' cy='50' r='30' fill='#FFD700'/>" +
            "</svg>"
        );
        loadDataWithBaseURL("file:///android_asset/", fallbackHtml, "text/html", "UTF-8", null);
    }
}
