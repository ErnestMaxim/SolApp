package me.solapp.data;

import jakarta.persistence.EntityManager;
import lombok.Getter;
import me.solapp.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.List;
import java.util.Properties;

public class DatabaseManager {

    private static final Logger LOGGER = LogManager.getLogger(DatabaseManager.class);

    private final EntityManager entityManager;

    @Getter
    private final AdminLogsService adminLogsService;
    @Getter
    private final CitiesService cityService;
    @Getter
    private final CountriesService countryService;
    @Getter
    private final UserRolesService userRolesService;
    @Getter
    private final UsersService userService;
    @Getter
    private final WeatherDailyForecastService weatherDailyForecastLogService;
    @Getter
    private final WeatherStatusService weatherStatusService;

    private static SessionFactory sessionFactory;


    // Update the DatabaseManager constructor to initialize the new service:
    private DatabaseManager(Builder builder) {
        this.entityManager = builder.entityManager;
        this.adminLogsService = new AdminLogsService(entityManager);
        this.cityService = new CitiesService(entityManager);
        this.countryService = new CountriesService(entityManager);
        this.userRolesService = new UserRolesService(entityManager);
        this.userService = new UsersService(entityManager);
        this.weatherDailyForecastLogService = new WeatherDailyForecastService(entityManager);
        this.weatherStatusService = new WeatherStatusService(entityManager);
    }


    public static class Builder {
        private EntityManager entityManager;

        private String jdbcDriver;
        private String jdbcUrl;
        private String jdbcUser;
        private String jdbcPassword;
        private String ddlGeneration;
        private String loggingLevelSql;
        private String jdbcDialect;

        public Builder setJdbcDriver(String jdbcDriver) {
            this.jdbcDriver = jdbcDriver;
            return this;
        }

        public Builder setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public Builder setJdbcUser(String jdbcUser) {
            this.jdbcUser = jdbcUser;
            return this;
        }

        public Builder setJdbcPassword(String jdbcPassword) {
            this.jdbcPassword = jdbcPassword;
            return this;
        }

        public Builder setJdbcDialect(String jdbcDialect) {
            this.jdbcDialect = jdbcDialect;
            return this;
        }

        public Builder setDdlGeneration(String ddlGeneration) {
            this.ddlGeneration = ddlGeneration;
            return this;
        }

        public Builder setLoggingLevelSql(String loggingLevelSql) {
            this.loggingLevelSql = loggingLevelSql;
            return this;
        }

        public DatabaseManager build() {
            try {
                LOGGER.debug("Initializing DatabaseManager...");
                Properties settings = new Properties();

                // Basic JDBC settings
                settings.put(Environment.DRIVER, jdbcDriver);
                settings.put(Environment.URL, jdbcUrl);
                settings.put(Environment.USER, jdbcUser);
                settings.put(Environment.PASS, jdbcPassword);

                // SQLite specific settings
                settings.put(Environment.DIALECT, jdbcDialect);
                settings.put(Environment.HBM2DDL_AUTO, ddlGeneration);
                settings.put(Environment.SHOW_SQL, loggingLevelSql);

                // Critical SQLite configurations
                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                settings.put("hibernate.id.new_generator_mappings", "false");
                settings.put("hibernate.connection.foreign_keys", "true");

                // Connection pool settings
                settings.put(Environment.C3P0_MIN_SIZE, "1");
                settings.put(Environment.C3P0_MAX_SIZE, "5");
                settings.put(Environment.C3P0_TIMEOUT, "1800");

                // SQLite requires certain settings for proper ID generation
                settings.put("hibernate.jdbc.use_get_generated_keys", "false");
                settings.put("hibernate.jdbc.batch_versioned_data", "false");

                Configuration configuration = new Configuration();
                configuration.setProperties(settings);

                // Add annotated classes
                configuration.addAnnotatedClass(Users.class);
                configuration.addAnnotatedClass(UserRoles.class);
                configuration.addAnnotatedClass(Cities.class);
                configuration.addAnnotatedClass(Countries.class);
                configuration.addAnnotatedClass(AdminLogs.class);
                configuration.addAnnotatedClass(WeatherDailyForecast.class);
                configuration.addAnnotatedClass(WeatherStatus.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
                this.entityManager = sessionFactory.openSession()
                        .getEntityManagerFactory()
                        .createEntityManager();

                LOGGER.info("EntityManager initialized successfully.");
            } catch (Exception ex) {
                LOGGER.error("Failed to initialize DatabaseManager: " + ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
            return new DatabaseManager(this);
        }
    }

    public void close() {
        LOGGER.debug("Closing DatabaseManager...");
        if (entityManager != null) {
            entityManager.close();
            LOGGER.info("EntityManager closed.");
        }
    }

    public void testDatabaseConnection() {
        try {
            List<Users> users = entityManager.createQuery("SELECT u FROM Users u", Users.class).getResultList();
            System.out.println("DEBUG: Users in the database: " + users);
        } catch (Exception e) {
            System.out.println("DEBUG: Exception during database test: " + e.getMessage());
            e.printStackTrace();
        }
    }

}