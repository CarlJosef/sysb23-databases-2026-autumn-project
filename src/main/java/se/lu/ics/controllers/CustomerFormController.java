package se.lu.ics.controllers;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import se.lu.ics.data.CustomerDao;
import se.lu.ics.data.DaoException;
import se.lu.ics.models.Customer;

/**
 * Controller for creating and editing customers.
 *
 * The same form is used for both new and existing customers.
 */
public class CustomerFormController {

    private static final String DEFAULT_LOYALTY_LEVEL = "Standard";
    private static final int MAX_CUSTOMER_NAME_LENGTH = 100;
    private static final int MAX_CUSTOMER_ADDRESS_LENGTH = 255;
    private static final int MAX_CUSTOMER_EMAIL_LENGTH = 100;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private CustomerDao customerDao;
    private IntConsumer onCustomerSaved;

    @FXML
    private StackPane customerDialogOverlay;

    @FXML
    private Label customerDialogTitleLabel;

    @FXML
    private TextField customerIdField;

    @FXML
    private TextField customerNameField;

    @FXML
    private TextField customerAddressField;

    @FXML
    private TextField customerEmailField;

    @FXML
    private ComboBox<String> customerLoyaltyComboBox;

    @FXML
    private Label customerValidationLabel;

    /**
     * Supplies the customer data access object used by the form.
     *
     * @param customerDao shared customer data access object
     */
    public void setCustomerDao(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    /**
     * Registers the callback invoked after a customer has been saved.
     *
     * @param onCustomerSaved callback receiving the saved customer ID
     */
    public void setOnCustomerSaved(IntConsumer onCustomerSaved) {
        this.onCustomerSaved = onCustomerSaved;
    }

    /**
     * Opens an empty form for creating a new customer.
     */
    public void openNewCustomer() {
        if (!loadLoyaltyLevels()) {
            return;
        }

        customerDialogTitleLabel.setText("New Customer");
        customerIdField.clear();
        customerNameField.clear();
        customerAddressField.clear();
        customerEmailField.clear();
        customerLoyaltyComboBox.setValue(getDefaultLoyaltyLevel());
        customerValidationLabel.setText("");

        setDialogVisible(true);
        customerNameField.requestFocus();
    }

    /**
     * Opens the form with the selected customer's current values.
     *
     * @param customer customer to edit
     */
    public void openEditCustomer(Customer customer) {
        if (customer == null) {
            showError(
                    "Customer unavailable",
                    "No customer was selected for editing.");
            return;
        }

        if (!loadLoyaltyLevels()) {
            return;
        }

        customerDialogTitleLabel.setText("Edit Customer");
        customerIdField.setText(String.valueOf(customer.getCustomerId()));
        customerNameField.setText(customer.getCustomerName());
        customerAddressField.setText(customer.getCustomerAddress());
        customerEmailField.setText(customer.getEmail());
        customerLoyaltyComboBox.setValue(customer.getLoyaltyName());
        customerValidationLabel.setText("");

        setDialogVisible(true);
        customerNameField.requestFocus();
    }

    // Closes the form without saving changes.
    @FXML
    private void handleCancelCustomer() {
        setDialogVisible(false);
    }

    // Validates and saves a new or existing customer.
    @FXML
    private void handleSaveCustomer() {
        String name = customerNameField.getText().strip();
        String address = customerAddressField.getText().strip();
        String email = customerEmailField.getText().strip();
        String loyaltyName = customerLoyaltyComboBox.getValue();

        if (!validateCustomerForm(name, address, email, loyaltyName)) {
            return;
        }

        if (customerDao == null) {
            showError(
                    "Database unavailable",
                    "Customer data access is not available.");
            return;
        }

        try {
            int savedCustomerId;

            if (customerIdField.getText().isBlank()) {
                savedCustomerId = customerDao.createCustomer(
                        name,
                        address,
                        email,
                        loyaltyName);
            } else {
                savedCustomerId = Integer.parseInt(customerIdField.getText());
                customerDao.updateCustomer(
                        savedCustomerId,
                        name,
                        address,
                        email,
                        loyaltyName);
            }

            setDialogVisible(false);

            if (onCustomerSaved != null) {
                onCustomerSaved.accept(savedCustomerId);
            }

        } catch (DaoException e) {
            customerValidationLabel.setText(
                    "The customer could not be saved. The email may already be in use or the entered information is invalid.");
        } catch (NumberFormatException e) {
            customerValidationLabel.setText("The customer ID is invalid.");
        }
    }

    // Validates form values before they are sent to the data access layer.
    private boolean validateCustomerForm(
            String name,
            String address,
            String email,
            String loyaltyName) {

        if (name.isBlank()
                || address.isBlank()
                || email.isBlank()
                || loyaltyName == null
                || loyaltyName.isBlank()) {
            customerValidationLabel.setText(
                    "Name, address, email and loyalty level are required.");
            return false;
        }

        if (name.length() > MAX_CUSTOMER_NAME_LENGTH) {
            customerValidationLabel.setText(
                    "Customer name must be at most 100 characters.");
            return false;
        }

        if (address.length() > MAX_CUSTOMER_ADDRESS_LENGTH) {
            customerValidationLabel.setText(
                    "Customer address must be at most 255 characters.");
            return false;
        }

        if (email.length() > MAX_CUSTOMER_EMAIL_LENGTH) {
            customerValidationLabel.setText(
                    "Email must be at most 100 characters.");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            customerValidationLabel.setText("Enter a valid email address.");
            return false;
        }

        customerValidationLabel.setText("");
        return true;
    }

    // Loads loyalty levels from the database so new levels appear without code
    // changes.
    private boolean loadLoyaltyLevels() {
        if (customerDao == null) {
            showError(
                    "Database unavailable",
                    "Customer data access is not available.");
            return false;
        }

        try {
            List<String> loyaltyLevelNames = customerDao.getLoyaltyLevelNames();

            if (loyaltyLevelNames.isEmpty()) {
                showError(
                        "Loyalty levels unavailable",
                        "No loyalty levels are registered in the database.");
                return false;
            }

            customerLoyaltyComboBox.getItems().setAll(loyaltyLevelNames);
            return true;

        } catch (DaoException e) {
            showError(
                    "Loyalty levels unavailable",
                    "The loyalty levels could not be loaded from the database.");
            return false;
        }
    }

    // Prefers Standard for new customers and falls back to the first level.
    private String getDefaultLoyaltyLevel() {
        List<String> loyaltyLevelNames = customerLoyaltyComboBox.getItems();

        if (loyaltyLevelNames.contains(DEFAULT_LOYALTY_LEVEL)) {
            return DEFAULT_LOYALTY_LEVEL;
        }

        return loyaltyLevelNames.get(0);
    }

    // Keeps visibility and layout participation synchronized for the overlay.
    private void setDialogVisible(boolean visible) {
        customerDialogOverlay.setVisible(visible);
        customerDialogOverlay.setManaged(visible);
    }

    // Shows a user-friendly error message.
    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Marvix - Customers");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
