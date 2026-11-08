package se.lu.ics.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.lu.ics.models.Customer;
import se.lu.ics.models.Order;
import se.lu.ics.models.Product;

/**
 * Data access object for customer database operations.
 *
 * All database access related to customers is handled here so that
 * controllers do not contain SQL or JDBC logic.
 */
public class CustomerDao {

    private final ConnectionHandler connectionHandler;

    /** Creates a CustomerDao using the shared database configuration. */
    public CustomerDao() {
        try {
            connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException(
                    "Could not load the database configuration.",
                    e);
        }
    }

    /**
     * Retrieves all customers.
     *
     * @return all customers ordered by customer ID
     */
    public List<Customer> getAllCustomers() {

        String query = """
                SELECT
                    c.CustomerID,
                    c.CustomerName,
                    c.CustomerAddress,
                    c.Email,
                    c.LoyaltyLevelID,
                    ll.LoyaltyName,
                    ll.DiscountPercentage
                FROM dbo.Customer AS c
                JOIN dbo.LoyaltyLevel AS ll
                    ON ll.LoyaltyLevelID = c.LoyaltyLevelID
                ORDER BY c.CustomerID
                """;

        List<Customer> customers = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                customers.add(mapCustomer(resultSet));
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve customers.",
                    e);
        }

        return customers;
    }

    /**
     * Retrieves a customer by customer ID.
     *
     * @param customerId customer ID to retrieve
     * @return matching customer, or null when no customer exists with the supplied
     *         ID
     */
    public Customer getCustomerById(int customerId) {

        String query = """
                EXEC dbo.usp_GetCustomerById
                    @CustomerId = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapCustomer(resultSet);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve customer.",
                    e);
        }
    }

    /**
     * Creates a customer.
     *
     * @param customerName    customer name
     * @param customerAddress customer address
     * @param email           customer email address
     * @param loyaltyName     loyalty level name
     * @return generated customer ID
     */
    public int createCustomer(
            String customerName,
            String customerAddress,
            String email,
            String loyaltyName) {

        String query = """
                EXEC dbo.usp_CreateCustomer
                    @CustomerName = ?,
                    @CustomerAddress = ?,
                    @LoyaltyLevelId = ?,
                    @Email = ?
                """;

        try (Connection connection = connectionHandler.getConnection()) {

            int loyaltyLevelId = getLoyaltyLevelId(connection, loyaltyName);

            try (PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, customerName);
                statement.setString(2, customerAddress);
                statement.setInt(3, loyaltyLevelId);
                statement.setString(4, email);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (!resultSet.next()) {
                        throw new DaoException(
                                "SQL Server did not return the new customer ID.");
                    }

                    return resultSet.getInt("CustomerID");
                }
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not create customer.",
                    e);
        }
    }

    /**
     * Updates an existing customer.
     *
     * @param customerId      customer ID to update
     * @param customerName    updated customer name
     * @param customerAddress updated customer address
     * @param email           updated customer email address
     * @param loyaltyName     updated loyalty level name
     */
    public void updateCustomer(
            int customerId,
            String customerName,
            String customerAddress,
            String email,
            String loyaltyName) {

        String query = """
                EXEC dbo.usp_UpdateCustomer
                    @CustomerId = ?,
                    @CustomerName = ?,
                    @CustomerAddress = ?,
                    @LoyaltyLevelId = ?,
                    @Email = ?
                """;

        try (Connection connection = connectionHandler.getConnection()) {

            int loyaltyLevelId = getLoyaltyLevelId(connection, loyaltyName);

            try (PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setInt(1, customerId);
                statement.setString(2, customerName);
                statement.setString(3, customerAddress);
                statement.setInt(4, loyaltyLevelId);
                statement.setString(5, email);

                statement.execute();
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not update customer.",
                    e);
        }
    }

    /**
     * Removes a customer when allowed by database constraints.
     *
     * @param customerId customer ID to remove
     */
    public void removeCustomer(int customerId) {

        String query = """
                EXEC dbo.usp_RemoveCustomer
                    @CustomerId = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, customerId);
            statement.execute();

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not remove customer.",
                    e);
        }
    }

    /**
     * Retrieves all orders belonging to a customer.
     *
     * @param customerId customer ID whose orders are requested
     * @return customer orders
     */
    public List<Order> getOrdersByCustomer(int customerId) {

        String query = """
                EXEC dbo.usp_GetOrdersByCustomer
                    @CustomerId = ?
                """;

        List<Order> orders = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    orders.add(mapOrder(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve customer orders.",
                    e);
        }

        return orders;
    }

    /**
     * Retrieves the latest order belonging to a customer.
     *
     * @param customerId customer ID whose latest order is requested
     * @return latest order, or null when the customer has no orders
     */
    public Order getLatestOrderByCustomer(int customerId) {

        String query = """
                EXEC dbo.usp_GetLatestOrderByCustomer
                    @CustomerId = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapOrder(resultSet);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve the customer's latest order.",
                    e);
        }
    }

    /**
     * Retrieves all products previously ordered by a customer.
     *
     * @param customerId customer ID whose ordered products are requested
     * @return products previously ordered by the customer
     */
    public List<Product> getProductsByCustomer(int customerId) {

        String query = """
                EXEC dbo.usp_GetProductsByCustomer
                    @CustomerId = ?
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve products ordered by the customer.",
                    e);
        }

        return products;
    }

    /**
     * Retrieves the names of all loyalty levels.
     *
     * @return loyalty level names ordered by loyalty level ID
     */
    public List<String> getLoyaltyLevelNames() {

        String query = """
                SELECT LoyaltyName
                FROM dbo.LoyaltyLevel
                ORDER BY LoyaltyLevelID
                """;

        List<String> loyaltyLevelNames = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                loyaltyLevelNames.add(resultSet.getString("LoyaltyName"));
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve loyalty levels.",
                    e);
        }

        return loyaltyLevelNames;
    }

    // Resolves the selected loyalty level without hardcoding database IDs.
    private int getLoyaltyLevelId(
            Connection connection,
            String loyaltyName) throws SQLException {

        String query = """
                SELECT LoyaltyLevelID
                FROM dbo.LoyaltyLevel
                WHERE LoyaltyName = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, loyaltyName);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new DaoException(
                            "Unknown customer loyalty level: "
                                    + loyaltyName);
                }

                return resultSet.getInt("LoyaltyLevelID");
            }
        }
    }

    // Maps the current result row to the customer model.
    private Customer mapCustomer(
            ResultSet resultSet) throws SQLException {

        return new Customer(
                resultSet.getInt("CustomerID"),
                resultSet.getString("CustomerName"),
                resultSet.getString("CustomerAddress"),
                resultSet.getString("Email"),
                resultSet.getInt("LoyaltyLevelID"),
                resultSet.getString("LoyaltyName"),
                resultSet.getBigDecimal("DiscountPercentage"));
    }

    // Maps the current result row to the order model.
    private Order mapOrder(
            ResultSet resultSet) throws SQLException {

        return new Order(
                resultSet.getInt("OrderID"),
                resultSet.getTimestamp("OrderDate").toLocalDateTime(),
                resultSet.getInt("CustomerID"),
                resultSet.getString("CustomerName"),
                resultSet.getString("LoyaltyName"),
                resultSet.getBigDecimal("DiscountPercentage"),
                resultSet.getBigDecimal("Subtotal"),
                resultSet.getBigDecimal("OrderTotal"));
    }

    // Maps the current result row to the product model.
    private Product mapProduct(
            ResultSet resultSet) throws SQLException {

        return new Product(
                resultSet.getInt("ProductID"),
                resultSet.getString("ProductName"),
                resultSet.getBigDecimal("Price"));
    }
}
