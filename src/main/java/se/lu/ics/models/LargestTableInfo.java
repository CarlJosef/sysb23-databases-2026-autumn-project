package se.lu.ics.models;

/**
 * Represents metadata about the database table containing the highest number of
 * rows.
 */
public class LargestTableInfo {

    private final String schemaName;
    private final String tableName;
    private final long rowCount;

    /**
     * Creates metadata for the table containing the highest number of rows.
     *
     * @param schemaName database schema containing the table
     * @param tableName  name of the table
     * @param rowCount   number of rows in the table
     */
    public LargestTableInfo(
            String schemaName,
            String tableName,
            long rowCount) {

        this.schemaName = schemaName;
        this.tableName = tableName;
        this.rowCount = rowCount;
    }

    /**
     * @return database schema containing the table
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * @return name of the table
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return number of rows in the table
     */
    public long getRowCount() {
        return rowCount;
    }
}
