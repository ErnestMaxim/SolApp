package me.solapp.core;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import me.solapp.data.CurrentSession;
import me.solapp.network.NetworkClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML private Label day1, day2, day3, day4, day5;
    @FXML private Label temp1, temp2, temp3, temp4, temp5;
    @FXML private HBox topHBox;
    @FXML private Label cityLabel;
    @FXML private Label countryLabel;
    @FXML private Label minMaxTemperature;

    private final NetworkClient client;
    private final StringProperty city = new SimpleStringProperty();
    private final StringProperty country = new SimpleStringProperty();

    private int currentUserId;

    public MainController() {
        client = new NetworkClient();
    }

    @FXML
    public void initialize() {
        cityLabel.textProperty().bind(city);
        countryLabel.textProperty().bind(country);
        setupNavigation();
        setDefaultValues();

        int userId = CurrentSession.getUserId();
        System.out.println("DEBUG: Current user ID in MainController: " + userId);

        if (userId > 0) {
            loadExistingUserLocation(userId);
        } else {
            System.err.println("DEBUG: No valid user ID in session");
        }
    }

    private void loadExistingUserLocation(int userId) {
        try {
            client.connect("localhost", 8080);
            String request = "GET_USER_LOCATION " + userId;
            System.out.println("DEBUG: Loading location for user ID: " + userId);
            String response = client.sendRequest(request);
            System.out.println("DEBUG: Location response: " + response);

            if (response != null && response.startsWith("SUCCESS")) {
                String cityName = response.split(": ")[1].trim();
                System.out.println("DEBUG: Found city name: " + cityName);
                updateWeather(cityName);
            } else {
                System.err.println("DEBUG: Failed to load user location. Response: " + response);
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Error loading user location: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadUserLocation() {
        try {
            client.connect("localhost", 8080);
            String response = client.sendRequest("GET_USER_LOCATION " + currentUserId);
            if (response.startsWith("SUCCESS")) {
                String cityName = response.split(": ")[1];
                updateWeather(cityName);
            }
        } catch (IOException e) {
            System.err.println("DEBUG: Failed to load user location: " + e.getMessage());
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                System.err.println("DEBUG: Error disconnecting: " + e.getMessage());
            }
        }
    }

    private void setDefaultValues() {
        minMaxTemperature.setText("--°C / --°C");
        Label[] dayLabels = {day1, day2, day3, day4, day5};
        Label[] tempLabels = {temp1, temp2, temp3, temp4, temp5};

        for (Label label : dayLabels) {
            if (label != null) label.setText("--");
        }
        for (Label label : tempLabels) {
            if (label != null) label.setText("--°C / --°C");
        }
    }

    private void setupNavigation() {
        if (topHBox == null) {
            System.out.println("DEBUG: topHBox is null! Check fx:id in FXML.");
            return;
        }

        if (topHBox.getChildren().size() > 0 && topHBox.getChildren().get(0) instanceof ImageView) {
            ImageView firstImageView = (ImageView) topHBox.getChildren().get(0);
            firstImageView.setOnMouseClicked(event -> {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                            getClass().getResource("/scenes/MapView.fxml"));
                    javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                    javafx.stage.Stage stage = (javafx.stage.Stage) topHBox.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("SolApp - Location");
                } catch (IOException e) {
                    System.err.println("DEBUG: Failed to load MapView.fxml: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    public boolean updateWeather(String cityName) {
        System.out.println("DEBUG: Updating weather for city: " + cityName);
        try {
            client.connect("localhost", 8080);
            String response = client.sendRequest("GET_WEATHER " + cityName);
            System.out.println("DEBUG: Server response: " + response);

            if (response == null || response.trim().isEmpty() || !response.startsWith("Weather Forecast for")) {
                System.err.println("DEBUG: Invalid or empty response format.");
                return false;
            }

            String[] lines = response.split("\n");
            if (lines.length < 2) {
                System.err.println("DEBUG: Insufficient data in response.");
                return false;
            }

            // Update the city label
            city.set(cityName);

            // Today's weather (second line in the response)
            updateCurrentDayWeather(lines[1]);

            // Forecasts for the next 5 days
            updateForecastDates(List.of(lines).subList(2, Math.min(lines.length, 7)));

            return true;

        } catch (IOException e) {
            System.err.println("DEBUG: IOException during weather update: " + e.getMessage());
            return false;
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                System.err.println("DEBUG: Error disconnecting: " + e.getMessage());
            }
        }
    }

    private String formatTemperature(String forecastLine) {
        try {
            String[] parts = forecastLine.split(": Max Temp: |, Min Temp: ");
            if (parts.length >= 3) {
                String maxTemp = parts[1].replace("°C", "").trim();
                String minTemp = parts[2].replace("°C", "").trim();
                return maxTemp + "°C / " + minTemp + "°C";
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Error formatting temperature: " + e.getMessage());
        }
        return "--°C / --°C";
    }

    private void updateCurrentDayWeather(String currentDayLine) {
        try {
            System.out.println("DEBUG: Updating current day with line: " + currentDayLine);
            String[] parts = currentDayLine.split(": Max Temp: |, Min Temp: ");
            if (parts.length >= 3) {
                String maxTemp = parts[1].replace("°C", "").trim();
                String minTemp = parts[2].replace("°C", "").trim();
                minMaxTemperature.setText(maxTemp + "°C / " + minTemp + "°C");
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Error parsing current day weather: " + e.getMessage());
            minMaxTemperature.setText("--°C / --°C");
        }
    }

    private void handleLoginSuccess(String response) {
        // Assuming the response is in the format "SUCCESS: Logged in. UserId: X"
        try {
            String userIdStr = response.split("UserId: ")[1].split("\n")[0];
            int userId = Integer.parseInt(userIdStr);
            CurrentSession.setUserId(userId);
            System.out.println("DEBUG: Set user ID in session: " + userId);
            // Continue with login success handling
        } catch (Exception e) {
            System.err.println("Error parsing user ID from login response: " + e.getMessage());
        }
    }


    private void updateForecastDates(List<String> forecastLines) {
        Label[] dayLabels = {day1, day2, day3, day4, day5};
        Label[] tempLabels = {temp1, temp2, temp3, temp4, temp5};

        System.out.println("DEBUG: Updating forecasts with " + forecastLines.size() + " lines");

        for (int i = 0; i < dayLabels.length && i < forecastLines.size(); i++) {
            String line = forecastLines.get(i);
            try {
                if (dayLabels[i] != null) {
                    dayLabels[i].setText(formatDate(line));
                }
                if (tempLabels[i] != null) {
                    tempLabels[i].setText(formatTemperature(line));
                }
            } catch (Exception e) {
                System.err.println("DEBUG: Error updating forecast " + i + ": " + e.getMessage());
            }
        }
    }

    private String formatDate(String forecastLine) {
        try {
            String date = forecastLine.split(": Max")[0].trim();
            return date; // Return the parsed date (e.g., "2025-01-10")
        } catch (Exception e) {
            System.err.println("DEBUG: Error formatting date: " + e.getMessage());
            return "--";
        }
    }

    public boolean updateLocationForUser(int userId, String cityName) {
        try {
            client.connect("localhost", 8080);
            String request = "UPDATE_LOCATION " + userId + " " + cityName;
            System.out.println("DEBUG: Sending request: " + request);
            String response = client.sendRequest(request);
            System.out.println("DEBUG: Server response: " + response);

            if (response.startsWith("SUCCESS")) {
                System.out.println("DEBUG: Location updated successfully for user ID: " + userId);
                return true;
            } else {
                System.err.println("DEBUG: Failed to update location: " + response);
                return false;
            }
        } catch (IOException e) {
            System.err.println("DEBUG: IOException during location update: " + e.getMessage());
            return false;
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                System.err.println("DEBUG: Error disconnecting: " + e.getMessage());
            }
        }
    }


}