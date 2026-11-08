package se.lu.ics.data;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.lu.ics.models.Product;

/**
 * Data access object for product database operations.
 *
 * Keeps SQL and JDBC logic out of the controller layer.
 */
public class ProductDao {

    private final ConnectionHandler connectionHandler;

    /** Creates a ProductDao using the shared database configuration. */
    public ProductDao() {
        try {
            connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException(
                    "Could not load the database configuration.",
                    e);
        }
    }

    /**
     * Retrieves all products ordered by product ID.
     *
     * @return all products in the database
     */
    public List<Product> getAllProducts() {
        String query = """
                SELECT
                    ProductID,
                    ProductName,
                    Price
                FROM dbo.Product
                ORDER BY ProductID
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapProduct(resultSet));
            }
        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve products.",
                    e);
        }

        return products;
    }

    /**
     * Retrieves a product by product ID.
     *
     * @param productId product ID to retrieve
     * @return matching product, or {@code null} when no product exists with the ID
     */
    public Product getProductById(int productId) {
        String query = """
                EXEC dbo.usp_GetProductById
                    @ProductId = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapProduct(resultSet);
                }

                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve product.",
                    e);
        }
    }

    /**
     * Creates a product and returns its generated ID.
     *
     * @param productName product name
     * @param price       product price
     * @return generated product ID
     */
    public int createProduct(
            String productName,
            BigDecimal price) {

        String query = """
                EXEC dbo.usp_CreateProduct
                    @ProductName = ?,
                    @Price = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, productName);
            statement.setBigDecimal(2, price);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DaoException(
                            "SQL Server did not return the new product ID.");
                }

                return resultSet.getInt("ProductID");
            }
        } catch (SQLException e) {
            throw new DaoException(
                    "Could not create product.",
                    e);
        }
    }

    /**
     * Updates an existing product.
     *
     * @param productId   product ID to update
     * @param productName new product name
     * @param price       new product price
     */
    public void updateProduct(
            int productId,
            String productName,
            BigDecimal price) {

        String query = """
                EXEC dbo.usp_UpdateProduct
                    @ProductId = ?,
                    @ProductName = ?,
                    @Price = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, productId);
            statement.setString(2, productName);
            statement.setBigDecimal(3, price);
            statement.execute();

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not update product.",
                    e);
        }
    }

    /**
     * Removes a product from the database.
     *
     * SQL Server prevents removal when the product is referenced by an order line.
     *
     * @param productId product ID to remove
     */
    public void removeProduct(int productId) {
        String query = """
                EXEC dbo.usp_RemoveProduct
                    @ProductId = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, productId);
            statement.execute();

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not remove product.",
                    e);
        }
    }

    // Maps the current result row to the domain model.
    private Product mapProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getInt("ProductID"),
                resultSet.getString("ProductName"),
                resultSet.getBigDecimal("Price"));
    }
}
