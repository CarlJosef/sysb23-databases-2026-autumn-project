package se.lu.ics.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import se.lu.ics.data.CustomerDao;
import se.lu.ics.data.DaoException;
import se.lu.ics.models.Customer;

/**
 * Controller for the Marvix customer main view.
 *
 * Handles customer listing, search, removal and coordination with the
 * customer form and customer analysis controllers.
 */
public class CustomerController {

    private static final String ALERT_TITLE = "Marvix - Customers";
    private static final int CUSTOMER_ORDERS_TAB_INDEX = 0;

    private CustomerDao customerDao;

    @FXML
    private TextField customerSearchField;

    @FXML
    private TableView<Customer> customersTable;

    @FXML
    private TableColumn<Customer, Integer> customerIdColumn;

    @FXML
    private TableColumn<Customer, String> customerNameColumn;

    @FXML
    private TableColumn<Customer, String> customerAddressColumn;

    @FXML
    private TableColumn<Customer, String> customerEmailColumn;

    @FXML
    private TableColumn<Customer, String> customerLoyaltyColumn;

    @FXML
    private TableColumn<Customer, BigDecimal> customerDiscountColumn;

    @FXML
    private TableColumn<Customer, Void> customerActionsColumn;

    // Controllers injected from fx:include in CustomerView.fxml.
    @FXML
    private CustomerFormController customerFormController;

    @FXML
    private CustomerAnalysisController customerAnalysisController;

    // Configures the customer view and loads existing customers.
    @FXML
    private void initialize() {
        configureCustomerTable();
        customersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        customerSearchField.textProperty().addListener((observable, previousValue, currentValue) -> {
            if (currentValue != null
                    && currentValue.isBlank()
                    && previousValue != null
                    && !previousValue.isBlank()) {
                loadCustomers(false);
            }
        });

        try {
            customerDao = new CustomerDao();
            customerFormController.setCustomerDao(customerDao);
            customerFormController.setOnCustomerSaved(this::refreshAndSelectCustomer);
            customerAnalysisController.setCustomerDao(customerDao);
            loadCustomers(false);
        } catch (DaoException e) {
            customersTable.setPlaceholder(new Label("Customer data could not be loaded."));
        }
    }

    // Configures columns and actions in the customer table.
    private void configureCustomerTable() {
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerAddressColumn.setCellValueFactory(new PropertyValueFactory<>("customerAddress"));
        customerEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        customerLoyaltyColumn.setCellValueFactory(new PropertyValueFactory<>("loyaltyName"));
        customerDiscountColumn.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));

        customerDiscountColumn.setCellFactory(tableColumn -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal discount, boolean empty) {
                super.updateItem(discount, empty);
                setText(empty || discount == null
                        ? null
                        : discount.stripTrailingZeros().toPlainString() + " %");
            }
        });

        configureCustomerActions();
    }

    // Adds Edit and Delete actions to each populated customer row.
    private void configureCustomerActions() {
        customerActionsColumn.setCellFactory(tableColumn -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionButtons = new HBox(8, editButton, deleteButton);

            {
                editButton.getStyleClass().add("button-secondary");
                deleteButton.getStyleClass().add("button-danger");
                actionButtons.setAlignment(Pos.CENTER_LEFT);

                editButton.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    customerFormController.openEditCustomer(customer);
                });

                deleteButton.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    removeCustomer(customer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionButtons);
            }
        });
    }

    // Loads all customers through the Data Access Layer.
    private void loadCustomers(boolean showErrorMessage) {
        if (customerDao == null) {
            customersTable.setPlaceholder(new Label("Customer data access is not available."));

            if (showErrorMessage) {
                showError("Database unavailable", "Customer data access is not available.");
            }
            return;
        }

        try {
            List<Customer> customers = customerDao.getAllCustomers();
            customersTable.setItems(FXCollections.observableArrayList(customers));
            customersTable.getSelectionModel().clearSelection();
            customersTable.setPlaceholder(new Label("No customers found."));
        } catch (DaoException e) {
            customersTable.getItems().clear();
            customersTable.setPlaceholder(new Label("Customer data could not be loaded."));

            if (showErrorMessage) {
                showError(
                        "Could not load customers",
                        "The customer data could not be retrieved from the database.");
            }
        }
    }

    /**
     * Opens the form for creating a new customer.
     *
     * This method is public because MarvixMainController starts the same workflow.
     */
    @FXML
    public void handleNewCustomer() {
        customerFormController.openNewCustomer();
    }

    // Searches for a customer using the entered customer ID.
    @FXML
    private void handleSearchCustomer() {
        String searchValue = customerSearchField.getText().strip();

        if (searchValue.isBlank()) {
            loadCustomers(true);
            return;
        }

        OptionalInt customerId = parseCustomerId(searchValue);
        if (customerId.isEmpty()) {
            return;
        }

        findCustomerById(customerId.getAsInt(), true)
                .ifPresent(this::showSingleCustomer);
    }

    // Opens Customer Analysis on its first tab; the analysis view owns tab
    // switching.
    @FXML
    private void handleOpenCustomerAnalysis() {
        Optional<Customer> customer = resolveCustomerForAnalysis();
        customer.ifPresent(value -> customerAnalysisController.openAnalysis(
                value,
                CUSTOMER_ORDERS_TAB_INDEX));
    }

    // Removes a customer after explicit user confirmation.
    private void removeCustomer(Customer customer) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(ALERT_TITLE);
        confirmation.setHeaderText("Remove customer");
        confirmation.setContentText(
                "Remove customer "
                        + customer.getCustomerId()
                        + " - "
                        + customer.getCustomerName()
                        + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        if (customerDao == null) {
            showError("Database unavailable", "Customer data access is not available.");
            return;
        }

        try {
            customerDao.removeCustomer(customer.getCustomerId());
            customerSearchField.clear();
            loadCustomers(true);
        } catch (DaoException e) {
            showError(
                    "Customer could not be removed",
                    "The customer could not be removed. Existing orders may prevent deletion.");
        }
    }

    // Resolves the selected, searched or single displayed customer for analysis.
    private Optional<Customer> resolveCustomerForAnalysis() {
        Customer selectedCustomer = customersTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            return Optional.of(selectedCustomer);
        }

        String searchValue = customerSearchField.getText().strip();
        if (!searchValue.isBlank()) {
            OptionalInt customerId = parseCustomerId(searchValue);
            if (customerId.isEmpty()) {
                return Optional.empty();
            }

            Optional<Customer> customer = findCustomerById(customerId.getAsInt(), true);
            customer.ifPresent(this::showSingleCustomer);
            return customer;
        }

        if (customersTable.getItems().size() == 1) {
            return Optional.of(customersTable.getItems().get(0));
        }

        showInformation(
                "Select a customer",
                "Select a customer in the table or enter a Customer ID before opening Customer Analysis.");
        return Optional.empty();
    }

    // Parses a positive customer ID and reports invalid input to the user.
    private OptionalInt parseCustomerId(String value) {
        final int customerId;

        try {
            customerId = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            showInformation("Invalid Customer ID", "Enter a numeric Customer ID.");
            return OptionalInt.empty();
        }

        if (customerId <= 0) {
            showInformation("Invalid Customer ID", "Customer ID must be greater than zero.");
            return OptionalInt.empty();
        }

        return OptionalInt.of(customerId);
    }

    // Retrieves one customer through the existing Customer DAO without propagating
    // null.
    private Optional<Customer> findCustomerById(int customerId, boolean showNotFoundMessage) {
        if (customerDao == null) {
            showError("Database unavailable", "Customer data access is not available.");
            return Optional.empty();
        }

        try {
            Customer customer = customerDao.getCustomerById(customerId);

            if (customer == null) {
                if (showNotFoundMessage) {
                    customersTable.getItems().clear();
                    customersTable.setPlaceholder(
                            new Label("No customer found for Customer ID " + customerId + "."));
                    showInformation(
                            "Customer not found",
                            "No customer exists with Customer ID " + customerId + ".");
                }
                return Optional.empty();
            }

            return Optional.of(customer);
        } catch (DaoException e) {
            showError(
                    "Customer search failed",
                    "The customer could not be retrieved from the database.");
            return Optional.empty();
        }
    }

    // Displays and selects one customer in the main table.
    private void showSingleCustomer(Customer customer) {
        customersTable.setItems(FXCollections.observableArrayList(customer));
        customersTable.getSelectionModel().select(customer);
        customersTable.scrollTo(0);
    }

    // Refreshes the customer table and selects the customer that was saved.
    private void refreshAndSelectCustomer(int customerId) {
        if (!customerSearchField.getText().isBlank()) {
            customerSearchField.clear();
        }

        loadCustomers(true);

        for (Customer customer : customersTable.getItems()) {
            if (customer.getCustomerId() == customerId) {
                customersTable.getSelectionModel().select(customer);
                customersTable.scrollTo(customer);
                break;
            }
        }
    }

    // Shows an informational message to the user.
    private void showInformation(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(ALERT_TITLE);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Shows a user-friendly error message.
    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(ALERT_TITLE);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
