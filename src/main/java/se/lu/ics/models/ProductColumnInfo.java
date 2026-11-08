package se.lu.ics.models;

/**
 * Represents metadata information about a column in the Product table.
 */
public class ProductColumnInfo {

    private final int ordinalPosition;
    private final String columnName;

    /**
     * Creates Product table column metadata.
     *
     * @param ordinalPosition position of the column within the Product table
     * @param columnName name of the Product table column
     */
    public ProductColumnInfo(int ordinalPosition, String columnName) {
        this.ordinalPosition = ordinalPosition;
        this.columnName = columnName;
    }

    /**
     * Returns the ordinal position of the column.
     *
     * @return column position within the Product table
     */
    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    /**
     * Returns the column name.
     *
     * @return Product table column name
     */
    public String getColumnName() {
        return columnName;
    }
}
