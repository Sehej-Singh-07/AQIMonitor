package aqimonitor;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * BuiltAQIData.java
 * Fetches historical AQI data for the largest US cities from AirNow API and writes to AQI_Data.csv.
 *
 * Requires:
 * - us_cities_N.csv in the same folder, format: city,state,lat,lon (header row included)
 * - org.json library on classpath
 *
 * Output columns:
 * city,state,date,hour,parameter,aqi,category
 */
public class BuiltAQIData {
    private static final String AIRNOW_API_KEY = "D2804F1D-E3E5-4430-8B4B-20F889CC4958";
    private static final String INPUT_CITIES_FILE = "us_cities_4.csv"; // Change as needed
    private static final String OUTPUT_AQI_FILE = "AQI_Data.csv";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalDate START_DATE = LocalDate.of(2025, 6, 10);
    private static final LocalDate END_DATE = LocalDate.of(2025, 6, 11);

    public static void main(String[] args) {
        List<CityInfo> cities = readCities(INPUT_CITIES_FILE);
        if (cities.isEmpty()) {
            System.err.println("No cities found. Please check " + INPUT_CITIES_FILE);
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_AQI_FILE))) {
            // Write CSV header
            writer.write("city,state,date,hour,parameter,aqi,category\n");
            writer.flush();

            for (CityInfo city : cities) {
                System.out.println("Processing: " + city.city + ", " + city.state);
                LocalDate date = START_DATE;
                while (!date.isAfter(END_DATE)) {
                    for (int hour = 0; hour < 24; hour++) {
                        String isoDate = date + "T" + String.format("%02d", hour) + "-0000";
                        JSONArray data = fetchAQIWithRetry(city.lat, city.lon, isoDate);
                        if (data != null) {
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject entry = data.getJSONObject(i);
                                String parameter = entry.optString("ParameterName", "");
                                int aqi = entry.optInt("AQI", -1);
                                String category = entry.has("Category") ?
                                        entry.getJSONObject("Category").optString("Name", "") : "";
                                String dateObserved = entry.optString("DateObserved", "");
                                String hourObserved = "";
                                if (dateObserved.contains("T")) {
                                    String[] parts = dateObserved.split("T");
                                    if (parts.length == 2 && parts[1].length() >= 2) {
                                        hourObserved = parts[1].substring(0, 2);
                                    }
                                }
                                if (hourObserved.isEmpty()) hourObserved = String.valueOf(hour);

                                writer.write(String.join(",",
                                        escape(city.city),
                                        escape(city.state),
                                        date.format(DATE_FORMAT),
                                        hourObserved,
                                        escape(parameter),
                                        String.valueOf(aqi),
                                        escape(category)
                                ));
                                writer.write("\n");
                            }
                            writer.flush();
                        }
                        // Sleep to avoid rate limiting
                        Thread.sleep(1500);
                    }
                    date = date.plusDays(1);
                }
            }
            System.out.println("Done! Output written to " + OUTPUT_AQI_FILE);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Reads city list from a CSV file (expects header row!)
    private static List<CityInfo> readCities(String filename) {
        List<CityInfo> cities = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                String[] parts = line.split(",", -1);
                if (parts.length < 4) continue;
                String city = parts[0].trim();
                String state = parts[1].trim();
                double lat = Double.parseDouble(parts[2]);
                double lon = Double.parseDouble(parts[3]);
                cities.add(new CityInfo(city, state, lat, lon));
            }
        } catch (IOException e) {
            System.err.println("Failed to read " + filename + ": " + e.getMessage());
        }
        return cities;
    }

    // Calls AirNow API for a given lat/lon at a specific ISO8601 dateTime with retry/backoff on 429
    private static JSONArray fetchAQIWithRetry(double lat, double lon, String isoDate) throws InterruptedException {
        String urlString = String.format(
            "https://www.airnowapi.org/aq/observation/latLong/historical/?format=application/json" +
            "&latitude=%.4f&longitude=%.4f&date=%s&distance=25&API_KEY=%s",
            lat, lon, isoDate, AIRNOW_API_KEY
        );
        int maxRetries = 5;
        int retryDelayMs = 120_000; // 2 minutes
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "BuiltAQIData");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                int status = conn.getResponseCode();
                InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();

                if (status == 429) {
                    System.err.println("API error 429 (rate limited) for " + lat + "," + lon + " @ " + isoDate +
                        ". Waiting " + (retryDelayMs / 60000) + " minutes before retry (" + attempt + "/" + maxRetries + ")...");
                    Thread.sleep(retryDelayMs);
                    continue;
                }
                if (status != 200) {
                    System.err.println("API error " + status + " for " + lat + "," + lon + " @ " + isoDate);
                    return null;
                }
                return new JSONArray(sb.toString());
            } catch (IOException e) {
                System.err.println("Failed API call for " + lat + "," + lon + " @ " + isoDate + ": " + e.getMessage());
                return null;
            }
        }
        System.err.println("Max retries reached for " + lat + "," + lon + " @ " + isoDate + ". Skipping.");
        return null;
    }

    // Escapes a CSV value (simple, handles commas and quotes)
    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    // CityInfo class
    private static class CityInfo {
        String city;
        String state;
        double lat;
        double lon;

        CityInfo(String city, String state, double lat, double lon) {
            this.city = city;
            this.state = state;
            this.lat = lat;
            this.lon = lon;
        }
    }
}