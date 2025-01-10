package me.solapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import me.solapp.data.UserRoles;

import java.util.List;

public class UserRolesService {
    private final EntityManager entityManager;

    public UserRolesService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<UserRoles> findAll() {
        return entityManager.createQuery("SELECT r FROM UserRoles r", UserRoles.class)
                .getResultList();
    }

    public UserRoles findById(Integer id) {
        return entityManager.find(UserRoles.class, id);
    }

    @Transactional
    public UserRoles findByName(String roleName) {
        try {
            return entityManager.createQuery(
                            "SELECT r FROM UserRoles r WHERE r.roleName = :roleName", UserRoles.class)
                    .setParameter("roleName", roleName)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


    @Transactional
    public void save(UserRoles userRole) {
        entityManager.getTransaction().begin();
        if (userRole.getId() == null) {
            entityManager.persist(userRole);
        } else {
            entityManager.merge(userRole);
        }
        entityManager.getTransaction().commit();
    }
}