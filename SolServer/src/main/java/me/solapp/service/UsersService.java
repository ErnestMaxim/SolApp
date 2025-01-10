package me.solapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import me.solapp.data.Cities;
import me.solapp.data.UserRoles;
import me.solapp.data.Users;

import java.util.List;
import java.util.Optional;

public class UsersService {
    private final EntityManager entityManager;

    public UsersService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Users> findAll() {
        return entityManager.createQuery("SELECT u FROM Users u", Users.class)
                .getResultList();
    }

    public Users findById(Integer id) {
        return entityManager.find(Users.class, id);
    }

    public Optional<Users> findByUsername(String username) {
        try {
            System.out.println("DEBUG: Searching for username: " + username);
            Users user = entityManager.createQuery(
                            "SELECT u FROM Users u WHERE u.username = :username", Users.class)
                    .setParameter("username", username)
                    .getSingleResult();
            System.out.println("DEBUG: User found: " + user);
            return Optional.of(user);
        } catch (NoResultException e) {
            System.out.println("DEBUG: No user found with username: " + username);
            return Optional.empty();
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in findByUsername: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Cities getUserLocation(Integer userId) {
        Users user = findById(userId);
        return user != null ? user.getLocation() : null;
    }

    public Optional<Users> authenticate(String username, String password) {
        // Find the user by username and password
        Users user = entityManager.createQuery(
                        "SELECT u FROM Users u WHERE u.username = :username AND u.password = :password",
                        Users.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .getResultStream()
                .findFirst() // Get the first matching user, if any
                .orElse(null);

        return Optional.ofNullable(user); // Return Optional<Users>
    }


    public UserRoles findRoleById(int roleId) {
        return entityManager.find(UserRoles.class, roleId);
    }

    @Transactional
    public boolean register(String username, String password, UserRoles role) {
        try {
            System.out.println("DEBUG: Starting registration for username: " + username);

            // Check if username already exists
            if (findByUsername(username).isPresent()) {
                System.out.println("DEBUG: Username already exists: " + username);
                return false;
            }

            // Create a new user
            Users newUser = new Users();
            newUser.setUsername(username);
            newUser.setPassword(password); // Add password hashing if needed
            newUser.setRole(role);

            // Log user details
            System.out.println("DEBUG: Attempting to persist user: " + newUser);

            // Begin transaction
            if (!entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
                System.out.println("DEBUG: Transaction started.");
            }

            // Persist the user
            entityManager.persist(newUser);
            System.out.println("DEBUG: User persisted successfully.");

            // Commit the transaction
            entityManager.getTransaction().commit();
            System.out.println("DEBUG: Transaction committed successfully.");

            return true;
        } catch (Exception e) {
            // Rollback the transaction in case of failure
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
                System.out.println("DEBUG: Transaction rolled back due to an error.");
            }
            System.out.println("DEBUG: Exception during registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    @Transactional
    public void updateUserLocation(int userId, Cities newLocation) {
        try {
            entityManager.getTransaction().begin();

            Users user = entityManager.find(Users.class, userId);
            if (user != null && newLocation != null) {
                System.out.println("DEBUG: Updating location for user " + userId + " to city " + newLocation.getId());
                user.setLocation(newLocation);
                entityManager.merge(user);
                entityManager.getTransaction().commit();
                System.out.println("DEBUG: Location update committed successfully");
            } else {
                System.err.println("DEBUG: User or location not found. User: " + user + ", Location: " + newLocation);
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Error updating user location: " + e.getMessage());
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void save(Users user) {
        entityManager.getTransaction().begin();
        if (user.getId() == null) {
            entityManager.persist(user);
        } else {
            entityManager.merge(user);
        }
        entityManager.getTransaction().commit();
    }
}