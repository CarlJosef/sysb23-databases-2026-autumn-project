package se.lu.ics.controllers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import se.lu.ics.data.CustomerDao;
import se.lu.ics.data.DaoException;
import se.lu.ics.data.OrderDao;
import se.lu.ics.models.Customer;
import se.lu.ics.models.Order;

/**
 * Controller for the Marvix order main view.
 *
 * Handles order listing, customer filtering, navigation to order details
 * and order-related reporting.
 */
public class OrderController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private OrderDao orderDao;
    private CustomerDao customerDao;

    @FXML
    private ScrollPane ordersScreen;

    @FXML
    private ComboBox<Customer> orderCustomerFilterComboBox;

    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn;

    @FXML
    private TableColumn<Order, LocalDateTime> orderDateColumn;

    @FXML
    private TableColumn<Order, Integer> orderCustomerIdColumn;

    @FXML
    private TableColumn<Order, String> orderCustomerNameColumn;

    @FXML
    private TableColumn<Order, String> orderLoyaltyColumn;

    @FXML
    private TableColumn<Order, BigDecimal> orderSubtotalColumn;

    @FXML
    private TableColumn<Order, BigDecimal> orderDiscountColumn;

    @FXML
    private TableColumn<Order, BigDecimal> orderDiscountAmountColumn;

    @FXML
    private TableColumn<Order, BigDecimal> orderTotalColumn;

    @FXML
    private TableColumn<Order, Void> orderActionsColumn;

    @FXML
    private TableView<Order> ordersContainingEveryProductTable;

    @FXML
    private TableColumn<Order, Integer> everyProductOrderIdColumn;

    @FXML
    private TableColumn<Order, LocalDateTime> everyProductOrderDateColumn;

    @FXML
    private TableColumn<Order, Integer> everyProductCustomerIdColumn;

    @FXML
    private TableColumn<Order, String> everyProductCustomerNameColumn;

    // Injected automatically from fx:include fx:id="orderDetails".
    @FXML
    private OrderDetailsController orderDetailsController;

    /**
     * Configures the order view and loads the initial order data.
     */
    @FXML
    private void initialize() {
        configureTables();
        configureCustomerFilterComboBox();

        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ordersContainingEveryProductTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        if (orderDetailsController != null) {
            orderDetailsController.setOrderController(this);
        }

        try {
            orderDao = new OrderDao();
            customerDao = new CustomerDao();

            loadCustomerFilter();
            loadOrders();
            loadOrdersContainingEveryProduct();

        } catch (DaoException e) {
            ordersTable.setPlaceholder(
                    tablePlaceholder("Order data could not be loaded."));
            ordersContainingEveryProductTable.setPlaceholder(
                    tablePlaceholder("Order report data could not be loaded."));
        }
    }

    // Configures the data mappings and presentation of both order tables.
    private void configureTables() {
        orderIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("orderId"));
        orderDateColumn.setCellValueFactory(
                new PropertyValueFactory<>("orderDate"));
        orderCustomerIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("customerId"));
        orderCustomerNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("customerName"));
        orderLoyaltyColumn.setCellValueFactory(
                new PropertyValueFactory<>("loyaltyName"));
        orderSubtotalColumn.setCellValueFactory(
                new PropertyValueFactory<>("subtotal"));
        orderDiscountColumn.setCellValueFactory(
                new PropertyValueFactory<>("discountPercentage"));
        orderDiscountAmountColumn.setCellValueFactory(
                data -> new ReadOnlyObjectWrapper<>(
                        discountAmount(data.getValue())));
        orderTotalColumn.setCellValueFactory(
                new PropertyValueFactory<>("orderTotal"));

        everyProductOrderIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("orderId"));
        everyProductOrderDateColumn.setCellValueFactory(
                new PropertyValueFactory<>("orderDate"));
        everyProductCustomerIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("customerId"));
        everyProductCustomerNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("customerName"));

        formatDateColumn(orderDateColumn);
        formatDateColumn(everyProductOrderDateColumn);
        formatMoneyColumn(orderSubtotalColumn);
        formatMoneyColumn(orderDiscountAmountColumn);
        formatMoneyColumn(orderTotalColumn);
        formatPercentageColumn(orderDiscountColumn);
        configureOrderActions();
    }

    // Adds the View action to each row in the main order table.
    private void configureOrderActions() {
        orderActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("View");

            {
                viewButton.getStyleClass().add("button-secondary");
                viewButton.setOnAction(event -> {
                    Order order = getTableView()
                            .getItems()
                            .get(getIndex());

                    openOrderDetails(order.getOrderId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });
    }

    // Formats customer choices consistently in the non-editable filter control.
    private void configureCustomerFilterComboBox() {
        orderCustomerFilterComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : formatCustomer(customer);
            }

            @Override
            public Customer fromString(String value) {
                return null;
            }
        });

        orderCustomerFilterComboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                setText(
                        empty || customer == null
                                ? null
                                : formatCustomer(customer));
            }
        });
    }

    // Loads customers used by the order filter.
    private void loadCustomerFilter() {
        List<Customer> customers = customerDao.getAllCustomers();
        orderCustomerFilterComboBox.setItems(
                FXCollections.observableArrayList(customers));
    }

    // Loads every order into the main table.
    private void loadOrders() {
        ordersTable.setItems(
                FXCollections.observableArrayList(orderDao.getAllOrders()));
        ordersTable.getSelectionModel().clearSelection();
        ordersTable.setPlaceholder(tablePlaceholder("No orders found."));
    }

    // Loads only the orders belonging to the selected customer.
    private void loadOrdersByCustomer(int customerId) {
        ordersTable.setItems(
                FXCollections.observableArrayList(
                        orderDao.getOrdersByCustomer(customerId)));
        ordersTable.getSelectionModel().clearSelection();
        ordersTable.setPlaceholder(
                tablePlaceholder("This customer has no orders."));
    }

    // Loads the A-grade report for orders containing every registered product.
    private void loadOrdersContainingEveryProduct() {
        ordersContainingEveryProductTable.setItems(
                FXCollections.observableArrayList(
                        orderDao.getOrdersContainingEveryProduct()));
        ordersContainingEveryProductTable.getSelectionModel().clearSelection();
        ordersContainingEveryProductTable.setPlaceholder(
                tablePlaceholder(
                        "No order currently contains every product."));
    }

    /**
     * Displays the main order list and refreshes its order data.
     *
     * Called from the main application controller and OrderDetailsController.
     */
    public void showOrders() {
        if (orderDetailsController != null) {
            orderDetailsController.hideDetails();
        }

        setOrdersVisible(true);

        if (orderDao == null) {
            return;
        }

        try {
            loadOrders();
            loadOrdersContainingEveryProduct();

        } catch (DaoException e) {
            showError(
                    "Could not load orders",
                    "Order data could not be retrieved from the database.");
        }
    }

    /**
     * Filters the main order table by the selected customer.
     *
     * A cleared customer selection displays all orders.
     */
    @FXML
    public void handleFilterOrders() {
        if (orderDao == null) {
            showError(
                    "Database unavailable",
                    "Order data access is not available.");
            return;
        }

        try {
            Customer customer = orderCustomerFilterComboBox.getValue();

            if (customer == null) {
                loadOrders();
            } else {
                loadOrdersByCustomer(customer.getCustomerId());
            }

        } catch (DaoException e) {
            showError(
                    "Order filter failed",
                    "Orders could not be filtered by customer.");
        }
    }

    /**
     * Opens the workflow for creating a new order.
     *
     * Called both from FXML and the main application controller.
     */
    @FXML
    public void handleNewOrder() {
        if (orderDetailsController == null) {
            showError(
                    "Order view unavailable",
                    "The order details view could not be loaded.");
            return;
        }

        setOrdersVisible(false);
        orderDetailsController.showNewOrder();
    }

    /**
     * Refreshes the report showing orders that contain every product.
     */
    @FXML
    public void handleRefreshOrdersContainingEveryProduct() {
        if (orderDao == null) {
            showError(
                    "Database unavailable",
                    "Order data access is not available.");
            return;
        }

        try {
            loadOrdersContainingEveryProduct();

        } catch (DaoException e) {
            showError(
                    "Could not refresh results",
                    "Orders containing every product could not be retrieved.");
        }
    }

    // Opens an existing order in the separate details view.
    private void openOrderDetails(int orderId) {
        if (orderDetailsController == null) {
            showError(
                    "Order view unavailable",
                    "The order details view could not be loaded.");
            return;
        }

        setOrdersVisible(false);
        orderDetailsController.showOrder(orderId);
    }

    /**
     * Refreshes order tables after a create, update or remove operation.
     *
     * Called by OrderDetailsController after successful order changes.
     */
    public void refreshOrdersAfterChange() {
        if (orderDao == null) {
            return;
        }

        try {
            loadOrders();
            loadOrdersContainingEveryProduct();

        } catch (DaoException e) {
            showError(
                    "Could not refresh orders",
                    "Updated order data could not be retrieved.");
        }
    }

    // Keeps visibility and layout participation synchronized for the main view.
    private void setOrdersVisible(boolean visible) {
        ordersScreen.setVisible(visible);
        ordersScreen.setManaged(visible);
    }

    // Creates a consistently styled empty-table message.
    private Label tablePlaceholder(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("table-placeholder");
        return label;
    }

    // Formats a customer for display in the order filter.
    private String formatCustomer(Customer customer) {
        return customer.getCustomerId()
                + " - "
                + customer.getCustomerName();
    }

    // Formats order dates consistently across order tables.
    private String formatDate(LocalDateTime date) {
        return date == null ? "-" : date.format(DATE_FORMAT);
    }

    // Formats monetary values with two decimal places.
    private String formatMoney(BigDecimal value) {
        return value == null
                ? "-"
                : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // Calculates the discount amount shown for an order.
    private BigDecimal discountAmount(Order order) {
        if (order.getSubtotal() == null
                || order.getOrderTotal() == null) {
            return BigDecimal.ZERO;
        }

        return order.getSubtotal().subtract(order.getOrderTotal());
    }

    // Applies date formatting to an order date column.
    private void formatDateColumn(
            TableColumn<Order, LocalDateTime> column) {

        column.setCellFactory(cellColumn -> new TableCell<>() {
            @Override
            protected void updateItem(
                    LocalDateTime value,
                    boolean empty) {

                super.updateItem(value, empty);
                setText(
                        empty || value == null
                                ? null
                                : formatDate(value));
            }
        });
    }

    // Applies money formatting to a monetary table column.
    private <T> void formatMoneyColumn(
            TableColumn<T, BigDecimal> column) {

        column.setCellFactory(cellColumn -> new TableCell<>() {
            @Override
            protected void updateItem(
                    BigDecimal value,
                    boolean empty) {

                super.updateItem(value, empty);
                setText(
                        empty || value == null
                                ? null
                                : formatMoney(value));
            }
        });
    }

    // Applies percentage formatting to the discount column.
    private void formatPercentageColumn(
            TableColumn<Order, BigDecimal> column) {

        column.setCellFactory(cellColumn -> new TableCell<>() {
            @Override
            protected void updateItem(
                    BigDecimal value,
                    boolean empty) {

                super.updateItem(value, empty);
                setText(
                        empty || value == null
                                ? null
                                : value.stripTrailingZeros().toPlainString()
                                        + " %");
            }
        });
    }

    // Displays an order-related error to the user.
    private void showError(
            String header,
            String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Marvix - Orders");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
