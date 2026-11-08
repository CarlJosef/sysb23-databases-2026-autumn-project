package se.lu.ics.models;

/**
 * Represents primary key metadata for a database table.
 */
public class PrimaryKeyInfo {

    private final String schemaName;
    private final String tableName;
    private final String primaryKeyName;

    /**
     * Creates primary key metadata for a database table.
     *
     * @param schemaName     database schema containing the table
     * @param tableName      database table name
     * @param primaryKeyName primary key constraint name
     */
    public PrimaryKeyInfo(
            String schemaName,
            String tableName,
            String primaryKeyName) {

        this.schemaName = schemaName;
        this.tableName = tableName;
        this.primaryKeyName = primaryKeyName;
    }

    /**
     * Returns the database schema name.
     *
     * @return database schema name
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * Returns the database table name.
     *
     * @return database table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Returns the primary key constraint name.
     *
     * @return primary key constraint name
     */
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }
}
