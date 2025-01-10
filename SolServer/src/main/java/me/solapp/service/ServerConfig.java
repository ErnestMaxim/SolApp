package me.solapp.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfig {
    private static final Properties properties = new Properties();

    /**
     * Loads the configuration file from the specified filepath.
     *
     * @param filepath Path to the configuration file (e.g., server.properties).
     * @throws IOException If the file is not found or cannot be loaded.
     */
    public static void loadConfig(String filepath) {
        try (InputStream fis = ServerConfig.class.getClassLoader().getResourceAsStream(filepath)) {
            if (fis == null) {
                throw new FileNotFoundException("Configuration file not found: " + filepath);
            }
            properties.load(fis);
            System.out.println("Configuration loaded successfully from: " + filepath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file: " + filepath, e);
        }
    }


    public static int getPort() {
        return Integer.parseInt(properties.getProperty("server.port", "8080"));
    }

    public static String getHost() {
        return properties.getProperty("server.host", "localhost");
    }

    public static String getDbDriver() {
        return properties.getProperty("db.driver", "org.sqlite.JDBC");
    }

    public static String getDbUrl() {
        return properties.getProperty("db.url", "jdbc:sqlite:D:/clion_projects/JavaDev/SolApp/SolServer/solapp.db");
    }

    public static String getDbUser() {
        return properties.getProperty("db.user", "");
    }

    public static String getDbPassword() {
        return properties.getProperty("db.password", "");
    }

    public static String getDbDialect() {
        return properties.getProperty("db.dialect", "org.hibernate.community.dialect.SQLiteDialect");
    }

    public static boolean getShowSql() {
        return Boolean.parseBoolean(properties.getProperty("db.show_sql", "false"));
    }

    public static String getHbm2ddlAuto() {
        return properties.getProperty("db.hbm2ddl.auto", "update");
    }

    public static String getWeatherDataPath() {
        return properties.getProperty("weather.data.path", "D:/clion_projects/JavaDev/SolApp/sol.json");
    }

    public static int getWeatherUpdateInterval() {
        return Integer.parseInt(properties.getProperty("weather.update.interval", "3600"));
    }
}
