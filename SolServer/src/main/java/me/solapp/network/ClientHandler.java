package me.solapp.network;

import me.solapp.data.*;
import me.solapp.service.*;

import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.time.format.DateTimeFormatter;


public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final UsersService userService;
    private final CitiesService cityService;
    private final WeatherDailyForecastService forecastLogService;
    private final WeatherStatusService weatherStatusService;
    private final WeatherDataLoader weatherDataLoader;
    private Optional<Users> currentUser;


    public ClientHandler(Socket clientSocket, DatabaseManager databaseManager) {
        this.clientSocket = clientSocket;
        this.userService = databaseManager.getUserService();
        this.cityService = databaseManager.getCityService();
        this.forecastLogService = databaseManager.getWeatherDailyForecastLogService();
        this.weatherStatusService = databaseManager.getWeatherStatusService();
        this.weatherDataLoader = new WeatherDataLoader(databaseManager);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            out.println("Connected to Weather App Server!"); // Initial response
            String input;

            while ((input = in.readLine()) != null) {
                System.out.println("DEBUG: Received request from client: " + input);

                String response = handleRequest(input.trim());
                if (response == null || response.isEmpty()) {
                    response = "ERROR: Server failed to process the request.";
                    System.out.println("DEBUG: Null or empty response generated. Returning error response.");
                }

                out.println(response); // Send response back to client
                System.out.println("DEBUG: Response sent to client: " + response);

                if (Protocol.EXIT.equalsIgnoreCase(input.trim())) {
                    System.out.println("DEBUG: Client requested to exit.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("DEBUG: IOException in ClientHandler: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("DEBUG: Closing client socket.");
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("DEBUG: Failed to close client socket: " + e.getMessage());
            }
        }
    }



    private String handleRequest(String input) {
        try {
            System.out.println("DEBUG: Received request: " + input);

            // Split input into parts
            String[] parts = input.split(" ", 3); // Split only into 3 parts to handle spaces in data
            if (parts.length == 0) {
                System.out.println("DEBUG: No command received.");
                return "ERROR: No command received.";
            }

            // Extract command
            String command = parts[0].toUpperCase();
            System.out.println("DEBUG: Processing command: " + command);

            // Handle commands
            switch (command) {
                case Protocol.LOGIN:
                    return handleLogin(parts);

                case Protocol.REGISTER:
                    return handleRegister(parts);

                case Protocol.GET_WEATHER:
                    return handleGetWeather(parts);

                case Protocol.EXIT:
                    return "Goodbye!";

                case Protocol.UPDATE_LOCATION:
                    return handleUpdateLocation(parts);

                case Protocol.UPLOAD_JSON:
                    return handleUploadJson(parts);

                case Protocol.GET_USER_LOCATION:
                    return handleGetUserLocation(parts);

                default:
                    System.out.println("DEBUG: Unknown command: " + command);
                    return "ERROR: Unknown command.";
            }
        } catch (Exception e) {
            // Log and return a general error response
            System.out.println("DEBUG: Exception occurred while processing request: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: Server encountered an error while processing the request.";
        }
    }


    private String handleUpdateLocation(String[] parts) {
        if (parts.length < 3) {
            return "ERROR: Missing user ID or location.\nEND_OF_RESPONSE";
        }

        try {
            int userId = Integer.parseInt(parts[1]);
            String cityName = parts[2];
            System.out.println("DEBUG: Updating location - User ID: " + userId + ", City: " + cityName);

            Optional<Cities> city = cityService.findByName(cityName);
            if (city.isEmpty()) {
                System.err.println("DEBUG: City not found: " + cityName);
                return "ERROR: City not found.\nEND_OF_RESPONSE";
            }

            // Update user's location with proper transaction
            userService.updateUserLocation(userId, city.get());
            System.out.println("DEBUG: Location updated successfully in database");
            return "SUCCESS: Location updated to " + cityName + "\nEND_OF_RESPONSE";

        } catch (NumberFormatException e) {
            System.err.println("DEBUG: Invalid user ID format: " + parts[1]);
            return "ERROR: Invalid user ID format.\nEND_OF_RESPONSE";
        } catch (Exception e) {
            System.err.println("DEBUG: Error updating location: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: Failed to update location.\nEND_OF_RESPONSE";
        }
    }



    private String handleLogin(String[] parts) {
        if (parts.length < 3) {
            return "ERROR: Missing username or password.\nEND_OF_RESPONSE";
        }

        String username = parts[1];
        String password = parts[2];
        Optional<Users> user = userService.authenticate(username, password);

        if (user.isPresent()) {
            int userId = user.get().getId(); // Retrieve the user ID
            return "SUCCESS: Logged in. UserId: " + userId + "\nEND_OF_RESPONSE";
        } else {
            return "ERROR: Invalid credentials.\nEND_OF_RESPONSE";
        }
    }

    private String handleRegister(String[] parts) {
        if (parts.length < 3) {
            System.out.println("DEBUG: REGISTER failed - Missing username or password.");
            return "ERROR: Missing username or password.\nEND_OF_RESPONSE";
        }

        String newUsername = parts[1];
        String newPassword = parts[2];
        System.out.println("DEBUG: Received REGISTER request for username: " + newUsername);

        // Check if username already exists
        Optional<Users> existingUser = userService.findByUsername(newUsername);
        if (existingUser.isPresent()) {
            System.out.println("DEBUG: Username already exists: " + newUsername);
            System.out.println("DEBUG: Found user: " + existingUser.get());
            return "ERROR: Username already exists.\nEND_OF_RESPONSE";
        }

        System.out.println("DEBUG: Username not found in the database: " + newUsername);

        // Fetch client role
        UserRoles clientRole = userService.findRoleById(2); // Assuming role_id = 2 is for Client
        if (clientRole == null) {
            System.out.println("DEBUG: Role ID 2 not found in database.");
            return "ERROR: Role not found in the database.\nEND_OF_RESPONSE";
        }

        try {
            // Attempt registration
            boolean isRegistered = userService.register(newUsername, newPassword, clientRole);
            if (isRegistered) {
                System.out.println("DEBUG: Registration successful for username: " + newUsername);
                return "SUCCESS: Registration completed successfully.\nEND_OF_RESPONSE";
            } else {
                System.out.println("DEBUG: Registration failed - Unknown reason.");
                return "ERROR: Failed to register user.\nEND_OF_RESPONSE";
            }
        } catch (Exception e) {
            // Log exception details
            System.out.println("DEBUG: Exception during registration: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: An unexpected error occurred during registration.\nEND_OF_RESPONSE";
        }
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String handleGetWeather(String[] parts) {
        if (parts.length < 2) {
            return "ERROR: Missing city name.";
        }

        String cityName = parts[1];
        var city = cityService.findByName(cityName);

        if (city.isEmpty()) {
            return "ERROR: City not found.";
        }

        List<WeatherDailyForecast> forecasts = forecastLogService.findForecastsByCity(city);

        if (forecasts.isEmpty()) {
            return "No forecasts available for " + cityName + ".";
        }

        StringBuilder response = new StringBuilder("Weather Forecast for " + cityName + ":\n");
        for (WeatherDailyForecast forecast : forecasts) {
            response.append(forecast.getForecastDate())
                    .append(": Max Temp: ").append(forecast.getMaxTemperature())
                    .append("°C, Min Temp: ").append(forecast.getMinTemperature())
                    .append("°C\n");
        }

        response.append("END_OF_RESPONSE"); // Add a clear terminator
        System.out.println("DEBUG: Generated response: " + response);
        return response.toString();
    }

    private String handleUploadJson(String[] parts) {
        if (currentUser.isEmpty() || !currentUser.get().getRole().isAdmin()) {
            return "ERROR: Unauthorized action. Admin rights required.";
        }

        if (parts.length < 2) {
            return "ERROR: Missing file path.";
        }

        String filePath = parts[1];
        try {
            weatherDataLoader.loadWeatherDataFromFile(filePath);
            return "SUCCESS: Weather data uploaded.";
        } catch (Exception e) {
            return "ERROR: Failed to upload weather data. " + e.getMessage();
        }
    }

    private String handleGetUserLocation(String[] parts) {
        if (parts.length < 2) {
            return "ERROR: Missing user ID.\nEND_OF_RESPONSE";
        }

        try {
            int userId = Integer.parseInt(parts[1]);
            System.out.println("DEBUG: Getting location for user ID: " + userId);

            Users user = userService.findById(userId);
            if (user != null && user.getLocation() != null) {
                String cityName = user.getLocation().getCityName();
                System.out.println("DEBUG: Found user location: " + cityName);
                return "SUCCESS: " + cityName + "\nEND_OF_RESPONSE";
            } else {
                System.out.println("DEBUG: No location found for user ID: " + userId);
                return "ERROR: No location set for user.\nEND_OF_RESPONSE";
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Error getting user location: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: Failed to get user location.\nEND_OF_RESPONSE";
        }
    }


}
