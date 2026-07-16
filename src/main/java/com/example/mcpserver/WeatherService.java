package com.example.mcpserver;

import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * Demo tools exposed over MCP.
 *
 * Each @Tool method becomes something Claude can call. The description on the
 * method (and on each @ToolParam) is what the model reads to decide when and
 * how to call it, so keep these specific and unambiguous.
 *
 * This demo uses a small in-memory dataset instead of a real weather API, so
 * it runs with zero external dependencies or API keys. Swap getForecast /
 * getAlerts for real HTTP calls (e.g. via RestClient) once you're ready to
 * wire up a live data source.
 */
@Service
public class WeatherService {

    private record Forecast(String condition, int highF, int lowF) {}

    private static final Map<String, Forecast> FORECASTS = Map.of(
            "phoenix", new Forecast("Sunny", 108, 84),
            "scottsdale", new Forecast("Sunny", 107, 83),
            "seattle", new Forecast("Light rain", 68, 55),
            "new york", new Forecast("Partly cloudy", 79, 64),
            "chicago", new Forecast("Thunderstorms", 82, 68)
    );

    private static final Map<String, List<String>> ALERTS = Map.of(
            "az", List.of("Excessive Heat Warning in effect until 8 PM MST"),
            "wa", List.of("Flood Watch in effect for coastal areas"),
            "ny", List.of(),
            "il", List.of("Severe Thunderstorm Watch until 10 PM CST")
    );

    @Tool(description = "Get the current weather forecast for a US city")
    public String getForecast(
            @ToolParam(description = "City name, e.g. 'Phoenix' or 'New York'") String city) {

        Forecast forecast = FORECASTS.get(city.toLowerCase().trim());
        if (forecast == null) {
            return "No forecast data available for '" + city + "'. Try one of: Phoenix, Scottsdale, Seattle, New York, Chicago.";
        }
        return "Forecast for " + city + ": " + forecast.condition()
                + ", high " + forecast.highF() + "°F, low " + forecast.lowF() + "°F.";
    }

    @Tool(description = "Get any active weather alerts for a given US state, by two-letter state code")
    public String getAlerts(
            @ToolParam(description = "Two-letter US state code, e.g. 'AZ' or 'NY'") String stateCode) {

        List<String> alerts = ALERTS.get(stateCode.toLowerCase().trim());
        if (alerts == null) {
            return "No alert data available for state code '" + stateCode + "'. Try one of: AZ, WA, NY, IL.";
        }
        if (alerts.isEmpty()) {
            return "No active weather alerts for " + stateCode.toUpperCase() + ".";
        }
        return "Active alerts for " + stateCode.toUpperCase() + ": " + String.join("; ", alerts);
    }
}
