package se.lu.ics.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.lu.ics.models.Order;
import se.lu.ics.models.OrderLine;

/**
 * Data access object for order and order-line database operations.
 *
 * Keeps SQL and JDBC logic out of the order controllers.
 */
public class OrderDao {

    private final ConnectionHandler connectionHandler;

    /** Creates a new OrderDao using the shared database configuration. */
    public OrderDao() {
        try {
            connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException(
                    "Could not load the database configuration.",
                    e);
        }
    }

    /**
     * Retrieves all orders from the database.
     *
     * @return all orders
     */
    public List<Order> getAllOrders() {
        String query = """
                EXEC dbo.usp_GetAllOrders
                """;

        List<Order> orders = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                orders.add(mapOrder(resultSet));
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve orders.",
                    e);
        }

        return orders;
    }

    /**
     * Retrieves all orders belonging to a specific customer.
     *
     * @param customerId customer whose orders should be retrieved
     * @return orders belonging to the customer
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
                    "Could not retrieve orders for the customer.",
                    e);
        }

        return orders;
    }

    /**
     * Creates a new order for a customer.
     *
     * @param customerId customer that owns the order
     * @return generated order ID
     */
    public int createOrder(int customerId) {
        String query = """
                EXEC dbo.usp_CreateOrder
                    @CustomerId = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DaoException(
                            "SQL Server did not return the new order ID.");
                }

                return resultSet.getInt("OrderID");
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not create order.",
                    e);
        }
    }

    /**
     * Retrieves summary information for a specific order.
     *
     * @param orderId order to retrieve
     * @return the order, or null when the order does not exist
     */
    public Order getOrderById(int orderId) {
        String query = """
                SELECT
                    OrderID,
                    OrderDate,
                    CustomerID,
                    CustomerName,
                    LoyaltyName,
                    DiscountPercentage,
                    Subtotal,
                    OrderTotal
                FROM dbo.vw_OrderTotal
                WHERE OrderID = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapOrder(resultSet);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve order.",
                    e);
        }
    }

    /**
     * Retrieves all order lines belonging to a specific order.
     *
     * @param orderId order whose lines should be retrieved
     * @return order lines for the order
     */
    public List<OrderLine> getOrderLines(int orderId) {
        String query = """
                EXEC dbo.usp_GetOrderById
                    @OrderId = ?
                """;

        List<OrderLine> orderLines = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int lineNumber = resultSet.getInt("LineNumber");

                    // The LEFT JOIN returns a null LineNumber when an order has no lines.
                    if (resultSet.wasNull()) {
                        continue;
                    }

                    orderLines.add(
                            mapOrderLine(
                                    resultSet,
                                    orderId,
                                    lineNumber));
                }
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve order lines.",
                    e);
        }

        return orderLines;
    }

    /**
     * Adds a product to an order.
     *
     * @param orderId    order receiving the product
     * @param productId  product to add
     * @param lineNumber line number within the order
     * @param quantity   quantity to add
     * @return generated order-line ID
     */
    public int addProductToOrder(
            int orderId,
            int productId,
            int lineNumber,
            int quantity) {

        String query = """
                EXEC dbo.usp_AddProductToOrder
                    @OrderId = ?,
                    @ProductId = ?,
                    @LineNumber = ?,
                    @Quantity = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, orderId);
            statement.setInt(2, productId);
            statement.setInt(3, lineNumber);
            statement.setInt(4, quantity);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DaoException(
                            "SQL Server did not return the new order line ID.");
                }

                return resultSet.getInt("OrderLineID");
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not add product to order.",
                    e);
        }
    }

    /**
     * Retrieves the quantity of a specific order line.
     *
     * @param orderId    order containing the line
     * @param lineNumber line number within the order
     * @return quantity, or null when the order line does not exist
     */
    public Integer getOrderLineQuantity(
            int orderId,
            int lineNumber) {

        String query = """
                EXEC dbo.usp_GetOrderLineQuantity
                    @OrderId = ?,
                    @LineNumber = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, orderId);
            statement.setInt(2, lineNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("Quantity");
                }

                return null;
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve order line quantity.",
                    e);
        }
    }

    /**
     * Updates the quantity of a specific order line.
     *
     * @param orderId    order containing the line
     * @param lineNumber line number within the order
     * @param quantity   new quantity
     */
    public void updateOrderLineQuantity(
            int orderId,
            int lineNumber,
            int quantity) {

        String query = """
                EXEC dbo.usp_UpdateOrderLineQuantity
                    @OrderId = ?,
                    @LineNumber = ?,
                    @Quantity = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, orderId);
            statement.setInt(2, lineNumber);
            statement.setInt(3, quantity);
            statement.execute();

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not update order line quantity.",
                    e);
        }
    }

    /**
     * Removes a product from an order by deleting its order line.
     *
     * @param orderId    order containing the line
     * @param lineNumber line number to remove
     */
    public void removeProductFromOrder(
            int orderId,
            int lineNumber) {

        String query = """
                EXEC dbo.usp_RemoveProductFromOrder
                    @OrderId = ?,
                    @LineNumber = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, orderId);
            statement.setInt(2, lineNumber);
            statement.execute();

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not remove product from order.",
                    e);
        }
    }

    /**
     * Retrieves orders containing every product currently registered.
     *
     * @return orders containing every registered product
     */
    public List<Order> getOrdersContainingEveryProduct() {
        String query = """
                SELECT
                    ot.OrderID,
                    ot.OrderDate,
                    ot.CustomerID,
                    ot.CustomerName,
                    ot.LoyaltyName,
                    ot.DiscountPercentage,
                    ot.Subtotal,
                    ot.OrderTotal
                FROM dbo.vw_OrderTotal AS ot
                JOIN dbo.vw_OrdersContainingEveryProduct AS ep
                    ON ep.OrderID = ot.OrderID
                ORDER BY
                    ot.OrderDate DESC,
                    ot.OrderID DESC
                """;

        List<Order> orders = new ArrayList<>();

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                orders.add(mapOrder(resultSet));
            }

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve orders containing every product.",
                    e);
        }

        return orders;
    }

    /**
     * Retrieves the total number of orders in the database.
     *
     * @return total order count
     */
    public int getTotalNumberOfOrders() {
        String query = """
                SELECT TotalNumberOfOrders
                FROM dbo.vw_OrderCount
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            if (!resultSet.next()) {
                return 0;
            }

            return resultSet.getInt("TotalNumberOfOrders");

        } catch (SQLException e) {
            throw new DaoException(
                    "Could not retrieve the total number of orders.",
                    e);
        }
    }

    // Maps the current result row to an Order.
    private Order mapOrder(ResultSet resultSet) throws SQLException {
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

    // Maps the current result row to an OrderLine.
    private OrderLine mapOrderLine(
            ResultSet resultSet,
            int orderId,
            int lineNumber) throws SQLException {

        return new OrderLine(
                orderId,
                lineNumber,
                resultSet.getInt("ProductID"),
                resultSet.getString("ProductName"),
                resultSet.getInt("Quantity"),
                resultSet.getBigDecimal("UnitPrice"));
    }
}
