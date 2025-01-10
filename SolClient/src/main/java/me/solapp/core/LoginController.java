package me.solapp.core;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import me.solapp.data.CurrentSession;
import me.solapp.network.NetworkClient;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label statusLabel;

    private final NetworkClient client;

    public LoginController() {
        client = new NetworkClient();
    }

    @FXML
    public void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> handleRegister());
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Error: Username and password cannot be empty.");
            return;
        }

        try {
            client.connect("localhost", 8080);
            String response = client.sendRequest("LOGIN " + username + " " + password);
            System.out.println("DEBUG: Received login response: " + response);

            if (response.startsWith("SUCCESS")) {
                // Extract user ID from response and set it in CurrentSession
                try {
                    String userIdStr = response.split("UserId: ")[1].trim();
                    int userId = Integer.parseInt(userIdStr);
                    System.out.println("DEBUG: Setting user ID in session: " + userId);
                    CurrentSession.setUserId(userId);

                    System.out.println("DEBUG: Login successful. Switching to MainView.");
                    switchToMainView();
                } catch (Exception e) {
                    System.err.println("DEBUG: Error parsing user ID from response: " + e.getMessage());
                    statusLabel.setText("Error: System error occurred.");
                }
            } else {
                statusLabel.setText(response);
            }
        } catch (IOException e) {
            statusLabel.setText("Error connecting to server.");
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Error: Username and password cannot be empty.");
            return;
        }

        try {
            client.connect("localhost", 8080);
            String response = client.sendRequest("REGISTER " + username + " " + password);
            System.out.println("DEBUG: Received response: " + response);

            if (response.equalsIgnoreCase("SUCCESS")) {
                switchToMainView();
            } else {
                statusLabel.setText(response);
            }
        } catch (IOException e) {
            statusLabel.setText("Error connecting to server.");
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void switchToMainView() {
        System.out.println("DEBUG: Switching to MainView.fxml...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/MainView.fxml"));
            Scene scene = new Scene(loader.load());

            // Get the current stage
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SolApp - Home");
            System.out.println("DEBUG: Successfully switched to MainView.fxml.");
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error: Unable to load the main view.");
        }
    }

}
