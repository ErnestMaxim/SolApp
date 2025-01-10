package me.solapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PessimisticLockException;
import jakarta.transaction.Transactional;
import me.solapp.data.WeatherStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class WeatherStatusService {
    private static final Logger LOGGER = LogManager.getLogger(WeatherStatusService.class);

    private final EntityManager entityManager;

    public WeatherStatusService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<WeatherStatus> findAll() {
        return entityManager.createQuery("SELECT w FROM WeatherStatus w", WeatherStatus.class)
                .getResultList();
    }

    public WeatherStatus findById(Integer id) {
        return entityManager.find(WeatherStatus.class, id);
    }

    @Transactional
    public Optional<WeatherStatus> findByName(String name) {
        return entityManager.createQuery(
                        "SELECT w FROM WeatherStatus w WHERE w.weatherStatus = :name", WeatherStatus.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }



    @Transactional
    public void save(WeatherStatus weatherStatus) {
        entityManager.getTransaction().begin();
        if (weatherStatus.getId() == null) {
            entityManager.persist(weatherStatus);
        } else {
            entityManager.merge(weatherStatus);
        }
        entityManager.getTransaction().commit();
    }

}