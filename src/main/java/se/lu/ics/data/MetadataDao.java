package se.lu.ics.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.lu.ics.models.ColumnInfo;
import se.lu.ics.models.LargestTableInfo;
import se.lu.ics.models.PrimaryKeyInfo;
import se.lu.ics.models.ProductColumnInfo;
import se.lu.ics.models.TableInfo;

/**
 * Provides database metadata used by the Marvix application.
 */
public class MetadataDao {

    private final ConnectionHandler connectionHandler;

    /**
     * Creates a new MetadataDao using the shared database configuration.
     */
    public MetadataDao() {
        try {
            connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException(
                    "Could not load the database configuration.",
                    e);
        }
    }

    /**
     * Retrieves the names of all primary key constraints in the database.
     *
     * @return primary key metadata ordered by schema and table name
     */
    public List<PrimaryKeyInfo> getPrimaryKeys() {
        String query = """
                SELECT
                    TABLE_SCHEMA,
                    TABLE_NAME,
                    CONSTRAINT_NAME
                FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                WHERE CONSTRAINT_TYPE = 'PRIMARY KEY'
                ORDER BY TABLE_SCHEMA, TABLE_NAME
                """;

        List<PrimaryKeyInfo> primaryKeys = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                primaryKeys.add(new PrimaryKeyInfo(
                        resultSet.getString("TABLE_SCHEMA"),
                        resultSet.getString("TABLE_NAME"),
                        resultSet.getString("CONSTRAINT_NAME")));
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve primary key metadata.",
                    e);
        }

        return primaryKeys;
    }

    /**
     * Retrieves all base tables in the database.
     *
     * @return table metadata ordered by schema and table name
     */
    public List<TableInfo> getTables() {
        String query = """
                SELECT
                    TABLE_SCHEMA,
                    TABLE_NAME
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_TYPE = 'BASE TABLE'
                ORDER BY TABLE_SCHEMA, TABLE_NAME
                """;

        List<TableInfo> tables = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                tables.add(new TableInfo(
                        resultSet.getString("TABLE_SCHEMA"),
                        resultSet.getString("TABLE_NAME")));
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve table metadata.",
                    e);
        }

        return tables;
    }

    /**
     * Retrieves all columns belonging to base tables in the database.
     *
     * @return column metadata ordered by schema, table and ordinal position
     */
    public List<ColumnInfo> getColumns() {
        String query = """
                SELECT
                    c.TABLE_SCHEMA,
                    c.TABLE_NAME,
                    c.ORDINAL_POSITION,
                    c.COLUMN_NAME
                FROM INFORMATION_SCHEMA.COLUMNS AS c
                INNER JOIN INFORMATION_SCHEMA.TABLES AS t
                    ON c.TABLE_SCHEMA = t.TABLE_SCHEMA
                    AND c.TABLE_NAME = t.TABLE_NAME
                WHERE t.TABLE_TYPE = 'BASE TABLE'
                ORDER BY
                    c.TABLE_SCHEMA,
                    c.TABLE_NAME,
                    c.ORDINAL_POSITION
                """;

        List<ColumnInfo> columns = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                columns.add(new ColumnInfo(
                        resultSet.getString("TABLE_SCHEMA"),
                        resultSet.getString("TABLE_NAME"),
                        resultSet.getInt("ORDINAL_POSITION"),
                        resultSet.getString("COLUMN_NAME")));
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve column metadata.",
                    e);
        }

        return columns;
    }

    /**
     * Retrieves all columns in the dbo.Product table.
     *
     * @return Product column metadata ordered by ordinal position
     */
    public List<ProductColumnInfo> getProductColumns() {
        String query = """
                SELECT
                    ORDINAL_POSITION,
                    COLUMN_NAME
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = 'dbo'
                  AND TABLE_NAME = 'Product'
                ORDER BY ORDINAL_POSITION
                """;

        List<ProductColumnInfo> productColumns = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                productColumns.add(new ProductColumnInfo(
                        resultSet.getInt("ORDINAL_POSITION"),
                        resultSet.getString("COLUMN_NAME")));
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve Product column metadata.",
                    e);
        }

        return productColumns;
    }

    /**
     * Retrieves the table containing the highest number of rows.
     *
     * @return largest table metadata, or null when no table is available
     */
    public LargestTableInfo getLargestTable() {
        String query = """
                SELECT TOP (1)
                    s.name AS TABLE_SCHEMA,
                    t.name AS TABLE_NAME,
                    SUM(p.rows) AS ROW_COUNT
                FROM sys.tables AS t
                INNER JOIN sys.schemas AS s
                    ON t.schema_id = s.schema_id
                INNER JOIN sys.partitions AS p
                    ON t.object_id = p.object_id
                WHERE p.index_id IN (0, 1)
                GROUP BY
                    s.name,
                    t.name
                ORDER BY
                    ROW_COUNT DESC,
                    s.name,
                    t.name
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return new LargestTableInfo(
                        resultSet.getString("TABLE_SCHEMA"),
                        resultSet.getString("TABLE_NAME"),
                        resultSet.getLong("ROW_COUNT"));
            }

            return null;

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve largest table metadata.",
                    e);
        }
    }
}
