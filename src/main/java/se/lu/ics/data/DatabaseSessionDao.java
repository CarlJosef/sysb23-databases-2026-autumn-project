package se.lu.ics.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import se.lu.ics.models.DatabaseSessionInfo;

/**
 * Data access object for information about the active SQL Server session.
 */
public class DatabaseSessionDao {

    private final ConnectionHandler connectionHandler;

    /**
     * Creates a DatabaseSessionDao using the shared database configuration.
     */
    public DatabaseSessionDao() {
        try {
            connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException(
                    "Could not load the database configuration.",
                    e);
        }
    }

    /**
     * Retrieves information about the current SQL Server session.
     *
     * @return information about the active database session
     */
    public DatabaseSessionInfo getCurrentSession() {
        String query = """
                SELECT
                    ORIGINAL_LOGIN() AS login_name,
                    USER_NAME() AS database_user,
                    DB_NAME() AS database_name,
                    IS_ROLEMEMBER('db_owner') AS is_db_owner,
                    IS_ROLEMEMBER('db_datareader') AS is_datareader,
                    IS_ROLEMEMBER('db_datawriter') AS is_datawriter
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            if (!resultSet.next()) {
                throw new DaoException(
                        "SQL Server did not return session information.");
            }

            String loginName = resultSet.getString("login_name");
            String databaseUser = resultSet.getString("database_user");
            String databaseName = resultSet.getString("database_name");
            String databaseRole = determineDatabaseRole(resultSet);

            return new DatabaseSessionInfo(
                    loginName,
                    databaseUser,
                    databaseRole,
                    databaseName);

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve the current database session.",
                    e);
        }
    }

    // Determines the most relevant database role for display in the application.
    private String determineDatabaseRole(
            ResultSet resultSet) throws SQLException {

        if (resultSet.getInt("is_db_owner") == 1) {
            return "db_owner";
        }

        if (resultSet.getInt("is_datawriter") == 1) {
            return "db_datawriter";
        }

        if (resultSet.getInt("is_datareader") == 1) {
            return "db_datareader";
        }

        return "Database user";
    }
}
