package me.solapp.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.transaction.Transactional;
import me.solapp.data.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WeatherDataLoader {
    private static final Logger LOGGER = LogManager.getLogger(WeatherDataLoader.class);

    private final CitiesService cityService;
    private final CountriesService countryService;
    private final WeatherStatusService weatherStatusService;
    private final WeatherDailyForecastService forecastLogService;

    public WeatherDataLoader(DatabaseManager dbManager) {
        this.cityService = dbManager.getCityService();
        this.countryService = dbManager.getCountryService();
        this.weatherStatusService = dbManager.getWeatherStatusService();
        this.forecastLogService = dbManager.getWeatherDailyForecastLogService();
        LOGGER.debug("WeatherDataLoader initialized with services.");
    }

    private <T> T loadOrCreateEntity(String name, Function<String, Optional<T>> findByName, Supplier<T> newEntitySupplier, Consumer<T> saveEntity) {
        LOGGER.debug("Attempting to load or create entity: " + name);
        return findByName.apply(name).orElseGet(() -> {
            LOGGER.debug("Entity not found. Creating new entity: " + name);
            T entity = newEntitySupplier.get();
            saveEntity.accept(entity);
            LOGGER.debug("Entity saved: " + entity);
            return findByName.apply(name).orElseThrow(() -> new RuntimeException("Failed to save entity: " + name));
        });
    }

    public void loadWeatherDataFromFile(String filePath) {
        LOGGER.info("Loading weather data from file: " + filePath);
        try (InputStream input = new FileInputStream(filePath);
             InputStreamReader reader = new InputStreamReader(input)) {

            JsonObject jsonData = JsonParser.parseReader(reader).getAsJsonObject();
            LOGGER.debug("Parsed JSON data: " + jsonData);

            if (jsonData.has("countries")) {
                loadCountries(jsonData);
            }

            if (jsonData.has("cities")) {
                loadCities(jsonData);
            }

            if (jsonData.has("weather_status")) {
                loadWeatherStatus(jsonData);
            }

            if (jsonData.has("weather_daily_forecast")) {
                loadWeatherDailyForecast(jsonData);
            }

        } catch (FileNotFoundException e) {
            LOGGER.error("JSON file not found: " + filePath, e);
        } catch (Exception e) {
            LOGGER.error("Error loading weather data: " + e.getMessage(), e);
        }
    }

    private void loadCountries(JsonObject jsonData) {
        LOGGER.info("Loading countries...");
        JsonArray countriesArray = jsonData.getAsJsonArray("countries");
        countriesArray.forEach(countryElement -> {
            JsonObject countryObj = countryElement.getAsJsonObject();
            String countryName = countryObj.get("country_name").getAsString();
            String countryIso = countryObj.get("country_iso").getAsString();

            loadOrCreateEntity(
                    countryName,
                    countryService::findByName,
                    () -> {
                        Countries country = new Countries();
                        country.setCountryName(countryName);
                        country.setCountryIso(countryIso);
                        return country;
                    },
                    countryService::save
            );
        });
        LOGGER.info("Countries loaded successfully.");
    }

    private void loadCities(JsonObject jsonData) {
        LOGGER.info("Loading cities...");
        JsonArray citiesArray = jsonData.getAsJsonArray("cities");
        citiesArray.forEach(cityElement -> {
            JsonObject cityObj = cityElement.getAsJsonObject();
            String cityName = cityObj.get("city_name").getAsString();
            BigDecimal latitude = cityObj.get("latitude").getAsBigDecimal();
            BigDecimal longitude = cityObj.get("longitude").getAsBigDecimal();
            int countryId = cityObj.get("country_id").getAsInt();

            Countries country = countryService.findById(countryId);
            if (country == null) {
                LOGGER.error("Country with ID " + countryId + " not found for city: " + cityName);
                return;
            }

            Cities city = loadOrCreateEntity(
                    cityName,
                    cityService::findByName,
                    () -> {
                        Cities newCity = new Cities();
                        newCity.setCityName(cityName);
                        newCity.setLatitude(latitude);
                        newCity.setLongitude(longitude);
                        newCity.setCountry(country);
                        return newCity;
                    },
                    cityService::save
            );
            LOGGER.debug("Loaded or created city: " + city);
        });
        LOGGER.info("Cities loaded successfully.");
    }


    private void loadWeatherStatus(JsonObject jsonData) {
        LOGGER.info("Loading weather statuses...");
        JsonArray weatherStatusArray = jsonData.getAsJsonArray("weather_status");
        weatherStatusArray.forEach(statusElement -> {
            JsonObject statusObj = statusElement.getAsJsonObject();
            String weatherStatus = statusObj.get("weather_status").getAsString();
            String description = statusObj.get("description").getAsString();

            WeatherStatus status = loadOrCreateEntity(
                    weatherStatus,
                    weatherStatusService::findByName,
                    () -> {
                        WeatherStatus newStatus = new WeatherStatus();
                        newStatus.setWeatherStatus(weatherStatus);
                        newStatus.setWeatherDescription(description);
                        return newStatus;
                    },
                    weatherStatusService::save
            );
            LOGGER.debug("Loaded or created WeatherStatus: ID=" + status.getId() + ", Name=" + status.getWeatherStatus());
        });
        LOGGER.info("Weather statuses loaded successfully.");
    }

    private void loadWeatherDailyForecast(JsonObject jsonData) {
        LOGGER.info("Loading weather daily forecasts...");
        JsonArray forecastArray = jsonData.getAsJsonArray("weather_daily_forecast");

        forecastArray.forEach(forecastElement -> {
            JsonObject forecastObj = forecastElement.getAsJsonObject();

            // Validate the presence of required fields
            if (!forecastObj.has("city_id") || !forecastObj.has("forecast_date") ||
                    !forecastObj.has("max_temperature") || !forecastObj.has("min_temperature")
                    || !forecastObj.has("weather_status_id")) {
                LOGGER.error("Missing required fields in forecast: " + forecastObj);
                return;
            }

            int cityId = forecastObj.get("city_id").getAsInt();

            // Check if the city exists
            Optional<Cities> optionalCity = cityService.findById(cityId);
            if (optionalCity.isEmpty()) {
                LOGGER.error("City with ID " + cityId + " not found for forecast.");
                return;
            }
            Cities city = optionalCity.get();

            try {
                LocalDate forecastDate = LocalDate.parse(forecastObj.get("forecast_date").getAsString());
                double maxTemp = forecastObj.get("max_temperature").getAsDouble();
                double minTemp = forecastObj.get("min_temperature").getAsDouble();
                int weatherStatusId = forecastObj.get("weather_status_id").getAsInt();

                // Validate if the weatherStatusId exists
                Optional<WeatherStatus> optionalWeatherStatus = Optional.ofNullable(weatherStatusService.findById(weatherStatusId));
                if (optionalWeatherStatus.isEmpty()) {
                    LOGGER.error("Weather status with ID " + weatherStatusId + " not found for forecast. Ensure this ID exists in weather_status.");
                    return;
                }
                WeatherStatus weatherStatus = optionalWeatherStatus.get();

                WeatherDailyForecast forecast = new WeatherDailyForecast();
                forecast.setCity(city);
                forecast.setForecastDate(forecastDate);
                forecast.setMaxTemperature(maxTemp);
                forecast.setMinTemperature(minTemp);
                forecast.setWeatherStatus(weatherStatus);
                forecastLogService.save(forecast);

                LOGGER.debug("Saved forecast: " + forecast);
            } catch (Exception e) {
                LOGGER.error("Error processing forecast data: " + forecastObj, e);
            }
        });

        LOGGER.info("Weather daily forecasts loaded successfully.");
    }


}
