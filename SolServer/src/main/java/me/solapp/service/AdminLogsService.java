package me.solapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import me.solapp.data.AdminLogs;

import java.util.List;

public class AdminLogsService {
    private final EntityManager entityManager;

    public AdminLogsService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<AdminLogs> findAll() {
        return entityManager.createQuery("SELECT a FROM AdminLogs a", AdminLogs.class)
                .getResultList();
    }

    public AdminLogs findById(Integer id) {
        return entityManager.find(AdminLogs.class, id);
    }

    @Transactional
    public void save(AdminLogs adminLog) {
        entityManager.getTransaction().begin();
        if (adminLog.getId() == null) {
            entityManager.persist(adminLog);
        } else {
            entityManager.merge(adminLog);
        }
        entityManager.getTransaction().commit();
    }

    public List<AdminLogs> findByAdminId(Integer adminId) {
        return entityManager.createQuery("SELECT a FROM AdminLogs a WHERE a.adminId = :adminId", AdminLogs.class)
                .setParameter("adminId", adminId)
                .getResultList();
    }
}
