package se.lu.ics.data;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Handles database connection configuration for the Marvix application.
 */
public class ConnectionHandler {

        private static final String CONFIGURATION_FILE_PATH = "/config/config.properties";

        private final String connectionUrl;

        /**
         * Creates a connection handler using the local database configuration file.
         *
         * @throws IOException if the configuration file cannot be loaded or is
         *                     incomplete
         */
        public ConnectionHandler() throws IOException {
                Properties connectionProperties = loadConnectionProperties();

                String databaseServerName = getRequiredProperty(
                                connectionProperties,
                                "database.server.name");
                String databaseServerPort = getRequiredProperty(
                                connectionProperties,
                                "database.server.port");
                String databaseName = getRequiredProperty(
                                connectionProperties,
                                "database.name");
                String databaseUsername = getRequiredProperty(
                                connectionProperties,
                                "database.user.name");
                String databasePassword = getRequiredProperty(
                                connectionProperties,
                                "database.user.password");

                connectionUrl = "jdbc:sqlserver://"
                                + databaseServerName
                                + ":"
                                + databaseServerPort
                                + ";database="
                                + databaseName
                                + ";user="
                                + databaseUsername
                                + ";password="
                                + databasePassword
                                + ";encrypt=true;"
                                + "trustServerCertificate=true;";
        }

        /**
         * Opens a new SQL Server connection using the configured connection URL.
         *
         * @return open database connection
         * @throws SQLException if the connection cannot be established
         */
        public Connection getConnection() throws SQLException {
                return DriverManager.getConnection(connectionUrl);
        }

        // Loads the local configuration that is excluded from version control.
        private Properties loadConnectionProperties() throws IOException {
                Properties connectionProperties = new Properties();

                try (InputStream inputStream = getClass().getResourceAsStream(CONFIGURATION_FILE_PATH)) {
                        if (inputStream == null) {
                                throw new IOException(
                                                "Database configuration file was not found: "
                                                                + CONFIGURATION_FILE_PATH);
                        }

                        connectionProperties.load(inputStream);
                }

                return connectionProperties;
        }

        // Prevents incomplete configuration values from reaching the JDBC connection
        // URL.
        private String getRequiredProperty(
                        Properties properties,
                        String propertyName) throws IOException {

                String propertyValue = properties.getProperty(propertyName);

                if (propertyValue == null || propertyValue.isBlank()) {
                        throw new IOException(
                                        "Missing required database configuration property: "
                                                        + propertyName);
                }

                return propertyValue.strip();
        }
}
