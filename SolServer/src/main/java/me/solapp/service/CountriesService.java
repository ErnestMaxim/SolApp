package me.solapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import me.solapp.data.Countries;

import java.util.List;
import java.util.Optional;

public class CountriesService {
    private final EntityManager entityManager;

    public CountriesService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Countries> findAll() {
        return entityManager.createQuery("SELECT c FROM Countries c", Countries.class)
                .getResultList();
    }

    public Countries findById(Integer id) {
        return entityManager.find(Countries.class, id);
    }

    public Optional<Countries> findByName(String countryName) {
        try {
            return Optional.ofNullable(
                    entityManager.createQuery(
                                    "SELECT c FROM Countries c WHERE c.countryName = :countryName", Countries.class)
                            .setParameter("countryName", countryName)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }


    @Transactional
    public void save(Countries country) {
        if (country.getId() == null) { // New entity
            try {
                entityManager.getTransaction().begin();
                entityManager.persist(country);
                entityManager.getTransaction().commit();
            } catch (Exception e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                throw e; // Re-throw the exception to signal failure
            }
        } else { // Update existing entity
            try {
                entityManager.getTransaction().begin();
                entityManager.merge(country);
                entityManager.getTransaction().commit();
            } catch (Exception e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                throw e; // Re-throw the exception to signal failure
            }
        }
    }

}