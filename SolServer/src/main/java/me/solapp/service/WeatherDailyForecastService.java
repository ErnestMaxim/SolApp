package me.solapp.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.solapp.data.Cities;
import me.solapp.data.WeatherDailyForecast;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WeatherDailyForecastService {
    private final EntityManager entityManager;

    public WeatherDailyForecastService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<WeatherDailyForecast> findForecastsByCity(Optional<Cities> city) {
        if (city.isEmpty()) {
            System.err.println("DEBUG: No city provided for forecast lookup.");
            return List.of();
        }

        System.out.println("DEBUG: Fetching forecasts for city: " + city.get().getCityName());
        List<WeatherDailyForecast> forecasts = entityManager.createQuery(
                        "SELECT w FROM WeatherDailyForecast w WHERE w.city = :city ORDER BY w.forecastDate",
                        WeatherDailyForecast.class)
                .setParameter("city", city.get())
                .getResultList();

        System.out.println("DEBUG: Retrieved forecasts: " + forecasts);
        return forecasts;
    }


    public List<WeatherDailyForecast> findForecastsByCityAndDate(Cities city, LocalDate date) {
        return entityManager.createQuery(
                        "SELECT w FROM WeatherDailyForecast w WHERE w.city = :city AND w.forecastDate = :date",
                        WeatherDailyForecast.class)
                .setParameter("city", city)
                .setParameter("date", date)
                .getResultList();
    }

    public List<WeatherDailyForecast> findAll() {
        return entityManager.createQuery("SELECT w FROM WeatherDailyForecast w", WeatherDailyForecast.class)
                .getResultList();
    }

    public List<WeatherDailyForecast> getSortedForecastsByCity(Cities city) {
        return findForecastsByCity(Optional.ofNullable(city)).stream()
                .sorted(Comparator.comparing(WeatherDailyForecast::getForecastDate))
                .collect(Collectors.toList());
    }


    @Transactional
    public void save(WeatherDailyForecast forecast) {
        // Check for an existing record
        String query = "SELECT w FROM WeatherDailyForecast w WHERE w.city = :city AND w.forecastDate = :forecastDate";
        List<WeatherDailyForecast> existing = entityManager.createQuery(query, WeatherDailyForecast.class)
                .setParameter("city", forecast.getCity())
                .setParameter("forecastDate", forecast.getForecastDate())
                .getResultList();

        if (existing.isEmpty()) {
            // No existing record, persist the new one
            entityManager.persist(forecast);
        } else {
            // Update the existing record
            WeatherDailyForecast existingForecast = existing.get(0);
            existingForecast.setMaxTemperature(forecast.getMaxTemperature());
            existingForecast.setMinTemperature(forecast.getMinTemperature());
            existingForecast.setWeatherStatus(forecast.getWeatherStatus());
            entityManager.merge(existingForecast);
        }
    }
}
