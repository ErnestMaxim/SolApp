package me.solapp;

import me.solapp.data.UserRoles;
import me.solapp.service.ServerConfig;
import me.solapp.data.DatabaseManager;
import me.solapp.network.ClientHandler;
import me.solapp.service.WeatherDataLoader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerMain {

    private static DatabaseManager databaseManager;

    public static void main(String[] args) {
        try {
            // Load server configuration
            System.out.println("Loading configuration...");
            ServerConfig.loadConfig("server.properties");
            System.out.println("Configuration loaded successfully!");

            // Debugging ServerConfig values
            System.out.println("Database URL: " + ServerConfig.getDbUrl());
            System.out.println("Weather Data Path: " + ServerConfig.getWeatherDataPath());
            System.out.println("Schema Generation Strategy: " + ServerConfig.getHbm2ddlAuto());

            // Initialize DatabaseManager with properties
            databaseManager = new DatabaseManager.Builder()
                    .setJdbcDriver(ServerConfig.getDbDriver())
                    .setJdbcUrl(ServerConfig.getDbUrl())
                    .setJdbcUser(ServerConfig.getDbUser())
                    .setJdbcPassword(ServerConfig.getDbPassword())
                    .setJdbcDialect(ServerConfig.getDbDialect())
                    .setDdlGeneration(ServerConfig.getHbm2ddlAuto())
                    .setLoggingLevelSql(String.valueOf(ServerConfig.getShowSql()))
                    .build();

            System.out.println("Database connection initialized successfully.");

            databaseManager.testDatabaseConnection();

            // Load data from JSON before starting the server
            WeatherDataLoader loader = new WeatherDataLoader(databaseManager);
            loader.loadWeatherDataFromFile(ServerConfig.getWeatherDataPath());
            System.out.println("Data loading completed.");

            // Start server
            try (ServerSocket serverSocket = new ServerSocket(ServerConfig.getPort())) {
                System.out.println("Server is running on " + ServerConfig.getHost() +
                        ":" + ServerConfig.getPort());

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " +
                            clientSocket.getInetAddress().getHostAddress());

                    // Create new thread for each client
                    ClientHandler clientHandler = new ClientHandler(clientSocket, databaseManager);
                    new Thread(clientHandler).start();
                }
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (databaseManager != null) {
                databaseManager.close();
            }
        }
    }
}
