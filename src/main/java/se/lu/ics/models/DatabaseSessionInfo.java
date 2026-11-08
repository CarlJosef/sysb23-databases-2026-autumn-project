package se.lu.ics.models;

/**
 * Represents read-only information about the active SQL Server session.
 */
public class DatabaseSessionInfo {

    private final String loginName;
    private final String databaseUser;
    private final String databaseRole;
    private final String databaseName;

    /**
     * Creates database session information from values retrieved from SQL Server.
     *
     * @param loginName    original SQL Server login name
     * @param databaseUser current database user name
     * @param databaseRole database role selected for display
     * @param databaseName name of the active database
     */
    public DatabaseSessionInfo(
            String loginName,
            String databaseUser,
            String databaseRole,
            String databaseName) {

        this.loginName = loginName;
        this.databaseUser = databaseUser;
        this.databaseRole = databaseRole;
        this.databaseName = databaseName;
    }

    /**
     * Returns the original SQL Server login name.
     *
     * @return SQL Server login name
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Returns the current database user name.
     *
     * @return database user name
     */
    public String getDatabaseUser() {
        return databaseUser;
    }

    /**
     * Returns the database role selected for display.
     *
     * @return database role
     */
    public String getDatabaseRole() {
        return databaseRole;
    }

    /**
     * Returns the active database name.
     *
     * @return database name
     */
    public String getDatabaseName() {
        return databaseName;
    }
}
