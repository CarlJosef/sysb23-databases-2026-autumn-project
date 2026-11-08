package se.lu.ics.models;

/**
 * Represents metadata information about a database table.
 */
public class TableInfo {

    private final String schemaName;
    private final String tableName;

    /**
     * Creates table metadata information.
     *
     * @param schemaName name of the database schema
     * @param tableName  name of the database table
     */
    public TableInfo(String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
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
}
