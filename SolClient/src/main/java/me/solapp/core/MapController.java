package me.solapp.core;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import me.solapp.data.CurrentSession;
import me.solapp.network.NetworkClient;

import java.io.IOException;

public class MapController {
    @FXML
    private HBox topHBox;

    @FXML
    private TextField locationTextField;

    @FXML
    private Button findButton;

    private int currentUserId;

    @FXML
    public void initialize() {
        findButton.setOnAction(event -> processLocation());
        setupNavigation();
        currentUserId = CurrentSession.getUserId();
    }

    private void processLocation() {
        String location = locationTextField.getText().trim();
        if (location.isEmpty()) {
            showErrorMessage("Error", "No location entered!", "Please enter a valid location.");
            return;
        }

        // Update location in database first
        if (currentUserId > 0) {
            boolean locationUpdated = updateLocationForUser(currentUserId, location);
            if (!locationUpdated) {
                showErrorMessage("Error", "Failed to update location",
                        "The location '" + location + "' could not be set for your account. Please try again.");
                return;
            }
        }

        // Then switch to main view
        switchToMainView(location);
    }

    private boolean isUserLoggedIn() {
        int userId = CurrentSession.getUserId();
        System.out.println("DEBUG: Checking login status for user ID: " + userId);
        return userId > 0;
    }

    private boolean updateLocationForUser(int userId, String cityName) {
        try {
            NetworkClient client = new NetworkClient(); // Create or get your network client
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
                NetworkClient client = new NetworkClient(); // Ensure client is closed properly
                client.disconnect();
            } catch (IOException e) {
                System.err.println("DEBUG: Error disconnecting: " + e.getMessage());
            }
        }
    }



    private void setupNavigation() {
        if (topHBox == null || topHBox.getChildren().isEmpty()) {
            System.out.println("topHBox is null or empty! Check fx:id in FXML.");
            return;
        }

        // Remove the default location navigation - require user input instead
        if (topHBox.getChildren().get(0) instanceof ImageView) {
            ImageView firstImageView = (ImageView) topHBox.getChildren().get(0);
            firstImageView.setOnMouseClicked(event -> {
                if (locationTextField.getText().trim().isEmpty()) {
                    showErrorMessage("Error", "No location selected", "Please enter a location first.");
                } else {
                    switchToMainView(locationTextField.getText().trim());
                }
            });
        }
    }

    private void switchToMainView(String location) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/MainView.fxml"));
            Scene scene = new Scene(loader.load());

            MainController mainController = loader.getController();
            // Pass the current session information if needed
            if (!mainController.updateWeather(location)) {
                showErrorMessage("Error", "Location not found",
                        "The location '" + location + "' was not found in our database. Please try another location.");
                return;
            }

            Stage stage = (Stage) topHBox.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SolApp - Weather for " + location);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Error", "System Error",
                    "Failed to load weather view. Please try again later.");
        }
    }

    private void showErrorMessage(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}