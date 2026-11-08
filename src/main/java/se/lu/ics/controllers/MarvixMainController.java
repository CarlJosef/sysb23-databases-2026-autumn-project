package se.lu.ics.controllers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import se.lu.ics.data.CustomerDao;
import se.lu.ics.data.DaoException;
import se.lu.ics.data.DatabaseSessionDao;
import se.lu.ics.data.OrderDao;
import se.lu.ics.data.ProductDao;
import se.lu.ics.models.DatabaseSessionInfo;
import se.lu.ics.models.Order;

/**
 * Main controller for the Marvix application shell.
 *
 * Handles application navigation, dashboard functionality and shared
 * database session information. Feature-specific functionality is handled
 * by dedicated controllers.
 */
public class MarvixMainController {

        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        private static final int RECENT_ORDER_LIMIT = 5;

        // Dashboard data access.
        private CustomerDao customerDao;
        private ProductDao productDao;
        private OrderDao orderDao;

        // Main application views.
        @FXML
        private ScrollPane dashboardScreen;

        @FXML
        private Node customerView;

        @FXML
        private Node productView;

        @FXML
        private Node orderView;

        @FXML
        private Node metadataView;

        @FXML
        private Node excelView;

        // Controllers loaded through fx:include.
        @FXML
        private CustomerController customerViewController;

        @FXML
        private ProductController productViewController;

        @FXML
        private OrderController orderViewController;

        // Navigation buttons.
        @FXML
        private Button dashboardNavigationButton;

        @FXML
        private Button customersNavigationButton;

        @FXML
        private Button productsNavigationButton;

        @FXML
        private Button ordersNavigationButton;

        @FXML
        private Button metadataNavigationButton;

        @FXML
        private Button excelNavigationButton;

        @FXML
        private Label breadcrumbLabel;

        // Dashboard summary values.
        @FXML
        private Label totalCustomersLabel;

        @FXML
        private Label totalProductsLabel;

        @FXML
        private Label totalOrdersLabel;

        @FXML
        private Label totalLoyaltyLevelsLabel;

        @FXML
        private Label loyaltyLevelNamesLabel;

        // Recent orders use a small read-only projection of the complete Order model.
        @FXML
        private TableView<Map<String, Object>> recentOrdersTable;

        @FXML
        private TableColumn<Map<String, Object>, Integer> recentOrderIdColumn;

        @FXML
        private TableColumn<Map<String, Object>, String> recentOrderCustomerColumn;

        @FXML
        private TableColumn<Map<String, Object>, String> recentOrderDateColumn;

        @FXML
        private TableColumn<Map<String, Object>, String> recentOrderTotalColumn;

        // Top-spending customers.
        @FXML
        private TableView<Map<String, Object>> topSpendingCustomersTable;

        @FXML
        private TableColumn<Map<String, Object>, Integer> topSpenderCustomerIdColumn;

        @FXML
        private TableColumn<Map<String, Object>, String> topSpenderCustomerNameColumn;

        @FXML
        private TableColumn<Map<String, Object>, String> topSpenderAmountSpentColumn;

        // Database session information.
        @FXML
        private Label databaseStatusLabel;

        @FXML
        private Label databaseStatusDotLabel;

        @FXML
        private Label databaseUserInitialsLabel;

        @FXML
        private Label databaseUserLabel;

        @FXML
        private Label databaseRoleLabel;

        // Initializes the application shell and dashboard.
        @FXML
        private void initialize() {

                configureDashboardTables();

                recentOrdersTable.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY);

                topSpendingCustomersTable.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY);

                showDashboard();
                loadDatabaseSession();

                try {
                        customerDao = new CustomerDao();
                        productDao = new ProductDao();
                        orderDao = new OrderDao();

                        loadDashboardData();

                } catch (DaoException e) {
                        showDashboardUnavailable();
                }
        }

        // Configures the dashboard table columns.
        private void configureDashboardTables() {

                recentOrderIdColumn.setCellValueFactory(
                                data -> new ReadOnlyObjectWrapper<>(
                                                (Integer) data.getValue().get("orderId")));

                recentOrderCustomerColumn.setCellValueFactory(
                                data -> new ReadOnlyObjectWrapper<>(
                                                (String) data.getValue().get("customerName")));

                recentOrderDateColumn.setCellValueFactory(
                                data -> new ReadOnlyObjectWrapper<>(
                                                (String) data.getValue().get("orderDate")));

                recentOrderTotalColumn.setCellValueFactory(
                                data -> new ReadOnlyObjectWrapper<>(
                                                (String) data.getValue().get("orderTotal")));

                topSpenderCustomerIdColumn.setCellValueFactory(
                                data -> new ReadOnlyObjectWrapper<>(
                                                (Integer) data.getValue().get("customerId")));

                topSpenderCustomerNameColumn.setCellValueFactory(
                                data -> new ReadOnlyObjectWrapper<>(
                                                (String) data.getValue().get("customerName")));

                topSpenderAmountSpentColumn.setCellValueFactory(
                                data -> new ReadOnlyObjectWrapper<>(
                                                (String) data.getValue().get("amountSpent")));
        }

        // Loads dashboard information from the existing DAO layer.
        private void loadDashboardData() {

                if (customerDao == null
                                || productDao == null
                                || orderDao == null) {

                        throw new DaoException(
                                        "Dashboard data access is not available.");
                }

                var customers = customerDao.getAllCustomers();
                var products = productDao.getAllProducts();
                List<Order> orders = orderDao.getAllOrders();

                totalCustomersLabel.setText(
                                String.valueOf(customers.size()));

                totalProductsLabel.setText(
                                String.valueOf(products.size()));

                totalOrdersLabel.setText(
                                String.valueOf(orders.size()));

                List<String> loyaltyLevelNames = customerDao.getLoyaltyLevelNames();

                totalLoyaltyLevelsLabel.setText(
                                String.valueOf(loyaltyLevelNames.size()));

                loyaltyLevelNamesLabel.setText(
                                String.join(", ", loyaltyLevelNames));

                loadRecentOrders(orders);
                loadTopSpenders(orders);
        }

        // Displays the latest orders; OrderDao returns newest orders first.
        private void loadRecentOrders(List<Order> orders) {

                ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList();

                int limit = Math.min(
                                RECENT_ORDER_LIMIT,
                                orders.size());

                for (int i = 0; i < limit; i++) {

                        Order order = orders.get(i);

                        Map<String, Object> row = new LinkedHashMap<>();

                        row.put(
                                        "orderId",
                                        order.getOrderId());

                        row.put(
                                        "customerName",
                                        order.getCustomerName());

                        row.put(
                                        "orderDate",
                                        order.getOrderDate() == null
                                                        ? "-"
                                                        : order.getOrderDate()
                                                                        .format(DATE_FORMAT));

                        row.put(
                                        "orderTotal",
                                        formatMoney(
                                                        order.getOrderTotal()));

                        rows.add(row);
                }

                recentOrdersTable.setItems(rows);

                recentOrdersTable.setPlaceholder(
                                new Label("No orders found."));
        }

        // Calculates the top spender or tied top spenders from loaded orders.
        private void loadTopSpenders(List<Order> orders) {

                Map<Integer, BigDecimal> totals = new LinkedHashMap<>();

                Map<Integer, String> customerNames = new LinkedHashMap<>();

                for (Order order : orders) {

                        int customerId = order.getCustomerId();

                        BigDecimal orderTotal = order.getOrderTotal() == null
                                        ? BigDecimal.ZERO
                                        : order.getOrderTotal();

                        totals.merge(
                                        customerId,
                                        orderTotal,
                                        BigDecimal::add);

                        customerNames.putIfAbsent(
                                        customerId,
                                        order.getCustomerName());
                }

                ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList();

                if (totals.isEmpty()) {

                        topSpendingCustomersTable.setItems(rows);

                        topSpendingCustomersTable.setPlaceholder(
                                        new Label(
                                                        "No customer spending data found."));

                        return;
                }

                BigDecimal highestAmount = totals.values()
                                .stream()
                                .max(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO);

                for (Map.Entry<Integer, BigDecimal> entry : totals.entrySet()) {

                        if (entry.getValue()
                                        .compareTo(highestAmount) != 0) {

                                continue;
                        }

                        Map<String, Object> row = new LinkedHashMap<>();

                        row.put(
                                        "customerId",
                                        entry.getKey());

                        row.put(
                                        "customerName",
                                        customerNames.get(
                                                        entry.getKey()));

                        row.put(
                                        "amountSpent",
                                        formatMoney(
                                                        entry.getValue()));

                        rows.add(row);
                }

                topSpendingCustomersTable.setItems(rows);

                topSpendingCustomersTable.setPlaceholder(
                                new Label(
                                                "No customer spending data found."));
        }

        // Shows an unavailable state when dashboard data cannot be loaded.
        private void showDashboardUnavailable() {

                totalCustomersLabel.setText("—");
                totalProductsLabel.setText("—");
                totalOrdersLabel.setText("—");
                totalLoyaltyLevelsLabel.setText("—");
                loyaltyLevelNamesLabel.setText("—");

                recentOrdersTable.getItems().clear();
                topSpendingCustomersTable.getItems().clear();

                recentOrdersTable.setPlaceholder(
                                new Label(
                                                "Dashboard data could not be loaded."));

                topSpendingCustomersTable.setPlaceholder(
                                new Label(
                                                "Dashboard data could not be loaded."));
        }

        // Loads the active SQL Server session and updates the global status.
        private void loadDatabaseSession() {

                try {

                        DatabaseSessionDao databaseSessionDao = new DatabaseSessionDao();

                        DatabaseSessionInfo session = databaseSessionDao.getCurrentSession();

                        updateDatabaseStatus(true);

                        databaseUserLabel.setText(
                                        session.getLoginName());

                        databaseRoleLabel.setText(
                                        session.getDatabaseRole());

                        databaseUserInitialsLabel.setText(
                                        createInitials(
                                                        session.getLoginName()));

                } catch (DaoException e) {

                        updateDatabaseStatus(false);

                        databaseUserLabel.setText(
                                        "No database session");

                        databaseRoleLabel.setText(
                                        "Not connected");

                        databaseUserInitialsLabel.setText("--");
                }
        }

        // Updates the database connection text and status indicator.
        private void updateDatabaseStatus(
                        boolean connected) {

                databaseStatusLabel.setText(
                                connected
                                                ? "Connected"
                                                : "Not connected");

                databaseStatusDotLabel
                                .getStyleClass()
                                .removeAll(
                                                "db-status-online",
                                                "db-status-offline");

                databaseStatusDotLabel
                                .getStyleClass()
                                .add(
                                                connected
                                                                ? "db-status-online"
                                                                : "db-status-offline");
        }

        // Creates up to two initials from a database login name.
        private String createInitials(
                        String value) {

                if (value == null
                                || value.isBlank()) {

                        return "--";
                }

                String[] parts = value.trim()
                                .split("[_\\s.-]+");

                StringBuilder initials = new StringBuilder();

                for (String part : parts) {

                        if (!part.isBlank()) {

                                initials.append(
                                                Character.toUpperCase(
                                                                part.charAt(0)));
                        }

                        if (initials.length() == 2) {
                                break;
                        }
                }

                return initials.toString();
        }

        /** Displays the dashboard view. */
        @FXML
        public void showDashboard() {

                showOnly(
                                dashboardScreen,
                                dashboardNavigationButton,
                                "Dashboard");
        }

        /** Displays the customer view. */
        @FXML
        public void showCustomers() {

                showOnly(
                                customerView,
                                customersNavigationButton,
                                "Customers");
        }

        /** Displays the product view. */
        @FXML
        public void showProducts() {

                showOnly(
                                productView,
                                productsNavigationButton,
                                "Products");
        }

        /** Displays the order view. */
        @FXML
        public void showOrders() {

                showOnly(
                                orderView,
                                ordersNavigationButton,
                                "Orders");

                orderViewController.showOrders();
        }

        /** Displays the metadata view. */
        @FXML
        public void showMetadata() {

                showOnly(
                                metadataView,
                                metadataNavigationButton,
                                "Metadata");
        }

        /** Displays the Excel view. */
        @FXML
        public void showExcel() {

                showOnly(
                                excelView,
                                excelNavigationButton,
                                "Excel");
        }

        // Shows one application view and hides all other main views.
        private void showOnly(
                        Node visibleView,
                        Button currentButton,
                        String breadcrumb) {

                for (Node view : List.of(
                                dashboardScreen,
                                customerView,
                                productView,
                                orderView,
                                metadataView,
                                excelView)) {

                        setVisibleAndManaged(
                                        view,
                                        view == visibleView);
                }

                for (Button button : List.of(
                                dashboardNavigationButton,
                                customersNavigationButton,
                                productsNavigationButton,
                                ordersNavigationButton,
                                metadataNavigationButton,
                                excelNavigationButton)) {

                        button.getStyleClass()
                                        .remove("nav-selected");
                }

                currentButton
                                .getStyleClass()
                                .add("nav-selected");

                breadcrumbLabel.setText(
                                breadcrumb);
        }

        // Keeps JavaFX visibility and layout participation synchronized.
        private void setVisibleAndManaged(
                        Node node,
                        boolean state) {

                node.setVisible(state);
                node.setManaged(state);
        }

        /** Opens the customer view and starts the new customer workflow. */
        @FXML
        public void handleNewCustomer() {

                showCustomers();

                customerViewController
                                .handleNewCustomer();
        }

        /** Opens the product view and starts the new product workflow. */
        @FXML
        public void handleNewProduct() {

                showProducts();

                productViewController
                                .handleNewProduct();
        }

        /** Opens the order view and starts the new order workflow. */
        @FXML
        public void handleNewOrder() {

                showOnly(
                                orderView,
                                ordersNavigationButton,
                                "Orders");

                orderViewController
                                .handleNewOrder();
        }

        /** Refreshes all dashboard information. */
        @FXML
        public void handleRefreshDashboard() {

                try {

                        loadDashboardData();
                        loadDatabaseSession();

                } catch (DaoException e) {

                        showError(
                                        "Dashboard refresh failed",
                                        "Dashboard data could not be retrieved from the database.");
                }
        }

        /** Refreshes the top-spending customer information. */
        @FXML
        public void handleRefreshTopSpenders() {

                if (orderDao == null) {

                        showError(
                                        "Database unavailable",
                                        "Order data access is not available.");

                        return;
                }

                try {

                        loadTopSpenders(
                                        orderDao.getAllOrders());

                } catch (DaoException e) {

                        showError(
                                        "Could not refresh results",
                                        "Top-spending customer data could not be retrieved.");
                }
        }

        // Formats monetary values consistently.
        private String formatMoney(
                        BigDecimal value) {

                if (value == null) {
                        return "0.00";
                }

                return value
                                .setScale(
                                                2,
                                                RoundingMode.HALF_UP)
                                .toPlainString();
        }

        // Displays a database-related error.
        private void showError(
                        String header,
                        String message) {

                Alert alert = new Alert(
                                Alert.AlertType.ERROR);

                alert.setTitle(
                                "Marvix - Database Management System");

                alert.setHeaderText(header);
                alert.setContentText(message);

                alert.showAndWait();
        }
}
