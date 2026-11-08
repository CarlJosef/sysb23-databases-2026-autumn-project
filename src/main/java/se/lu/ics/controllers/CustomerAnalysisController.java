package se.lu.ics.controllers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import se.lu.ics.data.CustomerDao;
import se.lu.ics.data.DaoException;
import se.lu.ics.models.Customer;
import se.lu.ics.models.Order;
import se.lu.ics.models.Product;

/**
 * Displays order history, latest order, and ordered products for a selected
 * customer.
 */
public class CustomerAnalysisController {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MONEY_SCALE = 2;

    private CustomerDao customerDao;

    @FXML
    private StackPane customerAnalysisOverlay;
    @FXML
    private Label customerAnalysisTitleLabel;
    @FXML
    private TabPane customerAnalysisTabPane;

    @FXML
    private TableView<Order> customerOrdersTable;
    @FXML
    private TableColumn<Order, Integer> customerOrderIdColumn;
    @FXML
    private TableColumn<Order, LocalDateTime> customerOrderDateColumn;
    @FXML
    private TableColumn<Order, BigDecimal> customerOrderSubtotalColumn;
    @FXML
    private TableColumn<Order, BigDecimal> customerOrderDiscountColumn;
    @FXML
    private TableColumn<Order, BigDecimal> customerOrderTotalColumn;

    @FXML
    private Label latestOrderIdLabel;
    @FXML
    private Label latestOrderDateLabel;
    @FXML
    private Label latestOrderTotalLabel;

    @FXML
    private TableView<Product> customerOrderedProductsTable;
    @FXML
    private TableColumn<Product, Integer> customerProductIdColumn;
    @FXML
    private TableColumn<Product, String> customerProductNameColumn;
    @FXML
    private TableColumn<Product, BigDecimal> customerProductPriceColumn;

    // Prepares table bindings and sizing when the FXML view is loaded.
    @FXML
    private void initialize() {
        configureCustomerOrdersTable();
        configureCustomerProductsTable();

        for (TableView<?> table : List.of(customerOrdersTable, customerOrderedProductsTable)) {
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
    }

    /**
     * Supplies the customer data access object used by the analysis view.
     *
     * @param customerDao data access object for customer analysis queries
     */
    public void setCustomerDao(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    /**
     * Loads analysis data for a customer and opens the requested analysis tab.
     *
     * @param customer customer whose data should be analysed
     * @param tabIndex zero-based index of the tab to display
     */
    public void openAnalysis(Customer customer, int tabIndex) {
        if (customer == null) {
            showError("Customer unavailable", "No customer was selected for analysis.");
            return;
        }

        if (customerDao == null) {
            showError("Database unavailable", "Customer data access is not available.");
            return;
        }

        try {
            loadCustomerOrders(customer.getCustomerId());
            loadLatestCustomerOrder(customer.getCustomerId());
            loadCustomerProducts(customer.getCustomerId());

            customerAnalysisTitleLabel.setText(
                    "Customer Analysis - " + customer.getCustomerId() + " - " + customer.getCustomerName());

            if (!customerAnalysisTabPane.getTabs().isEmpty()) {
                int safeTabIndex = Math.max(
                        0,
                        Math.min(tabIndex, customerAnalysisTabPane.getTabs().size() - 1));
                customerAnalysisTabPane.getSelectionModel().select(safeTabIndex);
            }

            setAnalysisVisible(true);
        } catch (DaoException e) {
            showError(
                    "Customer analysis failed",
                    "Customer analysis data could not be retrieved from the database.");
        }
    }

    // Configures the table used to show all orders for the selected customer.
    private void configureCustomerOrdersTable() {
        customerOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerOrderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        customerOrderSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        customerOrderDiscountColumn.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
        customerOrderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("orderTotal"));

        customerOrderDateColumn.setCellFactory(tableColumn -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime orderDate, boolean empty) {
                super.updateItem(orderDate, empty);
                setText(empty || orderDate == null ? null : orderDate.format(DATE_TIME_FORMAT));
            }
        });

        formatMoneyColumn(customerOrderSubtotalColumn);
        formatPercentageColumn(customerOrderDiscountColumn);
        formatMoneyColumn(customerOrderTotalColumn);
    }

    // Configures the table used to show products previously ordered by the
    // customer.
    private void configureCustomerProductsTable() {
        customerProductIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        customerProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        customerProductPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatMoneyColumn(customerProductPriceColumn);
    }

    // Shows every order registered for the selected customer.
    private void loadCustomerOrders(int customerId) {
        List<Order> orders = customerDao.getOrdersByCustomer(customerId);
        customerOrdersTable.setItems(FXCollections.observableArrayList(orders));
        customerOrdersTable.setPlaceholder(new Label("This customer has no orders."));
    }

    // Shows the most recent order registered for the selected customer.
    private void loadLatestCustomerOrder(int customerId) {
        Order latestOrder = customerDao.getLatestOrderByCustomer(customerId);

        if (latestOrder == null) {
            latestOrderIdLabel.setText("No order");
            latestOrderDateLabel.setText("-");
            latestOrderTotalLabel.setText("-");
            return;
        }

        latestOrderIdLabel.setText(String.valueOf(latestOrder.getOrderId()));
        latestOrderDateLabel.setText(
                latestOrder.getOrderDate() == null
                        ? "-"
                        : latestOrder.getOrderDate().format(DATE_TIME_FORMAT));
        latestOrderTotalLabel.setText(formatMoney(latestOrder.getOrderTotal()));
    }

    // Shows every product previously ordered by the selected customer.
    private void loadCustomerProducts(int customerId) {
        List<Product> products = customerDao.getProductsByCustomer(customerId);
        customerOrderedProductsTable.setItems(FXCollections.observableArrayList(products));
        customerOrderedProductsTable.setPlaceholder(
                new Label("This customer has not ordered any products."));
    }

    // Closes the customer analysis overlay without changing customer data.
    @FXML
    private void handleCloseCustomerAnalysis() {
        setAnalysisVisible(false);
    }

    // Formats percentage values consistently in analysis tables.
    private void formatPercentageColumn(TableColumn<Order, BigDecimal> column) {
        column.setCellFactory(tableColumn -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null
                        ? null
                        : value.stripTrailingZeros().toPlainString() + " %");
            }
        });
    }

    // Formats monetary values consistently in analysis tables.
    private <T> void formatMoneyColumn(TableColumn<T, BigDecimal> column) {
        column.setCellFactory(tableColumn -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : formatMoney(value));
            }
        });
    }

    // Uses two decimal places for monetary values shown in the user interface.
    private String formatMoney(BigDecimal value) {
        return value == null
                ? "-"
                : value.setScale(MONEY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    // Keeps JavaFX visibility and layout state synchronized for the overlay.
    private void setAnalysisVisible(boolean visible) {
        customerAnalysisOverlay.setVisible(visible);
        customerAnalysisOverlay.setManaged(visible);
    }

    // Presents database failures as a user-friendly error dialog.
    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Marvix - Customers");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
