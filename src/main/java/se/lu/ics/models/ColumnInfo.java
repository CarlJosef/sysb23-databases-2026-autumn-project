package se.lu.ics.models;

/**
 * Represents metadata information about a database column.
 */
public class ColumnInfo {

    private final String schemaName;
    private final String tableName;
    private final int ordinalPosition;
    private final String columnName;

    /**
     * Creates column metadata information.
     *
     * @param schemaName      database schema containing the column
     * @param tableName       database table containing the column
     * @param ordinalPosition column position within the table
     * @param columnName      database column name
     */
    public ColumnInfo(
            String schemaName,
            String tableName,
            int ordinalPosition,
            String columnName) {

        this.schemaName = schemaName;
        this.tableName = tableName;
        this.ordinalPosition = ordinalPosition;
        this.columnName = columnName;
    }

    /**
     * @return database schema name
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * @return database table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return column position within the table
     */
    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    /**
     * @return database column name
     */
    public String getColumnName() {
        return columnName;
    }
}
