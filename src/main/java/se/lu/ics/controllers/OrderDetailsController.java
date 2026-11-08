package se.lu.ics.controllers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import se.lu.ics.data.CustomerDao;
import se.lu.ics.data.DaoException;
import se.lu.ics.data.OrderDao;
import se.lu.ics.data.ProductDao;
import se.lu.ics.models.Customer;
import se.lu.ics.models.Order;
import se.lu.ics.models.OrderLine;
import se.lu.ics.models.Product;

/**
 * Controller for one active order.
 *
 * Handles order creation, order lines, quantities and totals.
 */
public class OrderDetailsController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MIN_ORDER_QUANTITY = 1;
    private static final int MAX_ORDER_QUANTITY = 100;
    private static final int DEFAULT_ORDER_QUANTITY = 1;

    private OrderDao orderDao;
    private CustomerDao customerDao;
    private ProductDao productDao;
    private OrderController orderController;
    private Integer currentOrderId;

    @FXML
    private ScrollPane orderDetailsScreen;

    @FXML
    private VBox createOrderCustomerPanel;

    @FXML
    private ComboBox<Customer> createOrderCustomerComboBox;

    @FXML
    private Label createOrderCustomerValidationLabel;

    @FXML
    private Label orderDetailsTitleLabel;

    @FXML
    private Label orderDetailsIdLabel;

    @FXML
    private Label orderDetailsDateLabel;

    @FXML
    private Label orderDetailsCustomerIdLabel;

    @FXML
    private Label orderDetailsCustomerNameLabel;

    @FXML
    private Label orderDetailsLoyaltyLabel;

    @FXML
    private TableView<OrderLine> orderLinesTable;

    @FXML
    private TableColumn<OrderLine, Integer> lineNumberColumn;

    @FXML
    private TableColumn<OrderLine, Integer> lineProductIdColumn;

    @FXML
    private TableColumn<OrderLine, String> lineProductNameColumn;

    @FXML
    private TableColumn<OrderLine, BigDecimal> lineUnitPriceColumn;

    @FXML
    private TableColumn<OrderLine, Integer> lineQuantityColumn;

    @FXML
    private TableColumn<OrderLine, BigDecimal> lineTotalColumn;

    @FXML
    private TableColumn<OrderLine, Void> lineActionsColumn;

    @FXML
    private ComboBox<Product> orderProductComboBox;

    @FXML
    private TextField orderLineNumberField;

    @FXML
    private Spinner<Integer> orderQuantitySpinner;

    @FXML
    private Label orderLineValidationLabel;

    @FXML
    private Label orderSubtotalLabel;

    @FXML
    private Label orderDiscountLabel;

    @FXML
    private Label orderTotalLabel;

    @FXML
    private void initialize() {
        orderQuantitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        MIN_ORDER_QUANTITY,
                        MAX_ORDER_QUANTITY,
                        DEFAULT_ORDER_QUANTITY));

        configureOrderLineTable();
        configureComboBox(
                createOrderCustomerComboBox,
                customer -> customer.getCustomerId()
                        + " - "
                        + customer.getCustomerName());
        configureComboBox(
                orderProductComboBox,
                product -> product.getProductId()
                        + " - "
                        + product.getProductName());

        orderLinesTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            orderDao = new OrderDao();
            customerDao = new CustomerDao();
            productDao = new ProductDao();
        } catch (DaoException e) {
            orderLineValidationLabel.setText(
                    "Order data access is not available.");
        }
    }

    /**
     * Supplies the parent order controller used for navigation and refreshes.
     *
     * @param orderController parent order controller
     */
    public void setOrderController(OrderController orderController) {
        this.orderController = orderController;
    }

    /** Opens the view for creating a new order. */
    public void showNewOrder() {
        if (!isDataAccessAvailable()) {
            showError(
                    "Database unavailable",
                    "Order data access is not available.");
            returnToOrders();
            return;
        }

        try {
            refreshReferenceData();
        } catch (DaoException e) {
            showError(
                    "Could not prepare new order",
                    "Customer or product data could not be loaded.");
            returnToOrders();
            return;
        }

        currentOrderId = null;
        orderDetailsTitleLabel.setText("Create Order");
        createOrderCustomerComboBox.setValue(null);
        createOrderCustomerValidationLabel.setText("");
        orderLineValidationLabel.setText("");
        resetOrderLineInput();
        clearOrderDetails();
        setCreateCustomerPanelVisible(true);
        setDetailsVisible(true);
    }

    /**
     * Opens an existing order for viewing and editing.
     *
     * @param orderId order to open
     */
    public void showOrder(int orderId) {
        if (!isDataAccessAvailable()) {
            showError(
                    "Database unavailable",
                    "Order data access is not available.");
            returnToOrders();
            return;
        }

        if (orderId <= 0) {
            showError(
                    "Invalid order",
                    "The selected order ID is invalid.");
            returnToOrders();
            return;
        }

        currentOrderId = orderId;
        orderLineValidationLabel.setText("");
        setCreateCustomerPanelVisible(false);

        try {
            loadProducts();
            loadOrderDetails(orderId);
            setDetailsVisible(true);
        } catch (DaoException e) {
            showError(
                    "Could not open order",
                    "Order details could not be retrieved from the database.");
            returnToOrders();
        }
    }

    /** Returns to the main order view. */
    @FXML
    public void showOrders() {
        returnToOrders();
    }

    /** Hides the order details view and clears the active order reference. */
    public void hideDetails() {
        setDetailsVisible(false);
        currentOrderId = null;
    }

    /** Creates a new order for the selected customer. */
    @FXML
    public void handleCreateOrder() {
        if (!isDataAccessAvailable()) {
            createOrderCustomerValidationLabel.setText(
                    "Order data access is not available.");
            return;
        }

        Customer customer = createOrderCustomerComboBox.getValue();
        if (customer == null) {
            createOrderCustomerValidationLabel.setText(
                    "Select a customer before creating the order.");
            return;
        }

        try {
            currentOrderId = orderDao.createOrder(customer.getCustomerId());
            setCreateCustomerPanelVisible(false);
            createOrderCustomerValidationLabel.setText("");
            orderLineValidationLabel.setText(
                    "Order created. Products can now be added.");
            refreshAfterOrderChange();
        } catch (DaoException e) {
            createOrderCustomerValidationLabel.setText(
                    "The order could not be created.");
        }
    }

    /** Adds the selected product to the active order. */
    @FXML
    public void handleAddProductToOrder() {
        if (currentOrderId == null) {
            orderLineValidationLabel.setText(
                    "Create or open an order before adding a product.");
            return;
        }

        Product product = orderProductComboBox.getValue();
        if (product == null) {
            orderLineValidationLabel.setText("Select a product.");
            return;
        }

        OptionalInt lineNumber = parsePositiveInteger(
                orderLineNumberField.getText());
        if (lineNumber.isEmpty()) {
            orderLineValidationLabel.setText(
                    "Line number must be a positive whole number.");
            return;
        }

        try {
            orderDao.addProductToOrder(
                    currentOrderId,
                    product.getProductId(),
                    lineNumber.getAsInt(),
                    orderQuantitySpinner.getValue());

            resetOrderLineInput();
            orderLineValidationLabel.setText("");
            refreshAfterOrderChange();
        } catch (DaoException e) {
            orderLineValidationLabel.setText(
                    "The product could not be added. Check the line number and quantity.");
        }
    }

    private void configureOrderLineTable() {
        lineNumberColumn.setCellValueFactory(
                new PropertyValueFactory<>("lineNumber"));
        lineProductIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("productId"));
        lineProductNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("productName"));
        lineUnitPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("unitPrice"));
        lineQuantityColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantity"));
        lineTotalColumn.setCellValueFactory(
                new PropertyValueFactory<>("lineTotal"));

        formatMoneyColumn(lineUnitPriceColumn);
        formatMoneyColumn(lineTotalColumn);
        configureOrderLineActions();
    }

    private void configureOrderLineActions() {
        lineActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button updateButton = new Button("Update Qty");
            private final Button removeButton = new Button("Remove");
            private final HBox buttons = new HBox(8, updateButton, removeButton);

            {
                updateButton.getStyleClass().add("button-secondary");
                removeButton.getStyleClass().add("button-danger");
                buttons.setAlignment(Pos.CENTER_LEFT);

                updateButton.setOnAction(event -> updateQuantity(
                        getTableView().getItems().get(getIndex())));
                removeButton.setOnAction(event -> removeOrderLine(
                        getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private <T> void configureComboBox(
            ComboBox<T> comboBox,
            java.util.function.Function<T, String> formatter) {

        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(T value) {
                return value == null ? "" : formatter.apply(value);
            }

            @Override
            public T fromString(String value) {
                return comboBox.getValue();
            }
        });

        comboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(T value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null
                        ? null
                        : formatter.apply(value));
            }
        });
    }

    private void refreshReferenceData() {
        List<Customer> customers = customerDao.getAllCustomers();
        createOrderCustomerComboBox.setItems(
                FXCollections.observableArrayList(customers));
        loadProducts();
    }

    private void loadProducts() {
        orderProductComboBox.setItems(
                FXCollections.observableArrayList(productDao.getAllProducts()));
    }

    private void loadOrderDetails(int orderId) {
        Order order = Optional.ofNullable(orderDao.getOrderById(orderId))
                .orElseThrow(() -> new DaoException("Order does not exist."));

        orderDetailsTitleLabel.setText(
                "Order Details - #" + order.getOrderId());
        orderDetailsIdLabel.setText(String.valueOf(order.getOrderId()));
        orderDetailsDateLabel.setText(formatDate(order.getOrderDate()));
        orderDetailsCustomerIdLabel.setText(
                String.valueOf(order.getCustomerId()));
        orderDetailsCustomerNameLabel.setText(order.getCustomerName());
        orderDetailsLoyaltyLabel.setText(order.getLoyaltyName());
        orderSubtotalLabel.setText(formatMoney(order.getSubtotal()));
        orderDiscountLabel.setText(formatMoney(calculateDiscountAmount(order)));
        orderTotalLabel.setText(formatMoney(order.getOrderTotal()));

        orderLinesTable.setItems(
                FXCollections.observableArrayList(
                        orderDao.getOrderLines(orderId)));
        orderLinesTable.setPlaceholder(
                createTablePlaceholder(
                        "This order does not contain any products yet."));
    }

    private void refreshAfterOrderChange() {
        if (currentOrderId == null) {
            return;
        }

        loadOrderDetails(currentOrderId);
        if (orderController != null) {
            orderController.refreshOrdersAfterChange();
        }
    }

    private void updateQuantity(OrderLine orderLine) {
        TextInputDialog dialog = new TextInputDialog(
                String.valueOf(orderLine.getQuantity()));
        dialog.setTitle("Marvix - Orders");
        dialog.setHeaderText(
                "Update quantity for line " + orderLine.getLineNumber());
        dialog.setContentText("Quantity (1-100):");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        OptionalInt quantity = parsePositiveInteger(result.get());
        if (quantity.isEmpty()
                || quantity.getAsInt() > MAX_ORDER_QUANTITY) {
            showInformation(
                    "Invalid quantity",
                    "Quantity must be a whole number between 1 and 100.");
            return;
        }

        try {
            orderDao.updateOrderLineQuantity(
                    orderLine.getOrderId(),
                    orderLine.getLineNumber(),
                    quantity.getAsInt());
            refreshAfterOrderChange();
        } catch (DaoException e) {
            showError(
                    "Quantity could not be updated",
                    "The order line quantity could not be updated.");
        }
    }

    private void removeOrderLine(OrderLine orderLine) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Marvix - Orders");
        confirmation.setHeaderText("Remove product from order");
        confirmation.setContentText(
                "Remove line "
                        + orderLine.getLineNumber()
                        + " - "
                        + orderLine.getProductName()
                        + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            orderDao.removeProductFromOrder(
                    orderLine.getOrderId(),
                    orderLine.getLineNumber());
            refreshAfterOrderChange();
        } catch (DaoException e) {
            showError(
                    "Product could not be removed",
                    "The product could not be removed from the order.");
        }
    }

    // Parses a positive integer without using null as a control value.
    private OptionalInt parsePositiveInteger(String value) {
        try {
            int number = Integer.parseInt(value.strip());
            return number > 0
                    ? OptionalInt.of(number)
                    : OptionalInt.empty();
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    private void resetOrderLineInput() {
        orderProductComboBox.setValue(null);
        orderLineNumberField.clear();
        orderQuantitySpinner.getValueFactory().setValue(
                DEFAULT_ORDER_QUANTITY);
    }

    private void clearOrderDetails() {
        orderDetailsIdLabel.setText("-");
        orderDetailsDateLabel.setText("-");
        orderDetailsCustomerIdLabel.setText("-");
        orderDetailsCustomerNameLabel.setText("-");
        orderDetailsLoyaltyLabel.setText("-");
        orderSubtotalLabel.setText("-");
        orderDiscountLabel.setText("-");
        orderTotalLabel.setText("-");
        orderLinesTable.getItems().clear();
    }

    private void returnToOrders() {
        hideDetails();
        if (orderController != null) {
            orderController.showOrders();
        }
    }

    private Label createTablePlaceholder(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("table-placeholder");
        return label;
    }

    private boolean isDataAccessAvailable() {
        return orderDao != null
                && customerDao != null
                && productDao != null;
    }

    private void setDetailsVisible(boolean visible) {
        orderDetailsScreen.setVisible(visible);
        orderDetailsScreen.setManaged(visible);
    }

    private void setCreateCustomerPanelVisible(boolean visible) {
        createOrderCustomerPanel.setVisible(visible);
        createOrderCustomerPanel.setManaged(visible);
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "-" : date.format(DATE_FORMATTER);
    }

    private String formatMoney(BigDecimal value) {
        return value == null
                ? "-"
                : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal calculateDiscountAmount(Order order) {
        if (order.getSubtotal() == null
                || order.getOrderTotal() == null) {
            return BigDecimal.ZERO;
        }

        return order.getSubtotal().subtract(order.getOrderTotal());
    }

    private <T> void formatMoneyColumn(
            TableColumn<T, BigDecimal> column) {

        column.setCellFactory(tableColumn -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null
                        ? null
                        : formatMoney(value));
            }
        });
    }

    private void showInformation(String header, String message) {
        showAlert(Alert.AlertType.INFORMATION, header, message);
    }

    private void showError(String header, String message) {
        showAlert(Alert.AlertType.ERROR, header, message);
    }

    private void showAlert(
            Alert.AlertType type,
            String header,
            String message) {

        Alert alert = new Alert(type);
        alert.setTitle("Marvix - Orders");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
