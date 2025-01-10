package me.solapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import me.solapp.data.Cities;

import java.util.Comparator;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

public class CitiesService {
    private final EntityManager entityManager;

    public CitiesService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Cities> findAll() {
        return entityManager.createQuery("SELECT c FROM Cities c", Cities.class)
                .getResultList();
    }

    public Optional<Cities> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Cities.class, id));
    }

    @Transactional
    public Optional<Cities> findByName(String cityName) {
        System.out.println("DEBUG: Searching for city with name: " + cityName);
        try {
            Cities city = entityManager.createQuery(
                            "SELECT c FROM Cities c WHERE c.cityName = :cityName", Cities.class)
                    .setParameter("cityName", cityName)
                    .getSingleResult();
            System.out.println("DEBUG: Found city: " + city);
            return Optional.of(city);
        } catch (NoResultException e) {
            System.out.println("DEBUG: No city found with name: " + cityName);
            return Optional.empty();
        }
    }


    public Cities findNearestCity(BigDecimal lat, BigDecimal lon, double radiusKm) {
        List<Cities> cities = findAll();
        return cities.stream()
                .filter(city -> calculateDistance(lat, lon, city.getLatitude(), city.getLongitude()) <= radiusKm)
                .min(Comparator.comparingDouble(city -> calculateDistance(lat, lon, city.getLatitude(), city.getLongitude())))
                .orElse(null);
    }

    public List<Cities> findCitiesWithinRadius(BigDecimal latitude, BigDecimal longitude, double radiusKm) {
        return findAll().stream()
                .filter(city -> calculateDistance(latitude, longitude, city.getLatitude(), city.getLongitude()) <= radiusKm)
                .collect(Collectors.toList());
    }

    private double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double dLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double dLon = Math.toRadians(lon2.subtract(lon1).doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    @Transactional
    public void save(Cities city) {
        entityManager.getTransaction().begin();
        if (city.getId() == null) {
            entityManager.persist(city);
        } else {
            entityManager.merge(city);
        }
        entityManager.getTransaction().commit();
    }
}
